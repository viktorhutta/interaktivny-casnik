package sk.hutta.server.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.hutta.server.dto.OrderItemRequest
import sk.hutta.server.dto.OrderRequest
import sk.hutta.server.model.*
import sk.hutta.server.repository.CustomerOrderRepository
import sk.hutta.server.repository.MenuItemRepository
import sk.hutta.server.repository.StaffUserRepository
import java.math.BigDecimal

@Service
@Transactional
class OrderService(
    private val menuItemRepository: MenuItemRepository,
    private val customerOrderRepository: CustomerOrderRepository,
    private val staffUserRepository: StaffUserRepository,
) {
    // Objednávku hosťa aj čašníka vytvára ten istý kód. Pri objednávke čašníka sa do histórie zapíše, kto ju zadal.
    fun createOrder(account: TableAccount, request: OrderRequest, staffId: Long? = null): CustomerOrder {
        if (account.status != TableAccountStatus.AWAITING_CONFIRMATION && account.status != TableAccountStatus.OPEN) {
            throw conflict("Účet už neprijíma objednávky")
        }
        if (request.items.isEmpty()) throw badRequest("Objednávka je prázdna")

        val order = CustomerOrder(account)
        for (itemRequest in request.items) {
            order.items.add(createOrderItem(order, itemRequest))
        }
        val staffUser = staffId?.let { staffUserRepository.getReferenceById(it) }
        order.statusChanges.add(OrderStatusChange(order, OrderStatus.NEW, staffUser = staffUser))
        return customerOrderRepository.save(order)
    }

    // Názov a ceny sa kopírujú z menu, aby neskoršia zmena menu nezmenila objednávku.
    private fun createOrderItem(order: CustomerOrder, request: OrderItemRequest): OrderItem {
        if (request.quantity !in 1..50) throw badRequest("Neplatný počet kusov")
        val menuItem = menuItemRepository.findById(request.menuItemId).orElseThrow { notFound() }
        if (!menuItem.isOrderable()) throw badRequest("Položka ${menuItem.name} nie je dostupná")
        val orderItem = OrderItem(order, menuItem, menuItem.name, request.quantity, menuItem.price)

        for (ingredientId in request.removedIngredientIds.toSet()) {
            val link = menuItem.ingredients.find { it.ingredient.id == ingredientId && it.type == IngredientType.BASE && it.removable }
                ?: throw badRequest("Ingredienciu nemožno odobrať")
            orderItem.ingredients.add(OrderItemIngredient(orderItem, link.ingredient, ChangeType.REMOVED, link.ingredient.name, BigDecimal.ZERO))
        }
        for (ingredientId in request.addedIngredientIds.toSet()) {
            val link = menuItem.ingredients.find { it.ingredient.id == ingredientId && it.type == IngredientType.EXTRA && it.ingredient.isUsable() }
                ?: throw badRequest("Ingredienciu nemožno pridať")
            orderItem.ingredients.add(OrderItemIngredient(orderItem, link.ingredient, ChangeType.ADDED, link.ingredient.name, link.extraPrice!!))
        }
        return orderItem
    }
}
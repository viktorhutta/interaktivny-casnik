package sk.hutta.server.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.hutta.server.dto.KitchenOrderDto
import sk.hutta.server.model.CustomerOrder
import sk.hutta.server.model.OrderStatus
import sk.hutta.server.repository.CustomerOrderRepository

@Service
@Transactional
class KitchenService(
    private val customerOrderRepository: CustomerOrderRepository,
    private val orderStatusService: OrderStatusService,
) {
    @Transactional(readOnly = true)
    fun getOrders() = customerOrderRepository.findForKitchen().map { it.toKitchenDto() }

    // Kuchyňa smie objednávku len začať pripravovať a označiť ako hotovú.
    fun changeStatus(orderId: Long, status: OrderStatus, staffId: Long): KitchenOrderDto {
        if (status != OrderStatus.IN_PREPARATION && status != OrderStatus.READY) throw forbidden("Tento stav kuchyňa nenastavuje")
        return orderStatusService.changeStatus(orderId, status, staffId).toKitchenDto()
    }
}

private fun CustomerOrder.toKitchenDto(): KitchenOrderDto {
    val order = toDto()
    return KitchenOrderDto(order.id, tableAccount.diningTable.label, order.status, statusChanges.first().changedAt, order.items)
}
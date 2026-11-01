package sk.hutta.server.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.hutta.server.model.*
import sk.hutta.server.repository.CustomerOrderRepository
import sk.hutta.server.repository.StaffUserRepository

@Service
@Transactional
class OrderStatusService(
    private val customerOrderRepository: CustomerOrderRepository,
    private val staffUserRepository: StaffUserRepository,
) {
    // Riadok objednávky sa zamkne, aby dve súčasné zmeny nepreskočili stav.
    fun changeStatus(orderId: Long, status: OrderStatus, staffId: Long): CustomerOrder {
        val order = customerOrderRepository.findWithLockById(orderId) ?: throw notFound()
        if (order.tableAccount.status == TableAccountStatus.AWAITING_CONFIRMATION) throw conflict("Účet stola ešte nie je potvrdený")
        if (order.currentStatus().next() != status) throw conflict("Objednávka je v inom stave")
        order.statusChanges.add(OrderStatusChange(order, status, staffUser = staffUserRepository.getReferenceById(staffId)))
        return order
    }
}

// Objednávka prechádza stavmi len v tomto poradí.
fun OrderStatus.next(): OrderStatus? = when (this) {
    OrderStatus.NEW -> OrderStatus.IN_PREPARATION
    OrderStatus.IN_PREPARATION -> OrderStatus.READY
    OrderStatus.READY -> OrderStatus.DELIVERED
    OrderStatus.DELIVERED -> null
}
package sk.hutta.server.dto

import sk.hutta.server.model.OrderStatus
import sk.hutta.server.model.PaymentMethod
import sk.hutta.server.model.TableAccountStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderStatusRequest(val status: OrderStatus)

data class KitchenOrderDto(val id: Long, val tableLabel: String, val status: OrderStatus, val createdAt: LocalDateTime, val items: List<OrderItemDto>)

data class WaiterTableDto(val accountId: Long, val tableLabel: String, val status: TableAccountStatus, val paymentMethod: PaymentMethod?, val total: BigDecimal, val waiterCallId: Long?, val readyOrders: List<OrderDto>)

data class DiningTableDto(val id: Long, val label: String)

data class AdminDiningTableDto(val id: Long, val label: String, val token: String)

data class DiningTableRequest(val label: String)
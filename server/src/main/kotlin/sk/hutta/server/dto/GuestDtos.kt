package sk.hutta.server.dto

import sk.hutta.server.model.ChangeType
import sk.hutta.server.model.OrderStatus
import sk.hutta.server.model.TableAccountStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class TableSessionRequest(val tableId: Long, val token: String)

data class TableSessionDto(val accountId: Long, val tableLabel: String, val status: TableAccountStatus)

data class OrderRequest(val items: List<OrderItemRequest>)

data class OrderItemRequest(val menuItemId: Long, val quantity: Int, val removedIngredientIds: List<Long> = emptyList(), val addedIngredientIds: List<Long> = emptyList())

data class AccountDto(val id: Long, val tableLabel: String, val status: TableAccountStatus, val waiterCalled: Boolean, val total: BigDecimal, val orders: List<OrderDto>)

data class OrderDto(val id: Long, val status: OrderStatus, val total: BigDecimal, val items: List<OrderItemDto>, val history: List<StatusChangeDto>)

data class OrderItemDto(val name: String, val quantity: Int, val price: BigDecimal, val ingredients: List<OrderItemIngredientDto>)

data class OrderItemIngredientDto(val name: String, val changeType: ChangeType, val price: BigDecimal)

data class StatusChangeDto(val status: OrderStatus, val changedAt: LocalDateTime)
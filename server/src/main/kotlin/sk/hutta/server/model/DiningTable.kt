package sk.hutta.server.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class DiningTable(
    var label: String,
    var token: String,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
)

enum class TableAccountStatus { AWAITING_CONFIRMATION, OPEN, AWAITING_PAYMENT, CLOSED }

enum class PaymentMethod { CARD, AT_WAITER }

@Entity
class TableAccount(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var diningTable: DiningTable,
    @Enumerated(EnumType.STRING)
    var status: TableAccountStatus,
    @Enumerated(EnumType.STRING)
    var paymentMethod: PaymentMethod? = null,
    var gatewayPaymentId: Long? = null,
    var createdAt: LocalDateTime = LocalDateTime.now(),
    var closedAt: LocalDateTime? = null,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
)

@Entity
class WaiterCall(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var tableAccount: TableAccount,
    var createdAt: LocalDateTime = LocalDateTime.now(),
    var resolved: Boolean = false,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
)
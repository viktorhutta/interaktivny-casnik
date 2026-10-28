package sk.hutta.server.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

enum class OrderStatus { NEW, IN_PREPARATION, READY, DELIVERED }

enum class ChangeType { REMOVED, ADDED }

@Entity
class CustomerOrder(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var tableAccount: TableAccount,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
) {
    @OneToMany(mappedBy = "customerOrder", cascade = [CascadeType.ALL])
    var items: MutableList<OrderItem> = mutableListOf()

    @OneToMany(mappedBy = "customerOrder", cascade = [CascadeType.ALL])
    @OrderBy("changedAt")
    var statusChanges: MutableList<OrderStatusChange> = mutableListOf()
}

@Entity
class OrderItem(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var customerOrder: CustomerOrder,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var menuItem: MenuItem,
    var name: String,
    var quantity: Int,
    var price: BigDecimal,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
) {
    @OneToMany(mappedBy = "orderItem", cascade = [CascadeType.ALL])
    var ingredients: MutableList<OrderItemIngredient> = mutableListOf()
}

@Entity
class OrderItemIngredient(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var orderItem: OrderItem,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var ingredient: Ingredient,
    @Enumerated(EnumType.STRING)
    var changeType: ChangeType,
    var name: String,
    var price: BigDecimal,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
)

@Entity
class OrderStatusChange(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var customerOrder: CustomerOrder,
    @Enumerated(EnumType.STRING)
    var status: OrderStatus,
    var changedAt: LocalDateTime = LocalDateTime.now(),
    @ManyToOne(fetch = FetchType.LAZY)
    var staffUser: StaffUser? = null,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
)
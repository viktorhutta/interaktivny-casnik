package sk.hutta.server.model

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
class Category(
    var name: String,
    var displayOrder: Int,
    var archived: Boolean = false,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
)

@Entity
class MenuItem(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var category: Category,
    var name: String,
    var description: String,
    var price: BigDecimal,
    var available: Boolean = true,
    var archived: Boolean = false,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
) {
    @OneToMany(mappedBy = "menuItem")
    var ingredients: MutableList<MenuItemIngredient> = mutableListOf()
}

@Entity
class Ingredient(
    var name: String,
    var available: Boolean = true,
    var archived: Boolean = false,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
)

enum class IngredientType { BASE, EXTRA }

@Entity
class MenuItemIngredient(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var menuItem: MenuItem,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var ingredient: Ingredient,
    @Enumerated(EnumType.STRING)
    var type: IngredientType,
    var removable: Boolean,
    var extraPrice: BigDecimal? = null,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
)
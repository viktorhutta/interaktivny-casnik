package sk.hutta.server

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import sk.hutta.server.model.*
import sk.hutta.server.service.next
import sk.hutta.server.service.total
import java.math.BigDecimal

// Jednotkové testy: bez Springu a bez databázy, len samotné výpočty.
class OrderLogicTest {

    private val order = CustomerOrder(TableAccount(DiningTable("1", "token"), TableAccountStatus.OPEN))
    private val pizza = MenuItem(Category("Jedlá", 1), "Pizza", "", BigDecimal("9.00"))

    @Test
    fun `stav objednávky sa posúva len o jeden krok dopredu`() {
        assertEquals(OrderStatus.IN_PREPARATION, OrderStatus.NEW.next())
        assertEquals(OrderStatus.READY, OrderStatus.IN_PREPARATION.next())
        assertEquals(OrderStatus.DELIVERED, OrderStatus.READY.next())
        assertNull(OrderStatus.DELIVERED.next())
    }

    @Test
    fun `cena položky zahŕňa pridané ingrediencie a počet kusov`() {
        val item = OrderItem(order, pizza, "Pizza", 2, BigDecimal("9.00"))
        item.ingredients.add(OrderItemIngredient(item, Ingredient("slanina"), ChangeType.ADDED, "slanina", BigDecimal("1.20")))
        item.ingredients.add(OrderItemIngredient(item, Ingredient("cibuľa"), ChangeType.REMOVED, "cibuľa", BigDecimal.ZERO))

        assertEquals(BigDecimal("20.40"), item.total())
    }

    @Test
    fun `suma objednávky je súčet jej položiek`() {
        order.items.add(OrderItem(order, pizza, "Pizza", 1, BigDecimal("9.00")))
        order.items.add(OrderItem(order, pizza, "Kofola", 3, BigDecimal("2.20")))

        assertEquals(BigDecimal("15.60"), order.items.sumOf { it.total() })
    }
}
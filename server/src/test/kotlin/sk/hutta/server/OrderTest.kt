package sk.hutta.server

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import sk.hutta.server.support.IntegrationTest

class OrderTest : IntegrationTest() {

    private fun kitchenOrderIds() = read<List<Number>>(get("/api/kitchen/orders", kitchenToken()), "$[*].id").map { it.toLong() }

    @Test
    fun `objednávku nepotvrdeného účtu server prijme, kuchyňa ju uvidí až po potvrdení`() {
        val guest = guestSession(newTable())
        val orderId = readId(post("/api/guest/account/orders", guest, orderJson(newMenuItem("8.50"))))
        assertFalse(orderId in kitchenOrderIds())

        post("/api/waiter/accounts/${accountId(guest)}/confirm", waiterToken())
        assertTrue(orderId in kitchenOrderIds())
    }

    @Test
    fun `ďalší hosť pri stole sa pripojí k tomu istému účtu`() {
        val table = newTable()
        assertEquals(accountId(guestSession(table)), accountId(guestSession(table)))
    }

    @Test
    fun `objednávka nedostupnej položky je odmietnutá`() {
        val guest = guestWithOrder()
        post("/api/guest/account/orders", guest.token, orderJson(newMenuItem("5.00", available = false)), expectedStatus = 400)
    }

    @Test
    fun `objednávka na účet čakajúci na platbu je odmietnutá`() {
        val guest = guestWithOrder()
        post("/api/guest/account/payment", guest.token, """{"method": "AT_WAITER"}""")
        post("/api/guest/account/orders", guest.token, orderJson(newMenuItem("1.00")), expectedStatus = 409)
    }

    @Test
    fun `suma účtu je súčet všetkých objednávok`() {
        val guest = guestWithOrder("8.50")
        post("/api/guest/account/orders", guest.token, orderJson(newMenuItem("2.20"), quantity = 3))
        assertEquals(15.1, read<Number>(get("/api/guest/account", guest.token), "$.total").toDouble())
    }

    @Test
    fun `zmena ceny v menu nezmení vytvorenú objednávku`() {
        val admin = adminToken()
        val categoryId = readId(post("/api/admin/categories", admin, """{"name": "Test", "displayOrder": 99}"""))
        val menuItemId = readId(post("/api/admin/menu-items", admin, menuItemJson(categoryId, "8.50", true)))
        val guest = guestWithOrder()
        post("/api/guest/account/orders", guest.token, orderJson(menuItemId))

        put("/api/admin/menu-items/$menuItemId", admin, menuItemJson(categoryId, "12.00", true))

        val account = get("/api/guest/account", guest.token)
        assertEquals(8.5, read<Number>(account, "$.orders[1].items[0].price").toDouble())
        assertEquals(17.0, read<Number>(account, "$.total").toDouble())
    }

    @Test
    fun `objednávka prechádza stavmi len v povolenom poradí`() {
        val guest = guestWithOrder()
        val kitchen = kitchenToken()
        val url = "/api/kitchen/orders/${guest.orderId}/status"

        post(url, kitchen, """{"status": "READY"}""", expectedStatus = 409)
        post(url, kitchen, """{"status": "IN_PREPARATION"}""")
        post(url, kitchen, """{"status": "DELIVERED"}""", expectedStatus = 403)
        post(url, kitchen, """{"status": "READY"}""")
        post("/api/waiter/orders/${guest.orderId}/delivered", waiterToken())
        post("/api/waiter/orders/${guest.orderId}/delivered", waiterToken(), expectedStatus = 409)

        val history = read<List<String>>(get("/api/guest/account", guest.token), "$.orders[0].history[*].status")
        assertEquals(listOf("NEW", "IN_PREPARATION", "READY", "DELIVERED"), history)
    }

    @Test
    fun `čašník objedná za hosťa na stôl bez účtu a účet je rovno otvorený`() {
        val table = newTable()
        post("/api/waiter/dining-tables/${table.id}/orders", waiterToken(), orderJson(newMenuItem("4.00")))

        val account = get("/api/guest/account", guestSession(table))
        assertEquals("OPEN", read(account, "$.status"))
        assertEquals(1, read<List<Any>>(account, "$.orders").size)
    }

    @Test
    fun `objednávka čašníka potvrdí nepotvrdený účet`() {
        val table = newTable()
        val guest = guestSession(table)
        post("/api/waiter/dining-tables/${table.id}/orders", waiterToken(), orderJson(newMenuItem("4.00")))
        assertEquals("OPEN", read(get("/api/guest/account", guest), "$.status"))
    }
}
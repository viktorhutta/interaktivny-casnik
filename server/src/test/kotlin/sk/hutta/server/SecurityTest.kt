package sk.hutta.server

import org.junit.jupiter.api.Test
import sk.hutta.server.support.IntegrationTest

class SecurityTest : IntegrationTest() {

    @Test
    fun `požiadavka bez tokenu alebo s neplatným tokenom je odmietnutá`() {
        get("/api/menu", expectedStatus = 401)
        get("/api/guest/account", expectedStatus = 401)
        get("/api/waiter/tables", "neplatny.token", expectedStatus = 401)
    }

    @Test
    fun `prihlásenie s nesprávnym heslom je odmietnuté`() {
        post("/api/auth/login", body = """{"username": "casnik", "password": "zle"}""", expectedStatus = 401)
    }

    @Test
    fun `hosť nesmie na adresy personálu`() {
        val guest = guestSession(newTable())
        get("/api/waiter/tables", guest, expectedStatus = 403)
        get("/api/kitchen/orders", guest, expectedStatus = 403)
        get("/api/admin/dining-tables", guest, expectedStatus = 403)
    }

    @Test
    fun `personál smie len na adresy svojej roly`() {
        get("/api/admin/dining-tables", waiterToken(), expectedStatus = 403)
        get("/api/kitchen/orders", waiterToken(), expectedStatus = 403)
        get("/api/waiter/tables", kitchenToken(), expectedStatus = 403)
        get("/api/guest/account", adminToken(), expectedStatus = 403)
    }

    @Test
    fun `neplatný token stola alebo neexistujúci stôl je odmietnutý`() {
        val table = newTable()
        post("/api/guest/session", body = """{"tableId": ${table.id}, "token": "zly-token"}""", expectedStatus = 404)
        post("/api/guest/session", body = """{"tableId": 999999, "token": "${table.token}"}""", expectedStatus = 404)
    }

    @Test
    fun `starý token stola po pregenerovaní neplatí`() {
        val table = newTable()
        post("/api/admin/dining-tables/${table.id}/token", adminToken())
        post("/api/guest/session", body = """{"tableId": ${table.id}, "token": "${table.token}"}""", expectedStatus = 404)
    }

    @Test
    fun `token hosťa po uzavretí účtu neplatí`() {
        val guest = guestWithOrder()
        post("/api/waiter/accounts/${guest.accountId}/close", waiterToken())
        get("/api/guest/account", guest.token, expectedStatus = 403)
        post("/api/guest/account/orders", guest.token, orderJson(newMenuItem("1.00")), expectedStatus = 403)
    }
}
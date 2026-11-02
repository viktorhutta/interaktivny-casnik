package sk.hutta.server.support

import com.jayway.jsonpath.JsonPath
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

// Spoločný základ integračných testov: celý server so Spring Security nad testovacou databázou.
// Každý test si zakladá vlastný stôl a vlastné položky menu, takže testy sa navzájom neovplyvňujú.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig::class)
abstract class IntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var finby: FakeFinbyClient

    // Pošle požiadavku, overí kód odpovede a vráti jej telo.
    fun call(method: HttpMethod, url: String, token: String? = null, body: String? = null, expectedStatus: Int = 200): String {
        val request = MockMvcRequestBuilders.request(method, url)
        if (token != null) request.header("Authorization", "Bearer $token")
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(body)
        return mockMvc.perform(request)
            .andExpect(status().`is`(expectedStatus))
            .andReturn().response.contentAsString
    }

    fun get(url: String, token: String? = null, expectedStatus: Int = 200) = call(HttpMethod.GET, url, token, null, expectedStatus)

    fun post(url: String, token: String? = null, body: String? = null, expectedStatus: Int = 200) = call(HttpMethod.POST, url, token, body, expectedStatus)

    fun put(url: String, token: String? = null, body: String? = null, expectedStatus: Int = 200) = call(HttpMethod.PUT, url, token, body, expectedStatus)

    // Vyberie hodnotu z JSON odpovede, napríklad read(json, "$.status").
    fun <T> read(json: String, path: String): T = JsonPath.read(json, path)

    fun readId(json: String, path: String = "$.id") = read<Number>(json, path).toLong()

    // Pomocné kroky, z ktorých sa skladajú scenáre

    fun login(username: String) = read<String>(post("/api/auth/login", body = """{"username": "$username", "password": "casnik2026"}"""), "$.token")

    fun adminToken() = login("spravca")

    fun waiterToken() = login("casnik")

    fun kitchenToken() = login("kuchyna")

    fun newTable(): Table {
        val json = post("/api/admin/dining-tables", adminToken(), """{"label": "T${System.nanoTime() % 1_000_000_000}"}""")
        return Table(readId(json), read(json, "$.token"))
    }

    fun newMenuItem(price: String, available: Boolean = true): Long {
        val admin = adminToken()
        val categoryId = readId(post("/api/admin/categories", admin, """{"name": "Test", "displayOrder": 99}"""))
        return readId(post("/api/admin/menu-items", admin, menuItemJson(categoryId, price, available)))
    }

    fun menuItemJson(categoryId: Long, price: String, available: Boolean) =
        """{"categoryId": $categoryId, "name": "Testovacie jedlo", "description": "", "price": $price, "available": $available, "ingredients": []}"""

    fun guestSession(table: Table) = read<String>(post("/api/guest/session", body = """{"tableId": ${table.id}, "token": "${table.token}"}"""), "$.token")

    fun accountId(guestToken: String) = readId(get("/api/guest/account", guestToken))

    fun orderJson(menuItemId: Long, quantity: Int = 1) = """{"items": [{"menuItemId": $menuItemId, "quantity": $quantity}]}"""

    // Stôl s potvrdeným účtom, na ktorom má hosť jednu objednávku.
    fun guestWithOrder(price: String = "8.50"): Guest {
        val table = newTable()
        val token = guestSession(table)
        val accountId = accountId(token)
        post("/api/waiter/accounts/$accountId/confirm", waiterToken())
        val orderId = readId(post("/api/guest/account/orders", token, orderJson(newMenuItem(price))))
        return Guest(table, token, accountId, orderId)
    }

    data class Table(val id: Long, val token: String)

    data class Guest(val table: Table, val token: String, val accountId: Long, val orderId: Long)
}
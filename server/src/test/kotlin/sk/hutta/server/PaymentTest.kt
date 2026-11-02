package sk.hutta.server

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import sk.hutta.server.model.PaymentMethod
import sk.hutta.server.model.TableAccountStatus
import sk.hutta.server.payment.CardPaymentMonitor
import sk.hutta.server.payment.CardPaymentWatcher
import sk.hutta.server.payment.GatewayPaymentStatus
import sk.hutta.server.repository.TableAccountRepository
import sk.hutta.server.service.PaymentService
import sk.hutta.server.support.IntegrationTest
import java.math.BigDecimal

class PaymentTest : IntegrationTest() {

    @Autowired
    lateinit var tableAccountRepository: TableAccountRepository

    @Autowired
    lateinit var cardPaymentMonitor: CardPaymentMonitor

    @Autowired
    lateinit var paymentService: PaymentService

    // Sledovanie platieb je v testoch vypnuté, test ho spustí sám v presne určenej chvíli.
    private fun runWatcher() = CardPaymentWatcher(tableAccountRepository, cardPaymentMonitor, paymentService).checkPayments()

    // Hosť zvolí platbu kartou, vráti id platby v simulovanej bráne.
    private fun payByCard(guest: Guest): Long {
        val json = post("/api/guest/account/payment", guest.token, """{"method": "CARD"}""")
        assertEquals("AWAITING_PAYMENT", read(json, "$.status"))
        return read<String>(json, "$.gatewayUrl").substringAfterLast("/").toLong()
    }

    private fun account(guest: Guest) = tableAccountRepository.findById(guest.accountId).get()

    @Test
    fun `po úspešnej platbe kartou server účet uzavrie sám`() {
        val guest = guestWithOrder("8.50")
        val paymentId = payByCard(guest)
        assertEquals(BigDecimal("8.50"), finby.amounts[paymentId])

        finby.statuses[paymentId] = GatewayPaymentStatus.PAID
        runWatcher()

        assertEquals(TableAccountStatus.CLOSED, account(guest).status)
        assertEquals(PaymentMethod.CARD, account(guest).paymentMethod)
    }

    @Test
    fun `po zamietnutej platbe účet čaká na platbu a hosť môže platiť znova`() {
        val guest = guestWithOrder()
        val paymentId = payByCard(guest)
        finby.statuses[paymentId] = GatewayPaymentStatus.REJECTED
        runWatcher()

        assertEquals(TableAccountStatus.AWAITING_PAYMENT, account(guest).status)
        assertNotEquals(paymentId, payByCard(guest))
    }

    @Test
    fun `čašník nemôže uzavrieť účet počas nedokončenej platby kartou`() {
        val guest = guestWithOrder()
        payByCard(guest)
        post("/api/waiter/accounts/${guest.accountId}/close", waiterToken(), expectedStatus = 409)
        assertEquals(TableAccountStatus.AWAITING_PAYMENT, account(guest).status)
    }

    @Test
    fun `po zmene na platbu u čašníka čašník účet uzavrie`() {
        val guest = guestWithOrder()
        payByCard(guest)
        post("/api/guest/account/payment", guest.token, """{"method": "AT_WAITER"}""")

        val json = post("/api/waiter/accounts/${guest.accountId}/close", waiterToken())
        assertEquals("AT_WAITER", read(json, "$.paymentMethod"))
    }

    @Test
    fun `čašník uzavrie účet zaplatený kartou ako platbu kartou`() {
        val guest = guestWithOrder()
        finby.statuses[payByCard(guest)] = GatewayPaymentStatus.PAID

        val json = post("/api/waiter/accounts/${guest.accountId}/close", waiterToken())
        assertEquals("CARD", read(json, "$.paymentMethod"))
    }

    @Test
    fun `hosť nemôže platiť za nepotvrdený ani prázdny účet`() {
        val table = newTable()
        val guest = guestSession(table)
        post("/api/guest/account/payment", guest, """{"method": "CARD"}""", expectedStatus = 409)

        post("/api/waiter/accounts/${accountId(guest)}/confirm", waiterToken())
        post("/api/guest/account/payment", guest, """{"method": "CARD"}""", expectedStatus = 409)
    }

    @Test
    fun `hosť nemôže účet uzavrieť sám`() {
        val guest = guestWithOrder()
        post("/api/waiter/accounts/${guest.accountId}/close", guest.token, expectedStatus = 403)
    }
}
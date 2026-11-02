package sk.hutta.server.support

import sk.hutta.server.payment.CreatedPayment
import sk.hutta.server.payment.FinbyClient
import sk.hutta.server.payment.GatewayPaymentStatus
import java.math.BigDecimal

// Simulovaná brána: platby si pamätá v mape a ich stav nastavuje test.
class FakeFinbyClient : FinbyClient("http://localhost", "1", "test") {

    private var nextPaymentId = 1000L
    val amounts = mutableMapOf<Long, BigDecimal>()
    val statuses = mutableMapOf<Long, GatewayPaymentStatus>()

    override fun createPayment(amount: BigDecimal, reference: String): CreatedPayment {
        val paymentId = nextPaymentId++
        amounts[paymentId] = amount
        statuses[paymentId] = GatewayPaymentStatus.PENDING
        return CreatedPayment(paymentId, "https://gateway.test/$paymentId")
    }

    override fun getPaymentStatus(paymentRequestId: Long) = statuses[paymentRequestId]
}
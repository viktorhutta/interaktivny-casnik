package sk.hutta.server.payment

import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

// Všetky dopyty na stav platby idú cez túto triedu, aby server neprekročil limity brány.
@Component
class CardPaymentMonitor(private val finbyClient: FinbyClient) {

    private class Check(val firstSeen: Instant) {
        var checkedAt: Instant? = null
        var status: GatewayPaymentStatus? = null
    }

    private val checks = mutableMapOf<Long, Check>()

    // Brána dovolí pýtať sa na platbu prvých 15 minút raz za 5 s, potom raz za minútu.
    // Kto sa pýta skôr, dostane posledný zistený stav. Konečný stav sa už znova nezisťuje.
    @Synchronized
    fun status(paymentId: Long): GatewayPaymentStatus? {
        val now = Instant.now()
        val check = checks.getOrPut(paymentId) { Check(now) }
        if (check.status == GatewayPaymentStatus.PAID || check.status == GatewayPaymentStatus.REJECTED) return check.status
        val interval = if (Duration.between(check.firstSeen, now) < Duration.ofMinutes(15)) Duration.ofSeconds(5) else Duration.ofMinutes(1)
        val checkedAt = check.checkedAt
        if (checkedAt == null || Duration.between(checkedAt, now) >= interval) {
            check.checkedAt = now
            check.status = finbyClient.getPaymentStatus(paymentId) ?: check.status
        }
        return check.status
    }

    // Server sleduje platbu najviac 30 minút. Ak by sa server reštartoval, sleduje ju znova od začiatku.
    @Synchronized
    fun isWatched(paymentId: Long): Boolean {
        val check = checks.getOrPut(paymentId) { Check(Instant.now()) }
        return Duration.between(check.firstSeen, Instant.now()) < Duration.ofMinutes(30)
    }

    @Synchronized
    fun forgetAllExcept(paymentIds: Set<Long>) {
        checks.keys.retainAll(paymentIds)
    }
}
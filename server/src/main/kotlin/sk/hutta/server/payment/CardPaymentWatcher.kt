package sk.hutta.server.payment

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import sk.hutta.server.model.TableAccountStatus
import sk.hutta.server.repository.TableAccountRepository
import sk.hutta.server.service.PaymentService

// Každých 5 s pozrie platby kartou, na ktoré sa čaká, a zaplatený účet uzavrie bez čašníka.
@Component
class CardPaymentWatcher(
    private val tableAccountRepository: TableAccountRepository,
    private val cardPaymentMonitor: CardPaymentMonitor,
    private val paymentService: PaymentService,
) {
    @Scheduled(fixedDelay = 5000)
    fun checkPayments() {
        val accounts = tableAccountRepository.findByStatusAndGatewayPaymentIdNotNull(TableAccountStatus.AWAITING_PAYMENT)
        cardPaymentMonitor.forgetAllExcept(accounts.map { it.gatewayPaymentId!! }.toSet())
        for (account in accounts) {
            val paymentId = account.gatewayPaymentId!!
            if (cardPaymentMonitor.isWatched(paymentId) && cardPaymentMonitor.status(paymentId) == GatewayPaymentStatus.PAID) {
                paymentService.closePaidAccount(account.id!!, paymentId)
            }
        }
    }
}
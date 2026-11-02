package sk.hutta.server.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.hutta.server.dto.PaymentDto
import sk.hutta.server.model.PaymentMethod
import sk.hutta.server.model.TableAccount
import sk.hutta.server.model.TableAccountStatus
import sk.hutta.server.payment.CardPaymentMonitor
import sk.hutta.server.payment.FinbyClient
import sk.hutta.server.payment.GatewayPaymentStatus
import sk.hutta.server.repository.CustomerOrderRepository
import sk.hutta.server.repository.TableAccountRepository
import java.time.LocalDateTime

@Service
@Transactional
class PaymentService(
    private val tableAccountRepository: TableAccountRepository,
    private val customerOrderRepository: CustomerOrderRepository,
    private val finbyClient: FinbyClient,
    private val cardPaymentMonitor: CardPaymentMonitor,
) {
    // Pri karte server vytvorí platbu v bráne a aplikácia otvorí jej platobnú stránku.
    // Každý pokus o platbu kartou je nová platba v bráne, server sleduje len poslednú.
    fun choosePayment(accountId: Long, method: PaymentMethod): PaymentDto {
        val account = tableAccountRepository.findWithLockById(accountId) ?: throw notFound()
        if (account.status == TableAccountStatus.CLOSED) throw forbidden("Účet je uzavretý")
        if (account.status == TableAccountStatus.AWAITING_CONFIRMATION) throw conflict("Účet ešte nie je potvrdený")
        val total = customerOrderRepository.findByTableAccountOrderById(account).flatMap { it.items }.sumOf { it.total() }
        if (total.signum() == 0) throw conflict("Účet je prázdny")

        // Predchádzajúci pokus o platbu kartou mohol medzitým prejsť.
        if (account.gatewayPaymentId?.let { cardPaymentMonitor.status(it) } == GatewayPaymentStatus.PAID) {
            close(account, PaymentMethod.CARD)
            return account.toPaymentDto()
        }
        account.status = TableAccountStatus.AWAITING_PAYMENT
        account.paymentMethod = method
        if (method == PaymentMethod.AT_WAITER) return account.toPaymentDto()

        val payment = finbyClient.createPayment(total, "ucet-${account.id}")
        account.gatewayPaymentId = payment.paymentRequestId
        return account.toPaymentDto(payment.gatewayUrl)
    }

    // Účet po platbe kartou uzatvára server len podľa stavu z brány.
    fun closePaidAccount(accountId: Long, paymentId: Long) {
        val account = tableAccountRepository.findWithLockById(accountId) ?: return
        if (account.status == TableAccountStatus.AWAITING_PAYMENT && account.gatewayPaymentId == paymentId) {
            close(account, PaymentMethod.CARD)
        }
    }

    // Pred uzavretím čašníkom server overí poslednú platbu kartou, aby hosť nezaplatil dvakrát.
    // Nedokončená platba bráni uzavretiu, kým hosť nezvolí platbu u čašníka alebo kým ju server prestane sledovať.
    fun closeByWaiter(accountId: Long): PaymentDto {
        val account = tableAccountRepository.findWithLockById(accountId) ?: throw notFound()
        if (account.status != TableAccountStatus.OPEN && account.status != TableAccountStatus.AWAITING_PAYMENT) {
            throw conflict("Účet nemožno uzavrieť")
        }
        val paymentId = account.gatewayPaymentId
        val cardStatus = paymentId?.let { cardPaymentMonitor.status(it) }
        val method = when {
            cardStatus == GatewayPaymentStatus.PAID -> PaymentMethod.CARD
            account.paymentMethod == PaymentMethod.CARD && cardStatus != GatewayPaymentStatus.REJECTED && cardPaymentMonitor.isWatched(paymentId!!) ->
                throw conflict("Platba kartou ešte nie je dokončená")
            else -> PaymentMethod.AT_WAITER
        }
        close(account, method)
        return account.toPaymentDto()
    }

    private fun close(account: TableAccount, method: PaymentMethod) {
        account.status = TableAccountStatus.CLOSED
        account.paymentMethod = method
        account.closedAt = LocalDateTime.now()
    }
}

private fun TableAccount.toPaymentDto(gatewayUrl: String? = null) = PaymentDto(status, paymentMethod!!, gatewayUrl)
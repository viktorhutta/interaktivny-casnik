package sk.hutta.server.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.hutta.server.dto.DiningTableDto
import sk.hutta.server.dto.OrderDto
import sk.hutta.server.dto.OrderRequest
import sk.hutta.server.dto.WaiterTableDto
import sk.hutta.server.model.OrderStatus
import sk.hutta.server.model.TableAccount
import sk.hutta.server.model.TableAccountStatus
import sk.hutta.server.repository.CustomerOrderRepository
import sk.hutta.server.repository.DiningTableRepository
import sk.hutta.server.repository.TableAccountRepository
import sk.hutta.server.repository.WaiterCallRepository

@Service
@Transactional
class WaiterService(
    private val diningTableRepository: DiningTableRepository,
    private val tableAccountRepository: TableAccountRepository,
    private val customerOrderRepository: CustomerOrderRepository,
    private val waiterCallRepository: WaiterCallRepository,
    private val orderService: OrderService,
    private val orderStatusService: OrderStatusService,
    private val paymentService: PaymentService,
) {
    // Z týchto údajov aplikácia poskladá kartu stola so zoznamom požiadaviek.
    @Transactional(readOnly = true)
    fun getTables() = tableAccountRepository.findForWaiter().map { account ->
        val orders = customerOrderRepository.findByTableAccountOrderById(account).map { it.toDto() }
        WaiterTableDto(
            account.id!!, account.diningTable.label, account.status, account.paymentMethod,
            orders.sumOf { it.total },
            waiterCallRepository.findByTableAccountAndResolvedFalse(account)?.id,
            orders.filter { it.status == OrderStatus.READY },
        )
    }

    // Zoznam všetkých stolov na výber pri objednávke za hosťa (bez tokenov).
    @Transactional(readOnly = true)
    fun getDiningTables() = diningTableRepository.findAllByOrderById().map { DiningTableDto(it.id!!, it.label) }

    // Čašník pri stole stojí, preto nepotvrdený účet objednávkou potvrdí a stôl bez účtu dostane rovno otvorený účet.
    fun createOrder(tableId: Long, request: OrderRequest, staffId: Long): OrderDto {
        val table = diningTableRepository.findWithLockById(tableId) ?: throw notFound()
        val account = tableAccountRepository.findWithLockByDiningTableAndStatusNot(table, TableAccountStatus.CLOSED)
            ?: tableAccountRepository.save(TableAccount(table, TableAccountStatus.OPEN))
        if (account.status == TableAccountStatus.AWAITING_CONFIRMATION) account.status = TableAccountStatus.OPEN
        return orderService.createOrder(account, request, staffId).toDto()
    }

    fun confirmAccount(accountId: Long) {
        val account = tableAccountRepository.findWithLockById(accountId) ?: throw notFound()
        if (account.status != TableAccountStatus.AWAITING_CONFIRMATION) throw conflict("Účet nečaká na potvrdenie")
        account.status = TableAccountStatus.OPEN
    }

    // Zaplatené u čašníka. Ak hosť medzitým zaplatil kartou, účet sa uzavrie ako platba kartou.
    fun closeAccount(accountId: Long) = paymentService.closeByWaiter(accountId)

    fun resolveWaiterCall(callId: Long) {
        waiterCallRepository.findById(callId).orElseThrow { notFound() }.resolved = true
    }

    // Čašník smie objednávku len označiť ako doručenú.
    fun deliverOrder(orderId: Long, staffId: Long): OrderDto =
        orderStatusService.changeStatus(orderId, OrderStatus.DELIVERED, staffId).toDto()
}
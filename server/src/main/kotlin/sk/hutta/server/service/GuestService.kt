package sk.hutta.server.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.hutta.server.dto.*
import sk.hutta.server.model.*
import sk.hutta.server.repository.*
import sk.hutta.server.security.TokenService

@Service
@Transactional
class GuestService(
    private val diningTableRepository: DiningTableRepository,
    private val tableAccountRepository: TableAccountRepository,
    private val customerOrderRepository: CustomerOrderRepository,
    private val waiterCallRepository: WaiterCallRepository,
    private val orderService: OrderService,
    private val tokenService: TokenService,
) {
    // Hosť sa pripojí k aktívnemu účtu stola, alebo sa založí nový a čaká na potvrdenie čašníkom.
    fun openSession(request: TableSessionRequest): TableSessionDto {
        val table = diningTableRepository.findWithLockById(request.tableId)
        if (table == null || table.token != request.token) throw notFound()
        val account = tableAccountRepository.findByDiningTableAndStatusNot(table, TableAccountStatus.CLOSED)
            ?: tableAccountRepository.save(TableAccount(table, TableAccountStatus.AWAITING_CONFIRMATION))
        return TableSessionDto(tokenService.createToken(account.id!!, "GUEST"), table.label, account.status)
    }

    @Transactional(readOnly = true)
    fun getAccount(accountId: Long): AccountDto {
        val account = tableAccountRepository.findById(accountId).orElseThrow { notFound() }
        checkNotClosed(account)
        val orders = customerOrderRepository.findByTableAccountOrderById(account).map { it.toDto() }
        return AccountDto(
            account.id!!, account.diningTable.label, account.status,
            waiterCallRepository.existsByTableAccountAndResolvedFalse(account),
            orders.sumOf { it.total },
            orders,
        )
    }

    fun createOrder(accountId: Long, request: OrderRequest): OrderDto {
        val account = tableAccountRepository.findWithLockById(accountId) ?: throw notFound()
        checkNotClosed(account)
        return orderService.createOrder(account, request).toDto()
    }

    fun callWaiter(accountId: Long) {
        val account = tableAccountRepository.findWithLockById(accountId) ?: throw notFound()
        checkNotClosed(account)
        if (!waiterCallRepository.existsByTableAccountAndResolvedFalse(account)) {
            waiterCallRepository.save(WaiterCall(account))
        }
    }

    // Token hosťa platí, len kým je jeho účet neuzavretý.
    private fun checkNotClosed(account: TableAccount) {
        if (account.status == TableAccountStatus.CLOSED) throw forbidden("Účet je uzavretý")
    }
}

// Aktuálny stav objednávky je posledný záznam histórie.
fun CustomerOrder.currentStatus() = statusChanges.last().status

fun OrderItem.total() = (price + ingredients.sumOf { it.price }) * quantity.toBigDecimal()

fun CustomerOrder.toDto() = OrderDto(
    id!!, currentStatus(), items.sumOf { it.total() },
    items.map { item ->
        OrderItemDto(item.name, item.quantity, item.price, item.ingredients.map { OrderItemIngredientDto(it.name, it.changeType, it.price) })
    },
    statusChanges.map { StatusChangeDto(it.status, it.changedAt) },
)
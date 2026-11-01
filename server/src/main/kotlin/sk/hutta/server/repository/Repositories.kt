package sk.hutta.server.repository

import org.springframework.data.jpa.repository.JpaRepository
import sk.hutta.server.model.*
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface CategoryRepository : JpaRepository<Category, Long> {
    fun findByArchivedFalseOrderByDisplayOrder(): List <Category>
}
interface MenuItemRepository : JpaRepository<MenuItem, Long> {
    fun findByArchivedFalse(): List<MenuItem>
}
interface IngredientRepository : JpaRepository<Ingredient, Long> {
    fun findByArchivedFalseOrderByName() : List<Ingredient>
}
interface MenuItemIngredientRepository : JpaRepository<MenuItemIngredient, Long>

interface DiningTableRepository : JpaRepository<DiningTable, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findWithLockById(id: Long): DiningTable?
}
interface TableAccountRepository : JpaRepository<TableAccount, Long> {
    fun findByDiningTableAndStatusNot(diningTable: DiningTable, status: TableAccountStatus): TableAccount?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findWithLockById(id: Long): TableAccount?

    // Čašník vidí neuzavreté účty a aj uzavreté, ktorým ešte ostala nedoručená objednávka.
    @Query("""
        select a from TableAccount a
        where a.status <> CLOSED
        or exists (
            select o from CustomerOrder o
            where o.tableAccount = a
            and not exists (select c from OrderStatusChange c where c.customerOrder = o and c.status = DELIVERED)
        )
        order by a.diningTable.id, a.id
    """)
    fun findForWaiter(): List<TableAccount>
}
interface WaiterCallRepository : JpaRepository<WaiterCall, Long> {
    fun existsByTableAccountAndResolvedFalse(tableAccount: TableAccount): Boolean
    fun findByTableAccountAndResolvedFalse(tableAccount: TableAccount): WaiterCall?
}
interface CustomerOrderRepository : JpaRepository<CustomerOrder, Long> {
    fun findByTableAccountOrderById(tableAccount: TableAccount): List<CustomerOrder>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findWithLockById(id: Long): CustomerOrder?

    // Kuchyňa vidí objednávky z potvrdených účtov, ktoré ešte nie sú hotové.
    @Query("""
        select o from CustomerOrder o
        where o.tableAccount.status <> AWAITING_CONFIRMATION
        and not exists (select c from OrderStatusChange c where c.customerOrder = o and c.status in (READY, DELIVERED))
        order by o.id
    """)
    fun findForKitchen(): List<CustomerOrder>
}

interface StaffUserRepository : JpaRepository<StaffUser, Long> {
    fun findByUsername(username: String): StaffUser?
}
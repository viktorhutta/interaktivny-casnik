package sk.hutta.server.repository

import org.springframework.data.jpa.repository.JpaRepository
import sk.hutta.server.model.*
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.Lock

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
}
interface WaiterCallRepository : JpaRepository<WaiterCall, Long> {
    fun existsByTableAccountAndResolvedFalse(tableAccount: TableAccount): Boolean
}
interface CustomerOrderRepository : JpaRepository<CustomerOrder, Long> {
    fun findByTableAccountOrderById(tableAccount: TableAccount): List<CustomerOrder>
}

interface StaffUserRepository : JpaRepository<StaffUser, Long>
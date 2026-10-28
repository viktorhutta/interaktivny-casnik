package sk.hutta.server.repository

import org.springframework.data.jpa.repository.JpaRepository
import sk.hutta.server.model.*

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
interface DiningTableRepository : JpaRepository<DiningTable, Long>
interface TableAccountRepository : JpaRepository<TableAccount, Long>
interface WaiterCallRepository : JpaRepository<WaiterCall, Long>
interface CustomerOrderRepository : JpaRepository<CustomerOrder, Long>
interface StaffUserRepository : JpaRepository<StaffUser, Long>
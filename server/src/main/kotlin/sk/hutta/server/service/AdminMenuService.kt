package sk.hutta.server.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.hutta.server.dto.*
import sk.hutta.server.model.*
import sk.hutta.server.repository.CategoryRepository
import sk.hutta.server.repository.IngredientRepository
import sk.hutta.server.repository.MenuItemRepository
import java.math.BigDecimal

@Service
@Transactional
class AdminMenuService(
    private val categoryRepository: CategoryRepository,
    private val menuItemRepository: MenuItemRepository,
    private val ingredientRepository: IngredientRepository,
) {
    // Kategórie

    fun getCategories() = categoryRepository.findByArchivedFalseOrderByDisplayOrder().map { it.toDto() }

    fun createCategory(request: CategoryRequest): AdminCategoryDto {
        checkName(request.name, 50)
        return categoryRepository.save(Category(request.name.trim(), request.displayOrder)).toDto()
    }

    fun updateCategory(id: Long, request: CategoryRequest): AdminCategoryDto {
        checkName(request.name, 50)
        val category = findCategory(id)
        category.name = request.name.trim()
        category.displayOrder = request.displayOrder
        return category.toDto()
    }

    fun archiveCategory(id: Long) {
        findCategory(id).archived = true
    }

    // Ingrediencie

    fun getIngredients() = ingredientRepository.findByArchivedFalseOrderByName().map { it.toDto() }

    fun createIngredient(request: IngredientRequest): AdminIngredientDto {
        checkName(request.name, 50)
        return ingredientRepository.save(Ingredient(request.name.trim(), request.available)).toDto()
    }

    fun updateIngredient(id: Long, request: IngredientRequest): AdminIngredientDto {
        checkName(request.name, 50)
        val ingredient = findIngredient(id)
        ingredient.name = request.name.trim()
        ingredient.available = request.available
        return ingredient.toDto()
    }

    fun archiveIngredient(id: Long) {
        findIngredient(id).archived = true
    }

    // Položky menu

    fun getMenuItems() = menuItemRepository.findByArchivedFalse().sortedBy { it.id }.map { it.toDto() }

    fun createMenuItem(request: MenuItemRequest): AdminMenuItemDto {
        val item = MenuItem(findCategory(request.categoryId), request.name, request.description, request.price)
        applyRequest(item, request)
        return menuItemRepository.save(item).toDto()
    }

    fun updateMenuItem(id: Long, request: MenuItemRequest): AdminMenuItemDto {
        val item = findMenuItem(id)
        applyRequest(item, request)
        return item.toDto()
    }

    fun archiveMenuItem(id: Long) {
        findMenuItem(id).archived = true
    }

    private fun applyRequest(item: MenuItem, request: MenuItemRequest) {
        checkName(request.name, 100)
        if (request.description.length > 255) throw badRequest("Popis môže mať najviac 255 znakov")
        if (request.price < BigDecimal.ZERO) throw badRequest("Cena nesmie byť záporná")
        item.category = findCategory(request.categoryId)
        item.name = request.name.trim()
        item.description = request.description.trim()
        item.price = request.price
        item.available = request.available
        setIngredients(item, request.ingredients)
    }

    // Existujúce väzby sa upravia, nové pridajú a tie, ktoré v požiadavke nie sú, zmažú.
    private fun setIngredients(item: MenuItem, requests: List<MenuItemIngredientDto>) {
        val ids = requests.map { it.ingredientId }
        if (ids.size != ids.toSet().size) throw badRequest("Ingrediencia je pri položke viackrát")
        item.ingredients.removeIf { it.ingredient.id !in ids }
        for (request in requests) {
            if (request.type == IngredientType.EXTRA && (request.extraPrice == null || request.extraPrice < BigDecimal.ZERO)) {
                throw badRequest("Ingrediencia navyše musí mať príplatok")
            }
            val link = item.ingredients.find { it.ingredient.id == request.ingredientId }
                ?: MenuItemIngredient(item, findIngredient(request.ingredientId), request.type, false).also { item.ingredients.add(it) }
            link.type = request.type
            link.removable = request.type == IngredientType.BASE && request.removable
            link.extraPrice = if (request.type == IngredientType.EXTRA) request.extraPrice else null
        }
    }

    private fun checkName(name: String, maxLength: Int) {
        if (name.isBlank() || name.trim().length > maxLength) throw badRequest("Názov musí mať 1 až $maxLength znakov")
    }

    private fun findCategory(id: Long) = categoryRepository.findById(id).filter { !it.archived }.orElseThrow { notFound() }

    private fun findIngredient(id: Long) = ingredientRepository.findById(id).filter { !it.archived }.orElseThrow { notFound() }

    private fun findMenuItem(id: Long) = menuItemRepository.findById(id).filter { !it.archived }.orElseThrow { notFound() }

    private fun Category.toDto() = AdminCategoryDto(id!!, name, displayOrder)

    private fun Ingredient.toDto() = AdminIngredientDto(id!!, name, available)

    private fun MenuItem.toDto() = AdminMenuItemDto(
        id!!, category.id!!, name, description, price, available,
        ingredients.map { MenuItemIngredientDto(it.ingredient.id!!, it.type, it.removable, it.extraPrice) },
    )
}
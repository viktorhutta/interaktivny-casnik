package sk.hutta.server.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.hutta.server.dto.CategoryDto
import sk.hutta.server.dto.MenuIngredientDto
import sk.hutta.server.dto.MenuItemDto
import sk.hutta.server.model.Ingredient
import sk.hutta.server.model.IngredientType
import sk.hutta.server.model.MenuItem
import sk.hutta.server.repository.CategoryRepository
import sk.hutta.server.repository.MenuItemRepository

@Service
class MenuService(
    private val categoryRepository: CategoryRepository,
    private val menuItemRepository: MenuItemRepository,
) {
    @Transactional(readOnly = true)
    fun getMenu(): List<CategoryDto> {
        val itemsByCategory = menuItemRepository.findByArchivedFalse()
            .filter { it.isOrderable() }
            .groupBy { it.category.id }
        return categoryRepository.findByArchivedFalseOrderByDisplayOrder().mapNotNull { category ->
            val items = itemsByCategory[category.id] ?: return@mapNotNull null
            CategoryDto(category.id!!, category.name, items.sortedBy { it.id }.map { it.toDto() })
        }
    }

    private fun MenuItem.toDto() = MenuItemDto(
        id!!, name, description, price,
        ingredients.filter { it.ingredient.isUsable() }.map {
            MenuIngredientDto(it.ingredient.id!!, it.ingredient.name, it.type, it.removable, it.extraPrice)
        },
    )
}

fun Ingredient.isUsable() = available && !archived

// Položku možno objednať, len ak je dostupná a má dostupné všetky základné ingrediencie.
fun MenuItem.isOrderable() = available && !archived &&
        ingredients.filter { it.type == IngredientType.BASE }.all { it.ingredient.isUsable() }
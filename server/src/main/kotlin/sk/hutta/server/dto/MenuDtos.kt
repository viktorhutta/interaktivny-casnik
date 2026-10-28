package sk.hutta.server.dto

import sk.hutta.server.model.IngredientType
import java.math.BigDecimal

// Menu pre hosťa

data class CategoryDto(val id: Long, val name: String, val items: List<MenuItemDto>)

data class MenuItemDto(val id: Long, val name: String, val description: String, val price: BigDecimal, val ingredients: List<MenuIngredientDto>)

data class MenuIngredientDto(val id: Long, val name: String, val type: IngredientType, val removable: Boolean, val extraPrice: BigDecimal?)

// Správa menu

data class AdminCategoryDto(val id: Long, val name: String, val displayOrder: Int)

data class CategoryRequest(val name: String, val displayOrder: Int)

data class AdminIngredientDto(val id: Long, val name: String, val available: Boolean)

data class IngredientRequest(val name: String, val available: Boolean)

data class AdminMenuItemDto(val id: Long, val categoryId: Long, val name: String, val description: String, val price: BigDecimal, val available: Boolean, val ingredients: List<MenuItemIngredientDto>)

data class MenuItemRequest(val categoryId: Long, val name: String, val description: String, val price: BigDecimal, val available: Boolean, val ingredients: List<MenuItemIngredientDto>)

data class MenuItemIngredientDto(val ingredientId: Long, val type: IngredientType, val removable: Boolean, val extraPrice: BigDecimal?)
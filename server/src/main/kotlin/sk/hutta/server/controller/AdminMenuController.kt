package sk.hutta.server.controller

import org.springframework.web.bind.annotation.*
import sk.hutta.server.dto.CategoryRequest
import sk.hutta.server.dto.IngredientRequest
import sk.hutta.server.dto.MenuItemRequest
import sk.hutta.server.service.AdminMenuService

@RestController
@RequestMapping("/api/admin")
class AdminMenuController(private val adminMenuService: AdminMenuService) {

    @GetMapping("/categories")
    fun getCategories() = adminMenuService.getCategories()

    @PostMapping("/categories")
    fun createCategory(@RequestBody request: CategoryRequest) = adminMenuService.createCategory(request)

    @PutMapping("/categories/{id}")
    fun updateCategory(@PathVariable id: Long, @RequestBody request: CategoryRequest) = adminMenuService.updateCategory(id, request)

    @DeleteMapping("/categories/{id}")
    fun archiveCategory(@PathVariable id: Long) = adminMenuService.archiveCategory(id)

    @GetMapping("/ingredients")
    fun getIngredients() = adminMenuService.getIngredients()

    @PostMapping("/ingredients")
    fun createIngredient(@RequestBody request: IngredientRequest) = adminMenuService.createIngredient(request)

    @PutMapping("/ingredients/{id}")
    fun updateIngredient(@PathVariable id: Long, @RequestBody request: IngredientRequest) = adminMenuService.updateIngredient(id, request)

    @DeleteMapping("/ingredients/{id}")
    fun archiveIngredient(@PathVariable id: Long) = adminMenuService.archiveIngredient(id)

    @GetMapping("/menu-items")
    fun getMenuItems() = adminMenuService.getMenuItems()

    @PostMapping("/menu-items")
    fun createMenuItem(@RequestBody request: MenuItemRequest) = adminMenuService.createMenuItem(request)

    @PutMapping("/menu-items/{id}")
    fun updateMenuItem(@PathVariable id: Long, @RequestBody request: MenuItemRequest) = adminMenuService.updateMenuItem(id, request)

    @DeleteMapping("/menu-items/{id}")
    fun archiveMenuItem(@PathVariable id: Long) = adminMenuService.archiveMenuItem(id)
}
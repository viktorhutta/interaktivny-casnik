package sk.hutta.server.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import sk.hutta.server.service.MenuService

@RestController
class MenuController(private val menuService: MenuService) {

    @GetMapping("/api/menu")
    fun getMenu() = menuService.getMenu()
}
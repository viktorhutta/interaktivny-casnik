package sk.hutta.server.controller

import org.springframework.web.bind.annotation.*
import sk.hutta.server.dto.DiningTableRequest
import sk.hutta.server.service.AdminTableService

@RestController
@RequestMapping("/api/admin")
class AdminTableController(private val adminTableService: AdminTableService) {

    @GetMapping("/dining-tables")
    fun getTables() = adminTableService.getTables()

    @PostMapping("/dining-tables")
    fun createTable(@RequestBody request: DiningTableRequest) = adminTableService.createTable(request)

    @PostMapping("/dining-tables/{id}/token")
    fun regenerateToken(@PathVariable id: Long) = adminTableService.regenerateToken(id)
}
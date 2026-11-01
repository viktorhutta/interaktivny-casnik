package sk.hutta.server.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import sk.hutta.server.service.WaiterService

@RestController
@RequestMapping("/api/waiter")
class WaiterController(private val waiterService: WaiterService) {

    @GetMapping("/tables")
    fun getTables() = waiterService.getTables()

    @PostMapping("/accounts/{id}/confirm")
    fun confirmAccount(@PathVariable id: Long) = waiterService.confirmAccount(id)

    @PostMapping("/accounts/{id}/close")
    fun closeAccount(@PathVariable id: Long) = waiterService.closeAccount(id)

    @PostMapping("/waiter-calls/{id}/resolve")
    fun resolveWaiterCall(@PathVariable id: Long) = waiterService.resolveWaiterCall(id)

    @PostMapping("/orders/{id}/delivered")
    fun deliverOrder(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: Long) = waiterService.deliverOrder(id, jwt.staffId())
}
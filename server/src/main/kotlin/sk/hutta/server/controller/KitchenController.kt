package sk.hutta.server.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import sk.hutta.server.dto.OrderStatusRequest
import sk.hutta.server.service.KitchenService

@RestController
@RequestMapping("/api/kitchen")
class KitchenController(private val kitchenService: KitchenService) {

    @GetMapping("/orders")
    fun getOrders() = kitchenService.getOrders()

    @PostMapping("/orders/{id}/status")
    fun changeStatus(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: Long, @RequestBody request: OrderStatusRequest) =
        kitchenService.changeStatus(id, request.status, jwt.staffId())
}
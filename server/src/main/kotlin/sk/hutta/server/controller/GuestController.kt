package sk.hutta.server.controller

import org.springframework.web.bind.annotation.*
import sk.hutta.server.dto.OrderRequest
import sk.hutta.server.dto.TableSessionRequest
import sk.hutta.server.service.GuestService

@RestController
@RequestMapping("/api/guest")
class GuestController(private val guestService: GuestService) {

    @PostMapping("/session")
    fun openSession(@RequestBody request: TableSessionRequest) = guestService.openSession(request)

    @GetMapping("/accounts/{accountId}")
    fun getAccount(@PathVariable accountId: Long) = guestService.getAccount(accountId)

    @PostMapping("/accounts/{accountId}/orders")
    fun createOrder(@PathVariable accountId: Long, @RequestBody request: OrderRequest) = guestService.createOrder(accountId, request)

    @PostMapping("/accounts/{accountId}/waiter-call")
    fun callWaiter(@PathVariable accountId: Long) = guestService.callWaiter(accountId)
}
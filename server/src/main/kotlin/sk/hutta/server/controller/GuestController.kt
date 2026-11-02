package sk.hutta.server.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import sk.hutta.server.dto.OrderRequest
import sk.hutta.server.dto.PaymentRequest
import sk.hutta.server.dto.TableSessionRequest
import sk.hutta.server.service.GuestService
import sk.hutta.server.service.PaymentService

// Id účtu sa berie z tokenu hosťa, nie z adresy, takže hosť nemôže pristúpiť k cudziemu účtu.
@RestController
@RequestMapping("/api/guest")
class GuestController(private val guestService: GuestService, private val paymentService: PaymentService) {

    @PostMapping("/session")
    fun openSession(@RequestBody request: TableSessionRequest) = guestService.openSession(request)

    @GetMapping("/account")
    fun getAccount(@AuthenticationPrincipal jwt: Jwt) = guestService.getAccount(jwt.accountId())

    @PostMapping("/account/orders")
    fun createOrder(@AuthenticationPrincipal jwt: Jwt, @RequestBody request: OrderRequest) = guestService.createOrder(jwt.accountId(), request)

    @PostMapping("/account/waiter-call")
    fun callWaiter(@AuthenticationPrincipal jwt: Jwt) = guestService.callWaiter(jwt.accountId())

    @PostMapping("/account/payment")
    fun choosePayment(@AuthenticationPrincipal jwt: Jwt, @RequestBody request: PaymentRequest) =
        paymentService.choosePayment(jwt.accountId(), request.method)
}
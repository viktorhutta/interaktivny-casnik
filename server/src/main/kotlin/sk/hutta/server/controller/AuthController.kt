package sk.hutta.server.controller

import org.springframework.web.bind.annotation.*
import sk.hutta.server.dto.LoginRequest
import sk.hutta.server.service.AuthService

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest) = authService.login(request)
}
package sk.hutta.server.dto

import sk.hutta.server.model.Role

data class LoginRequest(val username: String, val password: String)

data class LoginDto(val token: String, val role: Role)
package sk.hutta.server.service

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

fun badRequest(message: String) = ResponseStatusException(HttpStatus.BAD_REQUEST, message)

fun notFound() = ResponseStatusException(HttpStatus.NOT_FOUND)
fun conflict(message: String) = ResponseStatusException(HttpStatus.CONFLICT, message)
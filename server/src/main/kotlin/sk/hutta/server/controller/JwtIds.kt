package sk.hutta.server.controller

import org.springframework.security.oauth2.jwt.Jwt

// TokenService dáva do subject tokenu id účtu stola (hosť) alebo id zamestnanca (personál).
fun Jwt.accountId() = subject!!.toLong()

fun Jwt.staffId() = subject!!.toLong()
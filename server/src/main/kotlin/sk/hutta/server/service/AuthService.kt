package sk.hutta.server.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.hutta.server.dto.LoginDto
import sk.hutta.server.dto.LoginRequest
import sk.hutta.server.repository.StaffUserRepository
import sk.hutta.server.security.TokenService

@Service
@Transactional(readOnly = true)
class AuthService(
    private val staffUserRepository: StaffUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenService: TokenService,
) {
    // Zlé meno aj zlé heslo vrátia tú istú chybu, aby sa nedalo zistiť, ktoré mená existujú.
    fun login(request: LoginRequest): LoginDto {
        val user = staffUserRepository.findByUsername(request.username)
        if (user == null || !passwordEncoder.matches(request.password, user.passwordHash)) {
            throw unauthorized("Nesprávne meno alebo heslo")
        }
        return LoginDto(tokenService.createToken(user.id!!, user.role.name), user.role)
    }
}
package sk.hutta.server.security

import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class TokenService(private val jwtEncoder: JwtEncoder) {

    // subject je id účtu stola (hosť) alebo id zamestnanca (personál).
    fun createToken(subject: Long, role: String): String {
        val now = Instant.now()
        val claims = JwtClaimsSet.builder()
            .subject(subject.toString())
            .claim("role", role)
            .issuedAt(now)
            .expiresAt(now.plus(Duration.ofHours(12)))
            .build()
        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
    }
}
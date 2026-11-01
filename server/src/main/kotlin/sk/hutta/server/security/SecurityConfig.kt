package sk.hutta.server.security

import com.nimbusds.jose.jwk.source.ImmutableSecret
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import java.util.Base64
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Configuration
class SecurityConfig(@Value("\${app.jwt.secret}") jwtSecret: String) {

    // Tým istým kľúčom server tokeny podpisuje aj overuje (HMAC SHA-256).
    private val secretKey: SecretKey = SecretKeySpec(Base64.getDecoder().decode(jwtSecret), "HmacSHA256")

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }
            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
            authorizeHttpRequests {
                authorize("/error", permitAll)
                authorize("/api/ping", permitAll)
                authorize(HttpMethod.POST, "/api/guest/session", permitAll)
                authorize(HttpMethod.POST, "/api/auth/login", permitAll)
                authorize(HttpMethod.GET, "/api/menu", authenticated)
                authorize("/api/guest/**", hasRole("GUEST"))
                authorize("/api/waiter/**", hasRole("WAITER"))
                authorize("/api/kitchen/**", hasRole("KITCHEN"))
                authorize("/api/admin/**", hasRole("ADMIN"))
                authorize(anyRequest, denyAll)
            }
            oauth2ResourceServer {
                jwt { jwtAuthenticationConverter = roleConverter() }
            }
        }
        return http.build()
    }

    // Rola z tokenu (napr. GUEST) sa stane oprávnením ROLE_GUEST, ktoré kontroluje hasRole.
    private fun roleConverter(): JwtAuthenticationConverter {
        val authorities = JwtGrantedAuthoritiesConverter()
        authorities.setAuthoritiesClaimName("role")
        authorities.setAuthorityPrefix("ROLE_")
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter(authorities)
        return converter
    }

    @Bean
    fun jwtEncoder(): JwtEncoder = NimbusJwtEncoder(ImmutableSecret<SecurityContext>(secretKey))

    @Bean
    fun jwtDecoder(): JwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey).build()

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
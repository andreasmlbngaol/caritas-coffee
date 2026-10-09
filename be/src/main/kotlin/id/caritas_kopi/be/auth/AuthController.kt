package id.caritas_kopi.be.auth

import id.caritas_kopi.be.config.AppProperties
import id.caritas_kopi.be.common.ApiException
import id.caritas_kopi.be.user.Role
import id.caritas_kopi.be.user.UserRepository
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.constraints.NotBlank
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.time.Instant

data class LoginRequest(
    @field:NotBlank val username: String,
    @field:NotBlank val password: String,
)

data class SessionUser(
    val id: String,
    val name: String,
    val role: Role,
)

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val props: AppProperties,
) {

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest, response: HttpServletResponse): SessionUser {
        val user = userRepository.findByUsername(req.username).orElse(null)
            ?: throw ApiException.unauthorized("Username atau password salah")
        if (!user.isActive) throw ApiException.unauthorized("Username atau password salah")
        if (!passwordEncoder.matches(req.password, user.passwordHash)) {
            throw ApiException.unauthorized("Username atau password salah")
        }

        user.lastLoginAt = Instant.now()
        userRepository.save(user)

        val token = jwtService.generate(user.id!!, user.role)
        response.addHeader(
            "Set-Cookie",
            cookieHeader(props.jwt.cookieName, token, Duration.ofSeconds(props.jwt.ttlSeconds)),
        )
        return SessionUser(user.id.toString(), user.fullName ?: user.username, user.role)
    }

    @GetMapping("/me")
    fun me(auth: Authentication): SessionUser {
        val p = auth.authPrincipal()
        return SessionUser(p.id.toString(), p.name, p.role)
    }

    @PostMapping("/logout")
    fun logout(response: HttpServletResponse): Map<String, Boolean> {
        response.addHeader("Set-Cookie", cookieHeader(props.jwt.cookieName, "", Duration.ZERO))
        return mapOf("ok" to true)
    }

    private fun cookieHeader(name: String, value: String, maxAge: Duration): String {
        val secure = if (props.jwt.cookieSecure) "; Secure" else ""
        val age = if (maxAge.isZero) "Max-Age=0" else "Max-Age=${maxAge.seconds}"
        return "$name=$value; Path=/; HttpOnly; SameSite=Lax; $age$secure"
    }
}

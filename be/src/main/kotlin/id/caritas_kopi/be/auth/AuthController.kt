package id.caritas_kopi.be.auth

import id.caritas_kopi.be.config.AppProperties
import id.caritas_kopi.be.common.ApiException
import id.caritas_kopi.be.user.Role
import id.caritas_kopi.be.user.UserRepository
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
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
    private val throttle: LoginThrottle,
) {

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody req: LoginRequest,
        response: HttpServletResponse,
        httpReq: jakarta.servlet.http.HttpServletRequest,
    ): SessionUser {
        val username = req.username.trim()
        val throttleKey = "$username|${clientIp(httpReq)}"
        if (throttle.isBlocked(throttleKey)) {
            throw ApiException.tooManyRequests("Terlalu banyak percobaan login. Coba lagi nanti.")
        }

        val user = userRepository.findByUsername(username).orElse(null)
            ?: run { throttle.recordFailure(throttleKey); throw ApiException.unauthorized("Username atau password salah") }
        if (!user.isActive) {
            throttle.recordFailure(throttleKey)
            throw ApiException.unauthorized("Username atau password salah")
        }
        if (!passwordEncoder.matches(req.password, user.passwordHash)) {
            throttle.recordFailure(throttleKey)
            throw ApiException.unauthorized("Username atau password salah")
        }

        throttle.reset(throttleKey)
        user.lastLoginAt = Instant.now()
        userRepository.save(user)

        val token = jwtService.generate(user.id!!, user.role, user.tokenVersion)
        response.addHeader(
            "Set-Cookie",
            cookieHeader(props.jwt.cookieName, token, Duration.ofSeconds(props.jwt.ttlSeconds)),
        )
        return SessionUser(user.id.toString(), user.fullName ?: user.username, user.role)
    }

    /**
     * IP klien untuk pembatasan login. Utamakan X-Real-IP (di-set nginx dari
     * $remote_addr, tidak bisa dipalsukan klien). X-Forwarded-For bisa berisi
     * entri kiriman klien, jadi ambil elemen TERAKHIR (yang ditambahkan proxy),
     * bukan yang pertama. Terakhir fallback ke remote address.
     */
    private fun clientIp(req: jakarta.servlet.http.HttpServletRequest): String {
        req.getHeader("X-Real-IP")?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
        val fwd = req.getHeader("X-Forwarded-For")
        if (!fwd.isNullOrBlank()) {
            return fwd.substringAfterLast(',').trim().ifEmpty { req.remoteAddr ?: "unknown" }
        }
        return req.remoteAddr ?: "unknown"
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

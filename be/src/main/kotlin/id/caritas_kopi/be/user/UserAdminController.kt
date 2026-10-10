package id.caritas_kopi.be.user

import id.caritas_kopi.be.auth.authPrincipal
import id.caritas_kopi.be.common.ApiException
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.SecureRandom
import java.time.Instant
import java.util.UUID

data class UserDto(
    val id: String,
    val username: String,
    val fullName: String?,
    val role: Role,
    val isActive: Boolean,
    val createdAt: Instant,
    val lastLoginAt: Instant?,
)

data class CreateUserRequest(val username: String, @field:Size(max = 100, message = "Nama maksimal 100 karakter") val fullName: String)
data class CredentialsResponse(val username: String, val password: String)

@RestController
@RequestMapping("/api/admin/users")
class UserAdminController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    // Alfabet tanpa karakter ambigu (O/0, I/l/1) dan tanpa simbol.
    private val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789"
    private val random = SecureRandom()

    private fun generatePassword(length: Int = 10): String =
        (1..length).map { alphabet[random.nextInt(alphabet.length)] }.joinToString("")

    @GetMapping
    fun list(): List<UserDto> =
        userRepository.findAll().sortedByDescending { it.createdAt }.map { it.toDto() }

    @PostMapping
    @Transactional
    fun create(@Valid @RequestBody req: CreateUserRequest): CredentialsResponse {
        val username = req.username.trim()
        if (username.length < 3 || username.length > 30) {
            throw ApiException.badRequest("Username minimal 3 karakter")
        }
        if (!Regex("^[a-z0-9_.]+$").matches(username)) {
            throw ApiException.badRequest("Username hanya huruf kecil, angka, titik, underscore")
        }
        if (req.fullName.isBlank()) throw ApiException.badRequest("Nama wajib diisi")
        if (userRepository.existsByUsername(username)) throw ApiException.conflict("Username sudah dipakai")

        val password = generatePassword()
        val user = User(
            username = username,
            fullName = req.fullName.trim(),
            passwordHash = requireNotNull(passwordEncoder.encode(password)),
            role = Role.ENUMERATOR,
        )
        userRepository.save(user)
        return CredentialsResponse(username, password)
    }

    @PostMapping("/{id}/reset-password")
    @Transactional
    fun resetPassword(@PathVariable id: UUID): CredentialsResponse {
        val user = userRepository.findById(id).orElseThrow { ApiException.notFound("User tidak ditemukan") }
        val password = generatePassword()
        user.passwordHash = requireNotNull(passwordEncoder.encode(password))
        user.tokenVersion += 1 // batalkan sesi lama
        userRepository.save(user)
        return CredentialsResponse(user.username, password)
    }

    @PatchMapping("/{id}/toggle-active")
    @Transactional
    fun toggleActive(@PathVariable id: UUID, auth: Authentication): Map<String, Boolean> {
        val me = auth.authPrincipal()
        if (id == me.id) throw ApiException.badRequest("Tidak bisa menonaktifkan akun sendiri")
        val user = userRepository.findById(id).orElseThrow { ApiException.notFound("User tidak ditemukan") }
        user.isActive = !user.isActive
        user.tokenVersion += 1 // batalkan sesi lama (juga saat dinonaktifkan)
        userRepository.save(user)
        return mapOf("isActive" to user.isActive)
    }
}

fun User.toDto(): UserDto = UserDto(
    id = id.toString(),
    username = username,
    fullName = fullName,
    role = role,
    isActive = isActive,
    createdAt = createdAt,
    lastLoginAt = lastLoginAt,
)

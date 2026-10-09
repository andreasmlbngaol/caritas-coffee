package id.caritas_kopi.be.auth

import id.caritas_kopi.be.common.ApiException
import id.caritas_kopi.be.user.Role
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.UUID

/** Principal terautentikasi yang disimpan di SecurityContext. */
data class AuthPrincipal(
    val id: UUID,
    val username: String,
    val name: String,
    val role: Role,
) {
    val isAdmin: Boolean get() = role == Role.ADMIN

    fun authorities(): List<SimpleGrantedAuthority> = listOf(SimpleGrantedAuthority("ROLE_${role.name}"))
}

/** Ambil principal; lempar 401 bila tidak ada (jaring pengaman tambahan). */
fun Authentication.authPrincipal(): AuthPrincipal =
    principal as? AuthPrincipal ?: throw ApiException.unauthorized()

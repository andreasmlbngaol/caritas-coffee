package id.caritas_kopi.be.auth

import id.caritas_kopi.be.user.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val props: id.caritas_kopi.be.config.AppProperties,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = request.cookies?.firstOrNull { it.name == props.jwt.cookieName }?.value
        if (token != null && SecurityContextHolder.getContext().authentication == null) {
            jwtService.parseUserId(token)?.let { userId ->
                val user = userRepository.findById(userId).orElse(null)
                if (user != null && user.isActive) {
                    val principal = AuthPrincipal(
                        id = user.id!!,
                        username = user.username,
                        name = user.fullName ?: user.username,
                        role = user.role,
                    )
                    val auth = UsernamePasswordAuthenticationToken(
                        principal, null, principal.authorities(),
                    )
                    SecurityContextHolder.getContext().authentication = auth
                }
            }
        }
        filterChain.doFilter(request, response)
    }
}

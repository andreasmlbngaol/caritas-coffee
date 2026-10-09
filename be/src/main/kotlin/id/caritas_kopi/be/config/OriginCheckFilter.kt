package id.caritas_kopi.be.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Pengganti ringan cek Origin/Host bawaan Next server actions (CSRF dimatikan).
 * Untuk metode yang mengubah state ke endpoint API (kecuali POST /api/auth/login),
 * pastikan Origin (atau Referer) cocok dengan origin aplikasi yang dikonfigurasi.
 * Bila keduanya tidak ada (klien non-browser), permintaan dilewatkan.
 */
class OriginCheckFilter(
    private val props: AppProperties,
) : OncePerRequestFilter() {

    private val stateChanging = setOf("POST", "PUT", "PATCH", "DELETE")

    private val allowed: List<String> =
        (listOf(props.appUrl) + props.corsAllowedOrigins.split(","))
            .map { it.trim().trimEnd('/') }
            .filter { it.isNotEmpty() }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (request.method in stateChanging &&
            request.requestURI.startsWith("/api/") &&
            !(request.method == "POST" && request.requestURI == "/api/auth/login")
        ) {
            val origin = request.getHeader("Origin")?.trimEnd('/')
            val referer = request.getHeader("Referer")
            val ok = when {
                origin != null -> allowed.any { it == origin }
                referer != null -> allowed.any { referer.startsWith(it) }
                else -> true
            }
            if (!ok) {
                response.status = HttpServletResponse.SC_FORBIDDEN
                response.contentType = "application/json"
                response.writer.write("{\"error\":\"Origin tidak diizinkan\"}")
                return
            }
        }
        filterChain.doFilter(request, response)
    }
}

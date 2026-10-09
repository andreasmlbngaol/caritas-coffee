package id.caritas_kopi.be.config

import id.caritas_kopi.be.auth.JwtAuthFilter
import jakarta.servlet.DispatcherType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.DispatcherTypeRequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter,
    private val props: AppProperties,
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val origins = props.corsAllowedOrigins.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        http {
            csrf { disable() }
            // CORS hanya dipasang bila ada origin tepercaya (dev cross-origin).
            // Di produksi same-origin lewat nginx daftar kosong, dan memasang
            // filter CORS dengan daftar kosong membuat request yang membawa
            // header Origin (POST same-origin) ditolak "Invalid CORS request".
            if (origins.isNotEmpty()) {
                cors { configurationSource = corsSource(origins) }
            }
            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
            authorizeHttpRequests {
                // Forward ke /error (dispatch ERROR) saat respons berstatus error harus
                // lolos tanpa otentikasi. Kalau tidak, ExceptionTranslationFilter
                // menimpanya jadi 401 dan 403/404 asli hilang.
                authorize(DispatcherTypeRequestMatcher(DispatcherType.ERROR), permitAll)
                authorize(HttpMethod.POST, "/api/auth/login", permitAll)
                authorize(HttpMethod.POST, "/api/auth/logout", permitAll)
                authorize("/api/openapi/**", permitAll)
                authorize("/api/swagger-ui/**", permitAll)
                authorize("/api/swagger-ui.html", permitAll)
                // Analitik & admin: khusus ADMIN.
                authorize("/api/analitik/**", hasRole("ADMIN"))
                authorize("/api/admin/**", hasRole("ADMIN"))
                authorize(anyRequest, authenticated)
            }
            addFilterBefore<UsernamePasswordAuthenticationFilter>(jwtAuthFilter)
            addFilterBefore<UsernamePasswordAuthenticationFilter>(OriginCheckFilter(props))
            exceptionHandling {
                authenticationEntryPoint = org.springframework.security.web.authentication.HttpStatusEntryPoint(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                )
            }
        }
        return http.build()
    }

    private fun corsSource(origins: List<String>): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOrigins = origins
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }
}

package id.caritas_kopi.be.config

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.stereotype.Component

/**
 * Guard startup: di profil `prod`, tolak konfigurasi rahasia default/lemah.
 * Tanpa ini, app bisa boot dengan JWT_SECRET publik (auth bypass: token bisa
 * dipalsukan) atau admin/admin12345 bila .env tidak lengkap.
 */
@Component
class StartupGuard(
    private val props: AppProperties,
    private val environment: Environment,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val knownDefaults = setOf(
        "dev-secret-change-me-dev-secret-change-me",
        "admin12345",
    )

    @PostConstruct
    fun verify() {
        if (!environment.acceptsProfiles(Profiles.of("prod"))) return

        check(props.jwt.secret.length >= 32 && props.jwt.secret !in knownDefaults) {
            "JWT_SECRET produksi wajib >= 32 byte dan bukan nilai default"
        }
        check(props.admin.password.isNotBlank() && props.admin.password !in knownDefaults) {
            "ADMIN_PASSWORD produksi wajib diisi dan bukan nilai default"
        }
        if (!props.jwt.cookieSecure) {
            log.warn("StartupGuard: COOKIE_SECURE=false di produksi - cookie sesi akan dikirim tanpa flag Secure.")
        }
        log.info("StartupGuard: konfigurasi produksi valid.")
    }
}

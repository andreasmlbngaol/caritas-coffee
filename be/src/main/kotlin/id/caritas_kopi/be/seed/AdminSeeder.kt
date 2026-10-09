package id.caritas_kopi.be.seed

import id.caritas_kopi.be.config.AppProperties
import id.caritas_kopi.be.user.Role
import id.caritas_kopi.be.user.User
import id.caritas_kopi.be.user.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

/** Buat akun admin awal dari env ADMIN_USERNAME/ADMIN_PASSWORD bila belum ada. */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class AdminSeeder(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val props: AppProperties,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        val username = props.admin.username
        if (username.isBlank()) return
        if (userRepository.existsByUsername(username)) {
            log.info("Admin '{}' sudah ada, seeder dilewati.", username)
            return
        }
        val user = User(
            username = username,
            fullName = "Administrator",
            passwordHash = requireNotNull(passwordEncoder.encode(props.admin.password)),
            role = Role.ADMIN,
        )
        userRepository.save(user)
        log.info("Admin awal dibuat: {}", username)
    }
}

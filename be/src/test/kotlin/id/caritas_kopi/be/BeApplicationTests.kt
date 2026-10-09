package id.caritas_kopi.be

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.Test

@SpringBootTest
@Testcontainers
class BeApplicationTests {

    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:17-alpine")

        @JvmStatic
        @DynamicPropertySource
        fun props(registry: DynamicPropertyRegistry) {
            registry.add("DB_URL") { postgres.jdbcUrl }
            registry.add("DB_USER") { postgres.username }
            registry.add("DB_PASSWORD") { postgres.password }
            // Lewati impor wilayah (2,9 MB) agar context test cepat.
            registry.add("app.seed-wilayah") { "false" }
        }
    }

    @Test
    fun contextLoads() {
    }
}

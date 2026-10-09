package id.caritas_kopi.be

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Regresi status HTTP: dispatch ERROR (forward ke /error) tidak boleh menimpa
 * status asli jadi 401, dan exception bawaan Spring tidak boleh jadi 500.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ErrorStatusTest {

    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:17-alpine")

        @JvmStatic
        @DynamicPropertySource
        fun props(registry: DynamicPropertyRegistry) {
            registry.add("DB_URL") { postgres.jdbcUrl }
            registry.add("DB_USER") { postgres.username }
            registry.add("DB_PASSWORD") { postgres.password }
            registry.add("app.seed-wilayah") { "false" }
        }
    }

    @Autowired
    lateinit var mvc: MockMvc

    @Test
    fun unauthenticatedIs401() {
        mvc.perform(get("/api/petani")).andExpect(status().isUnauthorized)
    }

    @Test
    fun enumeratorOnAdminPathIs403Not401() {
        mvc.perform(get("/api/admin/users").with(user("e").roles("ENUMERATOR")))
            .andExpect(status().isForbidden)
    }

    @Test
    fun adminOnUnknownPathIs404Not500() {
        mvc.perform(get("/api/tidak-ada").with(user("a").roles("ADMIN")))
            .andExpect(status().isNotFound)
    }
}

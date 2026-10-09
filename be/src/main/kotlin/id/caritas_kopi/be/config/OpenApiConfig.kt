package id.caritas_kopi.be.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun openApi(): OpenAPI = OpenAPI().info(
        Info()
            .title("Database Kopi API")
            .description("REST API pendataan baseline desa & petani kopi (Caritas Kopi).")
            .version("0.1.0"),
    )
}

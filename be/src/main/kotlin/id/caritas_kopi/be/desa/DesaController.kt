package id.caritas_kopi.be.desa

import id.caritas_kopi.be.auth.authPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/desa")
class DesaController(private val service: DesaService) {

    @GetMapping
    fun list(auth: Authentication): List<DesaListDto> = service.list(auth.authPrincipal())

    @GetMapping("/{id}")
    fun detail(@PathVariable id: UUID, auth: Authentication): DesaDetailDto =
        service.detail(id, auth.authPrincipal())

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody req: BaselineDesaRequest, auth: Authentication): Map<String, String> =
        mapOf("id" to service.create(req, auth.authPrincipal()).toString())

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody req: BaselineDesaRequest, auth: Authentication) =
        service.update(id, req, auth.authPrincipal())

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID, auth: Authentication) = service.delete(id, auth.authPrincipal())
}

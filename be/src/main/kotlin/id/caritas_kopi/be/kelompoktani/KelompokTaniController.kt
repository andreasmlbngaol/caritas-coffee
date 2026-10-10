package id.caritas_kopi.be.kelompoktani

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/kelompok-tani")
class KelompokTaniController(private val service: KelompokTaniService) {

    @GetMapping
    fun list(@RequestParam(required = false) desa: String?): ResponseEntity<Any> {
        // Dengan ?desa= -> daftar ringkas untuk combobox; tanpa -> daftar lengkap.
        return if (desa != null) {
            ResponseEntity.ok(service.byDesa(desa))
        } else {
            ResponseEntity.ok(service.list())
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody req: KelompokTaniRequest): Map<String, String> =
        mapOf("id" to service.create(req).toString())

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @RequestBody req: KelompokTaniRequest) =
        service.update(id, req)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID) = service.delete(id)
}

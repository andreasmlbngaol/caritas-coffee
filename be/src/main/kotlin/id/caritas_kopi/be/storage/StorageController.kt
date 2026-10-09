package id.caritas_kopi.be.storage

import id.caritas_kopi.be.common.ApiException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.Duration

@RestController
class StorageController(private val storage: StorageService) {

    @PostMapping("/api/upload")
    fun upload(@RequestParam("file") file: MultipartFile): Map<String, String> {
        if (file.isEmpty) throw ApiException.badRequest("Tidak ada file")
        return mapOf("key" to storage.savePetaniPhoto(file.contentType, file.bytes))
    }

    /** Serve foto; key = path setelah /api/foto/ (mis. petani/xxx.webp). */
    @GetMapping("/api/foto/**")
    fun foto(request: HttpServletRequest): ResponseEntity<ByteArray> {
        val key = request.requestURI.substringAfter("/api/foto/")
        val bytes = storage.load(key)
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(storage.contentTypeOf(key)))
            .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
            .body(bytes)
    }
}

package id.caritas_kopi.be.storage

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.webp.WebpWriter
import id.caritas_kopi.be.common.ApiException
import id.caritas_kopi.be.config.AppProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.UUID

/** Penyimpanan foto lokal (volume Docker). Menggantikan Cloudflare R2. */
@Service
class StorageService(private val props: AppProperties) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val root: Path = Path.of(props.storage.path).toAbsolutePath().normalize()

    init {
        Files.createDirectories(root.resolve("petani"))
        log.info("Storage root: {}", root)
    }

    private val allowed = mapOf(
        "image/jpeg" to "jpg",
        "image/png" to "png",
        "image/webp" to "webp",
        "image/heic" to "heic",
        "image/heif" to "heif",
    )

    /** Proses + simpan foto; kembalikan key relatif (mis. `petani/uuid.webp`). */
    fun savePetaniPhoto(contentType: String?, bytes: ByteArray): String {
        val allowedExt = allowed[contentType]
            ?: throw ApiException.badRequest("Format harus JPG, PNG, WEBP, atau HEIC")

        // Verifikasi magic bytes: jangan percaya content-type dari klien. Kalau
        // isinya bukan gambar, tolak - mencegah file sembarang disajikan ulang
        // oleh /api/foto dengan Content-Type gambar.
        if (!looksLikeImage(contentType, bytes)) {
            throw ApiException.badRequest("File bukan gambar yang valid")
        }

        var data = bytes
        var ext = allowedExt

        // Kecilkan: rotate EXIF -> resize maks 1920px -> WebP q90.
        // HEIC/HEIF tidak diproses (tak didukung) -> simpan apa adanya.
        try {
            val image = ImmutableImage.loader().fromBytes(bytes)
            val factor = minOf(1920.0 / image.width, 1920.0 / image.height, 1.0)
            val scaled = if (factor < 1.0) image.scale(factor) else image
            data = scaled.bytes(WebpWriter.DEFAULT)
            ext = "webp"
        } catch (e: Exception) {
            log.warn("Konversi gambar gagal, simpan asli: {}", e.message)
        }

        val key = "petani/${UUID.randomUUID()}.$ext"
        val target = resolve(key)
        Files.createDirectories(target.parent)
        Files.write(target, data, StandardOpenOption.CREATE_NEW)
        return key
    }

    /** Cek signature file sesuai content-type yang diklaim. */
    private fun looksLikeImage(contentType: String?, bytes: ByteArray): Boolean {
        fun startsWith(prefix: IntArray): Boolean =
            bytes.size >= prefix.size && prefix.indices.all { bytes[it].toInt() and 0xFF == prefix[it] }

        fun ascii(offset: Int, s: String): Boolean =
            bytes.size >= offset + s.length && s.indices.all { bytes[offset + it].toInt() == s[it].code }

        return when (contentType) {
            "image/jpeg" -> startsWith(intArrayOf(0xFF, 0xD8, 0xFF))
            "image/png" -> startsWith(intArrayOf(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))
            "image/webp" -> ascii(0, "RIFF") && ascii(8, "WEBP")
            "image/heic", "image/heif" -> ascii(4, "ftyp")
            else -> false
        }
    }

    /** Baca file berdasarkan key (aman dari path traversal). */
    fun load(key: String): ByteArray {
        if (key.contains("..") || !key.startsWith("petani/")) {
            throw ApiException.notFound("Not found")
        }
        val path = resolve(key)
        if (!Files.exists(path)) throw ApiException.notFound("Not found")
        return Files.readAllBytes(path)
    }

    fun contentTypeOf(key: String): String = when (key.substringAfterLast('.', "").lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        "heic" -> "image/heic"
        "heif" -> "image/heif"
        else -> "application/octet-stream"
    }

    /** Resolve path di bawah root; tolak bila keluar root. */
    private fun resolve(key: String): Path {
        val path = root.resolve(key).normalize()
        if (!path.startsWith(root)) throw ApiException.notFound("Not found")
        return path
    }
}

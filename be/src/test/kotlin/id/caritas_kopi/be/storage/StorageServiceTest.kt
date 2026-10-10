package id.caritas_kopi.be.storage

import id.caritas_kopi.be.common.ApiException
import id.caritas_kopi.be.config.AppProperties
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** Batas keamanan penyimpanan lokal: path traversal & validasi gambar. */
class StorageServiceTest {

    private fun service(root: Path): StorageService = StorageService(
        AppProperties().apply { storage.path = root.toString() },
    )

    @Test
    fun `load menolak key dengan dotdot`(@TempDir dir: Path) {
        val svc = service(dir)
        assertFailsWith<ApiException> { svc.load("petani/../../etc/passwd") }
        assertFailsWith<ApiException> { svc.load("../secret") }
    }

    @Test
    fun `load menolak key di luar prefix petani`(@TempDir dir: Path) {
        val svc = service(dir)
        assertFailsWith<ApiException> { svc.load("other/x.webp") }
        assertFailsWith<ApiException> { svc.load("/etc/passwd") }
    }

    @Test
    fun `load key tidak ada - not found`(@TempDir dir: Path) {
        val svc = service(dir)
        assertFailsWith<ApiException> { svc.load("petani/hilang.webp") }
    }

    @Test
    fun `load mengembalikan isi file yang ditulis`(@TempDir dir: Path) {
        val svc = service(dir)
        val f = dir.resolve("petani/ada.webp")
        Files.createDirectories(f.parent)
        Files.write(f, byteArrayOf(1, 2, 3))
        assertTrue(svc.load("petani/ada.webp").contentEquals(byteArrayOf(1, 2, 3)))
    }

    @Test
    fun `upload menolak content-type tak diizinkan`(@TempDir dir: Path) {
        val svc = service(dir)
        assertFailsWith<ApiException> { svc.savePetaniPhoto("text/plain", "halo".toByteArray()) }
        assertFailsWith<ApiException> { svc.savePetaniPhoto(null, byteArrayOf()) }
    }

    @Test
    fun `upload menolak isi yang bukan gambar walau content-type gambar`(@TempDir dir: Path) {
        val svc = service(dir)
        // Klaim PNG, isi teks -> harus ditolak (magic bytes).
        assertFailsWith<ApiException> { svc.savePetaniPhoto("image/png", "bukan gambar".toByteArray()) }
    }

    @Test
    fun `upload PNG valid tersimpan dengan key petani`(@TempDir dir: Path) {
        val svc = service(dir)
        // Signature PNG minimal + payload; Scrimage gagal decode -> disimpan asli.
        val png = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A) + ByteArray(16)
        val key = svc.savePetaniPhoto("image/png", png)
        assertTrue(key.startsWith("petani/"), "key = $key")
        assertEquals("image/png", svc.contentTypeOf(key))
        assertTrue(svc.load(key).contentEquals(png))
    }

    @Test
    fun `contentTypeOf memetakan ekstensi`(@TempDir dir: Path) {
        val svc = service(dir)
        assertEquals("image/webp", svc.contentTypeOf("petani/a.webp"))
        assertEquals("image/jpeg", svc.contentTypeOf("petani/a.jpg"))
        assertEquals("application/octet-stream", svc.contentTypeOf("petani/a.bin"))
    }
}

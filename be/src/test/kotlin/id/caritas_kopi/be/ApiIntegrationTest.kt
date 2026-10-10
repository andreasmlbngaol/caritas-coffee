package id.caritas_kopi.be

import id.caritas_kopi.be.auth.AuthPrincipal
import id.caritas_kopi.be.user.Role
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.UUID

/**
 * Regresi jalur tulis API yang rawan:
 * - update petani mengganti seluruh anak (delete-lalu-insert) tanpa melanggar
 *   UNIQUE(petani_id, jenis/tahun) - lihat PetaniService.update.
 * - kelompokTaniId bukan UUID -> 400 (bukan 500).
 * - enumerator tidak bisa membaca data enumerator lain -> 404.
 * - analitik menghitung petani unik, bukan jumlah baris produk.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ApiIntegrationTest {

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
            // Origin tepercaya = appUrl; CORS diisi sama agar Spring CORS tidak
            // menolak lebih dulu (di prod same-origin daftar CORS kosong).
            registry.add("app.app-url") { ORIGIN }
            registry.add("app.cors-allowed-origins") { ORIGIN }
        }

        val DESA = "11.01.01.2001"
        const val ORIGIN = "http://localhost:8088"
    }

    @Autowired lateinit var mvc: MockMvc
    @Autowired lateinit var jdbc: JdbcTemplate
    @Autowired lateinit var passwordEncoder: org.springframework.security.crypto.password.PasswordEncoder
    @Autowired lateinit var throttle: id.caritas_kopi.be.auth.LoginThrottle

    private val enumA = UUID.randomUUID()
    private val enumB = UUID.randomUUID()
    private val admin = UUID.randomUUID()

    private fun auth(id: UUID, role: Role) = SecurityMockMvcRequestPostProcessors.authentication(
        UsernamePasswordAuthenticationToken(
            AuthPrincipal(id, if (role == Role.ADMIN) "admin" else "enum", "Tester", role),
            null,
            AuthPrincipal(id, "x", "Tester", role).authorities(),
        ),
    )

    @BeforeEach
    fun setUp() {
        throttle.clear()
        jdbc.update("delete from petani")
        jdbc.update("delete from baseline_desa")
        jdbc.update("delete from kelompok_tani")
        jdbc.update("delete from users")
        jdbc.update("delete from wilayah_desa")
        jdbc.update("delete from wilayah_kecamatan")
        jdbc.update("delete from wilayah_kabupaten")
        jdbc.update("delete from wilayah_provinsi")

        jdbc.update("insert into wilayah_provinsi(kode,nama) values ('11','Jawa Barat')")
        jdbc.update("insert into wilayah_kabupaten(kode,nama,provinsi_kode) values ('11.01','Garut','11')")
        jdbc.update("insert into wilayah_kecamatan(kode,nama,kabupaten_kode) values ('11.01.01','Cikajang','11.01')")
        jdbc.update("insert into wilayah_desa(kode,nama,kecamatan_kode) values (?, 'Sukamaju','11.01.01')", DESA)

        val hash = passwordEncoder.encode("secret123")!!
        for ((id, name, role) in listOf(Triple(enumA, "enumA", "ENUMERATOR"), Triple(enumB, "enumB", "ENUMERATOR"), Triple(admin, "admin", "ADMIN"))) {
            jdbc.update(
                "insert into users(id,username,password_hash,full_name,role,is_active) values (?,?,?,?,?,true)",
                id, name, hash, name, role,
            )
        }
    }

    private fun body(cherry: Double, extraProduk: String = ""): String = """
        {
          "desaKode": "$DESA",
          "namaLengkap": "Budi Santoso",
          "praktikGap": [{"jenis": "PEMANGKASAN_KOPI", "jawaban": "YA"}],
          "produksi": [{"tahun": 2025, "satuan": "KG", "cherry": $cherry}],
          "produk": [{"jenis": "CHERRY", "dijual": true, "volumeKgTahun": 50.0}$extraProduk],
          "pasar": [{"kategori": "KOMERSIAL", "aktif": true, "persentase": 100.0}],
          "kondisiKebun": [{"jenis": "KEPEMILIKAN_JELAS", "jawaban": true}]
        }
    """.trimIndent()

    private fun createPetani(authId: UUID, body: String): String {
        val res = mvc.perform(post("/api/petani").with(auth(authId, Role.ENUMERATOR)).contentType("application/json").content(body))
            .andReturn()
        check(res.response.status == 201) { "create failed: ${res.response.status} ${res.response.contentAsString}\nBODY=$body" }
        val id = Regex("\"id\"\\s*:\\s*\"([^\"]+)\"").find(res.response.contentAsString)!!.groupValues[1]
        return id
    }

    @Test
    fun updatePetaniReplacesChildrenWithoutUniqueViolation() {
        val id = createPetani(enumA, body(100.0))

        // Update dgn jenis/tahun anak yang SAMA - memicu delete-lalu-insert.
        mvc.perform(put("/api/petani/$id").with(auth(enumA, Role.ENUMERATOR)).contentType("application/json").content(body(200.0)))
            .andExpect(status().isOk)

        mvc.perform(get("/api/petani/$id").with(auth(enumA, Role.ENUMERATOR)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.produksi.length()").value(1))
            .andExpect(jsonPath("$.produksi[0].cherry").value(200.0))
            .andExpect(jsonPath("$.praktikGap.length()").value(1))
    }

    @Test
    fun malformedKelompokIdIsBadRequest() {
        val payload = body(10.0).replaceFirst("\"namaLengkap\"", "\"kelompokTaniId\": \"not-a-uuid\", \"namaLengkap\"")
        mvc.perform(post("/api/petani").with(auth(enumA, Role.ENUMERATOR)).contentType("application/json").content(payload))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun enumeratorCannotReadOthersPetani() {
        val id = createPetani(enumA, body(10.0))
        mvc.perform(get("/api/petani/$id").with(auth(enumB, Role.ENUMERATOR)))
            .andExpect(status().isNotFound)
    }

    @Test
    fun responseMengirimFieldNullEksplisit() {
        // Kontrak FE: field opsional bertipe `X | null`, jadi JSON harus berisi null
        // eksplisit (bukan key yang hilang -> terbaca undefined di JS).
        // namaPanggilan sengaja tidak diisi -> harus ada sebagai null.
        // value(nullValue()) menuntut key ADA dan bernilai null: bila key hilang,
        // JsonPath melempar "No value at JSON path" (bukan lolos). Jadi ini
        // membedakan null-eksplisit dari key-yang-di-omit.
        val id = createPetani(enumA, body(10.0))
        mvc.perform(get("/api/petani/$id").with(auth(enumA, Role.ENUMERATOR)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.namaPanggilan").value(org.hamcrest.Matchers.nullValue()))
            .andExpect(jsonPath("$.telepon").value(org.hamcrest.Matchers.nullValue()))
            .andExpect(jsonPath("$.kelompokTani").value(org.hamcrest.Matchers.nullValue()))
    }

    @Test
    fun analyticsCountsDistinctPetani() {
        // Satu petani, dua baris produk dgn jenis sama -> harus dihitung 1 petani.
        val extra = ",{\"jenis\": \"CHERRY\", \"dijual\": true, \"volumeKgTahun\": 20.0}"
        createPetani(enumA, body(10.0, extra))

        mvc.perform(get("/api/analitik/pasar-produk").with(auth(admin, Role.ADMIN)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.produk[0].petani").value(1))
            .andExpect(jsonPath("$.produk[0].volume").value(70.0))
    }

    @Test
    fun kelompokListCountsPetaniInOneQuery() {
        val ktId = jdbc.queryForObject(
            "insert into kelompok_tani(nama,kode,desa_kode) values ('Tani Maju','A','" + DESA + "') returning id",
            UUID::class.java,
        )!!
        // Dua petani di kelompok yang sama.
        createPetani(enumA, body(10.0).replaceFirst("\"namaLengkap\"", "\"kelompokTaniId\": \"$ktId\", \"namaLengkap\""))
        createPetani(enumB, body(20.0).replaceFirst("\"namaLengkap\"", "\"kelompokTaniId\": \"$ktId\", \"namaLengkap\""))

        mvc.perform(get("/api/kelompok-tani").with(auth(admin, Role.ADMIN)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].jumlahPetani").value(2))
    }

    @Test
    fun desaCreateUpdateRoundtripReplacesKebijakan() {
        // Baseline desa + anak kebijakan/kelembagaan - update mengganti seluruh anak.
        fun desaBody(kebijakan: String): String = """
            {"desaKode":"$DESA","tahunPendataan":2025,
             "kebijakan":[$kebijakan],
             "kelembagaan":[{"jenis":"KELOMPOK_TANI","jumlah":3}]}
        """.trimIndent()

        val res = mvc.perform(
            post("/api/desa").with(auth(enumA, Role.ENUMERATOR)).contentType("application/json")
                .content(desaBody("""{"jenis":"RPJM_DESA","ada":true}""")),
        ).andExpect(status().isCreated).andReturn()
        val id = Regex("\"id\"\\s*:\\s*\"([^\"]+)\"").find(res.response.contentAsString)!!.groupValues[1]

        // Update dgn jenis kebijakan berbeda -> anak lama dibuang, tidak menumpuk.
        mvc.perform(
            put("/api/desa/$id").with(auth(enumA, Role.ENUMERATOR)).contentType("application/json")
                .content(desaBody("""{"jenis":"RPJM_DESA","ada":true},{"jenis":"PERDES_PERTANIAN","ada":false}""")),
        ).andExpect(status().isOk)

        mvc.perform(get("/api/desa/$id").with(auth(enumA, Role.ENUMERATOR)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.kebijakan.length()").value(2))
            .andExpect(jsonPath("$.kelembagaan.length()").value(1))
    }

    @Test
    fun enumeratorCannotExportOthersPetani() {
        val id = createPetani(enumA, body(10.0))
        mvc.perform(get("/api/petani/$id/export/pdf").with(auth(enumB, Role.ENUMERATOR)))
            .andExpect(status().isNotFound)
    }

    @Test
    fun originCheckBlocksForeignOriginButAllowsAppOrigin() {
        val payload = body(10.0)
        // Origin asing -> 403 sebelum sampai controller.
        mvc.perform(
            post("/api/petani").with(auth(enumA, Role.ENUMERATOR))
                .header("Origin", "https://evil.example").contentType("application/json").content(payload),
        ).andExpect(status().isForbidden)

        // Origin aplikasi (appUrl tepercaya) -> diterima.
        mvc.perform(
            post("/api/petani").with(auth(enumA, Role.ENUMERATOR))
                .header("Origin", ORIGIN).contentType("application/json").content(payload),
        ).andExpect(status().isCreated)
    }

    @Test
    fun resetPasswordInvalidatesExistingSession() {
        // Login -> simpan cookie sesi.
        val login = mvc.perform(
            post("/api/auth/login").contentType("application/json")
                .content("""{"username":"enumA","password":"secret123"}"""),
        ).andExpect(status().isOk).andReturn()
        val cookie = login.response.getCookie("kopi_session")!!

        // Sesi valid sebelum reset.
        mvc.perform(get("/api/petani").cookie(cookie)).andExpect(status().isOk)

        // Admin reset password -> token lama harus ditolak.
        mvc.perform(post("/api/admin/users/$enumA/reset-password").with(auth(admin, Role.ADMIN)))
            .andExpect(status().isOk)

        mvc.perform(get("/api/petani").cookie(cookie)).andExpect(status().isUnauthorized)
    }

    @Test
    fun desaTidakAdaDitolakBadRequestBukanConflict() {
        // FK violation (desa palsu) harus jadi 400 dgn pesan "terkait", bukan 409 "duplikat".
        val payload = body(10.0).replaceFirst("\"desaKode\": \"$DESA\"", "\"desaKode\": \"99.99.99.9999\"")
        mvc.perform(post("/api/petani").with(auth(enumA, Role.ENUMERATOR)).contentType("application/json").content(payload))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("terkait")))
    }

    @Test
    fun loginDiblokirSetelahTerlaluBanyakPercobaanGagal() {
        val bad = """{"username":"enumA","password":"salah"}"""
        // 10 percobaan gagal diizinkan (401), percobaan ke-11 diblokir (429).
        repeat(10) {
            mvc.perform(post("/api/auth/login").contentType("application/json").content(bad))
                .andExpect(status().isUnauthorized)
        }
        mvc.perform(post("/api/auth/login").contentType("application/json").content(bad))
            .andExpect(status().isTooManyRequests)

        // Password benar tetap diblokir selama window (proteksi brute-force).
        mvc.perform(
            post("/api/auth/login").contentType("application/json")
                .content("""{"username":"enumA","password":"secret123"}"""),
        ).andExpect(status().isTooManyRequests)
    }

    @Test
    fun adminBisaBuatDanResetUserEnumerator() {
        // Buat user -> dapat kredensial -> bisa login.
        val created = mvc.perform(
            post("/api/admin/users").with(auth(admin, Role.ADMIN)).contentType("application/json")
                .content("""{"username":"petugas1","fullName":"Petugas Satu"}"""),
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value("petugas1"))
            .andReturn()
        val pw = Regex("\"password\"\\s*:\\s*\"([^\"]+)\"").find(created.response.contentAsString)!!.groupValues[1]
        val newId = jdbc.queryForObject("select id from users where username='petugas1'", UUID::class.java)!!

        mvc.perform(
            post("/api/auth/login").contentType("application/json")
                .content("""{"username":"petugas1","password":"$pw"}"""),
        ).andExpect(status().isOk)

        // Reset password -> token lama invalid.
        val login = mvc.perform(
            post("/api/auth/login").contentType("application/json")
                .content("""{"username":"petugas1","password":"$pw"}"""),
        ).andReturn()
        val cookie = login.response.getCookie("kopi_session")!!
        mvc.perform(post("/api/admin/users/$newId/reset-password").with(auth(admin, Role.ADMIN)))
            .andExpect(status().isOk)
        mvc.perform(get("/api/petani").cookie(cookie)).andExpect(status().isUnauthorized)
    }

    @Test
    fun usernameDuplikatDitolakConflict() {
        mvc.perform(
            post("/api/admin/users").with(auth(admin, Role.ADMIN)).contentType("application/json")
                .content("""{"username":"admin","fullName":"Dobel"}"""),
        ).andExpect(status().isConflict)
    }

    @Test
    fun usernameTidakValidDitolakBadRequest() {
        for (bad in listOf("ab", "HURUFBESAR", "ada spasi", "simbol!")) {
            mvc.perform(
                post("/api/admin/users").with(auth(admin, Role.ADMIN)).contentType("application/json")
                    .content("""{"username":"$bad","fullName":"X"}"""),
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun tidakBisaNonaktifkanAkunSendiri() {
        mvc.perform(patch("/api/admin/users/$admin/toggle-active").with(auth(admin, Role.ADMIN)))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun enumeratorTidakBisaAksesAdminUsers() {
        mvc.perform(get("/api/admin/users").with(auth(enumA, Role.ENUMERATOR)))
            .andExpect(status().isForbidden)
    }

    @Test
    fun wilayahLookupCascading() {
        // Tanpa parent -> provinsi.
        mvc.perform(get("/api/wilayah").with(auth(enumA, Role.ENUMERATOR)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].kode").value("11"))
        // Parent 1 segmen -> kabupaten.
        mvc.perform(get("/api/wilayah?parent=11").with(auth(enumA, Role.ENUMERATOR)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].kode").value("11.01"))
        // Parent 2 segmen -> kecamatan.
        mvc.perform(get("/api/wilayah?parent=11.01").with(auth(enumA, Role.ENUMERATOR)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].kode").value("11.01.01"))
        // Parent 3 segmen -> desa.
        mvc.perform(get("/api/wilayah?parent=11.01.01").with(auth(enumA, Role.ENUMERATOR)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].kode").value(DESA))
    }

    @Test
    fun uploadMenolakFileBukanGambar() {
        // Klaim PNG, isi teks -> magic bytes menolak (400).
        val file = org.springframework.mock.web.MockMultipartFile(
            "file", "x.png", "image/png", "bukan gambar".toByteArray(),
        )
        mvc.perform(multipart("/api/upload").file(file).with(auth(enumA, Role.ENUMERATOR)))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun uploadTanpaFileDitolak() {
        val file = org.springframework.mock.web.MockMultipartFile("file", "x.png", "image/png", ByteArray(0))
        mvc.perform(multipart("/api/upload").file(file).with(auth(enumA, Role.ENUMERATOR)))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun enumeratorTidakBisaHapusPetaniOrangLain() {
        val id = createPetani(enumA, body(10.0))
        mvc.perform(delete("/api/petani/$id").with(auth(enumB, Role.ENUMERATOR)))
            .andExpect(status().isNotFound)
        // Masih ada.
        mvc.perform(get("/api/petani/$id").with(auth(enumA, Role.ENUMERATOR))).andExpect(status().isOk)
    }
}

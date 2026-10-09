package id.caritas_kopi.be.kelompoktani

import id.caritas_kopi.be.common.ApiException
import id.caritas_kopi.be.petani.PetaniRepository
import id.caritas_kopi.be.wilayah.WilayahLookupService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

data class KelompokTaniDto(
    val id: String,
    val nama: String,
    val kode: String?,
    val desa: String,
    val desaKode: String,
    val kecamatan: String?,
    val jumlahPetani: Long,
    val createdAt: Instant,
)

data class KelompokTaniRequest(val nama: String, val kode: String? = null, val desaKode: String)

@Service
class KelompokTaniService(
    private val repo: KelompokTaniRepository,
    private val petaniRepo: PetaniRepository,
    private val wilayah: WilayahLookupService,
) {

    @Transactional(readOnly = true)
    fun list(): List<KelompokTaniDto> {
        val rows = repo.findAllByOrderByNamaAsc()
        val desaMap = wilayah.desaMap(rows.map { it.desaKode })
        val kecMap = wilayah.kecamatanMap(desaMap.values.map { it.kecamatanKode })
        return rows.map { kt ->
            val desa = desaMap[kt.desaKode]
            KelompokTaniDto(
                id = kt.id.toString(),
                nama = kt.nama,
                kode = kt.kode,
                desa = desa?.nama ?: "-",
                desaKode = kt.desaKode,
                kecamatan = desa?.let { kecMap[it.kecamatanKode]?.nama },
                jumlahPetani = petaniRepo.countByKelompokTaniId(kt.id!!),
                createdAt = kt.createdAt,
            )
        }
    }

    /** Daftar ringkas untuk combobox (by desa). */
    @Transactional(readOnly = true)
    fun byDesa(desaKode: String): List<Map<String, String?>> =
        repo.findByDesaKodeOrderByNamaAsc(desaKode).map {
            mapOf("id" to it.id.toString(), "nama" to it.nama, "kode" to it.kode)
        }

    @Transactional
    fun create(req: KelompokTaniRequest): UUID {
        validate(req)
        val kode = normKode(req.kode)
        if (kode != null && repo.findByDesaKodeAndKode(req.desaKode, kode).isPresent) {
            throw ApiException.conflict("Kode $kode sudah dipakai di desa ini")
        }
        return repo.save(KelompokTani(nama = req.nama.trim(), kode = kode, desaKode = req.desaKode)).id!!
    }

    @Transactional
    fun update(id: UUID, req: KelompokTaniRequest) {
        val kt = repo.findById(id).orElseThrow { ApiException.notFound("Data tidak ditemukan") }
        validate(req)
        val kode = normKode(req.kode)
        if (kode != null && (kode != kt.kode || req.desaKode != kt.desaKode) &&
            repo.findByDesaKodeAndKode(req.desaKode, kode).isPresent
        ) {
            throw ApiException.conflict("Kode $kode sudah dipakai di desa ini")
        }
        kt.nama = req.nama.trim()
        kt.kode = kode
        kt.desaKode = req.desaKode
        repo.save(kt)
    }

    @Transactional
    fun delete(id: UUID) {
        val kt = repo.findById(id).orElseThrow { ApiException.notFound("Data tidak ditemukan") }
        val dipakai = petaniRepo.countByKelompokTaniId(id)
        if (dipakai > 0) {
            throw ApiException.conflict(
                "Kelompok ini masih dipakai oleh $dipakai petani. Lepaskan dulu petani-petaninya dari kelompok ini (lewat edit petani).",
            )
        }
        repo.delete(kt)
    }

    private fun validate(req: KelompokTaniRequest) {
        if (req.nama.isBlank()) throw ApiException.badRequest("Nama kelompok wajib diisi")
        if (req.desaKode.isBlank()) throw ApiException.badRequest("Desa wajib dipilih")
    }

    private fun normKode(kode: String?): String? = kode?.trim()?.takeIf { it.isNotEmpty() }?.uppercase()
}

package id.caritas_kopi.be.wilayah

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class WilayahDto(val kode: String, val nama: String)

@RestController
@RequestMapping("/api/wilayah")
class WilayahController(
    private val provinsiRepo: WilayahProvinsiRepository,
    private val kabupatenRepo: WilayahKabupatenRepository,
    private val kecamatanRepo: WilayahKecamatanRepository,
    private val desaRepo: WilayahDesaRepository,
) {

    /** Lookup cascading: tanpa parent -> provinsi; dengan parent -> level berikutnya. */
    @GetMapping
    fun lookup(@RequestParam(required = false) parent: String?): List<WilayahDto> {
        if (parent.isNullOrBlank()) {
            return provinsiRepo.findAllByOrderByNamaAsc().map { WilayahDto(it.kode, it.nama) }
        }
        return when (parent.split(".").size) {
            1 -> kabupatenRepo.findByProvinsiKodeOrderByNamaAsc(parent).map { WilayahDto(it.kode, it.nama) }
            2 -> kecamatanRepo.findByKabupatenKodeOrderByNamaAsc(parent).map { WilayahDto(it.kode, it.nama) }
            else -> desaRepo.findByKecamatanKodeOrderByNamaAsc(parent).map { WilayahDto(it.kode, it.nama) }
        }
    }
}

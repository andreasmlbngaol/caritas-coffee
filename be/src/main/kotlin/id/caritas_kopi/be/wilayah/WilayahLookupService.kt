package id.caritas_kopi.be.wilayah

import org.springframework.stereotype.Service

/** Resolve nama lengkap desa (desa, kecamatan, kabupaten, provinsi) secara batch. */
@Service
class WilayahLookupService(
    private val provinsiRepo: WilayahProvinsiRepository,
    private val kabupatenRepo: WilayahKabupatenRepository,
    private val kecamatanRepo: WilayahKecamatanRepository,
    private val desaRepo: WilayahDesaRepository,
) {

    fun desaMap(kodes: Collection<String>): Map<String, WilayahDesa> {
        if (kodes.isEmpty()) return emptyMap()
        return desaRepo.findByKodeIn(kodes.toSet()).associateBy { it.kode }
    }

    fun kecamatanMap(kodes: Collection<String>): Map<String, WilayahKecamatan> =
        if (kodes.isEmpty()) emptyMap() else kecamatanRepo.findAllById(kodes.toSet()).associateBy { it.kode }

    fun kabupatenMap(kodes: Collection<String>): Map<String, WilayahKabupaten> =
        if (kodes.isEmpty()) emptyMap() else kabupatenRepo.findAllById(kodes.toSet()).associateBy { it.kode }

    fun provinsiMap(kodes: Collection<String>): Map<String, WilayahProvinsi> =
        if (kodes.isEmpty()) emptyMap() else provinsiRepo.findAllById(kodes.toSet()).associateBy { it.kode }
}

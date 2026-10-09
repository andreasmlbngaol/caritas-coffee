package id.caritas_kopi.be.wilayah

import org.springframework.data.jpa.repository.JpaRepository

interface WilayahProvinsiRepository : JpaRepository<WilayahProvinsi, String> {
    fun findAllByOrderByNamaAsc(): List<WilayahProvinsi>
}

interface WilayahKabupatenRepository : JpaRepository<WilayahKabupaten, String> {
    fun findByProvinsiKodeOrderByNamaAsc(provinsiKode: String): List<WilayahKabupaten>
}

interface WilayahKecamatanRepository : JpaRepository<WilayahKecamatan, String> {
    fun findByKabupatenKodeOrderByNamaAsc(kabupatenKode: String): List<WilayahKecamatan>
}

interface WilayahDesaRepository : JpaRepository<WilayahDesa, String> {
    fun findByKecamatanKodeOrderByNamaAsc(kecamatanKode: String): List<WilayahDesa>
    fun findByKodeIn(kodes: Collection<String>): List<WilayahDesa>
}

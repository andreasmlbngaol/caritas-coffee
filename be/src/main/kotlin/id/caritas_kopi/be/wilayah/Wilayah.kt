package id.caritas_kopi.be.wilayah

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(name = "wilayah_provinsi")
class WilayahProvinsi(
    @Id
    var kode: String = "",

    @Column(nullable = false)
    var nama: String = "",
)

@Entity
@Table(name = "wilayah_kabupaten", indexes = [Index(name = "idx_kabupaten_provinsi", columnList = "provinsi_kode")])
class WilayahKabupaten(
    @Id
    var kode: String = "",

    @Column(nullable = false)
    var nama: String = "",

    @Column(name = "provinsi_kode", nullable = false)
    var provinsiKode: String = "",
)

@Entity
@Table(name = "wilayah_kecamatan", indexes = [Index(name = "idx_kecamatan_kabupaten", columnList = "kabupaten_kode")])
class WilayahKecamatan(
    @Id
    var kode: String = "",

    @Column(nullable = false)
    var nama: String = "",

    @Column(name = "kabupaten_kode", nullable = false)
    var kabupatenKode: String = "",
)

@Entity
@Table(name = "wilayah_desa", indexes = [Index(name = "idx_desa_kecamatan", columnList = "kecamatan_kode")])
class WilayahDesa(
    @Id
    var kode: String = "",

    @Column(nullable = false)
    var nama: String = "",

    @Column(name = "kecamatan_kode", nullable = false)
    var kecamatanKode: String = "",
)

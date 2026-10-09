package id.caritas_kopi.be.petani

import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.PositiveOrZero
import java.time.Instant
import java.time.LocalDate

// ---------- Request ----------

data class PetaniRequest(
    @field:NotBlank(message = "Desa wajib dipilih")
    val desaKode: String,
    @field:NotBlank(message = "Nama lengkap wajib diisi")
    val namaLengkap: String,
    val namaPanggilan: String? = null,
    val jenisKelamin: JenisKelamin? = null,
    val tanggalLahir: LocalDate? = null,
    val alamatDomisili: String? = null,
    val telepon: String? = null,
    val tanggalPendaftaran: LocalDate? = null,
    val namaPetugasPendaftar: String? = null,
    val kontakDaruratNama: String? = null,
    val kontakDaruratTelepon: String? = null,
    val kontakDaruratHubungan: String? = null,
    val kodePetani: String? = null,
    // Kelompok: pilih yang ada ATAU buat baru.
    val kelompokTaniId: String? = null,
    @field:Valid
    val kelompokTaniBaru: KelompokBaruRequest? = null,
    @field:Valid
    val plot: List<PlotRequest> = emptyList(),
    @field:Valid
    val naungan: List<NaunganRequest> = emptyList(),
    @field:NotEmpty(message = "Data GAP wajib lengkap")
    @field:Valid
    val praktikGap: List<GapRequest> = emptyList(),
    @field:NotEmpty(message = "Data produksi wajib lengkap")
    @field:Valid
    val produksi: List<ProduksiRequest> = emptyList(),
    @field:NotEmpty(message = "Data produk dijual wajib lengkap")
    @field:Valid
    val produk: List<ProdukRequest> = emptyList(),
    @field:NotEmpty(message = "Data pasar wajib lengkap")
    @field:Valid
    val pasar: List<PasarRequest> = emptyList(),
    @field:NotEmpty(message = "Data kondisi kebun wajib lengkap")
    @field:Valid
    val kondisiKebun: List<KondisiRequest> = emptyList(),
)

data class KelompokBaruRequest(val nama: String, val kode: String? = null)

data class PlotRequest(
    val namaHamparan: String? = null,
    val varietas: String? = null,
    val tahunTanam: List<@Min(1900) @Max(2100) Int> = emptyList(),
    val kodeGps: String? = null,
    @field:PositiveOrZero(message = "Elevasi tidak boleh negatif")
    val elevasiMdpl: Double? = null,
    @field:PositiveOrZero(message = "Kemiringan tidak boleh negatif")
    val kemiringanPersen: Double? = null,
    @field:PositiveOrZero(message = "Luas kopi tidak boleh negatif")
    val luasKopiHa: Double? = null,
    val fotoKey: String? = null,
    @field:DecimalMin(value = "-90", message = "Latitude minimal -90")
    @field:DecimalMax(value = "90", message = "Latitude maksimal 90")
    val fotoLatitude: Double? = null,
    @field:DecimalMin(value = "-180", message = "Longitude minimal -180")
    @field:DecimalMax(value = "180", message = "Longitude maksimal 180")
    val fotoLongitude: Double? = null,
    val statusKepemilikan: StatusKepemilikanLahan? = null,
    val sistemBudidaya: SistemBudidaya? = null,
    val areaKonservasi: String? = null,
    @field:PositiveOrZero(message = "Jumlah tanaman baru tidak boleh negatif")
    val tanamanBaru: Int? = null,
    @field:PositiveOrZero(message = "Jumlah pohon produktif tidak boleh negatif")
    val pohonProduktif: Int? = null,
    @field:PositiveOrZero(message = "Jumlah pohon tidak produktif tidak boleh negatif")
    val pohonTidakProduktif: Int? = null,
    val pestisidaNama: String? = null,
    val pestisidaBulanTahun: String? = null,
)

data class NaunganRequest(
    val jenis: String? = null,
    @field:PositiveOrZero(message = "Jumlah tidak boleh negatif")
    val jumlah: Int? = null,
    val fungsi: String? = null,
    val pemangkasan: Boolean? = null,
    val produksiPerTahun: String? = null,
    val tahunTanam: Int? = null,
)

data class GapRequest(
    val jenis: JenisPraktikGap,
    val jawaban: JawabanGap? = null,
    val keterangan: String? = null,
)

data class ProduksiRequest(
    val tahun: Int,
    val satuan: SatuanProduksi? = null,
    @field:PositiveOrZero(message = "Cherry tidak boleh negatif")
    val cherry: Double? = null,
    @field:PositiveOrZero(message = "Gabah basah tidak boleh negatif")
    val gabahBasah: Double? = null,
    @field:PositiveOrZero(message = "Gabah kering tidak boleh negatif")
    val gabahKering: Double? = null,
    @field:PositiveOrZero(message = "Green bean tidak boleh negatif")
    val greenBean: Double? = null,
)

data class ProdukRequest(
    val jenis: JenisProdukDijual,
    val labelCustom: String? = null,
    val dijual: Boolean = false,
    @field:PositiveOrZero(message = "Volume tidak boleh negatif")
    val volumeKgTahun: Double? = null,
)

data class PasarRequest(
    val kategori: KategoriPasar,
    val labelCustom: String? = null,
    val aktif: Boolean = false,
    @field:PositiveOrZero(message = "Persentase tidak boleh negatif")
    val persentase: Double? = null,
    val profilPenjual: String? = null,
)

data class KondisiRequest(
    val jenis: JenisKondisiKebun,
    val jawaban: Boolean = false,
    val keterangan: String? = null,
)

// ---------- Response ----------

data class KelompokTaniRingkas(val id: String, val nama: String, val kode: String?)

data class DesaRingkas(
    val kode: String,
    val nama: String,
    val kecamatan: String,
    val kabupaten: String,
    val provinsi: String,
)

data class PlotDto(
    val id: String,
    val nomor: Int,
    val namaHamparan: String?,
    val varietas: String?,
    val tahunTanam: List<Int>,
    val kodeGps: String?,
    val elevasiMdpl: Double?,
    val kemiringanPersen: Double?,
    val luasKopiHa: Double?,
    val fotoKey: String?,
    val fotoLatitude: Double?,
    val fotoLongitude: Double?,
    val statusKepemilikan: StatusKepemilikanLahan?,
    val sistemBudidaya: SistemBudidaya?,
    val areaKonservasi: String?,
    val tanamanBaru: Int?,
    val pohonProduktif: Int?,
    val pohonTidakProduktif: Int?,
    val pestisidaNama: String?,
    val pestisidaBulanTahun: String?,
)

data class NaunganDto(
    val id: String,
    val jenis: String?,
    val jumlah: Int?,
    val fungsi: String?,
    val pemangkasan: Boolean?,
    val produksiPerTahun: String?,
    val tahunTanam: Int?,
)

data class GapDto(val id: String, val jenis: JenisPraktikGap, val jawaban: JawabanGap?, val keterangan: String?)

data class ProduksiDto(
    val id: String,
    val tahun: Int,
    val satuan: SatuanProduksi?,
    val cherry: Double?,
    val gabahBasah: Double?,
    val gabahKering: Double?,
    val greenBean: Double?,
    val produktivitas: Double?,
)

data class ProdukDto(
    val id: String,
    val jenis: JenisProdukDijual,
    val labelCustom: String?,
    val dijual: Boolean,
    val volumeKgTahun: Double?,
)

data class PasarDto(
    val id: String,
    val kategori: KategoriPasar,
    val labelCustom: String?,
    val aktif: Boolean,
    val persentase: Double?,
    val profilPenjual: String?,
)

data class KondisiDto(val id: String, val jenis: JenisKondisiKebun, val jawaban: Boolean, val keterangan: String?)

data class PetaniListDto(
    val id: String,
    val kodePetani: String?,
    val namaLengkap: String,
    val desa: String,
    val kecamatan: String,
    val kelompokTani: String?,
    val kelompokTaniId: String?,
    val kelompokTaniKode: String?,
    val telepon: String?,
    val createdBy: String?,
    val createdAt: Instant,
)

data class PetaniDetailDto(
    val id: String,
    val kodePetani: String?,
    val namaLengkap: String,
    val namaPanggilan: String?,
    val jenisKelamin: JenisKelamin?,
    val tanggalLahir: LocalDate?,
    val alamatDomisili: String?,
    val telepon: String?,
    val tanggalPendaftaran: LocalDate?,
    val namaPetugasPendaftar: String?,
    val kontakDaruratNama: String?,
    val kontakDaruratTelepon: String?,
    val kontakDaruratHubungan: String?,
    val desa: DesaRingkas,
    val kelompokTani: KelompokTaniRingkas?,
    val createdBy: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val plot: List<PlotDto>,
    val naungan: List<NaunganDto>,
    val praktikGap: List<GapDto>,
    val produksi: List<ProduksiDto>,
    val produk: List<ProdukDto>,
    val pasar: List<PasarDto>,
    val kondisiKebun: List<KondisiDto>,
)

package id.caritas_kopi.be.desa

import id.caritas_kopi.be.petani.DesaRingkas
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.PositiveOrZero
import java.time.Instant

data class BaselineDesaRequest(
    @field:NotBlank(message = "Desa wajib dipilih")
    val desaKode: String,
    @field:Min(value = 2000, message = "Tahun pendataan minimal 2000")
    @field:Max(value = 2100, message = "Tahun pendataan maksimal 2100")
    val tahunPendataan: Int,
    val sumberData: String? = null,
    @field:PositiveOrZero(message = "Luas wilayah tidak boleh negatif")
    val luasWilayahHa: Double? = null,
    @field:PositiveOrZero(message = "Jumlah penduduk tidak boleh negatif")
    val jumlahPenduduk: Int? = null,
    @field:PositiveOrZero(message = "Jumlah KK tidak boleh negatif")
    val jumlahKK: Int? = null,
    @field:PositiveOrZero(message = "Jumlah petani kopi tidak boleh negatif")
    val jumlahPetaniKopi: Int? = null,
    @field:PositiveOrZero(message = "Luas areal kopi tidak boleh negatif")
    val luasArealKopiHa: Double? = null,
    @field:PositiveOrZero(message = "Luas komoditi lain tidak boleh negatif")
    val luasKomoditiLainHa: Double? = null,
    @field:DecimalMin(value = "-90", message = "Latitude minimal -90")
    @field:DecimalMax(value = "90", message = "Latitude maksimal 90")
    val latitude: Double? = null,
    @field:DecimalMin(value = "-180", message = "Longitude minimal -180")
    @field:DecimalMax(value = "180", message = "Longitude maksimal 180")
    val longitude: Double? = null,
    val topografi: String? = null,
    @field:PositiveOrZero(message = "Ketinggian tidak boleh negatif")
    val ketinggianMdpl: Double? = null,
    val bulanHujan: String? = null,
    val bulanKering: String? = null,
    @field:PositiveOrZero(message = "Suhu rata-rata tidak boleh negatif")
    val suhuRataRataC: Double? = null,
    val jenisTanah: String? = null,
    val aksesJalan: String? = null,
    @field:PositiveOrZero(message = "Jarak ibukota kecamatan tidak boleh negatif")
    val jarakIbukotaKecamatanKm: Double? = null,
    @field:PositiveOrZero(message = "Jarak pasar tidak boleh negatif")
    val jarakPasarKm: Double? = null,
    @field:PositiveOrZero(message = "Jarak konservasi tidak boleh negatif")
    val jarakKonservasiKm: Double? = null,
    @field:PositiveOrZero(message = "Luas APL tidak boleh negatif")
    val luasAPLHa: Double? = null,
    val namaKawasanKonservasi: String? = null,
    @field:PositiveOrZero(message = "Produktivitas tidak boleh negatif")
    val produktivitasKgHaTahun: Double? = null,
    @field:PositiveOrZero(message = "Harga cherry tidak boleh negatif")
    val hargaCherryRp: Int? = null,
    @field:PositiveOrZero(message = "Harga green bean tidak boleh negatif")
    val hargaGreenBeanRpKg: Int? = null,
    val pembeliUtama: String? = null,
    @field:PositiveOrZero(message = "Jumlah pedagang pengumpul tidak boleh negatif")
    val jumlahPedagangPengumpul: Int? = null,
    @field:PositiveOrZero(message = "Jumlah koperasi aktif tidak boleh negatif")
    val koperasiAktifUnit: Int? = null,
    val eksportir: String? = null,
    val industriPengolahan: String? = null,
    val permasalahanUtama: String? = null,
    val berbatasanKonservasi: Boolean? = null,
    @field:PositiveOrZero(message = "Luas penyangga tidak boleh negatif")
    val luasPenyanggaHa: Double? = null,
    @field:PositiveOrZero(message = "Tutupan hutan tidak boleh negatif")
    val tutupanHutan: Double? = null,
    val tutupanHutanSatuan: SatuanTutupan? = null,
    @field:PositiveOrZero(message = "Tutupan agroforestry tidak boleh negatif")
    val tutupanAgroforestry: Double? = null,
    val tutupanAgroforestrySatuan: SatuanTutupan? = null,
    val rawanLongsor: Boolean? = null,
    val lokasiRawanLongsor: String? = null,
    val rawanErosi: Boolean? = null,
    val lokasiRawanErosi: String? = null,
    val konflikSatwa: Boolean? = null,
    val jenisSatwaKonflik: String? = null,
    val praktikKonservasi: String? = null,
    @field:NotEmpty(message = "Data kebijakan desa wajib lengkap")
    @field:Valid
    val kebijakan: List<KebijakanRequest> = emptyList(),
    @field:NotEmpty(message = "Data kelembagaan desa wajib lengkap")
    @field:Valid
    val kelembagaan: List<KelembagaanRequest> = emptyList(),
)

data class KebijakanRequest(val jenis: JenisKebijakan, val ada: Boolean = false, val keterangan: String? = null)
data class KelembagaanRequest(val jenis: JenisLembaga, val jumlah: Int? = null, val kondisi: String? = null)

data class KebijakanDto(val id: String, val jenis: JenisKebijakan, val ada: Boolean, val keterangan: String?)
data class KelembagaanDto(val id: String, val jenis: JenisLembaga, val jumlah: Int?, val kondisi: String?)

data class DesaListDto(
    val id: String,
    val desa: String,
    val kecamatan: String,
    val tahunPendataan: Int,
    val jumlahPetaniKopi: Int?,
    val luasArealKopiHa: Double?,
    val createdBy: String?,
    val createdAt: Instant,
)

data class DesaDetailDto(
    val id: String,
    val tahunPendataan: Int,
    val sumberData: String?,
    val desa: DesaRingkas,
    val createdBy: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val luasWilayahHa: Double?,
    val jumlahPenduduk: Int?,
    val jumlahKK: Int?,
    val jumlahPetaniKopi: Int?,
    val luasArealKopiHa: Double?,
    val luasKomoditiLainHa: Double?,
    val latitude: Double?,
    val longitude: Double?,
    val topografi: String?,
    val ketinggianMdpl: Double?,
    val bulanHujan: String?,
    val bulanKering: String?,
    val suhuRataRataC: Double?,
    val jenisTanah: String?,
    val aksesJalan: String?,
    val jarakIbukotaKecamatanKm: Double?,
    val jarakPasarKm: Double?,
    val jarakKonservasiKm: Double?,
    val luasAPLHa: Double?,
    val namaKawasanKonservasi: String?,
    val produktivitasKgHaTahun: Double?,
    val hargaCherryRp: Int?,
    val hargaGreenBeanRpKg: Int?,
    val pembeliUtama: String?,
    val jumlahPedagangPengumpul: Int?,
    val koperasiAktifUnit: Int?,
    val eksportir: String?,
    val industriPengolahan: String?,
    val permasalahanUtama: String?,
    val berbatasanKonservasi: Boolean?,
    val luasPenyanggaHa: Double?,
    val tutupanHutan: Double?,
    val tutupanHutanSatuan: SatuanTutupan?,
    val tutupanAgroforestry: Double?,
    val tutupanAgroforestrySatuan: SatuanTutupan?,
    val rawanLongsor: Boolean?,
    val lokasiRawanLongsor: String?,
    val rawanErosi: Boolean?,
    val lokasiRawanErosi: String?,
    val konflikSatwa: Boolean?,
    val jenisSatwaKonflik: String?,
    val praktikKonservasi: String?,
    val kebijakan: List<KebijakanDto>,
    val kelembagaan: List<KelembagaanDto>,
)

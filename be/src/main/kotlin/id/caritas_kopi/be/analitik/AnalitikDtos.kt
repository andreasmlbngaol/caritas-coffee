package id.caritas_kopi.be.analitik

// Bentuk respons mengikuti agregasi `analitik/queries.ts` proyek lama.

data class TrenProduksi(
    val tahun: String,
    val cherry: Double,
    val greenBean: Double,
    val gabahBasah: Double,
    val gabahKering: Double,
)

data class RingkasanDto(
    val jumlahPetani: Long,
    val jumlahDesa: Long,
    val jumlahKelompok: Long,
    val luasArealKopiHa: Double,
    val luasPlotHa: Double,
    val pohonProduktif: Double,
    val tahunTerbaru: Int?,
    val produktivitasRata: Double,
    val trenProduksi: List<TrenProduksi>,
)

data class GapItemStat(
    val jenis: String,
    val ya: Long,
    val tidak: Long,
    val kadang: Long,
    val total: Long,
    val pctYa: Double,
)

data class GapAdoptionDto(
    val totalPetani: Long,
    val items: List<GapItemStat>,
    val adopsiKeseluruhan: Double,
)

data class ProduksiTahun(
    val tahun: String,
    val cherry: Double,
    val gabahBasah: Double,
    val gabahKering: Double,
    val greenBean: Double,
    val produktivitasRata: Double,
    val jumlahPetani: Long,
)

data class TopDesa(
    val nama: String,
    val cherry: Double,
    val gabahBasah: Double,
    val gabahKering: Double,
    val greenBean: Double,
    val total: Double,
)

data class ProduksiDto(
    val tahunTerbaru: Int?,
    val byTahun: List<ProduksiTahun>,
    val topDesa: List<TopDesa>,
)

data class ProdukStat(
    val label: String,
    val jenis: String,
    val volume: Double,
    val petani: Long,
    val custom: Boolean,
    val rataVolume: Double,
)

data class PasarStat(
    val label: String,
    val kategori: String,
    val petani: Long,
    val custom: Boolean,
    val rataPersen: Double,
    val profil: List<String>,
)

data class VolumeJenis(val jenis: String, val volume: Double)

data class PasarProdukDto(
    val totalPetani: Long,
    val produk: List<ProdukStat>,
    val pasar: List<PasarStat>,
    val volumeByJenis: List<VolumeJenis>,
)

data class Frekuensi(val nama: String, val jumlah: Int)

data class KondisiStat(val jenis: String, val jumlah: Long, val pct: Double)

data class KonservasiDto(
    val totalPetani: Long,
    val kondisi: List<KondisiStat>,
    val jumlahDesa: Long,
    val luasPenyanggaHa: Double,
    val luasAPLHa: Double,
    val jarakKonservasiRata: Double,
    val rawanLongsor: Int,
    val rawanErosi: Int,
    val konflikSatwa: Int,
    val berbatasan: Int,
    val tutupanHutan: Double,
    val tutupanAgroforestry: Double,
    val praktik: List<Frekuensi>,
    val satwa: List<Frekuensi>,
    val kawasan: List<String>,
)

data class TopDesaPetani(val nama: String, val kecamatan: String, val petani: Long)

data class GenderStat(val L: Long, val P: Long, val kosong: Long)

data class UsiaStat(val label: String, val jumlah: Int)

data class LembagaStat(val jenis: String, val jumlah: Double, val desa: Long)

data class KebijakanStat(val jenis: String, val desa: Long)

data class WilayahAnalitikDto(
    val topDesa: List<TopDesaPetani>,
    val gender: GenderStat,
    val usia: List<UsiaStat>,
    val totalPenduduk: Double,
    val totalKK: Double,
    val totalArealKopi: Double,
    val totalPetaniKopi: Double,
    val rataKetinggian: Double,
    val rataSuhu: Double,
    val jumlahDesa: Int,
    val topografi: List<Frekuensi>,
    val jenisTanah: List<Frekuensi>,
    val aksesJalan: List<Frekuensi>,
    val pembeliUtama: List<Frekuensi>,
    val eksportir: List<Frekuensi>,
    val industri: List<Frekuensi>,
    val permasalahan: List<Frekuensi>,
    val lembaga: List<LembagaStat>,
    val kebijakan: List<KebijakanStat>,
)

data class NaunganStat(val nama: String, val petani: Int, val pohon: Long, val dipangkas: Int)

data class UmurStat(val label: String, val jumlah: Int)

data class AgronomiDto(
    val totalPetani: Long,
    val jumlahPlot: Int,
    val totalLuas: Double,
    val totalPohonProduktif: Long,
    val totalPohonTidakProduktif: Long,
    val totalTanamanBaru: Long,
    val rataProduktif: Double,
    val varietas: List<Frekuensi>,
    val sistemBudidaya: List<Frekuensi>,
    val kepemilikan: List<Frekuensi>,
    val areaKonservasi: List<Frekuensi>,
    val naungan: List<NaunganStat>,
    val pestisida: List<Frekuensi>,
    val umur: List<UmurStat>,
)

data class LokasiDesa(
    val lat: Double,
    val lng: Double,
    val nama: String,
    val kecamatan: String,
    val kabupaten: String,
    val luasArealKopiHa: Double,
    val petaniKopi: Double,
    val penduduk: Double,
    val ketinggian: Double?,
)

data class LokasiPlot(
    val lat: Double,
    val lng: Double,
    val luasKopiHa: Double,
    val varietas: String?,
    val hamparan: String?,
    val petani: String,
    val desa: String,
)

data class LokasiDto(val desa: List<LokasiDesa>, val plot: List<LokasiPlot>)

package id.caritas_kopi.be.petani

import id.caritas_kopi.be.kelompoktani.KelompokTani
import id.caritas_kopi.be.wilayah.WilayahDesa
import id.caritas_kopi.be.wilayah.WilayahKabupaten
import id.caritas_kopi.be.wilayah.WilayahKecamatan
import id.caritas_kopi.be.wilayah.WilayahProvinsi

fun PlotPetani.toDto() = PlotDto(
    id = id.toString(),
    nomor = nomor,
    namaHamparan = namaHamparan,
    varietas = varietas,
    tahunTanam = tahunTanam.toList(),
    kodeGps = kodeGps,
    elevasiMdpl = elevasiMdpl,
    kemiringanPersen = kemiringanPersen,
    luasKopiHa = luasKopiHa,
    fotoKey = fotoKey,
    fotoLatitude = fotoLatitude,
    fotoLongitude = fotoLongitude,
    statusKepemilikan = statusKepemilikan,
    sistemBudidaya = sistemBudidaya,
    areaKonservasi = areaKonservasi,
    tanamanBaru = tanamanBaru,
    pohonProduktif = pohonProduktif,
    pohonTidakProduktif = pohonTidakProduktif,
    pestisidaNama = pestisidaNama,
    pestisidaBulanTahun = pestisidaBulanTahun,
)

fun TanamanNaungan.toDto() = NaunganDto(
    id.toString(), jenis, jumlah, fungsi, pemangkasan, produksiPerTahun, tahunTanam,
)

fun PraktikGap.toDto() = GapDto(id.toString(), jenis, jawaban, keterangan)

fun RiwayatProduksi.toDto() = ProduksiDto(
    id.toString(), tahun, satuan, cherry, gabahBasah, gabahKering, greenBean, produktivitas,
)

fun ProdukDijual.toDto() = ProdukDto(
    id.toString(), jenis, labelCustom, dijual, volumeKgTahun,
)

fun PasarPetani.toDto() = PasarDto(
    id.toString(), kategori, labelCustom, aktif, persentase, profilPenjual,
)

fun KondisiKebun.toDto() = KondisiDto(id.toString(), jenis, jawaban, keterangan)

fun KelompokTani.toRingkas() = KelompokTaniRingkas(id.toString(), nama, kode)

/** Bangun DesaRingkas dari entitas wilayah + peta induknya. */
fun buildDesaRingkas(
    desa: WilayahDesa,
    kecamatanMap: Map<String, WilayahKecamatan>,
    kabupatenMap: Map<String, WilayahKabupaten>,
    provinsiMap: Map<String, WilayahProvinsi>,
): DesaRingkas {
    val kec = kecamatanMap[desa.kecamatanKode]
    val kab = kec?.let { kabupatenMap[it.kabupatenKode] }
    val prov = kab?.let { provinsiMap[it.provinsiKode] }
    return DesaRingkas(
        kode = desa.kode,
        nama = desa.nama,
        kecamatan = kec?.nama ?: "-",
        kabupaten = kab?.nama ?: "-",
        provinsi = prov?.nama ?: "-",
    )
}

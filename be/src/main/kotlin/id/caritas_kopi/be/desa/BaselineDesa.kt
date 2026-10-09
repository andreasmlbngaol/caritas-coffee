package id.caritas_kopi.be.desa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

enum class SatuanTutupan { PERSEN, HA }

enum class JenisKebijakan {
    RPJM_DESA,
    PERDES_PERTANIAN,
    PERDES_PERLINDUNGAN_HUTAN,
    PROGRAM_PERKEMBANGAN_KOPI,
    PROGRAM_KOPERASI,
    PROGRAM_PERHUTANAN_SOSIAL,
}

enum class JenisLembaga {
    KELOMPOK_TANI,
    GAPOKTAN,
    KOPERASI,
    BUMDES,
    PENYULUH,
    PENDAMPING,
}

@Entity
@Table(name = "baseline_desa")
class BaselineDesa(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @Column(name = "tahun_pendataan", nullable = false)
    var tahunPendataan: Int = 0,

    @Column(name = "sumber_data")
    var sumberData: String? = null,

    @Column(name = "desa_kode", nullable = false, unique = true)
    var desaKode: String = "",

    // A - Data Desa / Wilayah
    @Column(name = "luas_wilayah_ha")
    var luasWilayahHa: Double? = null,
    @Column(name = "jumlah_penduduk")
    var jumlahPenduduk: Int? = null,
    @Column(name = "jumlah_kk")
    var jumlahKK: Int? = null,
    @Column(name = "jumlah_petani_kopi")
    var jumlahPetaniKopi: Int? = null,
    @Column(name = "luas_areal_kopi_ha")
    var luasArealKopiHa: Double? = null,
    @Column(name = "luas_komoditi_lain_ha")
    var luasKomoditiLainHa: Double? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var topografi: String? = null,
    @Column(name = "ketinggian_mdpl")
    var ketinggianMdpl: Double? = null,
    @Column(name = "bulan_hujan")
    var bulanHujan: String? = null,
    @Column(name = "bulan_kering")
    var bulanKering: String? = null,
    @Column(name = "suhu_rata_rata_c")
    var suhuRataRataC: Double? = null,
    @Column(name = "jenis_tanah")
    var jenisTanah: String? = null,
    @Column(name = "akses_jalan")
    var aksesJalan: String? = null,
    @Column(name = "jarak_ibukota_kecamatan_km")
    var jarakIbukotaKecamatanKm: Double? = null,
    @Column(name = "jarak_pasar_km")
    var jarakPasarKm: Double? = null,
    @Column(name = "jarak_konservasi_km")
    var jarakKonservasiKm: Double? = null,
    @Column(name = "luas_apl_ha")
    var luasAPLHa: Double? = null,
    @Column(name = "nama_kawasan_konservasi")
    var namaKawasanKonservasi: String? = null,

    // D - Kondisi Bisnis Kopi
    @Column(name = "produktivitas_kg_ha_tahun")
    var produktivitasKgHaTahun: Double? = null,
    @Column(name = "harga_cherry_rp")
    var hargaCherryRp: Int? = null,
    @Column(name = "harga_green_bean_rp_kg")
    var hargaGreenBeanRpKg: Int? = null,
    @Column(name = "pembeli_utama")
    var pembeliUtama: String? = null,
    @Column(name = "jumlah_pedagang_pengumpul")
    var jumlahPedagangPengumpul: Int? = null,
    @Column(name = "koperasi_aktif_unit")
    var koperasiAktifUnit: Int? = null,
    var eksportir: String? = null,
    @Column(name = "industri_pengolahan")
    var industriPengolahan: String? = null,
    @Column(name = "permasalahan_utama")
    var permasalahanUtama: String? = null,

    // E - Kondisi Konservasi
    @Column(name = "berbatasan_konservasi")
    var berbatasanKonservasi: Boolean? = null,
    @Column(name = "luas_penyangga_ha")
    var luasPenyanggaHa: Double? = null,
    @Column(name = "tutupan_hutan")
    var tutupanHutan: Double? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "tutupan_hutan_satuan")
    var tutupanHutanSatuan: SatuanTutupan? = null,
    @Column(name = "tutupan_agroforestry")
    var tutupanAgroforestry: Double? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "tutupan_agroforestry_satuan")
    var tutupanAgroforestrySatuan: SatuanTutupan? = null,
    @Column(name = "rawan_longsor")
    var rawanLongsor: Boolean? = null,
    @Column(name = "lokasi_rawan_longsor")
    var lokasiRawanLongsor: String? = null,
    @Column(name = "rawan_erosi")
    var rawanErosi: Boolean? = null,
    @Column(name = "lokasi_rawan_erosi")
    var lokasiRawanErosi: String? = null,
    @Column(name = "konflik_satwa")
    var konflikSatwa: Boolean? = null,
    @Column(name = "jenis_satwa_konflik")
    var jenisSatwaKonflik: String? = null,
    @Column(name = "praktik_konservasi")
    var praktikKonservasi: String? = null,

    @Column(name = "created_by_id", nullable = false)
    var createdById: UUID = UUID.randomUUID(),
)

@Entity
@Table(name = "kebijakan_desa")
class KebijakanDesa(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var jenis: JenisKebijakan = JenisKebijakan.RPJM_DESA,

    @Column(nullable = false)
    var ada: Boolean = false,

    var keterangan: String? = null,

    @Column(name = "baseline_id", nullable = false)
    var baselineId: UUID = UUID.randomUUID(),
)

@Entity
@Table(name = "kelembagaan_desa")
class KelembagaanDesa(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var jenis: JenisLembaga = JenisLembaga.KELOMPOK_TANI,

    var jumlah: Int? = null,

    var kondisi: String? = null,

    @Column(name = "baseline_id", nullable = false)
    var baselineId: UUID = UUID.randomUUID(),
)

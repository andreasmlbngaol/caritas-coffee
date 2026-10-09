package id.caritas_kopi.be.petani

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

enum class JenisKelamin { L, P }

enum class StatusKepemilikanLahan { MS, SW, BH, TA, L }

enum class SistemBudidaya { AF, MK }

enum class JawabanGap { YA, TIDAK, KADANG }

enum class SatuanProduksi { KG, SOLUP, BAMBU, KALENG }

enum class JenisProdukDijual {
    CHERRY,
    GABAH_BASAH,
    GABAH_KERING,
    GB_WET_HULL,
    GB_NATURAL,
    GB_HONEY,
    GB_FULL_WASH,
    GB_WINE,
    LAINNYA,
}

enum class KategoriPasar {
    KOMERSIAL,
    KOMERSIAL_BERSERTIFIKAT,
    SPECIALTY,
    ORGANIK,
    LAINNYA,
}

enum class JenisPraktikGap {
    PEMANGKASAN_KOPI,
    PEMANGKASAN_NAUNGAN,
    PENGENDALIAN_GULMA,
    PEMUPUKAN,
    PEREMAJAAN_TANAMAN,
    PENGENDALIAN_PBKO,
    PENGENDALIAN_KARAT_DAUN,
    PESTISIDA_SESUAI_DOSIS,
    PENYIMPANAN_PESTISIDA,
    TERAS_SENGKEDAN,
    COVER_CROP,
    RORAK_RESAPAN,
    LIMBAH_PULP,
    PANEN_SELEKTIF,
    SORTASI_CHERRY,
    PENJEMURAN_BERSIH,
    PENYIMPANAN_HASIL,
    TANPA_BAKAR_LAHAN,
    TANPA_KIMIA_TERLARANG,
    APD_PESTISIDA,
    TANPA_PEKERJA_ANAK,
}

enum class JenisKondisiKebun {
    KEPEMILIKAN_JELAS,
    BATAS_KONSERVASI,
    BATAS_HUTAN_LINDUNG,
    DEKAT_SUNGAI,
    DEKAT_MATA_AIR,
    POHON_NAUNGAN,
    KONSERVASI_TANAH,
    PERNAH_BAKAR_LAHAN,
    KONFLIK_SATWA,
    EROSI_LONGSOR,
}

@Entity
@Table(name = "petani")
class Petani(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @Column(name = "kode_petani", unique = true)
    var kodePetani: String? = null,

    // A - Identitas
    @Column(name = "nama_lengkap", nullable = false)
    var namaLengkap: String = "",

    @Column(name = "nama_panggilan")
    var namaPanggilan: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "jenis_kelamin")
    var jenisKelamin: JenisKelamin? = null,

    @Column(name = "tanggal_lahir")
    var tanggalLahir: LocalDate? = null,

    @Column(name = "alamat_domisili")
    var alamatDomisili: String? = null,

    var telepon: String? = null,

    @Column(name = "tanggal_pendaftaran")
    var tanggalPendaftaran: LocalDate? = null,

    @Column(name = "nama_petugas_pendaftar")
    var namaPetugasPendaftar: String? = null,

    @Column(name = "kontak_darurat_nama")
    var kontakDaruratNama: String? = null,

    @Column(name = "kontak_darurat_telepon")
    var kontakDaruratTelepon: String? = null,

    @Column(name = "kontak_darurat_hubungan")
    var kontakDaruratHubungan: String? = null,

    @Column(name = "desa_kode", nullable = false)
    var desaKode: String = "",

    @Column(name = "kelompok_tani_id")
    var kelompokTaniId: UUID? = null,

    @Column(name = "created_by_id", nullable = false)
    var createdById: UUID = UUID.randomUUID(),
)

@Entity
@Table(name = "plot_petani")
class PlotPetani(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    var nomor: Int = 0,

    @Column(name = "petani_id", nullable = false)
    var petaniId: UUID = UUID.randomUUID(),

    @Column(name = "nama_hamparan")
    var namaHamparan: String? = null,

    var varietas: String? = null,

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tahun_tanam", columnDefinition = "integer[]")
    var tahunTanam: IntArray = IntArray(0),

    @Column(name = "kode_gps")
    var kodeGps: String? = null,

    @Column(name = "elevasi_mdpl")
    var elevasiMdpl: Double? = null,

    @Column(name = "kemiringan_persen")
    var kemiringanPersen: Double? = null,

    @Column(name = "luas_kopi_ha")
    var luasKopiHa: Double? = null,

    @Column(name = "foto_key")
    var fotoKey: String? = null,

    @Column(name = "foto_latitude")
    var fotoLatitude: Double? = null,

    @Column(name = "foto_longitude")
    var fotoLongitude: Double? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status_kepemilikan")
    var statusKepemilikan: StatusKepemilikanLahan? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "sistem_budidaya")
    var sistemBudidaya: SistemBudidaya? = null,

    @Column(name = "area_konservasi")
    var areaKonservasi: String? = null,

    @Column(name = "tanaman_baru")
    var tanamanBaru: Int? = null,

    @Column(name = "pohon_produktif")
    var pohonProduktif: Int? = null,

    @Column(name = "pohon_tidak_produktif")
    var pohonTidakProduktif: Int? = null,

    @Column(name = "pestisida_nama")
    var pestisidaNama: String? = null,

    @Column(name = "pestisida_bulan_tahun")
    var pestisidaBulanTahun: String? = null,
)

@Entity
@Table(name = "tanaman_naungan")
class TanamanNaungan(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "petani_id", nullable = false)
    var petaniId: UUID = UUID.randomUUID(),

    var jenis: String? = null,
    var jumlah: Int? = null,
    var fungsi: String? = null,
    var pemangkasan: Boolean? = null,

    @Column(name = "produksi_per_tahun")
    var produksiPerTahun: String? = null,

    @Column(name = "tahun_tanam")
    var tahunTanam: Int? = null,
)

@Entity
@Table(name = "praktik_gap")
class PraktikGap(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var jenis: JenisPraktikGap = JenisPraktikGap.PEMANGKASAN_KOPI,

    @Enumerated(EnumType.STRING)
    var jawaban: JawabanGap? = null,

    var keterangan: String? = null,

    @Column(name = "petani_id", nullable = false)
    var petaniId: UUID = UUID.randomUUID(),
)

@Entity
@Table(name = "riwayat_produksi")
class RiwayatProduksi(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    var tahun: Int = 0,

    @Enumerated(EnumType.STRING)
    var satuan: SatuanProduksi? = null,

    var cherry: Double? = null,

    @Column(name = "gabah_basah")
    var gabahBasah: Double? = null,

    @Column(name = "gabah_kering")
    var gabahKering: Double? = null,

    @Column(name = "green_bean")
    var greenBean: Double? = null,

    var produktivitas: Double? = null,

    @Column(name = "petani_id", nullable = false)
    var petaniId: UUID = UUID.randomUUID(),
)

@Entity
@Table(name = "produk_dijual")
class ProdukDijual(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var jenis: JenisProdukDijual = JenisProdukDijual.CHERRY,

    @Column(name = "label_custom")
    var labelCustom: String? = null,

    @Column(nullable = false)
    var dijual: Boolean = false,

    @Column(name = "volume_kg_tahun")
    var volumeKgTahun: Double? = null,

    @Column(name = "petani_id", nullable = false)
    var petaniId: UUID = UUID.randomUUID(),
)

@Entity
@Table(name = "pasar_petani")
class PasarPetani(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var kategori: KategoriPasar = KategoriPasar.KOMERSIAL,

    @Column(name = "label_custom")
    var labelCustom: String? = null,

    @Column(nullable = false)
    var aktif: Boolean = false,

    var persentase: Double? = null,

    @Column(name = "profil_penjual")
    var profilPenjual: String? = null,

    @Column(name = "petani_id", nullable = false)
    var petaniId: UUID = UUID.randomUUID(),
)

@Entity
@Table(name = "kondisi_kebun")
class KondisiKebun(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var jenis: JenisKondisiKebun = JenisKondisiKebun.KEPEMILIKAN_JELAS,

    @Column(nullable = false)
    var jawaban: Boolean = false,

    var keterangan: String? = null,

    @Column(name = "petani_id", nullable = false)
    var petaniId: UUID = UUID.randomUUID(),
)

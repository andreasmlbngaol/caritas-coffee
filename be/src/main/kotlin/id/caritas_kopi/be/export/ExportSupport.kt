package id.caritas_kopi.be.export

import id.caritas_kopi.be.desa.JenisKebijakan
import id.caritas_kopi.be.desa.JenisLembaga
import id.caritas_kopi.be.petani.JenisKondisiKebun
import id.caritas_kopi.be.petani.JenisPraktikGap
import id.caritas_kopi.be.petani.JenisProdukDijual
import id.caritas_kopi.be.petani.KategoriPasar
import id.caritas_kopi.be.petani.SatuanProduksi
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Label & pemformatan untuk export-model, dipetakan persis dari constants.ts proyek lama. */
object ExportSupport {

    private val ID = Locale("id", "ID")
    // NumberFormat TIDAK thread-safe. Export berjalan paralel (banyak request), jadi
    // buat per-panggilan - kalau dipakai bersama, hasil format bisa korup / exception.
    private fun newNum(): NumberFormat = NumberFormat.getNumberInstance(ID)
    private val dateFmt: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMMM yyyy", ID)

    val BULAN_ID = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember",
    )

    // ---------- C - Praktik GAP (21 item, 5 kelompok) ----------
    data class GapItem(val jenis: JenisPraktikGap, val label: String)
    data class GapGroup(val kelompok: String, val items: List<GapItem>)

    val GAP_GROUPS: List<GapGroup> = listOf(
        GapGroup(
            "Pemeliharaan tanaman",
            listOf(
                GapItem(JenisPraktikGap.PEMANGKASAN_KOPI, "Pemangkasan Kopi Rutin"),
                GapItem(JenisPraktikGap.PEMANGKASAN_NAUNGAN, "Pemangkasan Pohon Naungan"),
                GapItem(JenisPraktikGap.PENGENDALIAN_GULMA, "Pengendalian Gulma (Manual/Herbisida)"),
                GapItem(JenisPraktikGap.PEMUPUKAN, "Pemupukan (Organik/Anorganik)"),
                GapItem(JenisPraktikGap.PEREMAJAAN_TANAMAN, "Peremajaan Tanaman Tua (Replanting/Stumping/Grafting)"),
            ),
        ),
        GapGroup(
            "Pengelolaan Hama & Penyakit",
            listOf(
                GapItem(JenisPraktikGap.PENGENDALIAN_PBKO, "Pengendalian penggerek buah kopi"),
                GapItem(JenisPraktikGap.PENGENDALIAN_KARAT_DAUN, "Pengendalian penyakit karat daun (HV/CLR)"),
                GapItem(JenisPraktikGap.PESTISIDA_SESUAI_DOSIS, "Penggunaan pestisida sesuai dosis dan jadwal"),
                GapItem(JenisPraktikGap.PENYIMPANAN_PESTISIDA, "Penyimpanan pestisida terpisah dari bahan pangan"),
            ),
        ),
        GapGroup(
            "Konservasi Air & Tanah",
            listOf(
                GapItem(JenisPraktikGap.TERAS_SENGKEDAN, "Pembuatan teras/sengkedan di lahan miring"),
                GapItem(JenisPraktikGap.COVER_CROP, "Penanaman cover crop/tanaman penutup tanah"),
                GapItem(JenisPraktikGap.RORAK_RESAPAN, "Pembuatan rorak/lubang resapan"),
                GapItem(JenisPraktikGap.LIMBAH_PULP, "Pengelolaan limbah pulp/kulit kopi"),
            ),
        ),
        GapGroup(
            "Panen & Pasca-Panen",
            listOf(
                GapItem(JenisPraktikGap.PANEN_SELEKTIF, "Panen selektif (petik merah)"),
                GapItem(JenisPraktikGap.SORTASI_CHERRY, "Sortasi cherry sebelum dijual/diproses"),
                GapItem(JenisPraktikGap.PENJEMURAN_BERSIH, "Penjemuran di tempat bersih (tidak di jalan/tanah langsung)"),
                GapItem(JenisPraktikGap.PENYIMPANAN_HASIL, "Penyimpanan hasil panen di tempat kering dan bersih"),
            ),
        ),
        GapGroup(
            "Lingkungan & Sosial",
            listOf(
                GapItem(JenisPraktikGap.TANPA_BAKAR_LAHAN, "Tidak membuka lahan dengan cara membakar"),
                GapItem(JenisPraktikGap.TANPA_KIMIA_TERLARANG, "Tidak menggunakan bahan kimia terlarang"),
                GapItem(JenisPraktikGap.APD_PESTISIDA, "Menggunakan APD saat aplikasi pestisida"),
                GapItem(JenisPraktikGap.TANPA_PEKERJA_ANAK, "Tidak mempekerjakan anak di bawah umur"),
            ),
        ),
    )

    // ---------- F - Kondisi Kebun (10 item) ----------
    val KONDISI_KEBUN: List<Pair<JenisKondisiKebun, String>> = listOf(
        JenisKondisiKebun.KEPEMILIKAN_JELAS to "Kepemilikan lahan jelas (SHM, Surat Desa, Surat Pinjam Lahan)",
        JenisKondisiKebun.BATAS_KONSERVASI to "Berbatasan dengan kawasan konservasi",
        JenisKondisiKebun.BATAS_HUTAN_LINDUNG to "Berbatasan dengan hutan lindung",
        JenisKondisiKebun.DEKAT_SUNGAI to "Berdekatan dengan sungai",
        JenisKondisiKebun.DEKAT_MATA_AIR to "Berdekatan dengan mata air",
        JenisKondisiKebun.POHON_NAUNGAN to "Menggunakan pohon naungan",
        JenisKondisiKebun.KONSERVASI_TANAH to "Menerapkan konservasi tanah",
        JenisKondisiKebun.PERNAH_BAKAR_LAHAN to "Pernah membuka lahan dengan membakar",
        JenisKondisiKebun.KONFLIK_SATWA to "Pernah terjadi konflik satwa",
        JenisKondisiKebun.EROSI_LONGSOR to "Terdapat erosi/longsor",
    )

    // ---------- E.1 - Jenis Produk ----------
    val PRODUK: List<Pair<JenisProdukDijual, String>> = listOf(
        JenisProdukDijual.CHERRY to "Cherry",
        JenisProdukDijual.GABAH_BASAH to "Gabah Basah",
        JenisProdukDijual.GABAH_KERING to "Gabah Kering",
        JenisProdukDijual.GB_WET_HULL to "Green Bean - Wet Hull",
        JenisProdukDijual.GB_NATURAL to "Green Bean - Natural Process",
        JenisProdukDijual.GB_HONEY to "Green Bean - Honey Process",
        JenisProdukDijual.GB_FULL_WASH to "Green Bean - Full Wash",
        JenisProdukDijual.GB_WINE to "Green Bean - Wine Process",
    )

    // ---------- E.2 - Kategori Pasar ----------
    val PASAR: List<Pair<KategoriPasar, String>> = listOf(
        KategoriPasar.KOMERSIAL to "Komersial",
        KategoriPasar.KOMERSIAL_BERSERTIFIKAT to "Komersial Bersertifikat",
        KategoriPasar.SPECIALTY to "Specialty Coffee",
        KategoriPasar.ORGANIK to "Organik",
    )

    val TAHUN_PRODUKSI = listOf(2023, 2024, 2025, 2026)
    const val TAHUN_ESTIMASI = 2026

    val SATUAN_PRODUKSI: List<Pair<SatuanProduksi, String>> = listOf(
        SatuanProduksi.KG to "Kg",
        SatuanProduksi.SOLUP to "Solup",
        SatuanProduksi.BAMBU to "Bambu",
        SatuanProduksi.KALENG to "Kaleng",
    )

    val KEBIJAKAN: List<Pair<JenisKebijakan, String>> = listOf(
        JenisKebijakan.RPJM_DESA to "RPJM Desa",
        JenisKebijakan.PERDES_PERTANIAN to "Perdes Pertanian",
        JenisKebijakan.PERDES_PERLINDUNGAN_HUTAN to "Perdes Perlindungan Hutan",
        JenisKebijakan.PROGRAM_PERKEMBANGAN_KOPI to "Program Perkembangan Kopi",
        JenisKebijakan.PROGRAM_KOPERASI to "Program Koperasi",
        JenisKebijakan.PROGRAM_PERHUTANAN_SOSIAL to "Program Perhutanan Sosial",
    )

    val LEMBAGA: List<Pair<JenisLembaga, String>> = listOf(
        JenisLembaga.KELOMPOK_TANI to "Kelompok Tani",
        JenisLembaga.GAPOKTAN to "Gapoktan",
        JenisLembaga.KOPERASI to "Koperasi",
        JenisLembaga.BUMDES to "BUMDes",
        JenisLembaga.PENYULUH to "Penyuluh",
        JenisLembaga.PENDAMPING to "Pendamping",
    )

    // ---------- Pemformatan ----------
    fun fmt(v: String?): String = if (v.isNullOrEmpty()) "-" else v

    fun num(v: Number?): String = if (v == null) "-" else newNum().format(v)

    fun numUnit(v: Number?, unit: String): String = if (v == null) "-" else "${newNum().format(v)} $unit"

    fun bool(v: Boolean?): String = if (v == null) "-" else if (v) "Ya" else "Tidak"

    fun fmtBool(v: Boolean?): String = bool(v)

    fun fmtTutupan(v: Number?, satuan: String?): String =
        if (v == null) "-" else "${newNum().format(v)} ${if (satuan == "HA") "Ha" else "%"}"

    fun fmtBoolDetail(v: Boolean?, detail: String?, label: String): String = when {
        v == null -> "-"
        !v -> "Tidak"
        detail.isNullOrEmpty() -> "Ya"
        else -> "Ya, $label: $detail"
    }

    fun fmtDate(d: LocalDate?): String = d?.format(dateFmt) ?: "-"

    fun fmtDate(i: Instant?): String =
        i?.atZone(ZoneId.systemDefault())?.toLocalDate()?.format(dateFmt) ?: "-"

    /** status/sistem: kode mentah saja (MS, AF, ...). */
    fun kode(v: String?): String = v ?: "-"

    fun satuanLabel(v: SatuanProduksi?): String =
        SATUAN_PRODUKSI.firstOrNull { it.first == v }?.second ?: (v?.name ?: "-")

    /** "2026-06" -> "Juni 2026". */
    fun fmtBulanTahun(v: String?): String {
        if (v.isNullOrEmpty()) return "-"
        val parts = v.split("-")
        if (parts.size < 2) return v
        val bulan = BULAN_ID.getOrNull(parts[1].toIntOrNull()?.minus(1) ?: -1)
        return if (bulan != null) "$bulan ${parts[0]}" else v
    }

    fun escape(s: String): String = buildString(s.length) {
        for (c in s) when (c) {
            '&' -> append("&amp;")
            '<' -> append("&lt;")
            '>' -> append("&gt;")
            '"' -> append("&quot;")
            else -> append(c)
        }
    }
}

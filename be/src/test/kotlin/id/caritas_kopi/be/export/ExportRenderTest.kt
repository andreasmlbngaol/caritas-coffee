package id.caritas_kopi.be.export

import id.caritas_kopi.be.desa.DesaDetailDto
import id.caritas_kopi.be.desa.JenisKebijakan
import id.caritas_kopi.be.desa.JenisLembaga
import id.caritas_kopi.be.desa.KebijakanDto
import id.caritas_kopi.be.desa.KelembagaanDto
import id.caritas_kopi.be.petani.DesaRingkas
import id.caritas_kopi.be.petani.GapDto
import id.caritas_kopi.be.petani.JawabanGap
import id.caritas_kopi.be.petani.JenisKondisiKebun
import id.caritas_kopi.be.petani.JenisPraktikGap
import id.caritas_kopi.be.petani.JenisProdukDijual
import id.caritas_kopi.be.petani.KategoriPasar
import id.caritas_kopi.be.petani.KondisiDto
import id.caritas_kopi.be.petani.NaunganDto
import id.caritas_kopi.be.petani.PasarDto
import id.caritas_kopi.be.petani.PetaniDetailDto
import id.caritas_kopi.be.petani.PlotDto
import id.caritas_kopi.be.petani.ProdukDto
import id.caritas_kopi.be.petani.ProduksiDto
import id.caritas_kopi.be.petani.SatuanProduksi
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExportRenderTest {

    private val desa = DesaRingkas("11.01.01.2001", "Sukamaju", "Cikajang", "Garut", "Jawa Barat")

    private fun petani(): PetaniDetailDto = PetaniDetailDto(
        id = "p1", kodePetani = "CAR-KR-001", namaLengkap = "Budi Santoso", namaPanggilan = "Bud",
        jenisKelamin = id.caritas_kopi.be.petani.JenisKelamin.L,
        tanggalLahir = LocalDate.of(1980, 5, 1), alamatDomisili = "Jl. Mawar 1", telepon = "0812",
        tanggalPendaftaran = LocalDate.of(2025, 1, 2), namaPetugasPendaftar = "Petugas A",
        kontakDaruratNama = "Ani", kontakDaruratTelepon = "0813", kontakDaruratHubungan = "Istri",
        desa = desa, kelompokTani = null, createdBy = "Admin", createdAt = Instant.parse("2025-02-01T00:00:00Z"),
        updatedAt = Instant.parse("2025-02-01T00:00:00Z"),
        plot = listOf(
            PlotDto(
                id = "pl1", nomor = 1, namaHamparan = "Hamparan A", varietas = "Arabika", tahunTanam = listOf(2020),
                kodeGps = "G1", elevasiMdpl = 1200.0, kemiringanPersen = 15.0, luasKopiHa = 0.5,
                fotoKey = "petani/x.webp", fotoLatitude = -7.123456, fotoLongitude = 107.654321,
                statusKepemilikan = id.caritas_kopi.be.petani.StatusKepemilikanLahan.MS,
                sistemBudidaya = id.caritas_kopi.be.petani.SistemBudidaya.AF, areaKonservasi = "Ada",
                tanamanBaru = 10, pohonProduktif = 100, pohonTidakProduktif = 5,
                pestisidaNama = "Pestisida X", pestisidaBulanTahun = "2025-06",
            ),
        ),
        naungan = listOf(NaunganDto("n1", "Albah", 20, "Peneduh", true, "100 kg", 2019)),
        praktikGap = listOf(
            GapDto("g1", JenisPraktikGap.PEMANGKASAN_KOPI, JawabanGap.YA, "rutin"),
            GapDto("g2", JenisPraktikGap.PEMUPUKAN, JawabanGap.KADANG, null),
            GapDto("g3", JenisPraktikGap.PENGENDALIAN_GULMA, JawabanGap.TIDAK, "Keterangan panjang untuk menguji pembungkusan teks di dalam sel agar kolom tetap pada lebarnya dan tidak mendorong baris meluber ke halaman berikutnya."),
        ),
        produksi = listOf(
            ProduksiDto("r1", 2025, SatuanProduksi.KG, 100.0, null, null, null, 100.0),
        ),
        produk = listOf(
            ProdukDto("d1", JenisProdukDijual.CHERRY, null, true, 500.0),
            ProdukDto("d2", JenisProdukDijual.LAINNYA, "Kulit", false, null),
        ),
        pasar = listOf(
            PasarDto("ps1", KategoriPasar.KOMERSIAL, null, true, 80.0, "Pengepul"),
        ),
        kondisiKebun = listOf(
            KondisiDto("k1", JenisKondisiKebun.KEPEMILIKAN_JELAS, true, "SHM"),
        ),
    )

    private fun desaDto(): DesaDetailDto = DesaDetailDto(
        id = "b1", tahunPendataan = 2025, sumberData = "Wawancara", desa = desa,
        createdBy = "Admin", createdAt = Instant.parse("2025-02-01T00:00:00Z"),
        updatedAt = Instant.parse("2025-02-01T00:00:00Z"),
        luasWilayahHa = 100.0, jumlahPenduduk = 2000, jumlahKK = 500, jumlahPetaniKopi = 300,
        luasArealKopiHa = 50.0, luasKomoditiLainHa = 10.0, latitude = -7.1, longitude = 107.6,
        topografi = "Berbukit", ketinggianMdpl = 1200.0, bulanHujan = "Nov-Apr", bulanKering = "Mei-Okt",
        suhuRataRataC = 20.0, jenisTanah = "Andosol", aksesJalan = "Aspal",
        jarakIbukotaKecamatanKm = 5.0, jarakPasarKm = 3.0, jarakKonservasiKm = 8.0,
        luasAPLHa = 40.0, namaKawasanKonservasi = "TN Gunung", produktivitasKgHaTahun = 800.0,
        hargaCherryRp = 8000, hargaGreenBeanRpKg = 60000, pembeliUtama = "Koperasi",
        jumlahPedagangPengumpul = 4, koperasiAktifUnit = 1, eksportir = "Ya", industriPengolahan = "Ada",
        permasalahanUtama = "Hama", berbatasanKonservasi = true, luasPenyanggaHa = 20.0,
        tutupanHutan = 30.0, tutupanHutanSatuan = id.caritas_kopi.be.desa.SatuanTutupan.PERSEN,
        tutupanAgroforestry = 15.0, tutupanAgroforestrySatuan = id.caritas_kopi.be.desa.SatuanTutupan.HA,
        rawanLongsor = true, lokasiRawanLongsor = "Blok B", rawanErosi = false, lokasiRawanErosi = null,
        konflikSatwa = true, jenisSatwaKonflik = "Monyet", praktikKonservasi = "Terassering",
        kebijakan = listOf(KebijakanDto("kb1", JenisKebijakan.RPJM_DESA, true, "Ada")),
        kelembagaan = listOf(KelembagaanDto("kl1", JenisLembaga.KELOMPOK_TANI, 3, "Aktif")),
    )

    @Test
    fun `pdf petani punya magic bytes dan memuat label kunci`() {
        val model = buildPetaniExportModel(petani(), "http://localhost")
        val html = PetaniPdf.html(model)
        assertTrue(html.contains("FORMULIR DATA BASELINE PETANI KOPI"))
        assertTrue(html.contains("Pemangkasan Kopi Rutin"))
        assertTrue(html.contains("(estimasi)"))
        assertTrue(html.contains("Lihat foto"))
        assertTrue(html.contains("Juni 2025")) // pestisida bulan/tahun

        val pdf = PdfRenderer().render(html)
        assertTrue(pdf.size > 1000)
        assertEquals("%PDF", String(pdf, 0, 4))
    }

    @Test
    fun `pdf desa punya magic bytes`() {
        val model = buildDesaExportModel(desaDto())
        val html = DesaPdf.html(model)
        assertTrue(html.contains("FORMULIR DATA BASELINE DESA"))
        assertTrue(html.contains("RPJM Desa"))
        val pdf = PdfRenderer().render(html)
        assertEquals("%PDF", String(pdf, 0, 4))
    }

    private fun pdfText(pdf: ByteArray): String =
        org.apache.pdfbox.pdmodel.PDDocument.load(pdf).use { doc ->
            org.apache.pdfbox.text.PDFTextStripper().getText(doc)
        }

    private fun pdfPages(pdf: ByteArray): Int =
        org.apache.pdfbox.pdmodel.PDDocument.load(pdf).use { it.numberOfPages }

    @Test
    fun `pdf petani tepat 5 halaman (bagian C 21 praktik tidak meluber)`() {
        val pdf = PdfRenderer().render(PetaniPdf.html(buildPetaniExportModel(petani(), "http://localhost")))
        assertEquals(5, pdfPages(pdf), "bagian C meluber ke halaman tambahan")
    }

    @Test
    fun `pdf footer nomor halaman muncul di petani dan desa`() {
        val petaniPdf = PdfRenderer().render(PetaniPdf.html(buildPetaniExportModel(petani(), "http://localhost")))
        assertTrue(pdfText(petaniPdf).contains(" / "), "footer petani tidak ditemukan")

        val desaPdf = PdfRenderer().render(DesaPdf.html(buildDesaExportModel(desaDto())))
        assertTrue(pdfText(desaPdf).contains(" / "), "footer desa tidak ditemukan")
    }

    @Test
    fun `label D E desa italic tail dirender`() {
        val html = DesaPdf.html(buildDesaExportModel(desaDto()))
        assertTrue(html.contains("font-style:italic"))
        assertTrue(html.contains("Produktivitas Rata-rata"))
    }

    @Test
    fun `docx petani dan desa valid zip`() {
        val docx = PetaniDocx.build(buildPetaniExportModel(petani(), "http://localhost"))
        // DOCX = arsip ZIP -> magic "PK"
        assertEquals("PK", String(docx, 0, 2))

        val docxDesa = DesaDocx.build(buildDesaExportModel(desaDto()))
        assertEquals("PK", String(docxDesa, 0, 2))
    }

    @Test
    fun `docx tiap baris tabel punya jumlah sel seragam`() {
        // Regresi: XWPFTable.createRow() menyalin jumlah sel baris pertama, dulu bikin
        // baris punya sel kosong ekstra (tabel 2 kolom tampil jadi 4 kolom).
        val docx = PetaniDocx.build(buildPetaniExportModel(petani(), "http://localhost"))
        org.apache.poi.xwpf.usermodel.XWPFDocument(java.io.ByteArrayInputStream(docx)).use { doc ->
            assertTrue(doc.tables.isNotEmpty(), "tidak ada tabel")
            doc.tables.forEachIndexed { ti, t ->
                val counts = t.rows.map { it.tableCells.size }.distinct()
                assertEquals(1, counts.size, "tabel $ti punya baris dengan jumlah sel tidak seragam: $counts")
            }
        }
    }

    @Test
    fun `docx tiap tabel punya tblGrid`() {
        // Regresi: tanpa w:tblGrid, Word/LibreOffice mengabaikan lebar per-kolom dan
        // menampilkan semua kolom sama lebar (bagian C jadi kacau).
        val docx = PetaniDocx.build(buildPetaniExportModel(petani(), "http://localhost"))
        org.apache.poi.xwpf.usermodel.XWPFDocument(java.io.ByteArrayInputStream(docx)).use { doc ->
            doc.tables.forEachIndexed { ti, t ->
                val grid = t.ctTbl.tblGrid
                assertTrue(grid != null && grid.sizeOfGridColArray() > 0, "tabel $ti tidak punya tblGrid")
            }
        }
    }

    @Test
    fun `docx tiap baris punya tinggi minimum 23_4pt`() {
        // Regresi: tanpa trHeight, Word mengempiskan baris (teks rapat ke atas/bawah).
        // 468 twips = 23.4pt = pitch baris PDF.
        val docx = PetaniDocx.build(buildPetaniExportModel(petani(), "http://localhost"))
        org.apache.poi.xwpf.usermodel.XWPFDocument(java.io.ByteArrayInputStream(docx)).use { doc ->
            doc.tables.forEachIndexed { ti, t ->
                t.rows.forEachIndexed { ri, row ->
                    assertEquals(468, row.height, "tabel $ti baris $ri tinggi != 468 twips")
                    assertEquals(
                        org.apache.poi.xwpf.usermodel.TableRowHeightRule.AT_LEAST, row.heightRule,
                        "tabel $ti baris $ri heightRule != AT_LEAST",
                    )
                }
            }
        }
    }

    @Test
    fun `escape html`() {
        assertEquals("a &amp; b &lt;c&gt;", Html.esc("a & b <c>"))
    }
}

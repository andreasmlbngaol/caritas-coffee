package id.caritas_kopi.be.export

import id.caritas_kopi.be.export.Docx.CellSpec
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge

/** DOCX petani (padanan builder.ts `docx` npm). */
object PetaniDocx {

    fun build(m: PetaniExportModel): ByteArray {
        val doc = Docx.document()
        Docx.initFirstSection(doc, landscape = false)

        // ---------- A (portrait) ----------
        Docx.title(doc, "FORMULIR DATA BASELINE PETANI KOPI")
        Docx.heading(doc, "A - DATA IDENTITAS PETANI")
        val aTable = Docx.table(doc)
        Docx.borders(aTable)
        m.sectionA.forEach { (k, v) ->
            val r = Docx.row(aTable)
            Docx.cell(r, CellSpec(k, 38.0, bold = true, fill = Docx.HEADER_FILL))
            Docx.cell(r, CellSpec(v, 62.0))
        }
        Docx.spacer(doc)
        val pTable = Docx.table(doc)
        Docx.borders(pTable)
        listOf(
            "Kode Petani / ID_Petani" to m.petugas.kodePetani,
            "Nama Kelompok Tani" to m.petugas.namaKelompok,
            "Kode Kelompok Tani" to m.petugas.kodeKelompok,
        ).forEach { (k, v) ->
            val r = Docx.row(pTable)
            Docx.cell(r, CellSpec(k, 38.0, bold = true, fill = "FDEEEA", color = "C62828"))
            Docx.cell(r, CellSpec(v, 62.0))
        }

        // ---------- B (landscape) ----------
        Docx.endSection(doc, nextLandscape = true)
        Docx.heading(doc, "B.1 - DATA FISIK LOKASI PLOT")
        if (m.sectionB1.isEmpty()) {
            Docx.textParagraph(doc, "Belum ada data plot.")
        } else {
            val t = Docx.table(doc)
            Docx.borders(t)
            val h = Docx.headerRow(t)
            listOf(
                "Plot No." to 6.0, "Nama/ Hamparan" to 16.0, "Varietas Kopi" to 16.0, "Tahun Tanam" to 10.0,
                "Kode GPS" to 14.0, "Elevasi (mdpl)" to 12.0, "Kemiringan (%)" to 12.0, "Luas Kopi (Ha)" to 14.0,
            ).forEach { (txt, w) -> Docx.cell(h, CellSpec(txt, w, bold = true, fill = Docx.HEADER_FILL, center = true)) }
            m.sectionB1.forEach { r ->
                val row = Docx.row(t)
                Docx.cell(row, CellSpec(r[0], 6.0, center = true))
                Docx.cell(row, CellSpec(r[1], 16.0))
                Docx.cell(row, CellSpec(r[2], 16.0))
                Docx.cell(row, CellSpec(r[3], 10.0, center = true))
                Docx.cell(row, CellSpec(r[4], 14.0))
                Docx.cell(row, CellSpec(r[5], 12.0, center = true))
                Docx.cell(row, CellSpec(r[6], 12.0, center = true))
                Docx.cell(row, CellSpec(r[7], 14.0, center = true))
            }
        }

        if (m.sectionB1b.isNotEmpty()) {
            Docx.heading(doc, "B.1 - DATA FISIK LOKASI PLOT (lanjutan)")
            val t = Docx.table(doc)
            Docx.borders(t)
            val h = Docx.headerRow(t)
            listOf(
                "Plot No." to 5.0, "Foto Geotagged" to 13.0, "Status Kepemilikan Lahan" to 12.0, "Sistem Budidaya" to 10.0,
                "Area Konservasi" to 18.0, "Tanaman Baru" to 11.0, "Pohon Produktif" to 11.0,
                "Pohon Tdk Produktif" to 11.0, "Pestisida Terakhir" to 9.0,
            ).forEach { (txt, w) -> Docx.cell(h, CellSpec(txt, w, bold = true, fill = Docx.HEADER_FILL, center = true)) }
            m.sectionB1b.forEach { r ->
                val row = Docx.row(t)
                Docx.cell(row, CellSpec(r.no, 5.0, center = true))
                Docx.cell(row, CellSpec(if (r.fotoUrl != null) "Lihat foto" else "-", 13.0, center = true, href = r.fotoUrl))
                Docx.cell(row, CellSpec(r.status, 12.0, center = true))
                Docx.cell(row, CellSpec(r.sistem, 10.0, center = true))
                Docx.cell(row, CellSpec(r.konservasi, 18.0))
                Docx.cell(row, CellSpec(r.tanamanBaru, 11.0, center = true))
                Docx.cell(row, CellSpec(r.pohonProduktif, 11.0, center = true))
                Docx.cell(row, CellSpec(r.pohonTidakProduktif, 11.0, center = true))
                Docx.cell(row, CellSpec(r.pestisida, 9.0))
            }
            Docx.legend(
                doc,
                "Kode Kepemilikan: MS = Milik Sendiri  |  SW = Sewa  |  BH = Bagi Hasil  |  TA = Tanah Adat  |  L = Lainnya      " +
                    "Kode Sistem Budidaya: AF = Agroforestry  |  MK = Monokultur",
            )
        }

        Docx.heading(doc, "B.2 - DATA TANAMAN NAUNGAN / SELA / TEGAKAN (TINGKAT PETANI)")
        if (m.sectionB2.isEmpty()) {
            Docx.textParagraph(doc, "Tidak ada Tanaman Naungan, Sela, Tegakan.")
        } else {
            val t = Docx.table(doc)
            Docx.borders(t)
            val h = Docx.headerRow(t)
            listOf(
                "No." to 6.0, "Jenis Naungan/Sela/Tegakan" to 20.0, "Jumlah" to 9.0, "Fungsi" to 20.0,
                "Apakah dilakukan pemangkasan" to 15.0, "Produksi/Tahun" to 18.0, "Tahun Tanam" to 12.0,
            ).forEach { (txt, w) -> Docx.cell(h, CellSpec(txt, w, bold = true, fill = Docx.HEADER_FILL, center = true)) }
            m.sectionB2.forEach { r ->
                val row = Docx.row(t)
                Docx.cell(row, CellSpec(r[0], 6.0, center = true))
                Docx.cell(row, CellSpec(r[1], 20.0))
                Docx.cell(row, CellSpec(r[2], 9.0, center = true))
                Docx.cell(row, CellSpec(r[3], 20.0))
                Docx.cell(row, CellSpec(r[4], 15.0, center = true))
                Docx.cell(row, CellSpec(r[5], 18.0))
                Docx.cell(row, CellSpec(r[6], 12.0, center = true))
            }
        }

        // ---------- C (portrait) ----------
        Docx.endSection(doc, nextLandscape = false)
        Docx.heading(doc, "C - PRAKTIK GAP KEBUN")
        val cTable = Docx.table(doc)
        Docx.borders(cTable)
        val ch = Docx.headerRow(cTable)
        // Kolom kelompok 15% (PDF 13%) supaya "Pemeliharaan" tidak patah di Word; Ya/
        // Tidak/Kadang dilebihkan dari PDF agar headernya tidak patah 2 baris.
        listOf(" " to 15.0, "Jenis Praktik" to 28.0, "Ya" to 7.0, "Tidak" to 9.0, "Kadang" to 10.0, "Keterangan" to 31.0)
            .forEach { (txt, w) -> Docx.cell(ch, CellSpec(txt, w, bold = true, fill = Docx.HEADER_FILL, center = true)) }
        m.sectionC.forEach { g ->
            g.rows.forEachIndexed { i, r ->
                val row = Docx.row(cTable)
                if (i == 0) {
                    Docx.cell(row, CellSpec(g.kelompok, 15.0, bold = true, vMerge = STMerge.RESTART))
                } else {
                    Docx.cell(row, CellSpec("", 15.0, vMerge = STMerge.CONTINUE))
                }
                Docx.cell(row, CellSpec(r.label, 28.0))
                Docx.cell(row, CellSpec(if (r.jawaban == "YA") "✓" else "", 7.0, center = true))
                Docx.cell(row, CellSpec(if (r.jawaban == "TIDAK") "✓" else "", 9.0, center = true))
                Docx.cell(row, CellSpec(if (r.jawaban == "KADANG") "✓" else "", 10.0, center = true))
                Docx.cell(row, CellSpec(r.keterangan, 31.0))
            }
        }

        // ---------- D + E (portrait, satu section) ----------
        Docx.endSection(doc, nextLandscape = false)
        Docx.heading(doc, "D - RIWAYAT ESTIMASI PRODUKSI")
        val dTable = Docx.table(doc)
        Docx.borders(dTable)
        val dh = Docx.headerRow(dTable)
        listOf(
            "Tahun" to 12.0, "Satuan (Kg, Solup, Bambu, Kaleng)" to 22.0, "Cherry" to 11.0,
            "Gabah Basah / Labu" to 13.0, "Gabah Kering" to 12.0, "Green Bean" to 12.0, "Produktivitas" to 18.0,
        ).forEach { (txt, w) -> Docx.cell(dh, CellSpec(txt, w, bold = true, fill = Docx.HEADER_FILL, center = true)) }
        m.sectionD.forEach { r ->
            val row = Docx.row(dTable)
            Docx.cell(row, CellSpec(r[0], 12.0, bold = true))
            (1..6).forEach { j -> Docx.cell(row, CellSpec(r[j], listOf(22.0, 11.0, 13.0, 12.0, 12.0, 18.0)[j - 1], center = true)) }
        }

        Docx.heading(doc, "E.1 - JENIS PRODUK YANG DIJUAL")
        val e1 = Docx.table(doc)
        Docx.borders(e1)
        val e1h = Docx.headerRow(e1)
        listOf("Jenis Produk" to 55.0, "Ya" to 10.0, "Tidak" to 10.0, "Volume (Kg/Tahun)" to 25.0)
            .forEach { (txt, w) -> Docx.cell(e1h, CellSpec(txt, w, bold = true, fill = Docx.HEADER_FILL, center = true)) }
        m.sectionE1.forEach { r ->
            val row = Docx.row(e1)
            Docx.cell(row, CellSpec(r.label, 55.0))
            Docx.cell(row, CellSpec(if (r.aktif) "✓" else "", 10.0, center = true))
            Docx.cell(row, CellSpec(if (!r.aktif) "✓" else "", 10.0, center = true))
            Docx.cell(row, CellSpec(r.volume, 25.0, center = true))
        }

        Docx.heading(doc, "E.2 - KATEGORI PASAR")
        val e2 = Docx.table(doc)
        Docx.borders(e2)
        val e2h = Docx.headerRow(e2)
        listOf(
            "Kategori Pasar" to 40.0, "Ya" to 10.0, "Tidak" to 10.0,
            "Persentase (%)" to 12.0, "Profil Penjual (Pengepul, Pabrik, Ekspor, dll.)" to 28.0,
        ).forEach { (txt, w) -> Docx.cell(e2h, CellSpec(txt, w, bold = true, fill = Docx.HEADER_FILL, center = true)) }
        m.sectionE2.forEach { r ->
            val row = Docx.row(e2)
            Docx.cell(row, CellSpec(r.label, 40.0))
            Docx.cell(row, CellSpec(if (r.aktif) "✓" else "", 10.0, center = true))
            Docx.cell(row, CellSpec(if (!r.aktif) "✓" else "", 10.0, center = true))
            Docx.cell(row, CellSpec(r.persentase, 12.0, center = true))
            Docx.cell(row, CellSpec(r.profil, 28.0))
        }

        // ---------- F (portrait) ----------
        Docx.endSection(doc, nextLandscape = false)
        Docx.heading(doc, "F - KONDISI KEBUN")
        val fTable = Docx.table(doc)
        Docx.borders(fTable)
        val fh = Docx.headerRow(fTable)
        listOf("Kondisi Kebun" to 46.0, "Ya" to 9.0, "Tidak" to 9.0, "Keterangan" to 36.0)
            .forEach { (txt, w) -> Docx.cell(fh, CellSpec(txt, w, bold = true, fill = Docx.HEADER_FILL, center = true)) }
        m.sectionF.forEach { r ->
            val row = Docx.row(fTable)
            Docx.cell(row, CellSpec(r.label, 46.0))
            Docx.cell(row, CellSpec(if (r.jawaban) "✓" else "", 9.0, center = true))
            Docx.cell(row, CellSpec(if (!r.jawaban) "✓" else "", 9.0, center = true))
            Docx.cell(row, CellSpec(r.keterangan, 36.0))
        }

        return Docx.write(doc)
    }
}

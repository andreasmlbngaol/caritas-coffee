package id.caritas_kopi.be.export

import id.caritas_kopi.be.export.Docx.CellSpec

/** DOCX desa (padanan builder.ts `docx` npm). */
object DesaDocx {

    fun build(m: DesaExportModel): ByteArray {
        val doc = Docx.document()
        Docx.initFirstSection(doc, landscape = false)

        Docx.title(doc, "FORMULIR DATA BASELINE DESA")

        Docx.heading(doc, "A - DATA DESA / WILAYAH")
        val aTable = Docx.table(doc)
        Docx.borders(aTable)
        m.sectionA.forEach { (left, right) ->
            val r = Docx.row(aTable)
            Docx.cell(r, CellSpec(left.first, 21.0, bold = true, fill = Docx.HEADER_FILL))
            Docx.cell(r, CellSpec(left.second, 29.0))
            Docx.cell(r, CellSpec(right.first, 21.0, bold = true, fill = Docx.HEADER_FILL))
            Docx.cell(r, CellSpec(right.second, 29.0))
        }

        Docx.heading(doc, "B - KEBIJAKAN LOKAL")
        val bTable = Docx.table(doc)
        Docx.borders(bTable)
        val bh = Docx.headerRow(bTable)
        listOf("KEBIJAKAN LOKAL" to 40.0, "ADA" to 10.0, "TIDAK" to 10.0, "KETERANGAN" to 40.0)
            .forEach { (txt, w) -> Docx.cell(bh, CellSpec(txt, w, bold = true, fill = Docx.HEADER_FILL, center = true)) }
        m.sectionB.forEach { r ->
            val row = Docx.row(bTable)
            Docx.cell(row, CellSpec(r.label, 40.0))
            Docx.cell(row, CellSpec(if (r.ada) "✓" else "", 10.0, center = true))
            Docx.cell(row, CellSpec(if (!r.ada) "✓" else "", 10.0, center = true))
            Docx.cell(row, CellSpec(r.keterangan, 40.0))
        }

        Docx.heading(doc, "C - KELEMBAGAAN")
        val cTable = Docx.table(doc)
        Docx.borders(cTable)
        val ch = Docx.headerRow(cTable)
        listOf("LEMBAGA" to 30.0, "JUMLAH" to 20.0, "KONDISI" to 50.0)
            .forEach { (txt, w) -> Docx.cell(ch, CellSpec(txt, w, bold = true, fill = Docx.HEADER_FILL, center = true)) }
        m.sectionC.forEach { r ->
            val row = Docx.row(cTable)
            Docx.cell(row, CellSpec(r.label, 30.0, bold = true))
            Docx.cell(row, CellSpec(r.jumlah, 20.0, center = true))
            Docx.cell(row, CellSpec(r.kondisi, 50.0))
        }

        Docx.heading(doc, "D - KONDISI BISNIS KOPI SAAT INI")
        kvTable(doc, m.sectionD)

        Docx.heading(doc, "E - KONDISI KONSERVASI")
        kvTable(doc, m.sectionE)

        return Docx.write(doc)
    }

    private fun kvTable(doc: org.apache.poi.xwpf.usermodel.XWPFDocument, rows: List<Pair<String, String>>) {
        val t = Docx.table(doc)
        Docx.borders(t)
        rows.forEach { (k, v) ->
            val row = Docx.row(t)
            val m = Regex("^(.*?)\\s*(\\([^)]*\\))$").find(k)
            if (m != null) {
                Docx.cell(row, CellSpec("${m.groupValues[1]} ", 42.0, bold = true, fill = Docx.HEADER_FILL, italicTail = m.groupValues[2]))
            } else {
                Docx.cell(row, CellSpec(k, 42.0, bold = true, fill = Docx.HEADER_FILL))
            }
            Docx.cell(row, CellSpec(v, 58.0))
        }
    }
}

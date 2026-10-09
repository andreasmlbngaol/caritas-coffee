package id.caritas_kopi.be.export

import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.TableRowHeightRule
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFTable
import org.apache.poi.xwpf.usermodel.XWPFTableCell
import org.apache.poi.xwpf.usermodel.XWPFTableRow
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblLayoutType
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalJc
import java.math.BigInteger

/**
 * Helper DOCX (Apache POI XWPF) - padanan builder `docx` npm proyek lama.
 * Border 0.8px abu #999999, margin sel, header abu #E5E5E5, font Calibri 10pt.
 */
object Docx {
    const val BORDER = "999999"
    const val HEADER_FILL = "E5E5E5"
    private const val FONT = "Arial" // padanan Helvetica PDF (metrik sama, tersedia di Word)
    private const val FONT_SIZE = 9 // POI setFontSize(double) mengalikan 2 (simpan half-points)

    // ---------- Dokumen / section ----------

    fun document(): XWPFDocument = XWPFDocument()

    /** Akhiri section aktif; pindahkan sectPr body ke paragraf, buat sectPr baru. */
    fun endSection(doc: XWPFDocument, nextLandscape: Boolean) {
        val body = doc.document.body
        val sectPr = if (body.isSetSectPr) body.sectPr else body.addNewSectPr()
        val p = doc.createParagraph()
        val pPr = if (p.ctp.isSetPPr) p.ctp.pPr else p.ctp.addNewPPr()
        // Copy (bukan pindah) - memindah objek CT bikin orphan xmlbeans.
        pPr.addNewSectPr().set(sectPr)
        body.unsetSectPr()
        applyOrientation(body.addNewSectPr(), nextLandscape)
    }

    /** Set orientasi section body terakhir (dipanggil sekali di awal). */
    fun initFirstSection(doc: XWPFDocument, landscape: Boolean) {
        val body = doc.document.body
        val sectPr = if (body.isSetSectPr) body.sectPr else body.addNewSectPr()
        applyOrientation(sectPr, landscape)
    }

    private fun applyOrientation(sectPr: org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr, landscape: Boolean) {
        val pgSz = if (sectPr.isSetPgSz) sectPr.pgSz else sectPr.addNewPgSz()
        if (landscape) {
            pgSz.w = BigInteger.valueOf(16838)
            pgSz.h = BigInteger.valueOf(11906)
            pgSz.orient = STPageOrientation.LANDSCAPE
        } else {
            pgSz.w = BigInteger.valueOf(11906)
            pgSz.h = BigInteger.valueOf(16838)
            pgSz.orient = STPageOrientation.PORTRAIT
        }
        // Margin efektif 36pt (= 720 twips) keempat sisi: @page PDF 30pt + margin body
        // bawaan 6pt (openhtmltopdf), jadi tepi konten PDF juga di 36pt. Simetris.
        val mar = if (sectPr.isSetPgMar) sectPr.pgMar else sectPr.addNewPgMar()
        mar.top = BigInteger.valueOf(720)
        mar.bottom = BigInteger.valueOf(720)
        mar.left = BigInteger.valueOf(720)
        mar.right = BigInteger.valueOf(720)
    }

    // ---------- Paragraf ----------

    fun title(doc: XWPFDocument, text: String) {
        val p = doc.createParagraph()
        p.alignment = ParagraphAlignment.CENTER
        singleSpacing(p)
        val r = p.createRun()
        r.setText(text)
        r.isBold = true
        r.fontSize = 14
        r.fontFamily = FONT
    }

    fun heading(doc: XWPFDocument, text: String) {
        val p = doc.createParagraph()
        singleSpacing(p)
        p.spacingBefore = 280
        p.spacingAfter = 120
        val r = p.createRun()
        r.setText(text)
        r.isBold = true
        r.fontSize = 10
        r.fontFamily = FONT
    }

    fun spacer(doc: XWPFDocument) {
        val p = doc.createParagraph()
        singleSpacing(p)
        p.spacingAfter = 240 // 12pt, sama dengan <div height:12pt> PDF di antara tabel A
    }

    fun legend(doc: XWPFDocument, text: String) {
        val p = doc.createParagraph()
        singleSpacing(p)
        p.spacingBefore = 80
        val r = p.createRun()
        r.setText(text)
        r.fontSize = 8
        r.fontFamily = FONT
        r.color = "444444"
    }

    fun textParagraph(doc: XWPFDocument, text: String) {
        val p = doc.createParagraph()
        singleSpacing(p)
        val r = p.createRun()
        r.setText(text)
        r.fontSize = FONT_SIZE
        r.fontFamily = FONT
    }

    /** Spasi baris tunggal + tanpa jarak sebelum/sesudah (Word default 1.15 bikin tinggi beda dari PDF). */
    private fun singleSpacing(p: XWPFParagraph) {
        p.setSpacingBetween(1.0, org.apache.poi.xwpf.usermodel.LineSpacingRule.AUTO)
        p.spacingBefore = 0
        p.spacingAfter = 0
    }

    // ---------- Tabel ----------

    fun table(doc: XWPFDocument): XWPFTable {
        val t = doc.createTable()
        // createTable() menyisakan satu baris/sel default - buang agar kolom tidak bergeser.
        if (t.rows.isNotEmpty()) t.removeRow(0)
        setTableWidthPct(t, 100)
        return t
    }

    private fun setTableWidthPct(t: XWPFTable, pct: Int) {
        val tblPr = t.ctTbl.tblPr ?: t.ctTbl.addNewTblPr()
        val w = if (tblPr.isSetTblW) tblPr.tblW else tblPr.addNewTblW()
        w.type = STTblWidth.PCT
        w.w = BigInteger.valueOf(pct * 50L)
        val layout = if (tblPr.isSetTblLayout) tblPr.tblLayout else tblPr.addNewTblLayout()
        layout.type = STTblLayoutType.FIXED
    }

    /**
     * Baris baru tanpa sel bawaan. XWPFTable.createRow() menyalin jumlah sel baris
     * pertama (lihat bytecode: sizeOfTcArray baris 0), jadi sel bawaan itu dibuang
     * dulu agar tiap baris hanya punya sel yang kita tambahkan.
     */
    private fun newRow(t: XWPFTable): XWPFTableRow {
        val row = t.createRow()
        while (row.tableCells.isNotEmpty()) row.removeCell(0)
        // Tinggi baris minimum 23.4pt (468 twips) = pitch baris PDF. Word memakai
        // trHeight sebagai tinggi total minimum; margin sel vertikal dihilangkan (lihat
        // cell()) supaya LibreOffice tidak menambahnya di atas trHeight.
        row.height = 468
        row.heightRule = TableRowHeightRule.AT_LEAST
        return row
    }

    fun row(t: XWPFTable): XWPFTableRow = newRow(t)

    /** Baris header tabel: ulang di setiap halaman (padanan tableHeader: true). */
    fun headerRow(t: XWPFTable): XWPFTableRow {
        val row = newRow(t)
        val trPr = if (row.ctRow.isSetTrPr) row.ctRow.trPr else row.ctRow.addNewTrPr()
        trPr.addNewTblHeader()
        return row
    }

    /** Baris pertama tabel (tabel dibuat tanpa baris default). */
    fun firstRow(t: XWPFTable): XWPFTableRow = newRow(t)

    data class CellSpec(
        val text: String,
        val widthPct: Double,
        val bold: Boolean = false,
        val fill: String? = null,
        val color: String? = null,
        val center: Boolean = false,
        val vMerge: STMerge.Enum? = null,
        val href: String? = null,
        val italicTail: String? = null,
    )

    fun cell(row: XWPFTableRow, spec: CellSpec): XWPFTableCell {
        val cell = row.createCell()
        val tcPr = if (cell.ctTc.isSetTcPr) cell.ctTc.tcPr else cell.ctTc.addNewTcPr()
        // urutan elemen sesuai skema OOXML: tcW, vMerge, shd, tcMar, vAlign
        val tcW = if (tcPr.isSetTcW) tcPr.tcW else tcPr.addNewTcW()
        tcW.type = STTblWidth.PCT
        tcW.w = BigInteger.valueOf((spec.widthPct * 50).toLong())
        if (spec.vMerge != null) {
            val vm = if (tcPr.isSetVMerge) tcPr.vMerge else tcPr.addNewVMerge()
            vm.setVal(spec.vMerge)
        }
        if (spec.fill != null) {
            val shd = if (tcPr.isSetShd) tcPr.shd else tcPr.addNewShd()
            shd.fill = spec.fill
        }
        val mar = if (tcPr.isSetTcMar) tcPr.tcMar else tcPr.addNewTcMar()
        // Padding horizontal 7pt; vertikal 0 karena tinggi baris sudah dipatok trHeight
        // 23.4pt (lihat newRow) dan teks di-center vertikal, jadi jarak atas/bawah muncul
        // dari tinggi baris. Ini menghindari renderer menambah margin di atas trHeight.
        // Urutan anak CTTcMar sesuai skema OOXML: top, left, bottom, right.
        mar.addNewTop().w = BigInteger.valueOf(0)
        mar.addNewLeft().w = BigInteger.valueOf(140)
        mar.addNewBottom().w = BigInteger.valueOf(0)
        mar.addNewRight().w = BigInteger.valueOf(140)
        val vAlign = if (tcPr.isSetVAlign) tcPr.vAlign else tcPr.addNewVAlign()
        vAlign.`val` = STVerticalJc.CENTER

        fillCell(cell, spec.text, spec.bold, spec.color, if (spec.center) ParagraphAlignment.CENTER else null, spec.href, spec.italicTail)
        return cell
    }

    private fun fillCell(
        cell: XWPFTableCell,
        text: String,
        bold: Boolean,
        color: String?,
        align: ParagraphAlignment?,
        href: String?,
        italicTail: String?,
    ) {
        val p: XWPFParagraph = if (cell.paragraphs.isEmpty()) cell.addParagraph() else cell.paragraphs[0]
        singleSpacing(p)
        if (align != null) p.alignment = align
        if (italicTail != null) {
            val main = p.createRun()
            main.setText(text)
            main.isBold = bold
            main.fontSize = FONT_SIZE
            main.fontFamily = FONT
            val tail = p.createRun()
            tail.setText(italicTail)
            tail.isBold = bold
            tail.isItalic = true
            tail.fontSize = 8
            tail.fontFamily = FONT
            return
        }
        if (href != null) {
            val r = p.createHyperlinkRun(href)
            r.setText(text)
            r.isBold = bold
            r.fontSize = FONT_SIZE
            r.fontFamily = FONT
            r.color = color ?: "1D4ED8"
            r.underline = org.apache.poi.xwpf.usermodel.UnderlinePatterns.SINGLE
            return
        }
        text.split("\n").forEachIndexed { i, line ->
            if (i > 0) p.createRun().addBreak()
            val r = p.createRun()
            r.setText(line)
            r.isBold = bold
            r.fontSize = FONT_SIZE
            r.fontFamily = FONT
            if (color != null) r.color = color
        }
    }

    /** Border tabel abu tipis (size 2 = 0.25pt ~ 0.8px). */
    fun borders(t: XWPFTable) {
        val size = 2
        val color = BORDER
        t.setInsideHBorder(org.apache.poi.xwpf.usermodel.XWPFTable.XWPFBorderType.SINGLE, size, 0, color)
        t.setInsideVBorder(org.apache.poi.xwpf.usermodel.XWPFTable.XWPFBorderType.SINGLE, size, 0, color)
        t.setTopBorder(org.apache.poi.xwpf.usermodel.XWPFTable.XWPFBorderType.SINGLE, size, 0, color)
        t.setBottomBorder(org.apache.poi.xwpf.usermodel.XWPFTable.XWPFBorderType.SINGLE, size, 0, color)
        t.setLeftBorder(org.apache.poi.xwpf.usermodel.XWPFTable.XWPFBorderType.SINGLE, size, 0, color)
        t.setRightBorder(org.apache.poi.xwpf.usermodel.XWPFTable.XWPFBorderType.SINGLE, size, 0, color)
    }

    /**
     * Tambahkan w:tblGrid dari lebar sel baris pertama. POI tidak menulis tblGrid,
     * dan tanpa itu Word/LibreOffice mengabaikan lebar per-kolom (kolom jadi sama
     * rata). Nilai gridCol = lebar konten section x persen sel.
     */
    private fun buildGrid(t: XWPFTable, contentWidth: Long) {
        if (t.ctTbl.tblGrid != null) return
        val first = t.rows.firstOrNull() ?: return
        val pcts = first.tableCells.map { c -> numOrZero(c.ctTc.tcPr?.tcW?.w) / 50.0 }
        val grid = t.ctTbl.addNewTblGrid()
        pcts.forEach { p -> grid.addNewGridCol().w = BigInteger.valueOf((p / 100.0 * contentWidth).toLong()) }
    }

    fun write(doc: XWPFDocument): ByteArray {
        doc.tables.forEach { buildGrid(it, contentWidth(doc)) }
        val out = java.io.ByteArrayOutputStream()
        doc.write(out)
        doc.close()
        return out.toByteArray()
    }

    /** Lebar area konten body terakhir (pgSz.w - margin kiri - kanan), dalam twips. */
    private fun contentWidth(doc: XWPFDocument): Long {
        val sectPr = doc.document.body.sectPr ?: return 10466L
        val w = numOrZero(sectPr.pgSz?.w).takeIf { it > 0 } ?: 11906L
        val mar = sectPr.pgMar
        val left = numOrZero(mar?.left).takeIf { it > 0 } ?: 720L
        val right = numOrZero(mar?.right).takeIf { it > 0 } ?: 720L
        return w - left - right
    }

    private fun numOrZero(v: Any?): Long = when (v) {
        is Number -> v.toLong()
        is BigInteger -> v.toLong()
        null -> 0L
        else -> v.toString().toLongOrNull() ?: 0L
    }
}

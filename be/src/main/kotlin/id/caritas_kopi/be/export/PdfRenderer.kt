package id.caritas_kopi.be.export

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream

/**
 * Render PDF via openhtmltopdf (HTML/CSS) - memetakan tata letak react-pdf proyek lama.
 * Semua teks di-escape; newline jadi <br/>.
 */
@Component
class PdfRenderer {

    fun render(html: String): ByteArray {
        val out = ByteArrayOutputStream()
        val builder = PdfRendererBuilder()
        builder.useFastMode()
        builder.withHtmlContent(html, null)
        builder.toStream(out)
        builder.run()
        return out.toByteArray()
    }
}

// ---------- Helper HTML bersama ----------

object Html {
    const val BORDER = "#999999"
    const val LABEL_BG = "#e5e5e5"

    fun esc(s: String): String = ExportSupport.escape(s)

    /** newline -> <br/> setelah escape. */
    fun text(s: String): String = esc(s).replace("\n", "<br/>")

    fun cell(content: String, opts: CellOpts = CellOpts()): String {
        val styles = buildString {
            append("padding:6pt 7pt;border-right:0.8pt solid $BORDER;border-bottom:0.8pt solid $BORDER;")
            append("vertical-align:middle;")
            if (opts.width != null) append("width:${opts.width};")
            if (opts.fill != null) append("background:${opts.fill};")
            if (opts.center) append("text-align:center;")
            if (opts.bold) append("font-weight:bold;")
            if (opts.color != null) append("color:${opts.color};")
        }
        return "<td style=\"$styles\">${text(content)}</td>"
    }

    data class CellOpts(
        val width: String? = null,
        val fill: String? = null,
        val center: Boolean = false,
        val bold: Boolean = false,
        val color: String? = null,
    )

    fun head(content: String, width: String): String =
        cell(content, CellOpts(width = width, fill = LABEL_BG, center = true, bold = true))

    fun label(content: String, width: String): String =
        cell(content, CellOpts(width = width, fill = LABEL_BG, bold = true))

    fun tick(on: Boolean, width: String): String {
        // Centang digambar CSS (garis diputar 45 derajat), bukan glyph font, karena
        // font standar PDF (Helvetica) tak punya U+2713 sehingga tampil sebagai "#"/kotak.
        val box = if (on) {
            "<span style=\"display:inline-block;width:11px;height:11px;border:0.8pt solid #333;" +
                "position:relative;vertical-align:middle;\"><span style=\"position:absolute;left:3px;top:0.5px;" +
                "width:3px;height:6px;border:solid #1a1a1a;border-width:0 1.5pt 1.5pt 0;transform:rotate(45deg);\"></span></span>"
        } else {
            "<span style=\"display:inline-block;width:11px;height:11px;border:0.8pt solid #333;vertical-align:middle;\"></span>"
        }
        return "<td style=\"padding:6pt 7pt;border-right:0.8pt solid $BORDER;" +
            "border-bottom:0.8pt solid $BORDER;vertical-align:middle;text-align:center;width:$width;\">$box</td>"
    }

    fun table(body: String): String = "<table style=\"width:100%;table-layout:fixed;border-collapse:collapse;" +
        "border-top:0.8pt solid $BORDER;border-left:0.8pt solid $BORDER;\">$body</table>"

    fun row(cells: String): String = "<tr>$cells</tr>"

    fun sectionTitle(t: String): String =
        "<div style=\"font-size:10pt;font-weight:bold;margin:14pt 0 6pt 0;\">${esc(t)}</div>"

    /** Judul tanpa margin atas (dipakai di dalam baris D/E yang sudah punya margin-top sendiri). */
    fun sectionTitleFlat(t: String): String =
        "<div style=\"font-size:10pt;font-weight:bold;margin:0 0 6pt 0;\">${esc(t)}</div>"

    /** Label dengan parenthetical "(...)" di ujung: bold + italic 7.5pt (padanan LabelCell D/E). */
    fun labelTailItalic(k: String, width: String): String {
        val m = Regex("^(.*?)\\s*(\\([^)]*\\))$").find(k)
        val content = if (m != null) {
            "<b>${esc(m.groupValues[1])} </b><span style=\"font-size:7.5pt;font-style:italic;\">${esc(m.groupValues[2])}</span>"
        } else {
            "<b>${esc(k)}</b>"
        }
        return "<td style=\"padding:6pt 7pt;border-right:0.8pt solid $BORDER;border-bottom:0.8pt solid $BORDER;" +
            "vertical-align:middle;width:$width;background:$LABEL_BG;\">$content</td>"
    }

    const val RED = "#c62828"
    const val RED_FILL = "#fdeaea"
}

package id.caritas_kopi.be.export

/** Bangun HTML untuk PDF desa (padanan DesaPdf react-pdf). */
object DesaPdf {

    fun html(m: DesaExportModel): String {
        val sb = StringBuilder()
        sb.append(
            """
            <!DOCTYPE html><html><head><meta charset="utf-8"/><style>
            @page { size: A4 portrait; margin: 30pt;
                @bottom-right { content: counter(page) " / " counter(pages); font-size: 7pt; color: #999; } }
            body { font-family: Helvetica, sans-serif; font-size: 9pt; color: #1a1a1a; }
            .title { font-size: 14pt; font-weight: bold; text-align: center; margin: 0 0 4pt 0; }
            table { width: 100%; border-collapse: collapse; }
            .de-row { display: table; width: 100%; margin-top: 14pt; }
            .de-col { display: table-cell; width: 50%; vertical-align: top; }
            </style></head><body>
            """.trimIndent(),
        )

        sb.append("<div class=\"title\">FORMULIR DATA BASELINE DESA</div>")

        sb.append(Html.sectionTitle("A - DATA DESA / WILAYAH"))
        sb.append(Html.table(m.sectionA.joinToString("") { (left, right) ->
            Html.row(
                Html.cell(left.first, Html.CellOpts("21%", Html.LABEL_BG, bold = true)) + Html.cell(left.second, Html.CellOpts("29%")) +
                    Html.cell(right.first, Html.CellOpts("21%", Html.LABEL_BG, bold = true)) + Html.cell(right.second, Html.CellOpts("29%")),
            )
        }))

        sb.append(Html.sectionTitle("B - KEBIJAKAN LOKAL"))
        sb.append(Html.table(
            Html.row(
                Html.head("KEBIJAKAN LOKAL", "40%") + Html.head("ADA", "10%") + Html.head("TIDAK", "10%") + Html.head("KETERANGAN", "40%"),
            ) + m.sectionB.joinToString("") { r ->
                Html.row(
                    Html.cell(r.label, Html.CellOpts("40%")) + Html.tick(r.ada, "10%") + Html.tick(!r.ada, "10%") +
                        Html.cell(r.keterangan, Html.CellOpts("40%")),
                )
            },
        ))

        sb.append(Html.sectionTitle("C - KELEMBAGAAN"))
        sb.append(Html.table(
            Html.row(Html.head("LEMBAGA", "30%") + Html.head("JUMLAH", "20%") + Html.head("KONDISI", "50%")) +
                m.sectionC.joinToString("") { r ->
                    Html.row(
                        Html.cell(r.label, Html.CellOpts("30%", Html.LABEL_BG, bold = true)) +
                            Html.cell(r.jumlah, Html.CellOpts("20%", center = true)) + Html.cell(r.kondisi, Html.CellOpts("50%")),
                    )
                },
        ))

        // D kiri, E kanan (dua kolom)
        sb.append("<div class=\"de-row\"><div class=\"de-col\">")
        sb.append(Html.sectionTitleFlat("D - KONDISI BISNIS KOPI SAAT INI"))
        sb.append(kvTable(m.sectionD))
        sb.append("</div><div class=\"de-col\" style=\"padding-left:14pt;\">")
        sb.append(Html.sectionTitleFlat("E - KONDISI KONSERVASI"))
        sb.append(kvTable(m.sectionE))
        sb.append("</div></div>")

        sb.append("</body></html>")
        return sb.toString()
    }

    private fun kvTable(rows: List<Pair<String, String>>): String = Html.table(
        rows.joinToString("") { (k, v) ->
            Html.row(Html.labelTailItalic(k, "55%") + Html.cell(v))
        },
    )
}

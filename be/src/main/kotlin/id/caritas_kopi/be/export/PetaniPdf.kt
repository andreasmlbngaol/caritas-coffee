package id.caritas_kopi.be.export

/** Bangun HTML untuk PDF petani (padanan PetaniPdf react-pdf: 5 halaman, B landscape). */
object PetaniPdf {

    fun html(m: PetaniExportModel): String {
        val sb = StringBuilder()
        sb.append(
            """
            <!DOCTYPE html><html><head><meta charset="utf-8"/><style>
            @page { size: A4 portrait; margin: 30pt;
                @bottom-right { content: counter(page) " / " counter(pages); font-size: 7pt; color: #999; } }
            @page landscape { size: A4 landscape; margin: 30pt;
                @bottom-right { content: counter(page) " / " counter(pages); font-size: 7pt; color: #999; } }
            body { font-family: Helvetica, sans-serif; font-size: 9pt; color: #1a1a1a; }
            /* Konten halaman landscape: openhtmltopdf tidak melebarkan kotak isi untuk
               named page landscape, jadi lebar eksplisit. Sedikit di bawah lebar kotak isi
               (781.875pt) agar border kanan tabel tidak jatuh persis di tepi konten
               (openhtmltopdf memotongnya setengah). */
            .landscape { page: landscape; width: 775pt; }
            .title { font-size: 14pt; font-weight: bold; text-align: center; margin: 0 0 4pt 0; }
            .pagebreak { page-break-before: always; }
            .legend { font-size: 8pt; color: #444; margin-top: 4pt; }
            table { width: 100%; border-collapse: collapse; }
            </style></head><body>
            """.trimIndent(),
        )

        // ---------- HALAMAN 1: A (portrait) ----------
        sb.append("<div>")
        sb.append("<div class=\"title\">FORMULIR DATA BASELINE PETANI KOPI</div>")
        sb.append(Html.sectionTitle("A - DATA IDENTITAS PETANI"))
        sb.append(Html.table(m.sectionA.joinToString("") { (k, v) ->
            Html.row(Html.label(k, "38%") + Html.cell(v))
        }))
        sb.append("<div style=\"height:12pt\"></div>")
        sb.append(Html.table(
            Html.row(Html.cell("Kode Petani / ID_Petani", Html.CellOpts("38%", Html.RED_FILL, bold = true, color = Html.RED)) + Html.cell(m.petugas.kodePetani)) +
                Html.row(Html.cell("Nama Kelompok Tani", Html.CellOpts("38%", Html.RED_FILL, bold = true, color = Html.RED)) + Html.cell(m.petugas.namaKelompok)) +
                Html.row(Html.cell("Kode Kelompok Tani", Html.CellOpts("38%", Html.RED_FILL, bold = true, color = Html.RED)) + Html.cell(m.petugas.kodeKelompok)),
        ))
        sb.append("</div>")

        // ---------- HALAMAN 2: B (landscape) ----------
        sb.append("<div class=\"landscape pagebreak\">")
        sb.append(Html.sectionTitle("B.1 - DATA FISIK LOKASI PLOT"))
        if (m.sectionB1.isEmpty()) {
            sb.append("<div>Belum ada data plot.</div>")
        } else {
            sb.append(Html.table(
                Html.row(
                    Html.head("Plot No.", "6%") + Html.head("Nama/ Hamparan", "16%") + Html.head("Varietas Kopi", "16%") +
                        Html.head("Tahun Tanam", "10%") + Html.head("Kode GPS", "14%") + Html.head("Elevasi (mdpl)", "12%") +
                        Html.head("Kemiringan (%)", "12%") + Html.head("Luas Kopi (Ha)", "14%"),
                ) + m.sectionB1.joinToString("") { r ->
                    Html.row(
                        Html.cell(r[0], Html.CellOpts("6%", center = true)) + Html.cell(r[1], Html.CellOpts("16%")) +
                            Html.cell(r[2], Html.CellOpts("16%")) + Html.cell(r[3], Html.CellOpts("10%", center = true)) +
                            Html.cell(r[4], Html.CellOpts("14%")) + Html.cell(r[5], Html.CellOpts("12%", center = true)) +
                            Html.cell(r[6], Html.CellOpts("12%", center = true)) + Html.cell(r[7], Html.CellOpts("14%", center = true)),
                    )
                },
            ))
        }

        if (m.sectionB1b.isNotEmpty()) {
            sb.append(Html.sectionTitle("B.1 - DATA FISIK LOKASI PLOT (lanjutan)"))
            sb.append(Html.table(
                Html.row(
                    Html.head("Plot No.", "5%") + Html.head("Foto Geotagged", "13%") + Html.head("Status Kepemilikan Lahan", "12%") +
                        Html.head("Sistem Budidaya", "10%") + Html.head("Area Konservasi", "18%") + Html.head("Tanaman Baru", "11%") +
                        Html.head("Pohon Produktif", "11%") + Html.head("Pohon Tdk Produktif", "11%") + Html.head("Pestisida Terakhir", "9%"),
                ) + m.sectionB1b.joinToString("") { r ->
                    Html.row(
                        Html.cell(r.no, Html.CellOpts("5%", center = true)) +
                            fotoCell(r.fotoUrl) +
                            Html.cell(r.status, Html.CellOpts("12%", center = true)) + Html.cell(r.sistem, Html.CellOpts("10%", center = true)) +
                            Html.cell(r.konservasi, Html.CellOpts("18%")) + Html.cell(r.tanamanBaru, Html.CellOpts("11%", center = true)) +
                            Html.cell(r.pohonProduktif, Html.CellOpts("11%", center = true)) + Html.cell(r.pohonTidakProduktif, Html.CellOpts("11%", center = true)) +
                            Html.cell(r.pestisida, Html.CellOpts("9%")),
                    )
                },
            ))
            sb.append("<div class=\"legend\">Kode Kepemilikan: MS = Milik Sendiri  |  SW = Sewa  |  BH = Bagi Hasil  |  TA = Tanah Adat  |  L = Lainnya    Kode Sistem Budidaya: AF = Agroforestry  |  MK = Monokultur</div>")
        }

        sb.append(Html.sectionTitle("B.2 - DATA TANAMAN NAUNGAN / SELA / TEGAKAN (TINGKAT PETANI)"))
        if (m.sectionB2.isEmpty()) {
            sb.append("<div>Tidak ada Tanaman Naungan, Sela, Tegakan.</div>")
        } else {
            sb.append(Html.table(
                Html.row(
                    Html.head("No.", "6%") + Html.head("Jenis Naungan/Sela/Tegakan", "20%") + Html.head("Jumlah", "9%") +
                        Html.head("Fungsi", "20%") + Html.head("Apakah dilakukan pemangkasan", "15%") +
                        Html.head("Produksi/Tahun", "18%") + Html.head("Tahun Tanam", "12%"),
                ) + m.sectionB2.joinToString("") { r ->
                    Html.row(
                        Html.cell(r[0], Html.CellOpts("6%", center = true)) + Html.cell(r[1], Html.CellOpts("20%")) +
                            Html.cell(r[2], Html.CellOpts("9%", center = true)) + Html.cell(r[3], Html.CellOpts("20%")) +
                            Html.cell(r[4], Html.CellOpts("15%", center = true)) + Html.cell(r[5], Html.CellOpts("18%")) +
                            Html.cell(r[6], Html.CellOpts("12%", center = true)),
                    )
                },
            ))
        }
        sb.append("</div>")

        // ---------- HALAMAN 3: C (portrait) ----------
        // Satu tabel untuk seluruh bagian C agar kolom header dan data benar-benar
        // sejajar (padanan react-pdf: label 16% + sub-tabel 84% dengan kolom
        // 41.7/8.4/9.5/10.7/29.7% yang setara penuh 35/7/8/9/25%).
        sb.append("<div class=\"pagebreak\">")
        sb.append(Html.sectionTitle("C - PRAKTIK GAP KEBUN"))
        val cRows = StringBuilder()
        cRows.append(
            Html.row(
                Html.head(" ", "13%") + Html.head("Jenis Praktik", "30%") + Html.head("Ya", "6%") +
                    Html.head("Tidak", "6%") + Html.head("Kadang", "7%") + Html.head("Keterangan", "38%"),
            ),
        )
        m.sectionC.forEach { g ->
            // Sel label di-merge vertikal (rowspan) untuk tiap kelompok.
            g.rows.forEachIndexed { i, r ->
                val labelCell = if (i == 0) {
                    "<td rowspan=\"${g.rows.size}\" style=\"width:13%;" +
                        "border-right:0.8pt solid ${Html.BORDER};border-bottom:0.8pt solid ${Html.BORDER};" +
                        "vertical-align:middle;padding:6pt 7pt;font-weight:bold;\">${Html.esc(g.kelompok)}</td>"
                } else {
                    ""
                }
                cRows.append(
                    Html.row(
                        labelCell +
                            Html.cell(r.label, Html.CellOpts("30%")) +
                            Html.tick(r.jawaban == "YA", "6%") +
                            Html.tick(r.jawaban == "TIDAK", "6%") +
                            Html.tick(r.jawaban == "KADANG", "7%") +
                            Html.cell(r.keterangan, Html.CellOpts("38%")),
                    ),
                )
            }
        }
        sb.append(Html.table(cRows.toString()))
        sb.append("</div>")

        // ---------- HALAMAN 4: D + E (portrait) ----------
        sb.append("<div class=\"pagebreak\">")
        sb.append(Html.sectionTitle("D - RIWAYAT ESTIMASI PRODUKSI"))
        sb.append(tabelD(m))
        sb.append(Html.sectionTitle("E.1 - JENIS PRODUK YANG DIJUAL"))
        sb.append(Html.table(
            Html.row(Html.head("Jenis Produk", "55%") + Html.head("Ya", "10%") + Html.head("Tidak", "10%") + Html.head("Volume (Kg/Tahun)", "25%")) +
                m.sectionE1.joinToString("") { r ->
                    Html.row(
                        Html.cell(r.label, Html.CellOpts("55%")) + Html.tick(r.aktif, "10%") +
                            Html.tick(!r.aktif, "10%") + Html.cell(r.volume, Html.CellOpts("25%", center = true)),
                    )
                },
        ))
        sb.append(Html.sectionTitle("E.2 - KATEGORI PASAR"))
        sb.append(Html.table(
            Html.row(
                Html.head("Kategori Pasar", "40%") + Html.head("Ya", "10%") + Html.head("Tidak", "10%") +
                    Html.head("Persentase (%)", "12%") + Html.head("Profil Penjual (Pengepul, Pabrik, Ekspor, dll.)", "28%"),
            ) + m.sectionE2.joinToString("") { r ->
                Html.row(
                    Html.cell(r.label, Html.CellOpts("40%")) + Html.tick(r.aktif, "10%") + Html.tick(!r.aktif, "10%") +
                        Html.cell(r.persentase, Html.CellOpts("12%", center = true)) + Html.cell(r.profil, Html.CellOpts("28%")),
                )
            },
        ))
        sb.append("</div>")

        // ---------- HALAMAN 5: F (portrait) ----------
        sb.append("<div class=\"pagebreak\">")
        sb.append(Html.sectionTitle("F - KONDISI KEBUN"))
        sb.append(Html.table(
            Html.row(Html.head("Kondisi Kebun", "46%") + Html.head("Ya", "9%") + Html.head("Tidak", "9%") + Html.head("Keterangan", "36%")) +
                m.sectionF.joinToString("") { r ->
                    Html.row(
                        Html.cell(r.label, Html.CellOpts("46%")) + Html.tick(r.jawaban, "9%") + Html.tick(!r.jawaban, "9%") +
                            Html.cell(r.keterangan, Html.CellOpts("36%")),
                    )
                },
        ))
        sb.append("</div>")

        sb.append("</body></html>")
        return sb.toString()
    }

    private fun tabelD(m: PetaniExportModel): String = Html.table(
        Html.row(
            Html.head("Tahun", "12%") + Html.head("Satuan (Kg, Solup, Bambu, Kaleng)", "22%") + Html.head("Cherry", "11%") +
                Html.head("Gabah Basah / Labu", "13%") + Html.head("Gabah Kering", "12%") +
                Html.head("Green Bean", "12%") + Html.head("Produktivitas", "18%"),
        ) + m.sectionD.joinToString("") { r ->
            Html.row(
                Html.cell(r[0], Html.CellOpts("12%", bold = true)) + Html.cell(r[1], Html.CellOpts("22%", center = true)) +
                    Html.cell(r[2], Html.CellOpts("11%", center = true)) + Html.cell(r[3], Html.CellOpts("13%", center = true)) +
                    Html.cell(r[4], Html.CellOpts("12%", center = true)) + Html.cell(r[5], Html.CellOpts("12%", center = true)) +
                    Html.cell(r[6], Html.CellOpts("18%", center = true)),
            )
        },
    )

    private fun fotoCell(url: String?): String {
        val content = if (url != null) {
            "<a href=\"${Html.esc(url)}\" style=\"color:#1d4ed8;text-decoration:underline;\">Lihat foto</a>"
        } else "-"
        return "<td style=\"padding:6pt 7pt;border-right:0.8pt solid #999999;border-bottom:0.8pt solid #999999;" +
            "vertical-align:middle;text-align:center;width:13%;\">$content</td>"
    }
}

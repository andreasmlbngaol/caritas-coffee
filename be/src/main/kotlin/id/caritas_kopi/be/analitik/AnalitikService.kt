package id.caritas_kopi.be.analitik

import id.caritas_kopi.be.desa.BaselineDesaRepository
import id.caritas_kopi.be.desa.KebijakanDesaRepository
import id.caritas_kopi.be.desa.KelembagaanDesaRepository
import id.caritas_kopi.be.kelompoktani.KelompokTaniRepository
import id.caritas_kopi.be.petani.KondisiKebunRepository
import id.caritas_kopi.be.petani.PasarPetaniRepository
import id.caritas_kopi.be.petani.PetaniRepository
import id.caritas_kopi.be.petani.PlotPetaniRepository
import id.caritas_kopi.be.petani.PraktikGapRepository
import id.caritas_kopi.be.petani.ProdukDijualRepository
import id.caritas_kopi.be.petani.RiwayatProduksiRepository
import id.caritas_kopi.be.petani.TanamanNaunganRepository
import id.caritas_kopi.be.wilayah.WilayahLookupService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.Period
import java.util.UUID

/**
 * Agregasi analitik (khusus ADMIN) - padanan `analitik/queries.ts`.
 * Logika grouping teks bebas case-insensitive + pickSpelling + clean dipertahankan persis.
 *
 * ponytail: agregasi memuat tabel penuh ke memori (findAll) karena grouping teks
 * bebas butuh pickSpelling/clean yang tidak bisa diekspresikan di SQL. Aman untuk
 * skala PKL (ribuan baris); kalau data membesar, pindahkan grouping ke SQL + index.
 */
@Service
class AnalitikService(
    private val petaniRepo: PetaniRepository,
    private val plotRepo: PlotPetaniRepository,
    private val gapRepo: PraktikGapRepository,
    private val produksiRepo: RiwayatProduksiRepository,
    private val produkRepo: ProdukDijualRepository,
    private val pasarRepo: PasarPetaniRepository,
    private val kondisiRepo: KondisiKebunRepository,
    private val naunganRepo: TanamanNaunganRepository,
    private val baselineRepo: BaselineDesaRepository,
    private val kebijakanRepo: KebijakanDesaRepository,
    private val kelembagaanRepo: KelembagaanDesaRepository,
    private val kelompokRepo: KelompokTaniRepository,
    private val wilayah: WilayahLookupService,
) {
    private val dash = "-"

    private fun num(v: Number?): Double = v?.toDouble() ?: 0.0

    /** Buang kosong & placeholder "-" agar tidak ikut terhitung. */
    private fun clean(v: String?): String? {
        val t = v?.trim()
        return if (!t.isNullOrEmpty() && t != dash) t else null
    }

    /** Kunci grouping case-insensitive. */
    private fun ci(s: String): String = s.lowercase()

    /** Ejaan tampilan: paling sering; seri -> yang punya huruf kapital. */
    private fun pickSpelling(variants: Map<String, Int>): String {
        var best = ""
        var bestN = -1
        for ((s, n) in variants) {
            val better = n > bestN || (n == bestN && s.any { it.isUpperCase() } && best.none { it.isUpperCase() })
            if (better) {
                best = s
                bestN = n
            }
        }
        return best
    }

    private class Acc {
        var jumlah = 0
        val spell = LinkedHashMap<String, Int>()
    }

    private fun <T> tally(rows: List<T>, get: (T) -> String?): List<Frekuensi> {
        val m = LinkedHashMap<String, Acc>()
        for (row in rows) {
            val v = clean(get(row)) ?: continue
            val e = m.getOrPut(ci(v)) { Acc() }
            e.jumlah += 1
            e.spell.merge(v, 1, Int::plus)
        }
        return m.values.map { Frekuensi(pickSpelling(it.spell), it.jumlah) }
    }

    // ---------- Ringkasan dashboard ----------
    @Transactional(readOnly = true)
    fun getRingkasan(): RingkasanDto {
        val jumlahPetani = petaniRepo.count()
        val jumlahDesa = baselineRepo.count()
        val jumlahKelompok = kelompokRepo.count()
        val luasArealKopiHa = baselineRepo.findAll().sumOf { num(it.luasArealKopiHa) }
        val plots = plotRepo.findAll()
        val luasPlotHa = plots.sumOf { num(it.luasKopiHa) }
        val pohonProduktif = plots.sumOf { num(it.pohonProduktif) }

        val produksi = produksiRepo.findAll()
        val perTahun = produksi.groupBy { it.tahun }.toSortedMap()
        val tren = perTahun.map { (tahun, rows) ->
            TrenProduksi(
                tahun = tahun.toString(),
                cherry = rows.sumOf { num(it.cherry) },
                greenBean = rows.sumOf { num(it.greenBean) },
                gabahBasah = rows.sumOf { num(it.gabahBasah) },
                gabahKering = rows.sumOf { num(it.gabahKering) },
            )
        }
        val terbaru = tren.lastOrNull()
        val volumeTerbaru = terbaru?.let { it.cherry + it.gabahBasah + it.gabahKering + it.greenBean } ?: 0.0

        return RingkasanDto(
            jumlahPetani = jumlahPetani,
            jumlahDesa = jumlahDesa,
            jumlahKelompok = jumlahKelompok,
            luasArealKopiHa = luasArealKopiHa,
            luasPlotHa = luasPlotHa,
            pohonProduktif = pohonProduktif,
            tahunTerbaru = terbaru?.tahun?.toIntOrNull(),
            produktivitasRata = if (luasPlotHa > 0) volumeTerbaru / luasPlotHa else 0.0,
            trenProduksi = tren,
        )
    }

    // ---------- GAP: tingkat adopsi 21 praktik ----------
    @Transactional(readOnly = true)
    fun getGapAdoption(): GapAdoptionDto {
        val totalPetani = petaniRepo.count()
        val rows = gapRepo.findAll()
        val map = LinkedHashMap<String, LongArray>() // [ya, tidak, kadang]
        for (r in rows) {
            val e = map.getOrPut(r.jenis.name) { LongArray(3) }
            when (r.jawaban?.name) {
                "YA" -> e[0]++
                "TIDAK" -> e[1]++
                "KADANG" -> e[2]++
            }
        }
        val items = map.map { (jenis, e) ->
            val total = e.sum()
            GapItemStat(jenis, e[0], e[1], e[2], total, if (total > 0) e[0].toDouble() / total * 100 else 0.0)
        }
        val totalJawaban = items.sumOf { it.total }
        val totalYa = items.sumOf { it.ya }
        return GapAdoptionDto(
            totalPetani = totalPetani,
            items = items,
            adopsiKeseluruhan = if (totalJawaban > 0) totalYa.toDouble() / totalJawaban * 100 else 0.0,
        )
    }

    // ---------- Produksi ----------
    @Transactional(readOnly = true)
    fun getProduksi(): ProduksiDto {
        val allRiwayat = produksiRepo.findAll()
        val byTahun = allRiwayat.groupBy { it.tahun }.toSortedMap()

        val petani = petaniRepo.findAll()
        val petaniById = petani.associateBy { it.id!! }
        val desaMap = wilayah.desaMap(petani.map { it.desaKode })

        // Luas kopi per petani (produktivitas nyata kg/ha).
        val areaOf = plotRepo.findAll()
            .groupBy { it.petaniId }
            .mapValues { (_, rows) -> rows.sumOf { num(it.luasKopiHa) } }

        val perTahun = LinkedHashMap<Int, Pair<Double, Double>>() // volume to area
        for (r in allRiwayat) {
            val cur = perTahun[r.tahun] ?: (0.0 to 0.0)
            val volume = cur.first + num(r.cherry) + num(r.gabahBasah) + num(r.gabahKering) + num(r.greenBean)
            val area = cur.second + (areaOf[r.petaniId] ?: 0.0)
            perTahun[r.tahun] = volume to area
        }
        fun produktivitas(tahun: Int): Double {
            val e = perTahun[tahun] ?: return 0.0
            return if (e.second > 0) e.first / e.second else 0.0
        }

        val tahunTerbaru = byTahun.keys.lastOrNull()
        val perDesa = LinkedHashMap<String, TopDesa>()
        for (r in allRiwayat) {
            if (r.tahun != tahunTerbaru) continue
            val nama = petaniById[r.petaniId]?.desaKode?.let { desaMap[it]?.nama } ?: "-"
            val e = perDesa.getOrPut(nama) { TopDesa(nama, 0.0, 0.0, 0.0, 0.0, 0.0) }
            perDesa[nama] = e.copy(
                cherry = e.cherry + num(r.cherry),
                gabahBasah = e.gabahBasah + num(r.gabahBasah),
                gabahKering = e.gabahKering + num(r.gabahKering),
                greenBean = e.greenBean + num(r.greenBean),
            )
        }
        val topDesa = perDesa.values
            .map { it.copy(total = it.cherry + it.gabahBasah + it.gabahKering + it.greenBean) }
            .sortedByDescending { it.total }
            .take(7)

        return ProduksiDto(
            tahunTerbaru = tahunTerbaru,
            byTahun = byTahun.map { (tahun, rows) ->
                ProduksiTahun(
                    tahun = tahun.toString(),
                    cherry = rows.sumOf { num(it.cherry) },
                    gabahBasah = rows.sumOf { num(it.gabahBasah) },
                    gabahKering = rows.sumOf { num(it.gabahKering) },
                    greenBean = rows.sumOf { num(it.greenBean) },
                    produktivitasRata = produktivitas(tahun),
                    jumlahPetani = rows.size.toLong(),
                )
            },
            topDesa = topDesa,
        )
    }

    // ---------- Pasar & Produk ----------
    @Transactional(readOnly = true)
    fun getPasarProduk(): PasarProdukDto {
        val totalPetani = petaniRepo.count()
        val allProduk = produkRepo.findAll()
        val produk = allProduk.filter { it.dijual }
        val pasar = pasarRepo.findByAktifTrue()
        val volumeByJenis = allProduk.groupBy { it.jenis.name }
            .map { (jenis, rows) -> VolumeJenis(jenis, rows.sumOf { num(it.volumeKgTahun) }) }

        class ProdukAcc {
            val spell = LinkedHashMap<String, Int>()
            var label = ""
            var jenis = ""
            var volume = 0.0
            val petani = HashSet<UUID>()
            var custom = false
        }
        val produkMap = LinkedHashMap<String, ProdukAcc>()
        for (p in produk) {
            val custom = p.jenis.name == "LAINNYA"
            val raw = if (custom) clean(p.labelCustom) else null
            val label = if (custom) raw ?: "Lainnya (tanpa nama)" else p.jenis.name
            val key = if (custom) "L:${ci(label)}" else p.jenis.name
            val e = produkMap.getOrPut(key) { ProdukAcc().apply { this.label = label; jenis = p.jenis.name; this.custom = custom } }
            e.spell.merge(label, 1, Int::plus)
            e.volume += num(p.volumeKgTahun)
            e.petani.add(p.petaniId)
        }

        class PasarAcc {
            val spell = LinkedHashMap<String, Int>()
            var label = ""
            var kategori = ""
            val petani = HashSet<UUID>()
            var totalPersen = 0.0
            var persenN = 0
            val profil = LinkedHashMap<String, String>()
            var custom = false
        }
        val pasarMap = LinkedHashMap<String, PasarAcc>()
        for (p in pasar) {
            val custom = p.kategori.name == "LAINNYA"
            val raw = if (custom) clean(p.labelCustom) else null
            val label = if (custom) raw ?: "Lainnya (tanpa nama)" else p.kategori.name
            val key = if (custom) "L:${ci(label)}" else p.kategori.name
            val e = pasarMap.getOrPut(key) {
                PasarAcc().apply { this.label = label; kategori = p.kategori.name; this.custom = custom }
            }
            e.spell.merge(label, 1, Int::plus)
            e.petani.add(p.petaniId)
            val persen = p.persentase
            if (persen != null) { e.totalPersen += persen; e.persenN += 1 }
            val prof = clean(p.profilPenjual)
            if (prof != null) e.profil[ci(prof)] = prof
        }

        return PasarProdukDto(
            totalPetani = totalPetani,
            produk = produkMap.values.map { e ->
                ProdukStat(
                    label = pickSpelling(e.spell), jenis = e.jenis, volume = e.volume, petani = e.petani.size.toLong(),
                    custom = e.custom, rataVolume = if (e.petani.isNotEmpty()) e.volume / e.petani.size else 0.0,
                )
            }.sortedByDescending { it.volume },
            pasar = pasarMap.values.map { e ->
                PasarStat(
                    label = pickSpelling(e.spell), kategori = e.kategori, petani = e.petani.size.toLong(), custom = e.custom,
                    rataPersen = if (e.persenN > 0) e.totalPersen / e.persenN else 0.0,
                    profil = e.profil.values.toList(),
                )
            }.sortedByDescending { it.petani },
            volumeByJenis = volumeByJenis,
        )
    }

    // ---------- Konservasi & Kondisi Kebun ----------
    @Transactional(readOnly = true)
    fun getKonservasi(): KonservasiDto {
        val totalPetani = petaniRepo.count()
        val kondisi = kondisiRepo.findByJawabanTrue().groupBy { it.jenis.name }
            .map { (jenis, rows) ->
                KondisiStat(jenis, rows.size.toLong(), if (totalPetani > 0) rows.size.toDouble() / totalPetani * 100 else 0.0)
            }
        val desaRows = baselineRepo.findAll()

        val praktik = tally(desaRows) { it.praktikKonservasi }.sortedByDescending { it.jumlah }.take(10)
        val satwa = tally(desaRows) { it.jenisSatwaKonflik }.sortedByDescending { it.jumlah }.take(10)

        val kawasanSpell = LinkedHashMap<String, LinkedHashMap<String, Int>>()
        for (d in desaRows) {
            val t = clean(d.namaKawasanKonservasi) ?: continue
            kawasanSpell.getOrPut(ci(t)) { LinkedHashMap() }.merge(t, 1, Int::plus)
        }

        return KonservasiDto(
            totalPetani = totalPetani,
            kondisi = kondisi,
            jumlahDesa = desaRows.size.toLong(),
            luasPenyanggaHa = desaRows.sumOf { num(it.luasPenyanggaHa) },
            luasAPLHa = desaRows.sumOf { num(it.luasAPLHa) },
            jarakKonservasiRata = desaRows.mapNotNull { it.jarakKonservasiKm }.average().let { if (it.isNaN()) 0.0 else it },
            rawanLongsor = desaRows.count { it.rawanLongsor == true },
            rawanErosi = desaRows.count { it.rawanErosi == true },
            konflikSatwa = desaRows.count { it.konflikSatwa == true },
            berbatasan = desaRows.count { it.berbatasanKonservasi == true },
            tutupanHutan = desaRows.sumOf { num(it.tutupanHutan) },
            tutupanAgroforestry = desaRows.sumOf { num(it.tutupanAgroforestry) },
            praktik = praktik,
            satwa = satwa,
            kawasan = kawasanSpell.values.map { pickSpelling(it) },
        )
    }

    // ---------- Wilayah & Kelembagaan ----------
    @Transactional(readOnly = true)
    fun getWilayah(): WilayahAnalitikDto {
        val petani = petaniRepo.findAll()
        val perDesa = petani.groupBy { it.desaKode }
        val desaKodeSet = perDesa.keys
        val desaMap = wilayah.desaMap(desaKodeSet)
        val kecMap = wilayah.kecamatanMap(desaMap.values.map { it.kecamatanKode })

        val topDesa = perDesa.map { (kode, rows) ->
            TopDesaPetani(
                nama = desaMap[kode]?.nama ?: kode,
                kecamatan = desaMap[kode]?.let { kecMap[it.kecamatanKode]?.nama } ?: "-",
                petani = rows.size.toLong(),
            )
        }.sortedByDescending { it.petani }.take(10)

        var l = 0L; var p = 0L; var kosong = 0L
        for ((jk, rows) in petani.groupBy { it.jenisKelamin?.name }) {
            when (jk) {
                "L" -> l += rows.size
                "P" -> p += rows.size
                else -> kosong += rows.size
            }
        }

        val desaList = baselineRepo.findAll()
        val now = LocalDate.now()
        val buckets = listOf("< 30" to (0..29), "30-39" to (30..39), "40-49" to (40..49), "50-59" to (50..59), "≥ 60" to (60..200))
        val usia = buckets.map { UsiaStat(it.first, 0) }.toMutableList()
        for (pt in petani) {
            val tgl = pt.tanggalLahir ?: continue
            val age = Period.between(tgl, now).years
            val idx = buckets.indexOfFirst { age in it.second }
            if (idx >= 0) usia[idx] = usia[idx].copy(jumlah = usia[idx].jumlah + 1)
        }

        fun freq(get: (id.caritas_kopi.be.desa.BaselineDesa) -> String?): List<Frekuensi> =
            tally(desaList, get).sortedByDescending { it.jumlah }.take(8)

        fun avg(get: (id.caritas_kopi.be.desa.BaselineDesa) -> Double?): Double {
            val vals = desaList.mapNotNull(get)
            return if (vals.isEmpty()) 0.0 else vals.average()
        }

        val lembaga = kelembagaanRepo.findAll().groupBy { it.jenis.name }
            .map { (jenis, rows) -> LembagaStat(jenis, rows.sumOf { num(it.jumlah) }, rows.size.toLong()) }
            .sortedByDescending { it.jumlah }
        val kebijakan = kebijakanRepo.findAll().filter { it.ada }.groupBy { it.jenis.name }
            .map { (jenis, rows) -> KebijakanStat(jenis, rows.size.toLong()) }

        return WilayahAnalitikDto(
            topDesa = topDesa,
            gender = GenderStat(l, p, kosong),
            usia = usia,
            totalPenduduk = desaList.sumOf { num(it.jumlahPenduduk) },
            totalKK = desaList.sumOf { num(it.jumlahKK) },
            totalArealKopi = desaList.sumOf { num(it.luasArealKopiHa) },
            totalPetaniKopi = desaList.sumOf { num(it.jumlahPetaniKopi) },
            rataKetinggian = avg { it.ketinggianMdpl },
            rataSuhu = avg { it.suhuRataRataC },
            jumlahDesa = desaList.size,
            topografi = freq { it.topografi },
            jenisTanah = freq { it.jenisTanah },
            aksesJalan = freq { it.aksesJalan },
            pembeliUtama = freq { it.pembeliUtama },
            eksportir = freq { it.eksportir },
            industri = freq { it.industriPengolahan },
            permasalahan = freq { it.permasalahanUtama },
            lembaga = lembaga,
            kebijakan = kebijakan,
        )
    }

    // ---------- Agronomi Plot ----------
    @Transactional(readOnly = true)
    fun getAgronomi(): AgronomiDto {
        val totalPetani = petaniRepo.count()
        val plots = plotRepo.findAll()
        val shadeRows = naunganRepo.findAll()

        val varietasSpell = LinkedHashMap<String, Acc>()
        for (pl in plots) {
            for (raw in pl.varietas?.split(",") ?: emptyList()) {
                val v = clean(raw) ?: continue
                val e = varietasSpell.getOrPut(ci(v)) { Acc() }
                e.jumlah += 1
                e.spell.merge(v, 1, Int::plus)
            }
        }
        val varietas = varietasSpell.values
            .map { Frekuensi(pickSpelling(it.spell), it.jumlah) }
            .sortedByDescending { it.jumlah }.take(12)

        class NaunganAcc {
            val spell = LinkedHashMap<String, Int>()
            val petani = HashSet<UUID>()
            var pohon = 0L
            var dipangkas = 0
        }
        val naunganMap = LinkedHashMap<String, NaunganAcc>()
        for (n in shadeRows) {
            val v = clean(n.jenis) ?: "(tanpa nama)"
            val e = naunganMap.getOrPut(ci(v)) { NaunganAcc() }
            e.spell.merge(v, 1, Int::plus)
            e.petani.add(n.petaniId)
            e.pohon += (n.jumlah ?: 0).toLong()
            if (n.pemangkasan == true) e.dipangkas += 1
        }
        val naungan = naunganMap.values
            .map { NaunganStat(pickSpelling(it.spell), it.petani.size, it.pohon, it.dipangkas) }
            .sortedByDescending { it.petani }

        val pestisida = tally(plots) { it.pestisidaNama }.sortedByDescending { it.jumlah }.take(12)

        val nowYear = now().year
        val umurMap = LinkedHashMap<String, Int>()
        var totalPohonProduktif = 0L
        var totalPohonTidakProduktif = 0L
        var totalTanamanBaru = 0L
        var totalLuas = 0.0
        for (pl in plots) {
            totalPohonProduktif += (pl.pohonProduktif ?: 0).toLong()
            totalPohonTidakProduktif += (pl.pohonTidakProduktif ?: 0).toLong()
            totalTanamanBaru += (pl.tanamanBaru ?: 0).toLong()
            totalLuas += num(pl.luasKopiHa)
            for (tahun in pl.tahunTanam) {
                if (tahun < 1900 || tahun > nowYear) continue
                val umur = nowYear - tahun
                val bucket = when {
                    umur < 3 -> "< 3 th"
                    umur < 7 -> "3-6 th"
                    umur < 15 -> "7-14 th"
                    else -> "≥ 15 th"
                }
                umurMap.merge(bucket, 1, Int::plus)
            }
        }
        val umur = listOf("< 3 th", "3-6 th", "7-14 th", "≥ 15 th")
            .map { UmurStat(it, umurMap[it] ?: 0) }

        return AgronomiDto(
            totalPetani = totalPetani,
            jumlahPlot = plots.size,
            totalLuas = totalLuas,
            totalPohonProduktif = totalPohonProduktif,
            totalPohonTidakProduktif = totalPohonTidakProduktif,
            totalTanamanBaru = totalTanamanBaru,
            rataProduktif = if (plots.isNotEmpty()) totalPohonProduktif.toDouble() / plots.size else 0.0,
            varietas = varietas,
            sistemBudidaya = tally(plots) { it.sistemBudidaya?.name }.sortedByDescending { it.jumlah },
            kepemilikan = tally(plots) { it.statusKepemilikan?.name }.sortedByDescending { it.jumlah },
            areaKonservasi = tally(plots) { it.areaKonservasi }.sortedByDescending { it.jumlah },
            naungan = naungan,
            pestisida = pestisida,
            umur = umur,
        )
    }

    // ---------- Lokasi (peta) ----------
    @Transactional(readOnly = true)
    fun getLokasi(): LokasiDto {
        val baselines = baselineRepo.findAll().filter { it.latitude != null && it.longitude != null }
        val desaMap = wilayah.desaMap(baselines.map { it.desaKode })
        val kecMap = wilayah.kecamatanMap(desaMap.values.map { it.kecamatanKode })
        val kabMap = wilayah.kabupatenMap(kecMap.values.map { it.kabupatenKode })

        val desa = baselines.map { d ->
            val desaRow = desaMap[d.desaKode]
            val kec = desaRow?.let { kecMap[it.kecamatanKode] }
            val kab = kec?.let { kabMap[it.kabupatenKode] }
            LokasiDesa(
                lat = d.latitude!!, lng = d.longitude!!,
                nama = desaRow?.nama ?: "-", kecamatan = kec?.nama ?: "-", kabupaten = kab?.nama ?: "-",
                luasArealKopiHa = num(d.luasArealKopiHa), petaniKopi = num(d.jumlahPetaniKopi),
                penduduk = num(d.jumlahPenduduk), ketinggian = d.ketinggianMdpl,
            )
        }

        val petani = petaniRepo.findAll().associateBy { it.id!! }
        val plotDesaMap = wilayah.desaMap(petani.values.map { it.desaKode })
        val plots = plotRepo.findAll().filter { it.fotoLatitude != null && it.fotoLongitude != null }
        val plot = plots.map { pl ->
            val pt = petani[pl.petaniId]
            LokasiPlot(
                lat = pl.fotoLatitude!!, lng = pl.fotoLongitude!!,
                luasKopiHa = num(pl.luasKopiHa), varietas = pl.varietas, hamparan = pl.namaHamparan,
                petani = pt?.namaLengkap ?: "-",
                desa = pt?.desaKode?.let { plotDesaMap[it]?.nama } ?: "-",
            )
        }

        return LokasiDto(desa = desa, plot = plot)
    }

    private fun now(): LocalDate = LocalDate.now()
}

package id.caritas_kopi.be.export

import id.caritas_kopi.be.petani.PetaniDetailDto
import id.caritas_kopi.be.petani.JenisKelamin

/** Struktur presentasi-agnostik, dipakai bersama renderer PDF & DOCX. */
data class PetaniExportModel(
    val fileName: String,
    val penginput: String,
    val tanggalInput: String,
    val sectionA: List<Pair<String, String>>,
    val petugas: Petugas,
    val sectionB1: List<List<String>>,
    val sectionB1b: List<PlotLanjutan>,
    val sectionB2: List<List<String>>,
    val sectionC: List<GapGroupRows>,
    val sectionD: List<List<String>>,
    val sectionE1: List<ProdukRow>,
    val sectionE2: List<PasarRow>,
    val sectionF: List<KondisiRow>,
) {
    data class Petugas(val kodePetani: String, val namaKelompok: String, val kodeKelompok: String)
    data class PlotLanjutan(
        val no: String,
        val fotoUrl: String?,
        val status: String,
        val sistem: String,
        val konservasi: String,
        val tanamanBaru: String,
        val pohonProduktif: String,
        val pohonTidakProduktif: String,
        val pestisida: String,
    )
    data class GapRow(val label: String, val jawaban: String?, val keterangan: String)
    data class GapGroupRows(val kelompok: String, val rows: List<GapRow>)
    data class ProdukRow(val label: String, val aktif: Boolean, val volume: String)
    data class PasarRow(val label: String, val aktif: Boolean, val persentase: String, val profil: String)
    data class KondisiRow(val label: String, val jawaban: Boolean, val keterangan: String)
}

fun buildPetaniExportModel(p: PetaniDetailDto, appUrl: String): PetaniExportModel {
    val S = ExportSupport

    val jenisKelamin = when (p.jenisKelamin) {
        JenisKelamin.L -> "Laki-laki"
        JenisKelamin.P -> "Perempuan"
        null -> "-"
    }

    val sectionA = listOf(
        "Nama Lengkap (sesuai KTP)" to p.namaLengkap,
        "Nama Panggilan" to S.fmt(p.namaPanggilan),
        "Jenis Kelamin" to jenisKelamin,
        "Tanggal Lahir" to S.fmtDate(p.tanggalLahir),
        "Alamat Domisili" to S.fmt(p.alamatDomisili),
        "Nomor Telepon / HP" to S.fmt(p.telepon),
        "Tanggal Pendaftaran" to S.fmtDate(p.tanggalPendaftaran),
        "Nama Petugas Pendaftar" to S.fmt(p.namaPetugasPendaftar),
        "Nama Kontak Darurat" to S.fmt(p.kontakDaruratNama),
        "Nomor Telepon / HP Kontak Darurat" to S.fmt(p.kontakDaruratTelepon),
        "Hubungan dengan Kontak Darurat" to S.fmt(p.kontakDaruratHubungan),
        "Desa" to "${p.desa.nama}, ${p.desa.kecamatan}, ${p.desa.kabupaten}, ${p.desa.provinsi}",
    )

    val petugas = PetaniExportModel.Petugas(
        kodePetani = S.fmt(p.kodePetani),
        namaKelompok = p.kelompokTani?.nama ?: "-",
        kodeKelompok = p.kelompokTani?.kode ?: "-",
    )

    val sectionB1 = p.plot.map { pl ->
        listOf(
            pl.nomor.toString(),
            S.fmt(pl.namaHamparan),
            S.fmt(pl.varietas),
            if (pl.tahunTanam.isEmpty()) "-" else pl.tahunTanam.joinToString(", "),
            if (pl.fotoLatitude != null && pl.fotoLongitude != null) {
                String.format(java.util.Locale.US, "%.6f, %.6f", pl.fotoLatitude, pl.fotoLongitude)
            } else "-",
            S.num(pl.elevasiMdpl),
            S.num(pl.kemiringanPersen),
            S.num(pl.luasKopiHa),
        )
    }

    val sectionB1b = p.plot.map { pl ->
        PetaniExportModel.PlotLanjutan(
            no = pl.nomor.toString(),
            fotoUrl = pl.fotoKey?.let { "$appUrl/api/foto/$it" },
            status = S.kode(pl.statusKepemilikan?.name),
            sistem = S.kode(pl.sistemBudidaya?.name),
            konservasi = S.fmt(pl.areaKonservasi),
            tanamanBaru = S.num(pl.tanamanBaru),
            pohonProduktif = S.num(pl.pohonProduktif),
            pohonTidakProduktif = S.num(pl.pohonTidakProduktif),
            pestisida = listOfNotNull(
                pl.pestisidaNama,
                pl.pestisidaBulanTahun?.let { S.fmtBulanTahun(it) },
            ).filter { it.isNotEmpty() }.joinToString(" - ").ifEmpty { "-" },
        )
    }

    val sectionB2 = p.naungan.mapIndexed { i, n ->
        listOf(
            (i + 1).toString(),
            S.fmt(n.jenis),
            S.num(n.jumlah),
            S.fmt(n.fungsi),
            S.bool(n.pemangkasan),
            S.fmt(n.produksiPerTahun),
            S.fmt(n.tahunTanam?.toString()),
        )
    }

    val sectionC = S.GAP_GROUPS.map { g ->
        PetaniExportModel.GapGroupRows(
            kelompok = g.kelompok,
            rows = g.items.map { item ->
                val row = p.praktikGap.find { it.jenis == item.jenis }
                PetaniExportModel.GapRow(
                    label = item.label,
                    jawaban = row?.jawaban?.name,
                    keterangan = S.fmt(row?.keterangan),
                )
            },
        )
    }

    val sectionD = S.TAHUN_PRODUKSI.map { tahun ->
        val r = p.produksi.find { it.tahun == tahun }
        listOf(
            if (tahun == S.TAHUN_ESTIMASI) "$tahun (estimasi)" else tahun.toString(),
            S.satuanLabel(r?.satuan),
            S.num(r?.cherry),
            S.num(r?.gabahBasah),
            S.num(r?.gabahKering),
            S.num(r?.greenBean),
            S.num(r?.produktivitas),
        )
    }

    val sectionE1 = p.produk.map { r ->
        PetaniExportModel.ProdukRow(
            label = if (r.jenis.name == "LAINNYA") {
                "Lainnya: ${S.fmt(r.labelCustom)}"
            } else {
                S.PRODUK.firstOrNull { it.first == r.jenis }?.second ?: r.jenis.name
            },
            aktif = r.dijual,
            volume = if (r.dijual) S.num(r.volumeKgTahun) else "0",
        )
    }

    val sectionE2 = p.pasar.map { r ->
        PetaniExportModel.PasarRow(
            label = if (r.kategori.name == "LAINNYA") {
                "Lainnya: ${S.fmt(r.labelCustom)}"
            } else {
                S.PASAR.firstOrNull { it.first == r.kategori }?.second ?: r.kategori.name
            },
            aktif = r.aktif,
            persentase = if (r.aktif) S.num(r.persentase) else "0",
            profil = if (r.aktif) S.fmt(r.profilPenjual) else "-",
        )
    }

    val sectionF = S.KONDISI_KEBUN.map { (jenis, label) ->
        val row = p.kondisiKebun.find { it.jenis == jenis }
        PetaniExportModel.KondisiRow(label, row?.jawaban ?: false, S.fmt(row?.keterangan))
    }

    return PetaniExportModel(
        fileName = "Data Petani ${p.namaLengkap}",
        penginput = p.createdBy ?: "-",
        tanggalInput = S.fmtDate(p.createdAt),
        sectionA = sectionA,
        petugas = petugas,
        sectionB1 = sectionB1,
        sectionB1b = sectionB1b,
        sectionB2 = sectionB2,
        sectionC = sectionC,
        sectionD = sectionD,
        sectionE1 = sectionE1,
        sectionE2 = sectionE2,
        sectionF = sectionF,
    )
}

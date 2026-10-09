package id.caritas_kopi.be.export

import id.caritas_kopi.be.desa.DesaDetailDto

data class DesaExportModel(
    val fileName: String,
    val penginput: String,
    val tanggalInput: String,
    val sectionA: List<Pair<Pair<String, String>, Pair<String, String>>>,
    val sectionB: List<KebijakanRow>,
    val sectionC: List<LembagaRow>,
    val sectionD: List<Pair<String, String>>,
    val sectionE: List<Pair<String, String>>,
) {
    data class KebijakanRow(val label: String, val ada: Boolean, val keterangan: String)
    data class LembagaRow(val label: String, val jumlah: String, val kondisi: String)
}

fun buildDesaExportModel(b: DesaDetailDto): DesaExportModel {
    val S = ExportSupport
    val w = b.desa

    val koordinat = if (b.latitude != null && b.longitude != null) "${b.latitude}, ${b.longitude}" else "-"
    val musim = if (!b.bulanHujan.isNullOrEmpty() || !b.bulanKering.isNullOrEmpty()) {
        "Hujan: ${b.bulanHujan ?: "-"}\nKering: ${b.bulanKering ?: "-"}"
    } else "-"

    val sectionA = listOf(
        ("Desa" to w.nama) to ("Topografi" to S.fmt(b.topografi)),
        ("Kecamatan" to w.kecamatan) to ("Ketinggian (mdpl)" to S.num(b.ketinggianMdpl)),
        ("Kabupaten" to w.kabupaten) to ("Bulan Hujan dan Bulan Kering" to musim),
        ("Provinsi" to w.provinsi) to ("Suhu Rata-rata (°C)" to S.num(b.suhuRataRataC)),
        ("Luas Wilayah (Ha)" to S.num(b.luasWilayahHa)) to ("Jenis Tanah" to S.fmt(b.jenisTanah)),
        ("Jumlah Penduduk" to S.num(b.jumlahPenduduk)) to ("Akses Jalan" to S.fmt(b.aksesJalan)),
        ("Jumlah KK" to S.num(b.jumlahKK)) to
            ("Jarak ke Ibu Kota Kecamatan" to S.numUnit(b.jarakIbukotaKecamatanKm, "km")),
        ("Jumlah Petani Kopi" to S.num(b.jumlahPetaniKopi)) to
            ("Jarak ke Pasar" to S.numUnit(b.jarakPasarKm, "km")),
        ("Luas Areal Kopi (Ha)" to S.num(b.luasArealKopiHa)) to
            ("Jarak ke Kawasan Konservasi" to S.numUnit(b.jarakKonservasiKm, "km")),
        ("Luas Area Komoditi lainnya" to S.numUnit(b.luasKomoditiLainHa, "Ha")) to
            ("Luas Lahan APL" to S.numUnit(b.luasAPLHa, "Ha")),
        ("Koordinat Desa" to koordinat) to ("Nama Kawasan Konservasi" to S.fmt(b.namaKawasanKonservasi)),
        ("Tahun Pendataan" to S.fmt(b.tahunPendataan.toString())) to ("Sumber Data" to S.fmt(b.sumberData)),
    )

    val sectionB = S.KEBIJAKAN.map { (jenis, label) ->
        val row = b.kebijakan.find { it.jenis == jenis }
        DesaExportModel.KebijakanRow(label, row?.ada ?: false, row?.keterangan ?: "-")
    }

    val sectionC = S.LEMBAGA.map { (jenis, label) ->
        val row = b.kelembagaan.find { it.jenis == jenis }
        DesaExportModel.LembagaRow(
            label,
            if (row?.jumlah != null) row.jumlah.toString() else "0",
            row?.kondisi ?: "-",
        )
    }

    val sectionD = listOf(
        "Jumlah Petani Kopi (orang)" to S.num(b.jumlahPetaniKopi),
        "Luas Kebun Kopi (Ha)" to S.num(b.luasArealKopiHa),
        "Produktivitas Rata-rata (Kg/Ha/Tahun)" to S.num(b.produktivitasKgHaTahun),
        "Harga Cherry (Rp)" to S.num(b.hargaCherryRp),
        "Harga Green Bean (Rp/kg)" to S.num(b.hargaGreenBeanRpKg),
        "Pembeli Utama" to S.fmt(b.pembeliUtama),
        "Jumlah Pedagang Pengumpul (Orang)" to S.num(b.jumlahPedagangPengumpul),
        "Koperasi Aktif (Unit)" to S.num(b.koperasiAktifUnit),
        "Eksportir" to S.fmt(b.eksportir),
        "Industri Pengolahan" to S.fmt(b.industriPengolahan),
        "Permasalahan Utama" to S.fmt(b.permasalahanUtama),
    )

    val sectionE = listOf(
        "Berbatasan Kawasan Konservasi (Ya/Tidak)" to S.fmtBool(b.berbatasanKonservasi),
        "Luas Kawasan Penyangga (Ha)" to S.num(b.luasPenyanggaHa),
        "Tutupan Hutan (% atau Ha)" to S.fmtTutupan(b.tutupanHutan, b.tutupanHutanSatuan?.name),
        "Tutupan Agroforestry (% atau Ha)" to S.fmtTutupan(b.tutupanAgroforestry, b.tutupanAgroforestrySatuan?.name),
        "Daerah Rawan Longsor" to S.fmtBoolDetail(b.rawanLongsor, b.lokasiRawanLongsor, "lokasi"),
        "Daerah Rawan Erosi" to S.fmtBoolDetail(b.rawanErosi, b.lokasiRawanErosi, "lokasi"),
        "Konflik Satwa" to S.fmtBoolDetail(b.konflikSatwa, b.jenisSatwaKonflik, "jenis"),
        "Praktik Konservasi yang Sudah Ada" to S.fmt(b.praktikKonservasi),
    )

    return DesaExportModel(
        fileName = "Data Desa ${w.nama}",
        penginput = b.createdBy ?: "-",
        tanggalInput = S.fmtDate(b.createdAt),
        sectionA = sectionA,
        sectionB = sectionB,
        sectionC = sectionC,
        sectionD = sectionD,
        sectionE = sectionE,
    )
}

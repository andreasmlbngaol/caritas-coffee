package id.caritas_kopi.be.desa

import id.caritas_kopi.be.auth.AuthPrincipal
import id.caritas_kopi.be.common.ApiException
import id.caritas_kopi.be.petani.buildDesaRingkas
import id.caritas_kopi.be.user.UserRepository
import id.caritas_kopi.be.wilayah.WilayahLookupService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class DesaService(
    private val baselineRepo: BaselineDesaRepository,
    private val kebijakanRepo: KebijakanDesaRepository,
    private val kelembagaanRepo: KelembagaanDesaRepository,
    private val userRepo: UserRepository,
    private val wilayah: WilayahLookupService,
) {

    @Transactional(readOnly = true)
    fun list(me: AuthPrincipal): List<DesaListDto> {
        val rows = if (me.isAdmin) baselineRepo.findAllByOrderByCreatedAtDesc()
        else baselineRepo.findAllByCreatedByIdOrderByCreatedAtDesc(me.id)
        if (rows.isEmpty()) return emptyList()

        val desaMap = wilayah.desaMap(rows.map { it.desaKode })
        val kecMap = wilayah.kecamatanMap(desaMap.values.map { it.kecamatanKode })
        val userNames = namaPenginput(rows.map { it.createdById })

        return rows.map { b ->
            val desa = desaMap[b.desaKode]
            val kec = desa?.let { kecMap[it.kecamatanKode] }
            DesaListDto(
                id = b.id.toString(),
                desa = desa?.nama ?: "-",
                kecamatan = kec?.nama ?: "-",
                tahunPendataan = b.tahunPendataan,
                jumlahPetaniKopi = b.jumlahPetaniKopi,
                luasArealKopiHa = b.luasArealKopiHa,
                createdBy = userNames[b.createdById],
                createdAt = b.createdAt,
            )
        }
    }

    @Transactional(readOnly = true)
    fun detail(id: UUID, me: AuthPrincipal): DesaDetailDto {
        val b = baselineRepo.findById(id).orElseThrow { ApiException.notFound("Data tidak ditemukan") }
        requireOwned(b, me)

        val desaMap = wilayah.desaMap(listOf(b.desaKode))
        val kecMap = wilayah.kecamatanMap(desaMap.values.map { it.kecamatanKode })
        val kabMap = wilayah.kabupatenMap(kecMap.values.map { it.kabupatenKode })
        val provMap = wilayah.provinsiMap(kabMap.values.map { it.provinsiKode })
        val desa = desaMap[b.desaKode] ?: throw ApiException.notFound("Desa tidak ditemukan")
        val userNames = namaPenginput(listOf(b.createdById))

        return DesaDetailDto(
            id = b.id.toString(),
            tahunPendataan = b.tahunPendataan,
            sumberData = b.sumberData,
            desa = buildDesaRingkas(desa, kecMap, kabMap, provMap),
            createdBy = userNames[b.createdById],
            createdAt = b.createdAt,
            updatedAt = b.updatedAt,
            luasWilayahHa = b.luasWilayahHa,
            jumlahPenduduk = b.jumlahPenduduk,
            jumlahKK = b.jumlahKK,
            jumlahPetaniKopi = b.jumlahPetaniKopi,
            luasArealKopiHa = b.luasArealKopiHa,
            luasKomoditiLainHa = b.luasKomoditiLainHa,
            latitude = b.latitude,
            longitude = b.longitude,
            topografi = b.topografi,
            ketinggianMdpl = b.ketinggianMdpl,
            bulanHujan = b.bulanHujan,
            bulanKering = b.bulanKering,
            suhuRataRataC = b.suhuRataRataC,
            jenisTanah = b.jenisTanah,
            aksesJalan = b.aksesJalan,
            jarakIbukotaKecamatanKm = b.jarakIbukotaKecamatanKm,
            jarakPasarKm = b.jarakPasarKm,
            jarakKonservasiKm = b.jarakKonservasiKm,
            luasAPLHa = b.luasAPLHa,
            namaKawasanKonservasi = b.namaKawasanKonservasi,
            produktivitasKgHaTahun = b.produktivitasKgHaTahun,
            hargaCherryRp = b.hargaCherryRp,
            hargaGreenBeanRpKg = b.hargaGreenBeanRpKg,
            pembeliUtama = b.pembeliUtama,
            jumlahPedagangPengumpul = b.jumlahPedagangPengumpul,
            koperasiAktifUnit = b.koperasiAktifUnit,
            eksportir = b.eksportir,
            industriPengolahan = b.industriPengolahan,
            permasalahanUtama = b.permasalahanUtama,
            berbatasanKonservasi = b.berbatasanKonservasi,
            luasPenyanggaHa = b.luasPenyanggaHa,
            tutupanHutan = b.tutupanHutan,
            tutupanHutanSatuan = b.tutupanHutanSatuan,
            tutupanAgroforestry = b.tutupanAgroforestry,
            tutupanAgroforestrySatuan = b.tutupanAgroforestrySatuan,
            rawanLongsor = b.rawanLongsor,
            lokasiRawanLongsor = b.lokasiRawanLongsor,
            rawanErosi = b.rawanErosi,
            lokasiRawanErosi = b.lokasiRawanErosi,
            konflikSatwa = b.konflikSatwa,
            jenisSatwaKonflik = b.jenisSatwaKonflik,
            praktikKonservasi = b.praktikKonservasi,
            kebijakan = kebijakanRepo.findByBaselineId(id).map { it.toDto() },
            kelembagaan = kelembagaanRepo.findByBaselineId(id).map { it.toDto() },
        )
    }

    @Transactional
    fun create(req: BaselineDesaRequest, me: AuthPrincipal): UUID {
        if (baselineRepo.existsByDesaKode(req.desaKode)) {
            throw ApiException.conflict("Baseline untuk desa ini sudah diinput")
        }
        val b = BaselineDesa(createdById = me.id)
        applyScalars(b, req)
        val saved = baselineRepo.save(b)
        saveChildren(saved.id!!, req)
        return saved.id!!
    }

    @Transactional
    fun update(id: UUID, req: BaselineDesaRequest, me: AuthPrincipal) {
        val b = baselineRepo.findById(id).orElseThrow { ApiException.notFound("Data tidak ditemukan") }
        requireOwned(b, me)
        if (req.desaKode != b.desaKode && baselineRepo.existsByDesaKode(req.desaKode)) {
            throw ApiException.conflict("Baseline untuk desa tujuan sudah ada")
        }
        applyScalars(b, req)
        b.updatedAt = Instant.now()
        baselineRepo.save(b)

        kebijakanRepo.deleteByBaselineId(id)
        kelembagaanRepo.deleteByBaselineId(id)
        saveChildren(id, req)
    }

    @Transactional
    fun delete(id: UUID, me: AuthPrincipal) {
        val b = baselineRepo.findById(id).orElseThrow { ApiException.notFound("Data tidak ditemukan") }
        requireOwned(b, me)
        baselineRepo.delete(b)
    }

    // ---------- Helpers ----------

    private fun requireOwned(b: BaselineDesa, me: AuthPrincipal) {
        // Non-owner: sembunyikan keberadaan data (404), sama seperti notFound() di halaman detail Next lama.
        if (!me.isAdmin && b.createdById != me.id) throw ApiException.notFound("Data tidak ditemukan")
    }

    private fun applyScalars(b: BaselineDesa, req: BaselineDesaRequest) {
        if (req.desaKode.isBlank()) throw ApiException.badRequest("Desa wajib dipilih")
        b.desaKode = req.desaKode
        b.tahunPendataan = req.tahunPendataan
        b.sumberData = req.sumberData
        b.luasWilayahHa = req.luasWilayahHa
        b.jumlahPenduduk = req.jumlahPenduduk
        b.jumlahKK = req.jumlahKK
        b.jumlahPetaniKopi = req.jumlahPetaniKopi
        b.luasArealKopiHa = req.luasArealKopiHa
        b.luasKomoditiLainHa = req.luasKomoditiLainHa
        b.latitude = req.latitude
        b.longitude = req.longitude
        b.topografi = req.topografi
        b.ketinggianMdpl = req.ketinggianMdpl
        b.bulanHujan = req.bulanHujan
        b.bulanKering = req.bulanKering
        b.suhuRataRataC = req.suhuRataRataC
        b.jenisTanah = req.jenisTanah
        b.aksesJalan = req.aksesJalan
        b.jarakIbukotaKecamatanKm = req.jarakIbukotaKecamatanKm
        b.jarakPasarKm = req.jarakPasarKm
        b.jarakKonservasiKm = req.jarakKonservasiKm
        b.luasAPLHa = req.luasAPLHa
        b.namaKawasanKonservasi = req.namaKawasanKonservasi
        b.produktivitasKgHaTahun = req.produktivitasKgHaTahun
        b.hargaCherryRp = req.hargaCherryRp
        b.hargaGreenBeanRpKg = req.hargaGreenBeanRpKg
        b.pembeliUtama = req.pembeliUtama
        b.jumlahPedagangPengumpul = req.jumlahPedagangPengumpul
        b.koperasiAktifUnit = req.koperasiAktifUnit
        b.eksportir = req.eksportir
        b.industriPengolahan = req.industriPengolahan
        b.permasalahanUtama = req.permasalahanUtama
        b.berbatasanKonservasi = req.berbatasanKonservasi
        b.luasPenyanggaHa = req.luasPenyanggaHa
        b.tutupanHutan = req.tutupanHutan
        b.tutupanHutanSatuan = req.tutupanHutanSatuan
        b.tutupanAgroforestry = req.tutupanAgroforestry
        b.tutupanAgroforestrySatuan = req.tutupanAgroforestrySatuan
        b.rawanLongsor = req.rawanLongsor
        b.lokasiRawanLongsor = req.lokasiRawanLongsor
        b.rawanErosi = req.rawanErosi
        b.lokasiRawanErosi = req.lokasiRawanErosi
        b.konflikSatwa = req.konflikSatwa
        b.jenisSatwaKonflik = req.jenisSatwaKonflik
        b.praktikKonservasi = req.praktikKonservasi
    }

    private fun saveChildren(baselineId: UUID, req: BaselineDesaRequest) {
        req.kebijakan.forEach {
            kebijakanRepo.save(KebijakanDesa(baselineId = baselineId, jenis = it.jenis, ada = it.ada, keterangan = it.keterangan))
        }
        req.kelembagaan.forEach {
            kelembagaanRepo.save(KelembagaanDesa(baselineId = baselineId, jenis = it.jenis, jumlah = it.jumlah, kondisi = it.kondisi))
        }
    }

    private fun namaPenginput(ids: Collection<UUID>): Map<UUID, String> {
        if (ids.isEmpty()) return emptyMap()
        return userRepo.findAllById(ids.toSet()).associate { it.id!! to (it.fullName ?: it.username) }
    }
}

fun KebijakanDesa.toDto() = KebijakanDto(id.toString(), jenis, ada, keterangan)
fun KelembagaanDesa.toDto() = KelembagaanDto(id.toString(), jenis, jumlah, kondisi)

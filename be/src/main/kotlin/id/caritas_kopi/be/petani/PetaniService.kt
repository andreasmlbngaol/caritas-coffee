package id.caritas_kopi.be.petani

import id.caritas_kopi.be.auth.AuthPrincipal
import id.caritas_kopi.be.common.ApiException
import id.caritas_kopi.be.kelompoktani.KelompokTani
import id.caritas_kopi.be.kelompoktani.KelompokTaniRepository
import id.caritas_kopi.be.user.UserRepository
import id.caritas_kopi.be.wilayah.WilayahLookupService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class PetaniService(
    private val petaniRepo: PetaniRepository,
    private val plotRepo: PlotPetaniRepository,
    private val naunganRepo: TanamanNaunganRepository,
    private val gapRepo: PraktikGapRepository,
    private val produksiRepo: RiwayatProduksiRepository,
    private val produkRepo: ProdukDijualRepository,
    private val pasarRepo: PasarPetaniRepository,
    private val kondisiRepo: KondisiKebunRepository,
    private val kelompokRepo: KelompokTaniRepository,
    private val userRepo: UserRepository,
    private val wilayah: WilayahLookupService,
) {

    @Transactional(readOnly = true)
    fun list(me: AuthPrincipal): List<PetaniListDto> {
        val rows = if (me.isAdmin) petaniRepo.findAllByOrderByCreatedAtDesc()
        else petaniRepo.findAllByCreatedByIdOrderByCreatedAtDesc(me.id)
        if (rows.isEmpty()) return emptyList()

        val desaMap = wilayah.desaMap(rows.map { it.desaKode })
        val kecMap = wilayah.kecamatanMap(desaMap.values.map { it.kecamatanKode })
        val kabMap = wilayah.kabupatenMap(kecMap.values.map { it.kabupatenKode })
        val provMap = wilayah.provinsiMap(kabMap.values.map { it.provinsiKode })
        val kelompokMap = kelompokRepo.findAllById(rows.mapNotNull { it.kelompokTaniId }).associateBy { it.id }
        val userNames = namaPenginput(rows.map { it.createdById })

        return rows.map { p ->
            val ringkas = desaMap[p.desaKode]?.let {
                buildDesaRingkas(it, kecMap, kabMap, provMap)
            }
            PetaniListDto(
                id = p.id.toString(),
                kodePetani = p.kodePetani,
                namaLengkap = p.namaLengkap,
                desa = ringkas?.nama ?: "-",
                kecamatan = ringkas?.kecamatan ?: "-",
                kelompokTani = p.kelompokTaniId?.let { kelompokMap[it]?.nama },
                kelompokTaniId = p.kelompokTaniId?.toString(),
                kelompokTaniKode = p.kelompokTaniId?.let { kelompokMap[it]?.kode },
                telepon = p.telepon,
                createdBy = userNames[p.createdById],
                createdAt = p.createdAt,
            )
        }
    }

    @Transactional(readOnly = true)
    fun detail(id: UUID, me: AuthPrincipal): PetaniDetailDto {
        val p = petaniRepo.findById(id).orElseThrow { ApiException.notFound("Data tidak ditemukan") }
        requireOwned(p, me)

        val desaMap = wilayah.desaMap(listOf(p.desaKode))
        val kecMap = wilayah.kecamatanMap(desaMap.values.map { it.kecamatanKode })
        val kabMap = wilayah.kabupatenMap(kecMap.values.map { it.kabupatenKode })
        val provMap = wilayah.provinsiMap(kabMap.values.map { it.provinsiKode })
        val desa = desaMap[p.desaKode] ?: throw ApiException.notFound("Desa tidak ditemukan")
        val kelompok = p.kelompokTaniId?.let { kelompokRepo.findById(it).orElse(null) }
        val userNames = namaPenginput(listOf(p.createdById))

        return PetaniDetailDto(
            id = p.id.toString(),
            kodePetani = p.kodePetani,
            namaLengkap = p.namaLengkap,
            namaPanggilan = p.namaPanggilan,
            jenisKelamin = p.jenisKelamin,
            tanggalLahir = p.tanggalLahir,
            alamatDomisili = p.alamatDomisili,
            telepon = p.telepon,
            tanggalPendaftaran = p.tanggalPendaftaran,
            namaPetugasPendaftar = p.namaPetugasPendaftar,
            kontakDaruratNama = p.kontakDaruratNama,
            kontakDaruratTelepon = p.kontakDaruratTelepon,
            kontakDaruratHubungan = p.kontakDaruratHubungan,
            desa = buildDesaRingkas(desa, kecMap, kabMap, provMap),
            kelompokTani = kelompok?.toRingkas(),
            createdBy = userNames[p.createdById],
            createdAt = p.createdAt,
            updatedAt = p.updatedAt,
            plot = plotRepo.findByPetaniIdOrderByNomorAsc(id).map { it.toDto() },
            naungan = naunganRepo.findByPetaniId(id).map { it.toDto() },
            praktikGap = gapRepo.findByPetaniId(id).map { it.toDto() },
            produksi = produksiRepo.findByPetaniId(id).sortedBy { it.tahun }.map { it.toDto() },
            produk = produkRepo.findByPetaniId(id).map { it.toDto() },
            pasar = pasarRepo.findByPetaniId(id).map { it.toDto() },
            kondisiKebun = kondisiRepo.findByPetaniId(id).map { it.toDto() },
        )
    }

    @Transactional
    fun create(req: PetaniRequest, me: AuthPrincipal): UUID {
        validate(req)
        val kode = normKodePetani(req.kodePetani)
        if (kode != null && petaniRepo.findByKodePetani(kode).isPresent) {
            throw ApiException.conflict("Kode petani sudah dipakai")
        }
        val kelompokId = resolveKelompok(req, req.desaKode)

        val petani = Petani(
            namaLengkap = req.namaLengkap.trim(),
            namaPanggilan = req.namaPanggilan,
            jenisKelamin = req.jenisKelamin,
            tanggalLahir = req.tanggalLahir,
            alamatDomisili = req.alamatDomisili,
            telepon = req.telepon,
            tanggalPendaftaran = req.tanggalPendaftaran,
            namaPetugasPendaftar = req.namaPetugasPendaftar,
            kontakDaruratNama = req.kontakDaruratNama,
            kontakDaruratTelepon = req.kontakDaruratTelepon,
            kontakDaruratHubungan = req.kontakDaruratHubungan,
            desaKode = req.desaKode,
            kodePetani = kode,
            kelompokTaniId = kelompokId,
            createdById = me.id,
        )
        val saved = petaniRepo.save(petani)
        saveChildren(saved.id!!, req)
        return saved.id!!
    }

    @Transactional
    fun update(id: UUID, req: PetaniRequest, me: AuthPrincipal): Unit {
        val p = petaniRepo.findById(id).orElseThrow { ApiException.notFound("Data tidak ditemukan") }
        requireOwned(p, me)
        validate(req)

        val kode = normKodePetani(req.kodePetani)
        if (kode != null && kode != p.kodePetani &&
            petaniRepo.findByKodePetani(kode).isPresent
        ) {
            throw ApiException.conflict("Kode petani sudah dipakai")
        }
        val kelompokId = resolveKelompok(req, req.desaKode)

        p.namaLengkap = req.namaLengkap.trim()
        p.namaPanggilan = req.namaPanggilan
        p.jenisKelamin = req.jenisKelamin
        p.tanggalLahir = req.tanggalLahir
        p.alamatDomisili = req.alamatDomisili
        p.telepon = req.telepon
        p.tanggalPendaftaran = req.tanggalPendaftaran
        p.namaPetugasPendaftar = req.namaPetugasPendaftar
        p.kontakDaruratNama = req.kontakDaruratNama
        p.kontakDaruratTelepon = req.kontakDaruratTelepon
        p.kontakDaruratHubungan = req.kontakDaruratHubungan
        p.desaKode = req.desaKode
        p.kodePetani = kode
        p.kelompokTaniId = kelompokId
        p.updatedAt = Instant.now()
        petaniRepo.saveAndFlush(p)

        plotRepo.deleteByPetaniId(id)
        naunganRepo.deleteByPetaniId(id)
        gapRepo.deleteByPetaniId(id)
        produksiRepo.deleteByPetaniId(id)
        produkRepo.deleteByPetaniId(id)
        pasarRepo.deleteByPetaniId(id)
        kondisiRepo.deleteByPetaniId(id)
        saveChildren(id, req)
    }

    @Transactional
    fun delete(id: UUID, me: AuthPrincipal) {
        val p = petaniRepo.findById(id).orElseThrow { ApiException.notFound("Data tidak ditemukan") }
        requireOwned(p, me)
        petaniRepo.delete(p)
    }

    // ---------- Helpers ----------

    private fun validate(req: PetaniRequest) {
        if (req.namaLengkap.isBlank()) throw ApiException.badRequest("Nama lengkap wajib diisi")
        if (req.desaKode.isBlank()) throw ApiException.badRequest("Desa wajib dipilih")
    }

    /** Normalisasi kode petani (trim + UPPERCASE) agar cek unik konsisten dgn data lama. */
    private fun normKodePetani(kode: String?): String? =
        kode?.trim()?.takeIf { it.isNotEmpty() }?.uppercase()

    private fun requireOwned(p: Petani, me: AuthPrincipal) {
        // Non-owner: sembunyikan keberadaan data (404), sama seperti notFound() di halaman detail Next lama.
        if (!me.isAdmin && p.createdById != me.id) throw ApiException.notFound("Data tidak ditemukan")
    }

    private fun resolveKelompok(req: PetaniRequest, desaKode: String): UUID? {
        req.kelompokTaniId?.let { idStr ->
            val id = runCatching { UUID.fromString(idStr) }.getOrElse {
                throw ApiException.badRequest("Kelompok tani tidak valid")
            }
            val kt = kelompokRepo.findById(id)
                .orElseThrow { ApiException.badRequest("Kelompok tani tidak valid") }
            if (kt.desaKode != desaKode) throw ApiException.badRequest("Kelompok tani tidak valid")
            return kt.id
        }
        val baru = req.kelompokTaniBaru ?: return null
        if (baru.nama.isBlank()) return null
        val kode = baru.kode?.trim()?.takeIf { it.isNotEmpty() }
        val existing = if (kode != null) {
            kelompokRepo.findByDesaKodeAndKode(desaKode, kode).orElse(null)
        } else {
            kelompokRepo.findFirstByDesaKodeAndKodeIsNullAndNamaIgnoreCase(desaKode, baru.nama.trim()).orElse(null)
        }
        if (existing != null) return existing.id
        return kelompokRepo.save(KelompokTani(nama = baru.nama.trim(), kode = kode, desaKode = desaKode)).id
    }

    private fun saveChildren(petaniId: UUID, req: PetaniRequest) {
        req.plot.forEachIndexed { i, pl ->
            plotRepo.save(
                PlotPetani(
                    nomor = i + 1,
                    petaniId = petaniId,
                    namaHamparan = pl.namaHamparan,
                    varietas = pl.varietas,
                    tahunTanam = pl.tahunTanam.toIntArray(),
                    kodeGps = pl.kodeGps,
                    elevasiMdpl = pl.elevasiMdpl,
                    kemiringanPersen = pl.kemiringanPersen,
                    luasKopiHa = pl.luasKopiHa,
                    fotoKey = pl.fotoKey,
                    fotoLatitude = pl.fotoLatitude,
                    fotoLongitude = pl.fotoLongitude,
                    statusKepemilikan = pl.statusKepemilikan,
                    sistemBudidaya = pl.sistemBudidaya,
                    areaKonservasi = pl.areaKonservasi,
                    tanamanBaru = pl.tanamanBaru,
                    pohonProduktif = pl.pohonProduktif,
                    pohonTidakProduktif = pl.pohonTidakProduktif,
                    pestisidaNama = pl.pestisidaNama,
                    pestisidaBulanTahun = pl.pestisidaBulanTahun,
                ),
            )
        }
        req.naungan.forEach {
            naunganRepo.save(
                TanamanNaungan(
                    petaniId = petaniId, jenis = it.jenis, jumlah = it.jumlah, fungsi = it.fungsi,
                    pemangkasan = it.pemangkasan, produksiPerTahun = it.produksiPerTahun, tahunTanam = it.tahunTanam,
                ),
            )
        }
        req.praktikGap.forEach {
            gapRepo.save(
                PraktikGap(petaniId = petaniId, jenis = it.jenis, jawaban = it.jawaban, keterangan = it.keterangan),
            )
        }
        req.produksi.forEach {
            produksiRepo.save(
                RiwayatProduksi(
                    petaniId = petaniId, tahun = it.tahun, satuan = it.satuan, cherry = it.cherry,
                    gabahBasah = it.gabahBasah, gabahKering = it.gabahKering, greenBean = it.greenBean,
                    produktivitas = listOfNotNull(it.cherry, it.gabahBasah, it.gabahKering, it.greenBean).sum(),
                ),
            )
        }
        req.produk.forEach {
            produkRepo.save(
                ProdukDijual(
                    petaniId = petaniId, jenis = it.jenis, labelCustom = it.labelCustom,
                    dijual = it.dijual, volumeKgTahun = it.volumeKgTahun,
                ),
            )
        }
        req.pasar.forEach {
            pasarRepo.save(
                PasarPetani(
                    petaniId = petaniId, kategori = it.kategori, labelCustom = it.labelCustom,
                    aktif = it.aktif, persentase = it.persentase, profilPenjual = it.profilPenjual,
                ),
            )
        }
        req.kondisiKebun.forEach {
            kondisiRepo.save(
                KondisiKebun(petaniId = petaniId, jenis = it.jenis, jawaban = it.jawaban, keterangan = it.keterangan),
            )
        }
    }

    private fun namaPenginput(ids: Collection<UUID>): Map<UUID, String> {
        if (ids.isEmpty()) return emptyMap()
        return userRepo.findAllById(ids.toSet()).associate { it.id!! to (it.fullName ?: it.username) }
    }
}

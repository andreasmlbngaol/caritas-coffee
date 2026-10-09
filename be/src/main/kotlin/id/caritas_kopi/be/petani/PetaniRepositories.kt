package id.caritas_kopi.be.petani

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface PetaniRepository : JpaRepository<Petani, UUID> {
    fun findByKodePetani(kodePetani: String): Optional<Petani>
    fun findAllByOrderByCreatedAtDesc(): List<Petani>
    fun findAllByCreatedByIdOrderByCreatedAtDesc(createdById: UUID): List<Petani>
    fun countByDesaKode(desaKode: String): Long
    fun countByKelompokTaniId(kelompokTaniId: UUID): Long
}

interface PlotPetaniRepository : JpaRepository<PlotPetani, UUID> {
    fun findByPetaniIdOrderByNomorAsc(petaniId: UUID): List<PlotPetani>
    fun deleteByPetaniId(petaniId: UUID)
}

interface TanamanNaunganRepository : JpaRepository<TanamanNaungan, UUID> {
    fun findByPetaniId(petaniId: UUID): List<TanamanNaungan>
    fun deleteByPetaniId(petaniId: UUID)
}

interface PraktikGapRepository : JpaRepository<PraktikGap, UUID> {
    fun findByPetaniId(petaniId: UUID): List<PraktikGap>
    fun deleteByPetaniId(petaniId: UUID)
}

interface RiwayatProduksiRepository : JpaRepository<RiwayatProduksi, UUID> {
    fun findByPetaniId(petaniId: UUID): List<RiwayatProduksi>
    fun deleteByPetaniId(petaniId: UUID)
}

interface ProdukDijualRepository : JpaRepository<ProdukDijual, UUID> {
    fun findByPetaniId(petaniId: UUID): List<ProdukDijual>
    fun deleteByPetaniId(petaniId: UUID)
    fun findByDijualTrue(): List<ProdukDijual>
}

interface PasarPetaniRepository : JpaRepository<PasarPetani, UUID> {
    fun findByPetaniId(petaniId: UUID): List<PasarPetani>
    fun deleteByPetaniId(petaniId: UUID)
    fun findByAktifTrue(): List<PasarPetani>
}

interface KondisiKebunRepository : JpaRepository<KondisiKebun, UUID> {
    fun findByPetaniId(petaniId: UUID): List<KondisiKebun>
    fun deleteByPetaniId(petaniId: UUID)
    fun findByJawabanTrue(): List<KondisiKebun>
}

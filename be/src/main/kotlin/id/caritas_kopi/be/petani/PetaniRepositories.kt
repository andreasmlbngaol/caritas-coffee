package id.caritas_kopi.be.petani

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
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

    // Bulk delete langsung (bukan load-lalu-remove): update() mengganti seluruh
    // anak, dan insert baru HARUS terjadi setelah delete ter-flush. Tanpa ini
    // Hibernate bisa men-flush insert lebih dulu -> langgar UNIQUE(petani_id, ...).
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from PlotPetani e where e.petaniId = :petaniId")
    fun deleteByPetaniId(@Param("petaniId") petaniId: UUID)
}

interface TanamanNaunganRepository : JpaRepository<TanamanNaungan, UUID> {
    fun findByPetaniId(petaniId: UUID): List<TanamanNaungan>

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from TanamanNaungan e where e.petaniId = :petaniId")
    fun deleteByPetaniId(@Param("petaniId") petaniId: UUID)
}

interface PraktikGapRepository : JpaRepository<PraktikGap, UUID> {
    fun findByPetaniId(petaniId: UUID): List<PraktikGap>

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from PraktikGap e where e.petaniId = :petaniId")
    fun deleteByPetaniId(@Param("petaniId") petaniId: UUID)
}

interface RiwayatProduksiRepository : JpaRepository<RiwayatProduksi, UUID> {
    fun findByPetaniId(petaniId: UUID): List<RiwayatProduksi>

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from RiwayatProduksi e where e.petaniId = :petaniId")
    fun deleteByPetaniId(@Param("petaniId") petaniId: UUID)
}

interface ProdukDijualRepository : JpaRepository<ProdukDijual, UUID> {
    fun findByPetaniId(petaniId: UUID): List<ProdukDijual>

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ProdukDijual e where e.petaniId = :petaniId")
    fun deleteByPetaniId(@Param("petaniId") petaniId: UUID)
}

interface PasarPetaniRepository : JpaRepository<PasarPetani, UUID> {
    fun findByPetaniId(petaniId: UUID): List<PasarPetani>
    fun findByAktifTrue(): List<PasarPetani>

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from PasarPetani e where e.petaniId = :petaniId")
    fun deleteByPetaniId(@Param("petaniId") petaniId: UUID)
}

interface KondisiKebunRepository : JpaRepository<KondisiKebun, UUID> {
    fun findByPetaniId(petaniId: UUID): List<KondisiKebun>
    fun findByJawabanTrue(): List<KondisiKebun>

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from KondisiKebun e where e.petaniId = :petaniId")
    fun deleteByPetaniId(@Param("petaniId") petaniId: UUID)
}

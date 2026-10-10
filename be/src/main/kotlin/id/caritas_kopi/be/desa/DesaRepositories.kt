package id.caritas_kopi.be.desa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

interface BaselineDesaRepository : JpaRepository<BaselineDesa, UUID> {
    fun findByDesaKode(desaKode: String): Optional<BaselineDesa>
    fun existsByDesaKode(desaKode: String): Boolean
    fun findAllByOrderByCreatedAtDesc(): List<BaselineDesa>
    fun findAllByCreatedByIdOrderByCreatedAtDesc(createdById: UUID): List<BaselineDesa>
}

interface KebijakanDesaRepository : JpaRepository<KebijakanDesa, UUID> {
    fun findByBaselineId(baselineId: UUID): List<KebijakanDesa>

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from KebijakanDesa e where e.baselineId = :baselineId")
    fun deleteByBaselineId(@Param("baselineId") baselineId: UUID)
}

interface KelembagaanDesaRepository : JpaRepository<KelembagaanDesa, UUID> {
    fun findByBaselineId(baselineId: UUID): List<KelembagaanDesa>

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from KelembagaanDesa e where e.baselineId = :baselineId")
    fun deleteByBaselineId(@Param("baselineId") baselineId: UUID)
}

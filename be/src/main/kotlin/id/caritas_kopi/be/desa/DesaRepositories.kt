package id.caritas_kopi.be.desa

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface BaselineDesaRepository : JpaRepository<BaselineDesa, UUID> {
    fun findByDesaKode(desaKode: String): Optional<BaselineDesa>
    fun existsByDesaKode(desaKode: String): Boolean
    fun findAllByOrderByCreatedAtDesc(): List<BaselineDesa>
}

interface KebijakanDesaRepository : JpaRepository<KebijakanDesa, UUID> {
    fun deleteByBaselineId(baselineId: UUID)
    fun findByBaselineId(baselineId: UUID): List<KebijakanDesa>
}

interface KelembagaanDesaRepository : JpaRepository<KelembagaanDesa, UUID> {
    fun deleteByBaselineId(baselineId: UUID)
    fun findByBaselineId(baselineId: UUID): List<KelembagaanDesa>
}

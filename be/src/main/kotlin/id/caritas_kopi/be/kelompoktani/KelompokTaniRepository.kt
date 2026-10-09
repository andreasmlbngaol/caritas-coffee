package id.caritas_kopi.be.kelompoktani

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface KelompokTaniRepository : JpaRepository<KelompokTani, UUID> {
    fun findByDesaKodeOrderByNamaAsc(desaKode: String): List<KelompokTani>
    fun findByDesaKodeAndKode(desaKode: String, kode: String): Optional<KelompokTani>
    fun findFirstByDesaKodeAndKodeIsNullAndNamaIgnoreCase(desaKode: String, nama: String): Optional<KelompokTani>
    fun findAllByOrderByNamaAsc(): List<KelompokTani>
}

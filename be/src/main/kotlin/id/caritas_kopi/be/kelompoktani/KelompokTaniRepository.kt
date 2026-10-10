package id.caritas_kopi.be.kelompoktani

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional
import java.util.UUID

interface KelompokTaniRepository : JpaRepository<KelompokTani, UUID> {
    fun findByDesaKodeOrderByNamaAsc(desaKode: String): List<KelompokTani>
    fun findByDesaKodeAndKode(desaKode: String, kode: String): Optional<KelompokTani>
    fun findFirstByDesaKodeAndKodeIsNullAndNamaIgnoreCase(desaKode: String, nama: String): Optional<KelompokTani>
    fun findAllByOrderByNamaAsc(): List<KelompokTani>

    /** Jumlah petani per kelompok dalam satu query (hindari N+1 di list()). */
    @Query("select p.kelompokTaniId, count(p) from Petani p where p.kelompokTaniId is not null group by p.kelompokTaniId")
    fun countPetaniPerKelompok(): List<Array<Any>>
}

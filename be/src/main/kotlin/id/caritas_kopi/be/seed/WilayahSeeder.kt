package id.caritas_kopi.be.seed

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

/**
 * Impor hierarki wilayah dari db/wilayah.sql (satu tabel `wilayah(kode,nama)`).
 * Idempoten: dilewati bila tabel wilayah_provinsi sudah terisi.
 * Memakai JDBC batch agar cepat (puluhan ribu baris).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class WilayahSeeder(
    private val jdbc: JdbcTemplate,
    @org.springframework.beans.factory.annotation.Value("\${app.seed-wilayah:true}") private val enabled: Boolean,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)
    private val regex = Regex("""\('((?:[^'\\]|\\.)*)','((?:[^'\\]|\\.)*)'\)""")

    override fun run(args: ApplicationArguments) {
        if (!enabled) {
            log.info("Seeder wilayah dimatikan (app.seed-wilayah=false).")
            return
        }
        val count = jdbc.queryForObject("select count(*) from wilayah_provinsi", Long::class.java) ?: 0
        if (count > 0) {
            log.info("Wilayah sudah terisi ({} provinsi), seeder dilewati.", count)
            return
        }

        val rows = parse()
        val provinsi = ArrayList<Pair<String, String>>()
        val kabupaten = ArrayList<Triple<String, String, String>>()
        val kecamatan = ArrayList<Triple<String, String, String>>()
        val desa = ArrayList<Triple<String, String, String>>()

        for ((kode, nama) in rows) {
            val parts = kode.split(".")
            when (parts.size) {
                1 -> provinsi += kode to nama
                2 -> kabupaten += Triple(kode, nama, parts[0])
                3 -> kecamatan += Triple(kode, nama, parts.subList(0, 2).joinToString("."))
                4 -> desa += Triple(kode, nama, parts.subList(0, 3).joinToString("."))
            }
        }

        // Buang baris yang kode induknya tidak ada (konsisten dgn seed lama).
        val provCodes = provinsi.map { it.first }.toHashSet()
        val kabFiltered = kabupaten.filter { it.third in provCodes }
        val kabCodes = kabFiltered.map { it.first }.toHashSet()
        val kecFiltered = kecamatan.filter { it.third in kabCodes }
        val kecCodes = kecFiltered.map { it.first }.toHashSet()
        val desaFiltered = desa.filter { it.third in kecCodes }

        log.info(
            "Import wilayah: provinsi={}, kabupaten={}, kecamatan={}, desa={}",
            provinsi.size, kabFiltered.size, kecFiltered.size, desaFiltered.size,
        )

        batch("insert into wilayah_provinsi(kode,nama) values (?,?)",
            provinsi.map { arrayOf(it.first, it.second) })
        batch("insert into wilayah_kabupaten(kode,nama,provinsi_kode) values (?,?,?)",
            kabFiltered.map { arrayOf(it.first, it.second, it.third) })
        batch("insert into wilayah_kecamatan(kode,nama,kabupaten_kode) values (?,?,?)",
            kecFiltered.map { arrayOf(it.first, it.second, it.third) })
        batch("insert into wilayah_desa(kode,nama,kecamatan_kode) values (?,?,?)",
            desaFiltered.map { arrayOf(it.first, it.second, it.third) })

        log.info("Import wilayah selesai.")
    }

    private fun parse(): List<Pair<String, String>> {
        val sql = ClassPathResource("db/wilayah.sql").inputStream.bufferedReader().use { it.readText() }
        return regex.findAll(sql).map { m ->
            m.groupValues[1] to m.groupValues[2].replace("\\'", "'")
        }.toList()
    }

    private fun batch(sql: String, rows: List<Array<Any?>>) {
        jdbc.batchUpdate(sql, rows)
    }
}

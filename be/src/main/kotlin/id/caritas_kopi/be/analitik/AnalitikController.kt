package id.caritas_kopi.be.analitik

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** Analitik ADMIN-only (dijaga SecurityConfig: hasRole ADMIN pada prefix /api/analitik). */
@RestController
@RequestMapping("/api/analitik")
class AnalitikController(private val service: AnalitikService) {

    @GetMapping("/ringkasan")
    fun ringkasan(): RingkasanDto = service.getRingkasan()

    @GetMapping("/gap")
    fun gap(): GapAdoptionDto = service.getGapAdoption()

    @GetMapping("/produksi")
    fun produksi(): ProduksiDto = service.getProduksi()

    @GetMapping("/pasar-produk")
    fun pasarProduk(): PasarProdukDto = service.getPasarProduk()

    @GetMapping("/konservasi")
    fun konservasi(): KonservasiDto = service.getKonservasi()

    @GetMapping("/wilayah")
    fun wilayah(): WilayahAnalitikDto = service.getWilayah()

    @GetMapping("/agronomi")
    fun agronomi(): AgronomiDto = service.getAgronomi()

    @GetMapping("/lokasi")
    fun lokasi(): LokasiDto = service.getLokasi()
}

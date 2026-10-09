package id.caritas_kopi.be.export

import id.caritas_kopi.be.auth.authPrincipal
import id.caritas_kopi.be.config.AppProperties
import id.caritas_kopi.be.desa.DesaService
import id.caritas_kopi.be.petani.PetaniService
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class ExportController(
    private val petaniService: PetaniService,
    private val desaService: DesaService,
    private val pdfRenderer: PdfRenderer,
    private val props: AppProperties,
) {

    @GetMapping("/api/petani/{id}/export/pdf")
    fun petaniPdf(@PathVariable id: UUID, auth: Authentication): ResponseEntity<ByteArray> {
        val model = buildPetaniExportModel(petaniService.detail(id, auth.authPrincipal()), appUrl())
        return pdf(pdfRenderer.render(PetaniPdf.html(model)), "${model.fileName}.pdf")
    }

    @GetMapping("/api/petani/{id}/export/docx")
    fun petaniDocx(@PathVariable id: UUID, auth: Authentication): ResponseEntity<ByteArray> {
        val model = buildPetaniExportModel(petaniService.detail(id, auth.authPrincipal()), appUrl())
        return docx(PetaniDocx.build(model), "${model.fileName}.docx")
    }

    @GetMapping("/api/desa/{id}/export/pdf")
    fun desaPdf(@PathVariable id: UUID, auth: Authentication): ResponseEntity<ByteArray> {
        val model = buildDesaExportModel(desaService.detail(id, auth.authPrincipal()))
        return pdf(pdfRenderer.render(DesaPdf.html(model)), "${model.fileName}.pdf")
    }

    @GetMapping("/api/desa/{id}/export/docx")
    fun desaDocx(@PathVariable id: UUID, auth: Authentication): ResponseEntity<ByteArray> {
        val model = buildDesaExportModel(desaService.detail(id, auth.authPrincipal()))
        return docx(DesaDocx.build(model), "${model.fileName}.docx")
    }

    private fun appUrl(): String = props.appUrl.trimEnd('/')

    private fun pdf(bytes: ByteArray, filename: String): ResponseEntity<ByteArray> =
        attachment(bytes, MediaType.APPLICATION_PDF, filename)

    private fun docx(bytes: ByteArray, filename: String): ResponseEntity<ByteArray> =
        attachment(bytes, MediaType.parseMediaType(DOCX_MIME), filename)

    private fun attachment(bytes: ByteArray, mime: MediaType, filename: String): ResponseEntity<ByteArray> {
        val disposition = ContentDisposition.attachment().filename(filename, Charsets.UTF_8).build()
        return ResponseEntity.ok()
            .contentType(mime)
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
            .body(bytes)
    }

    companion object {
        const val DOCX_MIME = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    }
}

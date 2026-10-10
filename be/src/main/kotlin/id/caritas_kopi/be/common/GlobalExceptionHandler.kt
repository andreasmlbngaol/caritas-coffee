package id.caritas_kopi.be.common

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException

/** Bentuk error seragam: { "error": "..." } (+ opsional "fields"). */
data class ApiError(
    val error: String,
    val fields: Map<String, String>? = null,
)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ApiException::class)
    fun handleApi(e: ApiException): ResponseEntity<ApiError> =
        ResponseEntity.status(e.status).body(ApiError(e.message ?: "Terjadi kesalahan"))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val fields = e.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Tidak valid") }
        val message = fields.values.joinToString("; ").ifEmpty { "Data tidak valid" }
        return ResponseEntity.badRequest().body(ApiError(message, fields))
    }

    // Body JSON tidak terbaca / nilai enum tidak dikenal -> 400, bukan 500.
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadable(e: HttpMessageNotReadableException): ResponseEntity<ApiError> {
        org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
            .warn("Unreadable body: {}", e.mostSpecificCause.message)
        return ResponseEntity.badRequest().body(ApiError("Body permintaan tidak valid"))
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleDenied(e: AccessDeniedException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError("Forbidden"))

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleUploadSize(e: MaxUploadSizeExceededException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError("Ukuran foto maksimal 10 MB"))

    // Pelanggaran constraint DB (mis. kode/username duplikat karena race check-then-insert,
    // atau referensi wilayah yang tidak ada). Pesan constraint asli tidak dibocorkan ke klien,
    // tapi dibedakan berdasarkan SQLState PostgreSQL agar pesannya tidak menyesatkan.
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleIntegrity(e: DataIntegrityViolationException): ResponseEntity<ApiError> {
        org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
            .warn("Data integrity violation [{}]: {}", sqlState(e), e.mostSpecificCause.message)
        return when (sqlState(e)) {
            // foreign_key_violation: desa/kelompok yang dirujuk tidak ada.
            "23503" -> ResponseEntity.badRequest()
                .body(ApiError("Data terkait tidak ditemukan (mis. desa atau kelompok tani)"))
            // unique_violation: duplikat.
            "23505" -> ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError("Data sudah ada (duplikat)"))
            else -> ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError("Data tidak dapat disimpan"))
        }
    }

    /** SQLState dari cause rantai exception (PostgreSQL), null bila tak ada. */
    private fun sqlState(e: Throwable): String? {
        var cur: Throwable? = e
        while (cur != null) {
            if (cur is java.sql.SQLException) return cur.sqlState
            cur = cur.cause
        }
        return null
    }

    // Exception bawaan Spring (mis. NoResourceFoundException -> 404, HttpRequestMethodNotSupported
    // -> 405) sudah membawa status sendiri. Tanpa cabang ini, catch-all di bawah menelannya jadi 500.
    @ExceptionHandler(Exception::class)
    fun handleGeneric(e: Exception): ResponseEntity<ApiError> {
        if (e is ErrorResponse) {
            return ResponseEntity.status(e.statusCode).body(ApiError(e.body.detail ?: "Terjadi kesalahan"))
        }
        org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler::class.java).error("Unhandled error", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError("Terjadi kesalahan pada server"))
    }
}

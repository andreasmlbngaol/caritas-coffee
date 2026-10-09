package id.caritas_kopi.be.common

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
    fun handleUnreadable(e: HttpMessageNotReadableException): ResponseEntity<ApiError> =
        ResponseEntity.badRequest().body(ApiError("Body permintaan tidak valid"))

    @ExceptionHandler(AccessDeniedException::class)
    fun handleDenied(e: AccessDeniedException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError("Forbidden"))

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleUploadSize(e: MaxUploadSizeExceededException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError("Ukuran foto maksimal 10 MB"))

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

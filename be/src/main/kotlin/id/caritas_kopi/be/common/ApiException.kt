package id.caritas_kopi.be.common

import org.springframework.http.HttpStatus

/** Error aplikasi dengan status HTTP eksplisit. Pesan ditampilkan ke pengguna. */
class ApiException(val status: HttpStatus, message: String) : RuntimeException(message) {
    companion object {
        fun badRequest(message: String) = ApiException(HttpStatus.BAD_REQUEST, message)
        fun unauthorized(message: String = "Unauthorized") = ApiException(HttpStatus.UNAUTHORIZED, message)
        fun forbidden(message: String = "Forbidden") = ApiException(HttpStatus.FORBIDDEN, message)
        fun notFound(message: String = "Data tidak ditemukan") = ApiException(HttpStatus.NOT_FOUND, message)
        fun conflict(message: String) = ApiException(HttpStatus.CONFLICT, message)
        fun tooManyRequests(message: String = "Terlalu banyak permintaan") =
            ApiException(HttpStatus.TOO_MANY_REQUESTS, message)
    }
}

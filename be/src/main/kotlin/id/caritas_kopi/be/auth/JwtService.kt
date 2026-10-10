package id.caritas_kopi.be.auth

import id.caritas_kopi.be.config.AppProperties
import id.caritas_kopi.be.user.Role
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class JwtService(private val props: AppProperties) {

    private val key: SecretKey by lazy {
        val raw = props.jwt.secret.toByteArray(StandardCharsets.UTF_8)
        require(raw.size >= 32) { "JWT_SECRET harus minimal 32 byte" }
        Keys.hmacShaKeyFor(raw)
    }

    fun generate(userId: UUID, role: Role, tokenVersion: Int): String {
        val now = Instant.now()
        return Jwts.builder()
            .subject(userId.toString())
            .claim("role", role.name)
            .claim("ver", tokenVersion)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(props.jwt.ttlSeconds)))
            .signWith(key)
            .compact()
    }

    /** Kembalikan (userId, tokenVersion) bila token valid, else null. */
    fun parse(token: String): Pair<UUID, Int>? = try {
        val claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
        val id = UUID.fromString(claims.subject)
        val ver = (claims["ver"] as? Number)?.toInt() ?: 0
        id to ver
    } catch (_: Exception) {
        null
    }
}

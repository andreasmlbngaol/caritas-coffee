package id.caritas_kopi.be.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

enum class Role { ADMIN, ENUMERATOR }

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false, unique = true)
    var username: String = "",

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String = "",

    @Column(name = "full_name")
    var fullName: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.ENUMERATOR,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "last_login_at")
    var lastLoginAt: Instant? = null,

    /** Dinaikkan saat reset password / (de)aktivasi -> membatalkan token lama. */
    @Column(name = "token_version", nullable = false)
    var tokenVersion: Int = 0,
)

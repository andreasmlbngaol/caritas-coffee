package id.caritas_kopi.be.auth

import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Pembatas percobaan login gagal (anti brute-force) - fixed window sederhana.
 * Key = username + IP klien, sehingga satu sumber dibatasi per akun tanpa membuat
 * akun lain bisa dikunci dari sumber berbeda.
 *
 * ponytail: state in-memory per-instance. Cukup untuk 1 VPS single-instance;
 * kalau nanti multi-instance, pindah ke Redis/bucket bersama.
 */
@Component
class LoginThrottle(
    private val maxAttempts: Int = 10,
    private val window: Duration = Duration.ofMinutes(15),
) {
    private data class Attempts(var count: Int, var windowStart: Instant)

    // ponytail: batas ukuran map supaya penyerang yang memutar username tak bikin
    // memory tak terbatas; prune entri kedaluwarsa saat map membengkak.
    private val maxEntries = 10_000
    private val attempts = ConcurrentHashMap<String, Attempts>()

    /** True bila key sudah melewati batas pada window berjalan. */
    fun isBlocked(key: String): Boolean {
        val now = Instant.now()
        val a = attempts[key] ?: return false
        if (Duration.between(a.windowStart, now) > window) {
            attempts.remove(key)
            return false
        }
        return a.count >= maxAttempts
    }

    /** Catat satu percobaan gagal. */
    fun recordFailure(key: String) {
        val now = Instant.now()
        if (attempts.size >= maxEntries) prune(now)
        attempts.compute(key) { _, cur ->
            if (cur == null || Duration.between(cur.windowStart, now) > window) {
                Attempts(1, now)
            } else {
                cur.count += 1
                cur
            }
        }
    }

    /** Hapus catatan setelah login sukses. */
    fun reset(key: String) {
        attempts.remove(key)
    }

    /** Bersihkan semua catatan (dipakai test). */
    fun clear() {
        attempts.clear()
    }

    private fun prune(now: Instant) {
        attempts.entries.removeIf { Duration.between(it.value.windowStart, now) > window }
    }
}

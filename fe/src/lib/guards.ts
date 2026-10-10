// Penjaga (guard) kecil yang murni & tanpa dependensi - mudah diuji.
// Diletakkan terpisah agar tidak menyeret alias/import lain saat di-unit-test.

/** Path internal yang aman untuk redirect; selain itu → fallback "/".
 *  Menolak `//evil.com` (protocol-relative) selain path relatif biasa. */
export function internalPath(cb: string | null | undefined, fallback = "/"): string {
  return cb && /^\/(?!\/)/.test(cb) ? cb : fallback;
}

/** Nilai enum dari form hanya diteruskan bila termasuk daftar yang dikenal. */
export function enumOrNull<T extends string>(
  v: string | undefined,
  allowed: readonly T[],
): T | null {
  return v && (allowed as readonly string[]).includes(v) ? (v as T) : null;
}

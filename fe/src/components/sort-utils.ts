// Helper sorting tabel (client-side) - non-komponen, dipisah dari SortHeader
// agar Fast Refresh tetap bersih (file komponen hanya mengekspor komponen).
export type SortDir = "asc" | "desc";

export function parseSort<T extends string>(
  params: URLSearchParams,
  allowed: readonly T[],
  fallback: T,
  fallbackDir: SortDir = "asc",
): { sort: T; dir: SortDir } {
  const raw = params.get("sort") ?? "";
  const sort = (allowed as readonly string[]).includes(raw) ? (raw as T) : fallback;
  const d = params.get("dir");
  const dir: SortDir = d === "asc" || d === "desc" ? d : fallbackDir;
  return { sort, dir };
}

/** Nilai untuk dibandingkan dari sebuah baris, berdasarkan kolom. */
export function compareRows<C extends string>(
  a: unknown,
  b: unknown,
  get: (row: unknown, column: C) => string | number | null | undefined,
  column: C,
): number {
  const va = get(a, column);
  const vb = get(b, column);
  if (va == null && vb == null) return 0;
  if (va == null) return 1;
  if (vb == null) return -1;
  if (typeof va === "number" && typeof vb === "number") return va - vb;
  return String(va).localeCompare(String(vb), "id", { sensitivity: "base" });
}

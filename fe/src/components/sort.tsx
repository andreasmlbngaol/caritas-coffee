// Header kolom tabel yang bisa diklik untuk sorting (client-side).
// Urutan sort/dir disimpan di query string agar bisa di-bookmark.
import { Link, useLocation } from "react-router";
import { ArrowDown, ArrowUp, ChevronsUpDown } from "lucide-react";

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
export function compareRows(
  a: unknown,
  b: unknown,
  get: (row: unknown, column: string) => string | number | null | undefined,
  column: string,
): number {
  const va = get(a, column);
  const vb = get(b, column);
  if (va == null && vb == null) return 0;
  if (va == null) return 1;
  if (vb == null) return -1;
  if (typeof va === "number" && typeof vb === "number") return va - vb;
  return String(va).localeCompare(String(vb), "id", { sensitivity: "base" });
}

export function SortHeader({
  column,
  label,
  sort,
  dir,
  basePath,
}: {
  column: string;
  label: string;
  sort: string;
  dir: SortDir;
  basePath: string;
}) {
  const location = useLocation();
  const active = sort === column;
  // Kolom aktif: klik = balik arah. Kolom lain: mulai dari asc.
  const nextDir: SortDir = active && dir === "asc" ? "desc" : "asc";

  const qs = new URLSearchParams(location.search);
  qs.set("sort", column);
  qs.set("dir", nextDir);

  const Icon = active ? (dir === "asc" ? ArrowUp : ArrowDown) : ChevronsUpDown;

  return (
    <th
      aria-sort={active ? (dir === "asc" ? "ascending" : "descending") : "none"}
      className="px-5 py-3.5"
    >
      <Link
        to={`${basePath}?${qs.toString()}`}
        className={`inline-flex items-center gap-1.5 transition-colors hover:text-gray-900 ${
          active ? "text-gray-900" : ""
        }`}
      >
        {label}
        <Icon size={13} className={active ? "text-jade-800" : "text-gray-400"} aria-hidden />
      </Link>
    </th>
  );
}

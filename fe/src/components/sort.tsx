// Header kolom tabel yang bisa diklik untuk sorting (client-side).
// Urutan sort/dir disimpan di query string agar bisa di-bookmark.
import { Link, useLocation } from "react-router";
import { ArrowDown, ArrowUp, ChevronsUpDown } from "lucide-react";
import type { SortDir } from "./sort-utils";

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

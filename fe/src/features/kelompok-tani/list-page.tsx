import { useMemo } from "react";
import { Link, useSearchParams } from "react-router";
import { Plus, Pencil, Search } from "lucide-react";
import { pageWide, ListSkeleton } from "@/components/ui";
import { SortHeader, parseSort, compareRows } from "@/components/sort";
import { KelompokDeleteButton } from "./delete-button";
import { useKelompokList } from "./queries";
import type { KelompokTaniDto } from "@/api/types";

const SORT_COLUMNS = ["nama", "kode", "desa", "jumlahPetani"] as const;
type SortColumn = (typeof SORT_COLUMNS)[number];

function rowValue(row: KelompokTaniDto, column: string): string | number | null {
  switch (column) {
    case "nama":
      return row.nama;
    case "kode":
      return row.kode;
    case "desa":
      return row.desa;
    case "jumlahPetani":
      return row.jumlahPetani;
    default:
      return null;
  }
}

export function KelompokTaniListPage() {
  const [params, setParams] = useSearchParams();
  const q = params.get("q") ?? "";
  const { sort, dir } = parseSort<SortColumn>(params, SORT_COLUMNS, "kode");

  const { data, isPending } = useKelompokList();

  const items = useMemo(() => {
    const needle = q.trim().toLowerCase();
    const base = needle
      ? (data ?? []).filter(
          (k) =>
            k.nama.toLowerCase().includes(needle) ||
            (k.kode ?? "").toLowerCase().includes(needle) ||
            k.desa.toLowerCase().includes(needle),
        )
      : (data ?? []);
    const sorted = [...base].sort((a, b) => compareRows(a, b, (r, c) => rowValue(r as KelompokTaniDto, c), sort));
    return dir === "desc" ? sorted.reverse() : sorted;
  }, [data, q, sort, dir]);

  if (isPending) return <ListSkeleton />;

  return (
    <main className={pageWide}>
      <header className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-lg font-semibold tracking-tight">Kelompok Tani</h1>
          <p className="mt-1 text-sm text-gray-500">
            Master kelompok tani per desa. Dibuat juga otomatis dari form petani.
          </p>
        </div>
        <Link
          to="/kelompok-tani/baru"
          className="flex shrink-0 items-center gap-2 rounded-xl bg-jade-800 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-jade-900"
        >
          <Plus size={16} /> Tambah Kelompok
        </Link>
      </header>

      <form
        className="mt-6 flex gap-2"
        onSubmit={(e) => {
          e.preventDefault();
          const next = new URLSearchParams(params);
          next.set("q", String(new FormData(e.currentTarget).get("q") ?? ""));
          setParams(next);
        }}
      >
        <div className="relative flex-1 sm:max-w-sm">
          <Search size={16} className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-500" />
          <input
            name="q"
            defaultValue={q}
            placeholder="Cari nama / kode / desa…"
            className="w-full rounded-xl bg-white py-2.5 pl-10 pr-3 text-sm ring-1 ring-inset ring-gray-300 outline-none transition placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-jade-700"
          />
        </div>
        <button type="submit" className="rounded-xl bg-gray-900 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-gray-800">
          Cari
        </button>
      </form>

      {items.length === 0 ? (
        <div className="mt-8 rounded-2xl bg-white p-10 text-center shadow-sm ring-1 ring-gray-950/5">
          <p className="text-sm text-gray-500">
            {q ? "Tidak ada hasil untuk pencarian ini." : "Belum ada kelompok tani."}
          </p>
        </div>
      ) : (
        <div className="mt-6 overflow-x-auto rounded-2xl bg-white shadow-sm ring-1 ring-gray-950/5">
          <table className="w-full min-w-[640px] text-sm">
            <thead>
              <tr className="text-left text-[11px] font-semibold uppercase tracking-wider text-gray-500">
                <SortHeader column="nama" label="Nama Kelompok" sort={sort} dir={dir} basePath="/kelompok-tani" />
                <SortHeader column="kode" label="Kode" sort={sort} dir={dir} basePath="/kelompok-tani" />
                <SortHeader column="desa" label="Desa" sort={sort} dir={dir} basePath="/kelompok-tani" />
                <SortHeader column="jumlahPetani" label="Jumlah Petani" sort={sort} dir={dir} basePath="/kelompok-tani" />
                <th className="px-5 py-3.5 text-right">Aksi</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {items.map((k) => (
                <tr key={k.id} className="transition-colors hover:bg-gray-50/50">
                  <td className="px-5 py-3.5 font-medium text-gray-900">{k.nama}</td>
                  <td className="whitespace-nowrap px-5 py-3.5 text-gray-500">{k.kode ?? "-"}</td>
                  <td className="px-5 py-3.5 text-gray-500">
                    {k.desa}
                    {k.kecamatan && (
                      <span className="block text-xs text-gray-500">Kec. {k.kecamatan}</span>
                    )}
                  </td>
                  <td className="px-5 py-3.5 text-gray-500">{k.jumlahPetani}</td>
                  <td className="px-5 py-3.5">
                    <div className="flex items-center justify-end gap-1">
                      <Link
                        to={`/kelompok-tani/${k.id}/edit`}
                        title="Edit"
                        className="rounded-lg p-2 text-gray-500 transition-colors hover:bg-gray-100 hover:text-jade-800"
                      >
                        <Pencil size={16} />
                      </Link>
                      <KelompokDeleteButton id={k.id} nama={k.nama} />
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </main>
  );
}

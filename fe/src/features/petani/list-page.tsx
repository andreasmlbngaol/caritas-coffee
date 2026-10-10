import { useMemo } from "react";
import { Link, useSearchParams } from "react-router";
import { Plus, FileText, FileDown, Pencil, Search, ChevronLeft, ChevronRight } from "lucide-react";
import { pageWide, ListSkeleton } from "@/components/ui";
import { ErrorState } from "@/components/error-boundary";
import { SortHeader } from "@/components/sort";
import { parseSort, compareRows } from "@/components/sort-utils";
import { useAuth } from "@/features/auth/use-auth";
import { PetaniDeleteButton } from "./delete-button";
import { usePetaniList } from "./queries";
import type { PetaniListDto } from "@/api/types";

const PER_PAGE = 10;

const SORT_COLUMNS = ["namaLengkap", "kodePetani", "desa", "kelompokTani", "createdBy"] as const;
type SortColumn = (typeof SORT_COLUMNS)[number];

function rowValue(row: PetaniListDto, column: SortColumn): string | number | null {
  switch (column) {
    case "namaLengkap":
      return row.namaLengkap;
    case "kodePetani":
      return row.kodePetani;
    case "desa":
      return row.desa;
    case "kelompokTani":
      return row.kelompokTani;
    case "createdBy":
      return row.createdBy;
    default:
      return null;
  }
}

export function PetaniListPage() {
  const { user } = useAuth();
  const isAdmin = user?.role === "ADMIN";
  const [params, setParams] = useSearchParams();

  const q = params.get("q") ?? "";
  const pageNum = Math.max(1, Number(params.get("page")) || 1);
  const { sort, dir } = parseSort<SortColumn>(params, SORT_COLUMNS, "namaLengkap");

  const { data, isPending, isError, refetch } = usePetaniList();

  const filtered = useMemo(() => {
    const rows = data ?? [];
    const needle = q.trim().toLowerCase();
    const base = needle
      ? rows.filter(
          (p) =>
            p.namaLengkap.toLowerCase().includes(needle) ||
            (p.kodePetani ?? "").toLowerCase().includes(needle) ||
            (p.kelompokTaniKode ?? "").toLowerCase().includes(needle),
        )
      : rows;
    const sorted = [...base].sort((a, b) => compareRows(a, b, (r, c) => rowValue(r as PetaniListDto, c), sort));
    return dir === "desc" ? sorted.reverse() : sorted;
  }, [data, q, sort, dir]);

  const total = filtered.length;
  const totalPages = Math.max(1, Math.ceil(total / PER_PAGE));
  const page = Math.min(pageNum, totalPages);
  const items = filtered.slice((page - 1) * PER_PAGE, page * PER_PAGE);

  function pageUrl(p: number) {
    const next = new URLSearchParams(params);
    next.set("page", String(p));
    return `/petani?${next.toString()}`;
  }

  function goToPage(p: number) {
    const next = new URLSearchParams(params);
    next.set("page", String(Math.min(Math.max(1, p), totalPages)));
    setParams(next);
  }

  if (isPending) return <ListSkeleton />;
  if (isError) return <ErrorState message="Gagal memuat data petani." onRetry={() => void refetch()} />;

  return (
    <main className={pageWide}>
      <header className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-lg font-semibold tracking-tight">Data Petani</h1>
          {isAdmin && <p className="mt-1 text-sm text-gray-500">Semua data dari seluruh enumerator.</p>}
        </div>
        <Link
          to="/petani/baru"
          className="flex shrink-0 items-center gap-2 rounded-xl bg-jade-800 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-jade-900"
        >
          <Plus size={16} /> Input Data
        </Link>
      </header>

      <form
        className="mt-6 flex gap-2"
        onSubmit={(e) => {
          e.preventDefault();
          const next = new URLSearchParams(params);
          next.set("q", String(new FormData(e.currentTarget).get("q") ?? ""));
          next.set("page", "1");
          setParams(next);
        }}
      >
        <div className="relative flex-1 sm:max-w-sm">
          <Search size={16} className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-500" />
          <input
            name="q"
            defaultValue={q}
            aria-label="Cari petani"
            placeholder="Cari nama / kode petani / kode kelompok…"
            className="w-full rounded-xl bg-white py-2.5 pl-10 pr-3 text-sm ring-1 ring-inset ring-gray-300 outline-none transition placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-jade-700"
          />
        </div>
        <button
          type="submit"
          className="rounded-xl bg-gray-900 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-gray-800"
        >
          Cari
        </button>
      </form>

      {items.length === 0 ? (
        <div className="mt-8 rounded-2xl bg-white p-10 text-center shadow-sm ring-1 ring-gray-950/5">
          <p className="text-sm text-gray-500">
            {q ? "Tidak ada hasil untuk pencarian ini." : 'Belum ada data. Mulai dengan tombol "Input Data".'}
          </p>
        </div>
      ) : (
        <>
          <div className="mt-6 overflow-x-auto rounded-2xl bg-white shadow-sm ring-1 ring-gray-950/5">
            <table className="w-full min-w-[720px] text-sm">
              <thead>
                <tr className="text-left text-[11px] font-semibold uppercase tracking-wider text-gray-500">
                  <SortHeader column="namaLengkap" label="Nama" sort={sort} dir={dir} basePath="/petani" />
                  <SortHeader column="kodePetani" label="Kode Petani" sort={sort} dir={dir} basePath="/petani" />
                  <SortHeader column="desa" label="Desa" sort={sort} dir={dir} basePath="/petani" />
                  <SortHeader column="kelompokTani" label="Kelompok" sort={sort} dir={dir} basePath="/petani" />
                  {isAdmin && <SortHeader column="createdBy" label="Penginput" sort={sort} dir={dir} basePath="/petani" />}
                  <th className="px-5 py-3.5 text-right">Aksi</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {items.map((p) => (
                  <tr key={p.id} className="transition-colors hover:bg-gray-50/50">
                    <td className="px-5 py-3.5 font-medium">
                      <Link
                        to={`/petani/${p.id}`}
                        className="text-gray-900 underline-offset-2 transition-colors hover:text-jade-800 hover:underline"
                      >
                        {p.namaLengkap}
                      </Link>
                    </td>
                    <td className="whitespace-nowrap px-5 py-3.5 text-gray-500">{p.kodePetani ?? "-"}</td>
                    <td className="px-5 py-3.5 text-gray-500">
                      {p.desa}
                      <span className="block text-xs text-gray-500">Kec. {p.kecamatan}</span>
                    </td>
                    <td className="px-5 py-3.5 text-gray-500">
                      {p.kelompokTani ?? "-"}
                      {p.kelompokTaniKode && (
                        <span className="block text-xs text-gray-500">{p.kelompokTaniKode}</span>
                      )}
                    </td>
                    {isAdmin && <td className="px-5 py-3.5 text-gray-500">{p.createdBy ?? "-"}</td>}
                    <td className="px-5 py-3.5">
                      <div className="flex items-center justify-end gap-1">
                        <a
                          href={`/api/petani/${p.id}/export/pdf`}
                          title="Unduh PDF"
                          className="rounded-lg p-2 text-red-500 transition-colors hover:bg-red-50"
                        >
                          <FileDown size={16} />
                        </a>
                        <a
                          href={`/api/petani/${p.id}/export/docx`}
                          title="Unduh Word"
                          className="rounded-lg p-2 text-blue-500 transition-colors hover:bg-blue-50"
                        >
                          <FileText size={16} />
                        </a>
                        <Link
                          to={`/petani/${p.id}/edit`}
                          title="Edit"
                          className="rounded-lg p-2 text-gray-500 transition-colors hover:bg-gray-100 hover:text-jade-800"
                        >
                          <Pencil size={16} />
                        </Link>
                        <PetaniDeleteButton id={p.id} nama={p.namaLengkap} />
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {totalPages > 1 && (
            <div className="mt-4 flex items-center justify-between text-sm text-gray-500">
              <span>
                {total} data - halaman {page} dari {totalPages}
              </span>
              <div className="flex items-center gap-2">
                {page > 1 && (
                  <Link to={pageUrl(page - 1)} className="rounded-lg p-2 transition-colors hover:bg-white hover:text-gray-900" aria-label="Halaman sebelumnya">
                    <ChevronLeft size={16} />
                  </Link>
                )}
                <form
                  onSubmit={(e) => {
                    e.preventDefault();
                    const n = Number(new FormData(e.currentTarget).get("page"));
                    if (Number.isFinite(n)) goToPage(Math.trunc(n));
                  }}
                >
                  <input
                    key={page}
                    name="page"
                    type="number"
                    min={1}
                    max={totalPages}
                    defaultValue={page}
                    aria-label="Nomor halaman"
                    className="w-14 rounded-lg border border-gray-300 bg-white px-2 py-1 text-center text-gray-900 outline-none focus:border-jade-500"
                  />
                </form>
                {page < totalPages && (
                  <Link to={pageUrl(page + 1)} className="rounded-lg p-2 transition-colors hover:bg-white hover:text-gray-900" aria-label="Halaman berikutnya">
                    <ChevronRight size={16} />
                  </Link>
                )}
              </div>
            </div>
          )}
        </>
      )}
    </main>
  );
}

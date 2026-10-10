import { useMemo } from "react";
import { Link, useSearchParams } from "react-router";
import { Plus, FileText, FileDown, Pencil } from "lucide-react";
import { pageWide, ListSkeleton } from "@/components/ui";
import { ErrorState } from "@/components/error-boundary";
import { SortHeader } from "@/components/sort";
import { parseSort, compareRows } from "@/components/sort-utils";
import { useAuth } from "@/features/auth/use-auth";
import { DesaDeleteButton } from "./delete-button";
import { useDesaList } from "./queries";
import type { DesaListDto } from "@/api/types";

const SORT_COLUMNS = ["desa", "kecamatan", "createdBy"] as const;
type SortColumn = (typeof SORT_COLUMNS)[number];

function rowValue(row: DesaListDto, column: SortColumn): string | null {
  switch (column) {
    case "desa":
      return row.desa;
    case "kecamatan":
      return row.kecamatan;
    case "createdBy":
      return row.createdBy;
    default:
      return null;
  }
}

export function DesaListPage() {
  const { user } = useAuth();
  const isAdmin = user?.role === "ADMIN";
  const [params] = useSearchParams();
  const { sort, dir } = parseSort<SortColumn>(params, SORT_COLUMNS, "desa");

  const { data, isPending, isError, refetch } = useDesaList();

  const items = useMemo(() => {
    const sorted = [...(data ?? [])].sort((a, b) =>
      compareRows(a, b, (r, c) => rowValue(r as DesaListDto, c), sort),
    );
    return dir === "desc" ? sorted.reverse() : sorted;
  }, [data, sort, dir]);

  if (isPending) return <ListSkeleton />;
  if (isError) return <ErrorState message="Gagal memuat data desa." onRetry={() => void refetch()} />;

  return (
    <main className={pageWide}>
      <header className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-lg font-semibold tracking-tight">Data Desa</h1>
          {isAdmin && <p className="mt-1 text-sm text-gray-500">Semua data dari seluruh enumerator.</p>}
        </div>
        <Link
          to="/desa/baru"
          className="flex shrink-0 items-center gap-2 rounded-xl bg-jade-800 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-jade-900"
        >
          <Plus size={16} /> Input Data
        </Link>
      </header>

      {items.length === 0 ? (
        <div className="mt-8 rounded-2xl bg-white p-10 text-center shadow-sm ring-1 ring-gray-950/5">
          <p className="text-sm text-gray-500">
            Belum ada data. Mulai dengan tombol &#34;Input Data&#34;.
          </p>
        </div>
      ) : (
        <div className="mt-8 overflow-x-auto rounded-2xl bg-white shadow-sm ring-1 ring-gray-950/5">
          <table className="w-full min-w-[560px] text-sm">
            <thead>
              <tr className="text-left text-[11px] font-semibold uppercase tracking-wider text-gray-500">
                <SortHeader column="desa" label="Desa" sort={sort} dir={dir} basePath="/desa" />
                <SortHeader column="kecamatan" label="Kecamatan" sort={sort} dir={dir} basePath="/desa" />
                {isAdmin && <SortHeader column="createdBy" label="Penginput" sort={sort} dir={dir} basePath="/desa" />}
                <th className="px-5 py-3.5 text-right">Aksi</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {items.map((d) => (
                <tr key={d.id} className="transition-colors hover:bg-gray-50/50">
                  <td className="px-5 py-3.5 font-medium">
                    <Link
                      to={`/desa/${d.id}`}
                      className="text-gray-900 underline-offset-2 transition-colors hover:text-jade-800 hover:underline"
                    >
                      {d.desa}
                    </Link>
                  </td>
                  <td className="px-5 py-3.5 text-gray-500">{d.kecamatan}</td>
                  {isAdmin && <td className="px-5 py-3.5 text-gray-500">{d.createdBy ?? "-"}</td>}
                  <td className="px-5 py-3.5">
                    <div className="flex items-center justify-end gap-1">
                      <a
                        href={`/api/desa/${d.id}/export/pdf`}
                        title="Unduh PDF"
                        className="rounded-lg p-2 text-red-500 transition-colors hover:bg-red-50"
                      >
                        <FileDown size={16} />
                      </a>
                      <a
                        href={`/api/desa/${d.id}/export/docx`}
                        title="Unduh Word"
                        className="rounded-lg p-2 text-blue-500 transition-colors hover:bg-blue-50"
                      >
                        <FileText size={16} />
                      </a>
                      <Link
                        to={`/desa/${d.id}/edit`}
                        title="Edit"
                        className="rounded-lg p-2 text-gray-500 transition-colors hover:bg-gray-100 hover:text-jade-800"
                      >
                        <Pencil size={16} />
                      </Link>
                      <DesaDeleteButton id={d.id} nama={d.desa} />
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

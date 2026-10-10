import { Link, useParams } from "react-router";
import { ArrowLeft } from "lucide-react";
import { pageForm, ListSkeleton } from "@/components/ui";
import { ErrorState } from "@/components/error-boundary";
import { KelompokTaniForm } from "./form";
import { useKelompokList } from "./queries";

export function KelompokTaniBaruPage() {
  return (
    <main className={pageForm}>
      <header className="flex items-center gap-3">
        <Link
          to="/kelompok-tani"
          aria-label="Kembali ke daftar"
          className="rounded-xl p-2 text-gray-500 transition-colors hover:bg-white hover:text-gray-900 hover:shadow-sm hover:ring-1 hover:ring-gray-950/5"
        >
          <ArrowLeft size={18} />
        </Link>
        <h1 className="text-lg font-semibold tracking-tight">Tambah Kelompok Tani</h1>
      </header>
      <div className="mt-8">
        <KelompokTaniForm />
      </div>
    </main>
  );
}

export function KelompokTaniEditPage() {
  const { id = "" } = useParams();
  const { data, isPending, isError, refetch } = useKelompokList();
  const kt = data?.find((k) => k.id === id);

  if (isPending) return <ListSkeleton />;
  if (isError) return <ErrorState message="Gagal memuat data kelompok tani." onRetry={() => void refetch()} />;
  if (!kt) return <ErrorState title="Data tidak ditemukan" message="Kelompok tani ini tidak ada atau sudah dihapus." />;

  return (
    <main className={pageForm}>
      <header className="flex items-center gap-3">
        <Link
          to="/kelompok-tani"
          aria-label="Kembali ke daftar"
          className="rounded-xl p-2 text-gray-500 transition-colors hover:bg-white hover:text-gray-900 hover:shadow-sm hover:ring-1 hover:ring-gray-950/5"
        >
          <ArrowLeft size={18} />
        </Link>
        <h1 className="text-lg font-semibold tracking-tight">Edit Kelompok Tani</h1>
      </header>
      <div className="mt-8">
        <KelompokTaniForm id={id} defaults={{ nama: kt.nama, kode: kt.kode, desaKode: kt.desaKode }} />
      </div>
    </main>
  );
}

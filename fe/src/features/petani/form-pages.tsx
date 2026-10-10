import { Link, useParams } from "react-router";
import { ArrowLeft } from "lucide-react";
import { pageForm, ListSkeleton } from "@/components/ui";
import { ErrorState } from "@/components/error-boundary";
import { PetaniForm } from "./form";
import { usePetaniDetail } from "./queries";

export function PetaniBaruPage() {
  return (
    <main className={pageForm}>
      <header className="flex items-center gap-3">
        <Link
          to="/petani"
          aria-label="Kembali ke daftar"
          className="rounded-xl p-2 text-gray-500 transition-colors hover:bg-white hover:text-gray-900 hover:shadow-sm hover:ring-1 hover:ring-gray-950/5"
        >
          <ArrowLeft size={18} />
        </Link>
        <h1 className="text-lg font-semibold tracking-tight">Formulir Data Baseline Petani</h1>
      </header>
      <div className="mt-8">
        <PetaniForm />
      </div>
    </main>
  );
}

export function PetaniEditPage() {
  const { id = "" } = useParams();
  const { data, isPending, isError, refetch } = usePetaniDetail(id);

  if (isPending) return <ListSkeleton />;
  if (isError) return <ErrorState message="Gagal memuat data petani." onRetry={() => void refetch()} />;
  if (!data) return <ErrorState title="Data tidak ditemukan" message="Data petani ini tidak ada atau sudah dihapus." />;

  return (
    <main className={pageForm}>
      <header className="flex items-center gap-3">
        <Link
          to="/petani"
          aria-label="Kembali ke daftar"
          className="rounded-xl p-2 text-gray-500 transition-colors hover:bg-white hover:text-gray-900 hover:shadow-sm hover:ring-1 hover:ring-gray-950/5"
        >
          <ArrowLeft size={18} />
        </Link>
        <h1 className="text-lg font-semibold tracking-tight">Edit Data Baseline Petani</h1>
      </header>
      <div className="mt-8">
        <PetaniForm id={id} defaults={data} />
      </div>
    </main>
  );
}

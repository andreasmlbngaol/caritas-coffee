import { Link, useParams } from "react-router";
import { ArrowLeft } from "lucide-react";
import { pageForm, ListSkeleton } from "@/components/ui";
import { DesaForm } from "./form";
import { useDesaDetail } from "./queries";

export function DesaBaruPage() {
  return (
    <main className={pageForm}>
      <header className="flex items-center gap-3">
        <Link
          to="/desa"
          aria-label="Kembali ke daftar"
          className="rounded-xl p-2 text-gray-500 transition-colors hover:bg-white hover:text-gray-900 hover:shadow-sm hover:ring-1 hover:ring-gray-950/5"
        >
          <ArrowLeft size={18} />
        </Link>
        <h1 className="text-lg font-semibold tracking-tight">Formulir Data Baseline Desa</h1>
      </header>
      <div className="mt-8">
        <DesaForm />
      </div>
    </main>
  );
}

export function DesaEditPage() {
  const { id = "" } = useParams();
  const { data, isPending } = useDesaDetail(id);

  if (isPending || !data) return <ListSkeleton />;

  return (
    <main className={pageForm}>
      <header className="flex items-center gap-3">
        <Link
          to="/desa"
          aria-label="Kembali ke daftar"
          className="rounded-xl p-2 text-gray-500 transition-colors hover:bg-white hover:text-gray-900 hover:shadow-sm hover:ring-1 hover:ring-gray-950/5"
        >
          <ArrowLeft size={18} />
        </Link>
        <h1 className="text-lg font-semibold tracking-tight">Edit Data Baseline Desa</h1>
      </header>
      <div className="mt-8">
        <DesaForm id={id} defaults={data} />
      </div>
    </main>
  );
}

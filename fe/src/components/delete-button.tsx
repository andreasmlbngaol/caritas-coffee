import { useState, type ReactNode } from "react";
import { Trash2 } from "lucide-react";
import { Modal } from "./modal";

// Tombol hapus + konfirmasi via Modal. `onConfirm` melempar bila gagal;
// pesan error ditampilkan di dalam modal. Navigasi/pengalihan dilakukan
// pemanggil setelah onConfirm sukses.
export function DeleteButton({
  onConfirm,
  title,
  message,
}: {
  onConfirm: () => Promise<void>;
  title: string;
  message: ReactNode;
}) {
  const [open, setOpen] = useState(false);
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function confirmDelete() {
    setPending(true);
    setError(null);
    try {
      await onConfirm();
      setOpen(false);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Gagal menghapus data");
    } finally {
      setPending(false);
    }
  }

  return (
    <>
      <button
        type="button"
        title="Hapus"
        aria-label="Hapus"
        onClick={() => setOpen(true)}
        className="rounded-lg p-2 text-gray-500 transition-colors hover:bg-red-50 hover:text-red-600"
      >
        <Trash2 size={16} />
      </button>

      <Modal
        open={open}
        onClose={() => !pending && setOpen(false)}
        title={title}
        closeOnBackdrop={!pending}
      >
        <p className="mt-2 text-sm text-gray-600">{message}</p>

        {error && (
          <p role="alert" className="mt-3 rounded-lg bg-red-50 px-3 py-2 text-xs text-red-600">
            {error}
          </p>
        )}

        <div className="mt-5 flex justify-end gap-2">
          <button
            type="button"
            onClick={() => setOpen(false)}
            disabled={pending}
            className="rounded-xl px-4 py-2 text-sm font-medium text-gray-600 transition-colors hover:bg-gray-100 disabled:opacity-50"
          >
            Batal
          </button>
          <button
            type="button"
            onClick={confirmDelete}
            disabled={pending}
            className="rounded-xl bg-red-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-red-700 disabled:opacity-50"
          >
            {pending ? "Menghapus..." : "Hapus"}
          </button>
        </div>
      </Modal>
    </>
  );
}

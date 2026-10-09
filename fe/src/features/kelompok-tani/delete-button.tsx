import { DeleteButton } from "@/components/delete-button";
import { useDeleteKelompok } from "./queries";

export function KelompokDeleteButton({ id, nama }: { id: string; nama: string }) {
  const del = useDeleteKelompok();
  return (
    <DeleteButton
      title="Hapus kelompok tani?"
      onConfirm={async () => {
        await del.mutateAsync(id);
      }}
      message={
        <>
          Kelompok Tani <strong>{nama}</strong> akan dihapus permanen. Kelompok yang masih dipakai
          petani tidak bisa dihapus.
        </>
      }
    />
  );
}

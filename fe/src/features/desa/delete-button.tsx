import { DeleteButton } from "@/components/delete-button";
import { useDeleteDesa } from "./queries";

export function DesaDeleteButton({ id, nama }: { id: string; nama: string }) {
  const del = useDeleteDesa();
  return (
    <DeleteButton
      title="Hapus data baseline?"
      onConfirm={async () => {
        await del.mutateAsync(id);
      }}
      message={
        <>
          Data baseline Desa <strong>{nama}</strong> beserta data kebijakan dan kelembagaannya akan
          dihapus permanen dan tidak bisa dikembalikan.
        </>
      }
    />
  );
}

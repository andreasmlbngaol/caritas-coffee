import { DeleteButton } from "@/components/delete-button";
import { useDeletePetani } from "./queries";

export function PetaniDeleteButton({ id, nama }: { id: string; nama: string }) {
  const del = useDeletePetani();
  return (
    <DeleteButton
      title="Hapus data petani?"
      onConfirm={async () => {
        await del.mutateAsync(id);
      }}
      message={
        <>
          Data baseline petani <strong>{nama}</strong> beserta data plot, praktik GAP, produksi, dan kondisi
          kebunnya akan dihapus permanen dan tidak bisa dikembalikan.
        </>
      }
    />
  );
}

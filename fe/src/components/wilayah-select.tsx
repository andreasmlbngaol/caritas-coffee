import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { WilayahDto } from "@/api/types";
import { Combobox } from "./combobox";

type Option = { value: string; label: string };
const LABELS = ["Provinsi", "Kabupaten/Kota", "Kecamatan", "Desa"];

const toOptions = (rows: WilayahDto[]): Option[] =>
  rows.map((r) => ({ value: r.kode, label: r.nama }));

// "11.01.02.2001" → ["11", "11.01", "11.01.02", "11.01.02.2001"]
function deriveLevels(desaKode: string): string[] {
  const p = desaKode.split(".");
  return [p[0] ?? "", p.slice(0, 2).join("."), p.slice(0, 3).join("."), desaKode];
}

/** Lookup cascading 4 level. parent=undefined → provinsi. */
export function useWilayah(parent: string | undefined, enabled: boolean) {
  return useQuery({
    queryKey: ["wilayah", parent ?? "root"],
    queryFn: () =>
      api.get<WilayahDto[]>(
        parent ? `/api/wilayah?parent=${encodeURIComponent(parent)}` : "/api/wilayah",
      ),
    enabled,
    staleTime: 5 * 60_000,
  });
}

export function WilayahSelect({
  defaultDesaKode,
  onDesaChange,
}: {
  defaultDesaKode?: string;
  onDesaChange?: (desaKode: string) => void; // opsional - dipakai form petani
}) {
  const initial = defaultDesaKode ? deriveLevels(defaultDesaKode) : ["", "", "", ""];
  const [selected, setSelected] = useState<string[]>(initial);

  const prov = useWilayah(undefined, true);
  const kab = useWilayah(selected[0] || undefined, !!selected[0]);
  const kec = useWilayah(selected[1] || undefined, !!selected[1]);
  const desa = useWilayah(selected[2] || undefined, !!selected[2]);

  const options: Option[][] = [
    toOptions(prov.data ?? []),
    toOptions(kab.data ?? []),
    toOptions(kec.data ?? []),
    toOptions(desa.data ?? []),
  ];

  function handleChange(level: number, kode: string) {
    setSelected((s) => s.map((v, i) => (i === level ? kode : i > level ? "" : v)));

    // Beritahu pemakai saat desa berubah (termasuk jadi kosong karena kecamatan diganti)
    if (level === 3 || (level < 3 && selected[3])) {
      onDesaChange?.(level === 3 ? kode : "");
    }
  }

  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
      {LABELS.map((label, i) => (
        <Combobox
          key={label}
          label={label}
          placeholder={`Pilih ${label}`}
          name={i === 3 ? "desaKode" : undefined}
          required={i === 3}
          options={options[i] ?? []}
          value={selected[i] ?? ""}
          onChange={(v) => handleChange(i, v)}
          disabled={i > 0 && !selected[i - 1]}
        />
      ))}
    </div>
  );
}

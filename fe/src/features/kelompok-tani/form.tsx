import { useState } from "react";
import { useNavigate } from "react-router";
import { Section, SubSection, Field, inputCls } from "@/components/ui";
import { SubmitButton } from "@/components/submit-button";
import { UnsavedGuard } from "@/components/unsaved-guard";
import { WilayahSelect } from "@/components/wilayah-select";
import { useCreateKelompok, useUpdateKelompok } from "./queries";
import { get } from "@/lib/form";
import type { KelompokTaniRequest } from "@/api/types";

export type KelompokTaniDefaults = {
  nama: string;
  kode: string | null;
  desaKode: string;
};

function buildRequest(fd: FormData): KelompokTaniRequest {
  const desaKode = get(fd, "desaKode");
  if (!desaKode) throw new Error("Desa wajib dipilih.");
  const nama = get(fd, "nama");
  if (!nama) throw new Error("Nama kelompok wajib diisi.");
  const k1 = get(fd, "kode1");
  const k2 = get(fd, "kode2");
  let kode: string | null = null;
  if (k1 || k2) {
    if (!k1 || !k2) throw new Error("Kode kelompok belum lengkap (2 bagian).");
    kode = `${k1}-${k2}`.toUpperCase();
  }
  return { nama, kode, desaKode };
}

export function KelompokTaniForm({ id, defaults }: { id?: string; defaults?: KelompokTaniDefaults }) {
  const navigate = useNavigate();
  const create = useCreateKelompok();
  const update = useUpdateKelompok(id ?? "");
  const mutation = id ? update : create;
  const [error, setError] = useState<string | null>(null);

  const seg = defaults?.kode?.split("-") ?? [];

  async function onSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    try {
      const req = buildRequest(new FormData(e.currentTarget));
      if (id) await update.mutateAsync(req);
      else await create.mutateAsync(req);
      navigate("/kelompok-tani");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Gagal menyimpan data");
    }
  }

  return (
    <form onSubmit={onSubmit} className="space-y-6">
      <UnsavedGuard />
      <Section title="Data Kelompok Tani">
        <div className="space-y-6">
          <SubSection title="Desa">
            <WilayahSelect defaultDesaKode={defaults?.desaKode} />
          </SubSection>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Field hint="Kelompok Tani [Nama]" label="Nama Kelompok Tani" name="nama" required defaultValue={defaults?.nama ?? ""} />
            <div>
              <span className="mb-1.5 block text-xs font-medium text-gray-600">Kode Kelompok Tani</span>
              <div className="flex items-center gap-2">
                <div>
                  <input
                    name="kode1"
                    placeholder="KR"
                    data-label="Kode kelompok bagian 1"
                    defaultValue={seg[0] ?? ""}
                    className={`${inputCls} w-24 uppercase`}
                  />
                  <p className="mt-1 text-[11px] text-gray-500">Kode wilayah</p>
                </div>
                <span className="pb-5 text-gray-500">-</span>
                <div>
                  <input
                    name="kode2"
                    placeholder="KR01"
                    data-label="Kode kelompok bagian 2"
                    defaultValue={seg.slice(1).join("-")}
                    className={`${inputCls} w-28 uppercase`}
                  />
                  <p className="mt-1 text-[11px] text-gray-500">Nomor urut</p>
                </div>
              </div>
              <p className="mt-1 text-[11px] text-gray-500">
                Contoh: KR-KR01. Boleh dikosongkan; kalau diisi harus unik per desa.
              </p>
            </div>
          </div>
        </div>
      </Section>

      {error && (
        <p role="alert" className="rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700 ring-1 ring-inset ring-red-200">
          {error}
        </p>
      )}

      <div className="flex justify-end pb-10">
        <SubmitButton label="Simpan Kelompok Tani" pending={mutation.isPending} />
      </div>
    </form>
  );
}

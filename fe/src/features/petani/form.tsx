import { useState } from "react";
import { useNavigate } from "react-router";
import { Section, SubSection, Grid, Field, inputCls } from "@/components/ui";
import { YesNoRow } from "@/components/yes-no";
import { SubmitButton } from "@/components/submit-button";
import { DatePicker } from "@/components/date-picker";
import { UnsavedGuard } from "@/components/unsaved-guard";
import { WilayahKelompokFields } from "./wilayah-kelompok-fields";
import { PlotFields } from "./plot-fields";
import { GapFields, type GapDefaults } from "./gap-fields";
import { ProduksiFields } from "./produksi-fields";
import { ProdukFields } from "./produk-fields";
import { PasarFields } from "./pasar-fields";
import { JENIS_KELAMIN, KONDISI_KEBUN } from "./constants";
import { buildPetaniRequest } from "./serialize";
import { useCreatePetani, useUpdatePetani } from "./queries";
import type { PetaniDetailDto } from "@/api/types";

const blankStr = (v: string | null | undefined) => (v == null || v === "-" ? "" : v);
const toDateInput = (d: string | null | undefined) => (d ? d.slice(0, 10) : "");
const CURRENT_YEAR = new Date().getFullYear();

export function PetaniForm({ id, defaults }: { id?: string; defaults?: PetaniDetailDto }) {
  const navigate = useNavigate();
  const create = useCreatePetani();
  const update = useUpdatePetani(id ?? "");
  const mutation = id ? update : create;
  const [error, setError] = useState<string | null>(null);

  const gapDefaults: GapDefaults | undefined = defaults
    ? Object.fromEntries(
        defaults.praktikGap.map((g) => [g.jenis, { jawaban: g.jawaban, keterangan: g.keterangan ?? "-" }]),
      )
    : undefined;

  const kd = (jenis: string) => defaults?.kondisiKebun.find((x) => x.jenis === jenis);

  async function onSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    const fd = new FormData(e.currentTarget);
    try {
      const req = buildPetaniRequest(fd);
      if (id) await update.mutateAsync(req);
      else await create.mutateAsync(req);
      navigate("/petani");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Gagal menyimpan data");
    }
  }

  return (
    <form onSubmit={onSubmit} className="space-y-6">
      <UnsavedGuard />
      {/* A - DATA IDENTITAS PETANI */}
      <Section title="A - Data Identitas Petani">
        <div className="space-y-6">
          <SubSection title="Wilayah & Kode">
            <WilayahKelompokFields
              defaultDesaKode={defaults?.desa.kode}
              defaultKelompokTaniId={defaults?.kelompokTani?.id ?? undefined}
              defaultKodePetani={defaults?.kodePetani}
            />
          </SubSection>
          <SubSection title="Identitas">
            <Grid>
              <Field label="Nama Lengkap (sesuai KTP)" name="namaLengkap" required defaultValue={blankStr(defaults?.namaLengkap)} />
              <Field label="Nama Panggilan" name="namaPanggilan" defaultValue={blankStr(defaults?.namaPanggilan)} />
              <div>
                <label htmlFor="jenisKelamin" className="mb-1.5 block text-xs font-medium text-gray-600">
                  Jenis Kelamin
                </label>
                <select
                  id="jenisKelamin"
                  name="jenisKelamin"
                  data-label="Jenis Kelamin"
                  defaultValue={defaults?.jenisKelamin ?? ""}
                  className={inputCls}
                >
                  <option value="">- pilih -</option>
                  {JENIS_KELAMIN.map((j) => (
                    <option key={j.value} value={j.value}>
                      {j.label}
                    </option>
                  ))}
                </select>
              </div>
              <DatePicker
                label="Tanggal Lahir"
                name="tanggalLahir"
                defaultValue={toDateInput(defaults?.tanggalLahir)}
                yearRange={[1940, CURRENT_YEAR]}
              />
              <Field label="Nomor Telepon / HP" name="telepon" defaultValue={blankStr(defaults?.telepon)} />
              <DatePicker
                label="Tanggal Pendaftaran"
                name="tanggalPendaftaran"
                defaultValue={toDateInput(defaults?.tanggalPendaftaran)}
              />
              <Field label="Nama Petugas Pendaftar" name="namaPetugasPendaftar" defaultValue={blankStr(defaults?.namaPetugasPendaftar)} />
              <div className="sm:col-span-2 lg:col-span-3">
                <Field label="Alamat Domisili" name="alamatDomisili" defaultValue={blankStr(defaults?.alamatDomisili)} />
              </div>
            </Grid>
          </SubSection>
          <SubSection title="Kontak Darurat">
            <Grid>
              <Field label="Nama Kontak Darurat" name="kontakDaruratNama" defaultValue={blankStr(defaults?.kontakDaruratNama)} />
              <Field label="No. Telepon / HP Kontak Darurat" name="kontakDaruratTelepon" defaultValue={blankStr(defaults?.kontakDaruratTelepon)} />
              <Field label="Hubungan dengan Kontak Darurat" name="kontakDaruratHubungan" defaultValue={blankStr(defaults?.kontakDaruratHubungan)} />
            </Grid>
          </SubSection>
        </div>
      </Section>

      {/* B - DATA FISIK LOKASI PLOT */}
      <Section title="B - Data Fisik Lokasi Plot">
        <PlotFields defaults={defaults?.plot} naunganDefaults={defaults?.naungan} />
      </Section>

      {/* C - PRAKTIK GAP KEBUN */}
      <Section title="C - Praktik GAP Kebun">
        <GapFields defaults={gapDefaults} />
      </Section>

      {/* D - RIWAYAT ESTIMASI PRODUKSI (kartu per tahun) */}
      <Section title="D - Riwayat Estimasi Produksi">
        <ProduksiFields defaults={defaults?.produksi} />
      </Section>

      {/* E - PENJUALAN */}
      <Section title="E - Penjualan">
        <div className="space-y-6">
          <SubSection title="E.1 - Jenis Produk yang Dijual">
            <ProdukFields defaults={defaults?.produk} />
          </SubSection>
          <SubSection title="E.2 - Kategori Pasar">
            <PasarFields defaults={defaults?.pasar} />
          </SubSection>
        </div>
      </Section>

      {/* F - KONDISI KEBUN */}
      <Section title="F - Kondisi Kebun">
        <div className="space-y-3">
          {KONDISI_KEBUN.map((k) => (
            <YesNoRow
              key={k.jenis}
              name={`kb_${k.jenis}`}
              label={k.label}
              defaultValue={kd(k.jenis)?.jawaban ?? false}
            >
              <input
                name={`kb_${k.jenis}_ket`}
                placeholder="Keterangan"
                data-requires={`kb_${k.jenis}`}
                data-label={`Keterangan ${k.label}`}
                defaultValue={blankStr(kd(k.jenis)?.keterangan)}
                className={inputCls}
              />
            </YesNoRow>
          ))}
        </div>
      </Section>

      {error && (
        <p role="alert" className="rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700 ring-1 ring-inset ring-red-200">
          {error}
        </p>
      )}

      <div className="flex justify-end pb-10">
        <SubmitButton pending={mutation.isPending} />
      </div>
    </form>
  );
}

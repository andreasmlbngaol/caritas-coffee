// Helper baca FormData untuk membangun payload JSON ke BE.
// Padanan helper get/getBool/hasAnyWithPrefix di proyek lama (actions.ts).

export function get(fd: FormData, key: string): string | undefined {
  const v = fd.get(key);
  if (v === null) return undefined;
  const s = String(v).trim();
  return s === "" ? undefined : s;
}

export function getBool(fd: FormData, key: string): boolean {
  return get(fd, key) === "true";
}

export function hasAnyWithPrefix(fd: FormData, prefix: string): boolean {
  for (const k of fd.keys()) if (k.startsWith(prefix)) return true;
  return false;
}

/** Angka: kosong/null → 0. */
export function num(v: FormDataEntryValue | null | undefined): number {
  if (v === null || v === undefined || v === "") return 0;
  const n = Number(v);
  return Number.isFinite(n) ? n : 0;
}

/** Angka opsional: kosong/null → undefined. */
export function numOpt(v: FormDataEntryValue | null | undefined): number | undefined {
  if (v === null || v === undefined || v === "") return undefined;
  const n = Number(v);
  return Number.isFinite(n) ? n : undefined;
}

/** Integer opsional: kosong/null → undefined. */
export function intOpt(v: FormDataEntryValue | null | undefined): number | undefined {
  if (v === null || v === undefined || v === "") return undefined;
  const n = Number(v);
  return Number.isInteger(n) ? n : undefined;
}

/** Teks ternormalisasi: kosong/null → "-" (placeholder). */
export function str(v: string | undefined): string {
  return v ?? "-";
}

/** Teks opsional: kosong/null → undefined. */
export function strOpt(v: string | undefined): string | undefined {
  return v;
}

/** Nilai kosong/placeholder → "". Untuk prefill form. */
export const blankStr = (v: string | null | undefined): string =>
  v == null || v === "-" ? "" : v;

/** 0 / kosong → "". Untuk prefill form angka. */
export const blankNum = (v: number | null | undefined): number | "" =>
  v == null || v === 0 ? "" : v;

/** Date (ISO dari BE) → "YYYY-MM-DD" untuk input. */
export const toDateInput = (d: string | null | undefined): string =>
  d ? d.slice(0, 10) : "";

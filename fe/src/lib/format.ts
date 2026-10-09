// Helper format id-ID. Padanan `analitik/_components/analytics-ui.tsx` (fmt)
// dan konstanta BULAN_ID proyek lama.

export const idNum = new Intl.NumberFormat("id-ID");

export const fmt = (v: number | string | null | undefined, digits = 0): string => {
  if (v == null || v === "") return "-";
  const n = typeof v === "number" ? v : Number(v);
  if (!Number.isFinite(n)) return String(v);
  return idNum.format(Number(n.toFixed(digits)));
};

export const BULAN_ID = [
  "Januari",
  "Februari",
  "Maret",
  "April",
  "Mei",
  "Juni",
  "Juli",
  "Agustus",
  "September",
  "Oktober",
  "November",
  "Desember",
] as const;

/** "2026-06" -> "Juni 2026". */
export function fmtBulanTahun(v: string | null | undefined): string {
  if (!v) return "-";
  const [y, m] = v.split("-");
  const bulan = BULAN_ID[Number(m) - 1];
  return bulan ? `${bulan} ${y}` : v;
}

/** ISO / Date -> "15 Juni 2026". */
export function fmtDate(v: string | Date | null | undefined): string {
  if (!v) return "-";
  const d = typeof v === "string" ? new Date(v) : v;
  if (Number.isNaN(d.getTime())) return "-";
  return d.toLocaleDateString("id-ID", {
    day: "numeric",
    month: "long",
    year: "numeric",
  });
}

/** Nilai kosong / placeholder "-" -> "". */
export const blankStr = (v: string | null | undefined): string =>
  v == null || v === "-" ? "" : v;

/** 0 / kosong -> "". */
export const blankNum = (v: number | null | undefined): number | "" =>
  v == null || v === 0 ? "" : v;

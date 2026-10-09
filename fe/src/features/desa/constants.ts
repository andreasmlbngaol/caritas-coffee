// Konstanta label & enum domain desa (padanan app/(main)/desa/constants.ts).
import type { JenisKebijakan, JenisLembaga } from "@/api/types";

export const KEBIJAKAN: { jenis: JenisKebijakan; label: string }[] = [
  { jenis: "RPJM_DESA", label: "RPJM Desa" },
  { jenis: "PERDES_PERTANIAN", label: "Perdes Pertanian" },
  { jenis: "PERDES_PERLINDUNGAN_HUTAN", label: "Perdes Perlindungan Hutan" },
  { jenis: "PROGRAM_PERKEMBANGAN_KOPI", label: "Program Perkembangan Kopi" },
  { jenis: "PROGRAM_KOPERASI", label: "Program Koperasi" },
  { jenis: "PROGRAM_PERHUTANAN_SOSIAL", label: "Program Perhutanan Sosial" },
];

export const LEMBAGA: { jenis: JenisLembaga; label: string }[] = [
  { jenis: "KELOMPOK_TANI", label: "Kelompok Tani" },
  { jenis: "GAPOKTAN", label: "Gapoktan" },
  { jenis: "KOPERASI", label: "Koperasi" },
  { jenis: "BUMDES", label: "BUMDes" },
  { jenis: "PENYULUH", label: "Penyuluh" },
  { jenis: "PENDAMPING", label: "Pendamping" },
];

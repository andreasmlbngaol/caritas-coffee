// Serialisasi FormData form petani → PetaniRequest (JSON).
// Padanan parseAll/plotRows/... di proyek lama (actions.ts). Normalisasi
// "-" untuk teks kosong & 0 untuk angka kosong dipertahankan persis.
import type { JawabanGap, JenisPraktikGap } from "@/api/types";
import { GAP_ITEMS, KONDISI_KEBUN, PRODUK, PASAR, TAHUN_PRODUKSI } from "./constants";
import type {
  GapRequest,
  KondisiRequest,
  NaunganRequest,
  PasarRequest,
  PetaniRequest,
  PlotRequest,
  ProduksiRequest,
  ProdukRequest,
} from "./types";
import { get, hasAnyWithPrefix, num } from "@/lib/form";

const JAWABAN_GAP_VALUES = ["YA", "TIDAK", "KADANG"] as const;
const SATUAN_VALUES = ["KG", "SOLUP", "BAMBU", "KALENG"] as const;

function getBool(fd: FormData, key: string): boolean {
  return get(fd, key) === "true";
}

/** Teks kosong → "-" (placeholder). */
const s = (v: string | undefined) => v ?? "-";
/** Teks kosong → undefined. */
const so = (v: string | undefined) => v;

// ---------- Plot ----------
const PLOT_FIELDS = [
  "namaHamparan",
  "elevasiMdpl",
  "kemiringanPersen",
  "luasKopiHa",
  "fotoKey",
  "fotoLatitude",
  "fotoLongitude",
  "statusKepemilikan",
  "sistemBudidaya",
  "areaKonservasi",
  "tanamanBaru",
  "pohonProduktif",
  "pohonTidakProduktif",
  "pestisidaNama",
  "pestisidaBulanTahun",
] as const;

function varietasGabung(fd: FormData, i: number): string {
  const list: string[] = [];
  for (let k = 0; ; k++) {
    if (fd.get(`plot_${i}_varietas_${k}`) === null) break;
    const v = get(fd, `plot_${i}_varietas_${k}`);
    if (v) list.push(v);
  }
  return list.length ? list.join(", ") : "-";
}

function plotRows(fd: FormData): PlotRequest[] {
  const rows: PlotRequest[] = [];
  for (let i = 0; ; i++) {
    if (!hasAnyWithPrefix(fd, `plot_${i}_`)) break;

    const yearPrefix = `plot_${i}_tahunTanam_`;
    const yearKeys = [...fd.keys()]
      .filter((key) => key.startsWith(yearPrefix))
      .sort((a, b) => Number(a.slice(yearPrefix.length)) - Number(b.slice(yearPrefix.length)));
    const years: number[] = [];
    for (const key of yearKeys) {
      const raw = get(fd, key);
      if (raw === undefined) continue;
      const n = Number(raw);
      if (Number.isInteger(n) && n >= 1900 && n <= new Date().getFullYear()) years.push(n);
    }
    const uniqueYears = [...new Set(years)].sort((a, b) => a - b);

    const raw: Record<string, string | undefined> = {};
    for (const f of PLOT_FIELDS) raw[f] = get(fd, `plot_${i}_${f}`);
    const varietas = varietasGabung(fd, i);
    const bermakna = Object.values(raw).some((v) => v !== undefined) || varietas !== "-" || uniqueYears.length > 0;
    if (!bermakna) continue;

    rows.push({
      namaHamparan: s(raw.namaHamparan),
      varietas,
      tahunTanam: uniqueYears,
      elevasiMdpl: num(raw.elevasiMdpl ?? null),
      kemiringanPersen: num(raw.kemiringanPersen ?? null),
      luasKopiHa: num(raw.luasKopiHa ?? null),
      fotoKey: so(raw.fotoKey),
      fotoLatitude: raw.fotoLatitude !== undefined ? Number(raw.fotoLatitude) : undefined,
      fotoLongitude: raw.fotoLongitude !== undefined ? Number(raw.fotoLongitude) : undefined,
      statusKepemilikan: raw.statusKepemilikan as PlotRequest["statusKepemilikan"],
      sistemBudidaya: raw.sistemBudidaya as PlotRequest["sistemBudidaya"],
      areaKonservasi: s(raw.areaKonservasi),
      tanamanBaru: num(raw.tanamanBaru ?? null),
      pohonProduktif: num(raw.pohonProduktif ?? null),
      pohonTidakProduktif: num(raw.pohonTidakProduktif ?? null),
      pestisidaNama: so(raw.pestisidaNama),
      pestisidaBulanTahun: so(raw.pestisidaBulanTahun),
    });
  }
  return rows;
}

// ---------- Naungan ----------
const NAUNGAN_FIELDS = ["jenis", "jumlah", "fungsi", "pemangkasan", "produksiPerTahun", "tahunTanam"] as const;

function naunganRows(fd: FormData): NaunganRequest[] {
  const rows: NaunganRequest[] = [];
  for (let i = 0; ; i++) {
    if (!hasAnyWithPrefix(fd, `naung_${i}_`)) break;
    const raw: Record<string, string | undefined> = {};
    for (const f of NAUNGAN_FIELDS) raw[f] = get(fd, `naung_${i}_${f}`);
    const bermakna = NAUNGAN_FIELDS.some((f) => f !== "pemangkasan" && raw[f] !== undefined);
    if (!bermakna) continue;
    rows.push({
      jenis: s(raw.jenis),
      jumlah: num(raw.jumlah ?? null),
      fungsi: s(raw.fungsi),
      pemangkasan: getBool(fd, `naung_${i}_pemangkasan`),
      produksiPerTahun: s(raw.produksiPerTahun),
      tahunTanam: raw.tahunTanam !== undefined ? Number(raw.tahunTanam) : undefined,
    });
  }
  return rows;
}

// ---------- GAP (default TIDAK) ----------
function gapRows(fd: FormData): GapRequest[] {
  return GAP_ITEMS.map((g) => {
    const v = get(fd, `gap_${g.jenis}`) ?? "TIDAK";
    const jawaban = (JAWABAN_GAP_VALUES as readonly string[]).includes(v) ? (v as JawabanGap) : "TIDAK";
    return {
      jenis: g.jenis as JenisPraktikGap,
      jawaban,
      keterangan: s(get(fd, `gap_${g.jenis}_ket`)),
    };
  });
}

// ---------- Produksi ----------
function produksiRows(fd: FormData): ProduksiRequest[] {
  return TAHUN_PRODUKSI.map((tahun) => {
    const sat = get(fd, `prod_${tahun}_satuan`);
    const satuan = (SATUAN_VALUES as readonly string[]).includes(sat ?? "")
      ? (sat as ProduksiRequest["satuan"])
      : "KG";
    return {
      tahun,
      satuan,
      cherry: num(fd.get(`prod_${tahun}_cherry`)),
      gabahBasah: num(fd.get(`prod_${tahun}_gabahBasah`)),
      gabahKering: num(fd.get(`prod_${tahun}_gabahKering`)),
      greenBean: num(fd.get(`prod_${tahun}_greenBean`)),
    };
  });
}

// ---------- Produk ----------
function produkRows(fd: FormData): ProdukRequest[] {
  const fixed: ProdukRequest[] = PRODUK.map((p) => ({
    jenis: p.jenis,
    labelCustom: null,
    dijual: getBool(fd, `pd_${p.jenis}`),
    volumeKgTahun: num(fd.get(`pd_${p.jenis}_vol`)),
  }));

  const custom: ProdukRequest[] = [];
  for (let i = 0; ; i++) {
    if (!hasAnyWithPrefix(fd, `pdl_${i}_`)) break;
    const nama = get(fd, `pdl_${i}_nama`);
    if (!nama) continue;
    custom.push({
      jenis: "LAINNYA",
      labelCustom: nama,
      dijual: getBool(fd, `pdl_${i}_dijual`),
      volumeKgTahun: num(fd.get(`pdl_${i}_vol`)),
    });
  }
  return [...fixed, ...custom];
}

// ---------- Pasar ----------
function pasarRows(fd: FormData): PasarRequest[] {
  const fixed: PasarRequest[] = PASAR.map((p) => ({
    kategori: p.kategori,
    labelCustom: null,
    aktif: getBool(fd, `ps_${p.kategori}`),
    persentase: num(fd.get(`ps_${p.kategori}_persen`)),
    profilPenjual: s(get(fd, `ps_${p.kategori}_profil`)),
  }));

  const custom: PasarRequest[] = [];
  for (let i = 0; ; i++) {
    if (!hasAnyWithPrefix(fd, `psl_${i}_`)) break;
    const nama = get(fd, `psl_${i}_nama`);
    if (!nama) continue;
    custom.push({
      kategori: "LAINNYA",
      labelCustom: nama,
      aktif: getBool(fd, `psl_${i}_aktif`),
      persentase: num(fd.get(`psl_${i}_persen`)),
      profilPenjual: s(get(fd, `psl_${i}_profil`)),
    });
  }
  return [...fixed, ...custom];
}

// ---------- Kondisi kebun ----------
function kondisiRows(fd: FormData): KondisiRequest[] {
  return KONDISI_KEBUN.map((k) => ({
    jenis: k.jenis,
    jawaban: getBool(fd, `kb_${k.jenis}`),
    keterangan: s(get(fd, `kb_${k.jenis}_ket`)),
  }));
}

// ---------- Kode petani ----------
function buildKodePetani(fd: FormData): string | null {
  const s1 = get(fd, "kp1");
  const s2 = get(fd, "kp2");
  const s3 = get(fd, "kp3");
  if (!s2 && !s3) return null;
  if (!s1 || !s2 || !s3) throw new Error("Kode petani belum lengkap (harus 3 bagian).");
  return `${s1}-${s2}-${s3}`.toUpperCase();
}

// ---------- Kelompok tani baru ----------
function kelompokBaru(fd: FormData) {
  if (get(fd, "kelompokTaniId")) return null;
  const nama = get(fd, "kt_nama");
  if (!nama) return null;
  const k1 = get(fd, "kt_kode_1");
  const k2 = get(fd, "kt_kode_2");
  let kode: string | null = null;
  if (k1 || k2) {
    if (!k1 || !k2) throw new Error("Kode kelompok tani belum lengkap (2 bagian).");
    kode = `${k1}-${k2}`.toUpperCase();
  }
  return { nama, kode };
}

const IDENTITY_STR = [
  "namaPanggilan",
  "alamatDomisili",
  "telepon",
  "namaPetugasPendaftar",
  "kontakDaruratNama",
  "kontakDaruratTelepon",
  "kontakDaruratHubungan",
] as const;

/** FormData → PetaniRequest. Melempar Error berisi pesan validasi. */
export function buildPetaniRequest(fd: FormData): PetaniRequest {
  const desaKode = get(fd, "desaKode");
  if (!desaKode) throw new Error("Desa wajib dipilih");
  const namaLengkap = get(fd, "namaLengkap");
  if (!namaLengkap) throw new Error("Nama lengkap wajib diisi");

  const dateOpt = (key: string) => get(fd, key);
  const scalars = {
    namaPanggilan: s(get(fd, IDENTITY_STR[0])),
    alamatDomisili: s(get(fd, IDENTITY_STR[1])),
    telepon: s(get(fd, IDENTITY_STR[2])),
    namaPetugasPendaftar: s(get(fd, IDENTITY_STR[3])),
    kontakDaruratNama: s(get(fd, IDENTITY_STR[4])),
    kontakDaruratTelepon: s(get(fd, IDENTITY_STR[5])),
    kontakDaruratHubungan: s(get(fd, IDENTITY_STR[6])),
  };

  const jenisKelamin = get(fd, "jenisKelamin");

  return {
    desaKode,
    namaLengkap,
    ...scalars,
    jenisKelamin: jenisKelamin === "L" || jenisKelamin === "P" ? jenisKelamin : null,
    tanggalLahir: dateOpt("tanggalLahir") ?? null,
    tanggalPendaftaran: dateOpt("tanggalPendaftaran") ?? null,
    kodePetani: buildKodePetani(fd),
    kelompokTaniId: get(fd, "kelompokTaniId") ?? null,
    kelompokTaniBaru: kelompokBaru(fd),
    plot: plotRows(fd),
    naungan: naunganRows(fd),
    praktikGap: gapRows(fd),
    produksi: produksiRows(fd),
    produk: produkRows(fd),
    pasar: pasarRows(fd),
    kondisiKebun: kondisiRows(fd),
  };
}

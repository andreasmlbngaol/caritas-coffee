// Serialisasi FormData form desa → BaselineDesaRequest (JSON).
// Padanan parse/schema di proyek lama (actions.ts). Normalisasi "-" untuk teks
// kosong & 0 untuk angka kosong dipertahankan persis.
import { KEBIJAKAN, LEMBAGA } from "./constants";
import type { BaselineDesaRequest, KebijakanRequest, KelembagaanRequest } from "./types";
import { get, num } from "@/lib/form";

const s = (v: string | undefined) => v ?? "-";
const numOpt = (v: string | undefined) => (v === undefined ? undefined : Number(v));
const bool = (fd: FormData, key: string) => get(fd, key) === "true";
const satuanOpt = (v: string | undefined) => (v === "HA" ? "HA" : v === "PERSEN" ? "PERSEN" : undefined);

function kebijakanRows(fd: FormData): KebijakanRequest[] {
  return KEBIJAKAN.map((k) => ({
    jenis: k.jenis,
    ada: bool(fd, `kb_${k.jenis}`),
    keterangan: s(get(fd, `kb_${k.jenis}_ket`)),
  }));
}

function kelembagaanRows(fd: FormData): KelembagaanRequest[] {
  return LEMBAGA.map((l) => ({
    jenis: l.jenis,
    jumlah: num(get(fd, `lm_${l.jenis}_jumlah`)),
    kondisi: s(get(fd, `lm_${l.jenis}_kondisi`)),
  }));
}

/** FormData → BaselineDesaRequest. Melempar Error berisi pesan validasi. */
export function buildDesaRequest(fd: FormData): BaselineDesaRequest {
  const desaKode = get(fd, "desaKode");
  if (!desaKode) throw new Error("Desa wajib dipilih");
  const tahunRaw = get(fd, "tahunPendataan");
  const tahunPendataan = tahunRaw ? Number(tahunRaw) : NaN;
  if (!Number.isInteger(tahunPendataan) || tahunPendataan < 2000 || tahunPendataan > 2100) {
    throw new Error("Tahun pendataan harus diisi antara 2000 dan 2100");
  }

  const g = (key: string) => get(fd, key);

  return {
    desaKode,
    tahunPendataan,
    sumberData: s(g("sumberData")),
    luasWilayahHa: num(g("luasWilayahHa")),
    jumlahPenduduk: num(g("jumlahPenduduk")),
    jumlahKK: num(g("jumlahKK")),
    jumlahPetaniKopi: num(g("jumlahPetaniKopi")),
    luasArealKopiHa: num(g("luasArealKopiHa")),
    luasKomoditiLainHa: num(g("luasKomoditiLainHa")),
    latitude: numOpt(g("latitude")),
    longitude: numOpt(g("longitude")),
    topografi: s(g("topografi")),
    ketinggianMdpl: num(g("ketinggianMdpl")),
    bulanHujan: s(g("bulanHujan")),
    bulanKering: s(g("bulanKering")),
    suhuRataRataC: num(g("suhuRataRataC")),
    jenisTanah: s(g("jenisTanah")),
    aksesJalan: s(g("aksesJalan")),
    jarakIbukotaKecamatanKm: num(g("jarakIbukotaKecamatanKm")),
    jarakPasarKm: num(g("jarakPasarKm")),
    jarakKonservasiKm: num(g("jarakKonservasiKm")),
    luasAPLHa: num(g("luasAPLHa")),
    namaKawasanKonservasi: s(g("namaKawasanKonservasi")),
    produktivitasKgHaTahun: num(g("produktivitasKgHaTahun")),
    hargaCherryRp: num(g("hargaCherryRp")),
    hargaGreenBeanRpKg: num(g("hargaGreenBeanRpKg")),
    pembeliUtama: s(g("pembeliUtama")),
    jumlahPedagangPengumpul: num(g("jumlahPedagangPengumpul")),
    koperasiAktifUnit: num(g("koperasiAktifUnit")),
    eksportir: s(g("eksportir")),
    industriPengolahan: s(g("industriPengolahan")),
    permasalahanUtama: s(g("permasalahanUtama")),
    berbatasanKonservasi: bool(fd, "berbatasanKonservasi"),
    luasPenyanggaHa: num(g("luasPenyanggaHa")),
    tutupanHutan: num(g("tutupanHutan")),
    tutupanHutanSatuan: satuanOpt(g("tutupanHutanSatuan")) ?? "PERSEN",
    tutupanAgroforestry: num(g("tutupanAgroforestry")),
    tutupanAgroforestrySatuan: satuanOpt(g("tutupanAgroforestrySatuan")) ?? "PERSEN",
    rawanLongsor: bool(fd, "rawanLongsor"),
    lokasiRawanLongsor: s(g("lokasiRawanLongsor")),
    rawanErosi: bool(fd, "rawanErosi"),
    lokasiRawanErosi: s(g("lokasiRawanErosi")),
    konflikSatwa: bool(fd, "konflikSatwa"),
    jenisSatwaKonflik: s(g("jenisSatwaKonflik")),
    praktikKonservasi: s(g("praktikKonservasi")),
    kebijakan: kebijakanRows(fd),
    kelembagaan: kelembagaanRows(fd),
  };
}

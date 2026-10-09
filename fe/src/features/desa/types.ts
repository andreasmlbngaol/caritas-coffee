// Tipe request form desa (mirror BaselineDesaRequest BE). Form mengumpulkan lewat
// FormData (kontrak data-* dipertahankan) lalu diserialisasi ke JSON ini.
import type { JenisKebijakan, JenisLembaga, SatuanTutupan } from "@/api/types";

export interface KebijakanRequest {
  jenis: JenisKebijakan;
  ada: boolean;
  keterangan?: string | null;
}

export interface KelembagaanRequest {
  jenis: JenisLembaga;
  jumlah?: number | null;
  kondisi?: string | null;
}

export interface BaselineDesaRequest {
  desaKode: string;
  tahunPendataan: number;
  sumberData?: string | null;
  luasWilayahHa?: number | null;
  jumlahPenduduk?: number | null;
  jumlahKK?: number | null;
  jumlahPetaniKopi?: number | null;
  luasArealKopiHa?: number | null;
  luasKomoditiLainHa?: number | null;
  latitude?: number | null;
  longitude?: number | null;
  topografi?: string | null;
  ketinggianMdpl?: number | null;
  bulanHujan?: string | null;
  bulanKering?: string | null;
  suhuRataRataC?: number | null;
  jenisTanah?: string | null;
  aksesJalan?: string | null;
  jarakIbukotaKecamatanKm?: number | null;
  jarakPasarKm?: number | null;
  jarakKonservasiKm?: number | null;
  luasAPLHa?: number | null;
  namaKawasanKonservasi?: string | null;
  produktivitasKgHaTahun?: number | null;
  hargaCherryRp?: number | null;
  hargaGreenBeanRpKg?: number | null;
  pembeliUtama?: string | null;
  jumlahPedagangPengumpul?: number | null;
  koperasiAktifUnit?: number | null;
  eksportir?: string | null;
  industriPengolahan?: string | null;
  permasalahanUtama?: string | null;
  berbatasanKonservasi?: boolean | null;
  luasPenyanggaHa?: number | null;
  tutupanHutan?: number | null;
  tutupanHutanSatuan?: SatuanTutupan | null;
  tutupanAgroforestry?: number | null;
  tutupanAgroforestrySatuan?: SatuanTutupan | null;
  rawanLongsor?: boolean | null;
  lokasiRawanLongsor?: string | null;
  rawanErosi?: boolean | null;
  lokasiRawanErosi?: string | null;
  konflikSatwa?: boolean | null;
  jenisSatwaKonflik?: string | null;
  praktikKonservasi?: string | null;
  kebijakan: KebijakanRequest[];
  kelembagaan: KelembagaanRequest[];
}

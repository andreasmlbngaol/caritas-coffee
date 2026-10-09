// Tipe request form petani (mirror PetaniRequest BE). Form mengumpulkan lewat
// FormData (kontrak data-* dipertahankan) lalu diserialisasi ke JSON ini.
import type {
  JenisKelamin,
  JenisKondisiKebun,
  JenisPraktikGap,
  JenisProdukDijual,
  JawabanGap,
  KategoriPasar,
  SatuanProduksi,
  SistemBudidaya,
  StatusKepemilikanLahan,
} from "@/api/types";

export interface KelompokBaruRequest {
  nama: string;
  kode?: string | null;
}

export interface PlotRequest {
  namaHamparan?: string | null;
  varietas?: string | null;
  tahunTanam: number[];
  kodeGps?: string | null;
  elevasiMdpl?: number | null;
  kemiringanPersen?: number | null;
  luasKopiHa?: number | null;
  fotoKey?: string | null;
  fotoLatitude?: number | null;
  fotoLongitude?: number | null;
  statusKepemilikan?: StatusKepemilikanLahan | null;
  sistemBudidaya?: SistemBudidaya | null;
  areaKonservasi?: string | null;
  tanamanBaru?: number | null;
  pohonProduktif?: number | null;
  pohonTidakProduktif?: number | null;
  pestisidaNama?: string | null;
  pestisidaBulanTahun?: string | null;
}

export interface NaunganRequest {
  jenis?: string | null;
  jumlah?: number | null;
  fungsi?: string | null;
  pemangkasan?: boolean | null;
  produksiPerTahun?: string | null;
  tahunTanam?: number | null;
}

export interface GapRequest {
  jenis: JenisPraktikGap;
  jawaban?: JawabanGap | null;
  keterangan?: string | null;
}

export interface ProduksiRequest {
  tahun: number;
  satuan?: SatuanProduksi | null;
  cherry?: number | null;
  gabahBasah?: number | null;
  gabahKering?: number | null;
  greenBean?: number | null;
}

export interface ProdukRequest {
  jenis: JenisProdukDijual;
  labelCustom?: string | null;
  dijual: boolean;
  volumeKgTahun?: number | null;
}

export interface PasarRequest {
  kategori: KategoriPasar;
  labelCustom?: string | null;
  aktif: boolean;
  persentase?: number | null;
  profilPenjual?: string | null;
}

export interface KondisiRequest {
  jenis: JenisKondisiKebun;
  jawaban: boolean;
  keterangan?: string | null;
}

export interface PetaniRequest {
  desaKode: string;
  namaLengkap: string;
  namaPanggilan?: string | null;
  jenisKelamin?: JenisKelamin | null;
  tanggalLahir?: string | null;
  alamatDomisili?: string | null;
  telepon?: string | null;
  tanggalPendaftaran?: string | null;
  namaPetugasPendaftar?: string | null;
  kontakDaruratNama?: string | null;
  kontakDaruratTelepon?: string | null;
  kontakDaruratHubungan?: string | null;
  kodePetani?: string | null;
  kelompokTaniId?: string | null;
  kelompokTaniBaru?: KelompokBaruRequest | null;
  plot: PlotRequest[];
  naungan: NaunganRequest[];
  praktikGap: GapRequest[];
  produksi: ProduksiRequest[];
  produk: ProdukRequest[];
  pasar: PasarRequest[];
  kondisiKebun: KondisiRequest[];
}

// Tipe domain FE - mirror dari DTO BE (id.caritas_kopi.be.*).
// Sumber kebenaran kontrak ada di BE; berkas ini mengikuti bentuk responsnya.
// Bila kontrak BE berubah, jalankan `npm run gen:api` (OpenAPI) atau selaraskan manual.

// ---------- Enum ----------
export type Role = "ADMIN" | "ENUMERATOR";
export type SatuanTutupan = "PERSEN" | "HA";
export type JenisKebijakan =
  | "RPJM_DESA"
  | "PERDES_PERTANIAN"
  | "PERDES_PERLINDUNGAN_HUTAN"
  | "PROGRAM_PERKEMBANGAN_KOPI"
  | "PROGRAM_KOPERASI"
  | "PROGRAM_PERHUTANAN_SOSIAL";
export type JenisLembaga =
  | "KELOMPOK_TANI"
  | "GAPOKTAN"
  | "KOPERASI"
  | "BUMDES"
  | "PENYULUH"
  | "PENDAMPING";
export type JenisKelamin = "L" | "P";
export type StatusKepemilikanLahan = "MS" | "SW" | "BH" | "TA" | "L";
export type SistemBudidaya = "AF" | "MK";
export type JawabanGap = "YA" | "TIDAK" | "KADANG";
export type SatuanProduksi = "KG" | "SOLUP" | "BAMBU" | "KALENG";
export type JenisProdukDijual =
  | "CHERRY"
  | "GABAH_BASAH"
  | "GABAH_KERING"
  | "GB_WET_HULL"
  | "GB_NATURAL"
  | "GB_HONEY"
  | "GB_FULL_WASH"
  | "GB_WINE"
  | "LAINNYA";
export type KategoriPasar =
  | "KOMERSIAL"
  | "KOMERSIAL_BERSERTIFIKAT"
  | "SPECIALTY"
  | "ORGANIK"
  | "LAINNYA";
export type JenisPraktikGap =
  | "PEMANGKASAN_KOPI"
  | "PEMANGKASAN_NAUNGAN"
  | "PENGENDALIAN_GULMA"
  | "PEMUPUKAN"
  | "PEREMAJAAN_TANAMAN"
  | "PENGENDALIAN_PBKO"
  | "PENGENDALIAN_KARAT_DAUN"
  | "PESTISIDA_SESUAI_DOSIS"
  | "PENYIMPANAN_PESTISIDA"
  | "TERAS_SENGKEDAN"
  | "COVER_CROP"
  | "RORAK_RESAPAN"
  | "LIMBAH_PULP"
  | "PANEN_SELEKTIF"
  | "SORTASI_CHERRY"
  | "PENJEMURAN_BERSIH"
  | "PENYIMPANAN_HASIL"
  | "TANPA_BAKAR_LAHAN"
  | "TANPA_KIMIA_TERLARANG"
  | "APD_PESTISIDA"
  | "TANPA_PEKERJA_ANAK";
export type JenisKondisiKebun =
  | "KEPEMILIKAN_JELAS"
  | "BATAS_KONSERVASI"
  | "BATAS_HUTAN_LINDUNG"
  | "DEKAT_SUNGAI"
  | "DEKAT_MATA_AIR"
  | "POHON_NAUNGAN"
  | "KONSERVASI_TANAH"
  | "PERNAH_BAKAR_LAHAN"
  | "KONFLIK_SATWA"
  | "EROSI_LONGSOR";

// ---------- Auth ----------
export interface SessionUser {
  id: string;
  name: string;
  role: Role;
}

// ---------- Wilayah ----------
export interface WilayahDto {
  kode: string;
  nama: string;
}

// ---------- Kelompok Tani ----------
export interface KelompokTaniRingkas {
  id: string;
  nama: string;
  kode: string | null;
}

export interface KelompokTaniDto {
  id: string;
  nama: string;
  kode: string | null;
  desa: string;
  desaKode: string;
  kecamatan: string | null;
  jumlahPetani: number;
  createdAt: string;
}

export interface KelompokTaniRequest {
  nama: string;
  kode: string | null;
  desaKode: string;
}

// ---------- Petani ----------
export interface DesaRingkas {
  kode: string;
  nama: string;
  kecamatan: string;
  kabupaten: string;
  provinsi: string;
}

export interface PlotDto {
  id: string;
  nomor: number;
  namaHamparan: string | null;
  varietas: string | null;
  tahunTanam: number[];
  kodeGps: string | null;
  elevasiMdpl: number | null;
  kemiringanPersen: number | null;
  luasKopiHa: number | null;
  fotoKey: string | null;
  fotoLatitude: number | null;
  fotoLongitude: number | null;
  statusKepemilikan: StatusKepemilikanLahan | null;
  sistemBudidaya: SistemBudidaya | null;
  areaKonservasi: string | null;
  tanamanBaru: number | null;
  pohonProduktif: number | null;
  pohonTidakProduktif: number | null;
  pestisidaNama: string | null;
  pestisidaBulanTahun: string | null;
}

export interface NaunganDto {
  id: string;
  jenis: string | null;
  jumlah: number | null;
  fungsi: string | null;
  pemangkasan: boolean | null;
  produksiPerTahun: string | null;
  tahunTanam: number | null;
}

export interface GapDto {
  id: string;
  jenis: JenisPraktikGap;
  jawaban: JawabanGap | null;
  keterangan: string | null;
}

export interface ProduksiPetaniDto {
  id: string;
  tahun: number;
  satuan: SatuanProduksi | null;
  cherry: number | null;
  gabahBasah: number | null;
  gabahKering: number | null;
  greenBean: number | null;
  produktivitas: number | null;
}

export interface ProdukDto {
  id: string;
  jenis: JenisProdukDijual;
  labelCustom: string | null;
  dijual: boolean;
  volumeKgTahun: number | null;
}

export interface PasarDto {
  id: string;
  kategori: KategoriPasar;
  labelCustom: string | null;
  aktif: boolean;
  persentase: number | null;
  profilPenjual: string | null;
}

export interface KondisiDto {
  id: string;
  jenis: JenisKondisiKebun;
  jawaban: boolean;
  keterangan: string | null;
}

export interface PetaniListDto {
  id: string;
  kodePetani: string | null;
  namaLengkap: string;
  desa: string;
  kecamatan: string;
  kelompokTani: string | null;
  kelompokTaniId: string | null;
  kelompokTaniKode: string | null;
  telepon: string | null;
  createdBy: string | null;
  createdAt: string;
}

export interface PetaniDetailDto {
  id: string;
  kodePetani: string | null;
  namaLengkap: string;
  namaPanggilan: string | null;
  jenisKelamin: JenisKelamin | null;
  tanggalLahir: string | null;
  alamatDomisili: string | null;
  telepon: string | null;
  tanggalPendaftaran: string | null;
  namaPetugasPendaftar: string | null;
  kontakDaruratNama: string | null;
  kontakDaruratTelepon: string | null;
  kontakDaruratHubungan: string | null;
  desa: DesaRingkas;
  kelompokTani: KelompokTaniRingkas | null;
  createdBy: string | null;
  createdAt: string;
  updatedAt: string;
  plot: PlotDto[];
  naungan: NaunganDto[];
  praktikGap: GapDto[];
  produksi: ProduksiPetaniDto[];
  produk: ProdukDto[];
  pasar: PasarDto[];
  kondisiKebun: KondisiDto[];
}

// ---------- Desa ----------
export interface KebijakanDto {
  id: string;
  jenis: JenisKebijakan;
  ada: boolean;
  keterangan: string | null;
}

export interface KelembagaanDto {
  id: string;
  jenis: JenisLembaga;
  jumlah: number | null;
  kondisi: string | null;
}

export interface DesaListDto {
  id: string;
  desa: string;
  kecamatan: string;
  tahunPendataan: number;
  jumlahPetaniKopi: number | null;
  luasArealKopiHa: number | null;
  createdBy: string | null;
  createdAt: string;
}

export interface DesaDetailDto {
  id: string;
  tahunPendataan: number;
  sumberData: string | null;
  desa: DesaRingkas;
  createdBy: string | null;
  createdAt: string;
  updatedAt: string;
  luasWilayahHa: number | null;
  jumlahPenduduk: number | null;
  jumlahKK: number | null;
  jumlahPetaniKopi: number | null;
  luasArealKopiHa: number | null;
  luasKomoditiLainHa: number | null;
  latitude: number | null;
  longitude: number | null;
  topografi: string | null;
  ketinggianMdpl: number | null;
  bulanHujan: string | null;
  bulanKering: string | null;
  suhuRataRataC: number | null;
  jenisTanah: string | null;
  aksesJalan: string | null;
  jarakIbukotaKecamatanKm: number | null;
  jarakPasarKm: number | null;
  jarakKonservasiKm: number | null;
  luasAPLHa: number | null;
  namaKawasanKonservasi: string | null;
  produktivitasKgHaTahun: number | null;
  hargaCherryRp: number | null;
  hargaGreenBeanRpKg: number | null;
  pembeliUtama: string | null;
  jumlahPedagangPengumpul: number | null;
  koperasiAktifUnit: number | null;
  eksportir: string | null;
  industriPengolahan: string | null;
  permasalahanUtama: string | null;
  berbatasanKonservasi: boolean | null;
  luasPenyanggaHa: number | null;
  tutupanHutan: number | null;
  tutupanHutanSatuan: SatuanTutupan | null;
  tutupanAgroforestry: number | null;
  tutupanAgroforestrySatuan: SatuanTutupan | null;
  rawanLongsor: boolean | null;
  lokasiRawanLongsor: string | null;
  rawanErosi: boolean | null;
  lokasiRawanErosi: string | null;
  konflikSatwa: boolean | null;
  jenisSatwaKonflik: string | null;
  praktikKonservasi: string | null;
  kebijakan: KebijakanDto[];
  kelembagaan: KelembagaanDto[];
}

// ---------- Admin users ----------
export interface UserDto {
  id: string;
  username: string;
  fullName: string | null;
  role: Role;
  isActive: boolean;
  createdAt: string;
  lastLoginAt: string | null;
}

export interface CredentialsResponse {
  username: string;
  password: string;
}

// ---------- Analitik ----------
export interface TrenProduksi {
  tahun: string;
  cherry: number;
  greenBean: number;
  gabahBasah: number;
  gabahKering: number;
}

export interface RingkasanDto {
  jumlahPetani: number;
  jumlahDesa: number;
  jumlahKelompok: number;
  luasArealKopiHa: number;
  luasPlotHa: number;
  pohonProduktif: number;
  tahunTerbaru: number | null;
  produktivitasRata: number;
  trenProduksi: TrenProduksi[];
}

export interface GapItemStat {
  jenis: string;
  ya: number;
  tidak: number;
  kadang: number;
  total: number;
  pctYa: number;
}

export interface GapAdoptionDto {
  totalPetani: number;
  items: GapItemStat[];
  adopsiKeseluruhan: number;
}

export interface ProduksiTahun {
  tahun: string;
  cherry: number;
  gabahBasah: number;
  gabahKering: number;
  greenBean: number;
  produktivitasRata: number;
  jumlahPetani: number;
}

export interface TopDesa {
  nama: string;
  cherry: number;
  gabahBasah: number;
  gabahKering: number;
  greenBean: number;
  total: number;
}

export interface ProduksiAnalitikDto {
  tahunTerbaru: number | null;
  byTahun: ProduksiTahun[];
  topDesa: TopDesa[];
}

export interface ProdukStat {
  label: string;
  jenis: string;
  volume: number;
  petani: number;
  custom: boolean;
  rataVolume: number;
}

export interface PasarStat {
  label: string;
  kategori: string;
  petani: number;
  custom: boolean;
  rataPersen: number;
  profil: string[];
}

export interface VolumeJenis {
  jenis: string;
  volume: number;
}

export interface PasarProdukDto {
  totalPetani: number;
  produk: ProdukStat[];
  pasar: PasarStat[];
  volumeByJenis: VolumeJenis[];
}

export interface Frekuensi {
  nama: string;
  jumlah: number;
}

export interface KondisiStat {
  jenis: string;
  jumlah: number;
  pct: number;
}

export interface KonservasiDto {
  totalPetani: number;
  kondisi: KondisiStat[];
  jumlahDesa: number;
  luasPenyanggaHa: number;
  luasAPLHa: number;
  jarakKonservasiRata: number;
  rawanLongsor: number;
  rawanErosi: number;
  konflikSatwa: number;
  berbatasan: number;
  tutupanHutan: number;
  tutupanAgroforestry: number;
  praktik: Frekuensi[];
  satwa: Frekuensi[];
  kawasan: string[];
}

export interface TopDesaPetani {
  nama: string;
  kecamatan: string;
  petani: number;
}

export interface GenderStat {
  L: number;
  P: number;
  kosong: number;
}

export interface UsiaStat {
  label: string;
  jumlah: number;
}

export interface LembagaStat {
  jenis: string;
  jumlah: number;
  desa: number;
}

export interface KebijakanStat {
  jenis: string;
  desa: number;
}

export interface WilayahAnalitikDto {
  topDesa: TopDesaPetani[];
  gender: GenderStat;
  usia: UsiaStat[];
  totalPenduduk: number;
  totalKK: number;
  totalArealKopi: number;
  totalPetaniKopi: number;
  rataKetinggian: number;
  rataSuhu: number;
  jumlahDesa: number;
  topografi: Frekuensi[];
  jenisTanah: Frekuensi[];
  aksesJalan: Frekuensi[];
  pembeliUtama: Frekuensi[];
  eksportir: Frekuensi[];
  industri: Frekuensi[];
  permasalahan: Frekuensi[];
  lembaga: LembagaStat[];
  kebijakan: KebijakanStat[];
}

export interface NaunganStat {
  nama: string;
  petani: number;
  pohon: number;
  dipangkas: number;
}

export interface UmurStat {
  label: string;
  jumlah: number;
}

export interface AgronomiDto {
  totalPetani: number;
  jumlahPlot: number;
  totalLuas: number;
  totalPohonProduktif: number;
  totalPohonTidakProduktif: number;
  totalTanamanBaru: number;
  rataProduktif: number;
  varietas: Frekuensi[];
  sistemBudidaya: Frekuensi[];
  kepemilikan: Frekuensi[];
  areaKonservasi: Frekuensi[];
  naungan: NaunganStat[];
  pestisida: Frekuensi[];
  umur: UmurStat[];
}

export interface LokasiDesa {
  lat: number;
  lng: number;
  nama: string;
  kecamatan: string;
  kabupaten: string;
  luasArealKopiHa: number;
  petaniKopi: number;
  penduduk: number;
  ketinggian: number | null;
}

export interface LokasiPlot {
  lat: number;
  lng: number;
  luasKopiHa: number;
  varietas: string | null;
  hamparan: string | null;
  petani: string;
  desa: string;
}

export interface LokasiDto {
  desa: LokasiDesa[];
  plot: LokasiPlot[];
}

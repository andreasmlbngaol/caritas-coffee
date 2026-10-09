import { Link, useParams } from "react-router";
import { ArrowLeft, Check, FileDown, FileText, Pencil } from "lucide-react";
import { pageWide, ListSkeleton } from "@/components/ui";
import { useAuth } from "@/features/auth/use-auth";
import { idNum } from "@/lib/format";
import { DesaDeleteButton } from "./delete-button";
import { useDesaDetail } from "./queries";
import { KEBIJAKAN, LEMBAGA } from "./constants";

const thCls = "px-4 py-2.5 text-left text-[11px] font-semibold uppercase tracking-wider text-gray-500";
const labelCls = "bg-gray-50 px-4 py-2.5 text-xs font-medium text-gray-500";
const valueCls = "whitespace-pre-line px-4 py-2.5 text-sm";

const fmt = (v: string | number | null | undefined) =>
  v === null || v === undefined || v === "" ? "-" : String(v);
const num = (v: number | null | undefined) => (v == null ? "-" : idNum.format(v));
const numUnit = (v: number | null | undefined, unit: string) => (v == null ? "-" : `${idNum.format(v)} ${unit}`);
const fmtBool = (v: boolean | null | undefined) => (v == null ? "-" : v ? "Ya" : "Tidak");
const fmtTutupan = (v: number | null | undefined, s: string | null | undefined) =>
  v == null ? "-" : `${idNum.format(v)} ${s === "HA" ? "Ha" : "%"}`;
const fmtBoolDetail = (v: boolean | null | undefined, detail: string | null | undefined, label: string) =>
  v == null ? "-" : v ? (detail ? `Ya, ${label}: ${detail}` : "Ya") : "Tidak";

function Card({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <section className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-gray-950/5">
      <h2 className="mb-4 text-sm font-semibold tracking-tight">{title}</h2>
      {children}
    </section>
  );
}

function KVTable({ rows }: { rows: [string, string][] }) {
  return (
    <table className="w-full text-sm">
      <tbody className="divide-y divide-gray-100">
        {rows.map(([k, v]) => (
          <tr key={k}>
            <td className={`${labelCls} w-[55%]`}>{k}</td>
            <td className={valueCls}>{v}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

function TickCell({ on }: { on: boolean }) {
  return (
    <td className="px-4 py-2.5">
      <span className="flex justify-center">
        {on ? <Check size={15} className="text-jade-700" strokeWidth={3} /> : <span className="text-gray-300">-</span>}
      </span>
    </td>
  );
}

export function DesaDetailPage() {
  const { id = "" } = useParams();
  const { user } = useAuth();
  const isAdmin = user?.role === "ADMIN";
  const { data: b, isPending } = useDesaDetail(id);

  if (isPending || !b) return <ListSkeleton />;

  const w = b.desa;
  const koordinat = b.latitude != null && b.longitude != null ? `${b.latitude}, ${b.longitude}` : "-";
  const musim =
    b.bulanHujan || b.bulanKering ? `Hujan: ${b.bulanHujan ?? "-"}\nKering: ${b.bulanKering ?? "-"}` : "-";

  const sectionA: { left: [string, string]; right: [string, string] }[] = [
    { left: ["Desa", w.nama], right: ["Topografi", fmt(b.topografi)] },
    { left: ["Kecamatan", w.kecamatan], right: ["Ketinggian (mdpl)", num(b.ketinggianMdpl)] },
    { left: ["Kabupaten", w.kabupaten], right: ["Bulan Hujan dan Bulan Kering", musim] },
    { left: ["Provinsi", w.provinsi], right: ["Suhu Rata-rata (°C)", num(b.suhuRataRataC)] },
    { left: ["Luas Wilayah (Ha)", num(b.luasWilayahHa)], right: ["Jenis Tanah", fmt(b.jenisTanah)] },
    { left: ["Jumlah Penduduk", num(b.jumlahPenduduk)], right: ["Akses Jalan", fmt(b.aksesJalan)] },
    { left: ["Jumlah KK", num(b.jumlahKK)], right: ["Jarak ke Ibu Kota Kecamatan", numUnit(b.jarakIbukotaKecamatanKm, "km")] },
    { left: ["Jumlah Petani Kopi", num(b.jumlahPetaniKopi)], right: ["Jarak ke Pasar", numUnit(b.jarakPasarKm, "km")] },
    { left: ["Luas Areal Kopi (Ha)", num(b.luasArealKopiHa)], right: ["Jarak ke Kawasan Konservasi", numUnit(b.jarakKonservasiKm, "km")] },
    { left: ["Luas Area Komoditi lainnya", numUnit(b.luasKomoditiLainHa, "Ha")], right: ["Luas Lahan APL", numUnit(b.luasAPLHa, "Ha")] },
    { left: ["Koordinat Desa", koordinat], right: ["Nama Kawasan Konservasi", fmt(b.namaKawasanKonservasi)] },
    { left: ["Tahun Pendataan", fmt(b.tahunPendataan)], right: ["Sumber Data", fmt(b.sumberData)] },
  ];

  const sectionB = KEBIJAKAN.map((k) => {
    const row = b.kebijakan.find((x) => x.jenis === k.jenis);
    return { label: k.label, ada: row?.ada ?? false, keterangan: row?.keterangan ?? "-" };
  });
  const sectionC = LEMBAGA.map((l) => {
    const row = b.kelembagaan.find((x) => x.jenis === l.jenis);
    return { label: l.label, jumlah: row?.jumlah != null ? String(row.jumlah) : "0", kondisi: row?.kondisi ?? "-" };
  });
  const sectionD: [string, string][] = [
    ["Jumlah Petani Kopi (orang)", num(b.jumlahPetaniKopi)],
    ["Luas Kebun Kopi (Ha)", num(b.luasArealKopiHa)],
    ["Produktivitas Rata-rata (Kg/Ha/Tahun)", num(b.produktivitasKgHaTahun)],
    ["Harga Cherry (Rp)", num(b.hargaCherryRp)],
    ["Harga Green Bean (Rp/kg)", num(b.hargaGreenBeanRpKg)],
    ["Pembeli Utama", fmt(b.pembeliUtama)],
    ["Jumlah Pedagang Pengumpul (Orang)", num(b.jumlahPedagangPengumpul)],
    ["Koperasi Aktif (Unit)", num(b.koperasiAktifUnit)],
    ["Eksportir", fmt(b.eksportir)],
    ["Industri Pengolahan", fmt(b.industriPengolahan)],
    ["Permasalahan Utama", fmt(b.permasalahanUtama)],
  ];
  const sectionE: [string, string][] = [
    ["Berbatasan Kawasan Konservasi (Ya/Tidak)", fmtBool(b.berbatasanKonservasi)],
    ["Luas Kawasan Penyangga (Ha)", num(b.luasPenyanggaHa)],
    ["Tutupan Hutan (% atau Ha)", fmtTutupan(b.tutupanHutan, b.tutupanHutanSatuan)],
    ["Tutupan Agroforestry (% atau Ha)", fmtTutupan(b.tutupanAgroforestry, b.tutupanAgroforestrySatuan)],
    ["Daerah Rawan Longsor", fmtBoolDetail(b.rawanLongsor, b.lokasiRawanLongsor, "lokasi")],
    ["Daerah Rawan Erosi", fmtBoolDetail(b.rawanErosi, b.lokasiRawanErosi, "lokasi")],
    ["Konflik Satwa", fmtBoolDetail(b.konflikSatwa, b.jenisSatwaKonflik, "jenis")],
    ["Praktik Konservasi yang Sudah Ada", fmt(b.praktikKonservasi)],
  ];

  return (
    <main className={pageWide}>
      <header className="flex items-start justify-between gap-4">
        <div className="flex items-center gap-3">
          <Link
            to="/desa"
            aria-label="Kembali ke daftar"
            className="rounded-xl p-2 text-gray-500 transition-colors hover:bg-white hover:text-gray-900 hover:shadow-sm hover:ring-1 hover:ring-gray-950/5"
          >
            <ArrowLeft size={18} />
          </Link>
          <div>
            <h1 className="text-lg font-semibold tracking-tight">{w.nama}</h1>
            <p className="mt-0.5 text-sm text-gray-500">
              Kecamatan {w.kecamatan} · Tahun {b.tahunPendataan}
              {isAdmin && ` · Diinput oleh ${b.createdBy ?? "-"}`}
            </p>
          </div>
        </div>
        <div className="flex shrink-0 items-center gap-1">
          <a href={`/api/desa/${id}/export/pdf`} title="Unduh PDF" className="rounded-lg p-2 text-red-500 transition-colors hover:bg-red-50">
            <FileDown size={16} />
          </a>
          <a href={`/api/desa/${id}/export/docx`} title="Unduh Word" className="rounded-lg p-2 text-blue-500 transition-colors hover:bg-blue-50">
            <FileText size={16} />
          </a>
          <Link to={`/desa/${id}/edit`} title="Edit" className="rounded-lg p-2 text-gray-500 transition-colors hover:bg-gray-100 hover:text-jade-800">
            <Pencil size={16} />
          </Link>
          <DesaDeleteButton id={id} nama={w.nama} />
        </div>
      </header>

      <div className="mt-8 space-y-6">
        <Card title="A - Data Desa / Wilayah">
          <div className="overflow-x-auto">
            <table className="w-full min-w-[560px] text-sm">
              <tbody className="divide-y divide-gray-100">
                {sectionA.map((p, i) => (
                  <tr key={i}>
                    <td className={`${labelCls} w-[21%]`}>{p.left[0]}</td>
                    <td className={`${valueCls} w-[29%]`}>{p.left[1]}</td>
                    <td className={`${labelCls} w-[21%]`}>{p.right[0]}</td>
                    <td className={`${valueCls} w-[29%]`}>{p.right[1]}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>

        <Card title="B - Kebijakan Lokal">
          <table className="w-full text-sm">
            <thead>
              <tr>
                <th className={thCls}>Kebijakan Lokal</th>
                <th className={`${thCls} w-16 text-center`}>Ada</th>
                <th className={`${thCls} w-16 text-center`}>Tidak</th>
                <th className={thCls}>Keterangan</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {sectionB.map((r) => (
                <tr key={r.label}>
                  <td className="px-4 py-2.5 text-sm">{r.label}</td>
                  <TickCell on={r.ada} />
                  <TickCell on={!r.ada} />
                  <td className="px-4 py-2.5 text-sm text-gray-600">{r.keterangan}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>

        <Card title="C - Kelembagaan">
          <table className="w-full text-sm">
            <thead>
              <tr>
                <th className={thCls}>Lembaga</th>
                <th className={`${thCls} w-28 text-center`}>Jumlah</th>
                <th className={thCls}>Kondisi</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {sectionC.map((r) => (
                <tr key={r.label}>
                  <td className="px-4 py-2.5 text-sm font-medium">{r.label}</td>
                  <td className="px-4 py-2.5 text-center text-sm">{r.jumlah}</td>
                  <td className="px-4 py-2.5 text-sm text-gray-600">{r.kondisi}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>

        <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
          <Card title="D - Kondisi Bisnis Kopi Saat Ini">
            <KVTable rows={sectionD} />
          </Card>
          <Card title="E - Kondisi Konservasi">
            <KVTable rows={sectionE} />
          </Card>
        </div>
      </div>
    </main>
  );
}

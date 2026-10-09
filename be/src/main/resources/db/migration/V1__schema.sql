-- Schema Database Kopi (padanan prisma/schema.prisma).
-- Nama tabel & kolom memakai snake_case yang sama seperti versi Prisma.

-- ==================== AUTH & USERS ====================
CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username      TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    full_name     TEXT,
    role          TEXT NOT NULL,
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_login_at TIMESTAMPTZ
);

-- ==================== MASTER WILAYAH ====================
CREATE TABLE wilayah_provinsi (
    kode TEXT PRIMARY KEY,
    nama TEXT NOT NULL
);

CREATE TABLE wilayah_kabupaten (
    kode          TEXT PRIMARY KEY,
    nama          TEXT NOT NULL,
    provinsi_kode TEXT NOT NULL REFERENCES wilayah_provinsi (kode)
);
CREATE INDEX idx_kabupaten_provinsi ON wilayah_kabupaten (provinsi_kode);

CREATE TABLE wilayah_kecamatan (
    kode           TEXT PRIMARY KEY,
    nama           TEXT NOT NULL,
    kabupaten_kode TEXT NOT NULL REFERENCES wilayah_kabupaten (kode)
);
CREATE INDEX idx_kecamatan_kabupaten ON wilayah_kecamatan (kabupaten_kode);

CREATE TABLE wilayah_desa (
    kode           TEXT PRIMARY KEY,
    nama           TEXT NOT NULL,
    kecamatan_kode TEXT NOT NULL REFERENCES wilayah_kecamatan (kode)
);
CREATE INDEX idx_desa_kecamatan ON wilayah_desa (kecamatan_kode);

-- ==================== BASELINE DESA ====================
CREATE TABLE baseline_desa (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                  TIMESTAMPTZ NOT NULL DEFAULT now(),
    tahun_pendataan             INTEGER NOT NULL,
    sumber_data                 TEXT,
    desa_kode                   TEXT NOT NULL UNIQUE REFERENCES wilayah_desa (kode),
    luas_wilayah_ha             DOUBLE PRECISION,
    jumlah_penduduk             INTEGER,
    jumlah_kk                   INTEGER,
    jumlah_petani_kopi          INTEGER,
    luas_areal_kopi_ha          DOUBLE PRECISION,
    luas_komoditi_lain_ha       DOUBLE PRECISION,
    latitude                    DOUBLE PRECISION,
    longitude                   DOUBLE PRECISION,
    topografi                   TEXT,
    ketinggian_mdpl             DOUBLE PRECISION,
    bulan_hujan                 TEXT,
    bulan_kering                TEXT,
    suhu_rata_rata_c            DOUBLE PRECISION,
    jenis_tanah                 TEXT,
    akses_jalan                 TEXT,
    jarak_ibukota_kecamatan_km  DOUBLE PRECISION,
    jarak_pasar_km              DOUBLE PRECISION,
    jarak_konservasi_km         DOUBLE PRECISION,
    luas_apl_ha                 DOUBLE PRECISION,
    nama_kawasan_konservasi     TEXT,
    produktivitas_kg_ha_tahun   DOUBLE PRECISION,
    harga_cherry_rp             INTEGER,
    harga_green_bean_rp_kg      INTEGER,
    pembeli_utama               TEXT,
    jumlah_pedagang_pengumpul   INTEGER,
    koperasi_aktif_unit         INTEGER,
    eksportir                   TEXT,
    industri_pengolahan         TEXT,
    permasalahan_utama          TEXT,
    berbatasan_konservasi       BOOLEAN,
    luas_penyangga_ha           DOUBLE PRECISION,
    tutupan_hutan               DOUBLE PRECISION,
    tutupan_hutan_satuan        TEXT,
    tutupan_agroforestry        DOUBLE PRECISION,
    tutupan_agroforestry_satuan TEXT,
    rawan_longsor               BOOLEAN,
    lokasi_rawan_longsor        TEXT,
    rawan_erosi                 BOOLEAN,
    lokasi_rawan_erosi          TEXT,
    konflik_satwa               BOOLEAN,
    jenis_satwa_konflik         TEXT,
    praktik_konservasi          TEXT,
    created_by_id               UUID NOT NULL REFERENCES users (id)
);
CREATE INDEX idx_baseline_created_by ON baseline_desa (created_by_id);

CREATE TABLE kebijakan_desa (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    jenis       TEXT NOT NULL,
    ada         BOOLEAN NOT NULL DEFAULT FALSE,
    keterangan  TEXT,
    baseline_id UUID NOT NULL REFERENCES baseline_desa (id) ON DELETE CASCADE,
    UNIQUE (baseline_id, jenis)
);

CREATE TABLE kelembagaan_desa (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    jenis       TEXT NOT NULL,
    jumlah      INTEGER,
    kondisi     TEXT,
    baseline_id UUID NOT NULL REFERENCES baseline_desa (id) ON DELETE CASCADE
);

-- ==================== KELOMPOK TANI ====================
CREATE TABLE kelompok_tani (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nama       TEXT NOT NULL,
    kode       TEXT,
    desa_kode  TEXT NOT NULL REFERENCES wilayah_desa (kode),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (desa_kode, kode)
);

-- ==================== BASELINE PETANI ====================
CREATE TABLE petani (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    kode_petani             TEXT UNIQUE,
    nama_lengkap            TEXT NOT NULL,
    nama_panggilan          TEXT,
    jenis_kelamin           TEXT,
    tanggal_lahir           DATE,
    alamat_domisili         TEXT,
    telepon                 TEXT,
    tanggal_pendaftaran     DATE,
    nama_petugas_pendaftar  TEXT,
    kontak_darurat_nama     TEXT,
    kontak_darurat_telepon  TEXT,
    kontak_darurat_hubungan TEXT,
    desa_kode               TEXT NOT NULL REFERENCES wilayah_desa (kode),
    kelompok_tani_id        UUID REFERENCES kelompok_tani (id),
    created_by_id           UUID NOT NULL REFERENCES users (id)
);
CREATE INDEX idx_petani_created_by ON petani (created_by_id);
CREATE INDEX idx_petani_desa ON petani (desa_kode);

CREATE TABLE plot_petani (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nomor                INTEGER NOT NULL,
    petani_id            UUID NOT NULL REFERENCES petani (id) ON DELETE CASCADE,
    nama_hamparan        TEXT,
    varietas             TEXT,
    tahun_tanam          INTEGER[] NOT NULL DEFAULT '{}',
    kode_gps             TEXT,
    elevasi_mdpl         DOUBLE PRECISION,
    kemiringan_persen    DOUBLE PRECISION,
    luas_kopi_ha         DOUBLE PRECISION,
    foto_key             TEXT,
    foto_latitude        DOUBLE PRECISION,
    foto_longitude       DOUBLE PRECISION,
    status_kepemilikan   TEXT,
    sistem_budidaya      TEXT,
    area_konservasi      TEXT,
    tanaman_baru         INTEGER,
    pohon_produktif      INTEGER,
    pohon_tidak_produktif INTEGER,
    pestisida_nama       TEXT,
    pestisida_bulan_tahun TEXT
);
CREATE INDEX idx_plot_petani ON plot_petani (petani_id);

CREATE TABLE tanaman_naungan (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    petani_id          UUID NOT NULL REFERENCES petani (id) ON DELETE CASCADE,
    jenis              TEXT,
    jumlah             INTEGER,
    fungsi             TEXT,
    pemangkasan        BOOLEAN,
    produksi_per_tahun TEXT,
    tahun_tanam        INTEGER
);
CREATE INDEX idx_naungan_petani ON tanaman_naungan (petani_id);

CREATE TABLE praktik_gap (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    jenis      TEXT NOT NULL,
    jawaban    TEXT,
    keterangan TEXT,
    petani_id  UUID NOT NULL REFERENCES petani (id) ON DELETE CASCADE,
    UNIQUE (petani_id, jenis)
);

CREATE TABLE riwayat_produksi (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tahun         INTEGER NOT NULL,
    satuan        TEXT,
    cherry        DOUBLE PRECISION,
    gabah_basah   DOUBLE PRECISION,
    gabah_kering  DOUBLE PRECISION,
    green_bean    DOUBLE PRECISION,
    produktivitas DOUBLE PRECISION,
    petani_id     UUID NOT NULL REFERENCES petani (id) ON DELETE CASCADE,
    UNIQUE (petani_id, tahun)
);

CREATE TABLE produk_dijual (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    jenis           TEXT NOT NULL,
    label_custom    TEXT,
    dijual          BOOLEAN NOT NULL DEFAULT FALSE,
    volume_kg_tahun DOUBLE PRECISION,
    petani_id       UUID NOT NULL REFERENCES petani (id) ON DELETE CASCADE
);
CREATE INDEX idx_produk_petani ON produk_dijual (petani_id);

CREATE TABLE pasar_petani (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    kategori       TEXT NOT NULL,
    label_custom   TEXT,
    aktif          BOOLEAN NOT NULL DEFAULT FALSE,
    persentase     DOUBLE PRECISION,
    profil_penjual TEXT,
    petani_id      UUID NOT NULL REFERENCES petani (id) ON DELETE CASCADE
);
CREATE INDEX idx_pasar_petani ON pasar_petani (petani_id);

CREATE TABLE kondisi_kebun (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    jenis      TEXT NOT NULL,
    jawaban    BOOLEAN NOT NULL DEFAULT FALSE,
    keterangan TEXT,
    petani_id  UUID NOT NULL REFERENCES petani (id) ON DELETE CASCADE,
    UNIQUE (petani_id, jenis)
);

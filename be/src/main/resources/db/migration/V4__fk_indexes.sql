-- Indeks FK yang belum ada: dipakai oleh count petani per kelompok (analitik
-- kelompok & guard hapus), dan lookup anak per baseline.
CREATE INDEX IF NOT EXISTS idx_petani_kelompok ON petani (kelompok_tani_id);
CREATE INDEX IF NOT EXISTS idx_kelembagaan_baseline ON kelembagaan_desa (baseline_id);

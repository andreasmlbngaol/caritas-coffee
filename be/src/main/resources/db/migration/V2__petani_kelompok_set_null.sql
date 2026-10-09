-- Prisma lama memakai onDelete: SetNull untuk relasi Petani -> KelompokTani.
-- V1 mendeklarasikan FK inline tanpa perilaku ON DELETE, jadi constraint diberi
-- nama otomatis Postgres: petani_kelompok_tani_id_fkey. Ganti dengan SET NULL.
ALTER TABLE petani DROP CONSTRAINT petani_kelompok_tani_id_fkey;
ALTER TABLE petani ADD CONSTRAINT petani_kelompok_tani_id_fkey
    FOREIGN KEY (kelompok_tani_id) REFERENCES kelompok_tani (id) ON DELETE SET NULL;

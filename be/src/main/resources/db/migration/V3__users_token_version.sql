-- Sesi JWT bisa dibatalkan: reset password / (de)aktivasi menaikkan versi token
-- sehingga token lama langsung tidak berlaku (tanpa menunggu TTL).
ALTER TABLE users ADD COLUMN token_version INTEGER NOT NULL DEFAULT 0;

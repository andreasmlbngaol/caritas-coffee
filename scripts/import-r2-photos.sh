#!/usr/bin/env bash
# Pindahkan FOTO dari Cloudflare R2 ke storage lokal (volume Docker `uploads`).
#
# Latar: key di DB (kolom `petani.foto_key`) berbentuk `petani/<uuid>.<ext>`,
# dan storage lokal menyimpan file persis di path itu di bawah STORAGE_PATH.
# Jadi TIDAK perlu ubah data DB - cukup pindahkan byte-nya dari R2 ke folder
# `petani/` di dalam volume. Nama file = key, ekstensi menentukan Content-Type.
#
# Pakai:
#   R2_ENDPOINT=... R2_ACCESS_KEY_ID=... R2_SECRET_ACCESS_KEY=... R2_BUCKET_NAME=... \
#     scripts/import-r2-photos.sh [prod|dev]
# (utamakan lewat env, bukan argumen, agar kredensial tidak masuk history/ps)
set -euo pipefail
cd "$(dirname "$0")/.."

TARGET="${1:-prod}"
case "$TARGET" in
  prod) COMPOSE=(docker compose --env-file .env -f docker-compose.yml) ;;
  dev)  COMPOSE=(docker compose --env-file .env.dev -f docker-compose.dev.yaml) ;;
  *)    echo "ERROR: argumen harus 'prod' atau 'dev' (diberi: $TARGET)" >&2; exit 1 ;;
esac

: "${R2_ENDPOINT:?Set R2_ENDPOINT}"
: "${R2_ACCESS_KEY_ID:?Set R2_ACCESS_KEY_ID}"
: "${R2_SECRET_ACCESS_KEY:?Set R2_SECRET_ACCESS_KEY}"
: "${R2_BUCKET_NAME:?Set R2_BUCKET_NAME}"

command -v rclone >/dev/null 2>&1 || {
  echo "ERROR: rclone tidak ada. Pasang dulu: https://rclone.org/install/" >&2; exit 1
}

REMOTE=kopi-r2
STAGING="$(mktemp -d -t kopi-r2-XXXXXX)"
trap 'rm -rf "$STAGING"' EXIT

echo "==> 1/4 Konfigurasi rclone untuk R2 (target: $TARGET)"
# endpoint dinormalisasi tanpa slash di ujung (rclone menolaknya).
rclone config create "$REMOTE" s3 \
  provider=Cloudflare \
  access_key_id="$R2_ACCESS_KEY_ID" \
  secret_access_key="$R2_SECRET_ACCESS_KEY" \
  endpoint="${R2_ENDPOINT%/}" >/dev/null

echo "==> 2/4 Unduh semua objek dari R2 (bucket: $R2_BUCKET_NAME)"
rclone copy "$REMOTE:$R2_BUCKET_NAME/petani" "$STAGING/petani" --transfers 16 --checkers 16

COUNT_LOCAL=$(find "$STAGING/petani" -type f | wc -l | tr -d ' ')
[[ "$COUNT_LOCAL" -gt 0 ]] || { echo "ERROR: tidak ada file terunduh. Cek kredensial/bucket/prefix." >&2; exit 1; }
echo "    terunduh: $COUNT_LOCAL file"

echo "==> 3/4 Nyalakan be & salin ke volume uploads"
"${COMPOSE[@]}" up -d be
CID=$("${COMPOSE[@]}" ps -q be)
[[ -n "$CID" ]] || { echo "ERROR: container be tidak jalan." >&2; exit 1; }

# STORAGE_PATH di container (default /app/data/uploads); ambil dari env container.
STORAGE_PATH=$(docker exec "$CID" printenv STORAGE_PATH 2>/dev/null || echo /app/data/uploads)
docker exec "$CID" mkdir -p "$STORAGE_PATH/petani"
docker cp "$STAGING/petani/." "$CID:$STORAGE_PATH/petani/"
# File masuk sebagai root; pastikan user app tetap bisa baca.
docker exec -u 0 "$CID" chown -R app:app "$STORAGE_PATH/petani" 2>/dev/null || true

echo "==> 4/4 Verifikasi (file di disk vs foto_key unik di DB)"
COUNT_DISK=$(docker exec "$CID" sh -c "ls '$STORAGE_PATH/petani' | wc -l" | tr -d ' ')
COUNT_DB=$("${COMPOSE[@]}" exec -T db psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -tAc \
  "SELECT count(DISTINCT foto_key) FROM petani WHERE foto_key IS NOT NULL;" 2>/dev/null | tr -d ' ')

echo "    file di disk : $COUNT_DISK"
echo "    foto_key di DB: ${COUNT_DB:-?}"
if [[ -n "${COUNT_DB:-}" && "$COUNT_DB" -gt "$COUNT_DISK" ]]; then
  echo "    PERINGATAN: ada foto_key di DB tanpa file di disk. Cek daftar di bawah:"
  "${COMPOSE[@]}" exec -T db psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -tAc \
    "SELECT foto_key FROM petani WHERE foto_key IS NOT NULL;" \
    | while read -r k; do
        [[ -n "$k" ]] && docker exec "$CID" sh -c "test -f '$STORAGE_PATH/$k'" 2>/dev/null \
          || echo "      hilang: $k"
      done
fi
echo
echo "Selesai. Foto tersaji lewat GET /api/foto/{key} tanpa mengubah data DB."

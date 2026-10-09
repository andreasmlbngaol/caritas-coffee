#!/usr/bin/env bash
# Impor DATA dari Supabase (skema Prisma lama) ke DB DEV lokal.
# Skema lokal dibuat oleh Flyway; skrip ini HANYA memindahkan data.
# Wilayah TIDAK diimpor (sudah diisi WilayahSeeder saat BE start).
#
# Pakai:
#   SUPABASE_DB_URL='postgresql://postgres.<ref>:<pw>@<host>:5432/postgres' \
#     scripts/import-supabase.sh
# (utamakan lewat env, bukan argumen, agar password tidak masuk history/ps)
set -euo pipefail
cd "$(dirname "$0")/.."

SUPABASE_DB_URL="${1:-${SUPABASE_DB_URL:-}}"
[[ -n "$SUPABASE_DB_URL" ]] || { echo "ERROR: SUPABASE_DB_URL kosong." >&2; exit 1; }

COMPOSE=(docker compose --env-file .env.dev -f docker-compose.dev.yaml)
DB_USER=kopi_dev
DB_NAME=kopi_db_dev

DUMP_FILE="$(mktemp -t kopi-supabase-XXXXXX.sql)"
trap 'rm -f "$DUMP_FILE"' EXIT

APP_TABLES=(users baseline_desa kebijakan_desa kelembagaan_desa kelompok_tani \
  petani plot_petani tanaman_naungan praktik_gap riwayat_produksi produk_dijual \
  pasar_petani kondisi_kebun)

echo "==> 1/5 Nyalakan DB dev (Flyway + seeder wilayah dijalankan oleh BE)"
"${COMPOSE[@]}" up -d
printf '    menunggu BE siap'
for _ in $(seq 1 60); do
  code=$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8091/api/auth/me 2>/dev/null || true)
  [[ "$code" == "401" ]] && { echo " -> siap"; break; }
  printf '.'; sleep 5
done

echo "==> 2/5 Dump data dari Supabase"
DUMP_ARGS=()
for t in "${APP_TABLES[@]}"; do DUMP_ARGS+=(--table="public.$t"); done
# Tulis lewat stdout (redirect di host) supaya tidak kena masalah izin
# user `postgres` di dalam container terhadap file milik host.
docker run --rm --network host postgres:17-alpine \
  pg_dump "$SUPABASE_DB_URL" --data-only --no-owner --no-privileges \
  "${DUMP_ARGS[@]}" > "$DUMP_FILE"

# Jangan sampai truncate kalau dump gagal/kosong.
if [[ ! -s "$DUMP_FILE" ]] || ! grep -q '^COPY ' "$DUMP_FILE"; then
  echo "ERROR: dump kosong atau gagal. Cek SUPABASE_DB_URL & koneksi. Dibatalkan." >&2
  exit 1
fi
echo "    dump: $(wc -l < "$DUMP_FILE") baris"

CID=$("${COMPOSE[@]}" ps -q db)
[[ -n "$CID" ]] || { echo "ERROR: container db tidak jalan." >&2; exit 1; }

echo "==> 3/5 Kosongkan tabel aplikasi di DB dev (wilayah dibiarkan)"
TRUNCATE_LIST=$(IFS=,; echo "${APP_TABLES[*]}")
docker exec -i "$CID" psql -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 \
  -c "TRUNCATE ${TRUNCATE_LIST} CASCADE;"

echo "==> 4/5 Restore ke DB dev"
docker exec -i "$CID" psql -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 -1 < "$DUMP_FILE"

echo "==> 5/5 Ringkasan"
docker exec -i "$CID" psql -U "$DB_USER" -d "$DB_NAME" -c \
  "SELECT role, count(*) AS jumlah FROM users GROUP BY role ORDER BY role;"
docker exec -i "$CID" psql -U "$DB_USER" -d "$DB_NAME" -c \
  "SELECT (SELECT count(*) FROM petani) AS petani,
          (SELECT count(*) FROM baseline_desa) AS desa,
          (SELECT count(*) FROM kelompok_tani) AS kelompok_tani;"
echo
echo "PENTING: agar AdminSeeder tidak membuat admin lokal yang bentrok,"
echo "set ADMIN_USERNAME di .env.dev ke salah satu username ADMIN di atas,"
echo "atau kosongkan (ADMIN_USERNAME=), lalu jalankan: ${COMPOSE[*]} up -d"

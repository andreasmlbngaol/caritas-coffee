#!/usr/bin/env bash
#
# migrate-data.sh - Pindahkan data dari sistem lama (Supabase + Cloudflare R2)
# ke VPS ini (Postgres lokal + volume Docker `uploads`).
#
# Alur (interaktif; kredensial lama TIDAK ditulis ke disk):
#   1. Cek stack Docker sudah jalan (kalau belum, `docker compose up -d`).
#   2. Minta connection string Supabase + kredensial R2.
#   3. TRUNCATE tabel tujuan, lalu COPY data per-tabel (kolom yang beririsan
#      saja, jadi toleran terhadap perbedaan skema lama vs baru).
#   4. Sinkron seluruh prefix `petani/` dari R2 ke volume `uploads`.
#   5. Verifikasi jumlah baris + jumlah foto.
#
# PENTING: setelah migrasi, akun login = akun dari sistem LAMA (password bcrypt
# kompatibel). Admin yang dibuat setup-vps.sh akan tertimpa.
#
# Jalankan sebagai root:  sudo ./scripts/migrate-data.sh
#
set -euo pipefail

RED=$'\033[31m'; GREEN=$'\033[32m'; YELLOW=$'\033[33m'; BLUE=$'\033[34m'; BOLD=$'\033[1m'; NC=$'\033[0m'
info() { printf '%s\n' "${BLUE}==>${NC} $*"; }
ok()   { printf '%s\n' "${GREEN}OK${NC}  $*"; }
warn() { printf '%s\n' "${YELLOW}!${NC}   $*"; }
err()  { printf '%s\n' "${RED}ERR${NC} $*" >&2; }
die()  { err "$*"; exit 1; }
have() { command -v "$1" >/dev/null 2>&1; }

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_DIR"

[ -f .env ] || die ".env tidak ada. Jalankan dulu: sudo ./scripts/setup-vps.sh"
# shellcheck disable=SC1091
set -a; . ./.env; set +a

# Urutan muat: orang tua sebelum anak (memenuhi FK).
TABLES=(
  wilayah_provinsi wilayah_kabupaten wilayah_kecamatan wilayah_desa
  users kelompok_tani
  baseline_desa kebijakan_desa kelembagaan_desa
  petani plot_petani tanaman_naungan praktik_gap riwayat_produksi
  produk_dijual pasar_petani kondisi_kebun
)

# ---------- 0. stack harus jalan ----------
if ! docker compose ps --status running 2>/dev/null | grep -q 'db'; then
  info "Stack belum jalan - menjalankan docker compose up -d..."
  docker compose up -d
  info "Menunggu DB sehat..."
  for i in $(seq 1 60); do
    if docker compose exec -T db pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB" >/dev/null 2>&1; then break; fi
    sleep 2
  done
fi
docker compose exec -T db pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB" >/dev/null 2>&1 \
  || die "DB tidak sehat. Cek: docker compose logs db"
ok "Stack jalan & DB sehat."

psql_dst() { docker compose exec -T db psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" "$@"; }

# ---------- 1. Kredensial sumber ----------
printf '\n%s\n' "${BOLD}--- Sumber data lama ---${NC}"
printf '%s\n' "Gunakan connection string DIRECT Supabase (port 5432, sertakan sslmode=require)."
printf '%s\n' "Kredensial hanya dipakai saat ini, tidak ditulis ke disk."
read -rsp "DATABASE_URL Supabase: " SRC_URL; echo
[ -n "$SRC_URL" ] || die "DATABASE_URL wajib."
case "$SRC_URL" in *sslmode=*) ;; *) warn "URL tanpa sslmode - Supabase biasanya butuh sslmode=require.";; esac

psql_src() { docker compose exec -T db psql -v ON_ERROR_STOP=1 "$SRC_URL" "$@"; }

info "Menguji koneksi ke sumber..."
if ! psql_src -tAc "select 1" >/dev/null 2>&1; then
  die "Gagal konek ke Supabase. Periksa connection string & izin jaringan."
fi
ok "Koneksi sumber OK."

SRC_USERS=$(psql_src -tAc "select count(*) from users" 2>/dev/null || echo "?")
SRC_PETANI=$(psql_src -tAc "select count(*) from petani" 2>/dev/null || echo "?")
printf '  Sumber: users=%s, petani=%s\n' "$SRC_USERS" "$SRC_PETANI"

printf '\n%s\n' "${BOLD}--- Cloudflare R2 (foto) ---${NC}"
read -rp "R2 endpoint (https://<acct>.r2.cloudflarestorage.com): " R2_ENDPOINT
read -rp "R2 bucket name: " R2_BUCKET
read -rsp "R2 access key id: " R2_ACCESS_KEY_ID; echo
read -rsp "R2 secret access key: " R2_SECRET_ACCESS_KEY; echo

read -rp $'\nLanjut migrasi? Data di DB tujuan akan DITIMPA. [Y/n]: ' GO
case "${GO:-y}" in [Yy]*) ;; *) die "Dibatalkan." ;; esac

# ---------- 2. TRUNCATE tujuan ----------
info "Mengosongkan tabel tujuan..."
TRUNC_LIST=$(IFS=,; echo "${TABLES[*]}")
psql_dst -c "TRUNCATE ${TRUNC_LIST} CASCADE;"
ok "Tabel tujuan dikosongkan."

# ---------- 3. COPY per-tabel (kolom beririsan) ----------
# Kolom beririsan = kolom yang ada di sumber DAN tujuan. Skema baru bisa punya
# kolom tambahan (mis. users.token_version) yang terisi default - tidak disalin.
copy_table() {
  local t=$1
  local src_cols dst_cols cols="" c
  src_cols=$(psql_src -tAc "select coalesce(string_agg(column_name,',' order by ordinal_position),'') from information_schema.columns where table_schema='public' and table_name='${t}'")
  dst_cols=$(psql_dst -tAc "select coalesce(string_agg(column_name,',' order by ordinal_position),'') from information_schema.columns where table_schema='public' and table_name='${t}'")
  if [ -z "$src_cols" ]; then warn "  $t: tidak ada di sumber - lewati."; return; fi
  if [ -z "$dst_cols" ]; then warn "  $t: tidak ada di tujuan - lewati."; return; fi
  local IFS=','
  for c in $dst_cols; do
    case ",${src_cols}," in *",${c},"*) cols="${cols:+$cols,}${c}";; esac
  done
  unset IFS
  [ -n "$cols" ] || { warn "  $t: tidak ada kolom beririsan - lewati."; return; }

  psql_src -c "\copy (select ${cols} from public.${t}) to stdout" \
    | psql_dst -c "\copy public.${t}(${cols}) from stdin" >/dev/null
  local n
  n=$(psql_dst -tAc "select count(*) from ${t}")
  printf '  %-22s -> %s baris\n' "$t" "$n"
}

info "Menyalin data..."
for t in "${TABLES[@]}"; do copy_table "$t"; done
ok "Data DB selesai disalin."

# ---------- 4. Foto dari R2 ----------
printf '\n'
read -rp "Sinkron foto dari R2 sekarang? [Y/n]: " DO_R2
if [[ ! "${DO_R2:-y}" =~ ^[Yy]$ ]]; then
  warn "Lewati sinkron foto. Key foto di DB menunjuk ke objek R2 yang belum ada di VPS."
else
  if ! have rclone; then
    info "Memasang rclone..."
    apt-get update -y && apt-get install -y rclone
  fi
  TMP_PHOTOS=$(mktemp -d /var/tmp/ck-photos.XXXXXX)
  trap 'rm -rf "$TMP_PHOTOS"' EXIT
  export RCLONE_CONFIG_R2_TYPE=s3
  export RCLONE_CONFIG_R2_PROVIDER=Cloudflare
  export RCLONE_CONFIG_R2_ENDPOINT="$R2_ENDPOINT"
  export RCLONE_CONFIG_R2_ACCESS_KEY_ID="$R2_ACCESS_KEY_ID"
  export RCLONE_CONFIG_R2_SECRET_ACCESS_KEY="$R2_SECRET_ACCESS_KEY"
  info "Mengunduh prefix petani/ dari R2 (bisa lama)..."
  rclone copy "r2:${R2_BUCKET}/petani" "$TMP_PHOTOS/petani" --transfers 8 --checkers 16 --progress
  COUNT=$(find "$TMP_PHOTOS/petani" -type f 2>/dev/null | wc -l)
  ok "Terunduh $COUNT file."

  VOL=$(docker volume ls -q | grep -E '_uploads$' | head -1 || true)
  [ -n "$VOL" ] || die "Volume uploads tidak ditemukan. Pastikan stack pernah jalan."
  info "Menyalin ke volume Docker: $VOL"
  docker run --rm -v "${VOL}:/data" -v "${TMP_PHOTOS}:/src:ro" alpine \
    sh -c 'mkdir -p /data/petani && cp -rn /src/petani/. /data/petani/'
  IN_VOL=$(docker run --rm -v "${VOL}:/data" alpine sh -c 'find /data/petani -type f 2>/dev/null | wc -l')
  ok "File di volume uploads: $IN_VOL"
fi

# ---------- 5. Ringkasan ----------
printf '\n%s\n' "${BOLD}=== Migrasi selesai ===${NC}"
printf '  %-22s %s\n' "Tabel" "Baris"
for t in "${TABLES[@]}"; do
  n=$(psql_dst -tAc "select count(*) from ${t}" 2>/dev/null || echo "?")
  printf '  %-22s %s\n' "$t" "$n"
done
printf '\n'
ok "Login sekarang memakai akun dari sistem LAMA (password lama tetap berlaku)."
info "Restart BE agar cache/sesi bersih: docker compose restart be"

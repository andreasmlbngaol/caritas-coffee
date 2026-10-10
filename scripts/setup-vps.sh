#!/usr/bin/env bash
#
# setup-vps.sh - Deploy Database Kopi (Caritas Kopi) ke VPS Debian 13.
#
# Yang dilakukan (interaktif, idempotent - aman dijalankan ulang):
#   1. Cek OS Debian, root/sudo, tool dasar.
#   2. Install Docker (apt repo resmi) bila belum ada.
#   3. Tanya nilai konfigurasi (Enter = pakai default) -> tulis .env.
#   4. docker compose up -d --build, tunggu BE sehat.
#   5. Pasang nginx host + vhost subdomain -> proxy ke container FE.
#   6. Terbitkan sertifikat Let's Encrypt (certbot) + auto-renew.
#   7. Amankan firewall (ufw) bila tersedia.
#   8. Pasang backup harian (pg_dump + volume foto), retensi 14 hari.
#   9. Verifikasi akhir.
#
# Jalankan sebagai root (atau via sudo):  sudo ./scripts/setup-vps.sh
#
set -euo pipefail

# ---------- util ----------
RED=$'\033[31m'; GREEN=$'\033[32m'; YELLOW=$'\033[33m'; BLUE=$'\033[34m'; BOLD=$'\033[1m'; NC=$'\033[0m'
info() { printf '%s\n' "${BLUE}==>${NC} $*"; }
ok()   { printf '%s\n' "${GREEN}OK${NC}  $*"; }
warn() { printf '%s\n' "${YELLOW}!${NC}   $*"; }
err()  { printf '%s\n' "${RED}ERR${NC} $*" >&2; }
die()  { err "$*"; exit 1; }
have() { command -v "$1" >/dev/null 2>&1; }

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
ENV_FILE="$REPO_DIR/.env"
BACKUP_DIR="/var/backups/caritas-kopi"
BACKUP_RETENTION_DAYS=14

# ---------- 0. root & OS ----------
if [ "$(id -u)" -ne 0 ]; then
  if have sudo; then
    info "Butuh root - menjalankan ulang dengan sudo..."
    exec sudo -E bash "$0" "$@"
  fi
  die "Jalankan sebagai root: sudo $0"
fi

[ -f /etc/os-release ] || die "/etc/os-release tidak ada - bukan Linux?"
# shellcheck disable=SC1091
. /etc/os-release
if [ "${ID:-}" != "debian" ]; then
  warn "OS terdeteksi: ${PRETTY_NAME:-unknown}. Script dioptimalkan untuk Debian 13 - lanjut dengan hati-hati."
fi
info "OS: ${PRETTY_NAME:-unknown} (${VERSION_CODENAME:-?})"

# ---------- 1. tool dasar ----------
info "Memeriksa tool dasar (git, curl, openssl, ca-certificates)..."
MISSING=()
for t in git curl openssl; do have "$t" || MISSING+=("$t"); done
if [ "${#MISSING[@]}" -gt 0 ]; then
  info "Memasang: ${MISSING[*]}"
  apt-get update -y
  apt-get install -y ca-certificates "${MISSING[@]}"
fi
ok "Tool dasar siap."

# ---------- 2. Docker ----------
install_docker() {
  info "Memasang Docker (apt repo resmi)..."
  apt-get update -y
  apt-get install -y ca-certificates curl gnupg
  install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc
  chmod a+r /etc/apt/keyrings/docker.asc
  echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian ${VERSION_CODENAME} stable" \
    > /etc/apt/sources.list.d/docker.list
  apt-get update -y
  apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
  systemctl enable --now docker
  ok "Docker terpasang."
}

if ! have docker; then
  read -rp "Docker belum terpasang. Install sekarang? [Y/n]: " ans
  case "${ans:-y}" in
    [Yy]*) install_docker ;;
    *) die "Docker wajib. Batalkan." ;;
  esac
else
  ok "Docker sudah ada: $(docker --version)"
fi

if ! docker compose version >/dev/null 2>&1; then
  warn "Plugin 'docker compose' tidak ditemukan - memasang docker-compose-plugin..."
  apt-get update -y && apt-get install -y docker-compose-plugin
fi
ok "Compose: $(docker compose version | head -1)"

# ---------- helper prompt ----------
gen_pass() { openssl rand -base64 18 | tr -d '/+=' | cut -c1-20; }
gen_hex()  { openssl rand -hex 32; }

# Baca default dari .env lama (bila ada) agar aman dijalankan ulang.
env_default() {
  local key=$1
  [ -f "$ENV_FILE" ] || return 0
  grep -E "^${key}=" "$ENV_FILE" | head -1 | cut -d= -f2- | sed 's/^"//; s/"$//'
}

ask() { # var, prompt, default
  local __v=$1 prompt=$2 default=${3:-} val
  if [ -n "$default" ]; then read -rp "${prompt} [${default}]: " val; else read -rp "${prompt}: " val; fi
  printf -v "$__v" '%s' "${val:-$default}"
}

ask_secret() { # var, prompt, default(optional)
  local __v=$1 prompt=$2 default=${3:-} val
  if [ -n "$default" ]; then
    read -rsp "${prompt} [Enter = pakai default]: " val; echo
  else
    read -rsp "${prompt} (kosong = generate acak): " val; echo
  fi
  printf -v "$__v" '%s' "${val:-$default}"
}

# ---------- 3. Konfigurasi ----------
printf '\n%s\n' "${BOLD}--- Konfigurasi aplikasi ---${NC}"
printf '%s\n' "Tekan Enter untuk memakai nilai default di dalam [kurung]."

ask DOMAIN "Domain/subdomain publik (mis. kopi.perusahaan.com)" "$(env_default APP_URL | sed 's#https\?://##')"
[ -n "$DOMAIN" ] || die "Domain wajib diisi."
ask EMAIL "Email untuk Let's Encrypt (notifikasi kadaluarsa)" ""

APP_URL_DEFAULT="https://${DOMAIN}"
ask APP_URL "URL publik aplikasi (APP_URL)" "$(env_default APP_URL | grep . || echo "$APP_URL_DEFAULT")"

COOKIE_SECURE_DEFAULT="$(env_default COOKIE_SECURE)"; COOKIE_SECURE_DEFAULT=${COOKIE_SECURE_DEFAULT:-true}
ask COOKIE_SECURE "Cookie sesi hanya via HTTPS? (true di produksi; false saat uji HTTP lokal)" "$COOKIE_SECURE_DEFAULT"

DB_NAME=$(env_default POSTGRES_DB); DB_NAME=${DB_NAME:-kopi_db}
DB_USER=$(env_default POSTGRES_USER); DB_USER=${DB_USER:-kopi}
ask POSTGRES_DB "Nama database Postgres" "$DB_NAME"
ask POSTGRES_USER "User Postgres" "$DB_USER"
GEN_DB_PASS="$(env_default POSTGRES_PASSWORD)"
ask_secret POSTGRES_PASSWORD "Password Postgres" "${GEN_DB_PASS:-$(gen_pass)}"

JWT_DEFAULT="$(env_default JWT_SECRET)"; JWT_DEFAULT=${JWT_DEFAULT:-$(gen_hex)}
ask_secret JWT_SECRET "JWT secret" "$JWT_DEFAULT"

ADM_U=$(env_default ADMIN_USERNAME); ADM_U=${ADM_U:-caritas}
ADM_P="$(env_default ADMIN_PASSWORD)"
ask ADMIN_USERNAME "Username admin awal" "$ADM_U"
ask_secret ADMIN_PASSWORD "Password admin awal" "${ADM_P:-$(gen_pass)}"

FE_P=$(env_default FE_PORT); FE_P=${FE_P:-8088}
BE_P=$(env_default BE_PORT); BE_P=${BE_P:-8089}
DB_P=$(env_default POSTGRES_PORT); DB_P=${DB_P:-5544}
ask FE_PORT "Port internal FE (nginx host -> container)" "$FE_P"
ask BE_PORT "Port internal BE (localhost saja)" "$BE_P"
ask POSTGRES_PORT "Port internal Postgres (localhost saja)" "$DB_P"

printf '\n%s\n' "${BOLD}Ringkasan:${NC}"
printf '  Domain        : %s\n' "$DOMAIN"
printf '  APP_URL       : %s\n' "$APP_URL"
printf '  Postgres      : %s / %s (db=%s)\n' "$POSTGRES_USER" "********" "$POSTGRES_DB"
printf '  Admin         : %s\n' "$ADMIN_USERNAME"
printf '  Port FE/BE/DB : %s / %s / %s (bind 127.0.0.1)\n' "$FE_PORT" "$BE_PORT" "$POSTGRES_PORT"
read -rp $'\nLanjut tulis .env dan deploy? [Y/n]: ' GO
case "${GO:-y}" in [Yy]*) ;; *) die "Dibatalkan." ;; esac

# ---------- 4. Tulis .env ----------
if [ -f "$ENV_FILE" ]; then
  cp "$ENV_FILE" "${ENV_FILE}.bak.$(date +%Y%m%d_%H%M%S)"
  info "Backup .env lama dibuat."
fi
umask 077
cat > "$ENV_FILE" <<EOF
# Dibuat otomatis oleh scripts/setup-vps.sh pada $(date -Is)
POSTGRES_DB="$POSTGRES_DB"
POSTGRES_USER="$POSTGRES_USER"
POSTGRES_PASSWORD="$POSTGRES_PASSWORD"
POSTGRES_PORT=$POSTGRES_PORT

BE_PORT=$BE_PORT
FE_PORT=$FE_PORT

JWT_SECRET="$JWT_SECRET"
ADMIN_USERNAME="$ADMIN_USERNAME"
ADMIN_PASSWORD="$ADMIN_PASSWORD"

STORAGE_PATH=/app/data/uploads
APP_URL="$APP_URL"
COOKIE_SECURE=$COOKIE_SECURE
CORS_ALLOWED_ORIGINS=""

# Bind port container ke localhost (nginx host yang jadi gerbang publik).
BIND_HOST=127.0.0.1
EOF
chmod 600 "$ENV_FILE"
ok ".env ditulis: $ENV_FILE"

# ---------- 5. Build & up ----------
info "Build & jalankan container (docker compose up -d --build)..."
cd "$REPO_DIR"
docker compose up -d --build

info "Menunggu BE siap (Flyway + seed)..."
BE_READY=0
for i in $(seq 1 90); do
  code=$(curl -s -o /dev/null -w '%{http_code}' "http://127.0.0.1:${BE_PORT}/api/auth/me" || true)
  if [ "$code" != "000" ] && [ -n "$code" ]; then BE_READY=1; break; fi
  sleep 2
done
[ "$BE_READY" = "1" ] && ok "BE merespons (HTTP $code)." || die "BE tidak siap setelah ~3 menit. Cek: docker compose logs be"
docker compose ps

# ---------- 6. nginx host + vhost ----------
if ! have nginx; then
  info "Memasang nginx..."
  apt-get install -y nginx
fi
VHOST=/etc/nginx/sites-available/caritas-kopi
info "Menulis vhost nginx: $VHOST"
cat > "$VHOST" <<EOF
# Database Kopi - reverse proxy ke container FE (same-origin, /api ikut lewat).
server {
    listen 80;
    listen [::]:80;
    server_name ${DOMAIN};

    client_max_body_size 12m;

    location / {
        proxy_pass http://127.0.0.1:${FE_PORT};
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
EOF
ln -sf "$VHOST" /etc/nginx/sites-enabled/caritas-kopi
nginx -t
systemctl reload nginx
ok "nginx vhost aktif untuk ${DOMAIN}."

# ---------- 7. HTTPS (certbot) ----------
printf '\n'
PUB_IP=$(curl -fsS https://api.ipify.org 2>/dev/null || curl -fsS https://ifconfig.me 2>/dev/null || echo "")
DOM_IP=$(getent hosts "$DOMAIN" | awk '{print $1}' | head -1 || true)
info "IP publik VPS : ${PUB_IP:-?}"
info "IP DNS ${DOMAIN} : ${DOM_IP:-? (belum resolve)}"
if [ -n "$PUB_IP" ] && [ -n "$DOM_IP" ] && [ "$PUB_IP" != "$DOM_IP" ]; then
  warn "DNS belum menunjuk ke VPS ini. certbot akan gagal sampai A record benar."
fi

if [ -z "$EMAIL" ]; then
  warn "Email kosong - lewati certbot. Jalankan manual:"
  warn "  apt-get install -y certbot python3-certbot-nginx"
  warn "  certbot --nginx -d ${DOMAIN} --agree-tos -m you@example.com --redirect"
elif [ -z "$DOM_IP" ]; then
  warn "Domain belum resolve - lewati certbot. Setelah DNS benar, jalankan:"
  warn "  certbot --nginx -d ${DOMAIN} --agree-tos -m ${EMAIL} --redirect"
else
  info "Memasang certbot & menerbitkan sertifikat..."
  apt-get install -y certbot python3-certbot-nginx
  if certbot --nginx -d "$DOMAIN" --non-interactive --agree-tos -m "$EMAIL" --redirect; then
    ok "HTTPS aktif. Auto-renew lewat systemd timer certbot."
    systemctl list-timers 2>/dev/null | grep -i certbot || true
  else
    warn "certbot gagal (biasanya DNS belum propagate). Coba lagi nanti:"
    warn "  certbot --nginx -d ${DOMAIN} --agree-tos -m ${EMAIL} --redirect"
  fi
fi

# ---------- 8. Firewall (ufw) ----------
if have ufw; then
  # Deteksi port SSH aktif agar tidak mengunci diri sendiri bila bukan 22.
  SSH_PORT=$(ss -ltnp 2>/dev/null | grep -oE ':([0-9]+) ' | tr -d ': ' | sort -u | grep -E '^(22|2222)$' | head -1)
  SSH_PORT=${SSH_PORT:-$(awk '/^Port /{print $2; exit}' /etc/ssh/sshd_config 2>/dev/null || true)}
  SSH_PORT=${SSH_PORT:-22}
  info "Mengatur ufw (SSH ${SSH_PORT}, 80, 443)..."
  ufw allow "${SSH_PORT}/tcp" >/dev/null 2>&1 || true
  ufw allow 80/tcp >/dev/null 2>&1 || true
  ufw allow 443/tcp >/dev/null 2>&1 || true
  ufw --force enable >/dev/null 2>&1 || true
  ok "ufw aktif (SSH ${SSH_PORT}, 80, 443). Port DB/BE tidak dibuka (bind 127.0.0.1)."
  if [ "$SSH_PORT" != "22" ]; then
    warn "SSH terdeteksi di port ${SSH_PORT}. Pastikan koneksimu masih jalan sebelum menutup sesi!"
  fi
else
  warn "ufw tidak terpasang. Bila ingin firewall, jalankan:"
  warn "  apt-get install -y ufw && ufw allow 22/tcp && ufw allow 80/tcp && ufw allow 443/tcp && ufw --force enable"
fi

# ---------- 9. Backup harian ----------
info "Memasang backup harian ke ${BACKUP_DIR} (retensi ${BACKUP_RETENTION_DAYS} hari)..."
mkdir -p "$BACKUP_DIR"
cat > /usr/local/bin/caritas-kopi-backup.sh <<EOF
#!/usr/bin/env bash
# pg_dump + arsip volume foto. Dipasang oleh setup-vps.sh.
set -euo pipefail
REPO_DIR="${REPO_DIR}"
BACKUP_DIR="${BACKUP_DIR}"
RETENTION=${BACKUP_RETENTION_DAYS}
cd "\$REPO_DIR"
set -a; . ./.env; set +a
mkdir -p "\$BACKUP_DIR"
STAMP=\$(date +%F_%H%M)

# 1) Database
docker compose exec -T db pg_dump -U "\$POSTGRES_USER" -d "\$POSTGRES_DB" | gzip > "\$BACKUP_DIR/db-\$STAMP.sql.gz"

# 2) Volume foto (uploads)
VOL=\$(docker volume ls -q | grep -E '_uploads\$' | head -1 || true)
if [ -n "\$VOL" ]; then
  docker run --rm -v "\$VOL":/data:ro -v "\$BACKUP_DIR":/backup alpine \\
    tar czf "/backup/uploads-\$STAMP.tar.gz" -C /data . || true
fi

# 3) Retensi
find "\$BACKUP_DIR" -name '*.gz' -mtime +\$RETENTION -delete
echo "\$(date -Is) backup selesai: \$STAMP"
EOF
chmod +x /usr/local/bin/caritas-kopi-backup.sh
cat > /etc/cron.d/caritas-kopi-backup <<EOF
# Backup harian Database Kopi (jam 02:30).
30 2 * * * root /usr/local/bin/caritas-kopi-backup.sh >> /var/log/caritas-kopi-backup.log 2>&1
EOF
chmod 644 /etc/cron.d/caritas-kopi-backup
ok "Cron backup terpasang (/etc/cron.d/caritas-kopi-backup)."

# ---------- 10. Verifikasi ----------
printf '\n%s\n' "${BOLD}--- Verifikasi ---${NC}"
FE_CODE=$(curl -s -o /dev/null -w '%{http_code}' "http://127.0.0.1:${FE_PORT}/" || echo "000")
API_CODE=$(curl -s -o /dev/null -w '%{http_code}' "http://127.0.0.1:${FE_PORT}/api/auth/me" || echo "000")
printf '  FE  http://127.0.0.1:%s/          -> HTTP %s\n' "$FE_PORT" "$FE_CODE"
printf '  API /api/auth/me (tanpa login)     -> HTTP %s (401 = benar)\n' "$API_CODE"
HTTPS_CODE=$(curl -s -o /dev/null -w '%{http_code}' "https://${DOMAIN}/" 2>/dev/null || echo "000")
printf '  HTTPS https://%s/ -> HTTP %s\n' "$DOMAIN" "$HTTPS_CODE"

printf '\n%s\n' "${BOLD}=== Selesai ===${NC}"
printf '  Aplikasi : https://%s\n' "$DOMAIN"
printf '  Login    : %s / (password admin yang tadi dimasukkan)\n' "$ADMIN_USERNAME"
printf '  .env     : %s\n' "$ENV_FILE"
printf '  Log BE   : docker compose logs -f be\n'
printf '  Backup   : %s (harian 02:30)\n' "$BACKUP_DIR"
printf '\nLangkah berikutnya (opsional): migrasi data dari Supabase + foto dari R2:\n'
printf '  sudo ./scripts/migrate-data.sh\n'

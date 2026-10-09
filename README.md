# Database Kopi (Caritas Kopi)

Aplikasi web pendataan **baseline desa & petani kopi**. Enumerator mengisi data
lapangan lewat formulir; admin melihat analitik agregat dan mengelola akun. Data
bisa diekspor ke PDF/DOCX.

Repositori ini adalah reimplementasi monorepo (FE/BE terpisah) dari aplikasi
Next.js lama, di-self-host pada satu VPS (tanpa Vercel/Supabase/Cloudflare R2).
Panduan kerja detail ada di [CLAUDE.md](./CLAUDE.md).

## Peran

- **ENUMERATOR** - hanya input & melihat data miliknya sendiri.
- **ADMIN** - melihat seluruh data, mengelola pengguna, dan satu-satunya yang
  boleh mengakses analitik.

## Struktur

```text
caritas-kopi/
├─ fe/                    # React 19 + Vite + TypeScript (SPA)
├─ be/                    # Spring Boot 4 + Kotlin (REST API)
├─ docker-compose.yml     # prod: postgres + be + fe (nginx)
├─ docker-compose.dev.yaml# dev: hot reload, DB/storage terpisah
├─ .env.example           # template env prod
├─ .env.dev.example       # template env dev
└─ CLAUDE.md
```

FE (nginx) menyajikan aset statis dan mem-proxy `/api/*` ke BE sehingga
same-origin (tanpa CORS, cookie auth langsung jalan). Di dev, Vite mem-proxy
`/api` ke BE.

## Prasyarat

Docker + Docker Compose, atau Node 24+ dan JDK 25 bila menjalankan langsung.

## Menjalankan (produksi)

```bash
cp .env.example .env      # isi kredensial & JWT_SECRET
docker compose up --build
```

- Aplikasi: `http://localhost:8088` (`FE_PORT`)
- BE: `http://localhost:8089` (`BE_PORT`)
- DB: `localhost:5544` (`POSTGRES_PORT`)

Volume `pgdata` (database) dan `uploads` (foto) persisten antar restart.
Backup cukup mengarsipkan kedua volume ini.

## Menjalankan (dev, hot reload)

```bash
cp .env.dev.example .env.dev
docker compose --env-file .env.dev -f docker-compose.dev.yaml up
```

- FE: `http://localhost:5178`
- BE: `http://localhost:8091`
- DB: `localhost:5545`

Data dev (volume `pgdata-dev` & `uploads-dev`) terpisah dari prod.

## Perintah

FE (`fe/`):

```bash
npm run dev        # vite dev server (proxy /api -> BE)
npm run build      # tsc -b && vite build
npm run lint       # oxlint
npm run gen:api    # generate tipe TS dari OpenAPI BE (BE harus jalan)
```

BE (`be/`):

```bash
./gradlew bootRun  # jalankan API
./gradlew build    # compile + test
./gradlew bootJar  # jar produksi
```

## Unggah foto

Tanpa layanan eksternal. Foto disimpan di filesystem `STORAGE_PATH`
(default `/app/data/uploads`), dipetakan ke volume Docker `uploads`. BE
memvalidasi MIME (jpeg/png/webp/heic/heif, maks 10 MB), me-resize/rotate
(Scrimage, WebP q90), dan menyajikan lewat `GET /api/foto/{key}` (auth, blokir
path traversal).

## Keamanan

- JWT di cookie `httpOnly` (`SameSite` + `Secure` di produksi).
- `.env`/`.env.*` **tidak pernah** di-commit; hanya `.env.example` dan
  `.env.dev.example`.
- Setiap endpoint non-auth wajib terautentikasi; endpoint analitik &
  `/api/admin/**` wajib `ADMIN`, ditegakkan di server (bukan hanya UI).

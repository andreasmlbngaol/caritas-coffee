# Database Kopi

Aplikasi web pendataan **baseline desa & petani kopi** (proyek PKL). Enumerator
mengisi data lapangan lewat formulir; admin melihat analitik agregat dan
mengelola akun. Data bisa diekspor ke PDF/DOCX.

Ini adalah reimplementasi monorepo (FE/BE terpisah) dari aplikasi
[Next.js lama](https://github.com/andreasmlbngaol/caritas-kopi), di-self-host
pada satu VPS - tanpa Vercel/Supabase/Cloudflare R2.

<div align="center">

[![React](https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-8-646CFF?style=for-the-badge&logo=vite&logoColor=white)](https://vite.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-7-3178C6?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Tailwind](https://img.shields.io/badge/Tailwind%20CSS-4-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white)](https://tailwindcss.com/)
[![TanStack Query](https://img.shields.io/badge/TanStack%20Query-5-FF4154?style=for-the-badge&logo=reactquery&logoColor=white)](https://tanstack.com/query)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker%20Compose-v2-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![Nginx](https://img.shields.io/badge/nginx-alpine-009639?style=for-the-badge&logo=nginx&logoColor=white)](https://nginx.org/)

</div>

---

## Daftar Isi

- [Tentang](#tentang)
- [Peran](#peran)
- [Struktur](#struktur)
- [Arsitektur singkat](#arsitektur-singkat)
- [Menjalankan (Docker)](#menjalankan-docker)
- [Variabel lingkungan](#variabel-lingkungan)
- [Unggah foto](#unggah-foto)
- [Ekspor PDF/DOCX](#ekspor-pdfdocx)
- [Analitik](#analitik)
- [Keamanan](#keamanan)
- [Backup](#backup)
- [Lisensi](#lisensi)

---

## Tentang

Aplikasi ini mencatat data dasar desa dan petani kopi: profil desa, kebijakan,
kelembagaan, kelompok tani, petani beserta plot, tanaman naungan, praktik GAP,
riwayat produksi, produk yang dijual, pasar, dan kondisi kebun. Semua data
lapangan diisi enumerator; admin mengawasi lewat halaman analitik dan ekspor.

## Peran

| Peran | Hak akses |
|---|---|
| **ENUMERATOR** | Hanya input & melihat data miliknya sendiri. |
| **ADMIN** | Melihat seluruh data, mengelola pengguna, dan satu-satunya yang boleh mengakses analitik. |

Aturan akses ditegakkan di UI (nav) **dan** di server (setiap endpoint cek
role).

## Struktur

```text
caritas-kopi/
├─ fe/                      # React + Vite + TypeScript (SPA)
├─ be/                      # Spring Boot + Kotlin (REST API)
├─ scripts/
│  └─ import-supabase.sh    # impor data dari Supabase ke DB lokal
├─ docker-compose.yml       # prod: postgres + be + fe (nginx)
├─ docker-compose.dev.yaml  # dev: hot reload, DB/storage terpisah
├─ .env.example             # template env prod
├─ .env.dev.example         # template env dev
└─ CLAUDE.md                # panduan kerja detail
```

## Arsitektur singkat

FE dan BE adalah dua service terpisah. Di produksi **nginx (di dalam container
FE)** menyajikan aset statis dan mem-proxy `/api/*` ke BE, jadi semuanya
**same-origin** (tanpa CORS, cookie auth langsung jalan). Di dev, Vite
mem-proxy `/api` ke BE.

- **FE** - React 19, Vite, TypeScript, Tailwind 4, TanStack Query,
  react-hook-form + zod, react-router, Recharts, Leaflet.
- **BE** - Spring Boot 4, Kotlin, Spring Data JPA, Spring Security (JWT +
  BCrypt), Flyway, PostgreSQL. Ekspor PDF (openhtmltopdf) & DOCX (Apache POI).
- **DB** - PostgreSQL 17.

## Menjalankan (Docker)

Semua dijalankan lewat Docker Compose - tidak perlu menjalankan FE/BE/DB
sendiri-sendiri.

### Produksi

```bash
cp .env.example .env      # isi kredensial & JWT_SECRET
docker compose up --build
```

- Aplikasi: `http://localhost:8088` (`FE_PORT`)
- BE: `http://localhost:8089` (`BE_PORT`)
- DB: `localhost:5544` (`POSTGRES_PORT`)

### Dev (hot reload)

```bash
cp .env.dev.example .env.dev
docker compose --env-file .env.dev -f docker-compose.dev.yaml up
```

- FE: `http://localhost:5178`
- BE: `http://localhost:8091`
- DB: `localhost:5545`

Data dev (volume `pgdata-dev` & `uploads-dev`) terpisah total dari prod.

### Perintah berguna

```bash
docker compose down          # hentikan (data tetap)
docker compose down -v       # hentikan + hapus volume (reset total)
docker compose logs -f be    # lihat log BE
```

### Impor data dari Supabase

Skema lokal dibuat Flyway; skrip hanya memindahkan data (wilayah tidak diimpor,
sudah diisi seeder saat BE start).

```bash
SUPABASE_DB_URL='postgresql://...' scripts/import-supabase.sh
```

## Variabel lingkungan

Hanya `.env.example` dan `.env.dev.example` yang di-commit. File `.env` /
`.env.dev` asli berisi rahasia dan **tidak pernah** di-commit.

| Variabel | Kegunaan |
|---|---|
| `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` | Kredensial database |
| `POSTGRES_PORT` | Port host untuk DB |
| `BE_PORT` / `FE_PORT` | Port host untuk BE / FE |
| `JWT_SECRET` | Kunci HMAC JWT (minimal 32 byte) |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | Akun admin yang di-seed saat start |
| `STORAGE_PATH` | Lokasi penyimpanan foto |
| `APP_URL` | Base URL aplikasi (dipakai untuk tautan foto di ekspor) |

## Unggah foto

Tanpa layanan eksternal. Foto disimpan di filesystem `STORAGE_PATH`
(default `/app/data/uploads`), dipetakan ke volume Docker `uploads`. BE
memvalidasi MIME (jpeg/png/webp/heic/heif, maks 10 MB), me-resize/rotate
(Scrimage, WebP q90), dan menyajikan lewat `GET /api/foto/{key}` (auth, blokir
path traversal).

## Ekspor PDF/DOCX

Dijalankan di BE (data & file foto ada di sana). Tersedia
`GET /api/petani/{id}/export/pdf` dan `/docx`, sama untuk desa. Respons berupa
byte dengan `Content-Disposition: attachment`.

## Analitik

Agregasi di sisi server (padanan `queries.ts` aplikasi lama): ringkasan, adopsi
GAP (21 praktik), produksi (produktivitas kg/ha), pasar/produk, konservasi,
wilayah/kelembagaan, agronomi plot, dan sebaran peta. Hanya ADMIN yang bisa
mengakses.

## Keamanan

- JWT di cookie `httpOnly` (`SameSite` + `Secure` di produksi).
- `.env` / `.env.*` **tidak pernah** di-commit; hanya template `.example`.
- Setiap endpoint non-auth wajib terautentikasi; endpoint analitik &
  `/api/admin/**` wajib `ADMIN`, ditegakkan di server (bukan hanya UI).
- `/api/foto/{key}` memblokir path traversal & key di luar prefix `petani/`.
- Validasi input di trust boundary (Bean Validation di BE + zod di FE).

## Backup

Cukup arsipkan volume `pgdata` (database) dan `uploads` (foto). Keduanya
persisten antar restart/rebuild.

## Lisensi

Proyek ini dilisensikan di bawah **Apache License 2.0**.

Lihat file [LICENSE](./LICENSE) untuk ketentuan lengkap.

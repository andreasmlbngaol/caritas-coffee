# CLAUDE.md

Working guide for agents/AI in the **Database Kopi** (Caritas Kopi) monorepo - a
split of the old Next.js app into **FE (React + Vite)** and **BE (Spring Boot +
Kotlin)**. Read this before touching code.

> The old project (source of truth for behavior/features) lives at
> `~/Code/next/caritas-kopi`. This repo is a reimplementation with a FE/BE
> split; keep output behaviorally identical.

---

## 1. What this project is

A web app for **baseline data collection of coffee villages & farmers** (PKL
project). Enumerators fill in field data via forms; **admins** see aggregate
analytics and manage accounts. Data can be exported to PDF/DOCX.

Two roles:
- **ENUMERATOR** - can only input & view their own data.
- **ADMIN** - sees all data, manages users, and is **the only role allowed to
  access analytics**.

Access principle: enumerators focus on entering data; all analytics are
ADMIN-only, enforced in the UI (nav) **and** on the server (every endpoint
checks role).

---

## 2. Monorepo structure

```text
caritas-kopi/
├─ fe/                      # React + Vite + TypeScript (SPA)
├─ be/                      # Spring Boot + Kotlin (REST API)
├─ docker-compose.yml       # postgres + be + fe (nginx)
├─ docker-compose.dev.yaml  # dev: hot reload FE & BE
├─ .env.example             # env template for all services
├─ .gitignore
├─ README.md
└─ CLAUDE.md
```

FE and BE are two separate services. In production **nginx (inside the FE
container)** serves the static FE assets and proxies `/api/*` to BE, so
everything is **same-origin** (no CORS, cookie auth just works). In dev, Vite
proxies `/api` to BE via `server.proxy`.

---

## 3. Stack & versions

Versions verified via web/registry on **2026-10-09**. Rule: **always the latest
stable/LTS**; on dependency conflict, take the higher version and align the
rest to the main library's major.

### FE
| Tool | Version | Notes |
|---|---|---|
| React / React DOM | 19.3.0 | |
| Vite | 8.3.4 | |
| TypeScript | 7.0.2 | native compiler (tsgo); drop to 6.x only if tooling misbehaves |
| Tailwind CSS | 4.3.3 | via `@tailwindcss/vite` (not PostCSS) |
| react-router | 8.4.0 | import from `react-router` (not `react-router-dom`) |
| @tanstack/react-query | 5.104.1 | server state |
| react-hook-form | 7.89.0 | + `@hookform/resolvers` 5.9.1 |
| zod | 4.6.5 | client validation (mirrors BE validation) |
| recharts | 3.10.1 | analytics charts |
| leaflet / react-leaflet | 1.9.4 / 5.0.0 | maps |
| lucide-react | 1.53.0 | icons |
| oxlint | 1.87.0 | linting |

### BE
| Tool | Version | Notes |
|---|---|---|
| Java | 25 (LTS) | Gradle toolchain |
| Kotlin | 2.4.21 | latest stable |
| Gradle | 9.8.1 | wrapper |
| Spring Boot | 4.1.1 | latest GA (4.2 still milestone) |
| Spring Data JPA (Hibernate) | via Boot | CRUD + aggregation |
| Spring Security | via Boot | JWT + BCrypt |
| Flyway | via Boot starter | migrations + `flyway-database-postgresql` |
| PostgreSQL JDBC | via Boot | `org.postgresql:postgresql` |
| springdoc-openapi | 3.1.1 | OpenAPI, used to generate FE TS types |
| jjwt | 0.13.0 | JWT |
| Scrimage | 4.6.8 | resize/rotate/WebP (replaces `sharp`) |
| openhtmltopdf (PDFBox) | 1.0.10 | PDF export via HTML/CSS (replaces `@react-pdf/renderer`) |
| Apache POI (XWPF) | 5.5.1 | DOCX export (replaces `docx`) |
| Testcontainers | 1.21.4 | integration tests |
| Jackson | 3 (`tools.jackson`) | `spring-boot-starter-webmvc` + `jackson-module-kotlin` |

**Deliberately not used** (ponytail / YAGNI): MapStruct (DTO mapping is written
manually as Kotlin extension functions - terser, no KSP/kapt), axios (native
`fetch`), date-fns (`Intl` + `BULAN_ID` constants like the old project), ESLint
(oxlint is enough), **AWS SDK S3 / R2** (storage moved to local disk on the VPS,
see §5.4).

### Infra
PostgreSQL 17 (image `postgres:17-alpine`), nginx (image `nginx:alpine`).

---

## 4. Commands

### FE (`fe/`)
```bash
npm run dev        # vite dev server (proxy /api -> BE)
npm run build      # tsc -b && vite build
npm run lint       # oxlint
npm run gen:api    # generate TS types from BE OpenAPI
```

### BE (`be/`)
```bash
./gradlew bootRun              # run API (default profile)
./gradlew build                # compile + test
./gradlew test                 # tests (Testcontainers for DB)
./gradlew bootJar              # production jar
```

### Root
```bash
cp .env.example .env
# Production (full build, nginx serves FE + proxies /api):
docker compose up --build
docker compose down -v         # wipe volumes

# Dev (hot reload FE & BE, no full build) - DB/storage/env SEPARATE:
cp .env.dev.example .env.dev
docker compose --env-file .env.dev -f docker-compose.dev.yaml up
#   FE: http://localhost:5178   BE: http://localhost:8091   DB: localhost:5545
```
Dev Dockerfiles: `be/Dockerfile.dev` (`bootRun --continuous`),
`fe/Dockerfile.dev` (Vite HMR). Dev compose: `docker-compose.dev.yaml`
(volumes `pgdata-dev` & `uploads-dev`, env `.env.dev`; prod uses `pgdata`,
`uploads`, `.env` - the two never mix).

Before marking a task done:
- **FE**: `npm run lint` + `npm run build` clean (0 errors).
- **BE**: `./gradlew build` succeeds.

---

## 5. Architecture

### 5.1 Authentication
- Login: `POST /api/auth/login` (username + password). BE verifies with
  `BCryptPasswordEncoder`, checks `isActive`, updates `lastLoginAt`, then sets a
  **JWT in an `httpOnly` cookie** (`Secure` + `SameSite` in production; cookie
  name `kopi_session`).
- FE sends requests with `credentials: "include"`; it never stores the token in
  JS (safe from XSS).
- `GET /api/auth/me` -> current session (`id`, `name`, `role`); `POST /api/auth/logout`.
- Every endpoint except `/api/auth/**` requires authentication. Analytics and
  `/api/admin/**` endpoints require `ADMIN`.
- `SecurityFilterChain` + `OncePerRequestFilter` JWT; claims `sub` (userId) and
  `role`. Every service touching enumerator data filters
  `createdById == currentUserId` unless ADMIN.

### 5.2 BE layers (package `id.caritas_kopi.be`)
```text
be/
├─ config/        # SecurityConfig, OpenApiConfig, WebConfig, JacksonConfig
├─ auth/          # JwtService, JwtAuthFilter, AuthController, UserDetails
├─ common/        # ApiError/@RestControllerAdvice, formatValidasi
├─ wilayah/       # Provinsi/Kabupaten/Kecamatan/Desa + cascading lookup
├─ desa/          # BaselineDesa + KebijakanDesa + KelembagaanDesa
├─ petani/        # Petani + PlotPetani + TanamanNaungan + PraktikGap +
│                 # RiwayatProduksi + ProdukDijual + PasarPetani + KondisiKebun
├─ kelompoktani/
├─ user/          # User + admin/user management
├─ analitik/      # Aggregations (counterpart of old app/(main)/analitik/queries.ts)
├─ storage/       # local storage (Docker volume) + photo upload
├─ export/        # PDF (openhtmltopdf) & DOCX (POI) + export-model
└─ seed/          # AdminSeeder, WilayahSeeder (CommandLineRunner)
```
Per-module pattern: `Entity` (JPA) -> `Repository` (Spring Data) -> `Service`
(business + authorization + aggregation) -> `Controller` (REST) -> `Dto`
(request/response). Entity<->DTO mapping is manual extension functions, not
MapStruct.

### 5.3 FE layers (`fe/src`)
```text
fe/src/
├─ app/           # router, providers (QueryClient, Theme, Auth), layout, nav-rail
├─ components/    # shared UI primitives (§7)
├─ features/
│  ├─ auth/       # login, useAuth, ProtectedRoute, RoleGate
│  ├─ dashboard/  # "/" page (split per role)
│  ├─ petani/     # list/detail/form/export-trigger
│  ├─ desa/
│  ├─ kelompok-tani/
│  ├─ admin/      # user management
│  └─ analitik/   # 7 pages + chart/map/palette/analytics-ui
├─ lib/           # api client (fetch + credentials), auth-store, format, form
├─ api/types.ts   # OpenAPI-generated TS types
└─ styles/        # globals.css (jade theme + dark)
```
- **Data fetching**: TanStack Query (query key per module; invalidate after
  mutations). Replaces server actions.
- **Forms**: react-hook-form + zodResolver; zod schemas mirror BE validation.
  Replaces `useActionState` + server actions.
- **Routing**: react-router v8 data router; guards via loader/`ProtectedRoute`.
- **Maps**: `React.lazy` + `Suspense` (client-only, keep in its own chunk).

### 5.4 Photo upload (local storage, no external service)
Everything runs on one VPS. No Cloudflare R2 / S3. Photos are stored on the
filesystem at `STORAGE_PATH` (default `/app/data/uploads`), mapped to the
**Docker volume `uploads`** so they survive restarts/rebuilds.

- `POST /api/upload` (multipart) -> auth -> MIME validation
  (jpeg/png/webp/heic/heif) + max 10 MB -> Scrimage: rotate EXIF -> resize
  <=1920px -> WebP q90 (HEIC failure -> save original) -> write to
  `STORAGE_PATH/petani/{uuid}.{ext}` -> return `{key}`.
- `GET /api/foto/{key}` - auth; rejects `..` and keys outside the `petani/`
  prefix; resolves the path under `STORAGE_PATH` (never escapes root), streams
  the file + `Cache-Control: private, max-age=3600`.
- Backup = just back up the `uploads` + `pgdata` volumes.

### 5.5 PDF/DOCX export
- Runs in **BE** (data + photo files live there; heavy libraries stay out of
  the FE bundle).
- `export-model` per domain = a presentation-agnostic structure (arrays of
  `[label, value]`, check flags, `id-ID`-formatted strings) shared by the PDF
  **and** DOCX renderers.
- `GET /api/petani/{id}/export/pdf` & `/docx`, same for `desa`. Response is
  bytes with `Content-Disposition: attachment; filename="..."`.
- PDF = **openhtmltopdf** (HTML/CSS -> PDFBox; `PetaniPdf`/`DesaPdf` produce
  HTML mimicking the old react-pdf layout: 5 pages, section B landscape, GAP
  cells merged vertically `rowspan`, page-number footer; photos link to
  `APP_URL/api/foto/{key}`).
- DOCX = POI XWPF, matched to the PDF (same Arial font metrics, 9pt body,
  14pt title, 10pt heading, symmetric 36pt margins, 23.4pt row pitch, landscape
  for the plot section, `rowSpan` for GAP groups, `✓` checks). Gotchas encoded
  in `Docx.kt`: `setFontSize` takes points (POI doubles it), `createRow()`
  clones the first row's cell count (stripped in `newRow`), POI writes no
  `w:tblGrid` (built manually from cell widths), `w:tcMar` child order must be
  top/left/bottom/right, `w:trHeight` `AT_LEAST` 468 twips sets the min row
  height.

### 5.6 Analytics
Server-side aggregation (counterpart of `analitik/queries.ts`): summary, GAP
adoption (21 practices), production (real productivity kg/ha = volume / total
plot area), market/product, conservation, region/institutions, plot agronomy,
map locations. Free-text grouping logic is preserved exactly:
**case-insensitive** + `pickSpelling` + `clean` (drop empty/`"-"`).

---

## 6. Data model

Tables & columns use **snake_case** with the same names as the old Prisma
schema. Entities: `users`; region hierarchy (`wilayah_provinsi -> kabupaten ->
kecamatan -> desa`); `baseline_desa` (+ `kebijakan_desa`, `kelembagaan_desa`);
`kelompok_tani`; `petani` (+ `plot_petani`, `tanaman_naungan`, `praktik_gap`,
`riwayat_produksi`, `produk_dijual`, `pasar_petani`, `kondisi_kebun`).

- Schema migrations via **Flyway** (`V1__schema.sql`, `V2__petani_kelompok_set_null.sql`, ...).
  Region data from the old `wilayah.sql` (~2.9 MB) is loaded via the
  `WilayahSeeder`.
- Admin seed: `AdminSeeder` (`CommandLineRunner`) from env
  `ADMIN_USERNAME`/`ADMIN_PASSWORD`.
- All enums (Role, SatuanTutupan, JenisKebijakan, JenisLembaga, JenisKelamin,
  StatusKepemilikanLahan, SistemBudidaya, JawabanGap, SatuanProduksi,
  JenisProdukDijual, KategoriPasar, JenisPraktikGap, JenisKondisiKebun) keep the
  same values; in JPA they map with `@Enumerated(EnumType.STRING)`.

Indonesian label & enum constants live in the **FE**
(`features/petani/constants.ts`, `features/desa/constants.ts`): `GAP_GROUPS`,
`KONDISI_KEBUN`, `PRODUK`, `PASAR`, `TAHUN_PRODUKSI`, `STATUS_KEPEMILIKAN`,
`KEBIJAKAN`, `LEMBAGA`, `BULAN_ID`, etc. **Always use these constants** for
labels, never hardcode.

---

## 7. Shared UI primitives (`fe/src/components/`)

Port of the old `app/(main)/_components/`:
- `ui.tsx` - `inputCls`, `pageWide`, `Section`, `SubSection`, `Grid`, `Field`,
  `FieldUnitSelect`, `Skeleton`/`ListSkeleton`/`AnalyticsSkeleton`.
- `modal.tsx` - the **only** dialog (focus trap, Esc, restore focus, ARIA
  `role="dialog"`/`aria-modal`/`aria-labelledby`). Never build a manual overlay.
- `submit-button.tsx` - submit button + modal listing empty columns
  (required/optional). The `data-*` contract (`data-label`, `data-required`,
  `data-requires`, `data-requires-value`, `data-skip-check`, `data-group`,
  `data-segmented`) is preserved as-is.
- `delete-button.tsx` - delete + confirm via `Modal`.
- `unsaved-guard.tsx` - prevents data loss when leaving a form.
- `combobox.tsx`, `date-picker.tsx` (Monday-first, `BULAN_ID`),
  `yes-no.tsx` (Segmented/YesNoField/YesNoRow), `wilayah-select.tsx`
  (4-level cascading via `/api/wilayah?parent=`), `koordinat.tsx`,
  `sort.tsx` (`SortHeader`/`parseSort`/`compareRows`),
  `navigation-progress.tsx`, `theme-toggle.tsx`.
- Loading state: skeleton per list & analytics page.

---

## 8. DESIGN RULES (owner's explicit preference - follow strictly)

1. **No excessive color.** Color must have a *purpose*. Forbidden: accent color
   strips on card edges ("very AI"), coloring icons in the nav rail, coloring
   every card.
2. **Highlight = 1-2 important cards only.** Change a card background to solid
   jade + adjust text color. Use the `highlight` prop on `StatTile`. Each
   analytics page marks **one** most-important `StatTile`.
3. **Fewer unnecessary icons.** No decorative icons on titles.
4. **Chart legend order: Tidak -> Kadang -> Ya** (red -> orange -> green).
5. **Chart tooltips must have a white background** (`tooltipStyle` in `charts.tsx`).
6. **No em dash (-) / en dash (-).** Use a short hyphen `-`.
7. **Free-text data is case-insensitive** - grouping is case-insensitive
   (helpers `ci`, `tally`, `pickSpelling`).
8. **Placeholder "-" is not counted** in statistics (helper `clean()`).
9. **Login page**: one card in the center (not full screen), 2 columns; the left
   column is just a short feature list - **do not invent copy/marketing**.
10. **Theme toggle above the logout button** (rail expanded & collapsed, mobile
    drawer) and in the top-right corner of the login card.

---

## 9. Theme & dark mode

- The **jade** color is defined in `@theme` (`fe/src/styles/globals.css`). Use
  `bg-jade-*`/`text-jade-*`; do not add a new palette.
- **Dark mode is class-based** (`@custom-variant dark (&:where(.dark, .dark *))`).
  An inline script in `index.html` adds `.dark` before paint (anti-flash).
- **Strategy: remap tokens, not `dark:` on every element.** The `.dark` block
  flips `--color-gray-*` and `--color-jade-*`; classes that use one token in
  **two roles** (`bg-white` vs `text-white`) are overridden explicitly with
  `.dark .class { ... }` **outside `@layer`**.
- Defaults follow OS preference unless the user chose manually
  (`localStorage.theme`).
- Toggle: `components/theme-toggle.tsx` (uses `useSyncExternalStore`).
- Chart/map colors come from `useIsDark()` (class on `<html>`), not Tailwind
  tokens - keep `palette.ts`/`ChartTheme` separate.
- Leaflet maps are always light: do not flip gray tokens in popups/tooltips.

---

## 10. Security

- **NEVER commit or display the contents of `.env`/`.env.*`** (DB credentials,
  `JWT_SECRET`, `ADMIN_PASSWORD`). Only `.env.example`/`.env.dev.example` are
  committed.
- Never hardcode secrets; always use env.
- Validate input at trust boundaries (DTO Bean Validation in BE + zod in FE);
  never rely on UI validation alone.
- JWT in an `httpOnly` cookie; `SameSite` + `Secure` in production.
- `/api/foto/{key}` blocks path traversal & keys outside the `petani/` prefix.

---

## 11. Deployment

`docker-compose.yml` runs 3 services: `db` (postgres:17-alpine, `pgdata`
volume, healthcheck), `be` (multi-stage Dockerfile Gradle -> JRE 25, waits for a
healthy db, Flyway automatic, `uploads` volume for photos), `fe` (multi-stage
Dockerfile Node build -> nginx, proxies `/api` to `be:8080`). All configuration
via `.env` (see `.env.example`). The whole system is self-hosted on 1 VPS, no
Vercel/Supabase/Cloudflare.

---

## 12. Gotchas

- **Rules of hooks**: never `setState` in a `useEffect` body; for external state
  (DOM class, `localStorage`) use `useSyncExternalStore`.
- **Never define a component inside a component** - move it to module scope.
- **The `data-*` contract** on form fields is load-bearing for `SubmitButton`.
- **Accessibility**: icon buttons need `aria-label`; active nav items use
  `aria-current="page"`; respect `prefers-reduced-motion`.
- **Contrast**: avoid `text-gray-400` for meaningful text (fails WCAG AA); use
  `text-gray-500`+.
- **Mobile layout**: nav rail hidden below `md`; there is a hamburger button +
  drawer; content uses `pt-16 md:pt-10`.
- **Farmer code**: 3 parts `kp1-kp2-kp3` -> `CAR-KR-001`; group code has 2 parts.
- **When adding a field**: update the Flyway schema, entity, DTO, validation, FE
  constants, and (if needed) export-model at the same time.

---

## 13. Before marking a task done

1. FE: `npm run lint` clean, `npm run build` succeeds.
2. BE: `./gradlew build` succeeds.
3. Check consistency with the design rules (§8) and dark mode (§9).
4. For data/aggregation changes, verify against real DB contents when possible.

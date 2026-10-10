import { createBrowserRouter, Navigate, Outlet } from "react-router";
import type { ComponentType } from "react";
import { AppLayout } from "./layout";
import { ProtectedRoute, RoleGate } from "@/features/auth/protected-route";
import { LoginPage } from "@/features/auth/login-page";

// Rute dimuat malas (code-split per halaman) agar bundel awal kecil - penting
// untuk jaringan lambat. `lazy` bawaan react-router membuat navigation.state
// jadi "loading", sehingga NavigationProgress tetap berfungsi.
// ponytail: satu cast di helper ini; nama modul salah ketik = error saat dev.
function lazyPage(load: () => Promise<Record<string, unknown>>, name: string) {
  return async () => ({ Component: (await load())[name] as ComponentType });
}

// Rute analitik & admin dibungkus RoleGate (ADMIN-only) - di UI saja, server
// tetap menegakkan role di setiap endpoint.
const adminOnly = { element: <RoleGate role="ADMIN"><Outlet /></RoleGate> };

export const router = createBrowserRouter([
  { path: "/login", element: <LoginPage /> },
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <AppLayout />,
        children: [
          { path: "/", lazy: lazyPage(() => import("@/features/dashboard/dashboard-page"), "DashboardPage"), handle: { title: "Dashboard" } },
          { path: "/petani", lazy: lazyPage(() => import("@/features/petani/list-page"), "PetaniListPage"), handle: { title: "Data Petani" } },
          { path: "/petani/baru", lazy: lazyPage(() => import("@/features/petani/form-pages"), "PetaniBaruPage") },
          { path: "/petani/:id", lazy: lazyPage(() => import("@/features/petani/detail-page"), "PetaniDetailPage") },
          { path: "/petani/:id/edit", lazy: lazyPage(() => import("@/features/petani/form-pages"), "PetaniEditPage") },
          { path: "/desa", lazy: lazyPage(() => import("@/features/desa/list-page"), "DesaListPage"), handle: { title: "Data Desa" } },
          { path: "/desa/baru", lazy: lazyPage(() => import("@/features/desa/form-pages"), "DesaBaruPage") },
          { path: "/desa/:id", lazy: lazyPage(() => import("@/features/desa/detail-page"), "DesaDetailPage") },
          { path: "/desa/:id/edit", lazy: lazyPage(() => import("@/features/desa/form-pages"), "DesaEditPage") },
          { path: "/kelompok-tani", lazy: lazyPage(() => import("@/features/kelompok-tani/list-page"), "KelompokTaniListPage"), handle: { title: "Kelompok Tani" } },
          { path: "/kelompok-tani/baru", lazy: lazyPage(() => import("@/features/kelompok-tani/form-pages"), "KelompokTaniBaruPage") },
          { path: "/kelompok-tani/:id/edit", lazy: lazyPage(() => import("@/features/kelompok-tani/form-pages"), "KelompokTaniEditPage") },
          {
            ...adminOnly,
            children: [
              { path: "/admin/users", lazy: lazyPage(() => import("@/features/admin/users-page"), "UsersPage"), handle: { title: "Kelola Pengguna" } },
              { path: "/analitik/gap", lazy: lazyPage(() => import("@/features/analitik/gap-page"), "GapPage"), handle: { title: "Analitik GAP" } },
              { path: "/analitik/agronomi", lazy: lazyPage(() => import("@/features/analitik/agronomi-page"), "AgronomiPage"), handle: { title: "Analitik Agronomi Plot" } },
              { path: "/analitik/produksi", lazy: lazyPage(() => import("@/features/analitik/produksi-page"), "ProduksiPage"), handle: { title: "Analitik Produksi" } },
              { path: "/analitik/pasar", lazy: lazyPage(() => import("@/features/analitik/pasar-page"), "PasarPage"), handle: { title: "Analitik Pasar & Produk" } },
              { path: "/analitik/konservasi", lazy: lazyPage(() => import("@/features/analitik/konservasi-page"), "KonservasiPage"), handle: { title: "Analitik Konservasi" } },
              { path: "/analitik/wilayah", lazy: lazyPage(() => import("@/features/analitik/wilayah-page"), "WilayahPage"), handle: { title: "Analitik Wilayah & Kelembagaan" } },
              { path: "/analitik/peta", lazy: lazyPage(() => import("@/features/analitik/peta-page"), "PetaPage"), handle: { title: "Peta Sebaran" } },
            ],
          },
        ],
      },
    ],
  },
  { path: "*", element: <Navigate to="/" replace /> },
]);

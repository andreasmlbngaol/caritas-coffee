import { createBrowserRouter, Navigate } from "react-router";
import { AppLayout } from "./layout";
import { ProtectedRoute, RoleGate } from "@/features/auth/protected-route";
import { LoginPage } from "@/features/auth/login-page";
import { DashboardPage } from "@/features/dashboard/dashboard-page";
import { PetaniListPage } from "@/features/petani/list-page";
import { PetaniDetailPage } from "@/features/petani/detail-page";
import { PetaniBaruPage, PetaniEditPage } from "@/features/petani/form-pages";
import { DesaListPage } from "@/features/desa/list-page";
import { DesaDetailPage } from "@/features/desa/detail-page";
import { DesaBaruPage, DesaEditPage } from "@/features/desa/form-pages";
import { KelompokTaniListPage } from "@/features/kelompok-tani/list-page";
import { KelompokTaniBaruPage, KelompokTaniEditPage } from "@/features/kelompok-tani/form-pages";
import { UsersPage } from "@/features/admin/users-page";
import { GapPage } from "@/features/analitik/gap-page";
import { AgronomiPage } from "@/features/analitik/agronomi-page";
import { ProduksiPage } from "@/features/analitik/produksi-page";
import { PasarPage } from "@/features/analitik/pasar-page";
import { KonservasiPage } from "@/features/analitik/konservasi-page";
import { WilayahPage } from "@/features/analitik/wilayah-page";
import { PetaPage } from "@/features/analitik/peta-page";

// Rute analitik & admin dibungkus RoleGate (ADMIN-only) - di UI saja, server
// tetap menegakkan role di setiap endpoint.
const adminPage = (node: React.ReactNode) => <RoleGate role="ADMIN">{node}</RoleGate>;

export const router = createBrowserRouter([
  { path: "/login", element: <LoginPage /> },
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <AppLayout />,
        children: [
          { path: "/", element: <DashboardPage />, handle: { title: "Dashboard" } },
          { path: "/petani", element: <PetaniListPage />, handle: { title: "Data Petani" } },
          { path: "/petani/baru", element: <PetaniBaruPage /> },
          { path: "/petani/:id", element: <PetaniDetailPage /> },
          { path: "/petani/:id/edit", element: <PetaniEditPage /> },
          { path: "/desa", element: <DesaListPage />, handle: { title: "Data Desa" } },
          { path: "/desa/baru", element: <DesaBaruPage /> },
          { path: "/desa/:id", element: <DesaDetailPage /> },
          { path: "/desa/:id/edit", element: <DesaEditPage /> },
          { path: "/kelompok-tani", element: <KelompokTaniListPage />, handle: { title: "Kelompok Tani" } },
          { path: "/kelompok-tani/baru", element: <KelompokTaniBaruPage /> },
          { path: "/kelompok-tani/:id/edit", element: <KelompokTaniEditPage /> },
          { path: "/admin/users", element: adminPage(<UsersPage />), handle: { title: "Kelola Pengguna" } },
          { path: "/analitik/gap", element: adminPage(<GapPage />), handle: { title: "Analitik GAP" } },
          { path: "/analitik/agronomi", element: adminPage(<AgronomiPage />), handle: { title: "Analitik Agronomi Plot" } },
          { path: "/analitik/produksi", element: adminPage(<ProduksiPage />), handle: { title: "Analitik Produksi" } },
          { path: "/analitik/pasar", element: adminPage(<PasarPage />), handle: { title: "Analitik Pasar & Produk" } },
          { path: "/analitik/konservasi", element: adminPage(<KonservasiPage />), handle: { title: "Analitik Konservasi" } },
          { path: "/analitik/wilayah", element: adminPage(<WilayahPage />), handle: { title: "Analitik Wilayah & Kelembagaan" } },
          { path: "/analitik/peta", element: adminPage(<PetaPage />), handle: { title: "Peta Sebaran" } },
        ],
      },
    ],
  },
  { path: "*", element: <Navigate to="/" replace /> },
]);

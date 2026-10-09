import type { ReactNode } from "react";
import { Navigate, Outlet, useLocation } from "react-router";
import { useAuth } from "./use-auth";
import { ListSkeleton } from "@/components/ui";

/** Bungkus rute privat: belum login → /login. */
export function ProtectedRoute() {
  const { status } = useAuth();
  const location = useLocation();

  if (status === "loading") return <ListSkeleton />;
  if (status === "anonymous") {
    const from = location.pathname + location.search;
    return <Navigate to={`/login?callbackUrl=${encodeURIComponent(from)}`} replace />;
  }
  return <Outlet />;
}

/** Batasi rute ke role tertentu; role lain dialihkan ke dashboard. */
export function RoleGate({ role, children }: { role: "ADMIN"; children: ReactNode }) {
  const { user } = useAuth();
  if (user && user.role !== role) return <Navigate to="/" replace />;
  return <>{children}</>;
}

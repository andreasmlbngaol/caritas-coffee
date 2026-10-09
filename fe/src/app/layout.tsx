import { useEffect } from "react";
import { Outlet, useMatches } from "react-router";
import { useAuth } from "@/features/auth/use-auth";
import { NavRail } from "./nav-rail";
import { NavigationProgress, NumberWheelGuard } from "@/components/navigation-progress";

/** Kerangka halaman privat: rail nav + konten + indikator navigasi. */
export function AppLayout() {
  const { user } = useAuth();
  const matches = useMatches();

  const title = matches
    .map((m) => (m.handle as { title?: string } | undefined)?.title)
    .filter(Boolean)
    .at(-1);

  // Judul dokumen mengikuti `handle.title` rute aktif (padanan metadata Next.js).
  useEffect(() => {
    document.title = title ? `${title} - Database Kopi` : "Database Kopi";
  }, [title]);

  if (!user) return null;

  return (
    <div className="flex min-h-screen">
      <NavRail user={user} />
      <div className="min-w-0 flex-1">
        <Outlet />
      </div>
      <NumberWheelGuard />
      <NavigationProgress />
    </div>
  );
}

// Padanan app/(main)/analitik/_components/map.tsx.
// Leaflet menyentuh window → pisah chunk client-only lewat React.lazy.
import { lazy, Suspense } from "react";
import type { LokasiDesa, LokasiPlot } from "@/api/types";

const MapInner = lazy(() => import("./map-inner").then((m) => ({ default: m.MapInner })));

export function MapView({ desa, plot }: { desa: LokasiDesa[]; plot: LokasiPlot[] }) {
  return (
    <Suspense
      fallback={
        <div className="flex h-[520px] items-center justify-center rounded-2xl bg-gray-100 text-sm text-gray-500">
          Memuat peta…
        </div>
      }
    >
      <MapInner desa={desa} plot={plot} />
    </Suspense>
  );
}

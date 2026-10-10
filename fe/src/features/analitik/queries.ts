// TanStack Query hooks untuk endpoint analitik (semua ADMIN-only di BE).
// Satu hook per endpoint; agregasi dihitung server-side.
import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type {
  AgronomiDto,
  GapAdoptionDto,
  KonservasiDto,
  LokasiDto,
  PasarProdukDto,
  ProduksiAnalitikDto,
  RingkasanDto,
  WilayahAnalitikDto,
} from "@/api/types";

export const analitikKeys = {
  ringkasan: ["analitik", "ringkasan"] as const,
  gap: ["analitik", "gap"] as const,
  produksi: ["analitik", "produksi"] as const,
  pasarProduk: ["analitik", "pasar-produk"] as const,
  konservasi: ["analitik", "konservasi"] as const,
  wilayah: ["analitik", "wilayah"] as const,
  agronomi: ["analitik", "agronomi"] as const,
  lokasi: ["analitik", "lokasi"] as const,
};

export function useRingkasan() {
  return useQuery({
    queryKey: analitikKeys.ringkasan,
    queryFn: () => api.get<RingkasanDto>("/api/analitik/ringkasan"),
  });
}

export function useGapAdoption() {
  return useQuery({
    queryKey: analitikKeys.gap,
    queryFn: () => api.get<GapAdoptionDto>("/api/analitik/gap"),
  });
}

export function useProduksi() {
  return useQuery({
    queryKey: analitikKeys.produksi,
    queryFn: () => api.get<ProduksiAnalitikDto>("/api/analitik/produksi"),
  });
}

export function usePasarProduk() {
  return useQuery({
    queryKey: analitikKeys.pasarProduk,
    queryFn: () => api.get<PasarProdukDto>("/api/analitik/pasar-produk"),
  });
}

export function useKonservasi() {
  return useQuery({
    queryKey: analitikKeys.konservasi,
    queryFn: () => api.get<KonservasiDto>("/api/analitik/konservasi"),
  });
}

export function useWilayah() {
  return useQuery({
    queryKey: analitikKeys.wilayah,
    queryFn: () => api.get<WilayahAnalitikDto>("/api/analitik/wilayah"),
  });
}

export function useAgronomi() {
  return useQuery({
    queryKey: analitikKeys.agronomi,
    queryFn: () => api.get<AgronomiDto>("/api/analitik/agronomi"),
  });
}

export function useLokasi() {
  return useQuery({
    queryKey: analitikKeys.lokasi,
    queryFn: () => api.get<LokasiDto>("/api/analitik/lokasi"),
  });
}

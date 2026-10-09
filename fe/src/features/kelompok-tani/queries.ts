import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { KelompokTaniDto, KelompokTaniRingkas, KelompokTaniRequest } from "@/api/types";

export const kelompokKeys = {
  all: ["kelompok-tani"] as const,
  byDesa: (desa: string) => ["kelompok-tani", "desa", desa] as const,
  detail: (id: string) => ["kelompok-tani", id] as const,
};

export function useKelompokList() {
  return useQuery({
    queryKey: kelompokKeys.all,
    queryFn: () => api.get<KelompokTaniDto[]>("/api/kelompok-tani"),
  });
}

export function useKelompokByDesa(desaKode: string | undefined) {
  return useQuery({
    queryKey: kelompokKeys.byDesa(desaKode ?? ""),
    queryFn: () => api.get<KelompokTaniRingkas[]>(`/api/kelompok-tani?desa=${encodeURIComponent(desaKode!)}`),
    enabled: !!desaKode,
  });
}

export function useCreateKelompok() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (req: KelompokTaniRequest) => api.post<{ id: string }>("/api/kelompok-tani", req),
    onSuccess: () => qc.invalidateQueries({ queryKey: kelompokKeys.all }),
  });
}

export function useUpdateKelompok(id: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (req: KelompokTaniRequest) => api.put<void>(`/api/kelompok-tani/${id}`, req),
    onSuccess: () => qc.invalidateQueries({ queryKey: kelompokKeys.all }),
  });
}

export function useDeleteKelompok() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.del<void>(`/api/kelompok-tani/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: kelompokKeys.all }),
  });
}

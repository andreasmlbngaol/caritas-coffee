import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { DesaDetailDto, DesaListDto } from "@/api/types";
import type { BaselineDesaRequest } from "./types";

export const desaKeys = {
  all: ["desa"] as const,
  detail: (id: string) => ["desa", id] as const,
};

export function useDesaList() {
  return useQuery({
    queryKey: desaKeys.all,
    queryFn: () => api.get<DesaListDto[]>("/api/desa"),
  });
}

export function useDesaDetail(id: string) {
  return useQuery({
    queryKey: desaKeys.detail(id),
    queryFn: () => api.get<DesaDetailDto>(`/api/desa/${id}`),
  });
}

export function useCreateDesa() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (req: BaselineDesaRequest) => api.post<{ id: string }>("/api/desa", req),
    onSuccess: () => qc.invalidateQueries({ queryKey: desaKeys.all }),
  });
}

export function useUpdateDesa(id: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (req: BaselineDesaRequest) => api.put<void>(`/api/desa/${id}`, req),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: desaKeys.all });
      qc.invalidateQueries({ queryKey: desaKeys.detail(id) });
    },
  });
}

export function useDeleteDesa() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.del<void>(`/api/desa/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: desaKeys.all }),
  });
}

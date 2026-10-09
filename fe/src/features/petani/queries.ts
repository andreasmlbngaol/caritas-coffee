import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { PetaniDetailDto, PetaniListDto } from "@/api/types";
import type { PetaniRequest } from "./types";

export const petaniKeys = {
  all: ["petani"] as const,
  detail: (id: string) => ["petani", id] as const,
};

export function usePetaniList() {
  return useQuery({
    queryKey: petaniKeys.all,
    queryFn: () => api.get<PetaniListDto[]>("/api/petani"),
  });
}

export function usePetaniDetail(id: string) {
  return useQuery({
    queryKey: petaniKeys.detail(id),
    queryFn: () => api.get<PetaniDetailDto>(`/api/petani/${id}`),
  });
}

export function useCreatePetani() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (req: PetaniRequest) =>
      api.post<{ id: string }>("/api/petani", req),
    onSuccess: () => qc.invalidateQueries({ queryKey: petaniKeys.all }),
  });
}

export function useUpdatePetani(id: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (req: PetaniRequest) => api.put<void>(`/api/petani/${id}`, req),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: petaniKeys.all });
      qc.invalidateQueries({ queryKey: petaniKeys.detail(id) });
    },
  });
}

export function useDeletePetani() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.del<void>(`/api/petani/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: petaniKeys.all }),
  });
}

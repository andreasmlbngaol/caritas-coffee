import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { CredentialsResponse, UserDto } from "@/api/types";

export const userKeys = {
  all: ["admin", "users"] as const,
};

export function useUserList() {
  return useQuery({
    queryKey: userKeys.all,
    queryFn: () => api.get<UserDto[]>("/api/admin/users"),
  });
}

export function useCreateUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (req: { username: string; fullName: string }) =>
      api.post<CredentialsResponse>("/api/admin/users", req),
    onSuccess: () => qc.invalidateQueries({ queryKey: userKeys.all }),
  });
}

export function useResetPassword() {
  return useMutation({
    mutationFn: (id: string) => api.post<CredentialsResponse>(`/api/admin/users/${id}/reset-password`),
  });
}

export function useToggleActive() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.patch<{ isActive: boolean }>(`/api/admin/users/${id}/toggle-active`),
    onSuccess: () => qc.invalidateQueries({ queryKey: userKeys.all }),
  });
}

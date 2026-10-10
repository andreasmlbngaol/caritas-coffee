import { ListSkeleton } from "@/components/ui";
import { ErrorState } from "@/components/error-boundary";
import { useAuth } from "@/features/auth/use-auth";
import { NewUserForm } from "./new-user-form";
import { ResetPasswordButton } from "./reset-password-button";
import { useToggleActive, useUserList } from "./queries";
import type { UserDto } from "@/api/types";

function ToggleActiveButton({ user, isSelf }: { user: UserDto; isSelf: boolean }) {
  const toggle = useToggleActive();
  if (isSelf) return null;
  return (
    <button
      type="button"
      onClick={() => toggle.mutate(user.id)}
      disabled={toggle.isPending}
      className="rounded-lg px-2.5 py-1.5 text-xs font-medium text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-900 disabled:opacity-50"
    >
      {user.isActive ? "Nonaktifkan" : "Aktifkan"}
    </button>
  );
}

export function UsersPage() {
  const { user: me } = useAuth();
  const { data, isPending, isError, refetch } = useUserList();

  const users = [...(data ?? [])].sort((a, b) => {
    if (a.role !== b.role) return a.role === "ADMIN" ? -1 : 1;
    return a.createdAt.localeCompare(b.createdAt);
  });

  if (isPending) return <ListSkeleton />;
  if (isError) return <ErrorState message="Gagal memuat daftar pengguna." onRetry={() => void refetch()} />;

  return (
    <main className="mx-auto w-full max-w-7xl px-4 py-10 sm:px-8">
      <header>
        <h1 className="text-lg font-semibold tracking-tight">Manajemen Pengguna</h1>
        <p className="mt-1 text-sm text-gray-500">Tambah dan kelola akun enumerator.</p>
      </header>

      <section className="mt-8 rounded-2xl bg-white p-6 shadow-sm ring-1 ring-gray-950/5">
        <h2 className="mb-4 text-sm font-semibold">Tambah Enumerator</h2>
        <NewUserForm />
      </section>

      <section className="mt-6 overflow-hidden rounded-2xl bg-white shadow-sm ring-1 ring-gray-950/5">
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left text-[11px] font-semibold uppercase tracking-wider text-gray-500">
              <th className="px-5 py-3.5">Pengguna</th>
              <th className="px-5 py-3.5">Role</th>
              <th className="px-5 py-3.5">Status</th>
              <th className="px-5 py-3.5">Login Terakhir</th>
              <th className="px-5 py-3.5 text-right">Aksi</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {users.map((u) => (
              <tr key={u.id} className="transition-colors hover:bg-gray-50/50">
                <td className="px-5 py-3.5">
                  <div className="flex items-center gap-3">
                    <div className="flex h-8 w-8 items-center justify-center rounded-full bg-jade-100 text-[11px] font-semibold text-jade-800">
                      {(u.fullName ?? u.username).slice(0, 2).toUpperCase()}
                    </div>
                    <div>
                      <p className="font-medium leading-tight">{u.fullName ?? "-"}</p>
                      <p className="text-xs text-gray-500">@{u.username}</p>
                    </div>
                  </div>
                </td>
                <td className="px-5 py-3.5">
                  <span
                    className={`rounded-full px-2.5 py-1 text-xs font-medium ${
                      u.role === "ADMIN" ? "bg-gray-900 text-white" : "bg-gray-100 text-gray-600"
                    }`}
                  >
                    {u.role === "ADMIN" ? "Admin" : "Enumerator"}
                  </span>
                </td>
                <td className="px-5 py-3.5">
                  <span className="flex items-center gap-1.5 text-sm">
                    <span className={`h-1.5 w-1.5 rounded-full ${u.isActive ? "bg-jade-600" : "bg-gray-300"}`} />
                    {u.isActive ? "Aktif" : "Nonaktif"}
                  </span>
                </td>
                <td className="px-5 py-3.5 text-gray-500">
                  {u.lastLoginAt ? new Date(u.lastLoginAt).toLocaleString("id-ID") : "Belum pernah"}
                </td>
                <td className="px-5 py-3.5">
                  <div className="flex items-center justify-end gap-1">
                    <ResetPasswordButton userId={u.id} />
                    <ToggleActiveButton user={u} isSelf={u.id === me?.id} />
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </main>
  );
}

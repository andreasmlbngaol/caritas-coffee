import { useRef, useState } from "react";
import { CredentialsDialog } from "./credentials-dialog";
import { useCreateUser } from "./queries";
import type { CredentialsResponse } from "@/api/types";

const inputCls =
  "rounded-xl bg-gray-50 px-3 py-2.5 text-sm ring-1 ring-inset ring-gray-200 outline-none transition focus:bg-white focus:ring-2 focus:ring-inset focus:ring-jade-700";

export function NewUserForm() {
  const [error, setError] = useState<string | null>(null);
  const [credentials, setCredentials] = useState<CredentialsResponse | null>(null);
  const formRef = useRef<HTMLFormElement>(null);
  const create = useCreateUser();

  async function onSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    const fd = new FormData(e.currentTarget);
    try {
      const result = await create.mutateAsync({
        username: String(fd.get("username") ?? ""),
        fullName: String(fd.get("fullName") ?? ""),
      });
      setCredentials(result);
      formRef.current?.reset();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Gagal menambah pengguna");
    }
  }

  return (
    <>
      <form ref={formRef} onSubmit={onSubmit} className="flex flex-col gap-3">
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <input name="username" placeholder="Username" required className={inputCls} />
          <input name="fullName" placeholder="Nama lengkap" required className={inputCls} />
        </div>

        {error && <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{error}</p>}

        <div className="flex items-center gap-3">
          <button
            type="submit"
            disabled={create.isPending}
            className="w-fit rounded-xl bg-jade-800 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-jade-900 disabled:opacity-50"
          >
            {create.isPending ? "Menyimpan..." : "Tambah"}
          </button>
          <p className="text-xs text-gray-500">Password dibuat otomatis setelah disimpan.</p>
        </div>
      </form>

      {credentials && <CredentialsDialog credentials={credentials} onClose={() => setCredentials(null)} />}
    </>
  );
}

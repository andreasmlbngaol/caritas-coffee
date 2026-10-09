import { useState } from "react";
import { KeyRound } from "lucide-react";
import { CredentialsDialog } from "./credentials-dialog";
import { useResetPassword } from "./queries";
import type { CredentialsResponse } from "@/api/types";

export function ResetPasswordButton({ userId }: { userId: string }) {
  const [error, setError] = useState<string | null>(null);
  const [credentials, setCredentials] = useState<CredentialsResponse | null>(null);
  const reset = useResetPassword();

  async function handleReset() {
    setError(null);
    try {
      setCredentials(await reset.mutateAsync(userId));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Gagal reset password");
    }
  }

  return (
    <>
      <button
        type="button"
        onClick={handleReset}
        disabled={reset.isPending}
        title="Reset password"
        className="rounded-lg px-2.5 py-1.5 text-xs font-medium text-gray-500 transition-colors hover:bg-gray-100 hover:text-jade-800 disabled:opacity-50"
      >
        <span className="flex items-center gap-1.5">
          <KeyRound size={13} />
          {reset.isPending ? "..." : "Reset"}
        </span>
      </button>

      {credentials && <CredentialsDialog credentials={credentials} onClose={() => setCredentials(null)} />}
      {error && <span className="ml-2 text-xs text-red-600">{error}</span>}
    </>
  );
}

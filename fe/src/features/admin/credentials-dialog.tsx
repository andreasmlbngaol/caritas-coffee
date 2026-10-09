import { useState } from "react";
import { Check, Copy } from "lucide-react";
import { Modal } from "@/components/modal";
import type { CredentialsResponse } from "@/api/types";

export function CredentialsDialog({
  credentials,
  onClose,
}: {
  credentials: CredentialsResponse;
  onClose: () => void;
}) {
  const [copied, setCopied] = useState(false);
  const text = `username: ${credentials.username}\npassword: ${credentials.password}`;

  async function handleCopy() {
    try {
      await navigator.clipboard.writeText(text);
    } catch {
      const ta = document.createElement("textarea");
      ta.value = text;
      document.body.appendChild(ta);
      ta.select();
      document.execCommand("copy");
      document.body.removeChild(ta);
    }
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }

  return (
    <Modal open onClose={onClose} title="Kredensial Akun" panelClassName="max-w-sm">
      <p className="mt-1 text-xs text-gray-500">
        Simpan dan bagikan ke enumerator - password ini hanya ditampilkan sekali.
      </p>

      <pre className="mt-4 whitespace-pre-wrap rounded-xl bg-gray-50 p-4 font-mono text-sm text-gray-800 ring-1 ring-inset ring-gray-200">
        {text}
      </pre>

      <div className="mt-4 flex justify-end gap-2">
        <button
          type="button"
          onClick={onClose}
          className="rounded-xl px-4 py-2 text-sm font-medium text-gray-600 transition-colors hover:bg-gray-100"
        >
          Tutup
        </button>
        <button
          type="button"
          onClick={handleCopy}
          className="flex items-center gap-2 rounded-xl bg-jade-800 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-jade-900"
        >
          {copied ? <Check size={16} /> : <Copy size={16} />}
          {copied ? "Tersalin" : "Salin"}
        </button>
      </div>
    </Modal>
  );
}

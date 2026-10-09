import { useState } from "react";
import { useNavigate, useSearchParams } from "react-router";
import { Sprout, MapPin, TrendingUp, ShieldCheck } from "lucide-react";
import { api, ApiError } from "@/lib/api";
import { authStore } from "@/lib/auth-store";
import type { SessionUser } from "@/api/types";
import { ThemeToggle } from "@/components/theme-toggle";

const FEATURES = [
  { icon: Sprout, title: "Data petani & GAP" },
  { icon: MapPin, title: "Peta sebaran desa & plot" },
  { icon: TrendingUp, title: "Analitik produksi" },
  { icon: ShieldCheck, title: "Konservasi & wilayah" },
];

export function LoginPage() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const callbackUrl = params.get("callbackUrl") ?? "/";

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [pending, setPending] = useState(false);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setPending(true);
    setError(null);
    try {
      const user = await api.post<SessionUser>("/api/auth/login", { username, password });
      authStore.setUser(user);
      // Guard: hanya path internal, cegah open redirect ke situs luar
      const dest = callbackUrl.startsWith("/") ? callbackUrl : "/";
      navigate(dest, { replace: true });
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Username atau password salah");
    } finally {
      setPending(false);
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-gray-50 px-4 py-10">
      <div className="relative grid w-full max-w-4xl overflow-hidden rounded-3xl bg-white shadow-sm ring-1 ring-gray-950/5 lg:grid-cols-2">
        <div className="absolute right-3 top-3 z-10">
          <ThemeToggle collapsed />
        </div>

        <aside className="hidden bg-jade-900 p-10 text-white lg:flex lg:flex-col">
          <div className="flex items-center gap-3">
            <img
              src="/caritas_icon.webp"
              alt="Logo Caritas"
              width={44}
              height={44}
              className="h-11 w-11 rounded-xl object-contain"
            />
            <span className="text-base font-semibold tracking-tight">Database Kopi</span>
          </div>

          <p className="mt-8 text-sm text-jade-100/80">Fitur utama:</p>
          <ul className="mt-3 space-y-3">
            {FEATURES.map((f) => (
              <li key={f.title} className="flex items-center gap-3">
                <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-white/10">
                  <f.icon size={16} className="text-jade-200" />
                </span>
                <span className="text-sm font-medium">{f.title}</span>
              </li>
            ))}
          </ul>
        </aside>

        <div className="p-8 sm:p-10">
          <div className="mb-6 flex items-center gap-3 lg:hidden">
            <img
              src="/caritas_icon.webp"
              alt="Logo Caritas"
              width={40}
              height={40}
              className="h-10 w-10 rounded-xl object-contain"
            />
            <span className="text-base font-semibold tracking-tight">Database Kopi</span>
          </div>

          <h1 className="text-xl font-semibold tracking-tight">Masuk</h1>
          <p className="mt-1 text-sm text-gray-500">Gunakan akun yang diberikan admin.</p>

          <form onSubmit={onSubmit} className="mt-6 flex flex-col gap-4">
            <div>
              <label htmlFor="username" className="mb-1.5 block text-sm font-medium text-gray-700">
                Username
              </label>
              <input
                id="username"
                name="username"
                type="text"
                required
                autoComplete="username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="w-full rounded-xl bg-gray-50 px-3 py-2.5 text-sm ring-1 ring-inset ring-gray-200 outline-none transition focus:bg-white focus:ring-2 focus:ring-inset focus:ring-jade-700"
              />
            </div>
            <div>
              <label htmlFor="password" className="mb-1.5 block text-sm font-medium text-gray-700">
                Kata Sandi
              </label>
              <input
                id="password"
                name="password"
                type="password"
                required
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full rounded-xl bg-gray-50 px-3 py-2.5 text-sm ring-1 ring-inset ring-gray-200 outline-none transition focus:bg-white focus:ring-2 focus:ring-inset focus:ring-jade-700"
              />
            </div>
            {error && (
              <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">{error}</p>
            )}
            <button
              type="submit"
              disabled={pending}
              className="mt-1 rounded-xl bg-jade-800 py-2.5 text-sm font-medium text-white transition-colors hover:bg-jade-900 disabled:opacity-50"
            >
              {pending ? "Memproses..." : "Masuk"}
            </button>
          </form>
        </div>
      </div>
    </main>
  );
}

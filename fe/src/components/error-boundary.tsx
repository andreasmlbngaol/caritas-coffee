import { Component, type ErrorInfo, type ReactNode } from "react";
import { AlertTriangle } from "lucide-react";
import { pageWide } from "./ui";

/** Tampilan error halaman penuh / blok. Dipakai ErrorBoundary & state error query. */
export function ErrorState({
  title = "Terjadi kesalahan",
  message = "Coba muat ulang halaman atau ulangi beberapa saat lagi.",
  onRetry,
}: {
  title?: string;
  message?: string;
  onRetry?: () => void;
}) {
  return (
    <div className={pageWide}>
      <div className="mx-auto mt-16 max-w-md rounded-2xl bg-white p-8 text-center shadow-sm ring-1 ring-gray-950/5">
        <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-red-50 text-red-600">
          <AlertTriangle size={22} />
        </div>
        <h1 className="mt-4 text-sm font-semibold tracking-tight">{title}</h1>
        <p className="mt-2 text-sm text-gray-500">{message}</p>
        {onRetry && (
          <button
            type="button"
            onClick={onRetry}
            className="mt-5 rounded-xl bg-jade-800 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-jade-900"
          >
            Coba lagi
          </button>
        )}
      </div>
    </div>
  );
}

/** Batas error tingkat app: cegah satu throw me-render app jadi blank putih. */
export class ErrorBoundary extends Component<
  { children: ReactNode },
  { error: Error | null }
> {
  state: { error: Error | null } = { error: null };

  static getDerivedStateFromError(error: Error) {
    return { error };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error("Uncaught render error:", error, info.componentStack);
  }

  render() {
    if (this.state.error) {
      return (
        <ErrorState
          title="Aplikasi mengalami kesalahan"
          message="Muat ulang halaman untuk melanjutkan."
          onRetry={() => window.location.reload()}
        />
      );
    }
    return this.props.children;
  }
}

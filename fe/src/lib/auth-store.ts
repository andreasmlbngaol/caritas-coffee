// Store sesi global (di luar React) memakai useSyncExternalStore.
// Diisi sekali saat boot dari GET /api/auth/me; diperbarui setelah login/logout.
import { api, ApiError } from "@/lib/api";
import type { SessionUser } from "@/api/types";

export type AuthState = {
  user: SessionUser | null;
  status: "loading" | "authenticated" | "anonymous";
};

let state: AuthState = { user: null, status: "loading" };
const listeners = new Set<() => void>();

function emit(next: AuthState) {
  state = next;
  for (const l of listeners) l();
}

export const authStore = {
  subscribe(listener: () => void) {
    listeners.add(listener);
    return () => {
      listeners.delete(listener);
    };
  },
  getSnapshot(): AuthState {
    return state;
  },
  async refresh(): Promise<SessionUser | null> {
    try {
      const user = await api.get<SessionUser>("/api/auth/me");
      emit({ user, status: "authenticated" });
      return user;
    } catch (e) {
      if (e instanceof ApiError && e.status === 401) {
        emit({ user: null, status: "anonymous" });
        return null;
      }
      emit({ user: null, status: "anonymous" });
      return null;
    }
  },
  setUser(user: SessionUser) {
    emit({ user, status: "authenticated" });
  },
  clear() {
    emit({ user: null, status: "anonymous" });
  },
};

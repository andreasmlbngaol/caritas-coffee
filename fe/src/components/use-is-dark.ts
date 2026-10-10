import { useSyncExternalStore } from "react";

// Pantau kelas `dark` pada <html> (diubah skrip pra-paint & tombol tema).
function subscribe(callback: () => void) {
  const observer = new MutationObserver(callback);
  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ["class"],
  });
  return () => observer.disconnect();
}

function getSnapshot() {
  return document.documentElement.classList.contains("dark");
}

// Hook tema gelap: pantau kelas `dark` pada <html>. Dipakai juga oleh chart/map
// agar warna SVG ikut tema.
export function useIsDark() {
  return useSyncExternalStore(subscribe, getSnapshot, () => false);
}

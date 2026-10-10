import { useEffect, useRef, useState } from "react";
import { useLocation, useNavigation } from "react-router";
import { Coffee } from "lucide-react";

// Bar progres navigasi. Memakai state `navigation` dari react-router (idle /
// loading) plus penanda saat navigasi mulai agar terasa langsung.
export function NavigationProgress() {
  const navigation = useNavigation();
  const location = useLocation();
  const [progress, setProgress] = useState(0);
  const [visible, setVisible] = useState(false);
  const timersRef = useRef<ReturnType<typeof setTimeout>[]>([]);
  const visibleRef = useRef(false);
  const navigating = navigation.state !== "idle";

  function clearTimers() {
    timersRef.current.forEach(clearTimeout);
    timersRef.current = [];
  }

  function show(next: boolean) {
    visibleRef.current = next;
    setVisible(next);
  }

  useEffect(() => {
    if (navigating) {
      clearTimers();
      // Animasi bar dikendalikan timer (sistem eksternal), bukan turunan
      // langsung dari render - setState sinkron di sini memang disengaja.
      // oxlint-disable-next-line react/set-state-in-effect
      show(true);
      // oxlint-disable-next-line react/set-state-in-effect
      setProgress(0);
      timersRef.current.push(setTimeout(() => setProgress(35), 60));
      timersRef.current.push(setTimeout(() => setProgress(55), 500));
      timersRef.current.push(setTimeout(() => setProgress(70), 1400));
      timersRef.current.push(setTimeout(() => setProgress(82), 2800));
      timersRef.current.push(setTimeout(() => setProgress(90), 5000));
    } else if (visibleRef.current) {
      clearTimers();
      // oxlint-disable-next-line react/set-state-in-effect
      setProgress(100);
      timersRef.current.push(
        setTimeout(() => {
          show(false);
          setProgress(0);
        }, 400),
      );
    }
    return clearTimers;
  }, [navigating, location.key]);

  return (
    <div
      aria-hidden
      className={`pointer-events-none fixed inset-x-0 top-0 z-[100] transition-opacity duration-300 ${
        visible ? "opacity-100" : "opacity-0"
      }`}
    >
      <div className="h-[3px]">
        <div
          className="h-full rounded-r-full bg-gradient-to-r from-jade-800 via-jade-500 to-jade-300 shadow-[0_0_12px_rgba(21,128,61,0.5)] transition-[width] duration-500 ease-out"
          style={{ width: `${progress}%` }}
        />
      </div>
      <div
        className="absolute -top-1.5 -translate-x-1/2 transition-[left] duration-500 ease-out"
        style={{ left: `${progress}%` }}
      >
        <div className="flex h-6 w-6 animate-pulse items-center justify-center rounded-full bg-white shadow-md ring-1 ring-jade-200">
          <Coffee size={13} className="text-jade-700" />
        </div>
      </div>
    </div>
  );
}

// Blur input number saat scroll agar nilai tidak tak sengaja berubah.
export function NumberWheelGuard() {
  useEffect(() => {
    function onWheel() {
      const el = document.activeElement;
      if (el instanceof HTMLInputElement && el.type === "number") {
        el.blur();
      }
    }
    document.addEventListener("wheel", onWheel, { passive: true });
    return () => document.removeEventListener("wheel", onWheel);
  }, []);
  return null;
}

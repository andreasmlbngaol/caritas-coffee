import { useState } from "react";
import { ChevronDown } from "lucide-react";
import { inputCls } from "@/components/ui";
import { BULAN_ID } from "@/lib/format";

function MonthSelect({
  value,
  onChange,
  placeholder,
}: {
  value: string;
  onChange: (v: string) => void;
  placeholder: string;
}) {
  return (
    <div className="relative flex-1">
      <select
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className={`${inputCls} appearance-none pr-8 ${value ? "" : "text-gray-500"}`}
      >
        <option value="">{placeholder}</option>
        {BULAN_ID.map((b) => (
          <option key={b} value={b}>
            {b}
          </option>
        ))}
      </select>
      <ChevronDown
        size={14}
        className="pointer-events-none absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-500"
      />
    </div>
  );
}

export function MonthRange({
  name,
  label,
  onChange,
  defaultValue,
}: {
  name: string;
  label: string;
  onChange?: (range: { start: number; end: number } | null) => void;
  defaultValue?: string | null; // "Januari - Maret"
}) {
  const parts = defaultValue?.split(" - ") ?? [];
  const p0 = parts[0] ?? "";
  const p1 = parts[1] ?? "";
  const [dari, setDari] = useState((BULAN_ID as readonly string[]).includes(p0) ? p0 : "");
  const [sampai, setSampai] = useState((BULAN_ID as readonly string[]).includes(p1) ? p1 : "");

  function update(d: string, s: string) {
    setDari(d);
    setSampai(s);
    onChange?.(d && s ? { start: BULAN_ID.indexOf(d as (typeof BULAN_ID)[number]), end: BULAN_ID.indexOf(s as (typeof BULAN_ID)[number]) } : null);
  }

  return (
    <div>
      <label className="mb-1.5 block text-xs font-medium text-gray-600">{label}</label>
      <input
        type="hidden"
        name={name}
        value={dari && sampai ? `${dari} - ${sampai}` : ""}
        data-label={label}
      />
      <div className="flex items-center gap-2">
        <MonthSelect value={dari} onChange={(v) => update(v, sampai)} placeholder="Dari" />
        <span className="text-gray-300">-</span>
        <MonthSelect value={sampai} onChange={(v) => update(dari, v)} placeholder="Sampai" />
      </div>
    </div>
  );
}

// Generate tipe TS dari OpenAPI BE (springdoc di /v3/api-docs).
// Sengaja tanpa dependency: openapi-typescript mensyaratkan TypeScript ^5.x,
// sedangkan proyek ini di TS 7. Skrip ini hanya butuh Node fetch bawaan.
//
// Pemakaian:  npm run gen:api   (BE harus jalan; override lewat OPENAPI_URL)
// Hasil:      src/api/generated/schema.d.ts
import { mkdir, writeFile } from "node:fs/promises";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const url = process.env.OPENAPI_URL ?? "http://localhost:8080/v3/api-docs";
const out = resolve(dirname(fileURLToPath(import.meta.url)), "../src/api/generated/schema.d.ts");

const res = await fetch(url);
if (!res.ok) throw new Error(`Gagal mengambil ${url}: ${res.status} ${res.statusText}`);
const spec = await res.json();

const schemas = spec.components?.schemas ?? {};
const tsType = (s) => {
  if (!s) return "unknown";
  if (s.$ref) return s.$ref.split("/").pop();
  if (s.enum) return s.enum.map((v) => JSON.stringify(v)).join(" | ");
  if (s.type === "array") return `${tsType(s.items)}[]`;
  switch (s.type) {
    case "integer":
    case "number":
      return "number";
    case "boolean":
      return "boolean";
    case "string":
      return "string";
    default:
      return "unknown";
  }
};

const blocks = Object.entries(schemas).map(([name, schema]) => {
  const required = new Set(schema.required ?? []);
  const props = Object.entries(schema.properties ?? {});
  if (props.length === 0) return `export type ${name} = Record<string, unknown>;`;
  const fields = props.map(([key, s]) => {
    const opt = required.has(key) ? "" : "?";
    return `  ${key}${opt}: ${tsType(s)} | null;`;
  });
  return `export interface ${name} {\n${fields.join("\n")}\n}`;
});

const banner =
  "// AUTO-GENERATED dari OpenAPI BE (npm run gen:api). Jangan diedit tangan.\n" +
  `// Sumber: ${url}\n\n`;

await mkdir(dirname(out), { recursive: true });
await writeFile(out, banner + blocks.join("\n\n") + "\n");
console.log(`Wrote ${blocks.length} schema ke ${out}`);

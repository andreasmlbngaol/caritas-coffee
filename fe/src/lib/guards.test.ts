// Uji guard murni. Jalankan: `npm test` (Node test runner bawaan, tanpa deps).
import { test } from "node:test";
import assert from "node:assert/strict";
import { internalPath, enumOrNull } from "./guards.ts";

test("internalPath menerima path internal", () => {
  assert.equal(internalPath("/petani"), "/petani");
  assert.equal(internalPath("/petani?q=1"), "/petani?q=1");
});

test("internalPath menolak protocol-relative & absolut", () => {
  assert.equal(internalPath("//evil.com"), "/");
  assert.equal(internalPath("https://evil.com"), "/");
  assert.equal(internalPath(""), "/");
  assert.equal(internalPath(null), "/");
});

test("internalPath memakai fallback kustom", () => {
  assert.equal(internalPath("//evil.com", "/login"), "/login");
});

test("enumOrNull meneruskan nilai yang dikenal saja", () => {
  const ALLOWED = ["MS", "SW", "L"] as const;
  assert.equal(enumOrNull("MS", ALLOWED), "MS");
  assert.equal(enumOrNull("XX", ALLOWED), null);
  assert.equal(enumOrNull(undefined, ALLOWED), null);
  assert.equal(enumOrNull("", ALLOWED), null);
});

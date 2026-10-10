// Uji helper baca FormData. Jalankan: `npm test`.
import { test } from "node:test";
import assert from "node:assert/strict";
import { get, getBool, hasAnyWithPrefix, num, numOpt, intOpt, toDateInput } from "./form.ts";

const fd = (entries: Record<string, string>) => {
  const f = new FormData();
  for (const [k, v] of Object.entries(entries)) f.append(k, v);
  return f;
};

test("get: trim + kosong → undefined", () => {
  assert.equal(get(fd({ a: "  x  " }), "a"), "x");
  assert.equal(get(fd({ a: "   " }), "a"), undefined);
  assert.equal(get(fd({}), "a"), undefined);
});

test("getBool: hanya 'true' → true", () => {
  assert.equal(getBool(fd({ b: "true" }), "b"), true);
  assert.equal(getBool(fd({ b: "false" }), "b"), false);
  assert.equal(getBool(fd({}), "b"), false);
});

test("hasAnyWithPrefix", () => {
  assert.equal(hasAnyWithPrefix(fd({ plot_0_x: "1" }), "plot_0_"), true);
  assert.equal(hasAnyWithPrefix(fd({ plot_1_x: "1" }), "plot_0_"), false);
});

test("num/numOpt/intOpt: kosong & non-angka", () => {
  assert.equal(num(""), 0);
  assert.equal(num(null), 0);
  assert.equal(num("12.5"), 12.5);
  assert.equal(num("abc"), 0);
  assert.equal(numOpt(""), undefined);
  assert.equal(numOpt("12.5"), 12.5);
  assert.equal(intOpt("12.5"), undefined);
  assert.equal(intOpt("12"), 12);
});

test("toDateInput: potong ke YYYY-MM-DD", () => {
  assert.equal(toDateInput("2026-06-15T00:00:00Z"), "2026-06-15");
  assert.equal(toDateInput(null), "");
});

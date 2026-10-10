// Uji helper format murni. Jalankan: `npm test`.
import { test } from "node:test";
import assert from "node:assert/strict";
import { fmt, fmtBulanTahun, fmtDate, blankStr, blankNum } from "./format.ts";

test("fmt: kosong → '-'", () => {
  assert.equal(fmt(null), "-");
  assert.equal(fmt(undefined), "-");
  assert.equal(fmt(""), "-");
});

test("fmt: angka diformat id-ID + pembulatan", () => {
  assert.equal(fmt(1234), "1.234");
  assert.equal(fmt(1234.56, 1), "1.234,6");
  assert.equal(fmt("2500"), "2.500");
});

test("fmt: string non-angka dikembalikan apa adanya", () => {
  assert.equal(fmt("abc"), "abc");
});

test("fmtBulanTahun: '2026-06' → 'Juni 2026'", () => {
  assert.equal(fmtBulanTahun("2026-06"), "Juni 2026");
  assert.equal(fmtBulanTahun(null), "-");
  assert.equal(fmtBulanTahun(""), "-");
});

test("fmtDate: ISO → tanggal id-ID", () => {
  assert.equal(fmtDate("2026-06-15"), "15 Juni 2026");
  assert.equal(fmtDate(null), "-");
  assert.equal(fmtDate("bukan-tanggal"), "-");
});

test("blankStr/blankNum: placeholder & 0 → ''", () => {
  assert.equal(blankStr("-"), "");
  assert.equal(blankStr(null), "");
  assert.equal(blankStr("ada"), "ada");
  assert.equal(blankNum(0), "");
  assert.equal(blankNum(null), "");
  assert.equal(blankNum(5), 5);
});

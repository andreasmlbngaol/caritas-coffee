// Klien API tipis di atas fetch native. Selalu sertakan cookie sesi
// (credentials: "include"). Kesalahan server (ApiError BE) dibaca dari body
// JSON { error } dan dilempar sebagai ApiError.

export class ApiError extends Error {
  status: number;
  /** Peta error per-field dari BE (bila ada): { namaField: pesan }. */
  fields?: Record<string, string>;
  constructor(status: number, message: string, fields?: Record<string, string>) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.fields = fields;
  }
}

async function parseError(res: Response): Promise<{ message: string; fields?: Record<string, string> }> {
  try {
    const data = (await res.json()) as {
      error?: string;
      message?: string;
      fields?: Record<string, string>;
    };
    return {
      message: data.error ?? data.message ?? `Kesalahan ${res.status}`,
      fields: data.fields,
    };
  } catch {
    return { message: `Kesalahan ${res.status}` };
  }
}

async function request<T>(
  method: string,
  path: string,
  body?: unknown,
): Promise<T> {
  const res = await fetch(path, {
    method,
    credentials: "include",
    headers: body === undefined ? undefined : { "Content-Type": "application/json" },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (!res.ok) {
    const { message, fields } = await parseError(res);
    throw new ApiError(res.status, message, fields);
  }
  if (res.status === 204) return undefined as T;
  const text = await res.text();
  if (!text) return undefined as T;
  try {
    return JSON.parse(text) as T;
  } catch {
    // Body non-JSON (mis. halaman error HTML dari proxy) - jangan lempar
    // SyntaxError mentah; jadikan ApiError agar pesan error konsisten.
    throw new ApiError(res.status, `Respons tidak valid dari server (${res.status})`);
  }
}

/** URL foto dari key (`petani/xxx.webp`) - satu tempat, bukan hardcode di tiap komponen. */
export function fotoUrl(key: string): string {
  return `/api/foto/${key}`;
}

export const api = {
  get: <T>(path: string) => request<T>("GET", path),
  post: <T>(path: string, body?: unknown) => request<T>("POST", path, body),
  put: <T>(path: string, body?: unknown) => request<T>("PUT", path, body),
  patch: <T>(path: string, body?: unknown) => request<T>("PATCH", path, body),
  del: <T>(path: string) => request<T>("DELETE", path),
};

/** Unggah multipart (foto). Tidak set Content-Type agar boundary otomatis. */
export async function uploadFile(file: File): Promise<{ key: string }> {
  const fd = new FormData();
  fd.append("file", file);
  const res = await fetch("/api/upload", {
    method: "POST",
    credentials: "include",
    body: fd,
  });
  if (!res.ok) {
    const { message, fields } = await parseError(res);
    throw new ApiError(res.status, message, fields);
  }
  return (await res.json()) as { key: string };
}

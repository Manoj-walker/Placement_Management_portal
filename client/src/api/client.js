// Central HTTP client for the Placement NIT Spring Boot API.
// Base URL can be overridden with VITE_API_URL (see client/.env.example).

const BASE_URL = (import.meta.env.VITE_API_URL || "http://localhost:8080").replace(/\/+$/, "");
const TOKEN_KEY = "pn_token";

export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

export function getToken() {
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch {
    return null;
  }
}

export function setToken(token) {
  try {
    if (token) localStorage.setItem(TOKEN_KEY, token);
    else localStorage.removeItem(TOKEN_KEY);
  } catch {
    /* storage unavailable */
  }
}

function clearSessionAndRedirect() {
  setToken(null);
  try {
    localStorage.removeItem("pn_session");
  } catch {
    /* ignore */
  }
  if (window.location.pathname !== "/login") {
    window.location.assign("/login");
  }
}

async function request(path, { method = "GET", body, raw = false } = {}) {
  const headers = {};
  const token = getToken();
  if (token) headers.Authorization = `Bearer ${token}`;

  const options = { method, headers };
  if (body !== undefined) {
    if (body instanceof FormData) {
      options.body = body;
    } else {
      headers["Content-Type"] = "application/json";
      options.body = JSON.stringify(body);
    }
  }

  let response;
  try {
    response = await fetch(`${BASE_URL}${path}`, options);
  } catch {
    throw new ApiError(
      "Cannot reach the server. Start the backend (./mvnw spring-boot:run) and try again.",
      0
    );
  }

  if (response.status === 401 && !path.startsWith("/api/auth/")) {
    clearSessionAndRedirect();
    throw new ApiError("Your session expired. Please sign in again.", 401);
  }

  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    try {
      const data = await response.json();
      if (data?.error) message = data.error;
    } catch {
      /* non-JSON error body */
    }
    throw new ApiError(message, response.status);
  }

  if (raw) return response;
  if (response.status === 204) return null;

  const contentType = response.headers.get("content-type") || "";
  if (contentType.includes("application/json")) return response.json();
  return response.text();
}

export const api = {
  baseUrl: BASE_URL,
  get: (path) => request(path),
  post: (path, body) => request(path, { method: "POST", body }),
  put: (path, body) => request(path, { method: "PUT", body }),
  patch: (path, body) => request(path, { method: "PATCH", body }),
  del: (path) => request(path, { method: "DELETE" }),
  upload: (path, formData) => request(path, { method: "POST", body: formData }),
  download: (path) => request(path, { raw: true }),
};

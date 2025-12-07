import http from 'k6/http';
import { check } from 'k6';
import { BASE_URL, LOGIN_PATH, REQUEST_TIMEOUT, resolveLoginHeaders, resolveLoginPayload, pickTokenFromBody, ensureBaseUrl } from './config.js';

export function obtainAccessToken() {
  if (__ENV.K6_TOKEN) {
    return __ENV.K6_TOKEN;
  }

  const username = __ENV.K6_USERNAME;
  const password = __ENV.K6_PASSWORD;

  if (!username || !password) {
    console.warn('K6_TOKEN or K6_USERNAME/K6_PASSWORD were not provided. Proceeding without Authorization header.');
    return '';
  }

  const url = `${ensureBaseUrl()}${LOGIN_PATH}`;
  const payload = resolveLoginPayload();
  const headers = resolveLoginHeaders();

  const response = http.post(url, payload, {
    headers,
    timeout: REQUEST_TIMEOUT,
    tags: { name: 'auth_login' },
  });

  check(response, {
    'login responded': (res) => Boolean(res.status),
    'login success status': (res) => res.status >= 200 && res.status < 400,
  });

  const body = safeJson(response);
  const token = pickTokenFromBody(body);

  if (!token) {
    console.warn('Login succeeded but no token could be resolved. Consider supplying LOGIN_TOKEN_KEYS or K6_TOKEN.');
  }

  return token;
}

export function buildAuthHeaders(token, extra = {}) {
  const headers = { ...extra };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  return headers;
}

function safeJson(response) {
  try {
    return response.json();
  } catch (error) {
    console.warn(`Failed to parse login response body as JSON: ${error}`);
    return {};
  }
}

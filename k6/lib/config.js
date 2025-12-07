import { fail } from 'k6';

function parseJson(value, fallback) {
  if (!value) {
    return fallback;
  }

  try {
    return JSON.parse(value);
  } catch (error) {
    console.warn(`Unable to parse JSON value: ${error}`);
    return fallback;
  }
}

export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
export const LOGIN_PATH = __ENV.LOGIN_PATH || '/api/v1/auth/login';
export const REQUEST_TIMEOUT = __ENV.REQUEST_TIMEOUT || '60s';
export const MAX_SLEEP_SECONDS = Number(__ENV.MAX_SLEEP_SECONDS || 1);
export const RETRY_COUNT = Number(__ENV.REQUEST_RETRIES || 0);
export const RETRY_BACKOFF_MS = Number(__ENV.REQUEST_RETRY_BACKOFF_MS || 200);

const defaultStages = [
  { duration: '30s', target: 10 },
  { duration: '1m', target: 25 },
  { duration: '30s', target: 0 },
];

const defaultThresholds = {
  http_req_failed: ['rate<0.05'],
  http_req_duration: ['p(95)<1000'],
};

export const STAGES = parseJson(__ENV.K6_STAGES, defaultStages);
export const THRESHOLDS = parseJson(__ENV.K6_THRESHOLDS, defaultThresholds);
export const DEFAULT_HEADERS = parseJson(
  __ENV.DEFAULT_HEADERS,
  { 'Content-Type': 'application/json' },
);

export function resolveLoginHeaders() {
  return parseJson(__ENV.LOGIN_HEADERS, { 'Content-Type': 'application/json' });
}

export function resolveLoginPayload() {
  if (__ENV.LOGIN_PAYLOAD) {
    return __ENV.LOGIN_PAYLOAD;
  }

  const username = __ENV.K6_USERNAME || 'user@example.com';
  const password = __ENV.K6_PASSWORD || 'ChangeMe123!';

  return JSON.stringify({
    email: username,
    password,
  });
}

export function resolveTokenKeys() {
  const raw = __ENV.LOGIN_TOKEN_KEYS || 'token,accessToken,access_token,jwt';
  return raw
    .split(',')
    .map((entry) => entry.trim())
    .filter((entry) => entry.length > 0);
}

export function pickTokenFromBody(body) {
  if (!body || typeof body !== 'object') {
    return '';
  }

  for (const key of resolveTokenKeys()) {
    if (Object.prototype.hasOwnProperty.call(body, key) && body[key]) {
      return body[key];
    }
  }

  if (body.data && typeof body.data === 'object') {
    for (const key of resolveTokenKeys()) {
      if (Object.prototype.hasOwnProperty.call(body.data, key) && body.data[key]) {
        return body.data[key];
      }
    }
  }

  return '';
}

export function ensureBaseUrl() {
  if (!BASE_URL) {
    fail('BASE_URL is required and could not be resolved.');
  }
  return BASE_URL;
}

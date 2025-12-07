import http from 'k6/http';
import { check, sleep } from 'k6';
import { STAGES, THRESHOLDS, MAX_SLEEP_SECONDS, REQUEST_TIMEOUT, LOGIN_PATH, resolveLoginPayload, resolveLoginHeaders, ensureBaseUrl } from './lib/config.js';

export const options = {
  stages: STAGES,
  thresholds: THRESHOLDS,
};

const loginUrl = `${ensureBaseUrl()}${__ENV.AUTH_LOGIN_PATH || LOGIN_PATH}`;

export default function () {
  const response = http.post(loginUrl, resolveLoginPayload(), {
    headers: resolveLoginHeaders(),
    timeout: REQUEST_TIMEOUT,
    tags: { name: 'auth_login' },
  });

  check(response, {
    'status < 500': (res) => res.status && res.status < 500,
    'response time acceptable': (res) => res.timings.duration < 1000,
  });

  if (MAX_SLEEP_SECONDS > 0) {
    sleep(Math.random() * MAX_SLEEP_SECONDS);
  }
}

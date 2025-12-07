import { check, group, sleep } from 'k6';
import { STAGES, THRESHOLDS, MAX_SLEEP_SECONDS, DEFAULT_HEADERS, ensureBaseUrl } from './lib/config.js';
import { obtainAccessToken, buildAuthHeaders } from './lib/auth.js';
import { requestWithRetry } from './lib/http.js';

export const options = {
  stages: STAGES,
  thresholds: THRESHOLDS,
};

const baseUrl = ensureBaseUrl();

export function setup() {
  const token = obtainAccessToken();
  return { token };
}

export default function (data) {
  const headers = buildAuthHeaders(data.token, DEFAULT_HEADERS);

  group('auth-service-info', () => {
    const userInfo = requestWithRetry(
      'GET',
      `${baseUrl}/api/v1/auth/users/3edcBmQfHXg8WUGXCKdUizicAe43`, 
      null,
      { headers, tags: { name: 'get_user_info' } },
    );

    check(userInfo, {
      'get info status < 500': (res) => res.status && res.status < 500,
      'is rate limited (429)': (res) => res.status === 429,
    });
  });

  if (MAX_SLEEP_SECONDS > 0) {
    sleep(Math.random() * MAX_SLEEP_SECONDS);
  }
}
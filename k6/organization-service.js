import { check, group, sleep } from 'k6';
import { STAGES, THRESHOLDS, MAX_SLEEP_SECONDS, DEFAULT_HEADERS, ensureBaseUrl } from './lib/config.js';
import { obtainAccessToken, buildAuthHeaders } from './lib/auth.js';
import { requestWithRetry } from './lib/http.js';

export const options = {
  stages: STAGES,
  thresholds: THRESHOLDS,
};

const baseUrl = ensureBaseUrl();
const organizationId = __ENV.ORGANIZATION_ID || 'org_sample';
const organizationUid = __ENV.ORGANIZATION_USER_UID || 'user_sample';

export function setup() {
  const token = obtainAccessToken();
  return { token };
}

export default function (data) {
  const headers = buildAuthHeaders(data.token, DEFAULT_HEADERS);

  group('organization-service', () => {
    const list = requestWithRetry(
      'GET',
      `${baseUrl}/api/v1/organization-service/org`,
      null,
      { headers, tags: { name: 'organization_list' } },
    );

    check(list, {
      'list < 500': (res) => res.status && res.status < 500,
    });

    const byId = requestWithRetry(
      'GET',
      `${baseUrl}/api/v1/organization-service/org/${encodeURIComponent(organizationId)}`,
      null,
      { headers, tags: { name: 'organization_by_id' } },
    );

    check(byId, {
      'byId < 500': (res) => res.status && res.status < 500,
    });

    const permissions = requestWithRetry(
      'GET',
      `${baseUrl}/api/v1/organization-service/org/${encodeURIComponent(organizationId)}/permissions/${encodeURIComponent(organizationUid)}`,
      null,
      { headers, tags: { name: 'organization_permissions' } },
    );

    check(permissions, {
      'permissions < 500': (res) => res.status && res.status < 500,
    });
  });

  if (MAX_SLEEP_SECONDS > 0) {
    sleep(Math.random() * MAX_SLEEP_SECONDS);
  }
}

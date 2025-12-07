import { check, group, sleep } from 'k6';
import { STAGES, THRESHOLDS, MAX_SLEEP_SECONDS, DEFAULT_HEADERS, ensureBaseUrl } from './lib/config.js';
import { obtainAccessToken, buildAuthHeaders } from './lib/auth.js';
import { requestWithRetry } from './lib/http.js';

export const options = {
  stages: STAGES,
  thresholds: THRESHOLDS,
};

const baseUrl = ensureBaseUrl();
const bookingRequestId = __ENV.BOOKING_REQUEST_ID || 'bk_req_sample';
const bookingId = __ENV.BOOKING_ID || 'bk_sample';
const organizationId = __ENV.ORGANIZATION_ID || 'org_sample';
const statsStartDate = __ENV.BOOKING_STATS_START || '2024-01-01';
const statsEndDate = __ENV.BOOKING_STATS_END || '2024-12-31';

export function setup() {
  const token = obtainAccessToken();
  return { token };
}

export default function (data) {
  const headers = buildAuthHeaders(data.token, DEFAULT_HEADERS);

  group('booking-service', () => {
    const byRequest = requestWithRetry(
      'GET',
      `${baseUrl}/api/v1/booking-service?bookingRequestId=${encodeURIComponent(bookingRequestId)}`,
      null,
      { headers, tags: { name: 'booking_get_by_request' } },
    );

    check(byRequest, {
      'request lookup < 500': (res) => res.status && res.status < 500,
    });

    const byId = requestWithRetry(
      'GET',
      `${baseUrl}/api/v1/booking-service/${encodeURIComponent(bookingId)}`,
      null,
      { headers, tags: { name: 'booking_get_by_id' } },
    );

    check(byId, {
      'id lookup < 500': (res) => res.status && res.status < 500,
    });

    const stats = requestWithRetry(
      'GET',
      `${baseUrl}/api/v1/booking-service/dashboard/statistics?organizationId=${encodeURIComponent(organizationId)}&startDate=${encodeURIComponent(statsStartDate)}&endDate=${encodeURIComponent(statsEndDate)}`,
      null,
      { headers, tags: { name: 'booking_stats' } },
    );

    check(stats, {
      'stats response < 500': (res) => res.status && res.status < 500,
    });
  });

  if (MAX_SLEEP_SECONDS > 0) {
    sleep(Math.random() * MAX_SLEEP_SECONDS);
  }
}

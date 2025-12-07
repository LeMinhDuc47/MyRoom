import { check, group, sleep } from 'k6';
import { STAGES, THRESHOLDS, MAX_SLEEP_SECONDS, DEFAULT_HEADERS, ensureBaseUrl } from './lib/config.js';
import { obtainAccessToken, buildAuthHeaders } from './lib/auth.js';
import { requestWithRetry } from './lib/http.js';

export const options = {
  stages: STAGES,
  thresholds: THRESHOLDS,
};

const baseUrl = ensureBaseUrl();
const bookingId = __ENV.BOOKING_ID || 'bk_sample';
const organizationId = __ENV.ORGANIZATION_ID || 'org_sample';
const statsStartDate = __ENV.PAYMENT_STATS_START || '2024-01-01';
const statsEndDate = __ENV.PAYMENT_STATS_END || '2024-12-31';
const revenueDuration = __ENV.PAYMENT_REVENUE_DURATION || 'month';

export function setup() {
  const token = obtainAccessToken();
  return { token };
}

export default function (data) {
  const headers = buildAuthHeaders(data.token, DEFAULT_HEADERS);

  group('payment-service', () => {
    const byBooking = requestWithRetry(
      'GET',
      `${baseUrl}/api/v1/payment-service/orders/${encodeURIComponent(bookingId)}`,
      null,
      { headers, tags: { name: 'payment_get_by_booking' } },
    );

    check(byBooking, {
      'order lookup < 500': (res) => res.status && res.status < 500,
    });

    const stats = requestWithRetry(
      'GET',
      `${baseUrl}/api/v1/payment-service/dashboard/statistics?organizationId=${encodeURIComponent(organizationId)}&startDate=${encodeURIComponent(statsStartDate)}&endDate=${encodeURIComponent(statsEndDate)}`,
      null,
      { headers, tags: { name: 'payment_stats' } },
    );

    check(stats, {
      'stats < 500': (res) => res.status && res.status < 500,
    });

    const revenue = requestWithRetry(
      'GET',
      `${baseUrl}/api/v1/payment-service/dashboard/statistics/revenue?duration=${encodeURIComponent(revenueDuration)}&organizationId=${encodeURIComponent(organizationId)}`,
      null,
      { headers, tags: { name: 'payment_revenue' } },
    );

    check(revenue, {
      'revenue < 500': (res) => res.status && res.status < 500,
    });
  });

  if (MAX_SLEEP_SECONDS > 0) {
    sleep(Math.random() * MAX_SLEEP_SECONDS);
  }
}

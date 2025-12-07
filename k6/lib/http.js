import http from 'k6/http';
import { sleep } from 'k6';
import { REQUEST_TIMEOUT, RETRY_COUNT, RETRY_BACKOFF_MS } from './config.js';

export function requestWithRetry(method, url, payload, params = {}, retries = RETRY_COUNT) {
  let attempt = 0;
  let response;

  while (attempt <= retries) {
    response = send(method, url, payload, params);

    if (response.status && response.status < 500) {
      return response;
    }

    attempt += 1;
    if (attempt <= retries) {
      const backoffSeconds = (RETRY_BACKOFF_MS / 1000) * attempt;
      sleep(backoffSeconds);
    }
  }

  return response;
}

function send(method, url, payload, params) {
  const requestParams = { ...params, timeout: params.timeout || REQUEST_TIMEOUT };

  switch (method) {
    case 'GET':
      return http.get(url, requestParams);
    case 'POST':
      return http.post(url, payload, requestParams);
    case 'PUT':
      return http.put(url, payload, requestParams);
    case 'PATCH':
      return http.patch(url, payload, requestParams);
    case 'DELETE':
      return http.del(url, payload, requestParams);
    default:
      throw new Error(`Unsupported method ${method}`);
  }
}

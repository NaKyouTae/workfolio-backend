import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * 에러율 알림 테스트
 *
 * 테스트 대상 알림:
 *   - high-error-rate (critical): 5xx > 5%, for: 0s → 즉시 발동
 *   - elevated-error-rate (warning): 5xx > 1%, for: 5m → 5분 후 발동
 *
 * 실행: k6 run error-rate-test.js
 */

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  scenarios: {
    // 정상 트래픽 (에러율 계산의 분모)
    normal_traffic: {
      executor: 'constant-arrival-rate',
      rate: 5,
      timeUnit: '1s',
      duration: '7m',
      preAllocatedVUs: 5,
      exec: 'normalTraffic',
    },
    // 에러 트래픽 (5xx 발생)
    error_traffic: {
      executor: 'constant-arrival-rate',
      rate: 10,
      timeUnit: '1s',
      duration: '7m',
      preAllocatedVUs: 10,
      exec: 'errorTraffic',
    },
  },
};

export function normalTraffic() {
  const res = http.get(`${BASE_URL}/api/test/health`);
  check(res, { 'health: 200': (r) => r.status === 200 });
}

export function errorTraffic() {
  http.get(`${BASE_URL}/api/test/error`);
  // 500 응답이 정상 동작
}

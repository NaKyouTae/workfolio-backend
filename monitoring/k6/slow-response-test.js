import http from 'k6/http';
import { check } from 'k6';

/**
 * 응답시간 알림 테스트
 *
 * 테스트 대상 알림:
 *   - slow-response (warning): p95 > 1s, for: 5m → 5분 후 발동
 *
 * /api/test/slow?duration=3000 호출 → 응답시간 3초
 * p95가 1초를 초과하여 알림 트리거
 *
 * 실행: k6 run slow-response-test.js
 */

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  scenarios: {
    slow_requests: {
      executor: 'constant-vus',
      vus: 10,
      duration: '7m',
      exec: 'slowRequest',
    },
  },
};

export function slowRequest() {
  const res = http.get(`${BASE_URL}/api/test/slow?duration=3000`, {
    timeout: '10s',
  });
  check(res, { 'slow: 200': (r) => r.status === 200 });
}

import http from 'k6/http';
import { check } from 'k6';

/**
 * Tomcat 스레드 고갈 테스트
 *
 * 테스트 대상 알림:
 *   - tomcat-threads-high (warning): 스레드 사용률 > 80%, for: 3m → 3분 후 발동
 *
 * Tomcat 기본 max-threads=200
 * 180 VUs × 5초 sleep = 180개 스레드 점유 (90%)
 *
 * 실행: k6 run thread-exhaustion-test.js
 */

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  scenarios: {
    thread_exhaustion: {
      executor: 'ramping-vus',
      stages: [
        { duration: '1m', target: 100 },
        { duration: '1m', target: 180 },
        { duration: '5m', target: 180 },
        { duration: '1m', target: 0 },
      ],
      exec: 'occupyThread',
    },
  },
};

export function occupyThread() {
  const res = http.get(`${BASE_URL}/api/test/slow?duration=5000`, {
    timeout: '15s',
  });
  check(res, { 'thread: 200': (r) => r.status === 200 });
}

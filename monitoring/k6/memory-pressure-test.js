import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * JVM 메모리 압박 테스트
 *
 * 테스트 대상 알림:
 *   - jvm-heap-warning (warning): Heap > 70%, for: 10m
 *   - jvm-heap-critical (critical): Heap > 90%, for: 0s → 즉시 발동
 *   - gc-pause-high (warning): GC Pause 증가, for: 5m
 *
 * 50MB씩 메모리 할당 → Heap 사용량 증가
 * 테스트 종료 시 자동 해제
 *
 * 실행: k6 run memory-pressure-test.js
 */

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  scenarios: {
    memory_allocation: {
      executor: 'per-vu-iterations',
      vus: 1,
      iterations: 15,
      exec: 'allocateMemory',
    },
  },
};

export function allocateMemory() {
  const res = http.get(`${BASE_URL}/api/test/memory?sizeMb=50`);
  check(res, { 'memory: 200': (r) => r.status === 200 });

  if (res.status === 200) {
    const body = JSON.parse(res.body);
    console.log(
      `Heap: ${body.heapUsedMb}MB / ${body.heapMaxMb}MB (${body.heapUsagePercent}%)`
    );
  }

  sleep(10);
}

export function teardown() {
  console.log('Releasing allocated memory...');
  const res = http.del(`${BASE_URL}/api/test/memory`);
  console.log(`Cleanup: ${res.body}`);
}

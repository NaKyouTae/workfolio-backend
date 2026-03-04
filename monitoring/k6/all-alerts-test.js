import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * 전체 알림 통합 테스트
 *
 * 모든 알림을 단일 실행으로 테스트합니다.
 *
 * Phase 1 (0~8분): 에러율 테스트
 *   → high-error-rate (critical, 5xx > 5%, for: 0s)
 *   → elevated-error-rate (warning, 5xx > 1%, for: 5m)
 *
 * Phase 2 (9~20분): 느린 응답 + 스레드 고갈 (11분간)
 *   → slow-response (warning, p95 > 1s, for: 5m)
 *   → tomcat-threads-high (warning, 스레드 > 80%, for: 3m)
 *
 * Phase 3 (21~35분): 메모리 압박 + GC Pause (14분간)
 *   → jvm-heap-warning (warning, Heap > 70%, for: 10m)
 *   → jvm-heap-critical (critical, Heap > 90%, for: 0s)
 *   → gc-pause-high (warning, GC Pause > 5%, for: 5m)
 *
 * Phase 4 (0~5분): DB 커넥션 풀 고갈
 *   → hikaricp-exhausted (critical, 커넥션 > 90%, for: 0s)
 *
 * Phase 5 (36분): Redis 메모리 채우기
 *   → redis-memory-high (critical, 메모리 > 80%, for: 0s)
 *
 * Phase 6 (37~45분): Redis eviction 유지
 *   → redis-eviction (warning, eviction > 1/s, for: 5m)
 *
 * 테스트 불가:
 *   - server-down: 서버 중지로만 테스트 가능
 *
 * 실행: ./run-tests.sh
 * 총 소요시간: 약 45분
 */

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  scenarios: {
    // ── 정상 트래픽 (전체 시간 동안) ──
    normal_traffic: {
      executor: 'constant-arrival-rate',
      rate: 5,
      timeUnit: '1s',
      duration: '45m',
      preAllocatedVUs: 5,
      exec: 'normalTraffic',
    },

    // ── Phase 1: 에러율 테스트 (0~8분) ──
    error_traffic: {
      executor: 'constant-arrival-rate',
      rate: 10,
      timeUnit: '1s',
      duration: '8m',
      preAllocatedVUs: 10,
      exec: 'errorTraffic',
    },

    // ── Phase 2: 느린 응답 + 스레드 고갈 (9~20분, 11분간) ──
    slow_and_threads: {
      executor: 'ramping-vus',
      startTime: '9m',
      stages: [
        { duration: '1m', target: 100 },
        { duration: '1m', target: 200 },
        { duration: '8m', target: 200 },
        { duration: '1m', target: 0 },
      ],
      exec: 'slowTraffic',
    },

    // ── Phase 3a: 메모리 압박 (21~35분, 14분간) ──
    memory_pressure: {
      executor: 'per-vu-iterations',
      vus: 1,
      iterations: 20,
      startTime: '21m',
      exec: 'memoryPressure',
    },

    // ── Phase 3b: GC 압박 (23~35분) ──
    // 힙이 채워진 상태에서 대량 할당/폐기 → old gen GC 유발
    gc_pressure: {
      executor: 'constant-vus',
      vus: 5,
      duration: '12m',
      startTime: '23m',
      exec: 'gcChurn',
    },

    // ── Phase 4: DB 커넥션 고갈 (0~5분) ──
    db_exhaustion: {
      executor: 'constant-vus',
      vus: 15,
      duration: '5m',
      exec: 'lockDb',
    },

    // ── Phase 5: Redis 메모리 채우기 (36분 시점) ──
    redis_fill: {
      executor: 'per-vu-iterations',
      vus: 1,
      iterations: 1,
      startTime: '36m',
      exec: 'fillRedis',
    },

    // ── Phase 6: Redis eviction 유지 (37~45분) ──
    redis_eviction: {
      executor: 'constant-arrival-rate',
      rate: 5,
      timeUnit: '1s',
      duration: '8m',
      startTime: '37m',
      preAllocatedVUs: 5,
      exec: 'triggerEviction',
    },
  },
};

// ── Phase 함수 ──

export function normalTraffic() {
  const res = http.get(`${BASE_URL}/api/test/health`);
  check(res, { 'health: 200': (r) => r.status === 200 });
}

export function errorTraffic() {
  http.get(`${BASE_URL}/api/test/error`);
}

export function slowTraffic() {
  const res = http.get(`${BASE_URL}/api/test/slow?duration=10000`, {
    timeout: '30s',
  });
  check(res, { 'slow: 200': (r) => r.status === 200 });
}

export function memoryPressure() {
  const res = http.get(`${BASE_URL}/api/test/memory?sizeMb=100`);
  if (res.status === 200) {
    const body = JSON.parse(res.body);
    console.log(
      `[Memory] Heap: ${body.heapUsedMb}MB / ${body.heapMaxMb}MB (${body.heapUsagePercent}%)`
    );
  }
  sleep(30);
}

export function gcChurn() {
  const res = http.get(`${BASE_URL}/api/test/gc-churn?iterations=300&sizeMb=30`, {
    timeout: '120s',
  });
  check(res, { 'gc: 200': (r) => r.status === 200 });
  sleep(1);
}

export function lockDb() {
  const res = http.get(`${BASE_URL}/api/test/db-lock?duration=15`, {
    timeout: '30s',
  });
  check(res, { 'db: responded': (r) => r.status !== 0 });
}

export function fillRedis() {
  console.log('=== Filling Redis to trigger memory alert ===');
  const res = http.post(`${BASE_URL}/api/test/redis-fill?count=500&sizeBytes=524288`, null, {
    timeout: '120s',
  });
  if (res.status === 200) {
    const body = JSON.parse(res.body);
    console.log(`Redis filled: ${body.keys} keys, ~${body.totalMb}MB`);
  } else {
    console.log(`Redis fill failed: ${res.status} ${res.body}`);
  }
}

export function triggerEviction() {
  http.post(
    `${BASE_URL}/api/test/redis-fill?count=1&sizeBytes=10240`,
    null,
    { timeout: '10s' }
  );
}

// ── 정리 ──

export function teardown() {
  console.log('=== Cleanup ===');

  const memRes = http.del(`${BASE_URL}/api/test/memory`);
  console.log(`Memory cleanup: ${memRes.body}`);

  const redisRes = http.del(`${BASE_URL}/api/test/redis-fill`);
  console.log(`Redis cleanup: ${redisRes.body}`);
}

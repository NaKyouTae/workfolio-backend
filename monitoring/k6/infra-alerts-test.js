import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * 인프라 알림 테스트 (미발동 5개 알림 전용)
 *
 * Phase 1 (0~10분): Tomcat 스레드 고갈 + GC Pause
 *   → tomcat-threads-high (warning, 스레드 > 80%, for: 3m)
 *   → gc-pause-high (warning, GC Pause > 5%, for: 5m)
 *   - 먼저 힙 메모리를 채운 후 GC churn → old gen GC 유발
 *
 * Phase 2 (0~5분): DB 커넥션 풀 고갈
 *   → hikaricp-exhausted (critical, 커넥션 > 90%, for: 0s)
 *
 * Phase 3 (11분): Redis 메모리 채우기
 *   → redis-memory-high (critical, 메모리 > 80%, for: 0s)
 *
 * Phase 4 (12~20분): Redis eviction 유지 (계속 쓰기)
 *   → redis-eviction (warning, eviction > 1/s, for: 5m)
 *
 * 실행: ./run-tests.sh infra-alerts
 * 총 소요시간: 약 20분
 */

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  scenarios: {
    // ── Phase 1a: 힙 메모리 채우기 (0분, 1회) ──
    // 힙을 70%+ 채워서 이후 GC churn이 old gen GC를 유발하도록
    heap_fill: {
      executor: 'per-vu-iterations',
      vus: 1,
      iterations: 1,
      exec: 'fillHeap',
    },

    // ── Phase 1b: Tomcat 스레드 고갈 (0~10분) ──
    // 200 VUs × 10초 sleep = Tomcat 200 스레드 전부 점유
    thread_exhaustion: {
      executor: 'constant-vus',
      vus: 200,
      duration: '10m',
      exec: 'occupyThread',
    },

    // ── Phase 1c: GC 압박 (1~10분) ──
    // 힙이 채워진 상태에서 대량 할당/폐기 → old gen mixed GC 유발
    gc_pressure: {
      executor: 'constant-vus',
      vus: 5,
      duration: '9m',
      startTime: '1m',
      exec: 'gcChurn',
    },

    // ── Phase 2: DB 커넥션 고갈 (0~5분) ──
    // HikariCP 기본 max=10, 15개 동시 요청으로 고갈
    db_exhaustion: {
      executor: 'constant-vus',
      vus: 15,
      duration: '5m',
      exec: 'lockDb',
    },

    // ── Phase 3: Redis 메모리 채우기 (11분 시점, 1회) ──
    // 500 keys × 512KB = ~256MB → Redis maxmemory 256MB의 100%
    redis_fill: {
      executor: 'per-vu-iterations',
      vus: 1,
      iterations: 1,
      startTime: '11m',
      exec: 'fillRedis',
    },

    // ── Phase 4: Redis eviction 유지 (12~20분) ──
    // Redis가 가득 찬 상태에서 계속 쓰기 → eviction 발생
    redis_eviction: {
      executor: 'constant-arrival-rate',
      rate: 5,
      timeUnit: '1s',
      duration: '8m',
      startTime: '12m',
      preAllocatedVUs: 5,
      exec: 'triggerEviction',
    },
  },
};

// ── Phase 함수 ──

export function fillHeap() {
  console.log('=== Filling JVM heap to increase GC pressure ===');
  for (let i = 0; i < 15; i++) {
    const res = http.get(`${BASE_URL}/api/test/memory?sizeMb=100`, {
      timeout: '30s',
    });
    if (res.status === 200) {
      const body = JSON.parse(res.body);
      console.log(`[Heap] Chunk ${i + 1}: ${body.heapUsedMb}MB / ${body.heapMaxMb}MB (${body.heapUsagePercent}%)`);
    } else {
      console.log(`[Heap] Chunk ${i + 1} failed: ${res.status}`);
      break;
    }
    sleep(1);
  }
}

export function occupyThread() {
  const res = http.get(`${BASE_URL}/api/test/slow?duration=10000`, {
    timeout: '30s',
  });
  check(res, { 'thread: responded': (r) => r.status !== 0 });
}

export function gcChurn() {
  const res = http.get(`${BASE_URL}/api/test/gc-churn?iterations=300&sizeMb=30`, {
    timeout: '120s',
  });
  check(res, { 'gc: 200': (r) => r.status === 200 });
  sleep(1);
}

export function lockDb() {
  // SELECT SLEEP(15) → 15초간 DB 커넥션 점유
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
  // Redis가 가득 찬 상태에서 새 키 쓰기 → eviction 발생
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

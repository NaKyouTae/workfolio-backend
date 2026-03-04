#!/bin/bash

# ============================================
# k6 알림 테스트 실행 스크립트
# ============================================
#
# 사전 준비:
#   brew install k6
#
# 사용법:
#   ./run-tests.sh                     # 전체 알림 테스트 (21분)
#   ./run-tests.sh error-rate          # 에러율 알림만 (7분)
#   ./run-tests.sh slow-response       # 응답시간 알림만 (7분)
#   ./run-tests.sh thread-exhaustion   # 스레드 고갈 알림만 (8분)
#   ./run-tests.sh memory-pressure     # 메모리 압박 알림만 (3분)
#   ./run-tests.sh infra-alerts        # 인프라 알림 테스트 (18분)
#
# 환경변수:
#   BASE_URL=http://localhost:8080     # API 서버 주소
#   PROMETHEUS_URL=http://localhost:9090/api/v1/write  # Prometheus remote write

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TEST_NAME="${1:-all-alerts}"
BASE_URL="${BASE_URL:-http://localhost:8080}"
PROMETHEUS_URL="${PROMETHEUS_URL:-http://localhost:9090/api/v1/write}"

# k6 설치 확인
if ! command -v k6 &> /dev/null; then
  echo "k6가 설치되어 있지 않습니다."
  echo "설치: brew install k6"
  exit 1
fi

# 스크립트 파일 매핑
case "$TEST_NAME" in
  all-alerts)       SCRIPT="all-alerts-test.js" ;;
  error-rate)       SCRIPT="error-rate-test.js" ;;
  slow-response)    SCRIPT="slow-response-test.js" ;;
  thread-exhaustion) SCRIPT="thread-exhaustion-test.js" ;;
  memory-pressure)  SCRIPT="memory-pressure-test.js" ;;
  infra-alerts)     SCRIPT="infra-alerts-test.js" ;;
  *)
    echo "알 수 없는 테스트: $TEST_NAME"
    echo "사용 가능: all-alerts, error-rate, slow-response, thread-exhaustion, memory-pressure, infra-alerts"
    exit 1
    ;;
esac

# 서버 상태 확인 (actuator → test health 순서)
echo "서버 상태 확인: $BASE_URL"
if curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/health" 2>/dev/null | grep -q "200"; then
  echo "서버 정상 확인 (actuator)"
elif curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/test/health" 2>/dev/null | grep -q "200"; then
  echo "서버 정상 확인 (test health)"
else
  echo "서버가 응답하지 않습니다."
  echo "서버를 먼저 실행해주세요."
  exit 1
fi

echo ""
echo "============================================"
echo "  k6 알림 테스트: $TEST_NAME"
echo "  서버: $BASE_URL"
echo "  Prometheus: $PROMETHEUS_URL"
echo "============================================"
echo ""

# k6 실행 (Prometheus remote write 연동)
K6_PROMETHEUS_RW_SERVER_URL="$PROMETHEUS_URL" \
  k6 run \
  -e BASE_URL="$BASE_URL" \
  -o experimental-prometheus-rw \
  "$SCRIPT_DIR/$SCRIPT"

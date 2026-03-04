#!/bin/bash
set -e

echo "=== Workfolio Docker 환경 초기화 및 실행 ==="

# 1. 기존 컨테이너 중지 및 제거
echo ""
echo "[1/5] 기존 컨테이너 정리"
docker compose down --remove-orphans 2>/dev/null || true
docker compose -f docker-infra-compose.yml down --remove-orphans 2>/dev/null || true

# 2. 네트워크 생성 (이미 있으면 무시)
echo ""
echo "[2/5] server-network 생성"
docker network create server-network 2>/dev/null || echo "  -> server-network 이미 존재"

# 3. Spring Boot 빌드
echo ""
echo "[3/5] Spring Boot 빌드"
./gradlew build -x test
echo "  -> JAR 빌드 완료"

# 4. Docker 이미지 빌드
echo ""
echo "[4/5] Docker 이미지 빌드"
docker rmi workfolio-server:local 2>/dev/null || true
docker build -t workfolio-server:local .
echo "  -> 이미지 빌드 완료"

# 5. Docker Compose 실행
echo ""
echo "[5/5] Docker Compose 실행"
docker compose -f docker-infra-compose.yml up -d
echo "  -> 모니터링 인프라 시작 (prometheus, grafana, loki, promtail, tempo)"

docker compose up -d
echo "  -> Workfolio 서비스 시작 (postgres, workfolio-server)"

# 헬스체크 대기
echo ""
echo "=== 헬스체크 ==="

check_service() {
  local name=$1
  local url=$2
  local max_retry=${3:-10}

  for i in $(seq 1 $max_retry); do
    if curl -sf "$url" >/dev/null 2>&1; then
      echo "  [OK] $name"
      return 0
    fi
    sleep 3
  done
  echo "  [FAIL] $name"
  return 1
}

sleep 5
check_service "Prometheus" "http://localhost:9090/-/healthy" 10
check_service "Grafana" "http://localhost:3200/api/health" 15
check_service "Loki" "http://localhost:3100/ready" 10
check_service "Workfolio API" "http://localhost:9000/actuator/health" 20

echo ""
echo "=== 실행 완료 ==="
echo "  Workfolio API : http://localhost:9000"
echo "  PostgreSQL    : localhost:5432"
echo "  Prometheus    : http://localhost:9090"
echo "  Grafana       : http://localhost:3200 (workfolio/admin)"
echo "  Loki          : http://localhost:3100"
echo "  Tempo (OTLP)  : localhost:4317 (gRPC), localhost:4318 (HTTP)"
echo ""
echo "  중지: docker compose down"
echo "        docker compose -f docker-infra-compose.yml down"

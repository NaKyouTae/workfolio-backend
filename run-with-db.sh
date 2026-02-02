#!/bin/bash

echo "=== Crimson API Service with Database & Monitoring ==="

# 기존 컨테이너 정리
echo "1. 기존 컨테이너 정리"
docker-compose -f docker-compose.yml down
docker stop crimson-server mysql redis prometheus grafana 2>/dev/null || true
docker rm crimson-server mysql redis prometheus grafana 2>/dev/null || true

# Docker Compose로 전체 환경 실행
echo "2. Docker Compose로 전체 환경 실행"
docker-compose -f docker-compose.yml up -d

# MySQL 시작 대기
echo "3. MySQL 시작 대기 (10초)"
sleep 10

# MySQL 연결 확인
echo "4. MySQL 연결 확인"
for i in {1..5}; do
    if docker exec mysql mysql -u crimson -ptmrfounders0614 -e "SELECT 1" >/dev/null 2>&1; then
        echo "✅ MySQL 연결 성공"
        break
    else
        echo "⏳ MySQL 연결 대기 중... ($i/5)"
        sleep 3
    fi
    if [ $i -eq 5 ]; then
        echo "❌ MySQL 연결 실패"
        docker-compose logs mysql
        exit 1
    fi
done

# Redis 연결 확인
echo "5. Redis 연결 확인"
for i in {1..5}; do
    if docker exec redis redis-cli ping >/dev/null 2>&1; then
        echo "✅ Redis 연결 성공"
        break
    else
        echo "⏳ Redis 연결 대기 중... ($i/5)"
        sleep 3
    fi
    if [ $i -eq 5 ]; then
        echo "❌ Redis 연결 실패"
        docker-compose logs redis
        exit 1
    fi
done

# Prometheus 기동 확인
echo "6. Prometheus 기동 확인 (10초)"
sleep 10
for i in {1..5}; do
    if curl -f http://localhost:9090 >/dev/null 2>&1; then
        echo "✅ Prometheus 기동 성공"
        break
    else
        echo "⏳ Prometheus 기동 대기 중... ($i/5)"
        sleep 3
    fi
    if [ $i -eq 5 ]; then
        echo "❌ Prometheus 기동 실패"
        docker-compose logs prometheus
        exit 1
    fi
done

# Grafana 기동 확인
echo "7. Grafana 기동 확인 (10초)"
sleep 10
for i in {1..5}; do
    if curl -f http://localhost:4000/login >/dev/null 2>&1; then
        echo "✅ Grafana 기동 성공"
        break
    else
        echo "⏳ Grafana 기동 대기 중... ($i/5)"
        sleep 3
    fi
    if [ $i -eq 5 ]; then
        echo "❌ Grafana 기동 실패"
        docker-compose logs grafana
        exit 1
    fi
done

# 애플리케이션 시작 대기
echo "8. 애플리케이션 시작 대기 (10초)"
sleep 10

# Health Check
echo "9. Health Check"
for i in {1..5}; do
    if curl -f http://localhost:9000/api/order/health >/dev/null 2>&1; then
        echo "✅ Health Check 성공"
        break
    else
        echo "⏳ Health Check 대기 중... ($i/5)"
        sleep 3
    fi
    if [ $i -eq 5 ]; then
        echo "❌ Health Check 실패"
        docker-compose logs api-service
        exit 1
    fi
done

# API 테스트
echo "10. API 테스트"
curl -X POST http://localhost:9000/api/sample \
  -H "Content-Type: application/json" \
  -d '{"name": "Docker"}' \
  -w "\nHTTP Status: %{http_code}\n"

echo "11. 컨테이너 정보"
echo "=== 컨테이너 상태 ==="
docker-compose ps

echo "=== 네트워크 정보 ==="
docker network ls | grep crimson

echo "✅ 실행 완료!"
echo "🌐 애플리케이션 접속: http://localhost:9000"
echo "📊 Health Check: http://localhost:9000/api/order/health"
echo "🗄️ MySQL 접속: localhost:3306"
echo "🔴 Redis 접속: localhost:6379"
echo "📈 Prometheus 접속: http://localhost:9090"
echo "📊 Grafana 접속: http://localhost:4000 (기본 계정: admin/admin)"
echo "📝 로그 확인: docker-compose logs -f"
echo "🛑 중지: docker-compose down"
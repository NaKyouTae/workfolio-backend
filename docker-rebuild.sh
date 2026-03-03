#!/bin/bash
set -e

echo "=== 1. JAR 빌드 ==="
./gradlew bootJar

echo "=== 2. Docker 이미지 빌드 ==="
docker build -t workfolio-server:local .

echo "=== 3. 기존 컨테이너 & 볼륨 제거 ==="
docker compose down -v

echo "=== 4. 컨테이너 실행 ==="
docker compose up -d

echo "=== 완료 ==="
docker compose ps

#!/bin/bash

echo "🚀 workfolio Docker Compose 실행"

# 실행 권한 확인
if [ ! -x "run-with-db.sh" ]; then
    chmod +x run-with-db.sh
fi

# Docker 이미지 빌드
echo "🔨 Docker 이미지 빌드 시작..."
echo "📦 API 서비스 이미지 빌드 중..."

# 기존 이미지 정리
docker rmi workfolio-server:local 2>/dev/null || true

# Docker 이미지 빌드
if docker build --file /Dockerfile . -t workfolio-server:local; then
    echo "✅ Docker 이미지 빌드 성공"
else
    echo "❌ Docker 이미지 빌드 실패"
    exit 1
fi

# 빌드된 이미지 확인
echo "📋 빌드된 이미지 확인:"
docker images | grep workfolio

# Docker Compose 실행
echo ""
echo "📦 Docker Compose 시작..."
./run-with-db.sh

echo "==== Done ===="
#!/bin/bash
# Docker Compose 완전 초기화 및 재시작 스크립트
# 사용법: ./scripts/docker-compose-reset.sh [--rebuild-jar] [--rebuild-image]

set -e

REBUILD_JAR=false
REBUILD_IMAGE=true

# 인자 파싱
while [[ $# -gt 0 ]]; do
    case $1 in
        --rebuild-jar)
            REBUILD_JAR=true
            shift
            ;;
        --no-rebuild-image)
            REBUILD_IMAGE=false
            shift
            ;;
        *)
            echo "알 수 없는 옵션: $1"
            echo "사용법: $0 [--rebuild-jar] [--no-rebuild-image]"
            exit 1
            ;;
    esac
done

echo "🔄 Docker Compose 완전 초기화 시작..."
echo ""

# 1. 기존 컨테이너 및 볼륨 완전 삭제
echo "1️⃣  기존 컨테이너 및 볼륨 삭제 중..."
docker-compose -f docker-compose.ec2.yml down -v --remove-orphans || true
echo "✅ 컨테이너 및 볼륨 삭제 완료"
echo ""

# 2. 사용하지 않는 Docker 리소스 정리
echo "2️⃣  사용하지 않는 Docker 리소스 정리 중..."
docker system prune -f || true
echo "✅ 리소스 정리 완료"
echo ""

# 3. JAR 파일 재빌드 (옵션)
if [ "$REBUILD_JAR" = true ]; then
    echo "3️⃣  JAR 파일 재빌드 중..."
    ./gradlew clean bootJar -x test
    if [ $? -ne 0 ]; then
        echo "❌ JAR 파일 빌드 실패"
        exit 1
    fi
    echo "✅ JAR 파일 빌드 완료"
    echo ""
else
    echo "3️⃣  JAR 파일 빌드 건너뛰기 (--rebuild-jar 옵션 사용 시 재빌드)"
    echo ""
fi

# 4. Docker 이미지 재빌드
if [ "$REBUILD_IMAGE" = true ]; then
    echo "4️⃣  Docker 이미지 재빌드 중..."
    docker build -t workfolio-server:latest -f Dockerfile .
    if [ $? -ne 0 ]; then
        echo "❌ Docker 이미지 빌드 실패"
        exit 1
    fi
    echo "✅ Docker 이미지 빌드 완료"
    echo ""
else
    echo "4️⃣  Docker 이미지 빌드 건너뛰기"
    echo ""
fi

# 5. 서비스 시작
echo "5️⃣  서비스 시작 중..."
docker-compose -f docker-compose.ec2.yml up -d
if [ $? -ne 0 ]; then
    echo "❌ 서비스 시작 실패"
    exit 1
fi
echo "✅ 서비스 시작 완료"
echo ""

# 6. 서비스 상태 확인
echo "6️⃣  서비스 상태 확인 중..."
sleep 5
docker-compose -f docker-compose.ec2.yml ps
echo ""

# 7. 로그 확인 안내
echo "📋 다음 명령어로 로그를 확인할 수 있습니다:"
echo "   docker-compose -f docker-compose.ec2.yml logs -f workfolio-server"
echo ""
echo "✅ 초기화 및 재시작 완료!"


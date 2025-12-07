#!/bin/bash
# EC2 배포 스크립트
# 사용법: ./ec2-deploy.sh [--build] [--pull]

set -e

BUILD=false
PULL=false

# 인자 파싱
while [[ $# -gt 0 ]]; do
    case $1 in
        --build)
            BUILD=true
            shift
            ;;
        --pull)
            PULL=true
            shift
            ;;
        *)
            echo "알 수 없는 옵션: $1"
            echo "사용법: $0 [--build] [--pull]"
            exit 1
            ;;
    esac
done

echo "🚀 배포 시작..."

# 프로젝트 디렉토리 확인
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ 오류: docker-compose.yml 파일을 찾을 수 없습니다."
    echo "프로젝트 루트 디렉토리에서 실행해주세요."
    exit 1
fi

# Git에서 최신 코드 가져오기
if [ "$PULL" = true ]; then
    echo "📥 최신 코드 가져오기..."
    git pull origin main || echo "⚠️  Git pull 실패 (계속 진행)"
fi

# 기존 컨테이너 중지
echo "🛑 기존 컨테이너 중지 중..."
docker-compose down || true

# Docker 이미지 빌드
if [ "$BUILD" = true ]; then
    echo "🔨 Docker 이미지 빌드 중..."
    docker build -t workfolio-server:latest -f Dockerfile .
else
    echo "⏭️  이미지 빌드 건너뛰기 (--build 옵션 사용 시 빌드)"
fi

# 사용하지 않는 이미지 정리 (선택사항)
echo "🧹 사용하지 않는 Docker 리소스 정리 중..."
docker image prune -f || true

# 서비스 시작
echo "🚀 서비스 시작 중..."
docker-compose up -d

# 헬스 체크 대기
echo "⏳ 서비스 시작 대기 중 (30초)..."
sleep 30

# 서비스 상태 확인
echo ""
echo "📊 서비스 상태:"
docker-compose ps

# 로그 확인
echo ""
echo "📋 최근 로그 (마지막 50줄):"
docker-compose logs --tail=50 workfolio-service || echo "⚠️  로그를 가져올 수 없습니다"

echo ""
echo "✅ 배포 완료!"
echo ""
echo "다음 명령어로 로그 확인:"
echo "  docker-compose logs -f workfolio-service"
echo ""
echo "서비스 상태 확인:"
echo "  docker-compose ps"
echo ""
echo "리소스 사용량 확인:"
echo "  docker stats"


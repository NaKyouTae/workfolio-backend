#!/bin/bash
# EC2로 JAR 파일 업로드 스크립트
# 사용법: ./ec2-upload-jar.sh [JAR_FILE_PATH] [EC2_HOST] [KEY_FILE]

set -e

# 기본값 설정
JAR_FILE="${1:-build/libs/workfolio-server.jar}"
EC2_HOST="${2:-ec2-user@ec2-3-27-94-86.ap-southeast-2.compute.amazonaws.com}"
KEY_FILE="${3:-workfolio-server.pem}"
REMOTE_DIR="~/workfolio-backend/build/libs"

# JAR 파일 경로 확인
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ 오류: JAR 파일을 찾을 수 없습니다: $JAR_FILE"
    echo ""
    echo "사용법: $0 [JAR_FILE_PATH] [EC2_HOST] [KEY_FILE]"
    echo ""
    echo "예시:"
    echo "  $0 build/libs/workfolio-server.jar"
    echo "  $0 build/libs/workfolio-server.jar ec2-user@your-ec2-ip workfolio-server.pem"
    exit 1
fi

# 키 파일 확인
if [ ! -f "$KEY_FILE" ]; then
    echo "❌ 오류: 키 파일을 찾을 수 없습니다: $KEY_FILE"
    exit 1
fi

echo "📦 JAR 파일 업로드 시작..."
echo "   파일: $JAR_FILE"
echo "   대상: $EC2_HOST:$REMOTE_DIR"
echo ""

# 원격 디렉토리 생성
echo "📁 원격 디렉토리 생성 중..."
ssh -i "$KEY_FILE" "$EC2_HOST" "mkdir -p ~/workfolio-backend/build/libs" || {
    echo "❌ 원격 디렉토리 생성 실패"
    exit 1
}

# 파일 전송
echo "📤 파일 전송 중..."
scp -i "$KEY_FILE" "$JAR_FILE" "$EC2_HOST:$REMOTE_DIR/" || {
    echo "❌ 파일 전송 실패"
    exit 1
}

echo ""
echo "✅ 업로드 완료!"
echo ""
echo "다음 단계:"
echo "  1. EC2에 SSH 접속: ssh -i $KEY_FILE $EC2_HOST"
echo "  2. 파일 확인: ls -lh ~/workfolio-backend/build/libs/"
echo "  3. Docker 이미지 빌드: docker build -t workfolio-server:latest -f Dockerfile ."


#!/bin/bash
# EC2 t3.micro 초기 설정 스크립트
# 사용법: ./ec2-install.sh

set -e

echo "🚀 EC2 초기 설정 시작..."

# 1. 시스템 업데이트
echo "📦 시스템 업데이트 중..."
sudo yum update -y

# 2. 필수 패키지 설치
echo "📦 필수 패키지 설치 중..."
sudo yum install -y git docker

# 3. Docker 서비스 시작 및 자동 시작 설정
echo "🐳 Docker 설정 중..."
sudo systemctl start docker
sudo systemctl enable docker

# 4. 현재 사용자를 docker 그룹에 추가
echo "👤 Docker 그룹 설정 중..."
sudo usermod -aG docker $USER

# 5. Docker Compose 설치
echo "🐳 Docker Compose 설치 중..."
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 6. Docker Compose 심볼릭 링크 생성 (선택사항)
if [ ! -f /usr/bin/docker-compose ]; then
    sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
fi

# 7. 설치 확인
echo ""
echo "✅ 설치 완료!"
echo ""
echo "설치된 버전:"
echo "Git: $(git --version)"
echo "Docker: $(docker --version)"
echo "Docker Compose: $(docker-compose --version)"
echo ""
echo "⚠️  중요: Docker 그룹 변경사항을 적용하려면 다음 중 하나를 실행하세요:"
echo "   1. 새 터미널 세션 시작"
echo "   2. 'newgrp docker' 명령 실행"
echo "   3. 로그아웃 후 다시 로그인"
echo ""
echo "다음 단계:"
echo "   1. 'newgrp docker' 실행"
echo "   2. 프로젝트 클론: git clone <your-repo-url>"
echo "   3. 프로젝트 디렉토리로 이동: cd workfolio-backend"
echo "   4. 환경 변수 설정: nano docker-compose.env"
echo "   5. Docker 이미지 빌드 및 실행"


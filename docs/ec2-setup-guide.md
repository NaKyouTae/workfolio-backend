# EC2 t3.micro 설치 및 배포 가이드

## 1. EC2 인스턴스 준비

### 1.1 EC2 인스턴스 생성
- 인스턴스 타입: t3.micro
- AMI: Amazon Linux 2023
- 스토리지: 8GB (무료 티어는 30GB까지)
- 보안 그룹: SSH(22), HTTP(80), HTTPS(443), 애플리케이션 포트(8080) 열기

### 1.2 SSH 접속
```bash
ssh -i your-key.pem ec2-user@your-ec2-ip
```

## 2. 시스템 업데이트 및 필수 패키지 설치

```bash
# 시스템 업데이트
sudo yum update -y

# 필수 패키지 설치
sudo yum install -y git docker
```

## 3. Docker 설치 및 시작

```bash
# Docker 설치 확인 (Amazon Linux 2023에는 이미 설치되어 있을 수 있음)
docker --version

# Docker가 없다면 설치
sudo yum install -y docker

# Docker 서비스 시작
sudo systemctl start docker
sudo systemctl enable docker

# 현재 사용자를 docker 그룹에 추가 (sudo 없이 docker 사용)
sudo usermod -aG docker ec2-user

# 그룹 변경사항 적용 (새 세션 필요)
newgrp docker

# Docker 확인
docker ps
```

## 4. Git 설정

```bash
# Git 확인
git --version

# Git 설정 (선택사항)
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
```

## 5. 프로젝트 클론 및 빌드

### 5.1 프로젝트 클론
```bash
# 홈 디렉토리로 이동
cd ~

# 프로젝트 클론
git clone https://github.com/your-username/workfolio-backend.git
cd workfolio-backend
```

### 5.2 로컬에서 빌드 후 배포 (권장)
로컬에서 JAR 파일을 빌드한 후 EC2로 전송하는 방법:

**로컬에서:**
```bash
# 프로젝트 빌드
./gradlew clean build -x test

# JAR 파일 확인
ls -lh projects/api/build/libs/workfolio-server-boot.jar

# EC2로 전송
scp -i your-key.pem projects/api/build/libs/workfolio-server-boot.jar ec2-user@your-ec2-ip:~/workfolio-backend/projects/api/build/libs/
```

### 5.3 EC2에서 직접 빌드 (대안)
EC2에서 직접 빌드하려면 Java와 Gradle이 필요합니다:

```bash
# Java 21 설치
sudo yum install -y java-21-amazon-corretto-devel

# Gradle 설치
sudo yum install -y gradle

# 또는 SDKMAN 사용
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install gradle

# 프로젝트 빌드
./gradlew clean build -x test
```

## 6. Docker 이미지 빌드

```bash
# 프로젝트 디렉토리로 이동
cd ~/workfolio-backend

# Docker 이미지 빌드
docker build -t workfolio-server:latest -f Dockerfile .

# 이미지 확인
docker images
```

## 7. 환경 변수 설정

```bash
# 환경 변수 파일 생성
nano ~/workfolio-backend/docker-compose.env

# 필요한 환경 변수 설정 (예시)
# DB_HOST=your-db-host
# DB_PORT=5432
# DB_NAME=workfolio
# DB_USER=your-user
# DB_PASSWORD=your-password
# REDIS_HOST=your-redis-host
# REDIS_PORT=6379
# SUPABASE_ACCESS_KEY=your-key
```

## 8. Docker Compose로 서비스 실행

### 8.1 외부 DB/Redis 사용 시
외부 데이터베이스(예: RDS, ElastiCache)를 사용하는 경우:

```bash
# docker-compose.yml 수정 필요
# workfolio-service만 실행하도록 수정

# Docker Compose 실행
docker-compose up -d workfolio-service
```

### 8.2 로컬 DB/Redis 사용 시
EC2에서 PostgreSQL과 Redis도 함께 실행:

```bash
# 모든 서비스 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 서비스 상태 확인
docker-compose ps
```

## 9. 메모리 최적화 (t3.micro 1GB 메모리)

### 9.1 JVM 힙 메모리 제한
Dockerfile 또는 docker-compose.yml에서 JVM 옵션 설정:

```yaml
# docker-compose.yml에 추가
environment:
  - JAVA_OPTIONS=-Xmx512m -Xms256m
```

또는 Dockerfile 수정:
```dockerfile
ENV JAVA_OPTIONS="-Xmx512m -Xms256m -Djava.security.egd=file:/dev/./urandom"
```

### 9.2 Docker 메모리 제한
```bash
# docker-compose.yml에 추가
services:
  workfolio-service:
    mem_limit: 768m
    mem_reservation: 512m
```

### 9.3 시스템 스왑 활성화 (선택사항)
```bash
# 스왑 파일 생성 (1GB)
sudo dd if=/dev/zero of=/swapfile bs=1M count=1024
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile

# 영구적으로 활성화
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

## 10. 서비스 자동 시작 설정

### 10.1 systemd 서비스 생성
```bash
# 서비스 파일 생성
sudo nano /etc/systemd/system/workfolio.service
```

```ini
[Unit]
Description=Workfolio Backend Service
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/home/ec2-user/workfolio-backend
ExecStart=/usr/bin/docker-compose up -d
ExecStop=/usr/bin/docker-compose down
User=ec2-user
Group=ec2-user

[Install]
WantedBy=multi-user.target
```

```bash
# 서비스 활성화
sudo systemctl daemon-reload
sudo systemctl enable workfolio
sudo systemctl start workfolio

# 상태 확인
sudo systemctl status workfolio
```

## 11. 모니터링 및 로그

```bash
# 컨테이너 로그 확인
docker-compose logs -f workfolio-service

# 특정 컨테이너 로그
docker logs -f workfolio-server

# 리소스 사용량 확인
docker stats

# 디스크 사용량 확인
df -h
docker system df
```

## 12. 배포 스크립트 예시

```bash
# deploy.sh 생성
nano ~/workfolio-backend/deploy.sh
```

```bash
#!/bin/bash
set -e

echo "🚀 배포 시작..."

# 최신 코드 가져오기
git pull origin main

# 기존 컨테이너 중지 및 제거
docker-compose down

# 새 이미지 빌드
docker build -t workfolio-server:latest -f Dockerfile .

# 서비스 재시작
docker-compose up -d

# 로그 확인
docker-compose logs -f workfolio-service

echo "✅ 배포 완료"
```

```bash
# 실행 권한 부여
chmod +x ~/workfolio-backend/deploy.sh
```

## 13. 용량 관리

### 13.1 사용하지 않는 Docker 리소스 정리
```bash
# 사용하지 않는 이미지, 컨테이너, 볼륨 정리
docker system prune -a --volumes

# 또는 선택적 정리
docker image prune -a
docker container prune
docker volume prune
```

### 13.2 디스크 사용량 모니터링
```bash
# 디스크 사용량 확인
df -h

# 큰 파일 찾기
du -sh /* | sort -h

# Docker 사용량 확인
docker system df
```

## 14. 문제 해결

### 14.1 메모리 부족 시
```bash
# 메모리 사용량 확인
free -h
docker stats

# JVM 힙 메모리 줄이기
# JAVA_OPTIONS에 -Xmx256m 추가
```

### 14.2 디스크 부족 시
```bash
# Docker 정리
docker system prune -a --volumes

# 로그 파일 정리
sudo journalctl --vacuum-time=3d

# 오래된 로그 삭제
find /var/log -type f -name "*.log" -mtime +7 -delete
```

### 14.3 컨테이너가 시작되지 않을 때
```bash
# 로그 확인
docker-compose logs workfolio-service

# 컨테이너 상태 확인
docker-compose ps

# 컨테이너 재시작
docker-compose restart workfolio-service
```

## 15. 보안 설정

### 15.1 방화벽 설정
```bash
# 필요한 포트만 열기
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload
```

### 15.2 Docker 보안
```bash
# Docker 데몬이 root로 실행되는지 확인
# ec2-user를 docker 그룹에 추가했는지 확인
groups
```

## 참고사항

- **메모리**: t3.micro는 1GB 메모리만 있으므로 JVM 힙을 512MB 이하로 제한 권장
- **스토리지**: 기본 8GB면 충분하지만, 로그가 많아지면 모니터링 필요
- **성능**: t3.micro는 버스트 가능한 인스턴스이므로 CPU 크레딧 관리 필요
- **데이터베이스**: 프로덕션 환경에서는 RDS 사용 권장
- **Redis**: 프로덕션 환경에서는 ElastiCache 사용 권장


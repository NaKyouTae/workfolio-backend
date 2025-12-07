# EC2 t3.micro 빠른 시작 가이드

## ⚡ 빠른 설치 (5분)

### 1. EC2 인스턴스 접속
```bash
ssh -i your-key.pem ec2-user@your-ec2-ip
```

### 2. 초기 설정 스크립트 실행
```bash
# 프로젝트 클론 (또는 직접 업로드)
git clone <your-repo-url>
cd workfolio-backend

# 설치 스크립트 실행
chmod +x scripts/ec2-install.sh
./scripts/ec2-install.sh

# Docker 그룹 적용
newgrp docker
```

### 3. 환경 변수 설정
```bash
nano docker-compose.env
# 필요한 환경 변수 입력 (DB, Redis, Supabase 등)
```

### 4. 배포
```bash
# 로컬에서 JAR 빌드 후 전송 (권장)
./gradlew clean build -x test
scp -i your-key.pem projects/api/build/libs/workfolio-server-boot.jar ec2-user@your-ec2-ip:~/workfolio-backend/projects/api/build/libs/

# EC2에서
chmod +x scripts/ec2-deploy.sh
./scripts/ec2-deploy.sh --build
```

## 📊 용량 확인

### t3.micro 용량 분석
- **스토리지**: 8GB (충분함 ✅)
- **메모리**: 1GB (JVM 힙 512MB로 제한 필요 ⚠️)

### 예상 사용량
```
시스템:        ~2GB
Docker:        ~500MB
애플리케이션:  ~1GB
여유 공간:     ~4.5GB
```

## 🔧 메모리 최적화 필수 설정

### JVM 힙 메모리 제한
`docker-compose.ec2.yml`에서 이미 설정됨:
```yaml
environment:
  - JAVA_OPTIONS=-Xmx512m -Xms256m
```

### Docker 메모리 제한
```yaml
mem_limit: 768m
mem_reservation: 512m
```

## 🚀 배포 명령어

### 기본 배포
```bash
./scripts/ec2-deploy.sh
```

### 빌드 포함 배포
```bash
./scripts/ec2-deploy.sh --build
```

### 최신 코드 가져오기 + 배포
```bash
./scripts/ec2-deploy.sh --pull --build
```

### 수동 배포
```bash
docker-compose -f docker-compose.ec2.yml down
docker build -t workfolio-server:latest -f Dockerfile .
docker-compose -f docker-compose.ec2.yml up -d
```

## 📋 모니터링

### 로그 확인
```bash
docker-compose -f docker-compose.ec2.yml logs -f workfolio-service
```

### 리소스 사용량
```bash
docker stats
df -h
free -h
```

### 서비스 상태
```bash
docker-compose -f docker-compose.ec2.yml ps
curl http://localhost:8080/actuator/health
```

## 🧹 용량 관리

### Docker 정리
```bash
# 사용하지 않는 리소스 정리
docker system prune -a --volumes

# 이미지만 정리
docker image prune -a
```

### 디스크 사용량 확인
```bash
df -h
docker system df
```

## ⚠️ 주의사항

1. **메모리**: t3.micro는 1GB만 있으므로 JVM 힙을 512MB 이하로 제한
2. **데이터베이스**: 프로덕션은 RDS 사용 권장
3. **Redis**: 프로덕션은 ElastiCache 사용 권장
4. **스토리지**: 로그가 많아지면 정기적으로 정리 필요

## 🔗 관련 문서

- 상세 가이드: [ec2-setup-guide.md](./ec2-setup-guide.md)
- 설치 스크립트: `scripts/ec2-install.sh`
- 배포 스크립트: `scripts/ec2-deploy.sh`
- EC2용 compose: `docker-compose.ec2.yml`


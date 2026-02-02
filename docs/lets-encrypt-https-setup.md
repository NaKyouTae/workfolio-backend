# Let's Encrypt HTTPS 설정 가이드 (api.workfolio.kr)

EC2에서 Nginx와 Certbot을 사용하여 Let's Encrypt 인증서로 HTTPS를 설정하는 방법입니다.

## 사전 준비

1. **도메인 DNS 설정**
   - `api.workfolio.kr`의 A 레코드가 EC2 인스턴스의 퍼블릭 IP를 가리키도록 설정
   - 호스팅케이알에서 서브도메인 설정 방법은 아래 참고

### 호스팅케이알에서 서브도메인 설정 방법

1. **호스팅케이알 관리 페이지 접속**
   - 도메인 관리 페이지에서 `workfolio.kr` 선택
   - "네임서버/DNS" 탭 클릭

2. **DNS 레코드 추가**
   - "DNS 레코드 관리" 섹션에서 "+ 새 레코드 추가" 버튼 클릭
   - 다음 정보 입력:
     - **유형**: `A` 선택
     - **호스트 이름**: `api` 입력 (api.workfolio.kr을 만들기 위해)
     - **값**: EC2 인스턴스의 퍼블릭 IP 주소 입력 (예: `3.27.94.86`)
     - **TTL**: `180` 또는 기본값 사용
   - 저장

3. **확인**
   - DNS 레코드가 추가되었는지 확인
   - DNS 전파는 보통 몇 분에서 몇 시간 소요될 수 있습니다
   - 확인 명령어:
     ```bash
     nslookup api.workfolio.kr
     dig api.workfolio.kr
     ```

**참고:**
- 호스트 이름에 `api`만 입력하면 `api.workfolio.kr`이 생성됩니다
- `@`는 루트 도메인(`workfolio.kr`)을 의미합니다
- 서브도메인은 여러 개 추가할 수 있습니다 (예: `api`, `www`, `admin` 등)

2. **보안 그룹 설정**
   - HTTP(80) 포트 열기 (인증서 발급용)
   - HTTPS(443) 포트 열기

## 1. Nginx 설치

```bash
# Amazon Linux 2023에서 Nginx 설치
sudo yum install -y nginx

# Nginx 서비스 시작 및 자동 시작 설정
sudo systemctl start nginx
sudo systemctl enable nginx

# Nginx 상태 확인
sudo systemctl status nginx
```

## 2. Certbot 설치

```bash
# Certbot 설치
sudo yum install -y certbot python3-certbot-nginx

# Certbot 버전 확인
certbot --version
```

## 3. 초기 Nginx 설정 (HTTP)

인증서 발급 전에 HTTP로 접근 가능하도록 설정합니다:

### 방법 1: 프로젝트의 설정 파일 템플릿 사용 (권장)

```bash
# 프로젝트에서 설정 파일 복사
sudo cp ~/workfolio-backend/config/nginx/workfolio-http-only.conf /etc/nginx/conf.d/workfolio.conf

# 또는 직접 편집
sudo nano /etc/nginx/conf.d/workfolio.conf
```

### 방법 2: 직접 작성

```bash
# Nginx 설정 파일 생성
sudo nano /etc/nginx/conf.d/workfolio.conf
```

다음 내용을 추가:

```nginx
server {
    listen 80;
    server_name api.workfolio.kr;

    # Let's Encrypt 인증서 발급을 위한 경로 (Certbot이 자동으로 추가)
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    # 애플리케이션 프록시
    location / {
        proxy_pass http://localhost:9000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
# Nginx 설정 테스트
sudo nginx -t

# Nginx 재시작
sudo systemctl restart nginx
```

## 4. Let's Encrypt 인증서 발급

```bash
# Certbot으로 인증서 발급 (Nginx 플러그인 사용)
sudo certbot --nginx -d api.workfolio.kr

# 또는 대화형 모드
sudo certbot certonly --nginx -d api.workfolio.kr
```

**Certbot 질문에 대한 답변:**
- Email: 이메일 주소 입력 (갱신 알림용)
- Terms of Service: Y 입력
- Share email: 선택사항
- Redirect HTTP to HTTPS: Y 입력 (권장)

## 5. Nginx SSL 설정 확인

Certbot이 자동으로 Nginx 설정을 업데이트합니다. 확인:

```bash
# 설정 파일 확인
sudo cat /etc/nginx/conf.d/workfolio.conf
```

다음과 같은 설정이 자동으로 추가됩니다:

```nginx
server {
    listen 80;
    server_name api.workfolio.kr;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name api.workfolio.kr;

    ssl_certificate /etc/letsencrypt/live/api.workfolio.kr/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.workfolio.kr/privkey.pem;
    
    # SSL 보안 설정 (Certbot이 자동으로 추가)
    include /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;

    # 애플리케이션 프록시
    location / {
        proxy_pass http://localhost:9000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port $server_port;
        
        # WebSocket 지원
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        
        # 타임아웃 설정
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # Health check
    location /actuator/health {
        proxy_pass http://localhost:9000/actuator/health;
        proxy_set_header Host $host;
        access_log off;
    }
}
```

## 6. Nginx 재시작 및 테스트

```bash
# Nginx 설정 테스트
sudo nginx -t

# Nginx 재시작
sudo systemctl restart nginx

# HTTPS 접속 테스트
curl -I https://api.workfolio.kr
```

## 7. 자동 갱신 설정

Let's Encrypt 인증서는 90일마다 갱신이 필요합니다. 자동 갱신을 설정합니다:

```bash
# Certbot 자동 갱신 테스트
sudo certbot renew --dry-run
```

### 7.1 자동 갱신 설정 확인

```bash
# systemd timer 확인 (Amazon Linux 2023)
sudo systemctl list-timers --all | grep certbot

# 또는 모든 timer 확인
sudo systemctl list-timers --all

# certbot.timer 서비스 확인
sudo systemctl status certbot.timer
```

### 7.2 자동 갱신이 없는 경우 수동 설정

자동 갱신이 설정되지 않았다면 다음 중 하나를 사용하세요:

#### 방법 1: systemd timer 사용 (권장, Amazon Linux 2023)

```bash
# certbot.timer 서비스 파일 생성
sudo nano /etc/systemd/system/certbot.timer
```

다음 내용 추가:
```ini
[Unit]
Description=Certbot Renewal Timer

[Timer]
OnCalendar=daily
RandomizedDelaySec=3600
Persistent=true

[Install]
WantedBy=timers.target
```

```bash
# certbot.service 파일 생성
sudo nano /etc/systemd/system/certbot.service
```

다음 내용 추가:
```ini
[Unit]
Description=Certbot Renewal
After=network-online.target
Wants=network-online.target

[Service]
Type=oneshot
ExecStart=/usr/bin/certbot renew --quiet --deploy-hook "systemctl reload nginx"
```

```bash
# timer 활성화
sudo systemctl daemon-reload
sudo systemctl enable certbot.timer
sudo systemctl start certbot.timer

# 상태 확인
sudo systemctl status certbot.timer
sudo systemctl list-timers | grep certbot
```

#### 방법 2: cron 사용 (전통적인 방법)

```bash
# cron 설치 (없다면)
sudo yum install -y cronie

# cron 서비스 시작
sudo systemctl start crond
sudo systemctl enable crond

# root crontab 편집
sudo crontab -e
```

다음 줄 추가:
```
0 3 * * * /usr/bin/certbot renew --quiet --deploy-hook "systemctl reload nginx"
```

또는 더 안전한 방법:
```
0 3 * * * /usr/bin/certbot renew --quiet --deploy-hook "/bin/systemctl reload nginx" >> /var/log/certbot-renew.log 2>&1
```

```bash
# cron 작업 확인
sudo crontab -l
```

### 7.3 자동 갱신 확인

```bash
# systemd timer 사용 시
sudo systemctl list-timers | grep certbot

# cron 사용 시
sudo crontab -l

# 다음 실행 시간 확인 (systemd timer)
sudo systemctl list-timers certbot.timer
```

## 8. 방화벽 설정

```bash
# firewalld 사용 시
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload

# 또는 직접 포트 열기
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --add-port=443/tcp
sudo firewall-cmd --reload
```

## 9. Docker 컨테이너와 연동

Docker 컨테이너는 localhost:9000에서 실행되므로, Nginx가 프록시로 동작합니다.

```bash
# Docker 컨테이너 확인
docker ps

# 컨테이너가 실행 중인지 확인
curl http://localhost:9000/actuator/health
```

## 10. SSL 보안 강화 (선택사항)

더 강력한 SSL 설정을 원하면 프로젝트의 설정 파일 템플릿을 사용하거나 수동으로 추가:

### 방법 1: 프로젝트의 설정 파일 템플릿 사용

```bash
# 인증서 발급 후, 프로젝트의 HTTPS 설정 파일로 교체
sudo cp ~/workfolio-backend/config/nginx/workfolio.conf /etc/nginx/conf.d/workfolio.conf

# Certbot이 추가한 SSL 인증서 경로는 그대로 유지
# 설정 파일을 확인하고 필요시 수정
sudo nano /etc/nginx/conf.d/workfolio.conf
```

### 방법 2: 수동으로 추가

`/etc/nginx/conf.d/workfolio.conf`에 다음 내용 추가:

```nginx
server {
    listen 443 ssl http2;
    server_name api.workfolio.kr;

    # ... 기존 설정 ...

    # 추가 보안 설정
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_prefer_server_ciphers on;
    ssl_ciphers 'ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384';
    
    # HSTS (HTTP Strict Transport Security)
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    
    # 보안 헤더
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
}
```

## 인증서 정보 확인

```bash
# 인증서 만료일 확인
sudo certbot certificates

# 인증서 상세 정보
openssl x509 -in /etc/letsencrypt/live/api.workfolio.kr/cert.pem -text -noout
```

## 참고사항

1. **인증서 위치**
   - 인증서: `/etc/letsencrypt/live/api.workfolio.kr/fullchain.pem`
   - 개인키: `/etc/letsencrypt/live/api.workfolio.kr/privkey.pem`

2. **갱신 주기**
   - Let's Encrypt 인증서는 90일마다 갱신 필요
   - Certbot이 자동으로 갱신하지만, 수동 확인 권장

3. **백업**
   - 인증서와 개인키는 안전하게 백업 권장
   - `/etc/letsencrypt/` 디렉토리 전체 백업

4. **모니터링**
   - 인증서 만료일 모니터링 설정 권장
   - Certbot 이메일 알림 확인


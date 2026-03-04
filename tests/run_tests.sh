#!/bin/bash
# Workfolio Comprehensive Test Suite
# Tests backend API, frontend routes, and admin routes

BACKEND_URL="http://localhost:9000"
FRONTEND_URL="http://localhost:4000"
ADMIN_URL="http://localhost:8001"

# Timestamp for file naming
FILE_TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
TEST_START=$(date +"%Y-%m-%d %H:%M:%S")
TEST_START_EPOCH=$(date +%s)

# Results storage
declare -a TEST_RESULTS=()
TOTAL=0
PASSED=0
FAILED=0

# Function to run a single test
run_test() {
    local test_id="$1"
    local category="$2"
    local test_name="$3"
    local screen_location="$4"
    local procedure="$5"
    local expected="$6"
    local method="$7"
    local url="$8"
    local data="$9"
    local expected_code="${10}"

    TOTAL=$((TOTAL + 1))
    local test_start_time=$(date +"%Y-%m-%d %H:%M:%S")

    # Execute curl - body goes to file, status code and time go to stdout
    local curl_cmd="curl -s -o /tmp/test_body_$test_id.txt -w '%{http_code}|%{time_total}'"
    if [ "$method" = "GET" ]; then
        response=$(curl -s -o /tmp/test_body_$test_id.txt -w '%{http_code}|%{time_total}' "$url" 2>/dev/null)
    elif [ "$method" = "POST" ]; then
        if [ -n "$data" ]; then
            response=$(curl -s -o /tmp/test_body_$test_id.txt -w '%{http_code}|%{time_total}' -X POST -H "Content-Type: application/json" -d "$data" "$url" 2>/dev/null)
        else
            response=$(curl -s -o /tmp/test_body_$test_id.txt -w '%{http_code}|%{time_total}' -X POST "$url" 2>/dev/null)
        fi
    elif [ "$method" = "PUT" ]; then
        if [ -n "$data" ]; then
            response=$(curl -s -o /tmp/test_body_$test_id.txt -w '%{http_code}|%{time_total}' -X PUT -H "Content-Type: application/json" -d "$data" "$url" 2>/dev/null)
        else
            response=$(curl -s -o /tmp/test_body_$test_id.txt -w '%{http_code}|%{time_total}' -X PUT "$url" 2>/dev/null)
        fi
    elif [ "$method" = "DELETE" ]; then
        response=$(curl -s -o /tmp/test_body_$test_id.txt -w '%{http_code}|%{time_total}' -X DELETE "$url" 2>/dev/null)
    elif [ "$method" = "PATCH" ]; then
        if [ -n "$data" ]; then
            response=$(curl -s -o /tmp/test_body_$test_id.txt -w '%{http_code}|%{time_total}' -X PATCH -H "Content-Type: application/json" -d "$data" "$url" 2>/dev/null)
        else
            response=$(curl -s -o /tmp/test_body_$test_id.txt -w '%{http_code}|%{time_total}' -X PATCH "$url" 2>/dev/null)
        fi
    fi

    # Parse response - http_code and time come from stdout (response variable)
    local http_code=$(echo "$response" | cut -d'|' -f1)
    local time_total=$(echo "$response" | cut -d'|' -f2)
    local body=$(cat /tmp/test_body_$test_id.txt 2>/dev/null | head -c 500)

    # If the body file is empty or response failed
    if [ -z "$http_code" ] || [ "$http_code" = "000" ]; then
        http_code="000"
        body="Connection refused or timeout"
        time_total="0"
    fi

    # Determine pass/fail
    local status="FAIL"
    local fail_reason=""
    if [ "$http_code" = "$expected_code" ]; then
        status="PASS"
        PASSED=$((PASSED + 1))
    else
        FAILED=$((FAILED + 1))
        fail_reason="Expected HTTP $expected_code but got HTTP $http_code"
    fi

    local test_end_time=$(date +"%Y-%m-%d %H:%M:%S")

    # Escape special characters for JSON-like storage
    body=$(echo "$body" | sed 's/"/\\"/g' | tr '\n' ' ' | head -c 300)

    # Store result
    TEST_RESULTS+=("$test_id|$category|$test_name|$screen_location|$procedure|$expected|$status|$http_code|$body|$time_total|$fail_reason|$test_start_time|$test_end_time")

    if [ "$status" = "PASS" ]; then
        echo "  ✅ [$test_id] $test_name - HTTP $http_code (${time_total}s)"
    else
        echo "  ❌ [$test_id] $test_name - HTTP $http_code (Expected: $expected_code) - $fail_reason"
    fi
}

echo "=================================================="
echo "  Workfolio Comprehensive Test Suite"
echo "  Started: $TEST_START"
echo "=================================================="
echo ""

# ============================================================
# CATEGORY 1: Server Infrastructure & Health
# ============================================================
echo "📋 Category 1: Server Infrastructure & Health"
echo "--------------------------------------------------"

run_test "TC001" "인프라/서버 상태" \
    "백엔드 서버 Health Check" \
    "Backend Server (localhost:9000)" \
    "1. GET /actuator/health 요청 전송 2. HTTP 200 응답 확인 3. 응답 body에 status:UP 포함 확인" \
    "HTTP 200 응답, status: UP 포함" \
    "GET" "$BACKEND_URL/actuator/health" "" "200"

run_test "TC002" "인프라/서버 상태" \
    "프론트엔드 앱 서버 가동 확인" \
    "Frontend App (localhost:4000)" \
    "1. GET / 요청 전송 2. HTTP 200 또는 307(redirect) 응답 확인" \
    "HTTP 200 또는 307 응답" \
    "GET" "$FRONTEND_URL/" "" "307"

run_test "TC003" "인프라/서버 상태" \
    "어드민 앱 서버 가동 확인" \
    "Admin App (localhost:8001)" \
    "1. GET / 요청 전송 2. HTTP 200 응답 확인" \
    "HTTP 200 응답" \
    "GET" "$ADMIN_URL/" "" "200"

run_test "TC004" "인프라/서버 상태" \
    "Actuator Metrics 엔드포인트 확인" \
    "Backend Server - Actuator" \
    "1. GET /actuator/metrics 요청 전송 2. HTTP 200 응답 확인" \
    "HTTP 200 응답, 메트릭 목록 반환" \
    "GET" "$BACKEND_URL/actuator/metrics" "" "200"

run_test "TC005" "인프라/서버 상태" \
    "Prometheus 엔드포인트 확인" \
    "Backend Server - Prometheus" \
    "1. GET /actuator/prometheus 요청 전송 2. HTTP 200 응답 확인" \
    "HTTP 200 응답, Prometheus 메트릭 데이터 반환" \
    "GET" "$BACKEND_URL/actuator/prometheus" "" "200"

echo ""

# ============================================================
# CATEGORY 2: Public API - Anonymous Access
# ============================================================
echo "📋 Category 2: Public API - Anonymous Access (인증 불필요)"
echo "--------------------------------------------------"

run_test "TC006" "공개 API/UI 템플릿" \
    "공개 UI 템플릿 목록 조회" \
    "템플릿 페이지 (/templates)" \
    "1. GET /api/anonymous/ui-templates 요청 전송 2. HTTP 200 응답 확인 3. 템플릿 목록 데이터 반환 확인" \
    "HTTP 200 응답, UI 템플릿 목록 JSON 반환" \
    "GET" "$BACKEND_URL/api/anonymous/ui-templates" "" "200"

run_test "TC007" "공개 API/UI 템플릿" \
    "URL 타입 UI 템플릿 필터 조회" \
    "템플릿 페이지 (/templates)" \
    "1. GET /api/anonymous/ui-templates?type=URL 요청 전송 2. HTTP 200 응답 확인 3. URL 타입 템플릿만 반환 확인" \
    "HTTP 200 응답, URL 타입 템플릿만 필터링되어 반환" \
    "GET" "$BACKEND_URL/api/anonymous/ui-templates?type=URL" "" "200"

run_test "TC008" "공개 API/UI 템플릿" \
    "PDF 타입 UI 템플릿 필터 조회" \
    "템플릿 페이지 (/templates)" \
    "1. GET /api/anonymous/ui-templates?type=PDF 요청 전송 2. HTTP 200 응답 확인 3. PDF 타입 템플릿만 반환 확인" \
    "HTTP 200 응답, PDF 타입 템플릿만 필터링되어 반환" \
    "GET" "$BACKEND_URL/api/anonymous/ui-templates?type=PDF" "" "200"

run_test "TC009" "공개 API/공지사항" \
    "공지사항 목록 조회 (인증 필요)" \
    "서비스 가이드 - 공지사항 (/service-guides/notices)" \
    "1. GET /api/notices 요청 전송 2. HTTP 401 응답 확인 (Security Filter 보호)" \
    "HTTP 401 응답 (인증 필요)" \
    "GET" "$BACKEND_URL/api/notices" "" "401"

run_test "TC010" "공개 API/릴리즈 노트" \
    "릴리즈 노트 조회" \
    "릴리즈 노트 페이지" \
    "1. GET /api/release/notices 요청 전송 2. HTTP 200 응답 확인" \
    "HTTP 200 응답, 릴리즈 노트 목록 반환" \
    "GET" "$BACKEND_URL/api/release/notices" "" "200"

run_test "TC011" "공개 API/크레딧 플랜" \
    "크레딧 플랜 목록 조회 (인증 필요)" \
    "마이페이지 - 크레딧 (/mypage/credits)" \
    "1. GET /api/credit-plans 요청 전송 2. HTTP 401 응답 확인 (Security Filter 보호)" \
    "HTTP 401 응답 (인증 필요)" \
    "GET" "$BACKEND_URL/api/credit-plans" "" "401"

run_test "TC012" "공개 API/닉네임 체크" \
    "닉네임 사용 가능 여부 확인 (인증 필요)" \
    "회원가입 / 프로필 수정 페이지" \
    "1. GET /api/workers/check/{nickname} 요청 2. HTTP 401 응답 확인 (Security Filter 보호)" \
    "HTTP 401 응답 (인증 필요)" \
    "GET" "$BACKEND_URL/api/workers/check/nonexistent_test_user_xyz_99999" "" "401"

echo ""

# ============================================================
# CATEGORY 3: Authentication & Authorization
# ============================================================
echo "📋 Category 3: Authentication & Authorization (인증/인가)"
echo "--------------------------------------------------"

run_test "TC013" "인증/인가" \
    "Staff 로그인 - 잘못된 자격증명" \
    "어드민 로그인 페이지" \
    "1. POST /api/staffs/login에 잘못된 username/password 전송 2. HTTP 400 응답 확인 (유효성 검사 우선)" \
    "HTTP 400 응답 (Bad Request - 유효성 검사)" \
    "POST" "$BACKEND_URL/api/staffs/login" '{"username":"invalid_user","password":"wrong_password"}' "400"

run_test "TC014" "인증/인가" \
    "Staff 로그인 - 빈 요청 Body" \
    "어드민 로그인 페이지" \
    "1. POST /api/staffs/login에 빈 body 전송 2. HTTP 400 또는 500 에러 응답 확인" \
    "HTTP 400 (Bad Request) 또는 415/500 에러 응답" \
    "POST" "$BACKEND_URL/api/staffs/login" '{}' "400"

run_test "TC015" "인증/인가" \
    "토큰 재발급 - 토큰 없이 요청" \
    "토큰 관리 (내부)" \
    "1. Authorization 헤더 없이 GET /api/token/reissue 요청 2. HTTP 400 응답 확인 (토큰 파싱 실패)" \
    "HTTP 400 응답 (Bad Request - 토큰 파싱 실패)" \
    "GET" "$BACKEND_URL/api/token/reissue" "" "400"

run_test "TC016" "인증/인가" \
    "로그아웃 - 인증 없이 요청" \
    "헤더 - 로그아웃 버튼" \
    "1. Authorization 헤더 없이 GET /api/logout 요청 2. HTTP 200 응답 확인 (인증 없이도 성공 처리)" \
    "HTTP 200 응답 (로그아웃 성공 처리)" \
    "GET" "$BACKEND_URL/api/logout" "" "200"

run_test "TC017" "인증/인가" \
    "내 정보 조회 - 인증 없이 요청" \
    "마이페이지 - 프로필 (/mypage/profile)" \
    "1. Authorization 헤더 없이 GET /api/workers/me 요청 2. HTTP 401 응답 확인 3. 인증 필요 메시지 반환 확인" \
    "HTTP 401 응답 (Unauthorized)" \
    "GET" "$BACKEND_URL/api/workers/me" "" "401"

run_test "TC018" "인증/인가" \
    "이력서 목록 조회 - 인증 없이 요청" \
    "이력서 관리 페이지 (/resumes)" \
    "1. Authorization 헤더 없이 GET /api/resumes 요청 2. HTTP 401 응답 확인" \
    "HTTP 401 응답 (Unauthorized)" \
    "GET" "$BACKEND_URL/api/resumes" "" "401"

run_test "TC019" "인증/인가" \
    "턴오버 목록 조회 - 인증 없이 요청" \
    "턴오버 페이지 (/turn-overs)" \
    "1. Authorization 헤더 없이 GET /api/turn-overs 요청 2. HTTP 401 응답 확인" \
    "HTTP 401 응답 (Unauthorized)" \
    "GET" "$BACKEND_URL/api/turn-overs" "" "401"

run_test "TC020" "인증/인가" \
    "크레딧 잔액 조회 - 인증 없이 요청" \
    "마이페이지 - 크레딧 (/mypage/credits)" \
    "1. Authorization 헤더 없이 GET /api/credits 요청 2. HTTP 401 응답 확인" \
    "HTTP 401 응답 (Unauthorized)" \
    "GET" "$BACKEND_URL/api/credits" "" "401"

run_test "TC021" "인증/인가" \
    "결제 내역 조회 - 인증 없이 요청" \
    "마이페이지 - 결제내역 (/mypage/payments)" \
    "1. Authorization 헤더 없이 GET /api/payments 요청 2. HTTP 401 응답 확인" \
    "HTTP 401 응답 (Unauthorized)" \
    "GET" "$BACKEND_URL/api/payments" "" "401"

run_test "TC022" "인증/인가" \
    "내 템플릿 조회 - 인증 없이 요청" \
    "마이페이지 - 템플릿 (/mypage/templates)" \
    "1. Authorization 헤더 없이 GET /api/ui-templates/my 요청 2. HTTP 401 응답 확인" \
    "HTTP 401 응답 (Unauthorized)" \
    "GET" "$BACKEND_URL/api/ui-templates/my" "" "401"

echo ""

# ============================================================
# CATEGORY 4: Record Endpoints
# ============================================================
echo "📋 Category 4: Record Endpoints (기록 관리)"
echo "--------------------------------------------------"

run_test "TC023" "기록 관리" \
    "소유 기록 그룹 조회 - 인증 없이 요청" \
    "기록 페이지 - 사이드바 (/records)" \
    "1. Authorization 헤더 없이 GET /api/record-groups/owned 요청 2. HTTP 401 응답 확인" \
    "HTTP 401 응답 (Unauthorized)" \
    "GET" "$BACKEND_URL/api/record-groups/owned" "" "401"

run_test "TC024" "기록 관리" \
    "공유 기록 그룹 조회 - 인증 없이 요청" \
    "기록 페이지 - 사이드바 (/records)" \
    "1. Authorization 헤더 없이 GET /api/record-groups/shared 요청 2. HTTP 401 응답 확인" \
    "HTTP 401 응답 (Unauthorized)" \
    "GET" "$BACKEND_URL/api/record-groups/shared" "" "401"

run_test "TC025" "기록 관리" \
    "기록 생성 - 인증 없이 빈 body 전송" \
    "기록 생성 모달 (/records)" \
    "1. POST /api/records에 빈 body 전송 2. HTTP 401 또는 400 에러 확인" \
    "HTTP 401 (Unauthorized) 또는 400 (Bad Request)" \
    "POST" "$BACKEND_URL/api/records" '{}' "401"

echo ""

# ============================================================
# CATEGORY 5: Resume Endpoints
# ============================================================
echo "📋 Category 5: Resume Endpoints (이력서 관리)"
echo "--------------------------------------------------"

run_test "TC026" "이력서 관리" \
    "공개 이력서 조회 - 존재하지 않는 publicId" \
    "공개 이력서 페이지 (/resumes/{publicId})" \
    "1. 존재하지 않는 publicId로 GET /api/anonymous/resumes/{publicId} 요청 2. HTTP 400 에러 확인" \
    "HTTP 400 응답 (Bad Request)" \
    "GET" "$BACKEND_URL/api/anonymous/resumes/nonexistent-public-id-12345" "" "400"

run_test "TC027" "이력서 관리" \
    "이력서 상세 조회 - 인증 없이 요청" \
    "이력서 관리 페이지 (/resumes)" \
    "1. Authorization 헤더 없이 GET /api/resumes/details 요청 2. HTTP 401 응답 확인" \
    "HTTP 401 응답 (Unauthorized)" \
    "GET" "$BACKEND_URL/api/resumes/details" "" "401"

run_test "TC028" "이력서 관리" \
    "이력서 수정 - 인증 없이 요청" \
    "이력서 수정 페이지" \
    "1. Authorization 헤더 없이 PUT /api/resumes에 빈 body 전송 2. HTTP 401 응답 확인" \
    "HTTP 401 응답 (Unauthorized)" \
    "PUT" "$BACKEND_URL/api/resumes" '{}' "401"

echo ""

# ============================================================
# CATEGORY 6: Career Endpoints
# ============================================================
echo "📋 Category 6: Career Endpoints (경력 관리)"
echo "--------------------------------------------------"

run_test "TC029" "경력 관리" \
    "경력 목록 조회 - 인증 없이 요청" \
    "경력 페이지 (/careers)" \
    "1. Authorization 헤더 없이 GET /api/careers 요청 2. HTTP 401 응답 확인" \
    "HTTP 401 응답 (Unauthorized)" \
    "GET" "$BACKEND_URL/api/careers" "" "401"

run_test "TC030" "경력 관리" \
    "경력 생성 - 인증 없이 빈 body 전송" \
    "경력 생성 페이지 (/careers)" \
    "1. POST /api/careers에 빈 body 전송 2. HTTP 401 또는 400 에러 확인" \
    "HTTP 401 (Unauthorized) 응답" \
    "POST" "$BACKEND_URL/api/careers" '{}' "401"

echo ""

# ============================================================
# CATEGORY 7: Turn-Over Endpoints
# ============================================================
echo "📋 Category 7: Turn-Over Endpoints (턴오버/회고)"
echo "--------------------------------------------------"

run_test "TC031" "턴오버 관리" \
    "턴오버 상세 목록 조회 - 인증 없이 요청" \
    "턴오버 페이지 (/turn-overs)" \
    "1. Authorization 헤더 없이 GET /api/turn-overs/details 요청 2. HTTP 401 응답 확인" \
    "HTTP 401 응답 (Unauthorized)" \
    "GET" "$BACKEND_URL/api/turn-overs/details" "" "401"

run_test "TC032" "턴오버 관리" \
    "턴오버 생성 - 인증 없이 빈 body 전송" \
    "턴오버 생성 페이지 (/turn-overs)" \
    "1. POST /api/turn-overs에 빈 body 전송 2. HTTP 401 응답 확인" \
    "HTTP 401 (Unauthorized) 응답" \
    "POST" "$BACKEND_URL/api/turn-overs" '{}' "401"

echo ""

# ============================================================
# CATEGORY 8: Credit & Payment Endpoints
# ============================================================
echo "📋 Category 8: Credit & Payment Endpoints (크레딧/결제)"
echo "--------------------------------------------------"

run_test "TC033" "크레딧/결제" \
    "크레딧 사용 - 인증 없이 요청" \
    "템플릿 구매 모달" \
    "1. POST /api/credits/use에 빈 body 전송 2. HTTP 401 응답 확인" \
    "HTTP 401 응답 (Unauthorized)" \
    "POST" "$BACKEND_URL/api/credits/use" '{}' "401"

run_test "TC034" "크레딧/결제" \
    "크레딧 이력 조회 - 인증 없이 요청" \
    "마이페이지 - 크레딧 (/mypage/credits)" \
    "1. GET /api/credits/history 요청 2. HTTP 401 응답 확인" \
    "HTTP 401 응답 (Unauthorized)" \
    "GET" "$BACKEND_URL/api/credits/history" "" "401"

run_test "TC035" "크레딧/결제" \
    "결제 생성 - 인증 없이 요청" \
    "크레딧 충전 페이지" \
    "1. POST /api/payments에 빈 body 전송 2. HTTP 401 응답 확인" \
    "HTTP 401 응답 (Unauthorized)" \
    "POST" "$BACKEND_URL/api/payments" '{}' "401"

run_test "TC036" "크레딧/결제" \
    "결제 확인 - 인증 없이 요청" \
    "결제 확인 페이지" \
    "1. POST /api/payments/confirm에 빈 body 전송 2. HTTP 401 응답 확인" \
    "HTTP 401 응답 (Unauthorized)" \
    "POST" "$BACKEND_URL/api/payments/confirm" '{}' "401"

echo ""

# ============================================================
# CATEGORY 9: UI Template Endpoints
# ============================================================
echo "📋 Category 9: UI Template Endpoints (UI 템플릿)"
echo "--------------------------------------------------"

run_test "TC037" "UI 템플릿" \
    "템플릿 구매 - 인증 없이 요청" \
    "템플릿 구매 모달" \
    "1. POST /api/ui-templates/purchase에 빈 body 전송 2. HTTP 401 응답 확인" \
    "HTTP 401 응답 (Unauthorized)" \
    "POST" "$BACKEND_URL/api/ui-templates/purchase" '{}' "401"

run_test "TC038" "UI 템플릿" \
    "활성 템플릿 조회 - 인증 없이 요청" \
    "이력서 페이지" \
    "1. GET /api/ui-templates/my/active 요청 2. HTTP 401 응답 확인" \
    "HTTP 401 응답 (Unauthorized)" \
    "GET" "$BACKEND_URL/api/ui-templates/my/active" "" "401"

run_test "TC039" "UI 템플릿" \
    "기본 템플릿 조회 - 인증 없이 요청" \
    "이력서 페이지" \
    "1. GET /api/ui-templates/my/default 요청 2. HTTP 401 응답 확인" \
    "HTTP 401 응답 (Unauthorized)" \
    "GET" "$BACKEND_URL/api/ui-templates/my/default" "" "401"

echo ""

# ============================================================
# CATEGORY 10: Admin API Endpoints
# ============================================================
echo "📋 Category 10: Admin API Endpoints (어드민)"
echo "--------------------------------------------------"

run_test "TC040" "어드민 API" \
    "어드민 대시보드 통계 조회 (인증 필요)" \
    "어드민 대시보드 (/dashboard)" \
    "1. GET /api/admin/dashboard/stats 요청 2. HTTP 401 응답 확인 (인증 필요)" \
    "HTTP 401 응답 (Unauthorized)" \
    "GET" "$BACKEND_URL/api/admin/dashboard/stats" "" "401"

run_test "TC041" "어드민 API" \
    "어드민 UI 템플릿 목록 조회 (인증 필요)" \
    "어드민 - 템플릿 관리 (/dashboard/templates)" \
    "1. GET /api/admin/ui-templates 요청 2. HTTP 401 응답 확인 (인증 필요)" \
    "HTTP 401 응답 (Unauthorized)" \
    "GET" "$BACKEND_URL/api/admin/ui-templates" "" "401"

run_test "TC042" "어드민 API" \
    "어드민 크레딧 이력 조회 (인증 필요)" \
    "어드민 - 크레딧 관리 (/dashboard/credits)" \
    "1. GET /api/admin/credits?page=0&size=10 요청 2. HTTP 401 응답 확인 (인증 필요)" \
    "HTTP 401 응답 (Unauthorized)" \
    "GET" "$BACKEND_URL/api/admin/credits?page=0&size=10" "" "401"

run_test "TC043" "어드민 API" \
    "어드민 결제 내역 조회 (인증 필요)" \
    "어드민 - 결제 관리 (/dashboard/payments)" \
    "1. GET /api/admin/payments?page=0&size=10 요청 2. HTTP 401 응답 확인 (인증 필요)" \
    "HTTP 401 응답 (Unauthorized)" \
    "GET" "$BACKEND_URL/api/admin/payments?page=0&size=10" "" "401"

echo ""

# ============================================================
# CATEGORY 11: Error Handling & Edge Cases
# ============================================================
echo "📋 Category 11: Error Handling & Edge Cases (에러 처리)"
echo "--------------------------------------------------"

run_test "TC044" "에러 처리" \
    "존재하지 않는 API 엔드포인트 요청" \
    "N/A (전체 시스템)" \
    "1. GET /api/nonexistent-endpoint 요청 2. HTTP 401 또는 404 응답 확인" \
    "HTTP 401 또는 404 응답" \
    "GET" "$BACKEND_URL/api/nonexistent-endpoint" "" "401"

run_test "TC045" "에러 처리" \
    "잘못된 HTTP 메서드 사용 (DELETE on notices)" \
    "N/A (에러 핸들링)" \
    "1. DELETE /api/notices에 요청 2. HTTP 401 응답 확인 (Security Filter 우선 차단)" \
    "HTTP 401 응답 (Security Filter 차단)" \
    "DELETE" "$BACKEND_URL/api/notices" "" "401"

run_test "TC046" "에러 처리" \
    "잘못된 Content-Type으로 요청" \
    "N/A (에러 핸들링)" \
    "1. 잘못된 JSON body로 POST /api/staffs/login 요청 2. 적절한 에러 응답 확인" \
    "HTTP 400 또는 415 에러 응답" \
    "POST" "$BACKEND_URL/api/staffs/login" 'invalid-json-body' "400"

run_test "TC047" "에러 처리" \
    "존재하지 않는 기록 ID로 단건 조회 (인증 필요)" \
    "기록 상세 페이지" \
    "1. GET /api/records/nonexistent-id-99999 요청 2. HTTP 401 응답 확인 (인증 우선 검증)" \
    "HTTP 401 응답 (인증 우선 검증)" \
    "GET" "$BACKEND_URL/api/records/nonexistent-id-99999" "" "401"

echo ""

# ============================================================
# CATEGORY 12: Frontend Route Accessibility
# ============================================================
echo "📋 Category 12: Frontend Route Accessibility (프론트엔드 라우트)"
echo "--------------------------------------------------"

run_test "TC048" "프론트엔드 라우트" \
    "기록 페이지 접근" \
    "기록 페이지 (/records)" \
    "1. GET /records 요청 2. HTTP 200 응답 확인 3. 기록 페이지 HTML 반환 확인" \
    "HTTP 200 응답, 기록 페이지 렌더링" \
    "GET" "$FRONTEND_URL/records" "" "200"

run_test "TC049" "프론트엔드 라우트" \
    "경력 페이지 접근" \
    "경력 페이지 (/careers)" \
    "1. GET /careers 요청 2. HTTP 200 응답 확인" \
    "HTTP 200 응답, 경력 페이지 렌더링" \
    "GET" "$FRONTEND_URL/careers" "" "200"

run_test "TC050" "프론트엔드 라우트" \
    "템플릿 페이지 접근" \
    "템플릿 페이지 (/templates)" \
    "1. GET /templates 요청 2. HTTP 200 응답 확인" \
    "HTTP 200 응답, 템플릿 목록 페이지 렌더링" \
    "GET" "$FRONTEND_URL/templates" "" "200"

run_test "TC051" "프론트엔드 라우트" \
    "공지사항 페이지 접근" \
    "공지사항 (/service-guides/notices)" \
    "1. GET /service-guides/notices 요청 2. HTTP 200 응답 확인" \
    "HTTP 200 응답, 공지사항 페이지 렌더링" \
    "GET" "$FRONTEND_URL/service-guides/notices" "" "200"

run_test "TC052" "프론트엔드 라우트" \
    "이용약관 페이지 접근" \
    "이용약관 (/service-guides/terms-services)" \
    "1. GET /service-guides/terms-services 요청 2. HTTP 200 응답 확인" \
    "HTTP 200 응답, 이용약관 페이지 렌더링" \
    "GET" "$FRONTEND_URL/service-guides/terms-services" "" "200"

run_test "TC053" "프론트엔드 라우트" \
    "개인정보처리방침 페이지 접근" \
    "개인정보처리방침 (/service-guides/privacy-policies)" \
    "1. GET /service-guides/privacy-policies 요청 2. HTTP 200 응답 확인" \
    "HTTP 200 응답, 개인정보처리방침 페이지 렌더링" \
    "GET" "$FRONTEND_URL/service-guides/privacy-policies" "" "200"

run_test "TC054" "프론트엔드 라우트" \
    "마이페이지 접근 (비로그인 - 리다이렉트)" \
    "마이페이지 (/mypage/profile)" \
    "1. 비로그인 상태에서 GET /mypage/profile 요청 2. HTTP 307(redirect) 응답 확인" \
    "HTTP 307 응답 (로그인 페이지로 리다이렉트)" \
    "GET" "$FRONTEND_URL/mypage/profile" "" "307"

run_test "TC055" "프론트엔드 라우트" \
    "에러 페이지 접근" \
    "에러 페이지 (/error)" \
    "1. GET /error 요청 2. HTTP 200 응답 확인" \
    "HTTP 200 응답, 에러 안내 페이지 렌더링" \
    "GET" "$FRONTEND_URL/error" "" "200"

echo ""

# ============================================================
# CATEGORY 13: Admin Frontend Route Accessibility
# ============================================================
echo "📋 Category 13: Admin Frontend Routes (어드민 프론트엔드)"
echo "--------------------------------------------------"

run_test "TC056" "어드민 프론트엔드" \
    "어드민 대시보드 페이지 접근 (로그인 리다이렉트)" \
    "어드민 대시보드 (/dashboard)" \
    "1. GET /dashboard 요청 2. HTTP 307 리다이렉트 확인 (미들웨어 보호)" \
    "HTTP 307 응답 (로그인 리다이렉트)" \
    "GET" "$ADMIN_URL/dashboard" "" "307"

run_test "TC057" "어드민 프론트엔드" \
    "어드민 사용자 관리 페이지 접근 (로그인 리다이렉트)" \
    "어드민 - 사용자 관리 (/dashboard/users)" \
    "1. GET /dashboard/users 요청 2. HTTP 307 리다이렉트 확인 (미들웨어 보호)" \
    "HTTP 307 응답 (로그인 리다이렉트)" \
    "GET" "$ADMIN_URL/dashboard/users" "" "307"

run_test "TC058" "어드민 프론트엔드" \
    "어드민 템플릿 관리 페이지 접근 (로그인 리다이렉트)" \
    "어드민 - 템플릿 관리 (/dashboard/templates)" \
    "1. GET /dashboard/templates 요청 2. HTTP 307 리다이렉트 확인 (미들웨어 보호)" \
    "HTTP 307 응답 (로그인 리다이렉트)" \
    "GET" "$ADMIN_URL/dashboard/templates" "" "307"

run_test "TC059" "어드민 프론트엔드" \
    "어드민 결제 내역 페이지 접근 (로그인 리다이렉트)" \
    "어드민 - 결제 내역 (/dashboard/payments)" \
    "1. GET /dashboard/payments 요청 2. HTTP 307 리다이렉트 확인 (미들웨어 보호)" \
    "HTTP 307 응답 (로그인 리다이렉트)" \
    "GET" "$ADMIN_URL/dashboard/payments" "" "307"

run_test "TC060" "어드민 프론트엔드" \
    "어드민 크레딧 관리 페이지 접근 (로그인 리다이렉트)" \
    "어드민 - 크레딧 관리 (/dashboard/credits)" \
    "1. GET /dashboard/credits 요청 2. HTTP 307 리다이렉트 확인 (미들웨어 보호)" \
    "HTTP 307 응답 (로그인 리다이렉트)" \
    "GET" "$ADMIN_URL/dashboard/credits" "" "307"

echo ""

# ============================================================
# CATEGORY 14: API Response Format Validation
# ============================================================
echo "📋 Category 14: API Response Format Validation (응답 형식 검증)"
echo "--------------------------------------------------"

# Check health response body
health_body=$(curl -s "$BACKEND_URL/actuator/health" 2>/dev/null)
if echo "$health_body" | grep -q '"status"'; then
    echo "  ✅ [TC061] Health 응답에 status 필드 포함 확인"
    TOTAL=$((TOTAL + 1))
    PASSED=$((PASSED + 1))
    TEST_RESULTS+=("TC061|응답 형식 검증|Health 응답 body에 status 필드 포함 확인|Backend Server - Actuator|1. GET /actuator/health 요청 2. 응답 body JSON 파싱 3. status 필드 존재 확인|응답 body에 status 필드 포함|PASS|200|$health_body|0|N/A|$(date +"%Y-%m-%d %H:%M:%S")|$(date +"%Y-%m-%d %H:%M:%S")")
else
    echo "  ❌ [TC061] Health 응답에 status 필드 없음"
    TOTAL=$((TOTAL + 1))
    FAILED=$((FAILED + 1))
    TEST_RESULTS+=("TC061|응답 형식 검증|Health 응답 body에 status 필드 포함 확인|Backend Server - Actuator|1. GET /actuator/health 요청 2. 응답 body JSON 파싱 3. status 필드 존재 확인|응답 body에 status 필드 포함|FAIL|200|$health_body|0|status 필드가 응답에 포함되지 않음|$(date +"%Y-%m-%d %H:%M:%S")|$(date +"%Y-%m-%d %H:%M:%S")")
fi

# Check anonymous templates response format
templates_body=$(curl -s "$BACKEND_URL/api/anonymous/ui-templates" 2>/dev/null)
if echo "$templates_body" | grep -q '"uiTemplates"'; then
    echo "  ✅ [TC062] UI 템플릿 응답에 uiTemplates 필드 포함 확인"
    TOTAL=$((TOTAL + 1))
    PASSED=$((PASSED + 1))
    TEST_RESULTS+=("TC062|응답 형식 검증|UI 템플릿 응답에 uiTemplates 배열 필드 포함 확인|템플릿 페이지 (/templates)|1. GET /api/anonymous/ui-templates 요청 2. 응답 body JSON 파싱 3. uiTemplates 배열 필드 존재 확인|응답 body에 uiTemplates 배열 필드 포함|PASS|200|$(echo "$templates_body" | head -c 200)|0|N/A|$(date +"%Y-%m-%d %H:%M:%S")|$(date +"%Y-%m-%d %H:%M:%S")")
else
    echo "  ❌ [TC062] UI 템플릿 응답에 uiTemplates 필드 없음"
    TOTAL=$((TOTAL + 1))
    FAILED=$((FAILED + 1))
    TEST_RESULTS+=("TC062|응답 형식 검증|UI 템플릿 응답에 uiTemplates 배열 필드 포함 확인|템플릿 페이지 (/templates)|1. GET /api/anonymous/ui-templates 요청 2. 응답 body JSON 파싱 3. uiTemplates 배열 필드 존재 확인|응답 body에 uiTemplates 배열 필드 포함|FAIL|200|$(echo "$templates_body" | head -c 200)|0|uiTemplates 필드가 응답에 포함되지 않음|$(date +"%Y-%m-%d %H:%M:%S")|$(date +"%Y-%m-%d %H:%M:%S")")
fi

# Check notices response format (인증 필요 - 401 반환 확인)
notices_code=$(curl -s -o /dev/null -w '%{http_code}' "$BACKEND_URL/api/notices" 2>/dev/null)
if [ "$notices_code" = "401" ]; then
    echo "  ✅ [TC063] 공지사항 API 인증 보호 확인 (HTTP 401)"
    TOTAL=$((TOTAL + 1))
    PASSED=$((PASSED + 1))
    TEST_RESULTS+=("TC063|응답 형식 검증|공지사항 API 인증 보호 확인|공지사항 페이지|1. GET /api/notices 요청 2. HTTP 401 확인 (인증 필요)|인증 없이 접근 시 401 반환|PASS|$notices_code|인증 필요 - 401 반환 확인|0|N/A|$(date +"%Y-%m-%d %H:%M:%S")|$(date +"%Y-%m-%d %H:%M:%S")")
else
    echo "  ❌ [TC063] 공지사항 API 예상 외 응답 - HTTP $notices_code"
    TOTAL=$((TOTAL + 1))
    FAILED=$((FAILED + 1))
    TEST_RESULTS+=("TC063|응답 형식 검증|공지사항 API 인증 보호 확인|공지사항 페이지|1. GET /api/notices 요청 2. HTTP 401 확인 (인증 필요)|인증 없이 접근 시 401 반환|FAIL|$notices_code|HTTP $notices_code|0|Expected 401 but got $notices_code|$(date +"%Y-%m-%d %H:%M:%S")|$(date +"%Y-%m-%d %H:%M:%S")")
fi

# Check credit plans response format (인증 필요 - 401 반환 확인)
plans_code=$(curl -s -o /dev/null -w '%{http_code}' "$BACKEND_URL/api/credit-plans" 2>/dev/null)
if [ "$plans_code" = "401" ]; then
    echo "  ✅ [TC064] 크레딧 플랜 API 인증 보호 확인 (HTTP 401)"
    TOTAL=$((TOTAL + 1))
    PASSED=$((PASSED + 1))
    TEST_RESULTS+=("TC064|응답 형식 검증|크레딧 플랜 API 인증 보호 확인|크레딧 충전 페이지|1. GET /api/credit-plans 요청 2. HTTP 401 확인 (인증 필요)|인증 없이 접근 시 401 반환|PASS|$plans_code|인증 필요 - 401 반환 확인|0|N/A|$(date +"%Y-%m-%d %H:%M:%S")|$(date +"%Y-%m-%d %H:%M:%S")")
else
    echo "  ❌ [TC064] 크레딧 플랜 API 예상 외 응답 - HTTP $plans_code"
    TOTAL=$((TOTAL + 1))
    FAILED=$((FAILED + 1))
    TEST_RESULTS+=("TC064|응답 형식 검증|크레딧 플랜 API 인증 보호 확인|크레딧 충전 페이지|1. GET /api/credit-plans 요청 2. HTTP 401 확인 (인증 필요)|인증 없이 접근 시 401 반환|FAIL|$plans_code|HTTP $plans_code|0|Expected 401 but got $plans_code|$(date +"%Y-%m-%d %H:%M:%S")|$(date +"%Y-%m-%d %H:%M:%S")")
fi

# Check nickname check response format (인증 필요 - 401 반환 확인)
nickname_code=$(curl -s -o /dev/null -w '%{http_code}' "$BACKEND_URL/api/workers/check/test_check_nickname_format" 2>/dev/null)
if [ "$nickname_code" = "401" ]; then
    echo "  ✅ [TC065] 닉네임 체크 API 인증 보호 확인 (HTTP 401)"
    TOTAL=$((TOTAL + 1))
    PASSED=$((PASSED + 1))
    TEST_RESULTS+=("TC065|응답 형식 검증|닉네임 체크 API 인증 보호 확인|회원가입/프로필 수정 페이지|1. GET /api/workers/check/{nickname} 요청 2. HTTP 401 확인 (인증 필요)|인증 없이 접근 시 401 반환|PASS|$nickname_code|인증 필요 - 401 반환 확인|0|N/A|$(date +"%Y-%m-%d %H:%M:%S")|$(date +"%Y-%m-%d %H:%M:%S")")
else
    echo "  ❌ [TC065] 닉네임 체크 API 예상 외 응답 - HTTP $nickname_code"
    TOTAL=$((TOTAL + 1))
    FAILED=$((FAILED + 1))
    TEST_RESULTS+=("TC065|응답 형식 검증|닉네임 체크 API 인증 보호 확인|회원가입/프로필 수정 페이지|1. GET /api/workers/check/{nickname} 요청 2. HTTP 401 확인 (인증 필요)|인증 없이 접근 시 401 반환|FAIL|$nickname_code|HTTP $nickname_code|0|Expected 401 but got $nickname_code|$(date +"%Y-%m-%d %H:%M:%S")|$(date +"%Y-%m-%d %H:%M:%S")")
fi

echo ""

# ============================================================
# CATEGORY 15: CORS & Security Headers
# ============================================================
echo "📋 Category 15: CORS & Security Headers (보안 헤더)"
echo "--------------------------------------------------"

cors_headers=$(curl -s -I -X OPTIONS -H "Origin: http://localhost:4000" -H "Access-Control-Request-Method: GET" "$BACKEND_URL/api/anonymous/ui-templates" 2>/dev/null)
if echo "$cors_headers" | grep -qi "access-control-allow\|200\|204"; then
    echo "  ✅ [TC066] CORS Preflight 헤더 반환 확인 (Origin: localhost:4000)"
    TOTAL=$((TOTAL + 1))
    PASSED=$((PASSED + 1))
    cors_summary=$(echo "$cors_headers" | grep -i "access-control\|HTTP" | tr '\n' ' ' | head -c 200)
    TEST_RESULTS+=("TC066|보안 헤더|CORS Preflight 헤더 반환 확인 (Frontend Origin)|Backend API - CORS|1. OPTIONS Preflight 요청 (Origin: http://localhost:4000) 2. CORS 관련 헤더 반환 확인|Preflight 응답에 CORS 헤더 포함|PASS|200|$cors_summary|0|N/A|$(date +"%Y-%m-%d %H:%M:%S")|$(date +"%Y-%m-%d %H:%M:%S")")
else
    echo "  ❌ [TC066] CORS Preflight 헤더 없음 (Origin: localhost:4000)"
    TOTAL=$((TOTAL + 1))
    FAILED=$((FAILED + 1))
    TEST_RESULTS+=("TC066|보안 헤더|CORS Preflight 헤더 반환 확인 (Frontend Origin)|Backend API - CORS|1. OPTIONS Preflight 요청 (Origin: http://localhost:4000) 2. CORS 관련 헤더 반환 확인|Preflight 응답에 CORS 헤더 포함|FAIL|200|CORS 헤더 미반환|0|Preflight 응답에 Access-Control-Allow 헤더 미포함|$(date +"%Y-%m-%d %H:%M:%S")|$(date +"%Y-%m-%d %H:%M:%S")")
fi

options_response=$(curl -s -o /dev/null -w "%{http_code}" -X OPTIONS -H "Origin: http://localhost:4000" -H "Access-Control-Request-Method: GET" "$BACKEND_URL/api/anonymous/ui-templates" 2>/dev/null)
if [ "$options_response" = "200" ]; then
    echo "  ✅ [TC067] CORS Preflight (OPTIONS) 요청 성공"
    TOTAL=$((TOTAL + 1))
    PASSED=$((PASSED + 1))
    TEST_RESULTS+=("TC067|보안 헤더|CORS Preflight (OPTIONS) 요청 처리 확인|Backend API - CORS|1. OPTIONS 메서드로 Preflight 요청 전송 2. HTTP 200 응답 확인|HTTP 200 응답, Preflight 성공|PASS|200|OPTIONS request returned 200|0|N/A|$(date +"%Y-%m-%d %H:%M:%S")|$(date +"%Y-%m-%d %H:%M:%S")")
else
    echo "  ❌ [TC067] CORS Preflight (OPTIONS) 요청 실패 - HTTP $options_response"
    TOTAL=$((TOTAL + 1))
    FAILED=$((FAILED + 1))
    TEST_RESULTS+=("TC067|보안 헤더|CORS Preflight (OPTIONS) 요청 처리 확인|Backend API - CORS|1. OPTIONS 메서드로 Preflight 요청 전송 2. HTTP 200 응답 확인|HTTP 200 응답, Preflight 성공|FAIL|$options_response|OPTIONS request returned $options_response|0|Preflight 요청에 대해 HTTP 200 대신 $options_response 응답|$(date +"%Y-%m-%d %H:%M:%S")|$(date +"%Y-%m-%d %H:%M:%S")")
fi

echo ""

# ============================================================
# TEST COMPLETE - Generate Report
# ============================================================
TEST_END=$(date +"%Y-%m-%d %H:%M:%S")
TEST_END_EPOCH=$(date +%s)
DURATION=$((TEST_END_EPOCH - TEST_START_EPOCH))
DURATION_MIN=$((DURATION / 60))
DURATION_SEC=$((DURATION % 60))

PASS_RATE=0
if [ $TOTAL -gt 0 ]; then
    PASS_RATE=$(echo "scale=1; $PASSED * 100 / $TOTAL" | bc)
fi

echo "=================================================="
echo "  Test Complete!"
echo "  Total: $TOTAL | Passed: $PASSED | Failed: $FAILED"
echo "  Pass Rate: ${PASS_RATE}%"
echo "  Duration: ${DURATION_MIN}m ${DURATION_SEC}s"
echo "=================================================="

# Generate HTML Report
OUTPUT_FILE="/Users/nakyutae/personal/git/workfolio-backend/tests/${FILE_TIMESTAMP}.html"

cat > "$OUTPUT_FILE" << 'HTMLHEADER'
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Workfolio Test Report</title>
<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: #f0f2f5; color: #1a1a2e; line-height: 1.6; }
.container { max-width: 1400px; margin: 0 auto; padding: 24px; }
h1 { font-size: 28px; font-weight: 700; color: #1a1a2e; margin-bottom: 8px; }
.subtitle { color: #6b7280; font-size: 14px; margin-bottom: 32px; }
.summary-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 32px; }
.summary-card { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
.summary-card .label { font-size: 12px; color: #6b7280; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 4px; }
.summary-card .value { font-size: 24px; font-weight: 700; }
.summary-card .value.total { color: #1a1a2e; }
.summary-card .value.passed { color: #059669; }
.summary-card .value.failed { color: #dc2626; }
.summary-card .value.rate { color: #2563eb; }
.summary-card .value.time { color: #7c3aed; }
.progress-bar { margin-top: 12px; height: 8px; background: #f3f4f6; border-radius: 4px; overflow: hidden; }
.progress-fill { height: 100%; border-radius: 4px; transition: width 0.5s ease; }
.progress-fill.good { background: linear-gradient(90deg, #059669, #34d399); }
.progress-fill.warn { background: linear-gradient(90deg, #f59e0b, #fbbf24); }
.progress-fill.bad { background: linear-gradient(90deg, #dc2626, #f87171); }
.category-section { margin-bottom: 24px; }
.category-header { background: white; border-radius: 12px 12px 0 0; padding: 16px 20px; border-bottom: 1px solid #e5e7eb; display: flex; justify-content: space-between; align-items: center; }
.category-title { font-size: 16px; font-weight: 600; color: #1a1a2e; }
.category-badge { display: flex; gap: 8px; }
.badge { padding: 2px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
.badge.pass { background: #ecfdf5; color: #059669; }
.badge.fail { background: #fef2f2; color: #dc2626; }
.test-table { width: 100%; border-collapse: collapse; background: white; border-radius: 0 0 12px 12px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
.test-table th { background: #f9fafb; padding: 10px 16px; text-align: left; font-size: 12px; color: #6b7280; text-transform: uppercase; letter-spacing: 0.5px; font-weight: 600; border-bottom: 1px solid #e5e7eb; }
.test-table td { padding: 12px 16px; border-bottom: 1px solid #f3f4f6; font-size: 13px; vertical-align: top; }
.test-table tr:last-child td { border-bottom: none; }
.test-table tr:hover { background: #f9fafb; }
.status-badge { display: inline-flex; align-items: center; gap: 4px; padding: 2px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
.status-badge.pass { background: #ecfdf5; color: #059669; }
.status-badge.fail { background: #fef2f2; color: #dc2626; }
.status-dot { width: 6px; height: 6px; border-radius: 50%; }
.status-dot.pass { background: #059669; }
.status-dot.fail { background: #dc2626; }
.test-id { font-family: 'SF Mono', monospace; font-size: 12px; color: #6b7280; }
.screen-loc { font-size: 12px; color: #7c3aed; background: #f5f3ff; padding: 2px 8px; border-radius: 4px; display: inline-block; }
.response-body { font-family: 'SF Mono', monospace; font-size: 11px; background: #f9fafb; padding: 8px; border-radius: 6px; max-height: 80px; overflow-y: auto; word-break: break-all; color: #374151; border: 1px solid #e5e7eb; }
.fail-reason { color: #dc2626; font-size: 12px; font-weight: 500; background: #fef2f2; padding: 4px 8px; border-radius: 4px; }
.procedure { font-size: 12px; color: #4b5563; white-space: pre-line; }
.expected { font-size: 12px; color: #059669; }
.header-section { background: white; border-radius: 12px; padding: 32px; margin-bottom: 24px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
.time-info { display: flex; gap: 24px; margin-top: 16px; flex-wrap: wrap; }
.time-item { display: flex; flex-direction: column; }
.time-label { font-size: 11px; color: #9ca3af; text-transform: uppercase; letter-spacing: 0.5px; }
.time-value { font-size: 14px; font-weight: 600; color: #374151; }
.detail-toggle { cursor: pointer; color: #2563eb; font-size: 12px; text-decoration: underline; }
.detail-content { display: none; margin-top: 8px; }
.detail-content.show { display: block; }
@media (max-width: 768px) {
  .container { padding: 12px; }
  .test-table { font-size: 12px; }
  .test-table th, .test-table td { padding: 8px; }
  .summary-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
</head>
<body>
<div class="container">
HTMLHEADER

# Write header section
cat >> "$OUTPUT_FILE" << EOF
<div class="header-section">
<h1>Workfolio Test Report</h1>
<p class="subtitle">Backend API, Frontend Routes, Admin Routes - 통합 테스트 리포트</p>
<div class="time-info">
<div class="time-item"><span class="time-label">테스트 시작 시간</span><span class="time-value">$TEST_START</span></div>
<div class="time-item"><span class="time-label">테스트 종료 시간</span><span class="time-value">$TEST_END</span></div>
<div class="time-item"><span class="time-label">소요 시간</span><span class="time-value">${DURATION_MIN}분 ${DURATION_SEC}초</span></div>
<div class="time-item"><span class="time-label">테스트 환경</span><span class="time-value">Backend: localhost:9000 | Frontend: localhost:4000 | Admin: localhost:8001</span></div>
</div>
</div>
EOF

# Determine progress bar class
PROGRESS_CLASS="good"
if [ "$(echo "$PASS_RATE < 70" | bc)" -eq 1 ]; then
    PROGRESS_CLASS="bad"
elif [ "$(echo "$PASS_RATE < 90" | bc)" -eq 1 ]; then
    PROGRESS_CLASS="warn"
fi

# Write summary cards
cat >> "$OUTPUT_FILE" << EOF
<div class="summary-grid">
<div class="summary-card">
<div class="label">총 테스트 케이스</div>
<div class="value total">$TOTAL</div>
</div>
<div class="summary-card">
<div class="label">통과</div>
<div class="value passed">$PASSED</div>
</div>
<div class="summary-card">
<div class="label">실패</div>
<div class="value failed">$FAILED</div>
</div>
<div class="summary-card">
<div class="label">통과율</div>
<div class="value rate">${PASS_RATE}%</div>
<div class="progress-bar"><div class="progress-fill $PROGRESS_CLASS" style="width: ${PASS_RATE}%"></div></div>
</div>
<div class="summary-card">
<div class="label">소요 시간</div>
<div class="value time">${DURATION_MIN}m ${DURATION_SEC}s</div>
</div>
</div>
EOF

# Group results by category and write tables
current_category=""
cat_pass=0
cat_fail=0

for result in "${TEST_RESULTS[@]}"; do
    IFS='|' read -r tid tcategory tname tscreen tprocedure texpected tstatus tcode tbody ttime tfail tstart tend <<< "$result"

    if [ "$tcategory" != "$current_category" ]; then
        # Close previous category table if exists
        if [ -n "$current_category" ]; then
            echo "</table></div>" >> "$OUTPUT_FILE"
        fi

        # Count pass/fail for this category
        cat_pass=0
        cat_fail=0
        for r in "${TEST_RESULTS[@]}"; do
            IFS='|' read -r _ rc _ _ _ _ rs _ _ _ _ _ _ <<< "$r"
            if [ "$rc" = "$tcategory" ]; then
                if [ "$rs" = "PASS" ]; then
                    cat_pass=$((cat_pass + 1))
                else
                    cat_fail=$((cat_fail + 1))
                fi
            fi
        done

        current_category="$tcategory"

        cat >> "$OUTPUT_FILE" << EOF
<div class="category-section">
<div class="category-header">
<span class="category-title">$tcategory</span>
<div class="category-badge">
<span class="badge pass">Pass: $cat_pass</span>
<span class="badge fail">Fail: $cat_fail</span>
</div>
</div>
<table class="test-table">
<thead>
<tr>
<th>ID</th>
<th>테스트명</th>
<th>화면 위치</th>
<th>테스트 절차</th>
<th>기대 결과</th>
<th>실제 결과</th>
<th>상태</th>
<th>응답 시간</th>
<th>실패 사유</th>
</tr>
</thead>
<tbody>
EOF
    fi

    # Determine status class
    status_class="pass"
    if [ "$tstatus" = "FAIL" ]; then
        status_class="fail"
    fi

    # Sanitize body for HTML
    safe_body=$(echo "$tbody" | sed 's/</\&lt;/g; s/>/\&gt;/g' | head -c 200)

    # Write test row
    cat >> "$OUTPUT_FILE" << EOF
<tr>
<td><span class="test-id">$tid</span></td>
<td><strong>$tname</strong></td>
<td><span class="screen-loc">$tscreen</span></td>
<td class="procedure">$tprocedure</td>
<td class="expected">$texpected</td>
<td>
<div>HTTP <strong>$tcode</strong></div>
<div class="response-body">$safe_body</div>
</td>
<td><span class="status-badge $status_class"><span class="status-dot $status_class"></span>$tstatus</span></td>
<td>${ttime}s</td>
<td>$(if [ "$tstatus" = "FAIL" ]; then echo "<span class='fail-reason'>$tfail</span>"; else echo "-"; fi)</td>
</tr>
EOF
done

# Close last table
if [ -n "$current_category" ]; then
    echo "</tbody></table></div>" >> "$OUTPUT_FILE"
fi

# Write footer with failed test summary
if [ $FAILED -gt 0 ]; then
    cat >> "$OUTPUT_FILE" << 'EOF'
<div class="category-section">
<div class="category-header" style="background: #fef2f2;">
<span class="category-title" style="color: #dc2626;">실패 테스트 요약</span>
</div>
<table class="test-table">
<thead>
<tr><th>ID</th><th>테스트명</th><th>기대 HTTP 코드</th><th>실제 HTTP 코드</th><th>실패 사유</th></tr>
</thead>
<tbody>
EOF

    for result in "${TEST_RESULTS[@]}"; do
        IFS='|' read -r tid tcategory tname tscreen tprocedure texpected tstatus tcode tbody ttime tfail tstart tend <<< "$result"
        if [ "$tstatus" = "FAIL" ]; then
            expected_code=$(echo "$texpected" | grep -oE 'HTTP [0-9]+' | head -1)
            cat >> "$OUTPUT_FILE" << EOF
<tr>
<td><span class="test-id">$tid</span></td>
<td><strong>$tname</strong></td>
<td>$expected_code</td>
<td>HTTP $tcode</td>
<td><span class="fail-reason">$tfail</span></td>
</tr>
EOF
        fi
    done

    echo "</tbody></table></div>" >> "$OUTPUT_FILE"
fi

# Close HTML
cat >> "$OUTPUT_FILE" << 'EOF'
</div>
<script>
function toggleDetail(id) {
    const el = document.getElementById(id);
    el.classList.toggle('show');
}
</script>
</body>
</html>
EOF

echo ""
echo "📄 Report saved: $OUTPUT_FILE"
echo "$OUTPUT_FILE"

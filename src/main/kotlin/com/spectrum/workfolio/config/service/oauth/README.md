# OAuth 모듈 개선 사항

## 개요
기존 `WorkfolioOAuth2UserService`의 단일 책임 원칙 위반과 확장성 부족 문제를 해결하기 위해 모듈화를 진행했습니다.

## 주요 개선 사항

### 1. 전략 패턴 적용
- `OAuthUserInfoExtractor` 인터페이스를 통한 제공자별 사용자 정보 추출 로직 분리
- 각 OAuth 제공자별 구현체: `KakaoUserInfoExtractor`, `GoogleUserInfoExtractor`, `NaverUserInfoExtractor`

### 2. 팩토리 패턴 적용
- `OAuthUserInfoExtractorFactory`를 통한 제공자별 추출기 관리
- 새로운 OAuth 제공자 추가 시 기존 코드 수정 없이 확장 가능

### 3. 단일 책임 원칙 준수
- `UserRegistrationService`: 사용자 등록 로직만 담당
- `WorkfolioOAuth2UserService`: OAuth 로그인 처리만 담당
- 각 추출기: 해당 제공자의 사용자 정보 추출만 담당

### 4. 에러 처리 개선
- 제공자별 예외 클래스 정의
- 상세한 로깅 및 에러 메시지 제공
- 안전한 null 체크 및 예외 처리

### 5. 트랜잭션 관리
- `@Transactional` 어노테이션을 통한 일관성 보장
- 사용자 등록 과정에서 오류 발생 시 롤백 처리

## 사용법

### 새로운 OAuth 제공자 추가
1. `OAuthUserInfoExtractor` 인터페이스 구현
2. `SNSType`과 `AccountType` enum에 새 제공자 추가
3. `OAuthUserInfoExtractorFactory`에 새 추출기 등록

### 예시: Apple 로그인 추가
```kotlin
@Component
class AppleUserInfoExtractor : OAuthUserInfoExtractor {
    override fun extractUserInfo(oauth2User: OAuth2User): OAuthUserInfo {
        // Apple OAuth 사용자 정보 추출 로직
    }
}
```

## 장점

1. **확장성**: 새로운 OAuth 제공자 추가가 용이
2. **유지보수성**: 각 클래스가 단일 책임을 가짐
3. **테스트 용이성**: 각 컴포넌트를 독립적으로 테스트 가능
4. **에러 처리**: 명확한 예외 처리 및 로깅
5. **코드 재사용성**: 공통 로직의 재사용 가능

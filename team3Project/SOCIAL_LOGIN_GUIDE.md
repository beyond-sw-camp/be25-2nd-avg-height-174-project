# 소셜 로그인 구현 가이드

## 📋 수정된 파일 목록

### 1. Backend (Java)
| 파일 | 수정 내용 |
|------|-----------|
| `build.gradle` | OAuth2 Client 의존성 추가 |
| `User.java` | 소셜 로그인 필드 추가 (provider, provider_id, social_linked, profile_image) |
| `UserRepository.java` | `findByProviderId()` 메서드 추가 |
| `UserService.java` | 소셜 로그인 사용자 처리 로직 추가 (비밀번호 변경/재설정/탈퇴 예외 처리) |
| `SecurityConfig.java` | OAuth2 로그인 설정 추가 |
| `CustomOAuth2User.java` | OAuth2User 구현체 (Spring Security용) |
| `OAuthAttributes.java` | Provider별 OAuth2 속성 매핑 클래스 |
| `CustomOAuth2UserService.java` | 소셜 로그인 사용자 처리 서비스 |
| `OAuth2LoginSuccessHandler.java` | OAuth2 로그인 성공 시 JWT 발급 핸들러 |
| `OAuth2LoginFailureHandler.java` | OAuth2 로그인 실패 핸들러 |

### 2. Frontend (HTML)
| 파일 | 수정 내용 |
|------|-----------|
| `login.html` | 소셜 로그인 버튼 추가 (카카오, 구글, 네이버) |

### 3. Configuration
| 파일 | 수정 내용 |
|------|-----------|
| `application.properties` | OAuth2 Provider 설정 (Google, Kakao, Naver) |

### 4. Database
| 파일 | 수정 내용 |
|------|-----------|
| `V2__add_social_login.sql` | 소셜 로그인 컬럼 추가 SQL |

---

## 🔧 개발자 콘솔 설정 방법

### 1. Google OAuth2 설정

**Google Cloud Console**: https://console.cloud.google.com/

1. **새 프로젝트 생성** 또는 기존 프로젝트 선택
2. **API 및 서비스** → **OAuth 동의 화면** → **외부** 선택 → **만들기**
3. **앱 정보 입력**:
   - 앱 이름: `YourAppName`
   - 사용자 지원 이메일: `support@example.com`
4. **대상** → **저장하고 계속**
5. **API 및 서비스** → **사용자 인증 정보** → **사용자 인증 정보 만들기** → **OAuth 클라이언트 ID**
6. **애플리케이션 유형**: `웹 애플리케이션`
7. **이름**: `Web Client`
8. **승인된 리디렉션 URI**: `http://localhost:8082/login/oauth2/code/google`
9. **만들기** → **클라이언트 ID**와 **클라이언트 보안 비밀번호**를 복사
10. 환경 변수 설정:
    ```bash
    GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
    GOOGLE_CLIENT_SECRET=your-client-secret
    ```

### 2. Kakao OAuth2 설정

**Kakao Developers**: https://developers.kakao.com/

1. **애플리케이션 추가하기**
2. **플랫폼** → **Web** → 사이트 도메인: `http://localhost:8082`
3. **REST API 키** 복사 (이게 client-id)
4. **제품 설정** → **카카오 로그인** → **활성화**
5. **Redirect URI**: `http://localhost:8082/login/oauth2/code/kakao`
6. **제품 설정** → **보안** → **코드** → **클라이언트 시크릿** 생성 및 복사
7. **동의항목**: `profile_nickname`, `profile_image`, `account_email` 필수 동의 설정
8. 환경 변수 설정:
    ```bash
    KAKAO_CLIENT_ID=your-kakao-client-id
    KAKAO_CLIENT_SECRET=your-kakao-client-secret
    ```

### 3. Naver OAuth2 설정

**NAVER Developers**: https://developers.naver.com/

1. **Application** → **애플리케이션 등록**
2. **API 설정**:
   - 사용 API: `네이버 로그인`
   - 로그인 오픈 API 서비스 환경: `PC 웹`
   - PC 웹: `http://localhost:8082`
   - Callback URL: `http://localhost:8082/login/oauth2/code/naver`
3. **등록 완료** → **애플리케이션 정보**에서:
   - **Client ID** 복사
   - **Client Secret** 복사
4. **네이버 로그인** → **설정** → **이용 현황** → **개발 상태** 확인
5. 환경 변수 설정:
    ```bash
    NAVER_CLIENT_ID=your-naver-client-id
    NAVER_CLIENT_SECRET=your-naver-client-secret
    ```

---

## 🧪 테스트 시나리오

### 시나리오 1: 새로운 소셜 회원가입
1. `/users/login` 접속
2. **Kakao 로그인** 버튼 클릭
3. 카카오 계정으로 로그인
4. `/` (홈)으로 리다이렉트
5. `/users/me` 접속하여 회원정보 확인
6. `provider=KAKAO`, `social_linked=true` 확인

### 시나리오 2: 기존 이메일 계정과 소셜 연동
1. 일반 회원가입으로 `test@email.com` 계정 생성
2. 로그아웃
3. **Google 로그인** 버튼 클릭 (같은 `test@email.com` 계정)
4. 기존 계정에 소셜 정보 연동됨
5. `/users/me`에서 `social_linked=true` 확인

### 시나리오 3: 소셜 회원 정보 수정
1. 소셜 로그인 (Google)
2. `/users/update` 접속
3. 닉네임, 전화번호, 이메일 수정
4. 저장 후 `/users/me`에서 변경사항 확인

### 시나리오 4: 소셜 회원 비밀번호 변경 시도 (실패)
1. 소셜 로그인 (Kakao)
2. `/users/update` 접속
3. 비밀번호 변경 섹션에서 변경 시도
4. **"소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다."** 에러 발생

### 시나리오 5: 소셜 회원 탈퇴
1. 소셜 로그인 (Naver)
2. `/users/delete` 접속
3. 비밀번호 입력 없이 바로 탈퇴 가능
4. 탈퇴 완료 후 재가입 확인

### 시나리오 6: 일반 로그인으로 소셜 계정 접근 시도 (실패)
1. 소셜 로그인으로 가입된 계정 확인
2. `/users/login`에서 일반 로그인 시도
3. **"소셜 로그인 계정입니다. 해당 소셜 서비스로 로그인해주세요."** 에러 발생

---

## ⚠️ 주의사항 및 잠재적 오류 포인트

### 1. 비밀번호 null 처리
- **문제**: 소셜 로그인 사용자는 `password=null`
- **해결**: `UserService.login()`에서 소셜 회원은 일반 로그인 차단
- **파일**: `UserService.java:52-56`

### 2. 이메일 없는 Provider (카카오)
- **문제**: 카카오는 이메일 미동의 시 null
- **해결**: `OAuthAttributes.toEntity()`에서 이메일 null 시 `providerId@provider.com` 생성
- **파일**: `OAuthAttributes.java:76-80`

### 3. 동일 이메일 충돌
- **문제**: 일반 가입 이메일과 소셜 이메일 동일 시
- **해결**: `CustomOAuth2UserService.saveOrUpdate()`에서 이메일로 조회 후 연동
- **파일**: `CustomOAuth2UserService.java:47-56`

### 4. Provider ID 길이
- **문제**: 구글 sub는 매우 길 수 있음
- **해결**: DB `provider_id VARCHAR(255)`로 설정

### 5. 로그인 페이지 접근
- **문제**: Spring Security 6.x에서 OAuth2 로그인 경로
- **경로**: `/oauth2/authorization/{provider}` (Spring 자동 생성)

---

## 🔄 소셜 로그인 사용자의 회원 기능 동작 방식

| 기능 | 일반 회원 | 소셜 회원 | 비고 |
|------|----------|----------|------|
| 로그인 | 아이디/비밀번호 | 소셜 버튼 클릭 | 소셜 회원은 일반 로그인 불가 |
| 회원정보 조회 | 정상 | 정상 | 모든 정보 표시 |
| 닉네임 수정 | 가능 | 가능 | 즉시 JWT 갱신 |
| 이메일 수정 | 가능 | 가능 | 소셜과 별개로 저장 |
| 전화번호 수정 | 가능 | 가능 | |
| 비밀번호 변경 | 가능 | **불가** | 에러 메시지 표시 |
| 비밀번호 재설정 | 가능 | **불가** | 에러 메시지 표시 |
| 회원탈퇴 | 비밀번호 확인 필요 | 바로 가능 | 소셜은 비밀번호 없음 |

---

## 🛠️ 빌드 및 실행

```bash
# 1. Gradle 새로고침
./gradlew clean build

# 2. 환경 변수 설정 (Windows PowerShell)
$env:GOOGLE_CLIENT_ID="your-google-client-id"
$env:GOOGLE_CLIENT_SECRET="your-google-client-secret"
$env:KAKAO_CLIENT_ID="your-kakao-client-id"
$env:KAKAO_CLIENT_SECRET="your-kakao-client-secret"
$env:NAVER_CLIENT_ID="your-naver-client-id"
$env:NAVER_CLIENT_SECRET="your-naver-client-secret"

# 3. 애플리케이션 실행
./gradlew bootRun
```

---

## 📁 파일 구조

```
src/main/java/com/example/team3Project/
├── domain/user/
│   ├── User.java                    # 수정됨 (소셜 필드 추가)
│   ├── UserRepository.java          # 수정됨 (findByProviderId 추가)
│   ├── UserService.java             # 수정됨 (소셜 처리 로직)
│   └── UserController.java          # 기존 그대로 (JWT 사용)
├── global/
│   ├── config/
│   │   └── SecurityConfig.java      # 수정됨 (OAuth2 설정)
│   └── security/
│       └── oauth2/
│           ├── CustomOAuth2User.java           # 신규
│           ├── OAuthAttributes.java            # 신규
│           ├── CustomOAuth2UserService.java    # 신규
│           ├── OAuth2LoginSuccessHandler.java  # 신규
│           └── OAuth2LoginFailureHandler.java  # 신규
src/main/resources/
├── templates/users/
│   └── login.html                   # 수정됨 (소셜 버튼 추가)
├── application.properties           # 수정됨 (OAuth2 설정)
└── db/migration/
    └── V2__add_social_login.sql     # 신규 (DB 스키마)
```

---

## ✅ 체크리스트

- [ ] 개발자 콘솔에 애플리케이션 등록 완료
- [ ] 환경 변수 설정 완료
- [ ] DB 스키마 변경 SQL 실행 완료
- [ ] Gradle 의존성 새로고침 (`./gradlew build`)
- [ ] Google 로그인 테스트 완료
- [ ] Kakao 로그인 테스트 완료
- [ ] Naver 로그인 테스트 완료
- [ ] 회원정보 수정 테스트 완료
- [ ] 회원탈퇴 테스트 완료
- [ ] 비밀번호 변경 예외 처리 테스트 완료

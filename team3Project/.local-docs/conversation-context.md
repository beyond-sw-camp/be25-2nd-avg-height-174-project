# conversation-context.md

## 문서 목적
이 문서는 IntelliJ를 다시 켰을 때, Codex와 어디까지 대화했고 무엇을 합의했는지 빠르게 복구하기 위한 개인용 작업 기록 문서입니다.

## 현재 대화 기준 작업 상태
- 정책 도메인 요구사항을 다시 정리하는 대화를 진행했다.
- `.local-docs/current-requirements.md`와 `.local-docs/policy-domain-overview.md`는 현재 합의한 내용 기준으로 갱신했다.
- 실제 코드 수정은 아직 본격적으로 진행하지 않기로 했다.
- 정책 구조 변경은 설명 먼저, 사용자 확인 후 적용하는 방식으로 진행하기로 했다.

## 이번 대화에서 확정한 핵심 요구사항
### 정책 저장 구조
- 정책은 사용자 + 마켓 코드 단위로 관리한다.
- 금지어 / 치환어는 사용자 공통 정책으로 관리한다.

### 1차 국내 마켓 코드
- NAVER_SMART_STORE
- COUPANG
- ELEVEN_STREET
- GMARKET
- AUCTION

### 기본 기준
- 화면 기본 선택 마켓은 COUPANG이다.
- 현재 기본 테스트 마켓도 COUPANG이다.

### 마켓별 정책 항목
- 목표 마진율
- 최소 마진 금액
- 마켓 수수료율
- 카드 수수료율
- 환율
- 단위 올림 단위
- 자동 보호 설정
- 배송비 정책
- AI 기반 자동 최적화 설정

### 배송비 정책
- 배송비 유형은 무료배송 / 유료배송만 우선 구현한다.
- 제주 및 도서산간 추가 배송비는 하나의 필드로 합친다.
- 반품비를 포함한다.
- 조건부 무료배송은 이번 단계에서는 구현하지 않고, 추후 시간이 나면 확장하는 항목으로 둔다.

### AI 기반 자동 최적화
- Amazon 가격 기준 자동 조정은 더미 값 기준으로라도 계산 로직을 고려한다.
- 경쟁 상품 가격 자동 조정은 화면/구조는 두되 실제 연산 로직은 구현하지 않는다.

### 상품 가공 및 등록 흐름
- 상품 가공 후 실제 외부 마켓에 등록하지 않는다.
- 더미 등록 DB에 저장하는 방식으로 진행한다.
- 더미 등록 DB에는 정책 ID만이 아니라 계산 당시 핵심 값도 함께 저장하는 방향으로 간다.

### 자동 보호
- 환율 변동 시 가격 자동 업데이트가 켜져 있으면 더미 등록 DB 판매가도 갱신한다.
- 손실 발생 시 판매 중지는 외부 마켓 연동 없이 내부 상태값과 흐름 차단 중심으로 구현한다.

### 캐시 방향
- 현재 단계에서는 캐시 없이 구현한다.
- 시간이 나면 Redis 같은 공유 캐시 도입 가능성을 열어둔다.
- 로컬 캐시는 다중 인스턴스 일관성 문제 때문에 기본 방향으로 채택하지 않는다.

## 작업 방식 합의
- 코드를 바로 수정하지 말고 먼저 어떤 파일에 무엇을 왜 수정해야 하는지 설명한다.
- 사용자가 확인한 뒤 실제 코드 적용을 진행한다.
- 학습 목적을 고려해서 구조와 수정 이유를 먼저 설명한다.

## 다음 작업 시작점
- 정책 도메인을 사용자 1건 정책 구조에서 사용자 + 마켓 코드 구조로 바꾸는 설계를 설명한다.
- 먼저 설명할 파일:
  - `src/main/java/com/example/team3Project/domain/policy/entity/UserPolicySetting.java`
  - `src/main/java/com/example/team3Project/domain/policy/dao/UserPolicySettingRepository.java`
  - `src/main/java/com/example/team3Project/domain/policy/dto/PolicySettingUpsertRequest.java`
  - `src/main/java/com/example/team3Project/domain/policy/dto/PolicySettingResponse.java`
  - `src/main/java/com/example/team3Project/domain/policy/application/PolicySettingService.java`
  - `src/main/java/com/example/team3Project/domain/policy/api/PolicySettingController.java`
  - `src/main/java/com/example/team3Project/domain/policy/application/PolicyQueryService.java`
  - `src/main/java/com/example/team3Project/domain/product/processing/application/ProductProcessingService.java`
  - `src/main/java/com/example/team3Project/domain/product/processing/api/ProductProcessingController.java`

## 최근 반영 내용
- 반영 일시: 2026-03-29
- 수정된 파일:
  - `.local-docs/conversation-context.md`
- 수정 유형:
  - 생성
- 변경 요약:
  - 현재 대화에서 확정한 요구사항과 다음 작업 시작점을 기록했다.
  - 다음에 IDE를 다시 열었을 때 작업 맥락을 빠르게 복구할 수 있도록 정리했다.

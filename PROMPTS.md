# AI 활용 기록 (AI Assistant Usage Log)

> 컬리 안드로이드 사전과제 개발 시 AI Assistant(Claude)를 활용한 내용을 기록합니다.
> 설계 및 의사결정은 직접 수행하고, AI는 리서치/검증/코드 생성 도구로 활용했습니다.

---

## 1. 요구사항 분석 (2026.03.28)

**프롬프트:**
- "과제 첨부파일(mockserver, JSON, UI 이미지, 아이콘)의 구조를 분석해줘"
- "MockServer 모듈의 API 라우팅과 assets JSON 데이터 규모를 파악해줘"

**결과:**
- MockServer가 OkHttp Interceptor 기반으로 assets JSON을 읽어 응답하는 구조 파악
- API 2개: `/sections?page=Int`, `/section/products?sectionId=Int`
- 총 4페이지 20개 섹션, 마지막 페이지는 paging 필드 없음으로 종료 판단

---

## 2. 기술 스택 선정 (2026.03.28)

**프롬프트:**
- "2026년 3월 기준 Android 최신 기술 스택 버전과 변경사항을 조사해줘"
- "Strong Skipping Mode, Pausable Composition 등 2026 Compose 신규 기능의 영향도를 분석해줘"

**결과:**
- Kotlin 2.3.x, Compose BOM 2025.05.01, Room 2.7.x, Hilt 2.57.x, Coil 3.4.x 확정
- Strong Skipping Mode 기본 활성화 → @Immutable/@Stable, 람다 remember 래핑 불필요 확인
- Room 3.0은 alpha → 안정 버전 2.7.x 사용 결정
- Paging3는 단순 커서 API에 오버엔지니어링 → 수동 구현 결정

---

## 3. 아키텍처 설계 검증 (2026.03.28)

MVVM + UDF 아키텍처를 설계한 후, 2026년 공식 권장과 일치하는지 AI를 통해 검증.

**프롬프트:**
- "Google 공식 아키텍처 가이드와 Now in Android 앱의 상태 관리 방식을 조사해서 내 설계와 비교해줘"
- "단일 UiState + combine + stateIn 패턴이 여전히 공식 권장인지 검증해줘"

**결과:**
- Google 공식: 단일 UiState + combine + stateIn 패턴 여전히 권장 확인
- Now in Android 앱도 동일 패턴 사용 확인
- Domain Layer(UseCase)는 optional → 비즈니스 로직 있는 ToggleWishUseCase만 생성

---

## 4. 찜하기 동기화 설계 (2026.03.28)

Room을 Single Source of Truth로 사용하는 설계를 수립한 후, AI로 구현 코드 생성.

**프롬프트:**
- "Room Flow 기반으로 wishIds(Set<Long>)를 관찰하고, 모든 섹션에서 동기화되는 구조를 구현해줘"
- "derivedStateOf로 개별 카드의 찜 상태를 분리하는 코드를 작성해줘"

**결과:**
- Product 모델에 isWished 필드 없이, UI에서 `product.id in wishIds`로 대조
- Room Flow → Set 변환 → combine으로 UiState에 합성

---

## 5. 성능 최적화 전략 검증 (2026.03.28)

LazyColumn key/contentType, derivedStateOf 등의 최적화 전략을 수립한 후, AI로 2026 기준 유효성 검증.

**프롬프트:**
- "현재 Compose에서 Strong Skipping Mode가 기본 활성화된 상태에서, 어떤 수동 최적화가 여전히 유효한지 검증해줘"

**결과:**
- key: 아이템 식별 → 리컴포지션 스킵 (여전히 유효)
- contentType: Composable 노드 재활용 (여전히 유효)
- @Immutable/@Stable: 불필요 (Strong Skipping이 대체)
- collectAsStateWithLifecycle: 필수 (lifecycle-aware)

---

## 6. UI 컴포넌트 설계 (2026.03.28)

3가지 섹션 타입(grid/horizontal/vertical)별 컴포넌트 트리를 설계한 후, AI로 코드 생성.

**프롬프트:**
- "설계한 컴포넌트 트리에 맞춰 각 섹션 타입별 Composable을 구현해줘. LazyColumn 중첩 스크롤 제약을 고려해서"

**결과:**
- Grid/Vertical: 일반 Column + Row (LazyColumn 내 중첩 스크롤 불가)
- Horizontal: LazyRow (방향이 달라 중첩 가능)
- 상품 카드 2종: HorizontalProductCard(grid/horizontal 공용), VerticalProductCard

---

## 7. 구현 (2026.03.28~)

### Commit 1: 프로젝트 초기 설정 및 멀티모듈 구성

**프롬프트:**
- "mockserver 모듈을 Groovy → Kotlin DSL, kapt → ksp로 마이그레이션하고, 전체 의존성을 Version Catalog로 구성해줘"

**결과:**
- 멀티모듈 구성 + 전체 의존성 설정 완료
- 빌드 오류(core-ktx 1.17.0 → compileSdk 36 요구) 자동 감지 및 수정

### Commit 2: Domain 모델 및 Repository 인터페이스 정의

**프롬프트:**
- "PLAN.md의 설계에 맞춰 Domain 모델, Repository 인터페이스, ToggleWishUseCase를 작성해줘"

**결과:**
- 7개 파일 생성
- 리뷰 시 `Pair<List<Section>, Int?>` → `SectionsResult` 전용 클래스로 개선

### Commit 3: Data Layer 구현 (API, DTO, Room, Repository)

**프롬프트:**
- "MockServer의 API 경로와 JSON 구조에 맞는 Retrofit interface, DTO, Room, Repository, Hilt DI 모듈을 작성해줘"
- "섹션 상품 병렬 로드(async+awaitAll) 방식으로 구현하고, try-catch-finally로 로딩 상태를 관리하는 ViewModel 패턴을 적용해줘"

**결과:**
- 13개 파일 생성 (API, DTO, Room, Repository, DI)
- awaitAll 채택 (레이아웃 안정성 > 개별 표시의 체감 속도)

### Commit 4: MainViewModel 및 상태 관리 구현

**프롬프트:**
- "PLAN.md의 단일 UiState + combine + stateIn 설계에 맞춰 MainViewModel을 구현해줘"
- "리뷰 결과 refresh()에서 isRefreshing이 즉시 꺼지는 버그가 있으니, 로드 완료 후 finally에서 해제되도록 수정하고 update로 일관성 있게 적용해줘"

**결과:**
- MainUiState, MainViewModel 구현 (combine 4개 Flow → 단일 UiState)
- refresh() 버그 수정: isRefreshing 유지 + 기존 리스트 유지하며 교체
- loadSectionsWithProducts() 공통 메서드 추출로 중복 제거
- MutableStateFlow 상태 변경을 update로 통일

### Commit 5: 메인 화면 기본 구조 및 SectionHeader 구현

**프롬프트:**
- "LazyColumn + PullToRefreshBox + collectAsStateWithLifecycle 기반으로 메인 화면 구조를 작성해줘. key와 contentType 설정 포함"
- "전체 프로젝트 코드 리뷰를 돌려서 이슈를 찾아줘"
- "hasNextPage가 일반 var라 combine에서 반응형으로 동작하지 않으니 MutableStateFlow로 변경해줘"
- "refresh() 실패 시 currentPage가 리셋된 상태에서 중복 데이터가 추가되는 버그를 수정해줘. 실패 시 원래 값으로 복원하는 방식으로"

**결과:**
- MainScreen, SectionHeader, SectionContent, LoadingIndicator 구현
- 전체 코드 리뷰 수행 → 12개 이슈 발견 (CRITICAL 1, HIGH 2, MEDIUM 4, LOW 5)
- hasNextPage를 MutableStateFlow로 변경, combine에 5번째 Flow로 포함
- refresh() 실패 시 currentPage, hasNextPage 원래 값 복원 로직 추가
- MutableStateFlow 상태 변경을 .update{}로 전면 통일

---

## 8. 테스트 (진행 예정)

> 테스트 작성 시 AI 활용 내용을 추가 기록합니다.

---

## AI 활용 방식 요약

| 활용 방식 | 내용 |
|-----------|------|
| 리서치 도구 | 2026 최신 Android 기술 트렌드, 공식 권장 패턴 조사 |
| 설계 검증 | 내가 수립한 아키텍처가 공식 권장과 일치하는지 확인 |
| 코드 생성 | 설계 문서(PLAN.md) 기반으로 코드 자동 생성 |
| 트레이드오프 분석 | 기술 선택지의 장단점 비교 (Paging3 vs 수동, awaitAll vs 개별) |
| 빌드 검증 | 각 커밋마다 빌드 체크 + 의존성 검증 자동화 |

# AI 활용 기록 (AI Assistant Usage Log)

> 컬리 안드로이드 사전과제 개발 시 AI Assistant(Claude)를 활용한 내용을 기록합니다.

---

## 1. 요구사항 분석 (2026.03.28)

### 프롬프트
- 과제 요구사항, 첨부파일(mockserver, JSON, UI 이미지, 아이콘) 전체 분석 요청
- Mock Server 모듈의 동작 방식, API 라우팅, 데이터 규모 파악

### AI 활용 결과
- MockServer가 OkHttp Interceptor 기반으로 assets JSON 파일을 읽어 응답하는 구조 파악
- API 2개: `/sections?page=Int`, `/section/products?sectionId=Int`
- 총 4페이지 20개 섹션, 마지막 페이지는 paging 필드 없음으로 종료 판단
- 섹션 타입 3종(grid/horizontal/vertical) 분포 확인

---

## 2. 기술 스택 선정 (2026.03.28)

### 프롬프트
- 2026년 3월 기준 최신 Android 기술 트렌드 조사 요청
- Jetpack Compose, Kotlin, Room, Hilt, Coil 등 최신 버전 및 변경사항 확인
- Strong Skipping Mode, Pausable Composition 등 2026 신규 기능 조사

### AI 활용 결과
- Kotlin 2.3.x, Compose BOM 2025.05.01, Room 2.7.x, Hilt 2.57.x, Coil 3.4.x 확정
- Strong Skipping Mode 기본 활성화 확인 → @Immutable/@Stable 어노테이션 불필요
- 람다 자동 memoize 확인 → remember 래핑 불필요
- Room 3.0은 alpha이므로 과제에서 사용하지 않기로 결정
- Paging3는 단순 커서 기반 API에 오버엔지니어링, 수동 구현 결정

---

## 3. 아키텍처 설계 (2026.03.28)

### 프롬프트
- Google 공식 권장 아키텍처 2026년 기준 확인
- 단일 UiState vs 세분화 StateFlow 트렌드 검증
- MVI vs MVVM 현재 권장 패턴 확인
- Now in Android 레퍼런스 앱의 상태 관리 방식 확인

### AI 활용 결과
- Google 공식: 단일 UiState + combine + stateIn 패턴 여전히 권장 확인
- Now in Android 앱도 단일 StateFlow<UiState> 패턴 사용 확인
- Google은 "UDF(Unidirectional Data Flow)" 용어 사용, MVI와 구조적으로 유사
- Domain Layer(UseCase)는 optional → 단순 위임 UseCase 불필요, 로직 있는 것만 생성
- 모델 레이어: DTO → Domain (2레이어), UI Model 불필요 판단 (화면 1개, 일관성 유지)

---

## 4. 찜하기 동기화 설계 (2026.03.28)

### 프롬프트
- 앱 종료 후 재시작 시 찜 유지 방안
- 여러 섹션에서 동일 상품 찜 동기화 방안
- 성능 문제 없는 설계 요청

### AI 활용 결과
- Room을 Single Source of Truth로 사용
- Product 모델에 isWished 필드를 넣지 않고, wishIds(Set<Long>)를 별도 Flow로 관찰
- UI 렌더링 시점에 `product.id in wishIds`로 대조 → 모든 섹션 자동 동기화
- derivedStateOf로 개별 카드의 찜 상태 분리 → 리컴포지션 최소화

---

## 5. 리컴포지션 최적화 (2026.03.28)

### 프롬프트
- 2026 Compose 성능 최적화 전략 조사
- LazyColumn key vs contentType 차이 확인
- Strong Skipping Mode에서 어떤 최적화가 불필요해졌는지 확인

### AI 활용 결과
- key: 아이템 식별 → 변경 안된 아이템 리컴포지션 스킵
- contentType: Composable 노드 재활용 → 같은 타입끼리 뷰 재사용 (RecyclerView viewType과 유사)
- 자식 컴포저블에 전체 UiState 대신 개별 값 전달 → 불필요한 리컴포지션 방지
- collectAsStateWithLifecycle 필수 사용 (lifecycle-aware)

---

## 6. UI 컴포넌트 설계 (2026.03.28)

### 프롬프트
- UI 참고 이미지 기반 컴포넌트 트리 설계
- 3가지 섹션 타입별 레이아웃 설계
- LazyColumn 중첩 스크롤 제약사항 확인

### AI 활용 결과
- Grid/Vertical 섹션: 일반 Column + Row로 구현 (LazyColumn 내 중첩 스크롤 불가)
- Horizontal 섹션: LazyRow (방향이 달라 중첩 가능)
- 상품 카드 2종: HorizontalProductCard(grid/horizontal 공용), VerticalProductCard
- 가격 표시 분기: horizontal/grid(2줄) vs vertical(1줄)

---

## 7. 구현 (진행 예정)

> 구현 단계에서의 AI 활용 내용은 진행하며 추가 기록합니다.

---

## 8. 테스트 (진행 예정)

> 테스트 작성 시 AI 활용 내용을 추가 기록합니다.

---

## AI 활용 방식 요약

| 활용 영역 | 내용 |
|-----------|------|
| 리서치 | 2026 최신 Android 기술 트렌드, 공식 권장 패턴 조사 |
| 설계 의사결정 | 아키텍처, 상태 관리, 모델 레이어, 성능 전략 |
| 기술 검증 | 단일 UiState vs 세분화, MVI vs UDF, UseCase 필요성 |
| 코드 생성 | 프로젝트 구조, 데이터 모델, ViewModel 패턴 |
| 최적화 | 리컴포지션, 페이징, 찜 동기화 |

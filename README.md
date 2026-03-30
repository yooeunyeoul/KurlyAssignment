# 컬리 안드로이드 사전과제

상품 메인 화면 구현 과제입니다.

## 기술 스택

| 영역 | 기술 |
|------|------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM + UDF (Unidirectional Data Flow) |
| DI | Hilt + KSP |
| Network | Retrofit + OkHttp + MockInterceptor |
| Local DB | Room (찜하기 저장) |
| Async | Coroutines + Flow |
| Image | Coil |
| Test | JUnit + Turbine + Fake Repository |

## 아키텍처

```
Presentation (Compose UI + ViewModel)
    ↕ UiState (StateFlow)
Domain (Model + Repository Interface + UseCase)
    ↕
Data (DTO + Mapper + Repository Impl + Room + Retrofit)
    ↕
MockServer (컬리 제공 모듈)
```

- **단일 UiState**: `combine` + `stateIn(WhileSubscribed(5000))`으로 5개 Flow를 합쳐 단일 UiState 생성
- **Domain Layer**: UseCase는 비즈니스 로직이 있는 경우만 생성 (Google 공식 권장: optional)
- **모델 레이어**: DTO → Domain (2레이어). UI Model은 화면이 1개이므로 불필요한 추상화로 판단

## 멀티모듈 구조

```
KurlyAssignment/
├── app/          # 메인 앱 모듈
└── mockserver/   # 컬리 제공 Mock 서버 모듈
```

## 주요 구현 사항

### 섹션 타입별 레이아웃
- **Grid**: Column + Row (3x2 고정 6개), LazyColumn 중첩 방지
- **Horizontal**: LazyRow 가로 스크롤
- **Vertical**: Column 세로 나열, LazyColumn 중첩 방지

### 상품 카드
- Horizontal/Grid: 세로형 카드 (이미지 + 제목 2줄 말줄임 + 가격 2줄)
- Vertical: 가로형 카드 (이미지 + 제목 1줄 말줄임 + 가격 1줄)

### 가격 표시
- 할인 있음: 할인율(#FA622F) + 할인가(bold) + 원가(취소선)
- 할인 없음: 원가만 표시
- 천단위 콤마 포맷팅

### 찜하기
- Room 기반 로컬 저장 (서버 없이 구현)
- 앱 종료 후 재시작 시 유지
- 여러 섹션에서 동일 상품 자동 동기화 (wishIds Set<Long> 기반)
- 호이스팅: SectionContent에서 Boolean으로 변환 후 카드에 전달
- spring 기반 스케일 애니메이션

### 페이징
- 수동 구현 (Paging3 미사용 - 단순 커서 API에 적합한 판단)
- 섹션별 상품 병렬 로드 (async + awaitAll)
- derivedStateOf로 스크롤 끝 감지

### Pull to Refresh
- PullToRefreshBox (Material3)
- isRefreshing 유지하며 데이터 교체 (기존 리스트 유지)
- 실패 시 이전 상태로 복원

### UX 개선
- Shimmer/Skeleton UI (초기 로딩)
- 에러 화면 + 재시도 버튼
- 품절 상품 dim 처리

## 성능 최적화

- **Strong Skipping Mode**: Compose Compiler 기본 활성화 → @Immutable/@Stable, 람다 remember 불필요
- **LazyColumn key + contentType**: 아이템 식별 + 타입별 Composable 노드 재활용
- **호이스팅**: wishIds(Set) → Boolean 변환을 SectionContent에서 처리, 카드는 Boolean만 수신
- **collectAsStateWithLifecycle**: lifecycle-aware 상태 수집
- **MutableStateFlow update 통일**: 일관된 상태 변경 패턴
- **NumberFormat 인스턴스 재사용**: file-level 캐싱

## 테스트 (18개)

| 테스트 클래스 | 시나리오 |
|--------------|---------|
| MainViewModelTest (8개) | 초기 로딩, 페이징, 마지막 페이지, 찜 토글, 새로고침, 에러, 에러 복구, 중복 로딩 방지 |
| ToggleWishUseCaseTest (2개) | 찜 추가, 찜 제거 |
| DtoMapperTest (8개) | type 매핑 4종, 할인 유무 2종, 페이징 유무 2종 |

- Google 공식 권장에 따라 **Fake Repository** 사용 (Mock 대신)
- Turbine으로 WhileSubscribed StateFlow 테스트

## AI 활용

[PROMPTS.md](./PROMPTS.md) 참고

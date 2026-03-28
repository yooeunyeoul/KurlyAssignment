# 컬리 안드로이드 사전과제 - 구현 계획서

## 1. 과제 개요

- **목표**: 컬리 스타일 상품 메인 화면 구현
- **언어**: Kotlin
- **제출**: GitHub Public 레포지토리
- **평가 기준**: 개발 기초 역량 + AI 활용도 (가장 큰 비중), 추가 구현은 부가점

---

## 2. 기술 스택 (2026.03 기준)

| 영역 | 기술 | 버전 | 선택 이유 |
|------|------|------|-----------|
| Language | Kotlin | 2.3.x | 최신 안정 |
| UI | Jetpack Compose | BOM 2025.05.01 | Strong Skipping, Pausable Composition 기본 활성 |
| DI | Hilt + KSP | 2.57.x | mockserver 모듈이 Hilt 사용, KSP로 빌드 속도 개선 |
| Network | Retrofit + OkHttp | - | MockInterceptor가 OkHttp Interceptor 기반 |
| Local DB | Room + KSP | 2.7.x (안정) | 찜하기 저장, Flow 관찰 지원. 3.0은 alpha이므로 사용 안함 |
| Async | Coroutines + Flow | - | combine + stateIn 패턴 (Google 공식 권장) |
| Image | Coil | 3.4.x | Compose 네이티브 지원, 메모리/디스크 캐시 |
| Serialization | Gson | 2.9.x | mockserver 모듈이 Gson 사용 중, 일관성 유지 |

### 사용하지 않는 것과 이유

| 기술 | 미사용 이유 |
|------|-------------|
| Paging3 | API가 단순 커서 기반(next_page), 수동 구현이 적절. Paging3는 오버엔지니어링 |
| Navigation | 화면이 1개뿐, 네비게이션 라이브러리 불필요 |
| Room 3.0 | alpha 단계, 프로덕션 과제에 alpha 사용은 부적절 |
| DataStore | 관계형 데이터(찜 목록) 저장에는 Room이 적합 |
| UI Model | 화면 1개, Domain Model과 동일한 구조가 되므로 불필요한 추상화 |

---

## 3. 프로젝트 구조

### 멀티모듈

```
KurlyAssignment/
├── app/                      # 메인 앱 모듈
├── mockserver/               # 컬리 제공 Mock 서버 모듈 (그대로 복사)
├── build.gradle.kts          # 루트 빌드 파일
├── settings.gradle.kts       # include ':app', ':mockserver'
├── gradle/
│   └── libs.versions.toml    # Version Catalog
├── .gitignore
├── PLAN.md                   # 이 문서
├── PROMPTS.md                # AI 활용 기록
└── README.md                 # 프로젝트 설명
```

### app 모듈 패키지 구조

```
com.brady.kurly/
├── data/
│   ├── remote/
│   │   ├── api/
│   │   │   └── KurlyApi.kt                  # Retrofit interface
│   │   └── dto/
│   │       ├── SectionResponseDto.kt         # 섹션 API 응답
│   │       └── ProductResponseDto.kt         # 상품 API 응답
│   ├── local/
│   │   ├── dao/
│   │   │   └── WishDao.kt                   # Room DAO
│   │   ├── entity/
│   │   │   └── WishEntity.kt                # Room Entity
│   │   └── KurlyDatabase.kt                 # Room Database
│   ├── repository/
│   │   ├── SectionRepositoryImpl.kt
│   │   └── WishRepositoryImpl.kt
│   └── mapper/
│       └── DtoMapper.kt                     # DTO → Domain 매핑
│
├── domain/
│   ├── model/
│   │   ├── Section.kt                       # 섹션 도메인 모델
│   │   ├── SectionType.kt                   # enum (VERTICAL, HORIZONTAL, GRID)
│   │   ├── Product.kt                       # 상품 도메인 모델
│   │   └── SectionWithProducts.kt           # 섹션 + 상품 결합
│   └── repository/
│       ├── SectionRepository.kt             # interface
│       └── WishRepository.kt                # interface
│
├── presentation/
│   ├── main/
│   │   ├── MainViewModel.kt
│   │   ├── MainUiState.kt                   # 단일 UiState
│   │   ├── MainScreen.kt                    # 메인 화면 Composable
│   │   └── component/
│   │       ├── SectionHeader.kt             # 섹션 제목
│   │       ├── SectionContent.kt            # 타입별 분기
│   │       ├── GridSectionContent.kt        # 3x2 그리드
│   │       ├── HorizontalSectionContent.kt  # 가로 스크롤
│   │       ├── VerticalSectionContent.kt    # 세로 리스트
│   │       ├── HorizontalProductCard.kt     # horizontal/grid 공용 카드
│   │       ├── VerticalProductCard.kt       # vertical 전용 카드
│   │       ├── WishButton.kt               # 찜 토글 버튼
│   │       ├── PriceDisplay.kt             # 가격 표시 (할인 분기)
│   │       └── ProductImage.kt             # 이미지 + Coil
│   └── common/
│       ├── LoadingIndicator.kt
│       └── ErrorScreen.kt
│
├── di/
│   ├── NetworkModule.kt                     # Hilt - Retrofit, OkHttpClient
│   ├── DatabaseModule.kt                    # Hilt - Room
│   └── RepositoryModule.kt                  # Hilt - Repository 바인딩
│
└── KurlyApplication.kt                      # @HiltAndroidApp

```

---

## 4. API 구조 (Mock Server)

### 엔드포인트

| 경로 | 파라미터 | 응답 |
|------|----------|------|
| `/sections` | `page: Int` (1~4) | 섹션 목록 + 페이징 정보 |
| `/section/products` | `sectionId: Int` (1~20) | 상품 목록 |

### Base URL
```
https://kurly.com/
```

### 데이터 규모
- 섹션: 4페이지, 페이지당 5개 = 총 20개 섹션
- 상품: 섹션당 N개 (section_products_1 ~ 20)
- 마지막 페이지(sections_4.json): paging 필드 없음 → 종료 신호

---

## 5. 데이터 모델

### DTO (Data Layer)

```kotlin
data class SectionResponseDto(
    val data: List<SectionDto>,
    val paging: PagingDto?
)

data class SectionDto(
    val id: Int,
    val title: String,
    val type: String,
    val url: String
)

data class PagingDto(
    @SerializedName("next_page")
    val nextPage: Int?
)

data class ProductResponseDto(
    val data: List<ProductDto>
)

data class ProductDto(
    val id: Long,
    val name: String,
    val image: String,
    val originalPrice: Int,
    val discountedPrice: Int?,
    val isSoldOut: Boolean
)
```

### Domain Model

```kotlin
enum class SectionType { VERTICAL, HORIZONTAL, GRID }

data class Section(
    val id: Int,
    val title: String,
    val type: SectionType
)

data class Product(
    val id: Long,
    val name: String,
    val image: String,
    val originalPrice: Int,
    val discountedPrice: Int?,
    val isSoldOut: Boolean
)

data class SectionWithProducts(
    val id: Int,
    val title: String,
    val type: SectionType,
    val products: List<Product>
)
```

### 모델 레이어 결정: DTO → Domain (2레이어)

- UI Model 별도 생성하지 않음
- 이유: 화면이 1개이고, Domain Model에 없는 UI 전용 상태가 찜하기뿐인데 이건 wishIds(Set)로 분리
- 모든 모델에 일관되게 적용 (일부만 UI Model 만드는 혼재 방지)

### 매핑 (Data Layer)

```kotlin
fun SectionDto.toDomain() = Section(
    id = id,
    title = title,
    type = when (type) {
        "horizontal" -> SectionType.HORIZONTAL
        "grid" -> SectionType.GRID
        else -> SectionType.VERTICAL
    }
)

fun ProductDto.toDomain() = Product(
    id = id,
    name = name,
    image = image,
    originalPrice = originalPrice,
    discountedPrice = discountedPrice,
    isSoldOut = isSoldOut
)
```

---

## 6. 아키텍처 & 상태 관리

### 패턴: MVVM + UDF (Google 공식 권장)

```
Compose UI → Event → ViewModel → State → Compose UI
                         ↕
                    Repository (interface)
                         ↕
                   RepositoryImpl
                    ↙        ↘
              KurlyApi      WishDao
           (MockServer)     (Room)
```

### 단일 UiState (Google 공식 권장 패턴)

```kotlin
data class MainUiState(
    val sections: List<SectionWithProducts> = emptyList(),
    val wishIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasNextPage: Boolean = true,
    val error: String? = null
)
```

### ViewModel - combine + stateIn

```kotlin
val uiState: StateFlow<MainUiState> = combine(
    _sections,
    wishRepository.observeWishIds(),
    _pagingState
) { sections, wishIds, paging ->
    MainUiState(
        sections = sections,
        wishIds = wishIds.toSet(),
        isLoading = paging.isLoading,
        isLoadingMore = paging.isLoadingMore,
        isRefreshing = paging.isRefreshing,
        hasNextPage = paging.hasNextPage
    )
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = MainUiState(isLoading = true)
)
```

### UseCase 전략

- 단순 위임 UseCase는 생성하지 않음 (Google 공식: Domain Layer는 optional)
- ToggleWishUseCase만 생성 (찜 여부 판단 + insert/delete 분기 로직 존재)
- ViewModel에서 Repository 직접 호출

---

## 7. 찜하기 동기화 설계

### 핵심 원칙: Room을 Single Source of Truth로

```
Room (wishes 테이블)
    ↓ Flow<List<Long>>
ViewModel에서 .toSet()
    ↓ Set<Long>
UI: product.id in wishIds → 찜 여부 판단
```

### Room 스키마

```kotlin
@Entity(tableName = "wishes")
data class WishEntity(
    @PrimaryKey val productId: Long
)

@Dao
interface WishDao {
    @Query("SELECT productId FROM wishes")
    fun observeWishIds(): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM wishes WHERE productId = :productId)")
    suspend fun isWished(productId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wish: WishEntity)

    @Query("DELETE FROM wishes WHERE productId = :productId")
    suspend fun delete(productId: Long)
}
```

### 동기화 보장

- 같은 상품이 여러 섹션에 등장 → wishIds(Set)로 대조하므로 자동 동기화
- 앱 종료 후 재시작 → Room에 저장되어 있으므로 유지
- 찜 토글 → Room insert/delete → Flow 자동 emit → UI 자동 갱신

---

## 8. 페이징 설계

### 수동 구현 (Paging3 미사용)

```
1. 초기 로드: page=1 요청 → 5개 섹션 + next_page=2
2. 스크롤 끝 감지: lastIndex - 1 도달 시 다음 페이지 요청
3. 섹션별 상품: async + awaitAll로 병렬 로드
4. 종료 조건: paging 필드 없음 (sections_4.json)
5. 중복 요청 방지: isLoadingPage 플래그
```

### 상품 병렬 로드

```
페이지 N 로드 → 5개 섹션 정보 수신
    ↓
async { 섹션1 상품 로드 }
async { 섹션2 상품 로드 }
async { 섹션3 상품 로드 }  ← 5개 동시 요청
async { 섹션4 상품 로드 }
async { 섹션5 상품 로드 }
    ↓
awaitAll() → UI에 한번에 반영
```

---

## 9. UI 컴포넌트 설계

### 전체 화면 트리

```
MainScreen (PullToRefresh)
└── LazyColumn (key, contentType 설정)
    ├── SectionItem (반복)
    │   ├── SectionHeader (섹션 타이틀)
    │   └── SectionContent (타입별 분기)
    │       ├── GridSectionContent (Column + Row, 3x2 고정)
    │       ├── HorizontalSectionContent (LazyRow, 가로 스크롤)
    │       └── VerticalSectionContent (Column, 세로 나열)
    └── LoadingIndicator (다음 페이지 로딩 중)
```

### 상품 카드 2종

**HorizontalProductCard** (horizontal, grid 공용 - 세로형 카드)
```
┌──────────────┐
│ ┌──────────┐ │
│ │  Image ♥ │ │  이미지 + 우하단 찜버튼
│ └──────────┘ │
│ 제목 2줄 말줄임 │  maxLines=2
│ 30% 8,400원   │  할인율(#FA622F) + 할인가(bold) - 1줄
│ 9,900원       │  원가(취소선, 회색) - 2줄
└──────────────┘
```

**VerticalProductCard** (vertical 전용 - 가로형 카드)
```
┌──────────────────────────────────┐
│ ┌──────────┐ 제목 1줄 말줄임...   │  maxLines=1
│ │ Image  ♥ │ 30% 8,400원 9,900원 │  가격 한줄에 전부
│ └──────────┘                     │
└──────────────────────────────────┘
```

### LazyColumn 중첩 스크롤 제약

- LazyColumn 안에 LazyColumn/LazyVerticalGrid 중첩 불가 (크래시)
- Grid → Column + Row로 6개 고정 배치
- Vertical → Column으로 나열
- Horizontal → LazyRow (방향이 다르므로 중첩 가능)

### LazyColumn 최적화

```kotlin
LazyColumn {
    items(
        items = sections,
        key = { it.id },              // 리컴포지션 스킵 (아이템 식별)
        contentType = { it.type }     // Composable 노드 재활용 (같은 타입끼리)
    ) { section ->
        SectionItem(...)
    }
}
```

---

## 10. 성능 최적화 전략

### Compose 리컴포지션

| 전략 | 적용 | 비고 |
|------|------|------|
| Strong Skipping Mode | 자동 (Compose Compiler 2.0.20+) | 별도 설정 불필요 |
| @Immutable/@Stable | 미사용 | Strong Skipping이 자동 처리 |
| 람다 remember 래핑 | 미사용 | Strong Skipping이 자동 memoize |
| derivedStateOf | 사용 | 찜 상태를 카드 레벨에서 분리 |
| LazyColumn key | 사용 | section.id |
| LazyColumn contentType | 사용 | section.type |
| 자식에게 개별 값 전달 | 사용 | 전체 UiState 넘기지 않음 |
| collectAsStateWithLifecycle | 사용 | collectAsState 대신 |

### 찜 토글 리컴포지션 최소화

```kotlin
// ProductCard 내부에서 derivedStateOf로 분리
val isWished by remember(product.id, wishIds) {
    derivedStateOf { product.id in wishIds }
}
// wishIds가 변경되어도 이 상품의 찜 상태가 안 바뀌면 WishButton 스킵
```

### 이미지

- Coil 메모리 캐시 + 디스크 캐시 활성화
- crossfade 애니메이션으로 로딩 자연스럽게

### Flow

- WhileSubscribed(5_000): 화면 이탈 5초 후 수집 중단
- 화면 회전 시 재구독 방지

---

## 11. UX 개선 계획

### 필수 구현 (과제 요구사항)

- [x] 섹션별 레이아웃 (grid/horizontal/vertical)
- [x] 가격/할인 표시 (할인율, 할인가, 원가)
- [x] 찜하기 토글 (로컬 저장, 앱 재시작 유지)
- [x] Pull to Refresh
- [x] 페이징 (무한 스크롤)

### 2순위 UX (추가 공수 적음, 자연스러운 개선)

- [ ] 가격 천단위 콤마 포맷팅 (9900 → 9,900원)
- [ ] 품절 상품 dim 처리 (isSoldOut=true)
- [ ] 에러 화면 + 재시도 버튼
- [ ] 페이지 로딩 인디케이터 (하단 스피너)
- [ ] Shimmer/Skeleton UI (로딩 중 뼈대)

### 3순위 UX (시간 남으면)

- [ ] 찜 토글 애니메이션 (하트 팝 효과)
- [ ] 접근성 (contentDescription)
- [ ] 빈 상태 처리

---

## 12. 테스트 계획

### 우선순위

| 순위 | 대상 | 내용 |
|------|------|------|
| 1 | MainViewModel | 페이징, 찜 토글, 새로고침, 에러 처리 |
| 2 | Repository | DTO → Domain 매핑 정확성 |
| 3 | ToggleWishUseCase | 찜 토글 로직 |

### 기본 기능 완벽 구현 > 테스트 커버리지

과제에서 "기본 기능부터 순서대로 구현" 권장. 기능 누락 + 테스트만 있으면 감점.

---

## 13. 커밋 단위 작업 계획

### Commit 1: chore: 프로젝트 초기 설정 및 멀티모듈 구성

```
작업 내용:
  - mockserver/ 모듈 복사 (컬리 첨부파일 → 프로젝트)
  - mockserver/build.gradle → build.gradle.kts 마이그레이션
    - compileSdk 32 → 35, minSdk 23 유지, targetSdk 35
    - kapt → ksp 전환
    - 의존성 버전 최신화 (app 모듈과 호환)
  - settings.gradle.kts에 include(":mockserver") 추가
  - libs.versions.toml에 전체 의존성 추가:
    - Hilt 2.57.x + KSP
    - Retrofit 2.x + OkHttp 4.x
    - Room 2.7.x + KSP
    - Coil 3.4.x
    - Gson 2.9.x
    - Coroutines
    - lifecycle-runtime-compose (collectAsStateWithLifecycle)
  - 루트 build.gradle.kts에 Hilt, KSP 플러그인 추가
  - app/build.gradle.kts에 의존성 추가 + mockserver 모듈 의존
  - .gitignore 확인 및 보완
  - 찜하기 아이콘 복사:
    - ic_btn_heart_on.xml → app/src/main/res/drawable/
    - ic_btn_heart_off.xml → app/src/main/res/drawable/

생성/수정 파일:
  - mockserver/ (디렉토리 전체)
  - settings.gradle.kts
  - build.gradle.kts (루트)
  - app/build.gradle.kts
  - gradle/libs.versions.toml
  - .gitignore
  - app/src/main/res/drawable/ic_btn_heart_on.xml
  - app/src/main/res/drawable/ic_btn_heart_off.xml

빌드 확인: Gradle Sync 성공
```

### Commit 2: feat: Domain 모델 및 Repository 인터페이스 정의

```
작업 내용:
  - Domain 모델 정의
  - Repository 인터페이스 정의
  - 비즈니스 로직이 있는 UseCase만 생성

생성 파일:
  - domain/model/Section.kt           (id, title, type)
  - domain/model/SectionType.kt       (enum: VERTICAL, HORIZONTAL, GRID)
  - domain/model/Product.kt           (id, name, image, originalPrice, discountedPrice?, isSoldOut)
  - domain/model/SectionWithProducts.kt (섹션 + 상품 결합)
  - domain/repository/SectionRepository.kt  (interface)
  - domain/repository/WishRepository.kt     (interface)
  - domain/usecase/ToggleWishUseCase.kt     (찜 토글 로직)
```

### Commit 3: feat: Data Layer 구현 (API, DTO, Room, Repository)

```
작업 내용:
  - Retrofit API 인터페이스
  - DTO 모델 (API 응답 구조 그대로)
  - DTO → Domain 매핑
  - Room Database + WishDao + WishEntity
  - Repository 구현체
  - Hilt DI 모듈 (Network, Database, Repository)

생성 파일:
  - data/remote/api/KurlyApi.kt
  - data/remote/dto/SectionResponseDto.kt
  - data/remote/dto/ProductResponseDto.kt
  - data/local/entity/WishEntity.kt
  - data/local/dao/WishDao.kt
  - data/local/KurlyDatabase.kt
  - data/repository/SectionRepositoryImpl.kt
  - data/repository/WishRepositoryImpl.kt
  - data/mapper/DtoMapper.kt
  - di/NetworkModule.kt          (Retrofit + OkHttpClient + MockInterceptor)
  - di/DatabaseModule.kt         (Room)
  - di/RepositoryModule.kt       (Repository 바인딩)
  - KurlyApplication.kt          (@HiltAndroidApp)
  - AndroidManifest.xml 수정      (Application 클래스 + INTERNET 권한)
```

### Commit 4: feat: MainViewModel 및 상태 관리 구현

```
작업 내용:
  - MainUiState 정의 (단일 UiState)
  - MainViewModel 구현
    - combine(_sections, wishIds, _pagingState) → UiState
    - stateIn(WhileSubscribed(5_000))
    - loadNextPage() - 페이징 로직
    - refresh() - Pull to Refresh 로직
    - toggleWish() - 찜 토글
    - 섹션별 상품 병렬 로드 (async + awaitAll)
  - MainActivity에 @AndroidEntryPoint + Hilt 설정

생성 파일:
  - presentation/main/MainUiState.kt
  - presentation/main/MainViewModel.kt
수정 파일:
  - MainActivity.kt (@AndroidEntryPoint, MainScreen 호출)
```

### Commit 5: feat: 메인 화면 기본 구조 및 SectionHeader 구현

```
작업 내용:
  - MainScreen 기본 구조 (LazyColumn + PullToRefresh)
  - collectAsStateWithLifecycle으로 상태 수집
  - SectionItem (섹션 반복 단위)
  - SectionHeader (섹션 타이틀)
  - SectionContent (타입별 분기 when문)
  - LazyColumn key + contentType 설정
  - 로딩 인디케이터 (초기 로딩, 페이지 로딩)

생성 파일:
  - presentation/main/MainScreen.kt
  - presentation/main/component/SectionHeader.kt
  - presentation/main/component/SectionContent.kt
  - presentation/common/LoadingIndicator.kt
```

### Commit 6: feat: 상품 카드 공통 컴포넌트 구현 (이미지, 가격, 찜)

```
작업 내용:
  - ProductImage (Coil AsyncImage + 메모리/디스크 캐시)
  - WishButton (하트 아이콘 토글, ic_btn_heart_on/off 사용)
  - PriceDisplay
    - 할인 있을 때: 할인율(#FA622F) + 할인가(bold) + 원가(취소선)
    - 할인 없을 때: 원가만 표시
    - 천단위 콤마 포맷팅 (9900 → 9,900원)
  - derivedStateOf로 찜 상태 분리

생성 파일:
  - presentation/main/component/ProductImage.kt
  - presentation/main/component/WishButton.kt
  - presentation/main/component/PriceDisplay.kt
```

### Commit 7: feat: Grid 섹션 UI 구현

```
작업 내용:
  - GridSectionContent (Column + Row, 3x2 고정 6개)
  - HorizontalProductCard (세로형 카드 - grid/horizontal 공용)
    - 이미지 + 제목(2줄 말줄임) + 가격(2줄)
    - LazyColumn 중첩 방지 → 일반 Column + Row

생성 파일:
  - presentation/main/component/GridSectionContent.kt
  - presentation/main/component/HorizontalProductCard.kt
```

### Commit 8: feat: Horizontal 섹션 UI 구현

```
작업 내용:
  - HorizontalSectionContent (LazyRow, 가로 스크롤)
  - HorizontalProductCard 재사용 (Commit 7에서 생성)

생성 파일:
  - presentation/main/component/HorizontalSectionContent.kt
```

### Commit 9: feat: Vertical 섹션 UI 구현

```
작업 내용:
  - VerticalSectionContent (Column, 세로 나열)
  - VerticalProductCard (가로형 카드)
    - 이미지 + 제목(1줄 말줄임) + 가격(1줄에 전부)
    - LazyColumn 중첩 방지 → 일반 Column

생성 파일:
  - presentation/main/component/VerticalSectionContent.kt
  - presentation/main/component/VerticalProductCard.kt
```

### Commit 10: feat: UX 개선 (에러 처리, 품절, Skeleton)

```
작업 내용:
  - 에러 화면 + 재시도 버튼
  - 품절 상품 dim 처리 (isSoldOut=true → 이미지 반투명 + "품절" 텍스트)
  - Shimmer/Skeleton UI (로딩 중 뼈대 표시)
  - 빈 상태 처리

생성 파일:
  - presentation/common/ErrorScreen.kt
  - presentation/common/ShimmerEffect.kt
수정 파일:
  - ProductImage.kt (품절 dim 처리)
  - MainScreen.kt (에러/빈 상태 분기)
```

### Commit 11: test: ViewModel 단위 테스트 추가

```
작업 내용:
  - MainViewModel 테스트
    - 초기 로딩 상태 테스트
    - 섹션 + 상품 로드 성공 테스트
    - 찜 토글 테스트
    - 페이징 테스트 (다음 페이지 로드, 마지막 페이지 처리)
    - 새로고침 테스트
    - 에러 처리 테스트
  - Fake Repository 구현 (테스트용)

생성 파일:
  - app/src/test/.../presentation/main/MainViewModelTest.kt
  - app/src/test/.../data/repository/FakeSectionRepository.kt
  - app/src/test/.../data/repository/FakeWishRepository.kt
```

### Commit 12: docs: README 및 AI 프롬프트 기록 작성

```
작업 내용:
  - README.md 작성 (기술 스택, 아키텍처, 주요 구현 사항)
  - PROMPTS.md 최종 정리 (구현/테스트 단계 AI 활용 내용 추가)
  - PLAN.md 최종 상태 업데이트

생성/수정 파일:
  - README.md
  - PROMPTS.md
  - PLAN.md
```

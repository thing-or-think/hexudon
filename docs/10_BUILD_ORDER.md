# 10. LỘ TRÌNH TRIỂN KHAI REFACTOR (BUILD ORDER)

## Mục lục
1. [Nguyên tắc phân chia Phase](#1-nguyên-tắc-phân-chia-phase)
2. [Chi tiết các Giai đoạn Refactor (Phase Details)](#2-chi-tiết-các-giai-đoạn-refactor-phase-details)
   - [Phase 1: Tạo cấu trúc Package mới và Di chuyển Exception](#phase-1-tạo-cấu-trúc-package-mới-và-di-chuyển-exception)
   - [Phase 2: Di chuyển Entities và Value Objects của Domain Core](#phase-2-di-chuyển-entities-và-value-objects-của-domain-core)
   - [Phase 3: Di chuyển và Phân bổ các Domain Services (Engines)](#phase-3-di-chuyển-và-phân-bổ-các-domain-services-engines)
   - [Phase 4: Thiết lập Ports, Use Cases, DTOs và Application Service](#phase-4-thiết-lập-ports-use-cases-dtos-và-application-service)
   - [Phase 5: Triển khai các Inbound & Outbound Adapters](#phase-5-triển-khai-các-inbound--outbound-adapters)
   - [Phase 6: Cấu trúc tầng Infrastructure, Sửa đổi WebConfig và Xác minh](#phase-6-cấu-trúc-tầng-infrastructure-sửa-đổi-webconfig-và-xác-minh)

---

## 1. Nguyên tắc phân chia Phase

Để giảm thiểu tối đa rủi ro gây lỗi logic hiện tại, lộ trình refactor được chia thành 6 Phase độc lập theo nguyên lý **"Bottom-Up" (Từ dưới lên)**:
- Di chuyển các class lá (ít phụ thuộc nhất) trước, sau đó di chuyển các class phụ thuộc vào chúng.
- Cuối mỗi Phase, toàn bộ hệ thống bắt buộc phải:
  1. Compile thành công không có lỗi cú pháp.
  2. Vượt qua toàn bộ 57 Unit Tests nghiệp vụ hiện tại.
  3. Có thể thực hiện commit độc lập vào Git hoặc rollback nếu xảy ra sự cố.

---

## 2. Chi tiết các Giai đoạn Refactor (Phase Details)

### Phase 1: Tạo cấu trúc Package mới và Di chuyển Exception

#### Mục tiêu:
Thiết lập thư mục vật lý theo kiến trúc DDD/Hexagonal và di chuyển các lớp exception vì chúng là lá phụ thuộc của toàn bộ dự án.

#### Các package cần tạo:
- `domain/model`, `domain/valueobject`, `domain/event`, `domain/service`, `domain/repository`, `domain/exception`
- `application/port/in`, `application/port/out`, `application/service`, `application/dto`, `application/mapper`
- `adapter/in/rest`, `adapter/out/persistence`, `adapter/out/loader`, `adapter/out/configuration`
- `infrastructure/configuration`, `infrastructure/interceptor`, `infrastructure/scheduler`, `infrastructure/util`

#### Các class cần di chuyển:
- Di chuyển toàn bộ các class từ `com.naprock.hexudon.exception.*` sang `com.naprock.hexudon.domain.exception`.

#### Import & Dependency cần sửa:
- Cập nhật import đường dẫn Exception mới trong các class `model`, `engine`, `manager`, `controller`.

#### Rủi ro:
Thấp. Hầu như chỉ là thao tác di chuyển file vật lý và sửa import tự động của IDE.

#### Điều kiện hoàn thành (Definition of Done):
- Dự án compile thành công.
- Chạy test suite pass 100%.

#### Commit đề xuất:
`refactor(architecture): create hexagonal packages and migrate exceptions`

---

### Phase 2: Di chuyển Entities và Value Objects của Domain Core

#### Mục tiêu:
Đưa các mô hình nghiệp vụ bất biến và thực thể cốt lõi về đúng phân vùng Domain Core.

#### Các class cần di chuyển:
- Di chuyển `Agent.java`, `Team.java` sang `domain.model`.
- Di chuyển `Cell.java`, `Road.java`, `Spot.java`, `Action.java`, `ActionType.java`, `AgentType.java`, `MatchStatus.java`, `TerrainType.java`, `MatchConfig.java`, `MatchState.java`, `AgentExecutionResult.java`, `TurnSimulationResult.java` sang `domain.valueobject`.

#### Import & Dependency cần sửa:
- Cập nhật import của các model trong các lớp `engine`, `manager`, `controller` và các lớp Test.
- Loại bỏ các annotation Spring (nếu có) trong các class này (chỉ giữ Lombok).

#### Rủi ro:
Trung bình thấp. Cần cẩn thận với việc thay đổi package của các class có lượng sử dụng lớn trong dự án.

#### Điều kiện hoàn thành (Definition of Done):
- Không có lỗi compile.
- Toàn bộ Unit Test hiện tại chạy thành công.

#### Commit đề xuất:
`refactor(domain): migrate entities and value objects to domain core`

---

### Phase 3: Di chuyển và Phân bổ các Domain Services (Engines)

#### Mục tiêu:
Đóng gói các động cơ tính toán luật chơi thành các Domain Service thuần túy Java (không chứa Spring Bean annotation).

#### Các class cần di chuyển:
- Di chuyển `HexGridUtils.java`, `TerrainGenerator.java`, `MapValidator.java`, `ActionValidatorEngine.java`, `FuelManager.java`, `UdonCollectionEngine.java`, `MovementSimulator.java` sang `domain.service`.
- Tạo mới Interface `MatchStateRepository` đặt trong `domain.repository`.

#### Import & Dependency cần sửa:
- Xóa bỏ các annotation Spring `@Component` hoặc `@Service` trên các class engine này. Chúng sẽ được khởi tạo thủ công bởi Domain hoặc Application Service để giữ sự độc lập khỏi framework.

#### Rủi ro:
Trung bình. Phải đảm bảo không sửa bất kỳ thuật toán BFS hay trừ xăng nào của engine.

#### Điều kiện hoàn thành (Definition of Done):
- Biên dịch thành công. Các Unit Test cho Engine và Simulator pass 100%.

#### Commit đề xuất:
`refactor(domain): migrate game engines to domain services`

---

### Phase 4: Thiết lập Ports, Use Cases, DTOs và Application Service

#### Mục tiêu:
Xây dựng lớp điều phối ca sử dụng (Application Layer), định nghĩa các cổng giao tiếp vào/ra độc lập.

#### Các package/class cần tạo mới:
- Tạo Inbound Ports: `RegisterTeamUseCase`, `StartMatchUseCase`, `SubmitActionsUseCase`, `GetMatchStateUseCase` trong `application.port.in`.
- Tạo Outbound Ports: `MatchStateStorePort`, `MatchConfigLoaderPort` trong `application.port.out`.
- Tạo `MatchApplicationService` trong `application.service` thực thi các Inbound Ports nêu trên.

#### Các class cần di chuyển:
- Di chuyển toàn bộ các class từ package `dto` sang `application.dto`.
- Di chuyển `ActionMapper.java` sang `application.mapper`.

#### Phân rã logic của `MatchManager`:
- Chuyển toàn bộ logic điều hành của `MatchManager` sang `MatchApplicationService`. Xóa bỏ class `MatchManager`.

#### Rủi ro:
Cao. Đây là bước thay đổi cốt lõi trong luồng điều phối. Cần đảm bảo `MatchApplicationService` gọi chính xác các bước điều phối giống hệt `MatchManager` cũ.

#### Điều kiện hoàn thành (Definition of Done):
- Toàn bộ ứng dụng build thành công (mặc dù REST Controller và Loader chưa chạy do chưa cấu hình Adapter).

#### Commit đề xuất:
`refactor(application): implement ports, use cases, and match application service`

---

### Phase 5: Triển khai các Inbound & Outbound Adapters

#### Mục tiêu:
Kết nối các cổng Port của lớp Application với hạ tầng kỹ thuật (HTTP REST, File System, In-Memory Repository).

#### Các class cần tạo mới / sửa đổi:
- Tạo mới `InMemoryMatchStateRepository` trong `adapter.out.persistence`, triển khai `MatchStateStorePort` và `MatchStateRepository`.
- Chuyển đổi `MatchConfigLoader.java` thành `FileMatchConfigLoader` đặt tại `adapter.out.loader`, triển khai `MatchConfigLoaderPort`.
- Tạo `AdapterBeanConfig.java` trong `adapter.out.configuration` để đăng ký các bean của Application Service vào Spring context.

#### Các class cần di chuyển:
- Di chuyển `MatchController.java` sang `adapter.in.rest`. Thay đổi các kiểu tiêm phụ thuộc từ `MatchManager` sang các Inbound Port Interface.

#### Rủi ro:
Trung bình. Cần đảm bảo các Annotation Spring `@RestController` và mapping đường dẫn không bị thay đổi.

#### Điều kiện hoàn thành (Definition of Done):
- Dự án compile thành công.
- Các controller test chạy pass.

#### Commit đề xuất:
`refactor(adapter): implement web controllers and config loader adapters`

---

### Phase 6: Cấu trúc tầng Infrastructure, Sửa đổi WebConfig và Xác minh

#### Mục tiêu:
Di chuyển cấu hình Spring Boot, sửa lỗi Rate Limiter Interceptor đã biết, sửa Scheduler, cập nhật kiểm thử kiến trúc ArchUnit và hoàn tất dự án.

#### Các class cần di chuyển:
- Di chuyển `AppConfig.java`, `WebConfig.java` sang `infrastructure.configuration`.
- Di chuyển `RateLimiterInterceptor.java` sang `infrastructure.interceptor`.
- Di chuyển `SchedulerConfig.java` sang `infrastructure.scheduler`.
- Di chuyển `FileUtils.java` sang `infrastructure.util`.

#### Điều chỉnh và Vá lỗi kỹ thuật (Sửa lỗi theo README.md hiện tại):
- **Sửa lỗi Rate Limiter Path:** Trong `WebConfig.java`, sửa đường dẫn interceptor từ `/api/match/action` (thiếu s) thành `/api/match/actions` (đường dẫn chuẩn của controller).
- **Sửa lỗi Tiến trình Scheduler:** Trong `SchedulerConfig.java`, sửa đổi cách kiểm tra hoặc tiêm Port để đảm bảo gọi chính xác logic chuyển ngày mới.
- **Cập nhật ArchitectureTest:** Cập nhật các quy tắc kiểm tra import của ArchUnit theo cấu trúc package DDD/Hexagonal mới (`domain`, `application`, `adapter`, `infrastructure`).

#### Rủi ro:
Thấp. Đây là bước chuẩn hóa cuối cùng.

#### Điều kiện hoàn thành (Definition of Done):
- Chạy `mvn clean test` pass toàn bộ unit tests, integration tests, và đặc biệt là `ArchitectureTest` của ArchUnit.
- Ứng dụng khởi động bình thường bằng `mvn spring-boot:run`.

#### Commit đề xuất:
`refactor(infrastructure): finalize infrastructure configurations, fix bugs and pass arch tests`

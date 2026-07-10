# 03. DANH SÁCH CÁC LỚP (CLASS LIST)

## Mục lục
1. [Bảng danh sách toàn bộ các class và phân loại](#1-bảng-danh-sách-toàn-bộ-các-class-và-phân-loại)
2. [Đề xuất chia tách và cải tiến cấu trúc Class](#2-đề-xuất-chia-tách-và-cải-tiến-cấu-trúc-class)

---

## 1. Bảng danh sách toàn bộ các class và phân loại

Dưới đây là bảng tổng hợp chi tiết vị trí và vai trò mới của toàn bộ các lớp trong hệ thống sau khi refactor sang cấu trúc DDD và Hexagonal:

| Tên Class | Tầng (Layer) | Package mới | Loại (Type) | Ghi chú |
| :--- | :--- | :--- | :--- | :--- |
| `Agent` | Domain | `domain.model` | Entity | Thực thể điệp viên của một đội. |
| `Team` | Domain | `domain.model` | Entity | Thực thể đội chơi quản lý danh sách Agent. |
| `Spot` | Domain | `domain.valueobject` | Value Object | Vị trí đặc biệt chứa bánh Udon (đã được tối ưu thành Value Object thuộc quản lý của MatchState). |
| `Cell` | Domain | `domain.valueobject` | Value Object | Ô bản đồ lục giác (Tọa độ X, Y, Địa hình). |
| `Road` | Domain | `domain.valueobject` | Value Object | Đường nối giữa hai Cell liền kề. |
| `Action` | Domain | `domain.valueobject` | Value Object | Một bước hành động của Agent (`MOVE` hoặc `WAIT`). |
| `Submission` | Domain | `domain.valueobject` | Value Object | **[NEW]** Kế hoạch nộp cho một ngày (chứa danh sách hành động các Agent). |
| `MatchConfig` | Domain | `domain.valueobject` | Value Object | Thông số cấu hình luật chơi bất biến. |
| `MatchState` | Domain | `domain.valueobject` | Aggregate | Trạng thái tổng của trận đấu, đóng vai trò **Aggregate Root**. |
| `AgentExecutionResult`| Domain | `domain.valueobject` | Value Object | Kết quả thực thi hành động của Agent. |
| `TurnSimulationResult`| Domain | `domain.valueobject` | Value Object | Kết quả mô phỏng sau một Turn. |
| `MatchStatus` | Domain | `domain.valueobject` | Value Object | Enum trạng thái trận đấu (`WAITING`, `PLAYING`, `FINISHED`). |
| `TerrainType` | Domain | `domain.valueobject` | Value Object | Enum loại địa hình (`PLAIN`, `MOUNTAIN`, `ROAD`, `POND`). |
| `AgentType` | Domain | `domain.valueobject` | Value Object | Enum loại Agent (`PATROL`, `REFUEL`). |
| `ActionType` | Domain | `domain.valueobject` | Value Object | Enum loại hành động của Agent (`MOVE`, `WAIT`). |
| `MovementSimulator` | Domain | `domain.service` | Domain Service | Bộ máy mô phỏng di chuyển của Agent. |
| `FuelManager` | Domain | `domain.service` | Domain Service | Bộ máy quản lý nạp và tiêu hao nhiên liệu. |
| `TrafficCalculator` | Domain | `domain.service` | Domain Service | Bộ máy tính toán mật độ giao thông (sẽ triển khai logic). |
| `ScoringEngine` | Domain | `domain.service` | Domain Service | Bộ máy tính điểm thu thập Udon cho các đội. |
| `UdonCollectionEngine`| Domain | `domain.service` | Domain Service | Bộ máy xử lý thu thập Udon tại các Spot. |
| `ActionValidatorEngine`| Domain | `domain.service` | Domain Service | Động cơ kiểm tra tính hợp lệ của Action. |
| `MapValidator` | Domain | `domain.service` | Domain Service | Thuật toán BFS kiểm tra tính liên thông bản đồ. |
| `HexGridUtils` | Domain | `domain.service` | Domain Service | Tiện ích tính toán tọa độ, khoảng cách lưới lục giác. |
| `TerrainGenerator` | Domain | `domain.service` | Domain Service | Bộ sinh địa hình ngẫu nhiên cho bản đồ. |
| `MatchStateRepository`| Domain | `domain.repository`| Repository | Interface định nghĩa các hàm truy vấn/lưu trữ MatchState. |
| `BusinessException` | Domain | `domain.exception` | Exception | Ngoại lệ nghiệp vụ gốc (Base). |
| `SystemException` | Domain | `domain.exception` | Exception | Ngoại lệ hệ thống gốc (Base). |
| `GameRuleViolationException`| Domain | `domain.exception` | Exception | Ngoại lệ khi vi phạm luật chơi. |
| `MatchStateConflictException`| Domain | `domain.exception` | Exception | Ngoại lệ khi xung đột trạng thái trận đấu. |
| `RateLimitExceededException`| Domain | `domain.exception` | Exception | Ngoại lệ khi vượt quá giới hạn tần suất gửi yêu cầu. |
| `ResourceNotFoundException`| Domain | `domain.exception` | Exception | Ngoại lệ khi không tìm thấy tài nguyên. |
| `ConfigLoadException`| Domain | `domain.exception` | Exception | Ngoại lệ khi lỗi tải cấu hình game. |
| `ErrorCode` | Domain | `domain.exception` | Exception | Enum danh sách mã lỗi nghiệp vụ của hệ thống. |
| `RegisterTeamUseCase` | Application | `application.port.in`| Use Case | Interface định nghĩa ca sử dụng: Đăng ký đội. |
| `StartMatchUseCase` | Application | `application.port.in`| Use Case | Interface định nghĩa ca sử dụng: Khởi động trận. |
| `SubmitActionsUseCase`| Application | `application.port.in`| Use Case | Interface định nghĩa ca sử dụng: Nộp hành động và chạy Simulator. |
| `GetMatchStateUseCase`| Application | `application.port.in`| Use Case | Interface định nghĩa ca sử dụng: Lấy trạng thái trận đấu. |
| `MatchStateStorePort`| Application | `application.port.out`| Port (Outbound) | Interface cổng ra để lưu trữ/tải trạng thái trận đấu. |
| `MatchConfigLoaderPort`| Application | `application.port.out`| Port (Outbound) | Interface cổng ra để nạp file cấu hình game. |
| `MatchApplicationService`| Application | `application.service`| Application Service | Lớp thực thi điều phối chính cho tất cả các Use Case. |
| `ActionMapper` | Application | `application.mapper` | Mapper | Ánh xạ DTO Request/Response sang Domain và ngược lại. |
| `ActionRequest` | Application | `application.dto` | DTO | Record request hành động của Agent. |
| `ActionResponse` | Application | `application.dto` | DTO | Record response hành động của Agent. |
| `AgentActionPlanRequest`| Application | `application.dto` | DTO | Record request kế hoạch của Agent. |
| `AgentActionPlanResponse`| Application | `application.dto` | DTO | Record response kế hoạch của Agent. |
| `AgentResponse` | Application | `application.dto` | DTO | Record response thông tin Agent. |
| `CellResponse` | Application | `application.dto` | DTO | Record response thông tin ô bản đồ. |
| `DayActionRequest` | Application | `application.dto` | DTO | Record request nộp hành động của một ngày. |
| `DayActionResponse`| Application | `application.dto` | DTO | Record response kết quả nộp của một ngày. |
| `MatchStateResponse`| Application | `application.dto` | DTO | Record response trạng thái trận đấu. |
| `TeamActionRequest` | Application | `application.dto` | DTO | Record request hành động của một đội (giữ nguyên). |
| `TeamActionResponse`| Application | `application.dto` | DTO | Record response hành động của một đội (giữ nguyên). |
| `TeamRegisterRequest`| Application | `application.dto` | DTO | Record request đăng ký đội. |
| `TeamResponse` | Application | `application.dto` | DTO | Record response thông tin đội chơi. |
| `ErrorResponse` | Application | `application.dto` | DTO | Record cấu trúc phản hồi lỗi API. |
| `ValidationErrorDetail`| Application | `application.dto`| DTO | Record chi tiết lỗi Validation. |
| `MatchController` | Adapter | `adapter.in.rest` | Adapter | REST Controller tiếp nhận API. |
| `GlobalExceptionHandler`| Adapter | `adapter.in.rest` | Adapter | Handler bắt ngoại lệ và dịch sang HTTP response. |
| `InMemoryMatchStateRepository`| Adapter | `adapter.out.persistence`| Adapter | Triển khai lưu trữ `MatchState` in-memory. |
| `FileMatchConfigLoader`| Adapter | `adapter.out.loader`| Adapter | Triển khai đọc cấu hình game từ file vật lý. |
| `AdapterBeanConfig` | Adapter | `adapter.out.configuration`| Configuration | Khởi tạo Bean của Application Service trong Spring context. |
| `AppConfig` | Infrastructure| `infrastructure.configuration`| Configuration | Cấu hình Spring Context chung. |
| `WebConfig` | Infrastructure| `infrastructure.configuration`| Configuration | Cấu hình Web MVC và đăng ký Interceptor. |
| `RateLimiterInterceptor`| Infrastructure| `infrastructure.interceptor`| Interceptor | Bộ lọc kiểm soát tần suất API (Rate Limiting). |
| `SchedulerConfig` | Infrastructure| `infrastructure.scheduler`| Configuration | Cấu hình Spring Scheduler chạy ngầm để chuyển ngày tự động. |
| `FileUtils` | Infrastructure| `infrastructure.util` | Utility | Đọc nội dung tệp tin cấu hình từ đĩa. |
| `HexudonApplication` | Infrastructure| `com.naprock.hexudon`| Configuration | Lớp khởi chạy Spring Boot Application (nằm ở gốc). |

---

## 2. Đề xuất chia tách và cải tiến cấu trúc Class

Để tuân thủ hoàn toàn Hexagonal Architecture mà không làm thay đổi logic nghiệp vụ hiện có, chúng tôi thực hiện các đề xuất phân tách trách nhiệm sau:

### 2.1. Phân tách `MatchManager`
- **Hiện trạng:** `MatchManager` vừa giữ cấu hình (`MatchConfig`), vừa nắm giữ trạng thái trận đấu (`MatchState`), trực tiếp khởi tạo các Engine và tự quản lý luồng xử lý. Điều này vi phạm nghiêm trọng tính đơn nhiệm (Single Responsibility) và quy tắc đảo ngược phụ thuộc.
- **Giải pháp phân tách:**
  - Chuyển `MatchConfig` và `MatchState` thành các Value Objects nằm hoàn toàn trong Domain Core.
  - Tạo Interface các Port đầu vào (`RegisterTeamUseCase`, `StartMatchUseCase`, `SubmitActionsUseCase`, `GetMatchStateUseCase`).
  - Tạo `MatchApplicationService` đóng vai trò là Application Service điều phối luồng: nạp trạng thái qua Outbound Port, ủy quyền mô phỏng cho các Domain Engine, và lưu lại qua Outbound Port.
  - Tạo Outbound Port `MatchStateStorePort` để loại bỏ việc lưu trữ in-memory cứng nhắc khỏi lõi.

### 2.2. Phân tách `MatchConfigLoader`
- **Hiện trạng:** `MatchConfigLoader` được gọi trực tiếp bởi `MatchManager` để đọc file từ đĩa cứng. Điều này khiến tầng điều phối nghiệp vụ phụ thuộc trực tiếp vào đĩa cứng (I/O).
- **Giải pháp phân tách:**
  - Tạo Outbound Port `MatchConfigLoaderPort` ở tầng Application để định nghĩa hành vi tải cấu hình.
  - Chuyển đổi `MatchConfigLoader` hiện tại thành `FileMatchConfigLoader` ở tầng Adapter (`adapter.out.loader`), triển khai `MatchConfigLoaderPort`.
  - Application Service sẽ gọi `MatchConfigLoaderPort` để lấy cấu hình, giúp lõi hệ thống hoàn toàn độc lập với I/O.

# BẢN ĐỒ PHÂN BỔ PACKAGE GIAI ĐOẠN 4

Tài liệu này đặc tả cấu trúc package chi tiết của các tính năng mới trong Giai đoạn 4, tuân thủ nghiêm ngặt mô hình kiến trúc Hexagonal (Ports and Adapters) và thiết kế hướng miền (Domain-Driven Design).

---

## 1. Bản đồ Package và Vị trí Lưu trữ của các thành phần

Dưới đây là sơ đồ phân bổ các lớp thiết kế mới vào cấu trúc package của dự án:

### Tầng Domain (Domain Layer)
Tầng trung tâm chứa toàn bộ nghiệp vụ cốt lõi, hoàn toàn độc lập và không phụ thuộc vào bất kỳ thư viện framework bên ngoài nào:
*   `com.naprock.hexudon.domain.model.traffic`
    *   Chứa các thực thể, aggregate và value object liên quan tới lưu lượng giao thông.
    *   Thành phần: `TrafficFlow` (Entity), `RoadTrafficState` (Enum), `TrafficThreshold` (Value Object), `TrafficLevel` (Enum), `TrafficSnapshot` (Value Object).
*   `com.naprock.hexudon.domain.model.movement`
    *   Chứa các mô hình tính toán chi phí di chuyển.
    *   Thành phần: `MovementCost` (Value Object).
*   `com.naprock.hexudon.domain.model.score`
    *   Chứa mô hình điểm số của đội chơi và trận đấu.
    *   Thành phần: `TeamScore` (Entity), `MatchScore` (Entity), `UdonType` (Value Object).
*   `com.naprock.hexudon.domain.model.ranking`
    *   Chứa tiêu chí xếp hạng.
    *   Thành phần: `RankingCriteria` (Value Object).
*   `com.naprock.hexudon.domain.model.history`
    *   Chứa các mô hình lịch sử và sự kiện của trận đấu.
    *   Thành phần: `TurnHistory` (Entity), `GameEvent` (Entity), `ApiCommunicationLog` (Entity).
*   `com.naprock.hexudon.domain.model.recovery`
    *   Chứa mô hình hồi phục và lưu trữ tạm thời trạng thái trận đấu.
    *   Thành phần: `RecoveryPoint` (Entity).
*   `com.naprock.hexudon.domain.service`
    *   Chứa các Domain Services thực thi nghiệp vụ liên quan nhiều Aggregate.
    *   Thành phần: `TrafficCalculator` (Domain Service), `MovementCostCalculator` (Domain Service), `RankingService` (Domain Service).

### Tầng Application (Application Layer)
Tầng điều phối luồng nghiệp vụ, giao tiếp với tầng Domain và định nghĩa các Port cho Adapter:
*   `com.naprock.hexudon.application.port.in.traffic`
    *   Inbound Port cho việc tính toán giao thông.
    *   Thành phần: `CalculateTrafficUseCase` (Interface).
*   `com.naprock.hexudon.application.port.in.scoring`
    *   Inbound Port cho tính điểm và xếp hạng.
    *   Thành phần: `UpdateScoreUseCase` (Interface), `GetRankingUseCase` (Interface).
*   `com.naprock.hexudon.application.port.in.history`
    *   Inbound Port truy vấn lịch sử và logs.
    *   Thành phần: `QueryHistoryUseCase` (Interface), `QueryApiLogUseCase` (Interface).
*   `com.naprock.hexudon.application.port.in.recovery`
    *   Inbound Port xử lý phục hồi trận đấu.
    *   Thành phần: `MatchRecoveryUseCase` (Interface).
*   `com.naprock.hexudon.application.port.in.concurrency`
    *   Inbound Port xử lý xếp hàng request.
    *   Thành phần: `SubmitAgentActionUseCase` (Interface).
*   `com.naprock.hexudon.application.port.out.traffic`
    *   Outbound Port (SPI) lưu trữ dữ liệu giao thông.
    *   Thành phần: `TrafficRepository` (Interface).
*   `com.naprock.hexudon.application.port.out.history`
    *   Outbound Port lưu trữ lịch sử game và nhật ký API.
    *   Thành phần: `GameEventRepository` (Interface), `ApiCommunicationLogRepository` (Interface).
*   `com.naprock.hexudon.application.port.out.recovery`
    *   Outbound Port lưu trữ snapshot trạng thái trận đấu.
    *   Thành phần: `MatchSnapshotRepository` (Interface).
*   `com.naprock.hexudon.application.service`
    *   Các Application Services điều phối và quản lý giao dịch (Transaction).
    *   Thành phần: `TrafficCalculationService`, `ScoringAndRankingService`, `HistoryQueryService`, `CommunicationLogService`, `MatchRecoveryService`, `ConcurrentActionService`.
*   `com.naprock.hexudon.application.dto`
    *   Chứa các Request/Response DTO mức ứng dụng và Command/Query Objects.
    *   Thành phần: `SubmitActionCommand`, `MatchStateResponse`, `RankingResponse`, `TeamScoreResponse`.
*   `com.naprock.hexudon.application.mapper`
    *   Các mapper chuyển đổi cấu trúc dữ liệu.
    *   Thành phần: `ScoreMapper`, `RankingMapper`, `HistoryMapper`.

### Tầng Adapter (Adapter Layer)
Tầng triển khai kỹ thuật kết nối ngoại vi (Inbound qua REST, Outbound qua DB/Queue):
*   `com.naprock.hexudon.adapter.in.rest`
    *   REST Controllers tiếp nhận các yêu cầu HTTP.
    *   Thành phần: `MatchController`, `ScoreController`, `RankingController`, `HistoryController`.
    *   Interceptor: `ApiCommunicationInterceptor` (REST Adapter Interceptor).
*   `com.naprock.hexudon.adapter.out.persistence.traffic`
    *   Triển khai lưu trữ thông tin giao thông.
    *   Thành phần: `TrafficPersistenceAdapter` (implement `TrafficRepository`), `TrafficEntity` (In-Memory).
*   `com.naprock.hexudon.adapter.out.persistence.history`
    *   Triển khai lưu trữ lịch sử game và log mạng.
    *   Thành phần: `HistoryPersistenceAdapter`, `ApiLogPersistenceAdapter`, `GameEventEntity`, `ApiLogEntity`.
*   `com.naprock.hexudon.adapter.out.persistence.recovery`
    *   Triển khai lưu trữ snapshot trạng thái.
    *   Thành phần: `MatchSnapshotPersistenceAdapter`, `MatchSnapshotEntity`.
*   `com.naprock.hexudon.adapter.out.persistence.score`
    *   Triển khai lưu trữ điểm số.
    *   Thành phần: `ScorePersistenceAdapter`, `TeamScoreEntity`.

### Tầng Infrastructure (Infrastructure Layer)
Các cấu hình Spring Boot nâng cao cho Phase 4:
*   `com.naprock.hexudon.infrastructure.queue`
    *   Cấu hình hàng đợi xử lý đa luồng: `TurnExecutionQueue` (ThreadPool/BlockingQueue configuration).
*   `com.naprock.hexudon.infrastructure.ordering`
    *   Cấu hình dịch vụ định trình tự: `RequestOrderingService` (đo đạc Server Timestamp chính xác).

---

## 2. Nguyên tắc Thiết kế và Lý do phân bổ Package

### A. Nguyên tắc Cô lập Domain (Domain Isolation)
*   **Không rò rỉ công nghệ**: Tất cả các package con thuộc `domain.*` (như `domain.model.traffic`, `domain.service`) hoàn toàn không chứa bất kỳ Spring annotations nào như `@Service`, `@Component`, `@Repository`, hoặc Spring-data annotations như `@Entity`, `@Id`, `@Document`.
*   **Độc lập kiểm thử**: Các nghiệp vụ tính toán lưu lượng giao thông phức tạp, chi phí đi đường, so sánh điểm số xếp hạng có thể viết Unit Test cực kỳ dễ dàng bằng JUnit thuần túy mà không cần khởi động Spring Context, giảm thời gian chạy test từ vài giây xuống mili-giây.

### B. Tính Đóng gói và Che giấu thông tin (Encapsulation)
*   **Độc lập module dọc**: Chia các package con theo nhóm nghiệp vụ (`traffic`, `scoring`, `recovery`) giúp các lớp có mối liên hệ mật thiết được ở cạnh nhau. Việc này ngăn chặn sự phụ thuộc chéo lộn xộn giữa các thành phần không liên quan.
*   **Bảo vệ trạng thái qua Inbound/Outbound Ports**: Tầng Adapter Rest muốn cập nhật điểm số bắt buộc phải gọi qua Inbound Port `UpdateScoreUseCase` thuộc tầng Application. Tầng Application sau đó tương tác với Domain Model và lưu trữ kết quả qua Outbound Port `TeamScoreRepository`. Tầng Adapter Persistence thực thi lưu trữ nhưng không được phép trực tiếp thay đổi quy tắc tính điểm của Domain. Việc này bảo vệ Domain khỏi các tác động ngoài ý muốn từ bên ngoài.

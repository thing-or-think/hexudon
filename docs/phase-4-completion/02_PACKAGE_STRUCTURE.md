# THIẾT KẾ CẤU TRÚC GÓI (PACKAGE STRUCTURE) - GIAI ĐOẠN 4

Tài liệu này đặc tả bản đồ phân bổ cấu trúc thư mục (package structure) cho toàn bộ các thành phần mới trong Giai đoạn 4, tuân thủ nguyên tắc cô lập nghiệp vụ (Domain Isolation) và tính đóng gói (Encapsulation) của kiến trúc Lục giác (Hexagonal Architecture).

---

## 1. Bản đồ phân bổ gói chi tiết

Toàn bộ các lớp và thành phần được thêm mới trong Phase 4 sẽ được phân bổ chính xác theo sơ đồ gói dưới đây:

### 1.1. Lớp Domain (Domain Layer)
Chứa các logic nghiệp vụ thuần túy, không phụ thuộc Spring Boot hay bất kỳ thư viện bên ngoài nào.

*   `com.naprock.hexudon.domain.model.traffic`
    *   Chứa các thực thể và Value Object liên quan đến giao thông: `TrafficFlow`, `TrafficThreshold`, `TrafficLevel`, `TrafficSnapshot`, `TrafficHistory`.
*   `com.naprock.hexudon.domain.service.traffic`
    *   Chứa Domain Service tính toán giao thông: `TrafficCalculator`.
*   `com.naprock.hexudon.domain.model.cost`
    *   Chứa Value Object đại diện cho chi phí di chuyển: `MovementCost`.
*   `com.naprock.hexudon.domain.service.cost`
    *   Chứa Domain Service tính chi phí di chuyển dựa trên địa hình và giao thông: `MovementCostCalculator`.
*   `com.naprock.hexudon.domain.model.scoring`
    *   Chứa các Entity quản lý điểm số: `TeamScore`, `MatchScore`, và Value Object `UdonType`.
*   `com.naprock.hexudon.domain.service.ranking`
    *   Chứa Domain Service phân hạng và so sánh các đội chơi: `RankingService`, và Value Object tiêu chí xếp hạng `RankingCriteria`.
*   `com.naprock.hexudon.domain.model.history`
    *   Chứa Entity ghi nhận lịch sử đấu: `TurnHistory`, `GameEvent`.
*   `com.naprock.hexudon.domain.model.logging`
    *   Chứa Entity ghi nhận nhật ký API: `ApiCommunicationLog`.
*   `com.naprock.hexudon.domain.model.recovery`
    *   Chứa Entity ảnh chụp trạng thái và điểm phục hồi: `MatchSnapshot`, `RecoveryPoint`.

### 1.2. Lớp Application (Application Layer)
Điều phối các luồng nghiệp vụ và định nghĩa các cổng giao tiếp (Ports).

*   `com.naprock.hexudon.application.port.in.scoring`
    *   Use Case truy xuất điểm số và xếp hạng: `GetScoreUseCase`, `GetRankingUseCase`.
*   `com.naprock.hexudon.application.port.in.history`
    *   Use Case truy xuất lịch sử trận đấu: `GetMatchHistoryUseCase`.
*   `com.naprock.hexudon.application.port.in.action`
    *   Use Case tiếp nhận và lập lịch hành động Agent: `SubmitAgentActionUseCase`.
*   `com.naprock.hexudon.application.port.out.scoring`
    *   Outbound Port (SPI) lưu trữ điểm số: `TeamScoreRepositoryPort`.
*   `com.naprock.hexudon.application.port.out.traffic`
    *   Outbound Port lưu trữ lịch sử giao thông: `TrafficRepositoryPort`.
*   `com.naprock.hexudon.application.port.out.history`
    *   Outbound Port ghi lịch sử sự kiện: `GameEventRepositoryPort`.
*   `com.naprock.hexudon.application.port.out.logging`
    *   Outbound Port ghi nhật ký giao tiếp mạng: `ApiCommunicationLogRepositoryPort`.
*   `com.naprock.hexudon.application.port.out.recovery`
    *   Outbound Port lưu trữ ảnh chụp trạng thái: `MatchSnapshotRepositoryPort`.
*   `com.naprock.hexudon.application.service.concurrency`
    *   Các dịch vụ xử lý đa luồng, hàng đợi hành động: `TurnExecutionQueue`, `RequestOrderingService`.
*   `com.naprock.hexudon.application.service.logging`
    *   Dịch vụ xử lý ghi nhật ký mạng: `CommunicationLogService`.

### 1.3. Lớp Adapter (Adapter Layer)
Kết nối ứng dụng với các công nghệ bên ngoài (REST API, Database).

*   `com.naprock.hexudon.adapter.in.rest.scoring`
    *   REST Controller cho điểm số và xếp hạng: `ScoreController`, `RankingController`.
*   `com.naprock.hexudon.adapter.in.rest.history`
    *   REST Controller cho lịch sử đấu và nhật ký: `HistoryController`, `LogController`.
*   `com.naprock.hexudon.adapter.in.rest.action`
    *   REST Controller tiếp nhận hành động Agent: `MatchController`.
*   `com.naprock.hexudon.adapter.in.rest.interceptor`
    *   Interceptor ghi nhận và đo lường độ trễ mạng: `ApiLoggingInterceptor`.
*   `com.naprock.hexudon.adapter.out.persistence.traffic`
    *   Cấu phần lưu trữ dữ liệu giao thông xuống DB: `TrafficPersistenceAdapter`, `TrafficJpaEntity`, `SpringDataTrafficRepository`.
*   `com.naprock.hexudon.adapter.out.persistence.history`
    *   Cấu phần lưu trữ lịch sử trận đấu: `HistoryPersistenceAdapter`, `GameEventJpaEntity`.
*   `com.naprock.hexudon.adapter.out.persistence.recovery`
    *   Cấu phần lưu trữ ảnh chụp trạng thái: `RecoveryPersistenceAdapter`, `MatchSnapshotJpaEntity`.

---

## 2. Lý do phân bổ cấu trúc

Việc phân bổ chi tiết các gói trên dựa trên hai nguyên tắc kiến trúc cốt lõi:

### 2.1. Nguyên tắc cô lập Domain (Domain Isolation)
*   **Không phụ thuộc công nghệ:** Toàn bộ các package thuộc `com.naprock.hexudon.domain` chỉ chứa các lớp Java thuần túy. Ví dụ, `TrafficCalculator` tính toán lưu lượng giao thông dựa trên các thực thể nghiệp vụ mà không cần biết dữ liệu được lưu bằng JPA hay MongoDB.
*   **Bảo vệ quy tắc nghiệp vụ:** Logic tính toán chi phí di chuyển (`MovementCostCalculator`) nằm trọn vẹn trong Domain. Khi cấu hình thay đổi hoặc có thêm loại địa hình, chỉ cần thay đổi trong Domain mà không ảnh hưởng tới REST Controller hoặc DB Adapter.

### 2.2. Tính đóng gói và che giấu thông tin (Encapsulation)
*   **Giao tiếp qua Port:** Lớp REST Adapter (`com.naprock.hexudon.adapter.in.rest`) không bao giờ thao tác trực tiếp với Database. Nó bắt buộc phải gọi thông qua Inbound Ports (ví dụ: `GetScoreUseCase`).
*   **Độc lập lưu trữ:** Persistence Adapter (`com.naprock.hexudon.adapter.out.persistence`) chịu trách nhiệm ánh xạ (mapping) giữa Domain Model và Database Entity (JPA Entity). Database Entity có các annotation của Hibernate (như `@Entity`, `@Table`) hoàn toàn bị che giấu khỏi lớp Domain. Nếu cấu trúc bảng thay đổi, chỉ lớp Adapter này thay đổi, Domain Model giữ nguyên.
*   **Sử dụng DTO độc lập:** Các package REST Controller định nghĩa riêng các Request/Response DTO để phục vụ Validation và cấu trúc dữ liệu JSON. Điều này ngăn chặn việc lộ các thuộc tính nội bộ của Domain Entity ra ngoài API công cộng.

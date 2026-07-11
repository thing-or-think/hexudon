# THỨ TỰ XÂY DỰNG VÀ TÍCH HỢP MÃ NGUỒN (IMPLEMENTATION ROADMAP) - GIAI ĐOẠN 4

Tài liệu này vạch ra sơ đồ lộ trình phát triển gồm 8 phân đoạn chiến lược, sắp xếp theo thứ tự tích hợp kỹ thuật tối ưu từ dưới lên (Bottom-up). Mỗi bước xác định rõ cấu phần cần tạo, gói bị ảnh hưởng, mối quan hệ phụ thuộc và chiến lược kiểm thử.

---

## Phân đoạn 4.1: Hệ thống giao thông động (Traffic System)
*   **Thứ tự ưu tiên:** 1 (Nền tảng hạ tầng tính chi phí).
*   **Tập tin cần tạo mới/Chỉnh sửa:**
    *   `RoadTrafficState.java` (Enum) - `com.naprock.hexudon.domain.model.traffic`
    *   `TrafficFlow.java` (Value Object) - `com.naprock.hexudon.domain.model.traffic`
    *   `TrafficThreshold.java` (Value Object) - `com.naprock.hexudon.domain.model.traffic`
    *   `TrafficHistory.java` (Entity) - `com.naprock.hexudon.domain.model.traffic`
    *   `TrafficSnapshot.java` (Value Object) - `com.naprock.hexudon.domain.model.traffic`
    *   `TrafficCalculator.java` (Domain Service) - `com.naprock.hexudon.domain.service.traffic`
    *   `TrafficRepositoryPort.java` (Outbound Port) - `com.naprock.hexudon.application.port.out.traffic`
    *   `TrafficPersistenceAdapter.java` (Outbound Adapter) - `com.naprock.hexudon.adapter.out.persistence.traffic`
*   **Gói bị ảnh hưởng trực tiếp:** `com.naprock.hexudon.domain.model.traffic`, `com.naprock.hexudon.domain.service.traffic`, `com.naprock.hexudon.application.port.out.traffic`, `com.naprock.hexudon.adapter.out.persistence.traffic`.
*   **Mối quan hệ phụ thuộc (Dependency):** Không phụ thuộc module nào khác.
*   **Chiến lược viết Test:**
    *   **Unit Test:** Tạo bộ dữ liệu lịch sử giả định cho các lượt `T=1`, `T=2`, `T>=3` và kiểm tra logic tính Calculated Flow của `TrafficCalculator`. Kiểm tra điều kiện biên khi số đội chơi bằng 0.
    *   **Integration Test:** Kiểm tra việc lưu trữ và truy vấn `TrafficHistory` thông qua Persistence Adapter sử dụng cơ sở dữ liệu nhúng H2.

---

## Phân đoạn 4.2: Chi phí di chuyển địa hình (Movement Cost)
*   **Thứ tự ưu tiên:** 2.
*   **Tập tin cần tạo mới/Chỉnh sửa:**
    *   `MovementCost.java` (Value Object) - `com.naprock.hexudon.domain.model.cost`
    *   `MovementCostCalculator.java` (Domain Service) - `com.naprock.hexudon.domain.service.cost`
    *   Chỉnh sửa `PatrolAgent.java` và `RefuelAgent.java` để gọi `MovementCostCalculator`.
*   **Gói bị ảnh hưởng trực tiếp:** `com.naprock.hexudon.domain.model.cost`, `com.naprock.hexudon.domain.service.cost`, `com.naprock.hexudon.domain.model.entity`.
*   **Mối quan hệ phụ thuộc (Dependency):** Phụ thuộc vào Phân đoạn 4.1 (cần biết trạng thái `RoadTrafficState`).
*   **Chiến lược viết Test:**
    *   **Unit Test:** Kiểm thử `MovementCostCalculator` với 4 loại địa hình và 3 trạng thái giao thông khác nhau. Đảm bảo ô `POND` ném ra ngoại lệ quy tắc chơi. Đảm bảo chi phí chốt cố định khi Agent bắt đầu di chuyển.

---

## Phân đoạn 4.3: Hệ thống tính điểm (Scoring System)
*   **Thứ tự ưu tiên:** 3.
*   **Tập tin cần tạo mới/Chỉnh sửa:**
    *   `UdonType.java` (Value Object) - `com.naprock.hexudon.domain.model.scoring`
    *   `TeamScore.java` (Entity) - `com.naprock.hexudon.domain.model.scoring`
    *   `MatchScore.java` (Entity) - `com.naprock.hexudon.domain.model.scoring`
    *   `TeamScoreRepositoryPort.java` (Outbound Port) - `com.naprock.hexudon.application.port.out.scoring`
    *   Chỉnh sửa `Team.java` để tích hợp `TeamScore`.
*   **Gói bị ảnh hưởng trực tiếp:** `com.naprock.hexudon.domain.model.scoring`, `com.naprock.hexudon.application.port.out.scoring`, `com.naprock.hexudon.domain.model.entity`.
*   **Mối quan hệ phụ thuộc (Dependency):** Độc lập.
*   **Chiến lược viết Test:**
    *   **Unit Test:** Kiểm tra các phép toán cộng điểm Udon, đếm chủng loại mì Udon độc nhất bằng Set, lưu trữ khối lượng mì hàng ngày.
    *   **Architecture Test (ArchUnit):** Kiểm tra cấu trúc package để đảm bảo các lớp scoring trong Domain không tham chiếu tới lớp Adapter hoặc Spring Framework.

---

## Phân đoạn 4.4: Hệ thống xếp hạng (Ranking System)
*   **Thứ tự ưu tiên:** 4.
*   **Tập tin cần tạo mới/Chỉnh sửa:**
    *   `RankingCriteria.java` (Value Object) - `com.naprock.hexudon.domain.service.ranking`
    *   `RankingService.java` (Domain Service) - `com.naprock.hexudon.domain.service.ranking`
*   **Gói bị ảnh hưởng trực tiếp:** `com.naprock.hexudon.domain.service.ranking`.
*   **Mối quan hệ phụ thuộc (Dependency):** Phụ thuộc Phân đoạn 4.3 (`TeamScore`).
*   **Chiến lược viết Test:**
    *   **Unit Test:** Tạo các kịch bản so sánh điểm giữa các đội (Đội A nhiều Udon độc nhất hơn, Đội B có tích lũy ngày cao hơn, Đội C có servings lớn hơn, Đội D phản hồi nhanh hơn, và trường hợp tung xúc xắc ngẫu nhiên). Xác thực thuật toán sắp xếp thứ hạng hoạt động đúng thứ tự ưu tiên tuyệt đối.

---

## Phân đoạn 4.5: Lịch sử trận đấu & Nhật ký API (Logging)
*   **Thứ tự ưu tiên:** 5.
*   **Tập tin cần tạo mới/Chỉnh sửa:**
    *   `GameEvent.java` (Entity) & `TurnHistory.java` (Entity) - `com.naprock.hexudon.domain.model.history`
    *   `GameEventRepositoryPort.java` (Outbound Port) - `com.naprock.hexudon.application.port.out.history`
    *   `HistoryPersistenceAdapter.java` (Outbound Adapter) - `com.naprock.hexudon.adapter.out.persistence.history`
    *   `ApiCommunicationLog.java` (Entity) - `com.naprock.hexudon.domain.model.logging`
    *   `ApiCommunicationLogRepositoryPort.java` (Outbound Port) - `com.naprock.hexudon.application.port.out.logging`
    *   `CommunicationLogService.java` (Application Service) - `com.naprock.hexudon.application.service.logging`
    *   `ApiLoggingInterceptor.java` (Adapter Interceptor) - `com.naprock.hexudon.adapter.in.rest.interceptor`
*   **Gói bị ảnh hưởng trực tiếp:** `com.naprock.hexudon.domain.model.history`, `com.naprock.hexudon.domain.model.logging`, `com.naprock.hexudon.application.service.logging`, `com.naprock.hexudon.adapter.in.rest.interceptor`.
*   **Mối quan hệ phụ thuộc (Dependency):** Độc lập.
*   **Chiến lược viết Test:**
    *   **Unit Test:** Kiểm tra việc tính toán hiệu hiệu số thời gian (`durationMs`) và kích thước payload trong `ApiLoggingInterceptor`.
    *   **Integration Test:** Xác minh việc ghi nhật ký API bất đồng bộ (Asynchronous logging) xuống DB không gây chậm trễ cho luồng chính.

---

## Phân đoạn 4.6: Khôi phục trạng thái (Recovery)
*   **Thứ tự ưu tiên:** 6.
*   **Tập tin cần tạo mới/Chỉnh sửa:**
    *   `MatchSnapshot.java` (Entity) - `com.naprock.hexudon.domain.model.recovery`
    *   `RecoveryPoint.java` (Value Object) - `com.naprock.hexudon.domain.model.recovery`
    *   `MatchSnapshotRepositoryPort.java` (Outbound Port) - `com.naprock.hexudon.application.port.out.recovery`
    *   `RecoveryPersistenceAdapter.java` (Outbound Adapter) - `com.naprock.hexudon.adapter.out.persistence.recovery`
*   **Gói bị ảnh hưởng trực tiếp:** `com.naprock.hexudon.domain.model.recovery`, `com.naprock.hexudon.application.port.out.recovery`, `com.naprock.hexudon.adapter.out.persistence.recovery`.
*   **Mối quan hệ phụ thuộc (Dependency):** Phụ thuộc các thực thể lưu trạng thái (Phân đoạn 4.1, 4.2, 4.3).
*   **Chiến lược viết Test:**
    *   **Integration Test:** Mô phỏng quy trình tạo Snapshot ở Turn 3. Khởi tạo một đối tượng `MatchState` mới trong bộ nhớ, kích hoạt quy trình phục hồi và so sánh hai đối tượng trước và sau phục hồi. Đảm bảo dữ liệu trùng khớp 100%.

---

## Phân đoạn 4.7: Quản lý đồng thời (Concurrency)
*   **Thứ tự ưu tiên:** 7.
*   **Tập tin cần tạo mới/Chỉnh sửa:**
    *   `TurnExecutionQueue.java` (Application Service)
    *   `RequestOrderingService.java` (Application Service)
*   **Gói bị ảnh hưởng trực tiếp:** `com.naprock.hexudon.application.service.concurrency`.
*   **Mối quan hệ phụ thuộc (Dependency):** Phụ thuộc các Use Cases hành động Agent.
*   **Chiến lược viết Test:**
    *   **Concurrency Stress Test:** Sử dụng công cụ `CountDownLatch` để kích hoạt 10 luồng gửi request đồng thời vào `TurnExecutionQueue`. Kiểm tra tính an toàn dữ liệu, đảm bảo không bị mất gói hành động nào và thứ tự sắp xếp dựa trên thời gian chuẩn hóa là hoàn toàn chính xác.

---

## Phân đoạn 4.8: Giao diện lập trình (API)
*   **Thứ tự ưu tiên:** 8 (Mảnh ghép cuối cùng).
*   **Tập tin cần tạo mới/Chỉnh sửa:**
    *   `MatchController.java`, `ScoreController.java`, `RankingController.java`, `HistoryController.java`, `LogController.java`.
*   **Gói bị ảnh hưởng trực tiếp:** `com.naprock.hexudon.adapter.in.rest` (các package con).
*   **Mối quan hệ phụ thuộc (Dependency):** Phụ thuộc vào các Inbound Use Cases của tất cả các phân đoạn trước.
*   **Chiến lược viết Test:**
    *   **API Spring MockMVC Test:** Kiểm thử các Endpoint HTTP. Kiểm tra tính hợp lệ của Request DTO Validation. Mô phỏng tình huống gửi request khi lượt đấu đã đóng và kiểm tra mã lỗi HTTP 400 Bad Request trả về.

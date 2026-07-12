# LỘ TRÌNH XÂY DỰNG VÀ TÍCH HỢP MÃ NGUỒN (IMPLEMENTATION ROADMAP)

Tài liệu này đặc tả thứ tự triển khai mã nguồn chi tiết gồm 8 phân đoạn chiến lược từ Phase 4.1 đến Phase 4.8, thiết lập mối quan hệ phụ thuộc và chiến lược kiểm thử cho từng giai đoạn.

---

## Phân đoạn 4.1: Hệ thống giao thông động (Traffic System)
*   **Các tập tin tạo mới**:
    *   `com.naprock.hexudon.domain.model.traffic.TrafficFlow`
    *   `com.naprock.hexudon.domain.model.traffic.RoadTrafficState`
    *   `com.naprock.hexudon.domain.model.traffic.TrafficThreshold`
    *   `com.naprock.hexudon.domain.model.traffic.TrafficLevel`
    *   `com.naprock.hexudon.domain.model.traffic.TrafficSnapshot`
    *   `com.naprock.hexudon.domain.service.TrafficCalculator`
    *   `com.naprock.hexudon.application.port.in.CalculateTurnEnvironmentUseCase`
    *   `com.naprock.hexudon.application.port.out.traffic.TrafficRepository`
    *   `com.naprock.hexudon.application.service.TurnEnvironmentService`
    *   `com.naprock.hexudon.adapter.out.persistence.traffic.TrafficPersistenceAdapter`
    *   `com.naprock.hexudon.adapter.out.persistence.traffic.TrafficEntity`
*   **Các package bị ảnh hưởng**: `domain.model.traffic`, `domain.service`, `application.port.in.traffic`, `application.port.out.traffic`, `application.service`, `adapter.out.persistence.traffic`.
*   **Mối quan hệ phụ thuộc**: `TrafficCalculationService` thực thi `CalculateTrafficUseCase`, phụ thuộc vào `TrafficCalculator` để tính toán và `TrafficRepository` để lưu trữ dữ liệu.
*   **Chiến lược Kiểm thử (Test Strategy)**:
    *   *Unit Test*: Viết bài test độc lập cho `TrafficCalculator` để kiểm chứng công thức tính lưu lượng trung bình với các trường hợp số bước dừng khác nhau.
    *   *Edge Case Test*: Kiểm tra phép chia khi tham số số đội bằng 0 (đảm bảo không ném ngoại lệ toán học).

---

## Phân đoạn 4.2: Chi phí di chuyển địa hình (Movement Cost)
*   **Các tập tin tạo mới**:
    *   `com.naprock.hexudon.domain.model.movement.MovementCost`
    *   `com.naprock.hexudon.domain.service.MovementCostCalculator`
*   **Các package bị ảnh hưởng**: `domain.model.movement`, `domain.service`.
*   **Mối quan hệ phụ thuộc**: Phụ thuộc trực tiếp vào các thực thể và enum của Phân đoạn 4.1 (`RoadTrafficState`).
*   **Chiến lược Kiểm thử (Test Strategy)**:
    *   *Unit Test*: Viết test cho `MovementCostCalculator` với 3 loại địa hình cơ bản và các trạng thái giao thông khác nhau, so sánh kết quả trả về `MovementCost` với cấu hình mẫu.
    *   *Architecture Test*: Xác minh `MovementCost` là Value Object bất biến (tất cả các trường đều là final và không có setter).

---

## Phân đoạn 4.3: Hệ thống tính điểm (Score System)
*   **Các tập tin tạo mới**:
    *   `com.naprock.hexudon.domain.model.score.TeamScore`
    *   `com.naprock.hexudon.domain.model.score.MatchScore`
    *   `com.naprock.hexudon.domain.model.score.UdonType`
    *   `com.naprock.hexudon.application.port.in.scoring.UpdateScoreUseCase`
    *   `com.naprock.hexudon.application.port.out.scoring.TeamScoreRepository`
    *   `com.naprock.hexudon.application.service.ScoringAndRankingService` (phần tính điểm)
    *   `com.naprock.hexudon.adapter.out.persistence.score.ScorePersistenceAdapter`
    *   `com.naprock.hexudon.adapter.out.persistence.score.TeamScoreEntity`
    *   `com.naprock.hexudon.application.mapper.ScoreMapper`
    *   `com.naprock.hexudon.application.dto.TeamScoreResponse`
*   **Các package bị ảnh hưởng**: `domain.model.score`, `application.port.in.scoring`, `application.port.out.scoring`, `application.service`, `adapter.out.persistence.score`, `application.mapper`, `application.dto`.
*   **Mối quan hệ phụ thuộc**: Độc lập với hệ thống di chuyển, nhưng sẽ được tích hợp vào luồng chạy chính của trận đấu.
*   **Chiến lược Kiểm thử (Test Strategy)**:
    *   *Unit Test*: Xác minh dữ liệu tích lũy Udon theo ngày hoạt động chính xác thông qua chuỗi hành động chuyển Turn.
    *   *Mocking Test*: Mock `TeamScoreRepository` để kiểm tra luồng lưu trữ điểm số xuống database ở cuối lượt.

---

## Phân đoạn 4.4: Hệ thống xếp hạng đấu trường (Ranking System)
*   **Các tập tin tạo mới**:
    *   `com.naprock.hexudon.domain.model.ranking.RankingCriteria`
    *   `com.naprock.hexudon.domain.service.RankingService`
    *   `com.naprock.hexudon.application.port.in.scoring.GetRankingUseCase`
    *   `com.naprock.hexudon.application.dto.RankingResponse`
    *   `com.naprock.hexudon.application.mapper.RankingMapper`
*   **Các package bị ảnh hưởng**: `domain.model.ranking`, `domain.service`, `application.dto`, `application.mapper`.
*   **Mối quan hệ phụ thuộc**: Phụ thuộc trực tiếp vào `TeamScore` (Phân đoạn 4.3).
*   **Chiến lược Kiểm thử (Test Strategy)**:
    *   *Unit Test*: Viết bộ dữ liệu kiểm thử phân hạng so sánh giữa hai đội với 5 trường hợp hòa tiêu chí trước (Anti-tie-break), bao gồm cả trường hợp tung xúc xắc ngẫu nhiên dựa trên mã băm trận đấu.

---

## Phân đoạn 4.5: Lịch sử trận đấu & Log mạng (Logging System)
*   **Các tập tin tạo mới**:
    *   `com.naprock.hexudon.domain.model.history.TurnHistory`
    *   `com.naprock.hexudon.domain.model.history.GameEvent`
    *   `com.naprock.hexudon.domain.model.history.ApiCommunicationLog`
    *   `com.naprock.hexudon.application.port.in.history.QueryHistoryUseCase`
    *   `com.naprock.hexudon.application.port.in.history.QueryApiLogUseCase`
    *   `com.naprock.hexudon.application.port.out.history.GameEventRepository`
    *   `com.naprock.hexudon.application.port.out.history.ApiCommunicationLogRepository`
    *   `com.naprock.hexudon.application.service.HistoryQueryService`
    *   `com.naprock.hexudon.application.service.CommunicationLogService`
    *   `com.naprock.hexudon.adapter.out.persistence.history.HistoryPersistenceAdapter`
    *   `com.naprock.hexudon.adapter.out.persistence.history.ApiLogPersistenceAdapter`
    *   `com.naprock.hexudon.adapter.out.persistence.history.GameEventEntity`
    *   `com.naprock.hexudon.adapter.out.persistence.history.ApiLogEntity`
    *   `com.naprock.hexudon.application.mapper.HistoryMapper`
    *   `com.naprock.hexudon.adapter.in.rest.ApiCommunicationInterceptor`
*   **Các package bị ảnh hưởng**: `domain.model.history`, `application.port.in.history`, `application.port.out.history`, `application.service`, `adapter.out.persistence.history`, `adapter.in.rest`.
*   **Mối quan hệ phụ thuộc**: Chặn toàn bộ REST Adapter API để ghi log độ trễ, và được gọi từ luồng mô phỏng lượt để lưu lịch sử sự kiện game.
*   **Chiến lược Kiểm thử (Test Strategy)**:
    *   *Architecture Test*: Sử dụng ArchUnit để kiểm tra tầng Domain không tham chiếu tới các lớp Entity ở tầng Persistence và các Spring annotations.

---

## Phân đoạn 4.6: Phục hồi trận đấu (Recovery System)
*   **Các tập tin tạo mới**:
    *   `com.naprock.hexudon.domain.model.recovery.RecoveryPoint`
    *   `com.naprock.hexudon.domain.model.recovery.MatchSnapshot`
    *   `com.naprock.hexudon.application.port.in.recovery.MatchRecoveryUseCase`
    *   `com.naprock.hexudon.application.port.out.recovery.MatchSnapshotRepository`
    *   `com.naprock.hexudon.application.service.MatchRecoveryService`
    *   `com.naprock.hexudon.adapter.out.persistence.recovery.MatchSnapshotPersistenceAdapter`
    *   `com.naprock.hexudon.adapter.out.persistence.recovery.MatchSnapshotEntity`
*   **Các package bị ảnh hưởng**: `domain.model.recovery`, `application.port.in.recovery`, `application.port.out.recovery`, `application.service`, `adapter.out.persistence.recovery`.
*   **Mối quan hệ phụ thuộc**: Phụ thuộc sâu vào `MatchState`, `TrafficFlow`, `TeamScore` để chụp và khôi phục trạng thái.
*   **Chiến lược Kiểm thử (Test Strategy)**:
    *   *Integration Test*: Mô phỏng ghi nhận 5 lượt chơi, kích hoạt sập nguồn giả lập ở lượt 3, chạy phục hồi tự động, xác minh dữ liệu ở lượt 2 được nạp lại hoàn chỉnh và lượt 3 bị dọn dẹp sạch sẽ.

---

## Phân đoạn 4.7: Xử lý đồng thời & Xếp hàng (Concurrency System)
*   **Các tập tin tạo mới**:
    *   `com.naprock.hexudon.application.port.in.concurrency.SubmitAgentActionUseCase`
    *   `com.naprock.hexudon.application.service.ConcurrentActionService`
    *   `com.naprock.hexudon.infrastructure.queue.TurnExecutionQueue`
    *   `com.naprock.hexudon.infrastructure.ordering.RequestOrderingService`
*   **Các package bị ảnh hưởng**: `application.port.in.concurrency`, `application.service`, `infrastructure.queue`, `infrastructure.ordering`.
*   **Mối quan hệ phụ thuộc**: Nhận yêu cầu từ Controllers và đẩy vào Domain Aggregate thông qua hàng đợi an toàn luồng.
*   **Chiến lược Kiểm thử (Test Strategy)**:
    *   *Concurrency Unit Test*: Sử dụng `ExecutorService` để chạy song song 50 luồng nộp bài cùng lúc cho cùng một đội chơi, kiểm tra xem chỉ có duy nhất 1 bản ghi cuối cùng tồn tại trong hàng đợi.

---

## Phân đoạn 4.8: Cổng giao tiếp ngoại vi REST (API Endpoints)
*   **Các tập tin tạo mới**:
    *   `com.naprock.hexudon.adapter.in.rest.MatchController`
    *   `com.naprock.hexudon.adapter.in.rest.ScoreController`
    *   `com.naprock.hexudon.adapter.in.rest.RankingController`
    *   `com.naprock.hexudon.adapter.in.rest.HistoryController`
*   **Các package bị ảnh hưởng**: `adapter.in.rest`.
*   **Mối quan hệ phụ thuộc**: Đầu vào giao tiếp REST, gọi trực tiếp các Inbound Ports đã tạo ở các phân đoạn trước.
*   **Chiến lược Kiểm thử (Test Strategy)**:
    *   *REST Integration Test*: Sử dụng `MockMvc` để gọi thử nghiệm các API, kiểm định dữ liệu phản hồi khớp định dạng bảng thiết kế DTO và kiểm thử các mã lỗi HTTP khi gửi sai tham số.

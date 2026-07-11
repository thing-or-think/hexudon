# DANH MỤC CÁC LỚP THIẾT KẾ (CLASS LIST) - GIAI ĐOẠN 4

Tài liệu này tổng hợp toàn bộ các lớp (Class), Giao diện (Interface), Kiểu liệt kê (Enum) và Đối tượng giá trị (Value Object) được thêm mới hoặc chỉnh sửa trong Giai đoạn 4, phân loại chi tiết theo từng module nghiệp vụ.

---

## 1. Module Nghiệp vụ: Hệ thống giao thông động (Traffic Flow)

| Tên Thành phần | Kiểu | Package Hoàn chỉnh | Mô tả chức năng ngắn gọn |
| :--- | :--- | :--- | :--- |
| `TrafficFlow` | Value Object | `com.naprock.hexudon.domain.model.traffic` | Đại diện cho lưu lượng giao thông tại một tọa độ ô đường đi. |
| `RoadTrafficState` | Enum | `com.naprock.hexudon.domain.model.traffic` | Các trạng thái giao thông đường bộ: `Smooth`, `Congested`, `Traffic Jam`. |
| `TrafficLevel` | Enum | `com.naprock.hexudon.domain.model.traffic` | Các mức phân cấp lưu lượng giao thông: `Low`, `Medium`, `High`. |
| `TrafficThreshold` | Value Object | `com.naprock.hexudon.domain.model.traffic` | Định nghĩa ngưỡng lưu lượng để phân định trạng thái kẹt xe. |
| `TrafficSnapshot` | Value Object | `com.naprock.hexudon.domain.model.traffic` | Ảnh chụp nhanh dữ liệu giao thông của toàn bộ bản đồ tại một lượt (Turn). |
| `TrafficHistory` | Entity | `com.naprock.hexudon.domain.model.traffic` | Lưu trữ tổng số lượt dừng chân của Agent tại tọa độ đường trong quá khứ. |
| `TrafficCalculator` | Domain Service | `com.naprock.hexudon.domain.service.traffic` | Chứa logic thuần túy tính toán Calculated Flow và phân định RoadTrafficState. |
| `TrafficCalculationService` | Application Service | `com.naprock.hexudon.application.service.traffic` | Dịch vụ điều phối tính toán giao thông khi kết thúc Turn và chuẩn bị Turn tiếp theo. |
| `TrafficRepositoryPort` | Outbound Port (SPI) | `com.naprock.hexudon.application.port.out.traffic` | Giao diện cổng ra để truy vấn và lưu trữ dữ liệu lịch sử giao thông. |
| `TrafficPersistenceAdapter` | Outbound Adapter | `com.naprock.hexudon.adapter.out.persistence.traffic` | Triển khai lưu trữ lịch sử giao thông xuống cơ sở dữ liệu qua Spring Data JPA. |

---

## 2. Module Nghiệp vụ: Chi phí di chuyển địa hình (Terrain & Movement Cost)

| Tên Thành phần | Kiểu | Package Hoàn chỉnh | Mô tả chức năng ngắn gọn |
| :--- | :--- | :--- | :--- |
| `MovementCost` | Value Object | `com.naprock.hexudon.domain.model.cost` | Chứa chi phí di chuyển đã chốt (gồm lượng xăng hao tốn và số bước đi tiêu hao). |
| `MovementCostCalculator` | Domain Service | `com.naprock.hexudon.domain.service.cost` | Domain Service tính chi phí di chuyển cố định tại thời điểm bắt đầu đi. |

---

## 3. Module Nghiệp vụ: Hệ thống tính điểm (Scoring System)

| Tên Thành phần | Kiểu | Package Hoàn chỉnh | Mô tả chức năng ngắn gọn |
| :--- | :--- | :--- | :--- |
| `UdonType` | Value Object | `com.naprock.hexudon.domain.model.scoring` | Định nghĩa loại Udon độc nhất được phân biệt bằng mã định danh (ID). |
| `TeamScore` | Entity | `com.naprock.hexudon.domain.model.scoring` | Thực thể theo dõi điểm số chi tiết của từng đội: Số loại Udon, tích lũy ngày, số servings, response time. |
| `MatchScore` | Entity | `com.naprock.hexudon.domain.model.scoring` | Thực thể tổng hợp điểm số toàn bộ trận đấu của tất cả các đội. |
| `TeamScoreRepositoryPort` | Outbound Port (SPI) | `com.naprock.hexudon.application.port.out.scoring` | Cổng lưu trữ thông tin điểm số đội chơi xuống cơ sở dữ liệu bền vững. |
| `ScoreController` | Inbound Adapter | `com.naprock.hexudon.adapter.in.rest.scoring` | Controller cung cấp API lấy điểm số chi tiết thời gian thực. |

---

## 4. Module Nghiệp vụ: Hệ thống xếp hạng đấu trường (Ranking System)

| Tên Thành phần | Kiểu | Package Hoàn chỉnh | Mô tả chức năng ngắn gọn |
| :--- | :--- | :--- | :--- |
| `RankingCriteria` | Value Object | `com.naprock.hexudon.domain.service.ranking` | Chứa tiêu chí xếp hạng của đội chơi theo thứ tự ưu tiên tuyệt đối. |
| `RankingService` | Domain Service | `com.naprock.hexudon.domain.service.ranking` | Thực hiện thuật toán so sánh điểm và phân thứ tự xếp hạng (Anti-tie-break). |
| `RankingController` | Inbound Adapter | `com.naprock.hexudon.adapter.in.rest.scoring` | REST Controller cung cấp bảng xếp hạng hiện tại của vòng đấu. |

---

## 5. Module Nghiệp vụ: Lịch sử trận đấu & Log sự kiện (Game Event History)

| Tên Thành phần | Kiểu | Package Hoàn chỉnh | Mô tả chức năng ngắn gọn |
| :--- | :--- | :--- | :--- |
| `GameEvent` | Entity | `com.naprock.hexudon.domain.model.history` | Lưu trữ một sự kiện đơn lẻ trong game (eventId, turn, timestamp, teamId, payload). |
| `TurnHistory` | Entity | `com.naprock.hexudon.domain.model.history` | Lưu trữ toàn bộ diễn biến lịch sử tóm tắt của một lượt chơi (Turn). |
| `GameEventRepositoryPort` | Outbound Port (SPI) | `com.naprock.hexudon.application.port.out.history` | Cổng ra để ghi nhận và truy vấn lịch sử sự kiện đấu. |
| `HistoryPersistenceAdapter` | Outbound Adapter | `com.naprock.hexudon.adapter.out.persistence.history` | Adapter triển khai lưu lịch sử đấu xuống Database JPA. |
| `HistoryController` | Inbound Adapter | `com.naprock.hexudon.adapter.in.rest.history` | API REST cung cấp lịch sử đấu và timeline chi tiết phục vụ Visualizer. |

---

## 6. Module Nghiệp vụ: Giám sát giao tiếp mạng (Communication Logging)

| Tên Thành phần | Kiểu | Package Hoàn chỉnh | Mô tả chức năng ngắn gọn |
| :--- | :--- | :--- | :--- |
| `ApiCommunicationLog` | Entity | `com.naprock.hexudon.domain.model.logging` | Lưu trữ chi tiết thông tin cuộc gọi API: requestId, latency, size, status... |
| `CommunicationLogService` | Application Service | `com.naprock.hexudon.application.service.logging` | Xử lý nghiệp vụ phân tích và lưu nhật ký giao tiếp mạng bất đồng bộ. |
| `ApiCommunicationLogRepositoryPort` | Outbound Port (SPI) | `com.naprock.hexudon.application.port.out.logging` | Giao diện cổng lưu trữ log mạng. |
| `ApiLoggingInterceptor` | Inbound Adapter | `com.naprock.hexudon.adapter.in.rest.interceptor` | Interceptor đánh dấu thời gian, tính latency và kích thước payload của API. |
| `LogController` | Inbound Adapter | `com.naprock.hexudon.adapter.in.rest.history` | API REST cung cấp nhật ký mạng và phân tích hiệu suất phục vụ Visualizer. |

---

## 7. Module Nghiệp vụ: Trạng thái trận đấu & Khôi phục (Match Recovery)

| Tên Thành phần | Kiểu | Package Hoàn chỉnh | Mô tả chức năng ngắn gọn |
| :--- | :--- | :--- | :--- |
| `MatchSnapshot` | Entity | `com.naprock.hexudon.domain.model.recovery` | Thực thể chứa dữ liệu ảnh chụp trạng thái vòng đấu (Map, Agent, Score, Traffic). |
| `RecoveryPoint` | Value Object | `com.naprock.hexudon.domain.model.recovery` | Xác định lượt chơi an toàn gần nhất để hệ thống sẵn sàng Rollback. |
| `MatchSnapshotRepositoryPort` | Outbound Port (SPI) | `com.naprock.hexudon.application.port.out.recovery` | Cổng lưu trữ Snapshot trạng thái. |
| `RecoveryPersistenceAdapter` | Outbound Adapter | `com.naprock.hexudon.adapter.out.persistence.recovery` | Adapter thực thi ghi/đọc Snapshot từ cơ sở dữ liệu. |

---

## 8. Module Nghiệp vụ: Quản lý đồng thời (Concurrent Request)

| Tên Thành phần | Kiểu | Package Hoàn chỉnh | Mô tả chức năng ngắn gọn |
| :--- | :--- | :--- | :--- |
| `TurnExecutionQueue` | Application Service | `com.naprock.hexudon.application.service.concurrency` | Hàng đợi lưu hành động của các đội gửi lên trong thời gian chạy lượt hiện tại. |
| `RequestOrderingService` | Application Service | `com.naprock.hexudon.application.service.concurrency` | Thực hiện sắp xếp thứ tự công bằng dựa trên timestamp ghi nhận tại máy chủ. |

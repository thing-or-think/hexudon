# DANH MỤC CÁC LỚP VÀ GIAO DIỆN GIAI ĐOẠN 4

Tài liệu này tổng hợp toàn bộ các Class, Interface, Enum, Value Object và Entity được thêm mới hoặc chỉnh sửa trong Giai đoạn 4, được phân loại theo Module nghiệp vụ cụ thể.

---

## 1. Module 1: Hệ thống giao thông động (Traffic Flow)

| Tên Thành phần | Kiểu | Package Hoàn chỉnh | Mô tả chức năng ngắn gọn |
| :--- | :--- | :--- | :--- |
| `TrafficFlow` | Entity | `com.naprock.hexudon.domain.model.traffic` | Đại diện cho thông tin lưu lượng giao thông thực tế tại một tọa độ đường. |
| `RoadTrafficState` | Enum | `com.naprock.hexudon.domain.model.traffic` | Trạng thái nghẽn của đường (`SMOOTH`, `CONGESTED`, `TRAFFIC_JAM`). |
| `TrafficCalculator` | Domain Service | `com.naprock.hexudon.domain.service` | Chứa logic thuần túy tính calculated flow và phân định trạng thái nghẽn. |
| `TrafficThreshold` | Value Object | `com.naprock.hexudon.domain.model.traffic` | Chứa các ngưỡng (Thresholds) để xác định trạng thái giao thông. |
| `TrafficLevel` | Enum | `com.naprock.hexudon.domain.model.traffic` | Mức độ giao thông hỗ trợ tính toán cấu hình. |
| `TrafficSnapshot` | Value Object | `com.naprock.hexudon.domain.model.traffic` | Lưu trữ ảnh chụp nhanh trạng thái giao thông của toàn bản đồ tại một lượt. |
| `CalculateTrafficUseCase` | Inbound Port | `com.naprock.hexudon.application.port.in.traffic` | Giao diện điều phối việc cập nhật và tính toán giao thông. |
| `TrafficRepository` | Outbound Port | `com.naprock.hexudon.application.port.out.traffic` | Interface định nghĩa lưu trữ và truy vấn thông tin giao thông. |
| `TrafficCalculationService`| Application Service| `com.naprock.hexudon.application.service` | Điều phối tiến trình cập nhật lưu lượng giao thông khi đóng Turn. |
| `TrafficPersistenceAdapter`| Adapter | `com.naprock.hexudon.adapter.out.persistence.traffic`| Hiện thực hóa lưu trữ In-Memory trên RAM cho thông tin giao thông. |
| `TrafficEntity` | Persistence Model | `com.naprock.hexudon.adapter.out.persistence.traffic`| Mô hình thực thể lưu trữ trên RAM cho thông tin giao thông. |

---

## 2. Module 2: Chi phí di chuyển địa hình (Terrain & Movement Cost)

| Tên Thành phần | Kiểu | Package Hoàn chỉnh | Mô tả chức năng ngắn gọn |
| :--- | :--- | :--- | :--- |
| `MovementCostCalculator` | Domain Service | `com.naprock.hexudon.domain.service` | Chứa công thức tính chi phí di chuyển dựa trên địa hình và trạng thái giao thông. |
| `MovementCost` | Value Object | `com.naprock.hexudon.domain.model.movement` | Giá trị chi phí di chuyển (bất biến) gồm nhiên liệu tiêu hao và bước đi cần thiết. |

---

## 3. Module 3: Hệ thống tính điểm (Scoring System)

| Tên Thành phần | Kiểu | Package Hoàn chỉnh | Mô tả chức năng ngắn gọn |
| :--- | :--- | :--- | :--- |
| `TeamScore` | Entity | `com.naprock.hexudon.domain.model.score` | Lưu trữ chi tiết điểm số của một đội (các loại Udon, Servings, Response Time). |
| `MatchScore` | Entity | `com.naprock.hexudon.domain.model.score` | Quản lý bảng điểm tổng hợp của toàn bộ các đội trong trận đấu. |
| `UdonType` | Value Object | `com.naprock.hexudon.domain.model.score` | Loại Udon duy nhất đã thu thập để tính độ đa dạng tài nguyên. |
| `UpdateScoreUseCase` | Inbound Port | `com.naprock.hexudon.application.port.in.scoring` | Giao diện cập nhật điểm cho Agent/Đội khi thực hiện hành động thành công. |
| `TeamScoreRepository` | Outbound Port | `com.naprock.hexudon.application.port.out.scoring` | Interface lưu trữ và truy xuất điểm của các đội. |
| `ScorePersistenceAdapter` | Adapter | `com.naprock.hexudon.adapter.out.persistence.score` | Thực thi các hoạt động lưu trữ điểm số vào cơ sở dữ liệu. |
| `TeamScoreEntity` | DB Entity | `com.naprock.hexudon.adapter.out.persistence.score` | Bản ghi điểm số trong cơ sở dữ liệu. |
| `ScoreMapper` | Mapper | `com.naprock.hexudon.application.mapper` | Ánh xạ giữa TeamScore (Domain) <-> TeamScoreEntity <-> TeamScoreResponse. |
| `TeamScoreResponse` | DTO | `com.naprock.hexudon.application.dto` | Cấu trúc dữ liệu trả về thông tin điểm số của đội qua API. |

---

## 4. Module 4: Hệ thống xếp hạng đấu trường (Ranking System)

| Tên Thành phần | Kiểu | Package Hoàn chỉnh | Mô tả chức năng ngắn gọn |
| :--- | :--- | :--- | :--- |
| `RankingCriteria` | Value Object | `com.naprock.hexudon.domain.model.ranking` | Tiêu chí xếp hạng bao gồm bộ trọng số ưu tiên xếp hạng (Anti-tie-break). |
| `RankingService` | Domain Service | `com.naprock.hexudon.domain.service` | Logic so sánh thứ hạng giữa các đội dựa theo 5 cấp độ tiêu chí. |
| `GetRankingUseCase` | Inbound Port | `com.naprock.hexudon.application.port.in.scoring` | Giao diện truy vấn bảng xếp hạng hiện tại của trận đấu. |
| `RankingMapper` | Mapper | `com.naprock.hexudon.application.mapper` | Chuyển đổi dữ liệu bảng xếp hạng sang DTO. |
| `RankingResponse` | DTO | `com.naprock.hexudon.application.dto` | Trả về danh sách xếp hạng có thứ tự của các đội qua API. |
| `RankingController` | Adapter (In) | `com.naprock.hexudon.adapter.in.rest` | Endpoint REST cung cấp thông tin xếp hạng thời gian thực. |

---

## 5. Lịch sử trận đấu & Log sự kiện (Game Event History)

| Tên Thành phần | Kiểu | Package Hoàn chỉnh | Mô tả chức năng ngắn gọn |
| :--- | :--- | :--- | :--- |
| `TurnHistory` | Entity | `com.naprock.hexudon.domain.model.history` | Tổng hợp toàn bộ các hành động và thay đổi trạng thái xảy ra trong một Turn. |
| `GameEvent` | Entity | `com.naprock.hexudon.domain.model.history` | Lưu trữ một sự kiện game đơn lẻ (di chuyển, thu thập, cập nhật điểm, giao thông). |
| `QueryHistoryUseCase` | Inbound Port | `com.naprock.hexudon.application.port.in.history` | Giao diện truy vấn dòng thời gian sự kiện của trận đấu. |
| `GameEventRepository` | Outbound Port | `com.naprock.hexudon.application.port.out.history` | Interface lưu trữ sự kiện game vào cơ sở dữ liệu lịch sử. |
| `HistoryPersistenceAdapter`| Adapter | `com.naprock.hexudon.adapter.out.persistence.history`| Thực thi ghi nhận và truy vấn chuỗi lịch sử sự kiện game. |
| `GameEventEntity` | DB Entity | `com.naprock.hexudon.adapter.out.persistence.history`| Thực thể cơ sở dữ liệu đại diện cho một sự kiện game. |
| `HistoryMapper` | Mapper | `com.naprock.hexudon.application.mapper` | Ánh xạ cấu trúc sự kiện game thành DTO hiển thị. |
| `HistoryController` | Adapter (In) | `com.naprock.hexudon.adapter.in.rest` | REST Controller xuất dữ liệu timeline phục vụ Visualizer. |

---

## 6. Giám sát giao tiếp mạng (Communication Logging)

| Tên Thành phần | Kiểu | Package Hoàn chỉnh | Mô tả chức năng ngắn gọn |
| :--- | :--- | :--- | :--- |
| `ApiCommunicationLog` | Entity | `com.naprock.hexudon.domain.model.history` | Ghi nhận metadata của các yêu cầu HTTP từ các đội chơi đến server. |
| `QueryApiLogUseCase` | Inbound Port | `com.naprock.hexudon.application.port.in.history` | Interface truy cập dữ liệu nhật ký mạng để chẩn đoán độ trễ. |
| `ApiCommunicationLogRepository`| Outbound Port| `com.naprock.hexudon.application.port.out.history`| Interface định nghĩa việc lưu và truy vấn log mạng. |
| `CommunicationLogService` | Application Service| `com.naprock.hexudon.application.service` | Điều phối lưu trữ và phân tích hiệu năng độ trễ. |
| `ApiLogPersistenceAdapter`| Adapter | `com.naprock.hexudon.adapter.out.persistence.history`| Thực thi ghi nhật ký API vào MongoDB / JPA. |
| `ApiLogEntity` | DB Entity | `com.naprock.hexudon.adapter.out.persistence.history`| Cấu trúc bảng lưu trữ thông tin log mạng. |
| `ApiCommunicationInterceptor`| Adapter (In) | `com.naprock.hexudon.adapter.in.rest` | Spring MVC Interceptor chặn mọi API gửi đến để đo thời gian phản hồi. |

---

## 7. Giám sát trạng thái & Phục hồi (Match Recovery)

| Tên Thành phần | Kiểu | Package Hoàn chỉnh | Mô tả chức năng ngắn gọn |
| :--- | :--- | :--- | :--- |
| `MatchSnapshot` | Value Object | `com.naprock.hexudon.domain.model.recovery` | Đại diện cho ảnh chụp trạng thái đầy đủ bất biến tại đầu hoặc cuối Turn. |
| `RecoveryPoint` | Entity | `com.naprock.hexudon.domain.model.recovery` | Điểm phục hồi chứa thông tin snapshot và siêu dữ liệu rollback. |
| `MatchSnapshotRepository` | Outbound Port | `com.naprock.hexudon.application.port.out.recovery` | Interface lưu trữ và tìm kiếm các bản snapshot trạng thái. |
| `MatchRecoveryUseCase` | Inbound Port | `com.naprock.hexudon.application.port.in.recovery` | Giao diện thực thi rollback trạng thái hoặc kích hoạt rematch từ Turn X. |
| `MatchRecoveryService` | Application Service| `com.naprock.hexudon.application.service` | Phối hợp các bước khôi phục trạng thái và thông báo cho các bên liên quan. |
| `MatchSnapshotPersistenceAdapter`| Adapter | `com.naprock.hexudon.adapter.out.persistence.recovery`| Lưu trữ sâu ảnh chụp trạng thái vào cơ sở dữ liệu. |
| `MatchSnapshotEntity` | DB Entity | `com.naprock.hexudon.adapter.out.persistence.recovery`| Bảng lưu trữ dữ liệu nén JSON của Snapshot. |

---

## 8. Xử lý đồng thời & Xếp hàng (Concurrent Request)

| Tên Thành phần | Kiểu | Package Hoàn chỉnh | Mô tả chức năng ngắn gọn |
| :--- | :--- | :--- | :--- |
| `SubmitAgentActionUseCase`| Inbound Port | `com.naprock.hexudon.application.port.in.concurrency`| Giao diện để các Adapter Controller đẩy hành động vào hàng đợi. |
| `ConcurrentActionService` | Application Service| `com.naprock.hexudon.application.service` | Quản lý quy trình kiểm tra, đưa hành động vào hàng đợi và đóng Turn. |
| `TurnExecutionQueue` | Infra Component| `com.naprock.hexudon.infrastructure.queue` | Hàng đợi chứa hành động trong một Turn, quản lý đa luồng an toàn. |
| `RequestOrderingService` | Infra Component| `com.naprock.hexudon.infrastructure.ordering` | Trình tự hóa các yêu cầu dựa trên thời gian đo đạc chính xác tại Server. |

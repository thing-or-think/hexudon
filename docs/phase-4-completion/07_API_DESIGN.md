# THIẾT KẾ HỆ THỐNG REST API CHO ĐẤU TRƯỜNG

Tài liệu này đặc tả toàn bộ hệ thống API RESTful dành cho các đội chơi tương tác và hệ thống Visualizer giám sát trận đấu. Toàn bộ các DTO được thiết kế dưới dạng bảng tham số chi tiết.

---

## 1. Danh sách các REST Controllers và Endpoints

| Nhóm chức năng | Phương thức | Endpoint | Mô tả chức năng | Đối tượng sử dụng |
| :--- | :--- | :--- | :--- | :--- |
| **Hành động** | `POST` | `/api/v1/matches/{matchId}/actions` | Gửi danh sách hành động cho các Agent trong Turn hiện tại. | Bot của các đội chơi |
| **Trạng thái** | `GET` | `/api/v1/matches/{matchId}/state` | Truy vấn trạng thái chi tiết của trận đấu (Map, Agent, Spot). | Bot & Visualizer |
| **Điểm số** | `GET` | `/api/v1/matches/{matchId}/scores` | Lấy chi tiết điểm số hiện tại của tất cả các đội. | Bot & Visualizer |
| **Xếp hạng** | `GET` | `/api/v1/matches/{matchId}/ranking` | Truy vấn bảng xếp hạng đấu trường (áp dụng Anti-tie-break). | Bot & Visualizer |
| **Lịch sử** | `GET` | `/api/v1/matches/{matchId}/history` | Lấy lịch sử sự kiện game phục vụ hiển thị Timeline. | Visualizer |
| **Nhật ký mạng**| `GET` | `/api/v1/matches/{matchId}/logs/api` | Trích xuất nhật ký hiệu năng giao tiếp mạng để chẩn đoán độ trễ. | Visualizer & Giám sát |

---

## 2. Đặc tả cấu trúc DTO (Data Transfer Objects)

### 2.1. API Gửi hành động Agent (Submit Action)
*   **Request DTO**: `SubmitActionsRequest`

| Trường dữ liệu | Kiểu dữ liệu | Ý nghĩa | Ràng buộc Validation |
| :--- | :--- | :--- | :--- |
| `teamId` | String | ID định danh của đội chơi gửi yêu cầu. | Bắt buộc, không được để trống. |
| `turn` | Integer | Lượt chơi hiện tại mà bot đang muốn gửi hành động. | Bắt buộc, phải khớp với lượt hiện tại của trận đấu. |
| `agentActions` | List (AgentActionDTO) | Danh sách hành động chi tiết của các Agent thuộc đội. | Bắt buộc, kích thước danh sách không vượt quá số Agent. |

*   **DTO con**: `AgentActionDTO`

| Trường dữ liệu | Kiểu dữ liệu | Ý nghĩa | Ràng buộc Validation |
| :--- | :--- | :--- | :--- |
| `agentId` | String | ID định danh của Agent thực hiện hành động. | Bắt buộc, không được để trống. |
| `actionType` | String | Loại hành động (`MOVE`, `WAIT`, `COLLECT`, `SERVE`). | Bắt buộc, phải thuộc tập hợp hành động hợp lệ. |
| `direction` | String | Hướng di chuyển trên bản đồ Hex (`UP`, `DOWN`, v.v.). | Chỉ bắt buộc và không rỗng khi `actionType` là `MOVE`. |
| `stepSequence`| Integer | Thứ tự bước thực hiện trong lượt chơi hiện tại. | Bắt buộc, giá trị phải từ `1` đến `maxStepsPerTurn`. |

*   **Response DTO**: `SubmitActionsResponse`

| Trường dữ liệu | Kiểu dữ liệu | Ý nghĩa | Ràng buộc xuất bản |
| :--- | :--- | :--- | :--- |
| `matchId` | String | ID của trận đấu. | Không bao giờ rỗng. |
| `status` | String | Trạng thái tiếp nhận yêu cầu (`ACCEPTED`, `REJECTED`).| Giá trị thuộc nhóm định nghĩa tiếp nhận. |
| `timestamp` | Long | Thời điểm ghi nhận yêu cầu tại Server (mili-giây). | Số nguyên dương dài. |
| `message` | String | Thông báo chi tiết lý do từ chối nếu có. | Trả về thông tin chi tiết khi trạng thái là `REJECTED`. |

---

### 2.2. API Truy vấn trạng thái trận đấu (Get Match State)
*   **Response DTO**: `MatchStateResponse`

| Trường dữ liệu | Kiểu dữ liệu | Ý nghĩa | Ràng buộc xuất bản |
| :--- | :--- | :--- | :--- |
| `matchId` | String | ID định danh của trận đấu. | Không rỗng. |
| `currentTurn` | Integer | Lượt chơi hiện tại của hệ thống. | Giá trị lớn hơn hoặc bằng 0. |
| `status` | String | Trạng thái trận đấu (`PLAYING`, `FINISHED`, v.v.).| Thuộc tập hợp MatchStatus. |
| `turnStartTime` | Long | Thời gian bắt đầu lượt hiện thời (mili-giây). | Dùng để client tính thời gian đếm ngược còn lại. |
| `teams` | List (TeamStateDTO) | Danh sách trạng thái vị trí, tài nguyên của các đội. | Đầy đủ thông tin của tất cả đội tham gia. |
| `cells` | List (CellStateDTO) | Danh sách cấu trúc địa hình và giao thông các ô bản đồ. | Trả về toàn bộ ô trên bản đồ. |
| `spots` | List (SpotStateDTO) | Danh sách trạng thái kho chứa của các điểm cấp Udon. | Trả về toàn bộ các Spot. |

*   **DTO con**: `AgentStateDTO` (trong `TeamStateDTO`)

| Trường dữ liệu | Kiểu dữ liệu | Ý nghĩa | Ràng buộc xuất bản |
| :--- | :--- | :--- | :--- |
| `agentId` | String | ID định danh của Agent. | Không rỗng. |
| `coordinateX` | Integer | Tọa độ X trên lưới Hex. | Số nguyên. |
| `coordinateY` | Integer | Tọa độ Y trên lưới Hex. | Số nguyên. |
| `remainingFuel` | Integer | Lượng nhiên liệu còn lại của Agent. | Giá trị từ 0 đến mức tối đa trong cấu hình. |
| `remainingSteps`| Integer | Số bước đi Agent được thực hiện tiếp trong lượt này.| Giá trị từ 0 đến tối đa bước cấu hình. |

*   **DTO con**: `CellStateDTO`

| Trường dữ liệu | Kiểu dữ liệu | Ý nghĩa | Ràng buộc xuất bản |
| :--- | :--- | :--- | :--- |
| `coordinateX` | Integer | Tọa độ X của ô bản đồ. | Số nguyên. |
| `coordinateY` | Integer | Tọa độ Y của ô bản đồ. | Số nguyên. |
| `terrainType` | String | Loại địa hình (`PLAIN`, `MOUNTAIN`, `ROAD`, `POND`). | Thuộc tập hợp TerrainType. |
| `trafficState` | String | Trạng thái kẹt xe (`SMOOTH`, `CONGESTED`, `TRAFFIC_JAM`).| Chỉ xuất hiện và có giá trị khi địa hình là `ROAD`. |

---

### 2.3. API Xem điểm số (Get Score)
*   **Response DTO**: `MatchScoreResponse`

| Trường dữ liệu | Kiểu dữ liệu | Ý nghĩa | Ràng buộc xuất bản |
| :--- | :--- | :--- | :--- |
| `matchId` | String | ID định danh của trận đấu. | Không rỗng. |
| `scores` | List (TeamScoreResponseDTO) | Điểm số chi tiết của tất cả các đội. | Sắp xếp ngẫu nhiên theo thứ tự đăng ký. |

*   **DTO con**: `TeamScoreResponseDTO`

| Trường dữ liệu | Kiểu dữ liệu | Ý nghĩa | Ràng buộc xuất bản |
| :--- | :--- | :--- | :--- |
| `teamId` | String | ID định danh của đội chơi. | Không rỗng. |
| `uniqueUdonTypes` | Integer | Số lượng các loại Udon độc nhất đã thu được. | Số nguyên không âm. |
| `accumulatedDailyUdon`| Integer | Số lượng loại Udon tích lũy theo từng ngày. | Số nguyên không âm. |
| `totalServings` | Integer | Tổng số lần phục vụ thành công. | Số nguyên không âm. |
| `totalResponseTimeMs` | Long | Tổng thời gian phản hồi API của đội (mili-giây). | Số nguyên dương dài. |
| `totalPoints` | Integer | Tổng điểm số quy đổi cuối cùng để so sánh thô. | Tính toán theo cấu thức tính điểm ở File 05. |

---

### 2.4. API Xem bảng xếp hạng (Get Ranking)
*   **Response DTO**: `RankingResponse`

| Trường dữ liệu | Kiểu dữ liệu | Ý nghĩa | Ràng buộc xuất bản |
| :--- | :--- | :--- | :--- |
| `matchId` | String | ID định danh của trận đấu. | Không rỗng. |
| `rankings` | List (RankItemDTO) | Danh sách kết quả xếp hạng đã sắp xếp từ cao xuống thấp. | Xếp theo đúng thứ tự ưu tiên phân hạng tuyệt đối. |

*   **DTO con**: `RankItemDTO`

| Trường dữ liệu | Kiểu dữ liệu | Ý nghĩa | Ràng buộc xuất bản |
| :--- | :--- | :--- | :--- |
| `rank` | Integer | Thứ hạng hiện tại (`1` cho đội đứng đầu, `2`, `3`...). | Số nguyên tăng dần từ 1. |
| `teamId` | String | ID của đội chơi. | Không rỗng. |
| `totalPoints` | Integer | Tổng điểm số quy đổi của đội. | Số nguyên không âm. |
| `tieBreakerDetails` | String | Mô tả tiêu chí phụ nào đã quyết định thứ hạng này. | Trả về lý do (ví dụ: "Thắng nhờ tổng Servings lớn hơn"). |

---

### 2.5. API Truy vấn lịch sử trận đấu (Get History)
*   **Response DTO**: `HistoryResponse`

| Trường dữ liệu | Kiểu dữ liệu | Ý nghĩa | Ràng buộc xuất bản |
| :--- | :--- | :--- | :--- |
| `matchId` | String | ID định danh của trận đấu. | Không rỗng. |
| `events` | List (GameEventDTO) | Danh sách các sự kiện dòng thời gian của trận đấu. | Sắp xếp tăng dần theo trường `timestamp`. |

*   **DTO con**: `GameEventDTO`

| Trường dữ liệu | Kiểu dữ liệu | Ý nghĩa | Ràng buộc xuất bản |
| :--- | :--- | :--- | :--- |
| `eventId` | String | Khóa duy nhất định danh sự kiện. | Định dạng UUID. |
| `turn` | Integer | Lượt xảy ra sự kiện. | Số nguyên dương. |
| `timestamp` | Long | Thời gian thực sự kiện xảy ra (mili-giây). | Số nguyên dài. |
| `teamId` | String | Đội chơi liên quan trực tiếp đến sự kiện. | Có thể null nếu là sự kiện hệ thống (ví dụ: Traffic). |
| `agentId` | String | Agent liên quan trực tiếp đến sự kiện. | Có thể null nếu sự kiện mức Team hoặc System. |
| `eventType` | String | Loại sự kiện (`MOVEMENT`, `COLLECTION`, `TRAFFIC_UPDATE`).| Giá trị thuộc danh sách Event Type quy định. |
| `eventDetails` | String | Mô tả chi tiết hành động hoặc tham số (ví dụ: "di chuyển sang ô A").| Chuỗi ký tự mô tả trực quan. |

---

### 2.6. API Xuất nhật ký giao tiếp mạng (Get Api Logs)
*   **Response DTO**: `ApiLogsResponse`

| Trường dữ liệu | Kiểu dữ liệu | Ý nghĩa | Ràng buộc xuất bản |
| :--- | :--- | :--- | :--- |
| `matchId` | String | ID định danh của trận đấu. | Không rỗng. |
| `logs` | List (ApiLogDTO) | Danh sách nhật ký hiệu năng giao tiếp của các đội chơi. | Sắp xếp giảm dần theo thời gian tiếp nhận request. |

*   **DTO con**: `ApiLogDTO`

| Trường dữ liệu | Kiểu dữ liệu | Ý nghĩa | Ràng buộc xuất bản |
| :--- | :--- | :--- | :--- |
| `requestId` | String | Khóa duy nhất định danh yêu cầu HTTP. | Không rỗng. |
| `teamId` | String | ID đội chơi thực hiện yêu cầu. | Không rỗng. |
| `endpoint` | String | URI của REST API được gọi. | Định dạng đường dẫn dạng chuỗi. |
| `durationMs` | Long | Tổng độ trễ xử lý của server (mili-giây). | Số nguyên dương biểu diễn độ trễ mạng/máy chủ. |
| `payloadSize` | Long | Kích thước gói tin yêu cầu (bytes). | Kích thước vật lý của dữ liệu thô. |
| `status` | Integer | HTTP Status phản hồi (`200`, `400`, `500`). | Mã trạng thái tiêu chuẩn RFC. |

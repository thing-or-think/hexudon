# THIẾT KẾ RESTFUL API (API DESIGN) - GIAI ĐOẠN 4

Tài liệu này đặc tả chi tiết kiến trúc giao diện lập trình ứng dụng RESTful API cho Giai đoạn 4. Mọi cấu trúc dữ liệu gửi và nhận đều được mô tả bằng bảng thuộc tính dữ liệu cụ thể, tuyệt đối không sử dụng định dạng JSON thô.

---

## 1. Bản đồ các Inbound REST Controllers

Hệ thống cung cấp 4 Controllers chính ở tầng Adapter Inbound để xử lý các yêu cầu:
1.  **MatchController:** Tiếp nhận kế hoạch hành động của các Agent gửi từ đội chơi và điều phối trạng thái vòng đấu.
2.  **ScoreController:** Cung cấp thông tin điểm số thời gian thực phục vụ người xem và các hệ thống phân tích.
3.  **RankingController:** Cung cấp bảng xếp hạng chính thức được tính toán tự động sau mỗi lượt đấu.
4.  **HistoryController:** Cung cấp dòng thời gian sự kiện lịch sử trận đấu và xuất bản nhật ký truyền tải API phục vụ Visualizer.

---

## 2. Đặc tả chi tiết các API Endpoints và cấu trúc DTOs

### 2.1. API gửi hành động của Agent (Submit Agent Action)
*   **Method:** POST
*   **Path:** `/api/matches/action`
*   **Mô tả:** Đội chơi gửi danh sách các lệnh di chuyển hoặc chờ cho tất cả các Agent của mình trong lượt hiện tại.

#### Bảng cấu trúc Request DTO: SubmitActionRequest
| Trường dữ liệu (Field) | Kiểu dữ liệu | Ý nghĩa chức năng | Ràng buộc Validation |
| :--- | :--- | :--- | :--- |
| `teamName` | String | Tên của đội chơi thực hiện gửi hành động. | Không được trống, khớp với tên đội đăng ký. |
| `turnNumber` | Integer | Số thứ tự lượt đấu hiện tại mà đội chơi đang thực thi. | Không được null, lớn hơn 0, khớp với Turn hiện tại. |
| `agentActions` | List<AgentActionDTO> | Danh sách các hành động chi tiết phân bổ cho từng Agent. | Không được rỗng, số lượng phần tử bằng số Agent. |

#### Bảng cấu trúc DTO con: AgentActionDTO
| Trường dữ liệu (Field) | Kiểu dữ liệu | Ý nghĩa chức năng | Ràng buộc Validation |
| :--- | :--- | :--- | :--- |
| `agentId` | String | Mã định danh duy nhất của Agent thực hiện. | Không được trống. |
| `actionType` | String | Loại hành động của Agent. | Nhận giá trị cố định: `WAIT` hoặc `MOVE`. |
| `targetCoordinate` | CoordinateDTO | Tọa độ ô đích di chuyển. | Bắt buộc không null khi loại hành động là `MOVE`. |

#### Bảng cấu trúc DTO con: CoordinateDTO
| Trường dữ liệu (Field) | Kiểu dữ liệu | Ý nghĩa chức năng | Ràng buộc Validation |
| :--- | :--- | :--- | :--- |
| `x` | Integer | Tọa độ hàng của ô trên bản đồ. | Không null, nằm trong phạm vi chiều rộng bản đồ. |
| `y` | Integer | Tọa độ cột của ô trên bản đồ. | Không null, nằm trong phạm vi chiều cao bản đồ. |

#### Bảng cấu trúc Response DTO: SubmitActionResponse
| Trường dữ liệu (Field) | Kiểu dữ liệu | Ý nghĩa chức năng |
| :--- | :--- | :--- |
| `success` | Boolean | Trạng thái tiếp nhận request thành công (true/false). |
| `message` | String | Nội dung phản hồi chi tiết từ server (như xác nhận hợp lệ hoặc lý do từ chối). |
| `serverTimestamp` | Long | Dấu thời gian ghi nhận yêu cầu tại server khi đưa vào hàng đợi (ms). |

---

### 2.2. API lấy trạng thái trận đấu hiện tại (Get Match State)
*   **Method:** GET
*   **Path:** `/api/matches/state`
*   **Mô tả:** Trả về trạng thái chi tiết của trận đấu bao gồm bản đồ, vị trí Agent, xăng và các ô đường bộ kèm lưu lượng giao thông động hiện tại.

#### Bảng cấu trúc Response DTO: MatchStateResponse
| Trường dữ liệu (Field) | Kiểu dữ liệu | Ý nghĩa chức năng |
| :--- | :--- | :--- |
| `status` | String | Trạng thái hiện thời của trận đấu (`WAITING`, `PLAYING`, `FINISHED`). |
| `currentTurn` | Integer | Số thứ tự lượt đấu hiện tại đang diễn ra. |
| `remainingTimeMs` | Long | Thời gian còn lại (mili-giây) trước khi đóng Turn hiện tại. |
| `teams` | List<TeamResponseDTO> | Danh sách các đội chơi kèm thông tin xăng, vị trí chi tiết của Agent. |
| `spots` | List<SpotResponseDTO> | Danh sách các Spot Udon và số lượng mì tồn kho hiện tại cho từng đội. |
| `roadsTraffic` | List<RoadTrafficResponseDTO> | Trạng thái mật độ giao thông động của tất cả các ô đường. |

---

### 2.3. API xem điểm số (Get Score)
*   **Method:** GET
*   **Path:** `/api/scores`
*   **Mô tả:** Trích xuất điểm số chi tiết của tất cả các đội chơi phục vụ cập nhật bảng điểm hiển thị.

#### Bảng cấu trúc Response DTO: MatchScoreResponse
| Trường dữ liệu (Field) | Kiểu dữ liệu | Ý nghĩa chức năng |
| :--- | :--- | :--- |
| `matchId` | String | Mã định danh duy nhất của trận đấu. |
| `scores` | List<TeamScoreResponseDTO>| Danh sách điểm số chi tiết được ánh xạ theo từng đội chơi. |

#### Bảng cấu trúc DTO con: TeamScoreResponseDTO
| Trường dữ liệu (Field) | Kiểu dữ liệu | Ý nghĩa chức năng |
| :--- | :--- | :--- |
| `teamName` | String | Tên của đội chơi. |
| `uniqueUdonCount` | Integer | Tổng số chủng loại Udon độc nhất đội đã thu thập được. |
| `accumulatedDailyUdon` | Integer | Tổng số lượng mì Udon tích lũy theo ngày. |
| `totalServings` | Integer | Tổng số lượt phục vụ mì thành công. |
| `totalResponseTimeMs` | Long | Tổng thời gian phản hồi tích lũy của đội chơi (ms). |

---

### 2.4. API xem bảng xếp hạng (Get Ranking)
*   **Method:** GET
*   **Path:** `/api/rankings`
*   **Mô tả:** Lấy bảng xếp hạng các đội xếp theo thứ tự ưu tiên các tiêu chí giải quyết hòa điểm.

#### Bảng cấu trúc Response DTO: RankingResponse
| Trường dữ liệu (Field) | Kiểu dữ liệu | Ý nghĩa chức năng |
| :--- | :--- | :--- |
| `rankings` | List<TeamRankDTO> | Danh sách xếp hạng các đội chơi được sắp xếp từ cao xuống thấp. |

#### Bảng cấu trúc DTO con: TeamRankDTO
| Trường dữ liệu (Field) | Kiểu dữ liệu | Ý nghĩa chức năng |
| :--- | :--- | :--- |
| `rank` | Integer | Thứ hạng hiện tại của đội chơi (1, 2, 3...). |
| `teamName` | String | Tên của đội chơi. |
| `criteria` | RankingCriteriaDTO | Chi tiết các chỉ số tiêu chí dùng để phân thứ hạng. |

---

### 2.5. API truy vấn lịch sử sự kiện (Get Match History)
*   **Method:** GET
*   **Path:** `/api/matches/history`
*   **Mô tả:** Cung cấp toàn bộ dòng lịch sử sự kiện diễn ra phục vụ chức năng phát lại (replay) của Visualizer.

#### Bảng cấu trúc Response DTO: MatchHistoryResponse
| Trường dữ liệu (Field) | Kiểu dữ liệu | Ý nghĩa chức năng |
| :--- | :--- | :--- |
| `matchId` | String | Mã định danh trận đấu. |
| `turnHistories` | List<TurnHistoryDTO> | Dòng lịch sử các lượt đấu chứa danh sách chi tiết sự kiện diễn ra. |

---

### 2.6. API xuất nhật ký giao tiếp mạng (Get Communication Logs)
*   **Method:** GET
*   **Path:** `/api/logs/communication`
*   **Mô tả:** Kết xuất dữ liệu nhật ký giao tiếp mạng phục vụ thống kê phân tích hiệu năng và phát hiện spam.

#### Bảng cấu trúc Response DTO: CommunicationLogResponse
| Trường dữ liệu (Field) | Kiểu dữ liệu | Ý nghĩa chức năng |
| :--- | :--- | :--- |
| `logs` | List<ApiLogDTO> | Danh sách các bản ghi nhật ký truyền tải mạng chi tiết. |

#### Bảng cấu trúc DTO con: ApiLogDTO
| Trường dữ liệu (Field) | Kiểu dữ liệu | Ý nghĩa chức năng |
| :--- | :--- | :--- |
| `requestId` | String | Mã yêu cầu duy nhất được sinh ra ở Interceptor. |
| `teamId` | String | Tên đội chơi gửi yêu cầu. |
| `endpoint` | String | Địa chỉ API Endpoint được kết nối. |
| `durationMs` | Long | Thời gian phản hồi của mạng (Độ trễ xử lý - ms). |
| `payloadSize` | Long | Kích thước của dữ liệu gửi lên (Byte). |
| `status` | Integer | Mã trạng thái HTTP phản hồi về phía Client. |

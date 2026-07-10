# 07. THIẾT KẾ REST API (API DESIGN)

## Mục lục
1. [Danh sách API tổng quan](#1-danh-sách-api-tổng-quan)
2. [Đặc tả chi tiết từng API](#2-đặc-tả-chi-tiết-từng-api)
   - [2.1. Đăng ký đội chơi (POST /api/match/register)](#21-đăng-ký-đội-chơi-post-apimatchregister)
   - [2.2. Khởi động trận đấu (POST /api/match/start)](#22-khởi-động-trận-đấu-post-apimatchstart)
   - [2.3. Lấy trạng thái trận đấu (GET /api/match/state)](#23-lấy-trạng-thái-trận-đấu-get-apimatchstate)
   - [2.4. Nộp kế hoạch hành động (POST /api/match/actions)](#24-nộp-kế-hoạch-hành-động-post-apimatchactions)
3. [Cơ chế xử lý và Ánh xạ Ngoại lệ (Exception Mapping)](#3-cơ-chế-xử-lý-và-ánh-xạ-ngoại-lệ-exception-mapping)

---

## 1. Danh sách API tổng quan

Hệ thống Hexudon Game Server cung cấp các REST API sau dưới tiền tố `/api/match`:

| Method | Endpoint | Mô tả nghiệp vụ | Yêu cầu đặc biệt |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/match/register` | Đăng ký một đội chơi mới vào trận đấu. | |
| **POST** | `/api/match/start` | Bắt đầu chạy trận đấu (kích hoạt luật chơi). | Admin/Hệ thống gọi |
| **GET** | `/api/match/state` | Truy vấn toàn bộ thông tin trạng thái game hiện tại. | |
| **POST** | `/api/match/actions` | Nộp kế hoạch hành động ngày tiếp theo cho các Agent. | Header `X-Team-Name` |

---

## 2. Đặc tả chi tiết từng API

### 2.1. Đăng ký đội chơi (POST /api/match/register)

- **HTTP Method:** `POST`
- **Endpoint:** `/api/match/register`
- **Ca sử dụng ứng dụng (Use Case):** `RegisterTeamUseCase`

#### Cấu trúc dữ liệu yêu cầu (Request DTO - TeamRegisterRequest):
- Chứa thuộc tính `teamName` dạng chuỗi (String). Không được để trống, độ dài từ 1 đến 50 ký tự.

#### Cấu trúc dữ liệu phản hồi (Response DTO - TeamResponse):
- Trả về thông tin đội chơi đã đăng ký bao gồm:
  - `teamName`: Tên đội chơi.
  - `agents`: Danh sách 3 Agent được tạo tự động cho đội. Mỗi Agent gồm các thông tin: ID điệp viên (`id`), Loại điệp viên (`type` - `PATROL` hoặc `REFUEL`), Tọa độ X hiện tại (`posX`), Tọa độ Y hiện tại (`posY`), Lượng xăng hiện có (`fuel`), và số bước chân còn lại trong ngày (`remainingSteps`).

#### Quy tắc Validation:
- Nếu thuộc tính `teamName` rỗng: Trả về HTTP 400 (Bad Request).
- Nếu trận đấu đã bắt đầu hoặc danh sách đăng ký đã đủ đội: Trả về HTTP 409 (Conflict).
- Nếu tên đội chơi đã tồn tại: Trả về HTTP 409 (Conflict).

#### Ánh xạ dữ liệu (Mapper):
- Gọi phương thức `ActionMapper` để chuyển đổi thực thể `Team` được trả về từ Use Case thành đối tượng `TeamResponse`.

#### Luồng xử lý chi tiết:
1. `MatchController` nhận HTTP Request body chứa tên đội.
2. Kiểm tra tính hợp lệ của trường dữ liệu thông qua Spring Validator.
3. Controller gọi `RegisterTeamUseCase.registerTeam(teamName)`.
4. UseCase (trong Application Service) tải `MatchState` lên, kiểm tra xem trạng thái trận đấu có phải là `WAITING` và chưa đạt giới hạn đội chơi.
5. Tạo mới thực thể `Team`, khởi tạo 3 Agent có tọa độ (0,0) thuộc về Team này.
6. Thêm `Team` mới vào danh sách của `MatchState` rồi gọi Repository để lưu lại.
7. Trả thực thể `Team` về Controller.
8. Controller gọi `ActionMapper` chuyển thành `TeamResponse` và phản hồi HTTP 200 OK.

---

### 2.2. Khởi động trận đấu (POST /api/match/start)

- **HTTP Method:** `POST`
- **Endpoint:** `/api/match/start`
- **Ca sử dụng ứng dụng (Use Case):** `StartMatchUseCase`

#### Cấu trúc dữ liệu yêu cầu (Request DTO):
- Không yêu cầu request body.

#### Cấu trúc dữ liệu phản hồi (Response DTO):
- Trả về HTTP 200 OK (Không chứa dữ liệu body).

#### Quy tắc Validation:
- Trận đấu phải đang ở trạng thái `WAITING`. Nếu đã ở trạng thái `PLAYING` hoặc `FINISHED`, trả về HTTP 409 (Conflict).
- Phải có ít nhất một đội đã đăng ký thi đấu. Nếu chưa có đội nào, trả về HTTP 409 (Conflict).

#### Luồng xử lý chi tiết:
1. Controller nhận HTTP Request gọi khởi động.
2. Controller chuyển tiếp cuộc gọi vào `StartMatchUseCase.startMatch()`.
3. UseCase gọi Outbound Port `MatchConfigLoaderPort` để nạp cấu hình `MatchConfig` từ file.
4. Lấy `MatchState` từ bộ lưu trữ, gọi phương thức `matchState.start(...)` để chuyển trạng thái sang `PLAYING`, bắt đầu ngày thứ nhất (Day 1).
5. Khởi tạo tài nguyên xăng/bước cho toàn bộ Agent của các đội, đặt lại số lượng Udon cho toàn bộ Spot.
6. Ghi đè trạng thái `MatchState` mới cập nhật qua `MatchStateStorePort` và phản hồi kết quả thành công HTTP 200.

---

### 2.3. Lấy trạng thái trận đấu (GET /api/match/state)

- **HTTP Method:** `GET`
- **Endpoint:** `/api/match/state`
- **Ca sử dụng ứng dụng (Use Case):** `GetMatchStateUseCase`

#### Cấu trúc dữ liệu yêu cầu:
- Không yêu cầu tham số.

#### Cấu trúc dữ liệu phản hồi (Response DTO - MatchStateResponse):
- Trả về toàn bộ dữ liệu trận đấu bao gồm:
  - `status`: Trạng thái trận đấu (`WAITING`, `PLAYING`, `FINISHED`).
  - `currentTurn`: Ngày chơi hiện tại.
  - `teams`: Danh sách thông tin chi tiết các đội chơi (điểm số, danh sách Agent kèm tài nguyên xăng/bước chân, trạng thái vi phạm, trạng thái nộp bài).
  - `cells`: Lưới tọa độ và loại địa hình của bản đồ.
  - `spots`: Danh sách các điểm chứa Udon cùng số lượng Udon còn lại cho từng đội tại mỗi điểm.
  - `currentTurnActions`: Bản đồ lịch sử hành động của lượt chơi hiện tại của các Agent.

#### Ánh xạ dữ liệu (Mapper):
- Gọi Constructor `MatchStateResponse(MatchState)` để tự động chuyển toàn bộ thực thể Aggregate Root thành DTO phản hồi.

#### Luồng xử lý chi tiết:
1. Controller tiếp nhận request GET.
2. Gọi `GetMatchStateUseCase.getMatchState()`.
3. Application Service lấy `MatchState` trực tiếp từ Repository.
4. Trả về đối tượng `MatchState` gốc cho Controller.
5. Controller ánh xạ thành `MatchStateResponse` và trả về HTTP 200 OK.

---

### 2.4. Nộp kế hoạch hành động (POST /api/match/actions)

- **HTTP Method:** `POST`
- **Endpoint:** `/api/match/actions`
- **Ca sử dụng ứng dụng (Use Case):** `SubmitActionsUseCase`
- **Yêu cầu Headers:** `X-Team-Name` (Bắt buộc phải có tên đội hợp lệ đăng ký trong hệ thống).

#### Cấu trúc dữ liệu yêu cầu (Request DTO - DayActionRequest):
- `day`: Số thứ tự ngày gửi bài (phải khớp chính xác với ngày hiện tại của trận đấu).
- `agentPlans`: Danh sách kế hoạch hành động của các Agent. Mỗi kế hoạch gồm:
  - `agentId`: ID của Agent thi đấu.
  - `actions`: Danh sách tối đa 5 hành động (`order` tăng dần từ 1, `actionType` là `MOVE` hoặc `WAIT`, `targetX` và `targetY` là tọa độ ô đích nếu di chuyển).

#### Cấu trúc dữ liệu phản hồi (Response DTO - DayActionResponse):
- Trả về thông tin kết quả nộp bài khớp với định dạng yêu cầu kèm timestamp ghi nhận:
  - `day`: Ngày thi đấu.
  - `agentPlans`: Danh sách kế hoạch hành động của các Agent bao gồm thời gian ghi nhận (timestamp) từng hành động.

#### Quy tắc Validation:
- Trận đấu phải đang ở trạng thái `PLAYING`. Nếu khác, trả về HTTP 409 (Conflict).
- Ngày chơi gửi lên trong body phải khớp chính xác ngày hiện tại của trận đấu (`currentTurn`). Nếu không, trả về HTTP 400 (Bad Request).
- Agent ID gửi lên phải thuộc sở hữu của đội chơi gửi request. Nếu khác, trả về HTTP 404 (Not Found) hoặc HTTP 400.
- Số lượng hành động của một Agent không được vượt quá số bước đi tối đa. Tọa độ đích phải hợp lệ. Nếu vi phạm, trả về HTTP 400 (Bad Request).

#### Ánh xạ dữ liệu (Mapper):
- Sử dụng `ActionMapper` để ánh xạ từ DTO Request sang Map hành động nghiệp vụ trong Domain, và chuyển kết quả mô phỏng `TurnSimulationResult` thành DTO phản hồi `DayActionResponse`.

#### Luồng xử lý chi tiết:
1. Controller kiểm tra sự hiện diện của header `X-Team-Name` và tính hợp lệ dữ liệu Body qua Spring `@Valid`.
2. Controller gọi `ActionMapper` chuyển đổi request body thành cấu trúc Map hành động nghiệp vụ.
3. Gọi `SubmitActionsUseCase.submitActions(teamName, day, domainPlans)`.
4. Application Service nạp `MatchState` và `MatchConfig`.
5. Gọi `ActionValidatorEngine.validate(...)` để kiểm duyệt lỗi luật chơi.
6. Thiết lập các hành động vào Agent.
7. Gọi `MovementSimulator.simulateTeamTurn(...)` để tính toán di chuyển, xăng dầu và thu thập Udon thực tế.
8. Đánh dấu `team.setSubmittedPlan(true)`.
9. Lưu trữ lại `MatchState` đã sửa đổi.
10. Trả về `TurnSimulationResult` cho Controller.
11. Controller chuyển đổi kết quả thành `DayActionResponse` và trả về HTTP 200 OK.

---

## 3. Cơ chế xử lý và Ánh xạ Ngoại lệ (Exception Mapping)

Hệ thống sử dụng lớp `GlobalExceptionHandler` đặt tại lớp Adapter đầu vào để biến đổi toàn bộ Business Exception được ném từ Domain Core thành cấu trúc JSON lỗi chuẩn mực (`ErrorResponse`) và mã HTTP tương ứng:

| Ngoại lệ nghiệp vụ (Domain Exception) | Mã HTTP phản hồi | Ý nghĩa trả về Client |
| :--- | :--- | :--- |
| `ResourceNotFoundException` | **404 Not Found** | Không tìm thấy đội chơi hoặc điệp viên với ID được cung cấp. |
| `MatchStateConflictException` | **409 Conflict** | Trận đấu chưa bắt đầu, đã kết thúc, hoặc tên đội đã được đăng ký. |
| `GameRuleViolationException` | **400 Bad Request** | Vi phạm luật chơi (gửi sai ngày chơi, đi quá bước đi cho phép, hết xăng, di chuyển vào ô POND hoặc không liên thông). |
| `RateLimitExceededException` | **429 Too Many Requests** | Đội chơi gửi request quá tần suất quy định. |
| `SystemException` / Lỗi hệ thống khác| **500 Internal Error** | Gặp sự cố không mong muốn tại máy chủ (lỗi nạp file cấu hình, lỗi bộ nhớ). |

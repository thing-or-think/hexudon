# 08. VÒNG ĐỜI ĐỐI TƯỢNG (OBJECT LIFECYCLE)

## Mục lục
1. [Sơ đồ dòng chảy vòng đời đối tượng (ASCII Lifecycle Flow Diagram)](#1-sơ-đồ-dòng-chảy-vòng-đời-đối-tượng-ascii-lifecycle-flow-diagram)
2. [Đặc tả chi tiết vòng đời từng Đối tượng Nghiệp vụ](#2-đặc-tả-chi-tiết-vòng-đời-từng-đối-tượng-nghiệp-vụ)
   - [2.1. Trận đấu & Cấu hình (Match & MatchConfig)](#21-trận-đấu--cấu-hình-match--matchconfig)
   - [2.2. Trạng thái trận đấu (MatchState)](#22-trạng-thái-trận-đấu-matchstate)
   - [2.3. Đội thi đấu (Team)](#23-đội-thi-đấu-team)
   - [2.4. Điệp viên (Agent)](#24-điệp-viên-agent)
   - [2.5. Kế hoạch nộp (Submission / Action)](#25-kế-hoạch-nộp-submission--action)
   - [2.6. Mô phỏng & Kết quả (Simulation & Result)](#26-mô-phỏng--kết-quả-simulation--result)
3. [Bảng tóm tắt quản lý Vòng đời Đối tượng](#3-bảng-tóm-tắt-quản-lý-vòng-đời-đối-tượng)

---

## 1. Sơ đồ dòng chảy vòng đời đối tượng (ASCII Lifecycle Flow Diagram)

Dưới đây là sơ đồ ASCII minh họa mối liên kết và trình tự sinh ra, cập nhật, hủy bỏ của các đối tượng nghiệp vụ cốt lõi trong hệ thống:

```text
  [Match Config File]
          │
          │ (1) Nạp cấu hình qua ConfigLoader
          v
    +───────────+
    │MatchConfig│ (Bất biến suốt trận đấu)
    +─────┬─────+
          │
          │ (2) Tạo lập trận đấu và sinh lưới bản đồ ngẫu nhiên
          v
    +───────────+
    │MatchState │ <───────────────────────────────────────+ (Đồng bộ cập nhật)
    +─────┬─────+                                         │
          │                                               │
          │ (3) Client Đăng ký Đội                        │
          v                                               │
    +───────────+                                         │
    │   Team    │ (Được thêm vào MatchState)               │
    +─────┬─────+                                         │
          │                                               │
          │ (4) Tự động sinh ra cùng Team                 │
          v                                               │
    +───────────+                                         │
    │   Agent   │ (Tọa độ gốc 0,0)                         │
    +─────┬─────+                                         │
          │                                               │
          │ (5) Client nộp kế hoạch                       │
          v                                               │
    +───────────+                                         │
    │Submission │ (Chuỗi danh sách Action)                │
    +─────┬─────+                                         │
          │                                               │
          │ (6) Đưa vào chạy Simulator                     │
          v                                               │
    +───────────+                                         │
    │Simulation │ (Tính toán xăng, đi lại, udon)          │
    +─────┬─────+                                         │
          │                                               │
          │ (7) Tạo ra kết quả & cập nhật lại ────────────+
          v
    +───────────+
    │  Result   │ (TurnSimulationResult trả về Client)
    +───────────+
```

---

## 2. Đặc tả chi tiết vòng đời từng Đối tượng Nghiệp vụ

### 2.1. Trận đấu & Cấu hình (Match & MatchConfig)
- **Khi nào được tạo:** 
  - `MatchConfig` được tạo ra duy nhất một lần khi bắt đầu khởi động trận đấu (`startMatch`). Tầng Application gọi Adapter đọc file cấu hình, phân tích các thuộc tính luật chơi và ánh xạ thành đối tượng `MatchConfig`.
- **Khi nào được cập nhật:** 
  - Đối tượng này có tính bất biến (Immutable), giá trị các thuộc tính không bao giờ thay đổi trong suốt quá trình trận đấu diễn ra.
- **Khi nào bị hủy:** 
  - Hủy khi trận đấu kết thúc hoàn toàn và hệ thống chuẩn bị cho trận đấu mới.
- **Ai quản lý:** 
  - Quản lý bởi `MatchApplicationService`.

### 2.2. Trạng thái trận đấu (MatchState)
- **Khi nào được tạo:** 
  - Tạo mới khi khởi chạy Game Server hoặc tạo phòng đấu mới. Quá trình tạo mới sẽ sinh bản đồ lưới lục giác ngẫu nhiên thông qua `HexGridUtils` và `TerrainGenerator`.
- **Khi nào được cập nhật:**
  - Cập nhật khi có Team đăng ký tham gia (thêm vào danh sách `teams`).
  - Cập nhật thuộc tính `status` và `currentTurn` khi trận đấu bắt đầu và chuyển ngày mới.
  - Cập nhật tọa độ, lượng xăng, bước đi còn lại của các Agent sau mỗi ngày chạy mô phỏng.
  - Cập nhật số lượng Udon tại các Spot và điểm số tích lũy của từng đội thi đấu.
- **Khi nào bị hủy:** 
  - Bị hủy khi Server tắt hoặc khi Admin ra lệnh reset/xóa trận đấu hiện tại.
- **Ai quản lý:** 
  - Quản lý tập trung bởi Outbound Persistence Adapter (`InMemoryMatchStateRepository`) và điều phối qua `MatchApplicationService`.

### 2.3. Đội thi đấu (Team)
- **Khi nào được tạo:** 
  - Tạo ra khi Client gửi yêu cầu đăng ký đội chơi qua API `/api/match/register`.
- **Khi nào được cập nhật:**
  - Cập nhật điểm số `collectedUdon` mỗi khi Agent tuần tra của đội thu thập bánh Udon thành công.
  - Cập nhật số lần vi phạm spam `spamViolationCount` hoặc chuyển sang bị truất quyền thi đấu `disqualified` nếu vi phạm nặng.
  - Cập nhật trạng thái nộp bài `submittedPlan = true` khi đội hoàn tất nộp hành động.
- **Khi nào bị hủy:** 
  - Hủy cùng với sự hủy bỏ của Aggregate Root `MatchState`.
- **Ai quản lý:** 
  - Nằm trong cụm Aggregate của `MatchState` và chịu sự quản lý của Aggregate Root `MatchState`.

### 2.4. Điệp viên (Agent)
- **Khi nào được tạo:** 
  - Sinh ra tự động cùng lúc với việc tạo lập đội thi đấu `Team`. Hệ thống tạo 3 Agent xuất phát ở tọa độ mặc định (0, 0).
- **Khi nào được cập nhật:**
  - Cập nhật vị trí tọa độ `(posX, posY)` sau mỗi bước di chuyển hợp lệ trong lượt.
  - Cập nhật lượng xăng tiêu hao `fuel` hoặc lượng xăng được nạp đầy sau khi tiếp xăng.
  - Cập nhật số bước đi còn lại `remainingSteps`.
  - Cập nhật danh sách điểm `visitedSpotsToday` khi thu thập Udon trong ngày.
- **Khi nào bị hủy:** 
  - Hủy cùng thực thể `Team` sở hữu.
- **Ai quản lý:** 
  - Quản lý trực tiếp bởi thực thể `Team` sở hữu nó.

### 2.5. Kế hoạch nộp (Submission / Action)
- **Khi nào được tạo:** 
  - Được tạo mới mỗi lần đội chơi gửi yêu cầu nộp kế hoạch cho lượt đi qua API `/api/match/actions`.
- **Khi nào được cập nhật:**
  - Được ghi đè hoàn toàn kế hoạch mới nếu đội chơi thực hiện gửi lại yêu cầu API nhiều lần trong khoảng thời gian cho phép của Turn hiện tại.
- **Khi nào bị hủy:** 
  - Hệ thống tự động xóa sạch khi kết thúc Turn và bắt đầu sang ngày thi đấu mới (`nextDay`).
- **Ai quản lý:** 
  - Quản lý tạm thời bởi `MatchState` trong cấu trúc bản đồ `currentTurnActions`.

### 2.6. Mô phỏng & Kết quả (Simulation & Result)
- **Khi nào được tạo:** 
  - Quá trình mô phỏng (`Simulation`) được kích hoạt khi đến hạn chuyển ngày của Scheduler hoặc khi các đội gửi bài đầy đủ. Đối tượng kết quả `TurnSimulationResult` được tạo ra sau khi tính toán xong.
- **Khi nào được cập nhật:** 
  - Đối tượng kết quả này chỉ đọc (Read-only) và không cập nhật sau khi tạo lập.
- **Khi nào bị hủy:** 
  - Bị hủy sau khi được chuyển đổi thành DTO gửi về Client qua API hoặc lưu trữ vào lịch sử trận đấu.
- **Ai quản lý:** 
  - Khởi tạo bởi `MovementSimulator` và quản lý truyền nhận bởi `MatchApplicationService`.

---

## 3. Bảng tóm tắt quản lý Vòng đời Đối tượng

Dưới đây là bảng tổng hợp tóm tắt vai trò quản lý vòng đời đối với từng đối tượng nghiệp vụ chính trong kiến trúc mới:

| Đối tượng (Object) | Thực thể quản lý (Owner) | Phương thức Khởi tạo | Phương thức Hủy | Phạm vi hoạt động (Scope) |
| :--- | :--- | :--- | :--- | :--- |
| `MatchConfig` | `MatchApplicationService` | Khởi tạo khi load file cấu hình lúc bắt đầu trận. | Hủy khi kết thúc trận đấu. | Toàn bộ trận đấu (Bất biến). |
| `MatchState` | `InMemoryMatchStateRepository` | Khởi tạo lúc bắt đầu trận đấu hoặc khởi chạy Server. | Hủy khi tắt Server hoặc reset trận. | Toàn bộ trận đấu (Mutable). |
| `Team` | `MatchState` (Aggregate Root) | Khởi tạo qua yêu cầu đăng ký đội chơi của Client. | Hủy cùng MatchState. | Toàn bộ trận đấu. |
| `Agent` | `Team` | Tự sinh ra cùng với Team (3 Agent). | Hủy cùng Team sở hữu. | Toàn bộ trận đấu. |
| `Action` | `Agent` / `MatchState` | Khởi tạo khi Client nộp kế hoạch hành động. | Hủy khi kết thúc Turn sang ngày mới. | Trong phạm vi 1 Turn (Day). |
| `TurnSimulationResult` | `MovementSimulator` | Khởi tạo sau khi hoàn tất chạy mô phỏng di chuyển của Turn. | Giải phóng bộ nhớ sau khi trả response. | Trong phạm vi xử lý Request. |

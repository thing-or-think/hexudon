# 09. SƠ ĐỒ TUẦN TỰ HỆ THỐNG (SEQUENCE DIAGRAMS)

## Mục lục
1. [Luồng Khởi tạo và Đăng ký Đội chơi](#1-luồng-khởi-tạo-và-đăng-ký-đội-chơi)
2. [Luồng Khởi động Trận đấu (Start Match)](#2-luồng-khởi-động-trận-đấu-start-match)
3. [Luồng Nộp kế hoạch hành động và Chạy mô phỏng (Submit Actions & Simulation)](#3-luồng-nộp-kế-hoạch-hành-động-và-chạy-mô-phỏng-submit-actions--simulation)
4. [Luồng Tự động chuyển Ngày mới (Next Day Transition - Scheduler)](#4-luồng-tự-động-chuyển-ngày-mới-next-day-transition---scheduler)
5. [Luồng Truy vấn trạng thái trận đấu (Query Match State)](#5-luồng-truy-vấn-trạng-thái-trận-đấu-query-match-state)

---

## 1. Luồng Khởi tạo và Đăng ký Đội chơi

Quy trình xảy ra khi Client gửi yêu cầu tham gia thi đấu giải Hexudon:

```text
Client             MatchController     RegisterTeamUC     MatchAppService     MatchStateStore     MatchState (Aggregate)
  |                       |                   |                  |                   |                      |
  |-- POST /register ---->|                   |                  |                   |                      |
  |   (teamName)          |                   |                  |                   |                      |
  |                       |-- registerTeam -->|                  |                   |                      |
  |                       |   (teamName)      |-- registerTeam ->|                   |                      |
  |                       |                   |   (teamName)     |                   |                      |
  |                       |                   |                  |-- loadState ----->|                      |
  |                       |                   |                  |<-- MatchState ----|                      |
  |                       |                   |                  |                   |                      |
  |                       |                   |                  |-- registerTeam(team) ------------------->|
  |                       |                   |                  |   (Kiểm tra trạng thái WAITING & Thêm)   |
  |                       |                   |                  |<-- void ---------------------------------|
  |                       |                   |                  |                   |                      |
  |                       |                   |                  |-- saveState ----->|                      |
  |                       |                   |                  |   (Ghi đè RAM)    |                      |
  |                       |                   |                  |<-- void ----------|                      |
  |                       |<-- Team Object ---|<-- Team Object --|                   |                      |
  |                       |                                                                                 |
  |   (Ánh xạ DTO)        |                                                                                 |
  |<-- 200 OK (JSON) -----|                                                                                 |
  |   (TeamResponse)      |                                                                                 |
```

---

## 2. Luồng Khởi động Trận đấu (Start Match)

Quy trình Admin hoặc hệ thống kích hoạt bắt đầu trận đấu:

```text
Admin/System       MatchController      StartMatchUC      MatchAppService     ConfigLoaderPort    MatchStateStore     MatchState
  |                       |                   |                  |                   |                   |                |
  |-- POST /start ------->|                   |                  |                   |                   |                |
  |                       |-- startMatch ---->|                  |                   |                   |                |
  |                       |                   |-- startMatch --->|                   |                   |                |
  |                       |                   |                  |-- loadConfig ---->|                   |                |
  |                       |                   |                  |<-- MatchConfig ---|                   |                |
  |                       |                   |                  |                   |                   |                |
  |                       |                   |                  |-- loadState ─────────────────────────>|                |
  |                       |                   |                  |<-- MatchState ────────────────────────|                |
  |                       |                   |                  |                   |                   |                |
  |                       |                   |                  |-- start(maxFuel, maxSteps) ───────────────────────────>|
  |                       |                   |                  |   (status = PLAYING, nạp xăng/bước cho Agent, spot)    |
  |                       |                   |                  |<-- void ----------------------------------------------|
  |                       |                   |                  |                   |                   |                |
  |                       |                   |                  |-- saveState ─────────────────────────>|                |
  |                       |                   |                  |<-- void ------------------------------|                |
  |<-- 200 OK (Empty) ----|<-- void ----------|<-- void ---------|                   |                   |                |
```

---

## 3. Luồng Nộp kế hoạch hành động và Chạy mô phỏng (Submit Actions & Simulation)

Quy trình Client nộp kế hoạch hành động trong lượt và kích hoạt mô phỏng:

```text
Client             MatchController     SubmitActionsUC    MatchAppService     MatchStateStore    ActionValidator     MovementSimulator  FuelManager  UdonCollectionEngine
  |                       |                   |                  |                   |                  |                    |                |                |
  |-- POST /actions ----->|                   |                  |                   |                  |                    |                |                |
  |   Header:X-Team-Name  |                   |                  |                   |                  |                    |                |                |
  |   Body:DayActionReq   |                   |                  |                   |                  |                    |                |                |
  |                       |-- submitActions ->|                  |                   |                  |                    |                |                |
  |                       |                   |-- submitActions >|                   |                  |                    |                |                |
  |                       |                   |                  |-- loadState ----->|                  |                    |                |                |
  |                       |                   |                  |<-- MatchState ----|                  |                    |                |                |
  |                       |                   |                  |                   |                  |                    |                |                |
  |                       |                   |                  |-- validate ─────────────────────────>|                    |                |                |
  |                       |                   |                  |   (plans, config)                    |                    |                |                |
  |                       |                   |                  |<-- void ─────────────────────────────|                    |                |                |
  |                       |                   |                  |                   |                  |                    |                |                |
  |                       |                   |                  |-- simulateTeamTurn ──────────────────────────────────────>|                |                |
  |                       |                   |                  |   (Lặp tối đa 5 bước di chuyển của Agent)                 |                |                |
  |                       |                   |                  |   |                                                       |-- check cost ->|                |
  |                       |                   |                  |   |                                                       |<-- steps/fuel -|                |
  |                       |                   |                  |   |                                                       |                |                |
  |                       |                   |                  |   |-- autoRefuel ────────────────────────────────────────>|                |
  |                       |                   |                  |   |   (Refuel & Patrol gặp nhau)                          |                |
  |                       |                   |                  |   |<-- void ──────────────────────────────────────────────|                |
  |                       |                   |                  |   |                                                       |                |                |
  |                       |                   |                  |   |-- collectUdon ────────────────────────────────────────────────────────>|
  |                       |                   |                  |   |   (Patrol đứng Spot chưa thu hoạch)                   |                |
  |                       |                   |                  |   |<-- void ──────────────────────────────────────────────────────────────|
  |                       |                   |                  |<-- List<AgentExecutionResult> ────────────────────────────|                |                |
  |                       |                   |                  |                   |                  |                    |                |                |
  |                       |                   |                  |-- setSubmittedPlan(true) -> (Ghi nhận đội nộp bài)         |                |                |
  |                       |                   |                  |-- saveState ----->|                  |                    |                |                |
  |                       |                   |                  |<-- void ----------|                  |                    |                |                |
  |                       |<-- SimResult -----|<-- SimResult ----|                   |                  |                    |                |                |
  |                       |   (TurnSimResult) |                  |                   |                  |                    |                |                |
  |   (Ánh xạ DTO)        |                   |                  |                   |                  |                    |                |                |
  |<-- 200 OK (JSON) -----|                   |                  |                   |                  |                    |                |                |
```

---

## 4. Luồng Tự động chuyển Ngày mới (Next Day Transition - Scheduler)

Tiến trình chạy ngầm thực hiện việc kiểm tra thời gian hết lượt hoặc cả hai đội nộp bài đủ để tự động chuyển ngày:

```text
SchedulerThread                     MatchAppService                     MatchStateStore                     MatchState (Aggregate Root)
      |                                    |                                   |                                         |
      |-- (Mỗi giây kích hoạt một lần) ---->|                                   |                                         |
      |                                    |-- loadState ─────────────────────>|                                         |
      |                                    |<-- MatchState ────────────────────|                                         |
      |                                    |                                   |                                         |
      |                                    |-- (Kiểm tra điều kiện chuyển ngày:                                          |
      |                                    |    - Hết thời gian: currentTime - turnStartTime > turnTimeLimitMs           |
      |                                    |    - HOẶC Cả hai đội đã hoàn thành nộp bài: submittedPlan = true)            |
      |                                    |                                   |                                         |
      |                                    |-- nextDay() ───────────────────────────────────────────────────────────────>|
      |                                    |   - Tăng currentTurn = currentTurn + 1                                      |
      |                                    |   - (Nếu currentTurn > maxTurns -> status = FINISHED)                       |
      |                                    |   - Reset quota xăng (initialFuel) và bước đi cho Agent                     |
      |                                    |   - Reset kho Udon tại toàn bộ các Spot bản đồ                              |
      |                                    |   - Xóa lịch sử hành động lượt cũ (clearTurnActions)                        |
      |                                    |   - Đặt turnStartTime = System.currentTimeMillis()                          |
      |                                    |<-- void ────────────────────────────────────────────────────────────────────|
      |                                    |                                   |                                         |
      |                                    |-- saveState ─────────────────────>|                                         |
      |                                    |<-- void ──────────────────────────|                                         |
```

---

## 5. Luồng Truy vấn trạng thái trận đấu (Query Match State)

Client gửi yêu cầu kiểm tra tiến độ trận đấu bất đồng bộ:

```text
Client             MatchController     GetMatchStateUC    MatchAppService     MatchStateStore
  |                       |                   |                  |                   |
  |-- GET /state -------->|                   |                  |                   |
  |                       |-- getMatchState ->|                  |                   |
  |                       |                   |-- getMatchState >|                   |
  |                       |                   |                  |-- loadState ----->|
  |                       |                   |                  |<-- MatchState ----|
  |                       |<-- MatchState ----|<-- MatchState ---|                   |
  |                       |   (Domain Object) |                  |                   |
  |   (Ánh xạ DTO)        |                   |                  |                   |
  |<-- 200 OK (JSON) -----|                   |                  |                   |
```

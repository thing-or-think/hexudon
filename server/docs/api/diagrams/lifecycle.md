# Vòng đời trận đấu & API (Lifecycle Diagram)

Tài liệu này mô tả vòng đời của một trận đấu HEXUDON và sự chuyển đổi giữa các trạng thái thông qua các API tương ứng.

---

## 1. Sơ đồ trạng thái Trận đấu (Game State Machine)

```mermaid
stateDiagram-v2
    [*] --> Uninitialized: Server Ready

    state Uninitialized {
        [*] --> MapGenerated: Admin POST /generate
        MapGenerated --> GameCreated: Admin POST /init
    }

    Uninitialized --> WaitingForAgents: Admin POST /init (Tạo game)

    state WaitingForAgents {
        [*] --> SelectingTypes: Teams GET /config hoặc /board
        SelectingTypes --> TypesSubmitted: Team POST /agent-types
    }

    WaitingForAgents --> Playing: Hết thời gian chờ / Đủ thông tin

    state Playing {
        [*] --> DayStart: Server mở Ngày (Day i)
        DayStart --> FetchDayInfo: Team GET /day
        FetchDayInfo --> PlanActions: Đội tính toán kế hoạch di chuyển
        PlanActions --> ActionsSubmitted: Team POST /actions (hoặc /practice/actions)
        ActionsSubmitted --> ResolveDay: Server mô phỏng & tính kết quả Ngày i
        ResolveDay --> DayStart: Chuyển sang Ngày i+1 (Nếu i < TotalDays - 1)
    }

    Playing --> Finished: Đã hoàn thành tất cả các ngày

    state Finished {
        [*] --> ViewResults: GET /result
        ViewResults --> ViewStateAndReplay: GET /state, /replay, /actions
    }

    Finished --> [*]

    note right of Playing
        Trong quá trình Playing:
        - Admin có thể POST /reset để đưa game về WaitingForAgents
        - Admin có thể DELETE /{game_id} để hủy game
        - Đội chơi Practice có thể POST /practice/copy hoặc /practice/reset
    end note
```

---

## 2. Vòng đời sử dụng API theo góc nhìn Client (Bot Lifecycle Flowchart)

```mermaid
flowchart TD
    Start([Bắt đầu Bot]) --> ReadConfig[1. Gọi GET /api/game/config hoặc /board]
    ReadConfig --> ParseConfig[Phân tích bản đồ, ô cửa hàng spots, số bước, nhiên liệu]
    ParseConfig --> SelectAgents[2. Gọi POST /api/game/agent-types<br>Chỉ định loại tuần tra / nạp nhiên liệu]

    SelectAgents --> DayLoop{Còn Ngày tiếp theo?}

    DayLoop -- Có --> GetDayState[3. Gọi GET /api/game/day<br>Đọc vị trí hiện tại, nhiên liệu, tình trạng giao thông]
    GetDayState --> ComputePath[Tính toán lộ trình tối ưu cho từng Agent]
    ComputePath --> SubmitActions[4. Gọi POST /api/game/actions<br>Gửi kế hoạch di chuyển ngày]
    SubmitActions --> DayLoop

    DayLoop -- Không (Hết trận) --> FetchResult[5. Gọi GET /api/game/result<br>Đọc bảng xếp hạng và điểm số]
    FetchResult --> End([Kết thúc Trận đấu])

    subgraph ModifyingAndInspection [Tính năng phụ trợ & Luyện tập]
        FetchState[GET /api/game/state]
        FetchReplay[GET /api/game/replay]
        PracticePeer[GET /api/game/practice/peer]
        PracticeCopy[POST /api/game/practice/copy]
        PracticeReset[POST /api/game/practice/reset]
    end
```

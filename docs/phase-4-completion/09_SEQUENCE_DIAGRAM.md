# KỊCH BẢN LUỒNG XỬ LÝ HỆ THỐNG (SEQUENCE DIAGRAMS)

Tài liệu này mô tả kịch bản luồng xử lý chi tiết của 3 nghiệp vụ quan trọng nhất trong Giai đoạn 4 bằng ngôn ngữ biểu đồ tuần tự Mermaid.

---

## 1. Luồng Gửi hành động và Xử lý Di chuyển của Agent

Luồng này mô tả từ lúc Bot gửi request hành động, qua hệ thống bù trễ và xếp hàng, cho tới khi luồng mô phỏng chạy tính toán chi phí di chuyển và cập nhật tọa độ mới của Agent.

```mermaid
sequenceDiagram
    autonumber
    actor Bot as Bot của Đội chơi
    participant Ctrl as MatchController (REST Adapter)
    participant Ord as RequestOrderingService (Infra)
    participant Q as TurnExecutionQueue (Infra)
    participant Sim as GameSimulationThread (App Service)
    participant Dom as MatchState (Domain Aggregate)
    participant Calc as MovementCostCalculator (Domain Service)

    Note over Bot, Ctrl: Mở Turn mới - Bot nhận tín hiệu bắt đầu lượt
    Bot->>Ctrl: POST /api/v1/matches/{matchId}/actions (SubmitActionsRequest + clientDurationMs)
    activate Ctrl
    Ctrl->>Ord: orderRequest(teamId, clientDurationMs)
    activate Ord
    Ord->>Ord: Tính toán VST = ServerTurnStartTime + clientDurationMs
    Ord-->>Ctrl: Trả về Virtual Submission Timestamp (VST)
    deactivate Ord
    Ctrl->>Q: push(teamId, VST, Actions)
    activate Q
    Q-->>Ctrl: Xác nhận tiếp nhận hành động (ACCEPTED)
    deactivate Q
    Ctrl-->>Bot: Phản hồi HTTP 202 Accepted (SubmitActionsResponse)
    deactivate Ctrl

    Note over Sim, Dom: Luồng mô phỏng tự động kích hoạt khi hết Turn
    Sim->>Q: acquireAndLockQueue()
    activate Q
    Q-->>Sim: Trả về Map danh sách hành động của các đội
    deactivate Q
    Sim->>Sim: Sắp xếp các đội theo thứ tự VST tăng dần
    
    loop Với từng Bước đi (Step) từ maxStepsPerTurn về 1
        loop Với từng Agent của từng Đội (đã xếp thứ tự theo VST)
            Sim->>Dom: executeAction(agent, action)
            activate Dom
            Dom->>Calc: calculate(terrain, trafficState, config)
            activate Calc
            Calc-->>Dom: Trả về MovementCost (fuelNeeded, stepsNeeded)
            deactivate Calc
            Dom->>Dom: Khóa chi phí di chuyển cho bước hiện tại
            Dom->>Dom: Trừ nhiên liệu và cập nhật tọa độ Agent
            Dom-->>Sim: Trả về kết quả di chuyển (MoveResult)
            deactivate Dom
        end
    end
```

---

## 2. Luồng Đóng Turn và Tính toán Giao thông - Điểm số - Xếp hạng

Kịch bản vòng lặp đóng Turn (Turn Execution Loop) được thực thi bởi luồng mô phỏng để tính toán giao thông dựa trên lịch sử dừng chân, cập nhật trạng thái đường đi, tính điểm số và thiết lập bảng thứ tự xếp hạng mới.

```mermaid
sequenceDiagram
    autonumber
    participant Sim as GameSimulationThread (App Service)
    participant Dom as MatchState (Domain Aggregate)
    participant TrafficSvc as TrafficCalculationService (App Service)
    participant TrafficCalc as TrafficCalculator (Domain Service)
    participant ScoreSvc as ScoringAndRankingService (App Service)
    participant RankDom as RankingService (Domain Service)
    participant SnapRepo as MatchSnapshotRepository (Outbound Port)

    Note over Sim: Kết thúc mô phỏng các hành động trong lượt T
    Sim->>TrafficSvc: updateTrafficForNextTurn(matchState, config)
    activate TrafficSvc
    loop Với mỗi ô bản đồ có TerrainType = ROAD
        TrafficSvc->>Dom: Lấy stay steps của lượt T và lượt T-1
        Dom-->>TrafficSvc: Trả về stay steps (Turn T, Turn T-1)
        TrafficSvc->>TrafficCalc: calculateFlow(stayStepsT, stayStepsT_1, totalTeams)
        activate TrafficCalc
        TrafficCalc-->>TrafficSvc: Trả về calculatedFlow (double)
        deactivate TrafficCalc
        TrafficSvc->>TrafficCalc: determineState(calculatedFlow, threshold)
        activate TrafficCalc
        TrafficCalc-->>TrafficSvc: Trả về RoadTrafficState (SMOOTH/CONGESTED/JAM)
        deactivate TrafficCalc
        TrafficSvc->>Dom: Cập nhật RoadTrafficState cho ô đường nhựa
    end
    TrafficSvc-->>Sim: Hoàn thành cập nhật giao thông
    deactivate TrafficSvc

    Sim->>ScoreSvc: recalculateScoresAndRankings(matchState, config)
    activate ScoreSvc
    ScoreSvc->>Dom: Lấy dữ liệu điểm số TeamScore của các đội
    Dom-->>ScoreSvc: Trả về danh sách TeamScore hiện thời
    loop So sánh cặp các đội
        ScoreSvc->>RankDom: compareTeams(teamScore1, teamScore2)
        activate RankDom
        Note over RankDom: Thực hiện so sánh qua 5 cấp độ (Anti-tie-break)
        RankDom-->>ScoreSvc: Trả về kết quả so sánh (-1 / 0 / 1)
        deactivate RankDom
    end
    ScoreSvc->>ScoreSvc: Sắp xếp danh sách đội chơi thành bảng xếp hạng mới
    ScoreSvc-->>Sim: Hoàn thành cập nhật bảng thứ hạng
    deactivate ScoreSvc

    Sim->>Dom: nextDay(config)
    Note over Dom: Chuyển trạng thái trận đấu sang Turn T + 1
    Sim->>SnapRepo: saveSnapshot(MatchSnapshot)
    Note over Sim, SnapRepo: Ghi lại điểm phục hồi (Recovery Point)
    Sim-->>Sim: Phát tín hiệu WebSocket bắt đầu Turn T + 1
```

---

## 3. Luồng Xử lý Lỗi và Khôi phục Hệ thống (Match Recovery Flow)

Kịch bản diễn ra khi máy chủ bị sập đột ngột giữa lượt chơi $T$ và tự động khôi phục lại trạng thái nhất quán của lượt trước đó ($T-1$) khi khởi động lại.

```mermaid
sequenceDiagram
    autonumber
    actor Admin as Trọng tài (Admin)
    participant App as Máy chủ Spring Boot (Application)
    participant RecSvc as MatchRecoveryService (App Service)
    participant SnapRepo as MatchSnapshotRepository (Outbound Port)
    participant EventRepo as GameEventRepository (Outbound Port)
    participant Dom as MatchState (Domain Aggregate)
    actor Bot as Bot các Đội chơi

    Note over App: Khởi động lại hệ thống sau sự cố sập nguồn
    App->>RecSvc: autoRecoverLatestState()
    activate RecSvc
    RecSvc->>SnapRepo: findLatestRecoveryPoint()
    activate SnapRepo
    SnapRepo-->>RecSvc: Trả về RecoveryPoint (chứa Snapshot của Turn T-1)
    deactivate SnapRepo
    
    RecSvc->>RecSvc: Giải nén và đọc dữ liệu MatchSnapshot
    RecSvc->>Dom: Khôi phục MapState, AgentState, TeamScores về Turn T-1
    
    RecSvc->>EventRepo: deleteEventsAndLogsAfterTurn(T-1)
    activate EventRepo
    Note over EventRepo: Xóa toàn bộ sự kiện rác ghi nhận dở dang của Turn T
    EventRepo-->>RecSvc: Xác nhận dọn dẹp xong
    deactivate EventRepo

    RecSvc->>Dom: Thiết lập currentTurn = T
    RecSvc->>Dom: Đặt trạng thái trận đấu = PLAYING
    RecSvc-->>App: Hoàn thành phục hồi trạng thái nhất quán
    deactivate RecSvc

    App-->>Bot: Gửi thông báo WebSocket: Trận đấu khôi phục tại đầu Turn T
    Bot->>App: Gửi lại hành động của Turn T (chạy lại lượt chơi bị sập)
```

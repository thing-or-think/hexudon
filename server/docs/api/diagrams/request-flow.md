# Luồng gọi API (Request Flow)

Tài liệu này mô tả chi tiết các luồng tương tác API giữa Đội (Bot/Client), Quản trị viên (Admin UI/Server) và Máy chủ HEXUDON.

---

## 1. Luồng khởi tạo & Quản lý trận đấu (Admin Flow)

Mô tả cách Admin tạo bản đồ, khởi tạo trận đấu, thêm đội, reset hoặc xóa game.

```mermaid
sequenceDiagram
    autonumber
    actor Admin as Quản trị viên
    participant Server as Máy chủ HEXUDON

    rect rgb(240, 248, 255)
        note over Admin,Server: 1. Xem trước & Tạo bản đồ
        Admin->>Server: POST /api/game/generate
        Server-->>Admin: Map & Spots Preview
    end

    rect rgb(255, 250, 240)
        note over Admin,Server: 2. Khởi tạo game chính thức
        Admin->>Server: POST /api/game/init (game_id, map, spots, teams...)
        Server-->>Admin: 204 No Content
    end

    opt Thêm đội giữa trận
        Admin->>Server: POST /api/game/teams
        Server-->>Admin: 204 No Content
    end

    opt Đặt lại game (Reset)
        Admin->>Server: POST /api/game/reset
        Server-->>Admin: 204 No Content
    end

    opt Xóa game
        Admin->>Server: DELETE /api/game/{game_id}
        Server-->>Admin: 204 No Content
    end
```

---

## 2. Luồng thi đấu chính thức (Tournament Match Flow)

Mô tả toàn bộ vòng thi đấu của Đội tham gia: từ đọc cấu hình, chọn loại Agent, nộp kế hoạch hàng ngày đến nhận kết quả chung cuộc.

```mermaid
sequenceDiagram
    autonumber
    actor Team as Đội thi đấu (Bot)
    participant Server as Máy chủ HEXUDON

    rect rgb(235, 245, 255)
        note over Team,Server: Giai đoạn 1: Chuẩn bị trước trận đấu
        Team->>Server: GET /api/game/config?game_id={id} (hoặc /api/game/board)
        Server-->>Team: Cấu hình bản đồ, thời gian, vị trí Agent, ngưỡng
        Team->>Server: POST /api/game/agent-types (chọn loại Agent)
        Server-->>Team: 204 No Content
    end

    rect rgb(245, 255, 245)
        note over Team,Server: Giai đoạn 2: Lặp theo từng Ngày (Day 0..N-1)
        loop Mỗi Ngày (Day)
            Team->>Server: GET /api/game/day?game_id={id}
            Server-->>Team: Trạng thái vị trí, nhiên liệu, giao thông ngày hiện tại
            Team->>Server: POST /api/game/actions (Nộp kế hoạch di chuyển)
            Server-->>Team: 204 No Content
        end
    end

    rect rgb(255, 245, 245)
        note over Team,Server: Giai đoạn 3: Kết thúc & Tra cứu
        Team->>Server: GET /api/game/result?game_id={id}
        Server-->>Team: Bảng xếp hạng & Điểm chi tiết
        opt Tra cứu trạng thái & Replay
            Team->>Server: GET /api/game/state?game_id={id}
            Server-->>Team: Trạng thái toàn cục trận đấu
            Team->>Server: GET /api/game/replay?game_id={id}
            Server-->>Team: Chi tiết diễn biến từng bước các ngày
            Team->>Server: GET /api/game/actions?game_id={id}
            Server-->>Team: Lịch sử các hành động đã nộp
        end
    end
```

---

## 3. Luồng luyện tập tự chọn tốc độ (Practice Mode Flow)

Mô tả luồng thi đấu chế độ Luyện tập (Practice Mode), hỗ trợ tự nộp kế hoạch, so sánh với đội khác, sao chép tiến trình (fork) và đặt lại bài tập.

```mermaid
sequenceDiagram
    autonumber
    actor Team as Đội thi đấu (Bot)
    participant Server as Máy chủ HEXUDON

    rect rgb(250, 240, 255)
        note over Team,Server: 1. Khởi tạo & Đọc bảng luyện tập
        Team->>Server: GET /api/game/board?game_id={id}
        Server-->>Team: Thông tin bảng đấu luyện tập
        Team->>Server: POST /api/game/agent-types
        Server-->>Team: 204 No Content
    end

    rect rgb(245, 255, 250)
        note over Team,Server: 2. Vòng lặp luyện tập theo tốc độ riêng
        loop Nộp bài từng ngày
            Team->>Server: POST /api/game/practice/actions
            Server-->>Team: 204 No Content
        end
    end

    rect rgb(255, 255, 240)
        note over Team,Server: 3. Tính năng hỗ trợ Luyện tập
        opt Xem lịch sử bài làm của đội khác
            Team->>Server: GET /api/game/practice/peer?game_id={id}
            Server-->>Team: Lịch sử diễn biến của các đội khác
        end

        opt Sao chép (Fork) tiến trình đội khác
            Team->>Server: POST /api/game/practice/copy
            Server-->>Team: 204 No Content
        end

        opt Làm lại từ đầu (Reset practice)
            Team->>Server: POST /api/game/practice/reset
            Server-->>Team: 204 No Content
        end
    end
```

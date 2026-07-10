# 06. THIẾT KẾ TRẠNG THÁI VÀ VÒNG ĐỜI TRẬN ĐẤU (MATCH STATE DESIGN)

## Mục lục
1. [Sơ đồ trạng thái trận đấu (ASCII State Transition Diagram)](#1-sơ-đồ-trạng-thái-trận-đấu-ascii-state-transition-diagram)
2. [Vòng đời các đối tượng chính (Object Lifecycle)](#2-vòng-đời-các-đối-tượng-chính-object-lifecycle)
3. [Các trạng thái và quy tắc chuyển trạng thái (State Transitions)](#3-các-trạng-thái-và-quy-tắc-chuyển-trạng-thái-state-transitions)
4. [Mô hình quan hệ giữa các thực thể Domain (Entity Relationships)](#4-mô-hình-quan-er-giữa-các-thực-thể-domain-entity-relationships)
5. [Quy tắc đồng bộ dữ liệu (Data Synchronization Rules)](#5-quy-tắc-đồng-bộ-dữ-liệu-data-synchronization-rules)

---

## 1. Sơ đồ trạng thái trận đấu (ASCII State Transition Diagram)

Dưới đây là sơ đồ ASCII thể hiện vòng đời trạng thái của một trận đấu Hexudon:

```text
       +-------------------------------------------------+
       |                                                 |
       |                   [ KHỞI TẠO ]                  |
       |                        |                        |
       +------------------------|------------------------+
                                v
       +-------------------------------------------------+
       |                                                 |
       |                    WAITING                      |
       |  (Đang chờ các đội chơi đăng ký tham gia)        |
       |  - Đăng ký Team (tối đa maxTeams)               |
       |                                                 |
       +------------------------|------------------------+
                                |
                                | (Gọi startMatch)
                                v
       +-------------------------------------------------+
       |                                                 |
       |                    PLAYING                      |
       |  (Trận đấu đang diễn ra - Lặp Turn/Day)          |
       |  - Nộp kế hoạch (submitActions)                 |
       |  - Tự động chạy Simulator khi hết giờ/đủ bài    |
       |  - Tăng ngày mới (nextDay)                      |
       |                                                 |
       +------------------------|------------------------+
                                |
                                | (currentTurn > maxTurns)
                                v
       +-------------------------------------------------+
       |                                                 |
       |                    FINISHED                     |
       |  (Trận đấu kết thúc - Không nhận request mới)   |
       |  - Lưu trữ kết quả chung cuộc                  |
       |                                                 |
       +-------------------------------------------------+
```

---

## 2. Vòng đời các đối tượng chính (Object Lifecycle)

Trong hệ thống Hexudon Game Server, trạng thái được quản lý tập trung thông qua cụm Aggregate Root `MatchState`.

- **MatchState (Trạng thái trận đấu):**
  - *Khởi tạo:* Được tạo ra khi khởi chạy Game Server hoặc tạo mới một trận đấu. Trạng thái mặc định là `WAITING`.
  - *Cập nhật:* Thay đổi danh sách đội chơi, lưới bản đồ, điểm số Udon và ngày thi đấu hiện tại.
  - *Hủy bỏ:* Bị hủy khi Reset trận đấu hoặc xóa khỏi bộ nhớ.
- **Team (Đội thi đấu):**
  - *Khởi tạo:* Tạo mới khi Controller tiếp nhận API đăng ký đội chơi (`registerTeam`).
  - *Cập nhật:* Nhận danh sách 3 Agent, cộng điểm Udon khi thu thập được, ghi nhận số lần vi phạm rate limit hoặc chuyển trạng thái sang bị loại (`disqualified`).
  - *Hủy bỏ:* Hủy cùng với `MatchState`.
- **Agent (Điệp viên):**
  - *Khởi tạo:* Tự động sinh ra (3 Agent: 2 Patrol, 1 Refuel) ngay khi đội thi đấu đăng ký thành công. Tọa độ xuất phát là `(0, 0)`.
  - *Cập nhật:* Di chuyển thay đổi tọa độ `(posX, posY)`, tiêu hao nhiên liệu `fuel`, tiêu hao quota bước đi `remainingSteps`, lưu vết các Spot đã thu thập udon trong ngày.
- **Submission / Action (Kế hoạch hành động):**
  - *Khởi tạo:* Tạo mới mỗi lần đội chơi gửi HTTP request nộp bài cho lượt chơi.
  - *Cập nhật:* Thay thế kế hoạch cũ nếu đội chơi nộp đè trước khi bước vào mô phỏng.
  - *Hủy bỏ:* Xóa sạch khi hệ thống chuyển sang ngày mới (`clearTurnActions`).

---

## 3. Các trạng thái và quy tắc chuyển trạng thái (State Transitions)

Hệ thống áp dụng các quy tắc chuyển trạng thái chặt chẽ dưới sự kiểm soát của Aggregate Root `MatchState`:

### 3.1. WAITING -> PLAYING
- **Điều kiện kích hoạt:** Gọi phương thức `startMatch` thông qua API của Admin hoặc hệ thống kích hoạt tự động.
- **Quy tắc bắt buộc:**
  - Trạng thái hiện tại phải là `WAITING`.
  - Phải có ít nhất 1 đội đăng ký thành công (`teams` không rỗng).
- **Hành động đi kèm:**
  - Trạng thái chuyển sang `PLAYING`.
  - Lượt chơi đặt thành `currentTurn = 1`.
  - Sạc đầy năng lượng (`maxFuel`) và cấp quota bước đi tối đa (`maxStepsPerTurn`) cho tất cả các Agents.
  - Thiết lập lại số lượng bánh Udon tại tất cả các Spot trên bản đồ về mức cấu hình ban đầu.
  - Ghi nhận thời gian bắt đầu lượt (`turnStartTime`).

### 3.2. PLAYING -> PLAYING (Day/Turn Transition)
- **Điều kiện kích hoạt:** Hết thời gian giới hạn lượt (`turnTimeLimitMs`) kiểm soát bởi Scheduler hoặc khi cả hai đội chơi hoàn thành nộp bài sớm.
- **Hành động đi kèm:**
  - Tăng `currentTurn` thêm 1 đơn vị.
  - Xóa sạch danh sách hành động nộp của lượt cũ (`clearTurnActions`).
  - Sạc lại tài nguyên bình xăng mặc định (`initialFuel`) và quota bước đi của lượt cho toàn bộ Agent.
  - Xóa lịch sử ghé thăm Spot trong ngày của các Agent.
  - Thiết lập lại số lượng bánh Udon tại toàn bộ Spot bản đồ (theo luật chơi bánh Udon hồi phục mỗi ngày).
  - Cập nhật lại thời gian bắt đầu lượt mới (`turnStartTime`).

### 3.3. PLAYING -> FINISHED
- **Điều kiện kích hoạt:** Khi lượt chơi hiện tại vượt quá số lượng lượt cho phép (`currentTurn > maxTurns`) trong quá trình chuyển ngày.
- **Hành động đi kèm:**
  - Chuyển `status` sang `FINISHED`.
  - Ngăn chặn hoàn toàn mọi yêu cầu nộp hành động từ các đội thi đấu.

---

## 4. Mô hình quan hệ giữa các thực thể Domain (Entity Relationships)

Cấu trúc phân cấp mối quan hệ giữa các đối tượng trong Domain Core được biểu diễn như sau:

- **MatchState (1) <--> (0..2) Team:** Một trận đấu quản lý tối đa 2 đội thi đấu. Một đội thi đấu chỉ tồn tại trong ngữ cảnh của 1 trận đấu.
- **Team (1) <--> (3) Agent:** Mỗi đội thi đấu bắt buộc phải có chính xác 3 Agent (2 Patrol Agent và 1 Refuel Agent).
- **MatchState (1) <--> (1..*) Cell:** Lưới bản đồ gồm nhiều ô lục giác, được tạo lập một lần duy nhất lúc khởi tạo trận đấu.
- **MatchState (1) <--> (0..*) Road:** Các liên kết đường đi giữa các Cell.
- **MatchState (1) <--> (0..*) Spot:** Các điểm đặc biệt chứa Udon nằm trên các Cell cụ thể.
- **Agent (1) <--> (0..5) Action:** Mỗi Agent trong một Turn có thể có tối đa một chuỗi gồm 5 hành động.

---

## 5. Quy tắc đồng bộ dữ liệu (Data Synchronization Rules)

Do game server chạy in-memory và tiếp nhận các yêu cầu HTTP đồng thời (Concurrent Requests) từ các đội chơi, kết hợp với tiến trình lập lịch tự động chạy ngầm (Spring Scheduler Thread), việc bảo đảm tính nhất quán dữ liệu là vô cùng quan trọng:

- **Đồng bộ hóa luồng (Thread-Safety):** 
  - Mọi thao tác ghi hoặc thay đổi trạng thái trận đấu (như đăng ký đội, nộp hành động, chuyển ngày) trên Aggregate Root `MatchState` phải được bao bọc trong cơ chế đồng bộ hóa (Thread synchronization) tại Application Service hoặc Repository.
- **Ràng buộc nộp bài:**
  - Khi một đội chơi đã nộp bài thành công, cờ `submittedPlan` của đội đó được đặt thành `true`. Đội chơi vẫn được phép gửi lại kế hoạch mới để đè kế hoạch cũ miễn là thời gian của lượt chưa kết thúc.
  - Tiến trình mô phỏng chỉ được đọc thông tin hành động của Agent sau khi thời gian lượt đã khóa hoặc khi điều kiện chạy sớm được thỏa mãn. Điều này ngăn chặn việc đọc dữ liệu dở dang (Dirty Read) khi Client đang thực hiện gửi request.
- **Reset trạng thái lượt sạch sẽ:**
  - Khi chuyển ngày mới, việc dọn dẹp các biến đệm hành động cũ (`clearTurnActions`) và sạc lại năng lượng Agent phải được hoàn tất trước khi ghi nhận timestamp mới cho `turnStartTime`. Quy trình này đảm bảo tính nhất quán của giao dịch trạng thái.

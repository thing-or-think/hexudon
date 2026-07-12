# DANH SÁCH KIỂM TRA NGHIỆM THU CHẤT LƯỢNG (DEFINITION OF DONE)

Tài liệu này cung cấp danh sách kiểm tra (Checklist) nghiệm thu chất lượng thiết kế và triển khai cho Giai đoạn 4, tập trung xử lý các trường hợp biên (Edge cases) khốc liệt nhất của hệ thống.

---

## 1. Tiêu chí Hoàn thành Chung (General Definition of Done)

*   [ ] **Không vi phạm Quy tắc Kiến trúc Hexagonal**: Tầng Domain không chứa bất kỳ Spring Framework imports hoặc dependencies bên ngoài nào.
*   [ ] **Không rò rỉ Thực thể JPA/Mongo**: Lớp Entity của cơ sở dữ liệu không được truyền trực tiếp vào hoặc ra khỏi tầng Domain. Phải chuyển đổi qua Mapper ở tầng Application.
*   [ ] **Bao phủ kiểm thử (Test Coverage)**: Mỗi tính năng nghiệp vụ mới đều phải có Unit Test độc lập. Độ bao phủ tối thiểu của các Domain Services mới là `95%`.
*   [ ] **Tính Bất biến (Immutability)**: Mọi Value Object mới đều là bất biến.
*   [ ] **Mô tả Không chứa mã nguồn**: Toàn bộ tài liệu thiết kế không chứa code Java, Python hay pseudo-code.

---

## 2. Danh sách kiểm tra Nghiệp vụ và xử lý Trường hợp biên (Edge Cases)

### A. Phân hệ Giao thông Động (Traffic Flow)
*   [ ] **Xử lý số lượng đội bằng 0 (Division by Zero)**:
    *   *Kịch bản*: Trận đấu thử nghiệm hệ thống không có đội nào đăng ký (`totalTeams = 0`).
    *   *Yêu cầu*: Thuật toán tại `TrafficCalculator.calculateFlow()` phải chặn giá trị 0 này và trả về kết quả lưu lượng mặc định bằng `0.0`, không ném ra lỗi ngoại lệ `ArithmeticException` gây sập máy chủ.
*   [ ] **Phân định mức giao thông**:
    *   *Kịch bản*: Lưu lượng calculated flow đúng bằng giá trị ngưỡng `traffic.threshold.congested` hoặc `traffic.threshold.jam`.
    *   *Yêu cầu*: Phải so sánh chính xác để phân định trạng thái nghẽn đúng theo mô hình biên mở trái đóng phải (Ví dụ: `calculatedFlow >= congestedLimit` và `< jamLimit` thuộc trạng thái `CONGESTED`).

### B. Chi phí Di chuyển địa hình (Movement Cost)
*   [ ] **Chốt chi phí cố định (Cost Lock)**:
    *   *Kịch bản*: Agent bắt đầu bước di chuyển của Turn $T$ từ ô A sang ô B (ô B đang ở trạng thái kẹt xe `TRAFFIC_JAM`). Giữa Turn, do kịch bản hoặc lệnh quản trị, ô B được giải tỏa giao thông trở lại `SMOOTH`.
    *   *Yêu cầu*: Chi phí di chuyển của Agent phải được tính toán dựa trên trạng thái `TRAFFIC_JAM` tại đúng thời điểm bắt đầu lượt đi và khóa cứng lại cho bước đi này. Sự biến động giao thông diễn ra trong Turn không làm thay đổi chi phí đã trừ của Agent.
*   [ ] **Giới hạn chứa Agent trên ô bản đồ (Unlimited Capacity)**:
    *   *Kịch bản*: Có 100 Agent của tất cả các đội cùng thực hiện di chuyển vào chung một ô tọa độ $C$ tại cùng một bước đi.
    *   *Yêu cầu*: Hệ thống phải xử lý lưu vết thành công vị trí của toàn bộ 100 Agent này mà không ném lỗi trùng lặp vị trí. Sự chen chúc chỉ được thể hiện ở việc lưu lượng stay steps tại ô $C$ sẽ tăng vọt lên 100 lượt, dẫn tới nghẽn nặng cho các lượt chơi sau.

### C. Hệ thống Xếp hạng đấu trường (Ranking System)
*   [ ] **Hòa toàn diện ở 4 cấp độ đầu**:
    *   *Kịch bản*: Đội A và Đội B có cùng số lượng Udon độc nhất, cùng số Udon tích lũy theo ngày, cùng số servings thành công và tổng thời gian phản hồi mạng bằng nhau đến từng mili-giây.
    *   *Yêu cầu*: Hệ thống phải gọi hàm `resolveTie()` sử dụng thuật toán tung xúc xắc dựa trên phép băm giả ngẫu nhiên chuỗi ID trận đấu và tên đội để phân hạng một cách nhất quán (không thay đổi kết quả sau mỗi lần khởi động lại máy chủ).

### D. Xử lý Đồng thời & Bù trễ Mạng (Concurrency & Latency)
*   [ ] **Gửi Request muộn đúng 1 phần triệu giây (Microsecond Post-closing)**:
    *   *Kịch bản*: Một đội chơi gửi hành động muộn hơn thời gian đóng Turn (`turnStartTime + durationMs`) đúng $1$ microsecond (máy chủ tiếp nhận lúc thời gian trôi qua $2000.001$ ms).
    *   *Yêu cầu*: Bộ phận tiếp nhận của `TurnExecutionQueue` khi chuyển trạng thái sang `LOCKED` phải từ chối yêu cầu này ngay lập tức, ghi nhận thời gian phản hồi ở mức tối đa và tự động áp dụng hành động `WAIT` cho Agent của đội đó.
*   [ ] **Hai Agent của cùng một đội hoán đổi vị trí cho nhau (Agent Swap)**:
    *   *Kịch bản*: Trong cùng một bước đi của Turn, Agent 1 đang ở tọa độ $X_1$ di chuyển sang $X_2$, đồng thời Agent 2 đang ở tọa độ $X_2$ di chuyển sang $X_1$.
    *   *Yêu cầu*:
        1.  Hàng đợi hành động phải ghi nhận cả hai yêu cầu.
        2.  Thuật toán di chuyển tại `MatchState.simulateTurn()` không được phép đánh dấu xung đột vật lý và từ chối di chuyển. Hệ thống phải cho phép hai Agent đi ngang qua nhau và cập nhật tọa độ đích mới thành công vì bản đồ Hex của dự án hỗ trợ đứng chung ô.

### E. Tính Bất biến của Value Objects (Immutability Verification)
*   [ ] **Ngăn chặn sửa đổi thuộc tính**:
    *   *Hành động*: Kiểm tra mã nguồn các lớp: `Coordinate`, `MovementCost`, `UdonType`, `TrafficThreshold`, `MatchSnapshot`.
    *   *Yêu cầu*:
        1.  Mọi thuộc tính (fields) phải được khai báo với từ khóa `private final`.
        2.  Không định nghĩa bất kỳ phương thức thiết lập nào (không setter).
        3.  Nếu thuộc tính có kiểu dữ liệu là một đối tượng có thể thay đổi (Mutable object như List, Set, Map), constructor phải thực hiện copy sâu (`Collections.unmodifiableList`, `Collections.unmodifiableMap` hoặc sao chép phần tử) để tránh rò rỉ tham chiếu ra bên ngoài.

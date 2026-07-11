# DANH SÁCH KIỂM TRA NGHIỆM THU (DEFINITION OF DONE) - GIAI ĐOẠN 4

Tài liệu này định nghĩa Tiêu chuẩn hoàn thành (Definition of Done - DoD) và danh sách kiểm tra (Checklist) nghiệm thu chất lượng thiết kế/triển khai cho Giai đoạn 4, tập trung vào việc giải quyết các điều kiện biên và trường hợp cực đoan (Edge Cases).

---

## 1. Danh sách kiểm tra chất lượng theo Module nghiệp vụ

### 1.1. Module Giao thông động (Traffic Flow)
*   [ ] **Xử lý phép chia cho 0:** Đã tích hợp kiểm tra điều kiện biên: Nếu số lượng đội tham gia trận đấu bằng 0 (`teamCount = 0`), giá trị Calculated Flow mặc định trả về là `0.0` và trạng thái giao thông mặc định là `SMOOTH`.
*   [ ] **Bù đắp dữ liệu quá khứ:** Tại lượt chơi thứ 2 (`currentTurn = 2`), dữ liệu của 2 lượt trước (lượt T-2, tức lượt 0) không tồn tại. Hệ thống phải tự động gán dữ liệu lượt T-2 bằng dữ liệu của lượt T-1 (lượt 1) để tính toán bình thường.
*   [ ] **Cách ly luồng cập nhật:** Trạng thái giao thông động được tính toán và lưu trữ một lần duy nhất vào cuối mỗi lượt đấu. Bản đồ giao thông hoàn toàn bất biến trong suốt thời gian diễn ra các bước di chuyển của lượt đấu tiếp theo.

### 1.2. Module Chi phí di chuyển (Movement Cost)
*   [ ] **Chốt chi phí cố định (Cost Locking):** Chi phí di chuyển của Agent phải được tính toán dựa trên trạng thái giao thông tại thời điểm bắt đầu đi và chốt cố định trong suốt bước di chuyển đó. Mọi thay đổi mật độ giao thông xảy ra trong lượt không được ảnh hưởng đến chi phí xăng và bước đi của hành động hiện tại.
*   [ ] **Không giới hạn Agent đứng đồng thời:** Thiết kế của ô bản đồ (`Cell`) phải hỗ trợ chứa không giới hạn số lượng Agent đứng đồng thời tại một thời điểm bước đi mà không gây lỗi hoặc xung đột ghi đè dữ liệu.
*   [ ] **Ràng buộc địa hình cấm:** Di chuyển vào ô địa hình loại hồ nước (`POND`) phải bị phát hiện ở tầng Domain, ném ra ngoại lệ quy tắc chơi và hủy bỏ hành động ngay lập tức.

### 1.3. Module Tính điểm và Xếp hạng (Scoring & Ranking)
*   [ ] **Tính độc nhất của mì Udon:** Sử dụng cấu trúc kiểu dữ liệu Set (`Set<UdonType>`) để tự động loại bỏ các chủng loại mì Udon trùng lặp đã thu thập.
*   [ ] **Thuật toán xếp hạng 5 cấp (Anti-tie-break):** Triển khai đầy đủ 5 cấp so sánh ưu tiên tuyệt đối trong `RankingService`:
    1.  Số chủng loại Udon độc nhất đã thu thập (nhiều hơn xếp trên).
    2.  Tổng tích lũy Udon thu hoạch hàng ngày (nhiều hơn xếp trên).
    3.  Tổng số lần phục vụ mì Udon thành công (nhiều hơn xếp trên).
    4.  Tổng thời gian phản hồi API tích lũy (nhỏ hơn xếp trên).
    5.  Tung xúc xắc ngẫu nhiên (chỉ kích hoạt khi 4 chỉ số trên hòa điểm).
*   [ ] **Tái tung xúc xắc khi hòa:** Nếu giá trị xúc xắc ngẫu nhiên của 2 đội bằng nhau, hệ thống phải tự động thực hiện lại phép tung xúc xắc trong một vòng lặp cho đến khi có sự khác biệt để đảm bảo không bao giờ tồn tại trạng thái đồng hạng.

### 1.4. Module Khôi phục trạng thái (Match Recovery)
*   [ ] **Đảm bảo ghi Snapshot thành công:** Điểm khôi phục `RecoveryPoint` chỉ được đánh dấu cờ `validated = true` sau khi giao dịch ghi dữ liệu của bản chụp `MatchSnapshot` tương ứng xuống Database hoàn tất không lỗi.
*   [ ] **Dọn dẹp rác dữ liệu tương lai:** Quy trình khôi phục tự động hoặc yêu cầu tái đấu bắt buộc phải thực hiện xóa sạch toàn bộ các bản ghi sự kiện `GameEvent` và `TrafficHistory` của các lượt đấu lớn hơn hoặc bằng lượt khôi phục (`>= turnNumber + 1`) để tránh sai lệch dữ liệu phân tích.

---

## 2. Giải quyết các điều kiện biên cực đoan (Extreme Edge Cases)

### 2.1. Request đến muộn đúng 1 phần triệu giây (Microsecond Late Request)
*   **Tình huống:** Client gửi yêu cầu hành động và dữ liệu cập nhật đến Server đúng 1 phần triệu giây sau khi hệ thống chuyển trạng thái `isTurnClosed = true`.
*   **Yêu cầu nghiệm thu:**
    *   Hệ thống không được phép tiếp nhận xử lý yêu cầu này.
    *   REST Controller bắt buộc phải chặn đứng yêu cầu ngay ở tầng lọc, ném ra ngoại lệ đóng lượt và trả về mã phản hồi HTTP 400 Bad Request cho Client.
    *   Tuyệt đối không để xảy ra tình trạng luồng xử lý ghi dữ liệu vào hàng đợi ConcurrentMap khi luồng Scheduler đang thực hiện tráo đổi bộ đệm.

### 2.2. Va chạm hoán đổi vị trí Agent (Agent Swapping Position Collision)
*   **Tình huống:** Hai Agent của cùng một đội chơi hoán đổi vị trí trực tiếp cho nhau trong cùng một lượt (Agent A ở ô 1 di chuyển sang ô 2; đồng thời Agent B ở ô 2 di chuyển sang ô 1).
*   **Yêu cầu nghiệm thu:**
    *   Do ô bản đồ (`Cell`) hỗ trợ chứa không giới hạn số lượng Agent đứng đồng thời, hành động đi xuyên qua nhau này là hoàn toàn hợp lệ.
    *   Hệ thống phải cho phép hai Agent di chuyển bình thường, cập nhật tọa độ đích thành công mà không báo lỗi va chạm hoặc khóa chết luồng di chuyển.

### 2.3. Tính bất biến của Value Objects (Value Object Immutability)
*   **Tình huống:** Lập trình viên cố tình thay đổi thuộc tính của một đối tượng giá trị (như đổi tọa độ X của một `Coordinate` hoặc đổi `fuelCost` của `MovementCost`).
*   **Yêu cầu nghiệm thu:**
    *   Tất cả các lớp Value Object phải được định nghĩa dưới dạng Java `record` hoặc class khai báo từ khóa `final` với toàn bộ các thuộc tính là `private final`.
    *   Tuyệt đối không định nghĩa các phương thức Setter.
    *   Mọi thay đổi thông tin bắt buộc phải tạo ra một thực thể Value Object mới hoàn toàn.

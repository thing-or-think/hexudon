# VÒNG ĐỜI ĐỐI TƯỢNG VÀ XỬ LÝ ĐỒNG THỜI (OBJECT LIFECYCLE & CONCURRENCY) - GIAI ĐOẠN 4

Tài liệu này đặc tả cơ chế quản lý vòng đời đối tượng, kiến trúc đa luồng và chiến lược xếp hàng công bằng (Fair Ordering) chống lại ảnh hưởng của độ trễ mạng khi nhiều đội chơi gửi yêu cầu đồng thời trong một lượt đấu.

---

## 1. Cấu trúc Hàng đợi và Dịch vụ xử lý đồng thời

Tầng Application sử dụng hai thành phần chính để quản lý đa luồng:
1.  **TurnExecutionQueue (Hàng đợi lượt đấu):** Nơi tiếp nhận và lưu trữ tạm thời các hành động của các đội chơi trong suốt thời gian lượt đấu hiện tại diễn ra.
2.  **RequestOrderingService (Dịch vụ sắp xếp yêu cầu):** Chịu trách nhiệm chuẩn hóa thời gian và sắp xếp thứ tự ưu tiên các yêu cầu để đảm bảo tính công bằng trước khi chuyển giao sang luồng giả lập.

### 1.1. Cấu trúc lưu trữ trong bộ nhớ của TurnExecutionQueue
*   **Danh sách lưu trữ hành động:** Sử dụng cấu trúc Bản đồ an toàn luồng (`ConcurrentHashMap`) làm bộ đệm chứa hành động của các đội trong lượt.
    *   **Khóa (Key):** `teamName` (Kiểu dữ liệu: `String`).
    *   **Giá trị (Value):** `ActionSubmission` (Đối tượng chứa hành động, dấu thời gian nhận của Server, dấu thời gian gửi của Client).
*   **Biến trạng thái khóa lượt:** Sử dụng biến cờ hiệu bất biến nguyên tử (`AtomicBoolean`) có tên `isTurnClosed` để kiểm soát trạng thái tiếp nhận yêu cầu.

---

## 2. Cơ chế Bù trừ độ trễ mạng (Network Latency Compensation)

Khi các đội chơi gửi yêu cầu hành động từ các vùng địa lý khác nhau, độ trễ đường truyền mạng (Network Latency) sẽ gây ra bất lợi cho các đội chơi có kết nối kém hơn. Để giải quyết bài toán này, hệ thống áp dụng thuật toán Bù trừ độ trễ mạng:

### Thuật toán Đồng bộ thời gian và Tính toán dấu thời gian công bằng (Fair Ordering)
1.  **Giai đoạn bắt đầu trận đấu (Time Synchronization Handshake):**
    *   Trước khi trận đấu khởi tranh, mỗi client của đội chơi gửi 5 request đồng bộ thời gian liên tiếp lên Server để đo thời gian khứ hồi (Round-Trip Time - RTT).
    *   Server ghi nhận độ lệch thời gian (Clock Offset) giữa Client và Server: `offset = clientTime - serverTime`.
    *   Server lưu trữ giá trị `offset` trung bình này ứng với từng đội chơi (`teamOffsetMap`).
2.  **Giai đoạn nhận request hành động:**
    *   Khi đội chơi gửi yêu cầu hành động, Client bắt buộc phải đính kèm dấu thời gian gửi đi của Client (`clientSentTimestamp`) trong Header của yêu cầu.
    *   Server tiếp nhận yêu cầu và ngay lập tức ghi nhận dấu thời gian nhận thực tế tại Server (`serverReceivedTimestamp`).
3.  **Giai đoạn chuẩn hóa dấu thời gian gửi (Compensated Timestamp Calculation):**
    *   Server lấy giá trị `offset` của đội tương ứng từ `teamOffsetMap`.
    *   Tính toán thời điểm gửi quy đổi theo giờ Server: `serverEquivalentSentTime = clientSentTimestamp - offset`.
    *   Tính toán độ trễ mạng thực tế của request: `latency = serverReceivedTimestamp - serverEquivalentSentTime`.
4.  **Kiểm tra tính hợp lệ (Anti-Cheat Validation):**
    *   Nếu `serverEquivalentSentTime` lớn hơn `serverReceivedTimestamp` (Client gửi tương lai) hoặc nhỏ hơn `serverReceivedTimestamp - apiTimeoutMs` (Client gửi quá cũ hoặc cố tình gian lận): Server hủy bỏ tính toán bù trừ và gán `serverEquivalentSentTime = serverReceivedTimestamp`.
    *   Nếu hợp lệ: Sử dụng `serverEquivalentSentTime` làm khóa sắp xếp thứ tự ưu tiên.

---

## 3. Quy trình Đóng Turn và chuyển đổi luồng xử lý (Turn Closed Loop Transition)

Quy trình đóng lượt đấu và chuyển tiếp dữ liệu sang luồng giả lập đơn (Single-threaded simulation) được thiết kế để đảm bảo tính an toàn dữ liệu tuyệt đối (Thread Safety):

### Thuật toán từng bước chuyển giao lượt đấu:
1.  **Bước 1 - Kích hoạt đóng lượt:** Khi đồng hồ đếm ngược của lượt chơi hiện tại đạt giới hạn cấu hình (`turnTimeLimitMs`), hoặc khi tất cả các đội chơi đăng ký đã gửi thành công hành động, luồng lập lịch (Scheduler Thread) sẽ kích hoạt quy trình đóng lượt.
2.  **Bước 2 - Khóa hàng đợi tiếp nhận:** Thiết lập biến cờ hiệu nguyên tử `isTurnClosed = true`. Kể từ thời điểm này, mọi yêu cầu gửi lên từ Client sẽ bị REST Adapter từ chối ngay lập tức ở tầng kiểm tra điều kiện với mã lỗi HTTP 400 (Lượt đấu đã đóng).
3.  **Bước 3 - Trích xuất và tráo đổi bộ đệm:**
    *   Tách biệt dữ liệu: Luồng lập lịch thực hiện tráo đổi bản đồ `ConcurrentHashMap` hiện tại bằng một bản đồ trống mới cho lượt đấu tiếp theo.
    *   Quy trình này giải phóng hoàn toàn luồng xử lý REST để chuẩn bị cho lượt mới mà không gây nghẽn.
4.  **Bước 4 - Sắp xếp hành động:** Gọi `RequestOrderingService` để sắp xếp danh sách các đối tượng `ActionSubmission` thu được từ lượt vừa đóng theo thứ tự tăng dần của `serverEquivalentSentTime` (đội gửi trước được xếp trước).
5.  **Bước 5 - Giả lập đơn luồng (Single-threaded Execution):**
    *   Chuyển giao danh sách hành động đã sắp xếp vào phương thức giả lập của Domain Aggregate `MatchState.simulateTurn()`.
    *   Toàn bộ quá trình giả lập di chuyển, tính toán xăng, ăn Udon, cộng điểm được thực thi tuần tự bởi một luồng duy nhất của Worker Thread. Điều này đảm bảo không bao giờ xảy ra xung đột ghi (Write Conflict) trên trạng thái của `MatchState`.
6.  **Bước 6 - Cập nhật và lưu trữ:**
    *   Luồng Worker cập nhật lưu lượng giao thông động dựa trên số lượt dừng chân của Agent.
    *   Thực hiện tính điểm, cập nhật xếp hạng các đội.
    *   Lưu trữ ảnh chụp trạng thái `MatchSnapshot` và các sự kiện lịch sử xuống DB.
7.  **Bước 7 - Mở lượt đấu mới:**
    *   Worker Thread gọi `nextDay()` để tăng số thứ tự lượt đấu lên `currentTurn + 1`.
    *   Cập nhật `turnStartTime = thời gian hệ thống hiện tại`.
    *   Thiết lập biến cờ hiệu nguyên tử `isTurnClosed = false` để mở lại hàng đợi tiếp nhận yêu cầu.

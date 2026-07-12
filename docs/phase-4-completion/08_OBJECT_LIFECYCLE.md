# VÒNG ĐỜI ĐỐI TƯỢNG VÀ QUẢN LÝ ĐỒNG THỜI (CONCURRENCY)

Tài liệu này đặc tả cơ chế quản lý đa luồng, vòng đời tiếp nhận và xử lý hành động đồng thời của các đội chơi, và thuật toán bù đắp sai số độ trễ mạng (Network Latency Compensation) tại máy chủ.

---

## 1. Kiến trúc Đa luồng tầng Application

Trong môi trường thi đấu thời gian thực, máy chủ tiếp nhận hàng trăm HTTP request đồng thời từ các đội chơi trong cùng một lượt (Turn). Hệ thống phân rã xử lý thành hai luồng độc lập để đảm bảo hiệu năng tối đa:

1.  **Nhóm luồng Tiếp nhận (Request Ingestion Threads)**: Các luồng do Spring HTTP Container (Tomcat Thread Pool) quản lý, chịu trách nhiệm tiếp nhận HTTP POST gửi action, thực hiện validation cú pháp, tính toán độ trễ và lưu trữ tạm thời vào hàng đợi.
2.  **Luồng thực thi game (Game Simulation Thread)**: Luồng đơn nhất (Single thread) chuyên biệt chịu trách nhiệm chạy mô phỏng lượt (`executeTurn`) sau khi hết thời gian chờ của Turn. Thiết kế này triệt tiêu hoàn toàn khả năng xảy ra lỗi tranh chấp bộ nhớ (Race Condition) trong quá trình tính toán vị trí vật lý và điểm số của Domain Model.

---

## 2. Quy trình Xếp hàng Công bằng và Bù đắp Trễ Mạng (Network Latency Compensation)

Để đảm bảo tính công bằng giữa đội chơi có đường truyền mạng siêu tốc (ví dụ: đặt gần server, độ trễ 2ms) và đội chơi có đường truyền chậm (độ trễ 150ms), hệ thống áp dụng cơ chế tính toán thời điểm gửi danh nghĩa (Virtual Submission Timestamp).

### Thuật toán Bù đắp trễ mạng từng bước (Text-based Algorithm):
1.  **Bước 1 - Gửi tín hiệu đầu Turn**:
    *   Vào thời điểm bắt đầu lượt chơi mới (Turn $T$), Server ghi nhận thời điểm vật lý của máy chủ: `ServerTurnStartTime` (mili-giây).
    *   Server phát sóng tín hiệu đầu Turn tới tất cả các bot thông qua kết nối mạng.
2.  **Bước 2 - Đo lường tại Bot (Client)**:
    *   Bot của đội chơi khi nhận được tín hiệu sẽ ghi lại thời điểm cục bộ: `ClientReceivedTime`.
    *   Bot tính toán chiến thuật và đưa ra quyết định hành động.
    *   Trước khi gửi HTTP request, Bot ghi nhận thời điểm cục bộ: `ClientSentTime`.
    *   Bot tính toán thời gian xử lý thực tế của thuật toán:
        $$\text{ClientComputationDuration} = \text{ClientSentTime} - \text{ClientReceivedTime}$$
    *   Bot đính kèm tham số `clientDurationMs = ClientComputationDuration` vào tiêu đề (Header) hoặc Body của HTTP POST Action Request gửi lên Server.
3.  **Bước 3 - Quy đổi tại Server**:
    *   Khi luồng tiếp nhận của Server nhận được HTTP Request từ đội $A$, nó sẽ bỏ qua thời gian trễ đường truyền của gói tin và tính toán **Thời gian nộp bài ảo (Virtual Submission Timestamp - VST)**:
        $$\text{VST}(A) = \text{ServerTurnStartTime} + \text{clientDurationMs}(A)$$
    *   Chỉ số VST này phản ánh chính xác thời điểm mà thuật toán của đội $A$ hoàn thành và gửi đi tại máy của họ, hoàn toàn không chứa độ trễ truyền dẫn cáp mạng.
4.  **Bước 4 - Xếp hàng ưu tiên**:
    *   Server lưu trữ yêu cầu kèm chỉ số VST tương ứng vào `TurnExecutionQueue`.
    *   Khi đóng Turn, các yêu cầu hành động của các đội được sắp xếp theo thứ tự VST tăng dần (đội nào tính toán xong và gửi đi trước về mặt danh nghĩa sẽ được ưu tiên thực thi hành động trước ở từng bước đi).

---

## 3. Thiết kế Hàng đợi TurnExecutionQueue và Đảm bảo An toàn Luồng (Thread Safety)

### A. Cấu trúc lưu trữ an toàn luồng
*   `TurnExecutionQueue` sử dụng cấu trúc `ConcurrentHashMap` làm bộ nhớ đệm tiếp nhận tạm thời:
    *   Khóa (Key): `teamId` (String - định danh duy nhất của mỗi đội).
    *   Giá trị (Value): `TeamActions` (chứa danh sách hành động của các Agent thuộc đội và chỉ số VST).
*   **Lý do lựa chọn**: Khi một đội chơi gửi nhiều request đè nhau (do mạng chập chờn hoặc bot gửi lại), thao tác `put()` trên `ConcurrentHashMap` bảo đảm chỉ có một yêu cầu cuối cùng được giữ lại một cách an toàn luồng, không gây lỗi hỏng cấu trúc dữ liệu.

### B. Vòng đời Xử lý Lượt chơi (Turn Lifecycle States)

Vòng đời của một Turn diễn ra qua 4 trạng thái nghiêm ngặt:

1.  **Trạng thái TIẾP NHẬN (INGESTING)**:
    *   Bắt đầu khi Turn mới mở ra.
    *   Cho phép các luồng HTTP nạp dữ liệu hành động vào `TurnExecutionQueue`.
    *   Bất kỳ yêu cầu nào gửi đến có chỉ số Turn khớp với Turn hiện tại đều được ghi nhận VST và cập nhật vào map.
2.  **Trạng thái ĐÓNG TURN (LOCKING)**:
    *   Kích hoạt khi hết thời gian quy định (`concurrency.turn.timeout.ms`) hoặc khi tất cả các đội đã nộp bài đầy đủ.
    *   Luồng thực thi chính sẽ chuyển trạng thái của Queue sang `LOCKED`.
    *   Mọi HTTP request gửi hành động gửi đến sau thời điểm này đều bị từ chối ngay lập tức với mã lỗi `409 Conflict` (Lượt chơi đã đóng).
3.  **Trạng thái THỰC THI (SIMULATING)**:
    *   Luồng thực thi chính rút toàn bộ dữ liệu từ `TurnExecutionQueue` ra một danh sách cục bộ.
    *   Thực hiện sắp xếp danh sách này theo chỉ số VST tăng dần.
    *   Gọi hàm mô phỏng của Domain: `MatchState.simulateTurn()`.
    *   Trong suốt quá trình này, `TurnExecutionQueue` trống rỗng và ở trạng thái khóa.
4.  **Trạng thái CHUYỂN TIẾP (TRANSITIONING)**:
    *   Thực hiện tính toán giao thông mới, cập nhật điểm số, tạo Snapshot phục hồi.
    *   Gọi hàm `nextDay()` để chuyển đổi trạng thái trận đấu sang lượt tiếp theo.
    *   Chuyển trạng thái Queue quay lại `INGESTING`, giải phóng khóa và bắt đầu nhận hành động cho Turn mới.
    *   Gửi thông báo đẩy WebSocket đầu Turn mới tới các Bot.

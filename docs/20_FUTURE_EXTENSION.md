# Tài liệu Thiết kế Exception Handling - 20_FUTURE_EXTENSION

## 1. Purpose (Mục đích)
Tài liệu này định hướng lộ trình nâng cấp và mở rộng (Future Extensions) phân hệ Exception Handling & Error Management khi dự án **HEXUDON Server** phát triển lên quy mô lớn hơn (ví dụ: chuyển dịch sang Real-time WebSockets, kiến trúc Microservices, hoặc tích hợp hệ thống giám sát tập trung APM). Điều này đảm bảo kiến trúc hiện tại luôn có tính kế thừa và sẵn sàng mở rộng.

---

## 2. Scope (Phạm vi)
Áp dụng đối với các thiết kế kiến trúc nâng cao trong tương lai của dự án.

---

## 3. Future Extension Areas (Các hướng mở rộng tương lai)

### 3.1. Xử lý lỗi trên kết nối thời gian thực (WebSockets Error Handling)
Hiện tại, dự án sử dụng giao thức REST API (HTTP Request/Response). Trong tương lai, để tối ưu hóa tốc độ gửi lệnh và đồng bộ bản đồ, game có thể chuyển sang giao thức **WebSockets** (sử dụng STOMP hoặc Socket.io).

*   **Vấn đề**: `@RestControllerAdvice` và `@ExceptionHandler` thông thường của Spring MVC sẽ không bắt được các Exception ném ra từ các Thread xử lý WebSocket message.
*   **Giải pháp thiết kế**:
    1.  Sử dụng annotation `@MessageExceptionHandler` của Spring Messaging để bắt các lỗi phát sinh trong các WebSocket Controller (`@MessageMapping`).
    2.  Đóng gói lỗi thành WebSocket Error Frame dưới dạng JSON và gửi phản hồi đích danh tới WebSocket Session ID của Team vừa gửi lệnh sai.
    3.  Tận dụng lại enum `ErrorCode` có sẵn để giữ tính nhất quán về mã lỗi.

---

### 3.2. Tích hợp Trace ID & Giám sát phân tán (Distributed Tracing & APM)
Khi hệ thống chạy trên môi trường Production với lượng người chơi lớn, việc tra cứu một lỗi cụ thể từ hàng triệu dòng log file là rất khó khăn.

*   **Giải pháp thiết kế**:
    1.  Tích hợp **Spring Cloud Sleuth** (hoặc OpenTelemetry) để tự động sinh ra một mã định danh duy nhất gọi là `Trace ID` (hoặc `Correlation ID`) cho mỗi request đi vào hệ thống.
    2.  Bổ sung thuộc tính `traceId` vào lớp `ErrorResponse`:
        ```json
        {
          "errorCode": "INTERNAL_SERVER_ERROR",
          "message": "An unexpected error occurred.",
          "timestamp": 1720516800000,
          "traceId": "a5d8f92b7c3e4f1a"
        }
        ```
    3.  Trace ID này sẽ được ghi kèm vào mọi dòng log liên quan đến request đó. Client khi gặp lỗi có thể gửi mã `traceId` này cho đội ngũ kỹ thuật để tìm kiếm chính xác toàn bộ chu kỳ xử lý lỗi trong tích tắc.
    4.  Cấu hình `GlobalExceptionHandler` để tự động đẩy các Exception mức độ `ERROR` lên các nền tảng giám sát lỗi tập trung như **Sentry** hoặc **Elastic APM** thông qua SDK của chúng.

---

### 3.3. Quản lý thông điệp lỗi động (Dynamic Error Config)
Hiện nay, thông điệp mô tả mặc định của lỗi đang được khai báo tĩnh (hard-coded) trong enum `ErrorCode.java`.

*   **Giải pháp thiết kế**:
    1.  Tách biệt hoàn toàn phần text mô tả lỗi nghiệp vụ ra khỏi code Java. Lưu trữ chúng trong một bảng cơ sở dữ liệu (ví dụ bảng `error_messages`) hoặc lưu trong Spring Cloud Config Server.
    2.  Khi phát sinh ngoại lệ, `GlobalExceptionHandler` sẽ truy vấn nhanh cơ sở dữ liệu để lấy câu thông báo lỗi tương ứng với `ErrorCode` và ngôn ngữ của client.
    3.  Điều này giúp quản trị viên có thể tinh chỉnh thông điệp mô tả lỗi chi tiết cho game thủ mà không cần build và deploy lại toàn bộ Server.

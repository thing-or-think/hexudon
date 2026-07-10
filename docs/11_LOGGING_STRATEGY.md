# Tài liệu Thiết kế Exception Handling - 11_LOGGING_STRATEGY

## 1. Purpose (Mục đích)
Tài liệu này định nghĩa chiến lược ghi nhật ký (Logging Strategy) khi xảy ra lỗi trong hệ thống **HEXUDON Server**. Mục tiêu là thiết lập một cơ chế ghi log khoa học, có cấu trúc, giúp đội ngũ vận hành hệ thống (SRE, DevOps) và lập trình viên dễ dàng khoanh vùng, debug lỗi mà không làm giảm hiệu năng hệ thống hay làm rác file nhật ký.

---

## 2. Scope (Phạm vi)
Áp dụng đối với toàn bộ hoạt động ghi log lỗi thông qua thư viện SLF4J (với Logback backend) trên ứng dụng **HEXUDON Server**.

---

## 3. Log Levels Specification (Phân định cấp độ Log)

Hệ thống tuân thủ nghiêm ngặt việc phân chia cấp độ log khi xảy ra exception:

### 3.1. Cấp độ `ERROR`
*   **Khi nào dùng**: Chỉ dùng cho các lỗi hệ thống nghiêm trọng khiến một tính năng hoặc toàn bộ server không thể hoạt động bình thường, yêu cầu lập trình viên/quản trị viên phải can thiệp ngay lập tức.
*   **Các Exception tương ứng**: `ConfigLoadException` (khiến server không thể khởi động), các lỗi thuộc `Exception.class` (NullPointerException, OutOfMemoryError, SQLException, lỗi ổ đĩa đầy).
*   **Nguyên tắc ghi Stacktrace**: **Bắt buộc phải in đầy đủ stacktrace** ra file log để xác định chính xác dòng code bị lỗi.
*   **Ví dụ cú pháp**:
    ```java
    logger.error("CRITICAL_SYSTEM_ERROR - Failed to process match simulation. Day: {}", currentDay, ex);
    ```

### 3.2. Cấp độ `WARN`
*   **Khi nào dùng**: Dùng cho các lỗi nghiệp vụ thông thường do Client gửi yêu cầu sai, hoặc các cảnh báo bảo mật, giới hạn tần suất. Lỗi này phản ánh Client đang gọi sai chứ không phản ánh Server bị lỗi code.
*   **Các Exception tương ứng**: `GameRuleViolationException`, `MatchStateConflictException`, `ResourceNotFoundException`, `RateLimitExceededException`, `HttpMessageNotReadableException`.
*   **Nguyên tắc ghi Stacktrace**: **Tuyệt đối không in stacktrace**. Việc in stacktrace cho các lỗi nghiệp vụ bình thường sẽ làm ngập log file vô ích và gây suy giảm nghiêm trọng hiệu năng của máy chủ game (do chi phí sinh stacktrace của JVM là rất lớn). Chỉ ghi log một dòng ngắn gọn.
*   **Ví dụ cú pháp**:
    ```java
    logger.warn("BUSINESS_RULE_VIOLATION - Team '{}' failed to move agent '{}': {}", teamName, agentId, ex.getMessage());
    ```

### 3.3. Cấp độ `INFO`
*   **Khi nào dùng**: Ghi nhận các mốc sự kiện bình thường phục vụ theo dõi vòng đời trận đấu.
*   **Ví dụ**: Khi bắt đầu trận đấu, khi hoàn thành chu kỳ mô phỏng ngày, khi một đội đăng ký thành công.
*   **Ví dụ cú pháp**:
    ```java
    logger.info("MATCH_LIFECYCLE - Match started successfully with {} teams.", registeredTeamsCount);
    ```

### 3.4. Cấp độ `DEBUG`
*   **Khi nào dùng**: Ghi nhật ký chi tiết quá trình tính toán logic game (ví dụ: từng bước đi của agent, lượng xăng tiêu thụ chi tiết, cách tính điểm giao thông). Cấp độ này chỉ được kích hoạt trong môi trường Development hoặc Staging.

---

## 4. Log Message Format & Context (Định dạng & Ngữ cảnh ghi log)
Mỗi dòng log lỗi ghi ra phải đảm bảo cung cấp đầy đủ ngữ cảnh để có thể tra cứu nhanh chóng. Định dạng chuẩn của một dòng log lỗi nghiệp vụ:

```text
[Loại log] - [Context] - [Chi tiết thông điệp]
```

### Các thông tin ngữ cảnh cần đưa vào log:
*   `Team Name`: Đội chơi thực hiện request gây lỗi (lấy từ Header `X-Team-Name`).
*   `Client IP`: Địa chỉ IP của Client gửi yêu cầu (đặc biệt quan trọng với lỗi Rate Limit).
*   `Match State`: Trạng thái trận đấu hiện tại và ngày hiện tại (`currentTurn`).
*   `Request URI`: Đường dẫn API đang được gọi (ví dụ: `/api/match/actions`).

---

## 5. Sensitive Data Masking (Che giấu dữ liệu nhạy cảm)
Để tuân thủ các quy định về an toàn bảo mật, hệ thống logging áp dụng nguyên tắc: **Không bao giờ log trực tiếp dữ liệu nhạy cảm**.
*   **Mật khẩu và Tokens**: Nếu trong tương lai có tích hợp bảo mật, mọi API key, token bí mật hoặc mật khẩu kết nối database in ra log phải được mã hóa hoặc ghi đè bằng ký tự mặt nạ (ví dụ: `[MASKED]`).
*   **Đường dẫn vật lý của hệ thống**: Không in đường dẫn tuyệt đối của thư mục lưu trữ server trên đĩa cứng lên các log được trả ra bên ngoài.

---

## 6. Request/Response Logging for Debugging (Ghi log Request/Response)
Trong môi trường phát triển (Development), Interceptor hoặc Filter sẽ được cấu hình để log toàn bộ nội dung JSON Request và Response khi xảy ra lỗi ở mức độ `WARN` hoặc `ERROR`.
*   Điều này giúp tái hiện chính xác kịch bản lỗi bằng cách copy lại Payload JSON mà Client đã gửi lên.
*   Trong môi trường Production, tính năng log chi tiết payload này sẽ bị tắt đi để tối ưu hóa hiệu năng I/O và dung lượng đĩa cứng.

---

## 7. Common Mistakes (Sai lầm thường gặp)
*   **Nuốt Stacktrace khi có lỗi nghiêm trọng**: Ghi log `logger.error("Database connection failed: " + ex.getMessage())` mà thiếu tham số `ex` cuối cùng. Lỗi này làm mất hoàn toàn stacktrace và không thể xác định vị trí lỗi trong code.
*   **In stacktrace bừa bãi**: In stacktrace của `GameRuleViolationException` mỗi khi bot của người chơi di chuyển sai. Khi có hàng trăm bot chạy đồng thời, file log sẽ phình lên hàng Gigabyte chỉ sau vài giờ và làm sập ổ đĩa cứng của server.

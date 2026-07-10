# Tài liệu Thiết kế Exception Handling - 17_FAQ

## 1. Purpose (Mục đích)
Tài liệu này tổng hợp các câu hỏi thường gặp (Frequently Asked Questions) và giải đáp các thắc mắc chuyên sâu liên quan đến cơ chế xử lý ngoại lệ trong **HEXUDON Server**. Đây là tài liệu tham khảo nâng cao cho các lập trình viên khi đối mặt với các tình huống lỗi phức tạp hoặc các trường hợp đặc biệt (Edge Cases).

---

## 2. Scope (Phạm vi)
Giải đáp các vấn đề về luồng chạy đa luồng (multi-threading), đa ngôn ngữ (i18n), thứ tự bắt lỗi của Spring MVC và tích hợp framework.

---

## 3. Frequently Asked Questions (Hỏi & Đáp)

### Q1: Nếu lỗi xảy ra trong một luồng chạy ngầm (ví dụ: Scheduled Task của SchedulerConfig để tự động chuyển turn), lớp `GlobalExceptionHandler` có bắt được không?
*   **Trả lời**: **KHÔNG**.
*   **Giải thích**: Lớp `GlobalExceptionHandler` (chú thích bằng `@RestControllerAdvice`) chỉ bắt được các Exception ném ra từ các Thread được quản lý bởi Spring MVC `DispatcherServlet` (luồng xử lý REST request). Đối với các luồng chạy ngầm của Scheduler, khi Exception ném ra và không được bắt cục bộ, luồng đó sẽ bị chết hoặc bị luồng Scheduler nuốt mất lỗi.
*   **Cách xử lý**: 
    1.  Bắt buộc phải viết block `try-catch` cục bộ ôm lấy logic chạy ngầm trong Scheduler.
    2.  Trong block `catch`, ghi log ở mức `ERROR` kèm full stacktrace hoặc gửi thông báo cảnh báo qua webhook (như Slack/Telegram) để SRE biết và xử lý.
    3.  Không được để exception tự do bay ra ngoài Thread nghiệp vụ ngầm.

---

### Q2: Làm cách nào để Spring MVC xác định phương thức `@ExceptionHandler` nào sẽ xử lý khi một Exception kế thừa phát sinh?
*   **Trả lời**: Spring MVC sử dụng thuật toán tìm kiếm lớp khớp nhất (Most Specific Exception Matching).
*   **Giải thích**: Ví dụ, nếu bạn ném `GameRuleViolationException` (kế thừa từ `BusinessException`), Spring MVC sẽ tìm xem có phương thức `@ExceptionHandler` nào bắt đích danh `GameRuleViolationException` hay không. 
    *   Nếu có -> Sử dụng nó.
    *   Nếu không -> Spring sẽ quét lên lớp cha của nó là `BusinessException` xem có phương thức bắt hay không. Do ta đã viết `@ExceptionHandler(BusinessException.class)`, phương thức này sẽ được lựa chọn.
    *   Nếu không có nữa -> Spring tiếp tục tìm lên `RuntimeException` và cuối cùng là `Exception.class`.

---

### Q3: Làm thế nào để thay đổi ngôn ngữ (Đa ngôn ngữ - i18n) cho thông điệp lỗi nghiệp vụ?
*   **Trả lời**: Sử dụng Spring `MessageSource`.
*   **Cách thức thực hiện trong tương lai**:
    1.  Khai báo các file `messages.properties` (tiếng Anh) và `messages_vi.properties` (tiếng Việt) trong thư mục `src/main/resources/`.
    2.  Trong các file này, định nghĩa bản dịch cho các mã lỗi, ví dụ: `TEAM_ALREADY_EXISTS=Tên đội đã tồn tại trong hệ thống.`
    3.  Tại `GlobalExceptionHandler`, tiêm (inject) bean `MessageSource`.
    4.  Khi bắt lỗi, lấy Header `Accept-Language` từ Request để xác định Locale và gọi `messageSource.getMessage(ex.getErrorCode().getCode(), null, locale)` để lấy message tương ứng rồi đưa vào `ErrorResponse`.

---

### Q4: Sự khác nhau giữa `MethodArgumentNotValidException` và `ConstraintViolationException` là gì?
*   **Trả lời**:
    *   `MethodArgumentNotValidException`: Ném ra khi validation thất bại trên tham số đầu vào được chú thích bằng `@Valid` và `@RequestBody` (xử lý dữ liệu JSON).
    *   `ConstraintViolationException`: Ném ra khi validation thất bại trên các tham số đơn lẻ nằm ở URL (như `@PathVariable` hoặc `@RequestParam`) trên các Controller được chú thích bằng `@Validated`.
*   **Giải pháp**: `GlobalExceptionHandler` cần định nghĩa hai phương thức riêng biệt để xử lý hai exception này vì cấu trúc dữ liệu bóc tách lỗi của chúng là hoàn toàn khác nhau.

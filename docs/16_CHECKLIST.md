# Tài liệu Thiết kế Exception Handling - 16_CHECKLIST

## 1. Purpose (Mục đích)
Tài liệu này cung cấp bảng danh mục kiểm tra chất lượng (Technical Checklist) dành cho các kỹ sư phát triển trước khi gửi Pull Request (PR) và các Architect/Senior Developers khi thực hiện duyệt code (Code Review). Mục tiêu là đảm bảo chất lượng triển khai của module Exception tuân thủ 100% các tiêu chuẩn thiết kế đã đặt ra.

---

## 2. Scope (Phạm vi)
Áp dụng đối với toàn bộ mã nguồn liên quan đến xử lý lỗi, validate dữ liệu đầu vào và logging của dự án **HEXUDON Server**.

---

## 3. Quality Checklist (Bảng kiểm tra chi tiết)

### 3.1. Kiến trúc phân lớp (Architecture Compliance)
*   [ ] Toàn bộ các Exception tự định nghĩa đều kế thừa trực tiếp từ `BusinessException` hoặc `SystemException`.
*   [ ] Tuyệt đối không import các class của Spring Framework (như `HttpStatus`, `ResponseEntity`, `@RestControllerAdvice`) vào các gói `base`, `business`, `system`.
*   [ ] Chỉ có duy nhất một class `@RestControllerAdvice` trong hệ thống để quản lý lỗi tập trung.
*   [ ] Không bắt Exception ở tầng logic game (Engine) trừ khi có thể tự phục hồi lỗi. Mọi lỗi vi phạm quy tắc chơi đều được ném ra ngoài (bubbled up).

### 3.2. Thiết kế Mã lỗi (ErrorCode Design)
*   [ ] Mọi mã lỗi trong Enum `ErrorCode` đều sử dụng chữ hoa, định dạng `SNAKE_CASE` (ví dụ: `RATE_LIMIT_EXCEEDED`).
*   [ ] Giá trị chuỗi định danh lỗi (`code`) là duy nhất trong toàn hệ thống, không có sự trùng lặp.
*   [ ] Mỗi mã lỗi đều có một thông điệp mặc định ngắn gọn, rõ ràng bằng tiếng Anh.

### 3.3. Định dạng JSON Phản hồi (Error Response Format)
*   [ ] JSON trả về luôn có tối thiểu 3 trường: `errorCode`, `message`, `timestamp`.
*   [ ] Định dạng `timestamp` trả về là số nguyên Epoch Milliseconds (kiểu `long`).
*   [ ] Thêm cấu hình `@JsonInclude(JsonInclude.Include.NON_NULL)` trên class `ErrorResponse` để ẩn các trường null khi serialize.
*   [ ] Trường `errors` chỉ xuất hiện và chứa thông tin chi tiết khi xảy ra lỗi validation (`VALIDATION_ERROR`).

### 3.4. Kiểm định dữ liệu (Validation Integration)
*   [ ] Annotation `@Valid` được cấu hình đầy đủ trước các tham số request body tại Controller.
*   [ ] Đã cấu hình `@Valid` đệ quy tại các collection lồng nhau trong DTO (như `@Valid List<AgentPlanDto> agentPlans`).
*   [ ] Thông điệp ràng buộc (`message`) tại các thuộc tính DTO được viết cụ thể, thân thiện (ví dụ: `"Team name must not be blank."` thay vì để trống).

### 3.5. Nhật ký Lỗi & Hiệu năng (Logging Strategy)
*   [ ] Đã cấu hình log level `WARN` cho lỗi nghiệp vụ (Business) và **không ghi kèm stacktrace**.
*   [ ] Đã cấu hình log level `ERROR` cho lỗi hệ thống (System) và **bắt buộc ghi kèm stacktrace**.
*   [ ] Tuyệt đối không sử dụng `e.printStackTrace()` hoặc `System.out.println()` để ghi lỗi.
*   [ ] Tuân thủ nguyên tắc "Không vừa log vừa throw". Không ghi log lỗi tại Engine/Manager khi ném Exception.
*   [ ] Ghi log lỗi có đầy đủ ngữ cảnh (`teamName`, `IP`, `currentTurn`).

### 3.6. Bảo mật (Security Guidelines)
*   [ ] Không bao giờ expose thông báo lỗi thô của SqlException hoặc NullPointerException ra Client.
*   [ ] Khi xảy ra lỗi hệ thống (500), thông điệp trả về luôn là câu thông báo chung cố định để che giấu lỗ hổng.

### 3.7. Kiểm thử (Testing)
*   [ ] Có Unit Test kiểm định cấu trúc JSON và dữ liệu của `ErrorResponse`.
*   [ ] Có Integration Test gọi API thực tế để kiểm tra mã trạng thái HTTP (400, 404, 429, 500) và định dạng lỗi tương ứng.

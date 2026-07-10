# Tài liệu Thiết kế Exception Handling - 18_MIGRATION_GUIDE

## 1. Purpose (Mục đích)
Tài liệu này hướng dẫn chi tiết cách thức chuyển đổi (Migration Guide) hệ thống xử lý lỗi thô sơ, tự phát hiện thời điểm hiện tại của **HEXUDON Server** sang hệ thống Exception Handling tập trung theo chuẩn Clean Architecture mới. Tài liệu giúp lập trình viên refactor code cũ một cách an toàn mà không làm hỏng logic nghiệp vụ game hiện có.

---

## 2. Scope (Phạm vi)
Áp dụng cho toàn bộ các file Java hiện có trong `src/main/java/com/naprock/hexudon/controller/`, `manager/`, `engine/`, `loader/` và `exception/`.

---

## 3. Current State Analysis (Phân tích mã nguồn hiện tại)
Hiện tại, trong dự án đã có sẵn một số class exception thô sơ:
*   `GameException` và `GameRuleViolationException` đã được viết nhưng chưa kế thừa đúng phân cấp base mới và chưa tích hợp đầy đủ mã lỗi enum `ErrorCode`.
*   `GlobalExceptionHandler` cũ đã được định nghĩa nhưng cấu trúc `ErrorResponse` trả về còn đơn giản, chưa hỗ trợ mảng chi tiết lỗi Validation (`ValidationErrorDetail`) và chưa phân tách rõ log level.
*   Một số class Controller hoặc Loader vẫn ném các exception thô của Java.

---

## 4. Migration Plan (Các bước refactoring cụ thể)

### Bước 1: Thay thế và cập nhật cấu trúc `exception` package
1.  Di chuyển các exception class hiện có vào đúng thư mục con đã định dạng ở tài liệu **02_PACKAGE_STRUCTURE.md**.
2.  Refactor class `GameException` thành `BusinessException` làm base exception nghiệp vụ.
3.  Cập nhật class `GameRuleViolationException` kế thừa từ `BusinessException` và sửa đổi constructor để nhận tham số là `ErrorCode`.

### Bước 2: Dọn dẹp mã nguồn trong các Controller
1.  Kiểm tra tất cả các phương thức trong `MatchController`.
2.  Nếu phát hiện các block `try-catch` thủ công trả về `ResponseEntity.status(400).body(...)` -> **Xóa hoàn toàn block try-catch này**.
3.  Để exception tự do bay ra khỏi Controller, để Spring MVC chuyển cho `GlobalExceptionHandler` xử lý.
4.  Thêm annotation `@Valid` vào trước `@RequestBody RegisterTeamRequest` và `@RequestBody SubmitActionRequest` trong các phương thức của Controller.

### Bước 3: Sửa đổi logic ném ngoại lệ trong Engine & Loader
1.  Tại `MatchConfigLoader`: Thay thế việc ném `RuntimeException` chung bằng việc ném `ConfigLoadException` khi gặp sự cố đọc file cấu hình.
2.  Tại các class Engine (ví dụ `MovementSimulator`, `FuelManager`): Cập nhật logic kiểm tra luật chơi, chuyển từ ném lỗi thô sang ném `GameRuleViolationException` kèm mã lỗi tương ứng từ enum `ErrorCode`.
    *   *Ví dụ cũ*: `throw new GameException("No fuel")`
    *   *Ví dụ mới*: `throw new GameRuleViolationException(ErrorCode.AGENT_OUT_OF_FUEL, "Agent lacks fuel steps.")`

### Bước 4: Refactor `GlobalExceptionHandler` và `ErrorResponse`
1.  Thay thế toàn bộ code của `ErrorResponse.java` bằng đặc tả mới tại tài liệu **04** và **07**, tích hợp thêm record `ValidationErrorDetail`.
2.  Thay thế toàn bộ code của `GlobalExceptionHandler.java` bằng đặc tả mới tại tài liệu **08**, bổ sung logic bắt lỗi validation và logging theo chuẩn.

### Bước 5: Cập nhật và chạy lại Integration Tests
1.  Chạy lại các test cases cũ của Server.
2.  Sửa đổi các assert kiểm tra JSON trả về để khớp với format mới (kiểm tra sự tồn tại của trường `errorCode`, `timestamp`, `message` và mảng `errors`).

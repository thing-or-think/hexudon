# Tài liệu Thiết kế Exception Handling - 05_EXCEPTION_HIERARCHY

## 1. Purpose (Mục đích)
Tài liệu này trình bày sơ đồ phân cấp (Exception Hierarchy) và cấu trúc kế thừa của các lớp ngoại lệ trong **HEXUDON Server**. Đồng thời, tài liệu cung cấp cơ sở lập luận khoa học và các quyết định thiết kế cốt lõi đứng sau cấu trúc phân cấp này.

---

## 2. Scope (Phạm vi)
Phân tích và phân loại toàn bộ hệ thống exception: bao gồm các exception tùy biến của dự án, các exception xây dựng sẵn của Spring Framework và Java Standard Library.

---

## 3. Class Hierarchy Diagram (Sơ đồ cây kế thừa)

Dưới đây là cấu trúc hình cây biểu diễn quan hệ kế thừa của các ngoại lệ trong hệ thống:

```text
java.lang.Throwable
 └── java.lang.Exception
      └── java.lang.RuntimeException (Unchecked Exception)
           ├── com.naprock.hexudon.exception.base.BusinessException
           │    ├── com.naprock.hexudon.exception.business.GameRuleViolationException
           │    ├── com.naprock.hexudon.exception.business.MatchStateConflictException
           │    ├── com.naprock.hexudon.exception.business.RateLimitExceededException
           │    └── com.naprock.hexudon.exception.business.ResourceNotFoundException
           │
           ├── com.naprock.hexudon.exception.base.SystemException
           │    └── com.naprock.hexudon.exception.system.ConfigLoadException
           │
           └── [Spring Framework Exceptions]
                ├── org.springframework.web.bind.MethodArgumentNotValidException
                ├── jakarta.validation.ConstraintViolationException
                └── org.springframework.web.bind.BindException
```

---

## 4. Design Decisions & Rationale (Các quyết định thiết kế)

### 4.1. Tại sao lựa chọn Unchecked Exception (RuntimeException)?
Toàn bộ các exception tự định nghĩa của **HEXUDON Server** đều kế thừa từ `RuntimeException` thay vì `Exception` (Checked Exception). 

*   **Không gây ô nhiễm chữ ký phương thức (No Method Signature Pollution)**: Nếu sử dụng Checked Exceptions, mọi phương thức từ Engine -> Manager -> Controller đều phải khai báo `throws ...`. Khi có sự thay đổi hoặc bổ sung exception mới, ta buộc phải sửa đổi hàng loạt chữ ký phương thức trên toàn hệ thống, phá vỡ tính đóng gói và nguyên tắc Open/Closed Principle (OCP).
*   **Không thể phục hồi tại chỗ (Non-recoverable Errors)**: Trong bối cảnh Game Server REST API, hầu như tất cả lỗi phát sinh (di chuyển sai luật, cấu hình lỗi, rate limit) đều không thể tự phục hồi bởi code Java chạy ngầm tại thời điểm đó. Cách xử lý duy nhất và đúng đắn nhất là ngắt luồng xử lý hiện tại ngay lập tức (Fail-Fast) và trả thông tin lỗi về cho Client sửa đổi hành vi. Unchecked Exception là công cụ hoàn hảo cho mô hình xử lý này.
*   **Tận dụng tối đa Spring `@Transactional`**: Mặc định, Spring Framework chỉ thực hiện Rollback Transaction khi phát hiện `RuntimeException` (Unchecked Exception) ném ra từ các Service/Manager. Nếu dùng Checked Exception, nhà phát triển phải khai báo tường minh `@Transactional(rollbackFor = Exception.class)`, rất dễ bị bỏ quên dẫn đến lỗi bất đồng bộ dữ liệu.

---

### 4.2. Tách biệt Business Exception và System Exception
*   **BusinessException (Nhánh nghiệp vụ)**: Đại diện cho các lỗi mà nguyên nhân xuất phát từ phía Client (gửi sai dữ liệu, thực hiện sai luật game). Những lỗi này không phản ánh máy chủ chạy sai, do đó hệ thống không cần ghi log stacktrace đầy đủ và trả về mã HTTP Status dạng `4xx`.
*   **SystemException (Nhánh hệ thống)**: Đại diện cho các lỗi kỹ thuật, lỗi hạ tầng mà Client không thể tự sửa đổi (lỗi đọc file cấu hình, lỗi mất kết nối DB, lỗi mã nguồn). Những lỗi này bắt buộc phải ghi log chi tiết kèm stacktrace để Quản trị viên hệ thống (SRE/Ops) điều tra lỗi, đồng thời che giấu chi tiết và trả về mã `5xx` cho Client để đảm bảo an toàn.

---

## 5. Comparison: Checked vs Unchecked Exception

| Tiêu chí | Checked Exception (`Exception`) | Unchecked Exception (`RuntimeException`) |
| :--- | :--- | :--- |
| **Bắt buộc xử lý ở Compile-time** | Có (bắt buộc phải `try-catch` hoặc khai báo `throws`) | Không (phát hiện ở Runtime) |
| **Tác động tới Codebase** | Làm code cồng kềnh, giảm khả năng bảo trì. | Code ngắn gọn, tuân thủ nguyên tắc Clean Code. |
| **Spring Transaction Rollback** | Phải cấu hình bổ sung. | Tự động rollback transaction. |
| **Phù hợp với REST API** | Không phù hợp. | Rất phù hợp vì Global Exception Handler sẽ tự gom bắt. |

---

## 6. How to Extend the Hierarchy (Hướng dẫn mở rộng cấu trúc lớp lỗi)
Khi lập trình viên cần tạo ra một lỗi mới:

1.  **Bước 1: Xác định bản chất lỗi**:
    *   Nếu lỗi do client gửi dữ liệu sai quy tắc game -> Kế thừa `BusinessException`.
    *   Nếu lỗi do lỗi phần cứng, kết nối mạng, đọc file -> Kế thừa `SystemException`.
2.  **Bước 2: Tạo lớp cụ thể**:
    *   Tạo lớp mới đặt tên có hậu tố `Exception`.
    *   Truyền mã lỗi `ErrorCode` thích hợp vào constructor của lớp cha (`super(errorCode, ...)`).
3.  **Bước 3: Định nghĩa Handler**:
    *   Bổ sung phương thức bắt exception mới này trong `GlobalExceptionHandler` nếu nó cần cách định dạng đặc biệt (nếu không, nó sẽ tự động rơi vào nhánh xử lý của `BusinessException` hoặc `SystemException` chung).

---

## 7. Common Mistakes (Sai lầm thường gặp)
*   **Kế thừa trực tiếp từ `Throwable` hoặc `Error`**: Lỗi này rất nghiêm trọng vì `Error` đại diện cho các lỗi hệ thống không thể cứu vãn của JVM (như OutOfMemoryError).
*   **Tạo quá nhiều lớp ngoại lệ cụ thể không cần thiết**: Ví dụ tạo `InvalidPosXException` và `InvalidPosYException` thay vì dùng chung `GameRuleViolationException("INVALID_COORDINATES", "...")`.

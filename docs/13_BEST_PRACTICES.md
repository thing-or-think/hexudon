# Tài liệu Thiết kế Exception Handling - 13_BEST_PRACTICES

## 1. Purpose (Mục đích)
Tài liệu này tổng hợp các quy chuẩn thiết kế tối ưu, quy tắc viết code (Best Practices) và các chỉ dẫn kỹ thuật trong phân hệ Exception Handling & Error Management của **HEXUDON Server**. Mục tiêu là giúp đội ngũ phát triển duy trì chất lượng mã nguồn đồng đều, dễ đọc, dễ kiểm thử và tối ưu hiệu năng vận hành.

---

## 2. Scope (Phạm vi)
Áp dụng đối với toàn bộ kỹ sư phần mềm tham gia phát triển, bảo trì và đánh giá mã nguồn (Code Review) của dự án.

---

## 3. Exception Design Best Practices (Thiết kế Ngoại lệ)

### 3.1. Tránh ô nhiễm Exception Class (Exception Class Proliferation)
*   **Quy tắc**: Không tạo ra các Exception Class quá chi tiết nếu chúng có chung cách xử lý kỹ thuật và HTTP Status. Thay vào đó, hãy tái sử dụng các Class Exception có sẵn và phân biệt bằng mã `ErrorCode` và thông điệp lỗi (message).
*   *Nên làm*: Sử dụng `ResourceNotFoundException` và truyền tham số `"Team not found"` hoặc `"Agent not found"`.
*   *Không nên làm*: Tạo riêng `TeamNotFoundException` và `AgentNotFoundException` kế thừa độc lập, làm tăng số lượng class vô ích.

### 3.2. Không ném các Ngoại lệ chung (Generic Exceptions)
*   Tuyệt đối không viết `throw new RuntimeException("message")` hoặc `throw new Exception("message")` trong code nghiệp vụ.
*   Việc ném ngoại lệ chung khiến bộ bắt lỗi `@RestControllerAdvice` không thể phân loại lỗi chính xác và buộc phải xử lý nó như một lỗi hệ thống 500 (Internal Server Error), gây hiểu nhầm cho client.

### 3.3. Sử dụng các Exception có sẵn của Java khi phù hợp
Nếu lỗi mang tính chất cú pháp lập trình thuần túy, hãy sử dụng các exception tiêu chuẩn của Java thay vì tạo exception mới:
*   `IllegalArgumentException`: Khi tham số truyền vào hàm bị sai (ở phạm vi nội bộ hàm).
*   `IllegalStateException`: Khi trạng thái đối tượng chưa sẵn sàng để thực hiện hàm.

---

## 4. Logging Best Practices (Nhật ký lỗi)

### 4.1. Quy tắc "Không vừa Log vừa Throw" (Do NOT Log and Throw)
*   Đây là sai lầm phổ biến nhất trong các dự án Java. Khi phát hiện lỗi ở Engine, lập trình viên ghi log lỗi rồi ném Exception. Manager bắt được Exception lại ghi log rồi ném tiếp lên Controller. Controller tiếp tục ghi log hoặc chuyển cho Global Exception Handler log thêm một lần nữa.
*   **Hậu quả**: File log bị trùng lặp thông tin từ 3 đến 4 lần cho cùng một lỗi, gây nhiễu và làm dung lượng log phình to.
*   **Quy chuẩn**: Chỉ ném exception lên tầng trên. Việc ghi log lỗi thuộc trách nhiệm duy nhất của **GlobalExceptionHandler** ở tầng ngoài cùng của ứng dụng.

```text
[SAI]
try {
    simulator.move(agent, cell);
} catch (GameRuleException ex) {
    logger.error("Move failed", ex); // <--- SAI: Log ở đây
    throw ex;                         // <--- và tiếp tục throw
}

[ĐÚNG]
if (violatesRule) {
    throw new GameRuleViolationException(ErrorCode.INVALID_TARGET_TERRAIN, "Cannot move...");
} // Không log tại chỗ, để GlobalExceptionHandler tự động log ở lớp ngoài cùng.
```

### 4.2. Sử dụng Ghi log có tham số (Parameterized Logging)
*   Sử dụng cú pháp `{}` của SLF4J thay vì cộng chuỗi trực tiếp.
*   *Lý do*: Việc cộng chuỗi `logger.debug("Processing agent " + agentId + " at " + posX)` sẽ tốn chi phí cấp phát bộ nhớ tạo chuỗi ngay cả khi mức log DEBUG đang bị tắt. Sử dụng `{}` giúp hệ thống chỉ build chuỗi khi mức log đó thực sự được kích hoạt.

---

## 5. Clean Architecture Compliance (Tuân thủ Clean Architecture)
*   **Quy tắc**: Các lớp nằm trong các package `engine` (Core Logic) và `model` (Domain) không được phép import bất kỳ package nào liên quan đến Spring Framework, HTTP hoặc Servlet.
*   Các Exception ném ra từ `engine` chỉ chứa thông điệp nghiệp vụ và `ErrorCode`. Lớp `GlobalExceptionHandler` (Web Layer) sẽ làm nhiệm vụ nhận diện Exception đó và chuyển đổi sang HTTP Status Code tương ứng.

---

## 6. HTTP API Best Practices (Quy chuẩn REST API)
*   **HTTP Status phản ánh đúng ngữ nghĩa**: Tuân thủ đúng các mã trạng thái HTTP tiêu chuẩn.
*   **Cấu trúc JSON bất biến**: Giữ nguyên tên trường của DTO lỗi (`errorCode`, `message`, `timestamp`, `errors`) để Client không bị lỗi biên dịch hoặc lỗi runtime khi parse JSON.

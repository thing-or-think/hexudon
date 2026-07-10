# Tài liệu Thiết kế Exception Handling - 19_TESTING_GUIDE

## 1. Purpose (Mục đích)
Tài liệu này hướng dẫn lập trình viên phương pháp xây dựng các ca kiểm thử (Test Cases) tự động để xác minh độ chính xác của cơ chế xử lý lỗi trong **HEXUDON Server**. Việc kiểm thử tự động giúp bảo vệ hệ thống không bị lỗi hồi quy (regression) khi cập nhật logic game hoặc refactor mã nguồn trong tương lai.

---

## 2. Scope (Phạm vi)
Bao gồm Unit Testing cho các DTO/Exception class và Integration Testing sử dụng Spring `MockMvc` cho các API Endpoints.

---

## 3. Unit Testing Guidelines (Kiểm thử đơn vị)
Tập trung kiểm tra tính đúng đắn của logic khởi tạo trong các Exception và Validator.

### 3.1. Kiểm thử Exception Constructors
*   **Mục tiêu**: Đảm bảo các Exception con gán đúng HTTP status code và `ErrorCode`.
*   **Nội dung kiểm tra**:
    *   Tạo mới `GameRuleViolationException(ErrorCode.AGENT_OUT_OF_FUEL, "message")`.
    *   Dùng AssertJ để verify: `getStatus()` trả về `400`, `getErrorCode()` trả về `ErrorCode.AGENT_OUT_OF_FUEL` và `getMessage()` trả về đúng nội dung.

### 3.2. Kiểm thử Bean Validation tĩnh trên DTO
*   **Mục tiêu**: Đảm bảo các annotation tĩnh hoạt động bình thường mà không cần khởi chạy Spring Container.
*   **Cách làm**:
    *   Khởi tạo `ValidatorFactory factory = Validation.buildDefaultValidatorFactory()`.
    *   Sử dụng `factory.getValidator().validate(dtoObject)` để lấy danh sách các vi phạm ràng buộc (`ConstraintViolation`).
    *   Verify rằng khi gửi `teamName = ""` thì sẽ phát hiện 1 vi phạm và chứa đúng message đã định nghĩa.

---

## 4. Integration Testing with MockMvc (Kiểm thử tích hợp REST API)
Đây là phần quan trọng nhất, giả lập các HTTP request lỗi gửi lên server và xác minh phản hồi JSON thô.

### 4.1. Cấu trúc một lớp Test tiêu chuẩn
Lớp test tích hợp sẽ sử dụng `@SpringBootTest` kết hợp `@AutoConfigureMockMvc` để kiểm tra toàn bộ luồng từ HTTP xuống Database/Engine.

### 4.2. Các kịch bản kiểm thử bắt buộc (Core Test Cases)

#### Kịch bản 1: Kiểm thử lỗi Validation đầu vào (Validation Error)
*   **Mục tiêu**: Gửi dữ liệu đăng ký đội sai quy tắc và kiểm tra cấu trúc JSON lỗi chi tiết.
*   **Request**: `POST /api/match/register` với payload `{"teamName": ""}` (tên trống).
*   **Kết quả mong đợi (Assertions)**:
    *   HTTP Status Code: `400 Bad Request`.
    *   JSON Path `$.errorCode` bằng `"VALIDATION_ERROR"`.
    *   JSON Path `$.errors` là một mảng và có độ dài bằng 1.
    *   JSON Path `$.errors[0].field` bằng `"teamName"`.
    *   JSON Path `$.errors[0].message` bằng `"Team name must not be blank."`.

#### Kịch bản 2: Kiểm thử lỗi trùng tên đội (Business Error)
*   **Mục tiêu**: Đăng ký một đội chơi đã tồn tại.
*   **Tiền điều kiện**: Hệ thống đã có đội đăng ký tên `"TeamAlpha"`.
*   **Request**: `POST /api/match/register` với payload `{"teamName": "TeamAlpha"}`.
*   **Kết quả mong đợi (Assertions)**:
    *   HTTP Status Code: `400 Bad Request`.
    *   JSON Path `$.errorCode` bằng `"TEAM_ALREADY_EXISTS"`.
    *   JSON Path `$.message` chứa thông tin giải thích trùng tên.

#### Kịch bản 3: Kiểm thử gọi sai phương thức HTTP (HTTP Method Not Allowed)
*   **Mục tiêu**: Gọi API đăng ký bằng phương thức `GET` thay vì `POST`.
*   **Request**: `GET /api/match/register`.
*   **Kết quả mong đợi (Assertions)**:
    *   HTTP Status Code: `405 Method Not Allowed`.
    *   JSON Path `$.errorCode` bằng `"METHOD_NOT_ALLOWED"`.

#### Kịch bản 4: Kiểm thử lỗi Spam Request (Rate Limit)
*   **Mục tiêu**: Giả lập Client gửi 20 request liên tiếp trong 1 giây.
*   **Request**: Gửi liên tục `GET /api/match/state`.
*   **Kết quả mong đợi (Assertions)**:
    *   HTTP Status Code ở request thứ 11 trở đi phải là `429 Too Many Requests`.
    *   JSON Path `$.errorCode` bằng `"RATE_LIMIT_EXCEEDED"`.

# Tài liệu Thiết kế Exception Handling - 09_HTTP_STATUS_MAPPING

## 1. Purpose (Mục đích)
Tài liệu này đặc tả chi tiết bảng ánh xạ (Mapping) giữa các Exception cụ thể trong **HEXUDON Server** với các mã trạng thái HTTP (HTTP Status Codes) tiêu chuẩn của giao thức REST API. Điều này đảm bảo tính tương thích và tuân thủ các chuẩn thiết kế API quốc tế, giúp các thư viện HTTP Client của đối thủ/người chơi dễ dàng nhận biết kết quả thông qua mã trả về từ Web Server.

---

## 2. Scope (Phạm vi)
Áp dụng đối với tất cả các Exception được xử lý bởi `GlobalExceptionHandler` và chuyển dịch thành HTTP Response.

---

## 3. Mapping Matrix (Bảng ánh xạ chi tiết)

Dưới đây là bảng ánh xạ đầy đủ giữa lớp Ngoại lệ, Custom Error Code và HTTP Status:

| Ngoại lệ phát sinh (Exception Class) | HTTP Status Code | HTTP Status Name | Custom Error Code | Rationale (Lý do lựa chọn ánh xạ) |
| :--- | :--- | :--- | :--- | :--- |
| `GameRuleViolationException` | `400` | Bad Request | *Tùy biến theo luật vi phạm* | Client gửi hành động sai quy định trò chơi. Máy chủ từ chối thực thi do dữ liệu nghiệp vụ không hợp lệ. |
| `MatchStateConflictException`| `400` | Bad Request | `MATCH_STATE_CONFLICT` | Trạng thái hiện tại của game không cho phép thực hiện hành động này. Yêu cầu gửi lên bị xung đột với quy trình lifecycle. |
| `ResourceNotFoundException` | `404` | Not Found | `TEAM_NOT_FOUND` / `AGENT_NOT_FOUND` | Thực thể được yêu cầu (Đội, Agent, Cell) không tồn tại trên hệ thống. |
| `RateLimitExceededException`| `429` | Too Many Requests | `RATE_LIMIT_EXCEEDED` | Client gửi quá số lượng request định mức trên giây. Ngăn chặn tấn công DOS/Spam. |
| `MethodArgumentNotValidException`| `400`| Bad Request | `VALIDATION_ERROR` | Cấu trúc DTO gửi lên bị thiếu trường bắt buộc hoặc vi phạm các kiểm tra ràng buộc tĩnh `@Min`/`@NotBlank`. |
| `ConstraintViolationException`| `400`| Bad Request | `VALIDATION_ERROR` | Vi phạm ràng buộc dữ liệu tại tham số URL hoặc Path Variable. |
| `HttpMessageNotReadableException`| `400`| Bad Request | `INVALID_JSON_PAYLOAD` | Request body bị lỗi cú pháp JSON nghiêm trọng, Server không thể phân tích cú pháp (De-serialize). |
| `HttpRequestMethodNotSupportedException`| `405`| Method Not Allowed | `METHOD_NOT_ALLOWED` | Client gọi sai phương thức HTTP (Ví dụ gọi `GET /api/match/register` thay vì `POST`). |
| `HttpMediaTypeNotSupportedException`| `415`| Unsupported Media Type| `UNSUPPORTED_MEDIA_TYPE`| Client không gửi Header `Content-Type: application/json` khi gọi các POST APIs. |
| `ConfigLoadException` | `500` | Internal Server Error | `CONFIG_ERROR` | Lỗi phát sinh từ phía Server khi cố gắng đọc file cấu hình hệ thống. Lỗi này thuộc trách nhiệm của Server. |
| `java.lang.NullPointerException`| `500`| Internal Server Error | `INTERNAL_SERVER_ERROR` | Lỗi lập trình hoặc trạng thái dữ liệu rỗng ngoài ý muốn trên Server. Báo lỗi hệ thống chung để bảo mật. |
| `java.lang.Exception` (Fallback)| `500`| Internal Server Error | `INTERNAL_SERVER_ERROR` | Mọi lỗi không xác định khác phát sinh tại máy chủ. |

---

## 4. Design Principles (Nguyên tắc ánh xạ mã lỗi HTTP)
1.  **4xx cho lỗi phía Client (Client-Side Errors)**: Bất kỳ khi nào Client có khả năng tự sửa đổi nội dung request để thực hiện lại thành công, bắt buộc phải trả mã `4xx`.
2.  **5xx cho lỗi phía Server (Server-Side Errors)**: Khi máy chủ gặp sự cố về tài nguyên phần cứng, lỗi code lập trình, mất kết nối cơ sở dữ liệu, bắt buộc phải trả mã `5xx`.
3.  **Không lạm dụng mã 500**: Tránh ném lỗi hệ thống `500` cho các lỗi do Client truyền sai tham số. Lỗi này làm sai lệch số liệu giám sát của các hệ thống APM (như Prometheus, NewRelic), gây báo động giả (False Alarm) cho đội ngũ vận hành.
4.  **Chính xác về mặt ngữ nghĩa (Semantic Accuracy)**:
    *   Sử dụng đúng mã `404` khi không tìm thấy tài nguyên thay vì mã `400` chung chung.
    *   Sử dụng đúng mã `429` khi bị chặn tần suất thay vì mã `403` (Forbidden - vốn dành cho lỗi phân quyền).

---

## 5. Examples & Implementation Guide (Hướng dẫn triển khai)
Tại `GlobalExceptionHandler`, việc thiết lập HTTP Status được thực hiện thông qua lớp `ResponseEntity` của Spring MVC:

```java
// Ví dụ xử lý ResourceNotFoundException
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
    ErrorResponse response = new ErrorResponse(
        ex.getErrorCode().getCode(),
        ex.getMessage(),
        System.currentTimeMillis()
    );
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // status = 404
}
```

---

## 6. Common Mistakes (Sai lầm thường gặp)
*   Trả về mã `200 OK` kèm theo JSON chứa cờ `success: false` khi xảy ra lỗi. Điều này phá vỡ cơ chế tự động xử lý lỗi của nhiều HTTP client và proxy.
*   Trả về mã `403 Forbidden` khi Client gửi request quá nhanh (Spam). Mã chính xác phải là `429 Too Many Requests`.

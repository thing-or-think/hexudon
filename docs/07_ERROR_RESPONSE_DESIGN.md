# Tài liệu Thiết kế Exception Handling - 07_ERROR_RESPONSE_DESIGN

## 1. Purpose (Mục đích)
Tài liệu này đặc tả chi tiết cấu trúc JSON phản hồi lỗi (Error Response Design) của hệ thống **HEXUDON Server**. Mục tiêu là đảm bảo mọi API Endpoint khi xảy ra lỗi đều trả về dữ liệu có chung một định dạng chuẩn, giúp phía Client (Web UI, Game Bot) lập trình bóc tách lỗi một cách dễ dàng và đồng nhất.

---

## 2. Scope (Phạm vi)
Áp dụng đối với tất cả dữ liệu trả về từ các API có mã trạng thái HTTP HTTP `4xx` và `5xx`.

---

## 3. Design Goals (Mục tiêu thiết kế)
*   **Consistency (Nhất quán)**: Mọi lỗi, dù là lỗi validation, lỗi nghiệp vụ game hay lỗi máy chủ sập, đều phải sử dụng cấu trúc JSON được đặc tả dưới đây.
*   **Cleanliness (Sạch sẽ)**: Ẩn các trường không có dữ liệu (ví dụ: trường danh sách lỗi chi tiết khi không phải lỗi validation) để giảm dung lượng mạng truyền tải.
*   **Time-zone Agnostic (Độc lập múi giờ)**: Sử dụng kiểu số nguyên dài đại diện cho Epoch Milliseconds để lưu thời gian lỗi, tránh các vấn đề lệch múi giờ giữa Client và Server.

---

## 4. Response Schema Specification (Đặc tả Schema phản hồi)

Cấu trúc đối tượng JSON lỗi gồm các trường sau:

| Trường (Field) | Kiểu dữ liệu | Nullability | Mô tả |
| :--- | :--- | :--- | :--- |
| `errorCode` | String | Non-null | Mã định danh lỗi (ví dụ: `AGENT_OUT_OF_FUEL`). Client sử dụng trường này để rẽ nhánh xử lý logic. |
| `message` | String | Non-null | Chuỗi mô tả lỗi chi tiết dành cho con người đọc (Developer Friendly Message). |
| `timestamp` | Long | Non-null | Thời điểm xảy ra lỗi tại Server, định dạng Epoch Milliseconds (ví dụ: `1720516800123`). |
| `errors` | Array | Nullable | Danh sách chứa chi tiết các lỗi ràng buộc dữ liệu. Chỉ xuất hiện khi `errorCode = VALIDATION_ERROR`. |

### Cấu trúc phần tử con trong mảng `errors` (ValidationErrorDetail):
Mỗi phần tử đại diện cho lỗi ở một trường dữ liệu cụ thể:
*   **`field`** (String, Non-null): Đường dẫn thuộc tính bị lỗi đầu vào (ví dụ: `agentPlans[0].actions[1].actionType`).
*   **`rejectedValue`** (String, Nullable): Giá trị sai mà Client đã gửi lên (đã chuyển đổi thành chuỗi).
*   **`message`** (String, Non-null): Câu mô tả chi tiết lỗi vi phạm ràng buộc (ví dụ: `must not be null`).

---

## 5. Serializer Configuration (Cấu hình tuần tự hóa)
Để đảm bảo JSON phản hồi sạch sẽ, ta sử dụng annotation `@JsonInclude(JsonInclude.Include.NON_NULL)` của Jackson trên class `ErrorResponse`. 

*   Nếu trường `errors` có giá trị `null` (trong các lỗi không phải validation), Jackson sẽ hoàn toàn loại bỏ trường `errors` khỏi chuỗi JSON trả về cho Client.
*   Trường `timestamp` luôn được serialize thành kiểu số nguyên thay vì định dạng ngày giờ dạng String.

---

## 6. Examples (Các ví dụ thực tế)

### Ví dụ 1: Lỗi Nghiệp vụ thông thường (Ví dụ: Agent không đủ xăng)
*   **HTTP Status Code**: `400 Bad Request`
*   **JSON Response Body**:
```json
{
  "errorCode": "AGENT_OUT_OF_FUEL",
  "message": "Agent A1 (PATROL) currently has 10 fuel steps remaining, but moving to cell (4, 5) requires 15 fuel steps.",
  "timestamp": 1720516800123
}
```

### Ví dụ 2: Lỗi Validation Dữ liệu đầu vào (Ví dụ: gửi thiếu và sai trường)
*   **HTTP Status Code**: `400 Bad Request`
*   **JSON Response Body**:
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Request body validation failed. Please check the details.",
  "timestamp": 1720516800456,
  "errors": [
    {
      "field": "day",
      "rejectedValue": "0",
      "message": "must be greater than or equal to 1"
    },
    {
      "field": "agentPlans[0].agentId",
      "rejectedValue": "",
      "message": "must not be blank"
    }
  ]
}
```

### Ví dụ 3: Lỗi Hệ thống không xác định (Ví dụ: NullPointerException trong Controller)
*   **HTTP Status Code**: `500 Internal Server Error`
*   **JSON Response Body**:
```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred. Please contact the administrator.",
  "timestamp": 1720516800999
}
```
*(Lưu ý: Không trả về chi tiết NullPointerException hay stacktrace ra client để bảo mật)*

---

## 7. Versioning & Backward Compatibility (Tương thích ngược)
*   **Không xóa hoặc đổi tên trường**: Các trường `errorCode`, `message`, `timestamp` và `errors` là cố định vĩnh viễn qua các version API.
*   **Mở rộng an toàn**: Nếu tương lai cần bổ sung thông tin (ví dụ: `errorCategory` hoặc `helpLink`), ta có thể thêm trường mới vào `ErrorResponse` dạng nullable. Các Client cũ sẽ tự động bỏ qua các trường mới này mà không bị sập.

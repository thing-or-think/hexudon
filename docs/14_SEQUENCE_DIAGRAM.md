# Tài liệu Thiết kế Exception Handling - 14_SEQUENCE_DIAGRAM

## 1. Purpose (Mục đích)
Tài liệu này mô tả trực quan luồng đi của dữ liệu và thứ tự thực thi (Sequence Workflow) giữa các thành phần trong hệ thống khi xảy ra ngoại lệ. Sử dụng sơ đồ Mermaid, tài liệu giúp lập trình viên hình dung rõ cách các Exception được kích hoạt, lan truyền ngược qua các lớp kiến trúc và được bắt gọn tại Global Exception Handler.

---

## 2. Scope (Phạm vi)
Áp dụng đối với các kịch bản lỗi phổ biến trên REST API của **HEXUDON Server**.

---

## 3. Scenarios (Các kịch bản lỗi chi tiết)

### Kịch bản 1: Vi phạm luật chơi tại Physics Engine
Khi Client gửi kế hoạch di chuyển Agent đi vào ô Hồ nước (`POND`). Lỗi được phát hiện sâu trong Engine tính toán vật lý.

```mermaid
sequenceDiagram
    autonumber
    actor Client as Game Bot
    participant Interceptor as RateLimiterInterceptor
    participant Controller as MatchController
    participant Manager as MatchManager
    participant Engine as MovementSimulator
    participant GEH as GlobalExceptionHandler

    Client->>Interceptor: POST /api/match/actions (X-Team-Name: TeamAlpha)
    activate Interceptor
    Interceptor->>Interceptor: Kiểm tra tần suất (Hợp lệ)
    Interceptor-->>Controller: Chuyển tiếp Request
    deactivate Interceptor
    activate Controller
    Controller->>Controller: Bean Validation (Hợp lệ)
    Controller->>Manager: submitActions(plans)
    activate Manager
    Manager->>Engine: simulateMovement(agent, targetCell)
    activate Engine
    Engine->>Engine: Kiểm tra targetCell.getTerrain()
    Note over Engine: Phát hiện target là POND!
    Engine-->>Engine: Khởi tạo GameRuleViolationException
    destroy Engine
    Engine-->>Manager: throw GameRuleViolationException
    deactivate Engine
    destroy Manager
    Manager-->>Controller: throw GameRuleViolationException
    deactivate Manager
    destroy Controller
    Controller-->>GEH: Bắn Exception lên Spring MVC
    deactivate Controller
    activate GEH
    GEH->>GEH: 1. Đọc errorCode: INVALID_TARGET_TERRAIN<br/>2. Ghi log WARN (Không stacktrace)<br/>3. Tạo JSON ErrorResponse
    GEH-->>Client: Trả về HTTP 400 Bad Request + ErrorResponse JSON
    deactivate GEH
```

---

### Kịch bản 2: Lỗi Validation DTO tại Controller
Khi Client gửi request đăng ký đội với tên rỗng (`""`). Lỗi được chặn ngay tại cửa ngõ Controller.

```mermaid
sequenceDiagram
    autonumber
    actor Client as Game Bot
    participant Disp as Spring DispatcherServlet
    participant Controller as MatchController
    participant GEH as GlobalExceptionHandler

    Client->>Disp: POST /api/match/register (teamName: "")
    activate Disp
    Disp->>Disp: Kích hoạt Bean Validation<br/>(teamName vi phạm @NotBlank)
    Note over Disp: Khởi tạo MethodArgumentNotValidException
    Disp->>GEH: Chuyển tiếp MethodArgumentNotValidException
    deactivate Disp
    activate GEH
    GEH->>GEH: 1. Duyệt BindingResult lấy danh sách FieldErrors<br/>2. Ánh xạ sang ValidationErrorDetail DTOs<br/>3. Đóng gói ErrorResponse (VALIDATION_ERROR)<br/>4. Log level INFO
    GEH-->>Client: Trả về HTTP 400 Bad Request + chi tiết lỗi Validation
    deactivate GEH
```

---

### Kịch bản 3: Bị chặn Spam bởi RateLimiterInterceptor
Khi Client gửi quá nhiều request trong 1 giây. Lỗi được xử lý ở tầng Interceptor trước khi vào Controller.

```mermaid
sequenceDiagram
    autonumber
    actor Client as Game Bot
    participant Interceptor as RateLimiterInterceptor
    participant GEH as GlobalExceptionHandler

    Client->>Interceptor: POST /api/match/state (Request thứ 20 trong 1s)
    activate Interceptor
    Interceptor->>Interceptor: Kiểm tra bộ nhớ đệm Bucket4j / Redis
    Note over Interceptor: Tần suất vượt quá giới hạn!
    Interceptor-->>Interceptor: Khởi tạo RateLimitExceededException
    Interceptor->>GEH: throw RateLimitExceededException
    deactivate Interceptor
    activate GEH
    GEH->>GEH: 1. Log WARN (Cảnh báo spam IP)<br/>2. Đóng gói ErrorResponse (RATE_LIMIT_EXCEEDED)
    GEH-->>Client: Trả về HTTP 429 Too Many Requests + JSON ErrorResponse
    deactivate GEH
```

---

### Kịch bản 4: Lỗi hệ thống phát sinh đột xuất (NullPointerException)
Khi xảy ra lỗi lập trình nội bộ tại MatchManager (ví dụ: truy cập thuộc tính của Object null).

```mermaid
sequenceDiagram
    autonumber
    actor Client as Game Bot
    participant Controller as MatchController
    participant Manager as MatchManager
    participant GEH as GlobalExceptionHandler

    Client->>Controller: GET /api/match/state
    activate Controller
    Controller->>Manager: getCurrentState()
    activate Manager
    Manager->>Manager: Logic lỗi sinh NullPointerException
    Manager-->>Controller: throw NullPointerException
    deactivate Manager
    Controller-->>GEH: Bắn Exception lên Spring MVC
    deactivate Controller
    activate GEH
    GEH->>GEH: 1. Log ERROR kèm full Stacktrace (Khẩn cấp)<br/>2. Tạo ErrorResponse ẩn lỗi thật (INTERNAL_SERVER_ERROR)
    GEH-->>Client: Trả về HTTP 500 Internal Server Error + JSON thông báo chung
    deactivate GEH
```

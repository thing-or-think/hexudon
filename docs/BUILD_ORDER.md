# Kế hoạch Triển khai Exception Handling - BUILD_ORDER.md

Tài liệu này đặc tả **thứ tự triển khai (Build Order)** và **checklist** chi tiết để chuẩn hóa toàn bộ hệ thống xử lý ngoại lệ (Exception Handling) và quản lý lỗi của dự án **HEXUDON Server**.

---

## Mục lục
1. [Tổng quan Lộ trình Triển khai](#tổng-quan-lộ-trình-triển-khai)
2. [Chi tiết các bước triển khai](#chi-tiết-các-bước-triển-khai)
   * [Bước 1: Chuẩn hóa mã lỗi ErrorCode](#bước-1-chuẩn-hóa-mã-lỗi-errorcode)
   * [Bước 2: Triển khai Base Exception (Business & System)](#bước-2-triển-khai-base-exception-business--system)
   * [Bước 3: Triển khai các Custom Exception cụ thể](#bước-3-triển-khai-các-custom-exception-cụ-thể)
   * [Bước 4: Chuẩn hóa DTO ErrorResponse & ValidationErrorDetail](#bước-4-chuẩn-hóa-dto-errorresponse--validationerrordetail)
   * [Bước 5: Tái cấu trúc GlobalExceptionHandler](#bước-5-tái-cấu-trúc-globalexceptionhandler)
   * [Bước 6: Refactor Model Layer (Spot, Agent, Team, MatchState)](#bước-6-refactor-model-layer-spot-agent-team-matchstate)
   * [Bước 7: Refactor Engine Layer (ActionValidator, MovementSimulator, UdonCollection)](#bước-7-refactor-engine-layer-actionvalidator-movementsimulator-udoncollection)
   * [Bước 8: Refactor Manager & Interceptors](#bước-8-refactor-manager--interceptors)
   * [Bước 9: Loại bỏ code thừa (Main.java)](#bước-9-loại-bỏ-code-thừa-mainjava)
   * [Bước 10: Xây dựng bộ test tích hợp và kiểm thử ngoại lệ](#bước-10-xây-dựng-bộ-test-tích-hợp-và-kiểm-thử-ngoại-lệ)
3. [Tổng hợp dữ liệu thiết kế](#tổng-hợp-dữ-liệu-thiết-kế)
   * [Thống kê tổng số bước và file cần sửa](#thống-kê-tổng-số-bước-và-file-cần-sửa)
   * [Danh sách ngoại lệ (Exception)](#danh-sách-ngoại-lệ-exception)
   * [Danh sách mã lỗi (ErrorCode)](#danh-sách-mã-lỗi-errorcode)
4. [Phân tích rủi ro & Thứ tự tối ưu](#phân-tích-rủi-ro--thứ-tự-tối-ưu)

---

## Tổng quan Lộ trình Triển khai

```text
[Bước 1: ErrorCode] 
       ↓
[Bước 2: Base Exceptions]
       ↓
[Bước 3: Custom Exceptions] ── (Xóa GameException)
       ↓
[Bước 4: Response DTOs]
       ↓
[Bước 5: GlobalExceptionHandler]
       ↓
[Bước 6: Refactor Models]
       ↓
[Bước 7: Refactor Engines]
       ↓
[Bước 8: Refactor Manager & Interceptor]
       ↓
[Bước 9: Dọn dẹp Main.java]
       ↓
[Bước 10: Testing]
```

---

## Chi tiết các bước triển khai

### Bước 1: Chuẩn hóa mã lỗi ErrorCode
#### Mục tiêu
Xây dựng một Enum `ErrorCode` duy nhất chứa tất cả các mã lỗi nghiệp vụ và hệ thống. Đây là dependency cơ sở cho tất cả các exception class và response payload sau này.
#### File cần sửa/tạo mới
*   `[NEW]` [ErrorCode.java](file:///d:/Documents/GitHub/hexudon/src/main/java/com/naprock/hexudon/exception/code/ErrorCode.java) (Tạo mới gói `exception.code`)
#### Công việc
*   [ ] Định nghĩa enum `ErrorCode` với hai trường `code` (String) và `defaultMessage` (String).
*   [ ] Đưa toàn bộ 20 mã lỗi thuộc 7 nhóm (Validation, Registration, Match Lifecycle, Action Submission, Game Rules, Resources, System) đã thiết kế vào enum này.
*   [ ] Viết getter cho các trường dữ liệu của enum.
#### Exception liên quan
Không có.
#### ErrorCode liên quan
Toàn bộ danh mục `ErrorCode` (như `VALIDATION_ERROR`, `TEAM_ALREADY_EXISTS`, `AGENT_OUT_OF_FUEL`,...).
#### Sau bước này
Hệ thống có một danh mục mã lỗi tập trung làm cơ sở định danh lỗi cho client.
*   **Độ khó**: Dễ
*   **Ước lượng thời gian**: 30 phút
*   **Mức độ ưu tiên**: Rất cao
*   **Dependency**: Không có

---

### Bước 2: Triển khai Base Exception (Business & System)
#### Mục tiêu
Tạo ra các Exception gốc đại diện cho hai nhánh lỗi: Lỗi nghiệp vụ (do Client) và Lỗi hạ tầng (do Server), giúp phân nhóm xử lý tại Global Exception Handler.
#### File cần sửa/tạo mới
*   `[NEW]` [BusinessException.java](file:///d:/Documents/GitHub/hexudon/src/main/java/com/naprock/hexudon/exception/base/BusinessException.java) (Tạo mới gói `exception.base`)
*   `[NEW]` [SystemException.java](file:///d:/Documents/GitHub/hexudon/src/main/java/com/naprock/hexudon/exception/base/SystemException.java) (Tạo mới gói `exception.base`)
#### Công việc
*   [ ] Viết class `BusinessException` kế thừa từ `RuntimeException`. Khai báo thuộc tính `ErrorCode` và `int status` (HTTP Status code).
*   [ ] Viết class `SystemException` kế thừa từ `RuntimeException`. Khai báo thuộc tính `ErrorCode`.
*   [ ] Xây dựng các constructor nhận message, code và cause (nguyên nhân gốc) cho cả hai class.
#### Exception liên quan
`BusinessException`, `SystemException`.
#### ErrorCode liên quan
`INTERNAL_SERVER_ERROR`.
#### Sau bước này
Định hình xong khung kế thừa ngoại lệ của HEXUDON Server.
*   **Độ khó**: Dễ
*   **Ước lượng thời gian**: 30 phút
*   **Mức độ ưu tiên**: Rất cao
*   **Dependency**: Bước 1

---

### Bước 3: Triển khai các Custom Exception cụ thể
#### Mục tiêu
Tạo ra các lớp ngoại lệ nghiệp vụ và kỹ thuật cụ thể có ngữ nghĩa rõ ràng, đồng thời xóa bỏ exception gốc thô sơ `GameException` để tránh việc ném lỗi vô danh.
#### File cần sửa/tạo mới
*   `[NEW]` [ResourceNotFoundException.java](file:///d:/Documents/GitHub/hexudon/src/main/java/com/naprock/hexudon/exception/business/ResourceNotFoundException.java)
*   `[NEW]` [MatchStateConflictException.java](file:///d:/Documents/GitHub/hexudon/src/main/java/com/naprock/hexudon/exception/business/MatchStateConflictException.java)
*   `[MODIFY]` [GameRuleViolationException.java](file:///d:/Documents/GitHub/hexudon/src/main/java/com/naprock/hexudon/exception/GameRuleViolationException.java) (Di chuyển vào `exception.business` và đổi kế thừa sang `BusinessException`)
*   `[MODIFY]` [RateLimitExceededException.java](file:///d:/Documents/GitHub/hexudon/src/main/java/com/naprock/hexudon/exception/RateLimitExceededException.java) (Di chuyển vào `exception.business` và đổi kế thừa sang `BusinessException`)
*   `[MODIFY]` [ConfigLoadException.java](file:///d:/Documents/GitHub/hexudon/src/main/java/com/naprock/hexudon/exception/ConfigLoadException.java) (Di chuyển vào `exception.system` và đổi kế thừa sang `SystemException`)
*   `[DELETE]` [GameException.java](file:///d:/Documents/GitHub/hexudon/src/main/java/com/naprock/hexudon/exception/GameException.java) (Xóa bỏ hoàn toàn)
#### Công việc
*   [ ] Triển khai `ResourceNotFoundException` và `MatchStateConflictException` kế thừa từ `BusinessException`. Thiết lập HTTP status mặc định lần lượt là `404` và `400`.
*   [ ] Thay đổi khai báo kế thừa của `GameRuleViolationException` sang `BusinessException` với HTTP status mặc định là `400`.
*   [ ] Thay đổi khai báo kế thừa của `RateLimitExceededException` sang `BusinessException` với HTTP status mặc định là `429`.
*   [ ] Thay đổi khai báo kế thừa của `ConfigLoadException` sang `SystemException`.
*   [ ] Xóa file `GameException.java`. Cập nhật các file import liên quan tạm thời để tránh compile error (ở bước này các class cũ sẽ báo đỏ, cần refactor các class đó ngay ở bước tiếp theo).
#### Exception liên quan
`ResourceNotFoundException`, `MatchStateConflictException`, `GameRuleViolationException`, `RateLimitExceededException`, `ConfigLoadException`.
#### ErrorCode liên quan
`RATE_LIMIT_EXCEEDED`, `CONFIG_ERROR`.
#### Sau bước này
Hoàn thành cấu trúc phân cấp lỗi nghiệp vụ chi tiết.
*   **Độ khó**: Trung bình (Do phải sửa đổi import trên nhiều file có sẵn)
*   **Ước lượng thời gian**: 1 giờ
*   **Mức độ ưu tiên**: Cao
*   **Dependency**: Bước 2

---

### Bước 4: Chuẩn hóa DTO ErrorResponse & ValidationErrorDetail
#### Mục tiêu
Thiết kế lại cấu trúc dữ liệu phản hồi lỗi để hỗ trợ hiển thị chi tiết các trường bị validate lỗi khi client gửi request sai định dạng.
#### File cần sửa/tạo mới
*   `[NEW]` [ValidationErrorDetail.java](file:///d:/Documents/GitHub/hexudon/src/main/java/com/naprock/hexudon/exception/response/ValidationErrorDetail.java) (Tạo mới gói `exception.response`)
*   `[MODIFY]` [ErrorResponse.java](file:///d:/Documents/GitHub/hexudon/src/main/java/com/naprock/hexudon/exception/ErrorResponse.java) (Di chuyển vào `exception.response`)
#### Công việc
*   [ ] Định nghĩa Java Record `ValidationErrorDetail` chứa các trường `field`, `rejectedValue`, `message`.
*   [ ] Thêm danh sách `List<ValidationErrorDetail> errors` vào class `ErrorResponse`.
*   [ ] Cấu hình annotation `@JsonInclude(JsonInclude.Include.NON_NULL)` trên class `ErrorResponse` để ẩn trường `errors` khi không có lỗi validation.
*   [ ] Tạo các constructors tương ứng cho `ErrorResponse`.
#### Exception liên quan
Không có.
#### ErrorCode liên quan
`VALIDATION_ERROR`.
#### Sau bước này
Sẵn sàng cấu trúc DTO trả về cho client.
*   **Độ khó**: Dễ
*   **Ước lượng thời gian**: 30 phút
*   **Mức độ ưu tiên**: Cao
*   **Dependency**: Bước 3

---

### Bước 5: Tái cấu trúc GlobalExceptionHandler
#### Mục tiêu
Nâng cấp bộ bắt lỗi tập trung để xử lý tự động `BusinessException` (đọc HTTP status động), bóc tách danh sách lỗi của Spring validation, log đúng cấp độ và ẩn lỗi hệ thống thô.
#### File cần sửa/tạo mới
*   `[MODIFY]` [GlobalExceptionHandler.java](file:///d:/Documents/GitHub/hexudon/src/main/java/com/naprock/hexudon/exception/GlobalExceptionHandler.java) (Di chuyển vào gói `exception.handler`)
#### Công việc
*   [ ] Thêm phương thức bắt `@ExceptionHandler(BusinessException.class)`. Đọc `status` và `errorCode` từ exception, log level `WARN` không in stacktrace, trả về HTTP status động.
*   [ ] Thêm phương thức bắt `@ExceptionHandler(MethodArgumentNotValidException.class)`. Bóc tách `FieldError` từ `BindingResult` thành `ValidationErrorDetail`, trả về HTTP 400 và code `VALIDATION_ERROR`, log level `INFO`.
*   [ ] Thêm phương thức bắt `@ExceptionHandler(HttpMessageNotReadableException.class)` để xử lý lỗi JSON hỏng. Trả về HTTP 400 và code `INVALID_JSON_PAYLOAD`.
*   [ ] Sửa đổi phương thức `@ExceptionHandler(Exception.class)` làm fallback. Log level `ERROR` kèm full stacktrace, trả về thông báo chung `"An unexpected error occurred."` và code `INTERNAL_SERVER_ERROR`.
#### Exception liên quan
`BusinessException`, `MethodArgumentNotValidException`, `HttpMessageNotReadableException`, `Exception`.
#### ErrorCode liên quan
`VALIDATION_ERROR`, `INVALID_JSON_PAYLOAD`, `INTERNAL_SERVER_ERROR`.
#### Sau bước này
Hệ thống xử lý lỗi tập trung đi vào hoạt động, đảm bảo an toàn bảo mật và chuẩn hóa response.
*   **Độ khó**: Khá
*   **Ước lượng thời gian**: 1 giờ
*   **Mức độ ưu tiên**: Cao
*   **Dependency**: Bước 4

---

### Bước 6: Refactor Model Layer (Spot, Agent, Team, MatchState)
#### Mục tiêu
Loại bỏ việc ném `GameException` thô với chuỗi text cứng, thay thế các `IllegalArgumentException`/`IllegalStateException` không có mã lỗi bằng custom exceptions tương ứng.
#### File cần sửa/tạo mới
*   `[MODIFY]` [Spot.java](file:///d:/Documents/GitHub/hexudon/src/main/java/com/naprock/hexudon/model/Spot.java)
*   `[MODIFY]` [Agent.java](file:///d:/Documents/GitHub/hexudon/src/main/java/com/naprock/hexudon/model/Agent.java)
*   `[MODIFY]` [Team.java](file:///d:/Documents/GitHub/hexudon/src/main/java/com/naprock/hexudon/model/Team.java)
*   `[MODIFY]` [MatchState.java](file:///d:/Documents/GitHub/hexudon/src/main/java/com/naprock/hexudon/model/MatchState.java)
#### Công việc
*   [ ] **Tại Spot.java**: Thay thế các `IllegalArgumentException`/`IllegalStateException` ném ra khi set hoặc decrement Udon bằng `GameRuleViolationException` hoặc các exception nghiệp vụ cụ thể.
*   [ ] **Tại Agent.java**: Chuyển các `IllegalArgumentException("Spot must not be null.")` sang kiểm tra nghiệp vụ và ném đúng lỗi có `ErrorCode`.
*   [ ] **Tại Team.java**: Sửa `requireAgent` ném `ResourceNotFoundException(ErrorCode.AGENT_NOT_FOUND, ...)` thay vì `GameException`. Sửa `ensureEligible` ném `GameRuleViolationException(ErrorCode.TEAM_DISABLED, ...)` thay vì `GameException`.
*   [ ] **Tại MatchState.java**: Thay thế toàn bộ 7 vị trí đang ném `GameException` (như registerTeam, requireTeam, start, ensurePlaying) bằng các Exception nghiệp vụ cụ thể: `MatchStateConflictException`, `ResourceNotFoundException`. Sử dụng mã `ErrorCode` chính xác.
#### Exception liên quan
`ResourceNotFoundException`, `MatchStateConflictException`, `GameRuleViolationException`.
#### ErrorCode liên quan
`TEAM_NOT_FOUND`, `AGENT_NOT_FOUND`, `TEAM_ALREADY_EXISTS`, `MAX_TEAMS_REACHED`, `MATCH_NOT_WAITING`, `MATCH_NOT_PLAYING`, `MATCH_FINISHED`.
#### Sau bước này
Toàn bộ lớp dữ liệu Model được dọn dẹp sạch sẽ, không còn ném exception vô danh.
*   **Độ khó**: Khá (Đòi hỏi sự tỉ mỉ khi sửa đổi các điều kiện logic kiểm tra)
*   **Ước lượng thời gian**: 2 giờ
*   **Mức độ ưu tiên**: Cao
*   **Dependency**: Bước 5

---

### Bước 7: Refactor Engine Layer (ActionValidator, MovementSimulator, UdonCollection)
#### Mục tiêu
Tái cấu trúc các Engine nghiệp vụ để loại bỏ việc ném `GameException` vô danh, thay thế các `IllegalArgumentException` trong các switch case và loại bỏ việc trả về `null` tùy tiện gây nguy cơ NullPointerException.
#### File cần sửa/tạo mới
*   `[MODIFY]` [ActionValidatorEngine.java](file:///d:/Documents/GitHub/hexudon/src/main/java/com/naprock/hexudon/engine/ActionValidatorEngine.java)
*   `[MODIFY]` [MovementSimulator.java](file:///d:/Documents/GitHub/hexudon/src/main/java/com/naprock/hexudon/engine/MovementSimulator.java)
*   `[MODIFY]` [UdonCollectionEngine.java](file:///d:/Documents/GitHub/hexudon/src/main/java/com/naprock/hexudon/engine/UdonCollectionEngine.java)
#### Công việc
*   [ ] **Tại ActionValidatorEngine.java**: Sửa đổi 3 vị trí ném `GameException` (validateDuplicateAgent, validateAgentCount, validateActionOrder) sang ném `GameRuleViolationException` với các mã lỗi tương ứng (`DUPLICATE_AGENT_PLAN`, `INCOMPLETE_AGENT_PLANS`, `NON_CONSECUTIVE_ORDER`).
*   [ ] Sửa đổi các block `default -> throw new IllegalArgumentException` trong switch-case tính toán xăng/bước đi của địa hình.
*   [ ] **Tại MovementSimulator.java**: Sửa đổi 3 vị trí ném `GameException` không tham số (khi Agent đi vào Hồ nước, thiếu bước đi, thiếu nhiên liệu) sang ném `GameRuleViolationException` kèm mã lỗi tương ứng (`INVALID_TARGET_TERRAIN`, `STEPS_LIMIT_EXCEEDED`, `AGENT_OUT_OF_FUEL`).
*   [ ] Refactor phương thức `simulateStep()` để trả về `Optional<Action>` thay vì trả về `null` trực tiếp. Cập nhật mã gọi hàm tại `simulateTeamTurn()` để bóc tách Optional.
*   [ ] **Tại UdonCollectionEngine.java**: Refactor phương thức `findSpotAt()` trả về `Optional<Spot>` thay vì trả về `null`. Cập nhật code gọi hàm tại `collectUdon()` để tránh NPE.
#### Exception liên quan
`GameRuleViolationException`.
#### ErrorCode liên quan
`INVALID_TARGET_TERRAIN`, `AGENT_OUT_OF_FUEL`, `STEPS_LIMIT_EXCEEDED`, `DUPLICATE_AGENT_PLAN`, `INCOMPLETE_AGENT_PLANS`, `NON_CONSECUTIVE_ORDER`.
#### Sau bước này
Toàn bộ logic vận hành game được kiểm soát lỗi chặt chẽ, an toàn với kiểu dữ liệu `Optional`.
*   **Độ khó**: Khá
*   **Ước lượng thời gian**: 2 giờ
*   **Mức độ ưu tiên**: Cao
*   **Dependency**: Bước 6

---

### Bước 8: Refactor Manager & Interceptors
#### Mục tiêu
Chuẩn hóa luồng đi của Interceptor và MatchManager, loại bỏ việc ném `GameException` với chuỗi text cứng, đồng thời thay thế việc ghi log ra màn hình bằng `System.out.println` sang Logger chuẩn của SLF4J.
#### File cần sửa/tạo mới
*   `[MODIFY]` [MatchManager.java](file:///d:/Documents/GitHub/hexudon/src/main/java/com/naprock/hexudon/manager/MatchManager.java)
*   `[MODIFY]` [RateLimiterInterceptor.java](file:///d:/Documents/GitHub/hexudon/src/main/java/com/naprock/hexudon/interceptor/RateLimiterInterceptor.java)
#### Công việc
*   [ ] **Tại MatchManager.java**: Sửa đổi câu lệnh `throw new GameException("The submitted day...")` sang ném `GameRuleViolationException(ErrorCode.DAY_MISMATCH, ...)`.
*   [ ] Khai báo `private static final Logger log = LoggerFactory.getLogger(MatchManager.class)` tại `MatchManager`.
*   [ ] Thay thế toàn bộ các câu lệnh `System.out.println` tại Constructor bằng `log.info(...)`.
*   [ ] **Tại RateLimiterInterceptor.java**: Thay thế việc ném `GameException` thô bằng:
    *   Lỗi thiếu header -> `GameRuleViolationException(ErrorCode.MISSING_REQUIRED_HEADER, ...)`
    *   Lỗi không tìm thấy team -> `ResourceNotFoundException(ErrorCode.TEAM_NOT_FOUND, ...)`
    *   Lỗi team bị loại -> `GameRuleViolationException(ErrorCode.TEAM_DISABLED, ...)`
#### Exception liên quan
`GameRuleViolationException`, `ResourceNotFoundException`, `RateLimitExceededException`.
#### ErrorCode liên quan
`DAY_MISMATCH`, `MISSING_REQUIRED_HEADER`, `TEAM_NOT_FOUND`, `TEAM_DISABLED`.
#### Sau bước này
Lớp điều phối và bộ chặn HTTP API đã được chuẩn hóa về xử lý ngoại lệ và ghi nhật ký hệ thống.
*   **Độ khó**: Trung bình
*   **Ước lượng thời gian**: 1.5 giờ
*   **Mức độ ưu tiên**: Trung bình
*   **Dependency**: Bước 7

---

### Bước 9: Loại bỏ code thừa (Main.java)
#### Mục tiêu
Dọn dẹp mã nguồn rác được sinh tự động khi khởi tạo dự án để tránh gây hiểu nhầm về cấu trúc package và làm bẩn file log hoặc in bừa bãi ra console.
#### File cần sửa/tạo mới
*   `[DELETE]` [Main.java](file:///d:/Documents/GitHub/hexudon/src/main/java/org/example/Main.java)
#### Công việc
*   [ ] Xóa bỏ hoàn toàn tệp tin `Main.java` trong thư mục `src/main/java/org/example/`.
*   [ ] Xóa bỏ package rác `org.example`.
#### Exception liên quan
Không có.
#### ErrorCode liên quan
Không có.
#### Sau bước này
Mã nguồn dự án hoàn toàn sạch sẽ, chỉ chứa các package nghiệp vụ của HEXUDON.
*   **Độ khó**: Dễ
*   **Ước lượng thời gian**: 10 phút
*   **Mức độ ưu tiên**: Thấp
*   **Dependency**: Không có

---

### Bước 10: Xây dựng bộ test tích hợp và kiểm thử ngoại lệ
#### Mục tiêu
Xây dựng và hoàn thiện các Unit Test, Integration Test sử dụng JUnit 5 và MockMvc để đảm bảo tất cả các API khi gặp dữ liệu lỗi đều trả về chính xác HTTP Status và cấu trúc JSON mong muốn.
#### File cần sửa/tạo mới
*   `[NEW]` [ErrorCodeTest.java](file:///d:/Documents/GitHub/hexudon/src/test/java/com/naprock/hexudon/exception/ErrorCodeTest.java)
*   `[NEW]` [ErrorResponseTest.java](file:///d:/Documents/GitHub/hexudon/src/test/java/com/naprock/hexudon/exception/ErrorResponseTest.java)
*   `[NEW]` [GlobalExceptionHandlerTest.java](file:///d:/Documents/GitHub/hexudon/src/test/java/com/naprock/hexudon/exception/GlobalExceptionHandlerTest.java)
*   `[NEW]` [MatchControllerIntegrationTest.java](file:///d:/Documents/GitHub/hexudon/src/test/java/com/naprock/hexudon/controller/MatchControllerIntegrationTest.java)
#### Công việc
*   [ ] Viết unit test xác minh các mã lỗi trong `ErrorCode` không bị trùng lặp chuỗi code.
*   [ ] Viết unit test xác minh class `ErrorResponse` và record `ValidationErrorDetail` serialize thành JSON đúng định dạng cấu trúc chỉ định.
*   [ ] Viết unit test giả lập (Mocking) ném `BusinessException` và kiểm tra phản hồi từ `GlobalExceptionHandler`.
*   [ ] Viết các bài test tích hợp MockMvc trên `MatchController` kiểm tra các API endpoints:
    *   Truyền tên đội rỗng -> assert HTTP 400, code `VALIDATION_ERROR`.
    *   Đăng ký trùng tên -> assert HTTP 400, code `TEAM_ALREADY_EXISTS`.
    *   Gửi lệnh khi match chưa chạy -> assert HTTP 400, code `MATCH_NOT_PLAYING`.
    *   Gửi request spam -> assert HTTP 429, code `RATE_LIMIT_EXCEEDED`.
#### Exception liên quan
Tất cả các exception đã thiết kế.
#### ErrorCode liên quan
Tất cả các mã lỗi đã thiết kế.
#### Sau bước này
Hệ thống Exception Handling đã được tự động hóa kiểm tra và nghiệm thu thành công.
*   **Độ khó**: Khá
*   **Ước lượng thời gian**: 2 giờ
*   **Mức độ ưu tiên**: Trung bình
*   **Dependency**: Bước 8

---

## Tổng hợp dữ liệu thiết kế

### Thống kê tổng số bước và file cần sửa
*   **Tổng số bước triển khai**: 10 bước.
*   **Tổng số file cần tạo mới/sửa đổi/xóa bỏ**: 22 files.
    *   *Tạo mới*: 8 files.
    *   *Sửa đổi (Refactor)*: 12 files.
    *   *Xóa bỏ*: 2 files.

### Danh sách ngoại lệ (Exception)
*   **Danh sách Exception hiện có trong dự án**:
    1.  `GameException` (Sẽ bị xóa)
    2.  `GameRuleViolationException` (Sẽ refactor)
    3.  `RateLimitExceededException` (Sẽ refactor)
    4.  `ConfigLoadException` (Sẽ refactor)
*   **Danh sách Exception đề xuất thêm mới**:
    1.  `BusinessException` (Base class lỗi nghiệp vụ, HTTP status code động)
    2.  `SystemException` (Base class lỗi hệ thống máy chủ)
    3.  `ResourceNotFoundException` (Lỗi không tìm thấy Team, Agent, Cell - HTTP 404)
    4.  `MatchStateConflictException` (Lỗi xung đột trạng thái trận đấu - HTTP 400)

### Danh sách mã lỗi (ErrorCode)
*   **Danh sách ErrorCode hiện có**: Không có (Hệ thống cũ dùng chuỗi text thô cứng).
*   **Danh sách ErrorCode đề xuất tạo mới (20 mã lỗi)**:
    *   *Validation & Payload*: `VALIDATION_ERROR`, `INVALID_JSON_PAYLOAD`, `MISSING_REQUIRED_HEADER`.
    *   *Registration*: `TEAM_NAME_BLANK`, `TEAM_ALREADY_EXISTS`, `MAX_TEAMS_REACHED`.
    *   *Lifecycle*: `MATCH_NOT_WAITING`, `MATCH_NOT_PLAYING`, `MATCH_FINISHED`, `MATCH_ALREADY_STARTED`.
    *   *Actions format*: `DAY_MISMATCH`, `DUPLICATE_AGENT_PLAN`, `INCOMPLETE_AGENT_PLANS`, `NON_CONSECUTIVE_ORDER`.
    *   *Game Rules*: `INVALID_TARGET_TERRAIN`, `AGENT_OUT_OF_FUEL`, `STEPS_LIMIT_EXCEEDED`, `PATH_NOT_ADJACENT`, `AGENT_DISABLED`.
    *   *Resource*: `TEAM_NOT_FOUND`, `AGENT_NOT_FOUND`, `CELL_OUT_OF_BOUNDS`.
    *   *System*: `CONFIG_ERROR`, `RATE_LIMIT_EXCEEDED`, `INTERNAL_SERVER_ERROR`.

---

## Phân tích rủi ro & Thứ tự tối ưu

### 1. Phân tích rủi ro khi triển khai
*   **Rủi ro 1: Biên dịch thất bại khi xóa GameException (Compile Breakage)**:
    *   *Nguyên nhân*: Do `GameException` đang được sử dụng ở rất nhiều nơi (Engine, Model, Interceptor). Việc xóa nó ở Bước 3 khi chưa refactor các lớp gọi nó sẽ khiến dự án bị đỏ lòm và không thể compile.
    *   *Giải pháp giảm thiểu*: Lập trình viên nên thực hiện refactor theo đúng trình tự từ Core (Model) ra ngoài (Engine, Manager, Controller). Tại Bước 3, tạm thời giữ lại khai báo rỗng của `GameException` hoặc tiến hành thay thế hàng loạt (Find & Replace) chuỗi `GameException` thành `BusinessException` hoặc các exception cụ thể tương ứng trước khi xóa tệp tin.
*   **Rủi ro 2: Ảnh hưởng tương thích ngược đối với bot client (Backward Compatibility)**:
    *   *Nguyên nhân*: Client cũ của người chơi có thể đang phân tích cú pháp chuỗi thông báo lỗi (ví dụ chuỗi `"TEAM_NOT_FOUND"` từ message). Khi ta chuyển sang cấu trúc JSON lỗi mới, bot client có thể bị sập.
    *   *Giải pháp giảm thiểu*: Phải đảm bảo trường `message` trong JSON `ErrorResponse` vẫn giữ lại thông tin mô tả cơ bản của lỗi cũ để bảo toàn logic của client.

### 2. Thứ tự triển khai tối ưu
Thứ tự triển khai từ trong ra ngoài (Domain-driven / Bottom-up approach) là lựa chọn tối ưu nhất:
1.  Định nghĩa mã lỗi trước (`ErrorCode`).
2.  Xây dựng lớp Exception cơ sở (`BusinessException`, `SystemException`).
3.  Tạo các Custom Exception cụ thể.
4.  Cập nhật Model nghiệp vụ (lớp trong cùng - không phụ thuộc Spring).
5.  Cập nhật Engine tính toán game.
6.  Xây dựng DTO phản hồi lỗi và bộ xử lý Global Exception Handler (Web layer - lớp ngoài cùng).
7.  Tích hợp validation và logging.
8.  Kiểm thử để nghiệm thu toàn bộ hệ thống lỗi.

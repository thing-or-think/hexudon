# Tài liệu Thiết kế Exception Handling - 06_ERROR_CODE_DESIGN

## 1. Purpose (Mục đích)
Tài liệu này định nghĩa hệ thống mã lỗi nghiệp vụ (Custom Error Codes) cho **HEXUDON Server**. Việc chuẩn hóa mã lỗi giúp Client (ví dụ: các bot tự động chơi) nhận biết chính xác nguyên nhân thất bại và tự động điều chỉnh hành vi chiến thuật lập tức mà không cần phân tích cú pháp thông báo lỗi (error message).

---

## 2. Scope (Phạm vi)
Áp dụng đối với tất cả các mã lỗi nghiệp vụ được lưu trữ trong enum `ErrorCode` và trả ra trong JSON `ErrorResponse`.

---

## 3. Design Principles (Nguyên tắc thiết kế mã lỗi)
*   **Unique (Duy nhất)**: Mỗi mã lỗi đại diện cho một và chỉ một nguyên nhân gốc rễ cụ thể.
*   **Predictable Scheme (Quy tắc đặt tên dễ đoán)**: Sử dụng chuỗi ký tự hoa phân tách bằng dấu gạch dưới (SNAKE_CASE) và phân nhóm theo tiền tố.
*   **Semantic HTTP Mapping**: Mỗi mã lỗi được gắn kết chặt chẽ với một mã trạng thái HTTP thích hợp ở tầng API.

---

## 4. Error Code Catalog (Danh mục mã lỗi hệ thống)

Hệ thống mã lỗi được chia thành 7 nhóm chức năng chính:

### Nhóm 1: Validation & Payload Format (Tiền tố `VAL_` hoặc lỗi chung)
| Error Code | HTTP Status | Description | Usage (Khi nào dùng) |
| :--- | :--- | :--- | :--- |
| `VALIDATION_ERROR` | 400 Bad Request | Request body contains fields that violate constraint rules. | Dùng khi Bean Validation (`@Valid`) phát hiện dữ liệu sai định dạng (ví dụ: trống tên đội). |
| `INVALID_JSON_PAYLOAD`| 400 Bad Request | The HTTP request body contains malformed JSON syntax. | Trình phân tích cú pháp Jackson không thể đọc JSON gửi lên. |
| `MISSING_REQUIRED_HEADER`| 400 Bad Request| Missing required custom headers like X-Team-Name. | Khi gửi Action mà thiếu Header xác định đội chơi. |

### Nhóm 2: Registration & Team Management (Tiền tố `REG_`)
| Error Code | HTTP Status | Description | Usage (Khi nào dùng) |
| :--- | :--- | :--- | :--- |
| `TEAM_NAME_BLANK` | 400 Bad Request | Team name must not be blank. | Khi đăng ký đội có tên chỉ chứa khoảng trắng hoặc rỗng. |
| `TEAM_ALREADY_EXISTS` | 400 Bad Request | A team with the specified name is already registered. | Khi đăng ký tên đội trùng với đội đã đăng ký trước đó. |
| `MAX_TEAMS_REACHED` | 400 Bad Request | The maximum number of teams has been reached. | Đăng ký thêm đội thứ 3 khi giới hạn chỉ cho phép tối đa 2 đội. |

### Nhóm 3: Match Lifecycle & State Conflicts (Tiền tố `LFC_`)
| Error Code | HTTP Status | Description | Usage (Khi nào dùng) |
| :--- | :--- | :--- | :--- |
| `MATCH_NOT_WAITING` | 400 Bad Request | The match is not in WAITING state. | Cố đăng ký đội mới khi trận đấu đang chơi hoặc đã kết thúc. |
| `MATCH_NOT_PLAYING` | 400 Bad Request | The match has not started yet. | Cố gửi lệnh điều khiển Agent khi trận đấu chưa bắt đầu. |
| `MATCH_FINISHED` | 400 Bad Request | The match has already finished. | Gửi lệnh khi trận đấu đã đạt giới hạn ngày (`maxTurns`) và kết thúc. |
| `MATCH_ALREADY_STARTED`| 400 Bad Request| The match is already in progress. | Gọi API `/start` khi trận đấu đã được kích hoạt chạy trước đó. |

### Nhóm 4: Action Submission Violations (Tiền tố `ACT_`)
| Error Code | HTTP Status | Description | Usage (Khi nào dùng) |
| :--- | :--- | :--- | :--- |
| `DAY_MISMATCH` | 400 Bad Request | The submitted day does not match the current game day. | Gửi kế hoạch cho ngày 5 trong khi server đang ở ngày 4. |
| `DUPLICATE_AGENT_PLAN`| 400 Bad Request | Each agent may have only one action plan per day. | Trong list gửi lên có 2 plan khác nhau cho cùng một Agent ID. |
| `INCOMPLETE_AGENT_PLANS`| 400 Bad Request| Must provide plans for all agents belonging to the team.| Danh sách gửi lên thiếu plan của một trong các Agent thuộc đội. |
| `NON_CONSECUTIVE_ORDER`| 400 Bad Request | Action orders must be consecutive starting from 1. | Danh sách lệnh của Agent có thứ tự nhảy vọt (ví dụ: lệnh 1, lệnh 3). |

### Nhóm 5: Game Rule & Physics Engine Violations (Tiền tố `GME_`)
| Error Code | HTTP Status | Description | Usage (Khi nào dùng) |
| :--- | :--- | :--- | :--- |
| `INVALID_TARGET_TERRAIN`| 400 Bad Request | Agent cannot move onto inaccessible terrain (e.g. Pond).| Lệnh di chuyển Agent vào ô có địa hình Hồ nước (`POND`). |
| `AGENT_OUT_OF_FUEL` | 400 Bad Request | Agent does not have enough fuel to perform this movement. | Nhiên liệu còn lại thấp hơn mức tiêu thụ của ô mục tiêu. |
| `STEPS_LIMIT_EXCEEDED`| 400 Bad Request | Movement plan exceeds the agent's step limit for this day. | Tổng số bước đi (cost) của chuỗi lệnh vượt quá số bước tối đa. |
| `PATH_NOT_ADJACENT` | 400 Bad Request | Target cell is not adjacent to the agent's current cell. | Cố di chuyển nhảy cóc sang ô không giáp cạnh (Hexagonal grid). |
| `AGENT_DISABLED` | 400 Bad Request | Agent is disabled and cannot act. | Agent đã bị truất quyền thi đấu hoặc bị khóa trạng thái. |

### Nhóm 6: Resource & Entity Resolution (Tiền tố `RSC_`)
| Error Code | HTTP Status | Description | Usage (Khi nào dùng) |
| :--- | :--- | :--- | :--- |
| `TEAM_NOT_FOUND` | 404 Not Found | The specified team could not be found. | Giá trị Header `X-Team-Name` không khớp với bất kỳ đội nào. |
| `AGENT_NOT_FOUND` | 404 Not Found | The specified agent ID does not belong to your team. | Gửi plan cho Agent ID không nằm trong danh sách sở hữu của Team. |
| `CELL_OUT_OF_BOUNDS` | 400 Bad Request | Target coordinates are out of the game map grid. | Tọa độ `targetX` hoặc `targetY` nằm ngoài phạm vi bản đồ game. |

### Nhóm 7: System & Infrastructure (Tiền tố `SYS_`)
| Error Code | HTTP Status | Description | Usage (Khi nào dùng) |
| :--- | :--- | :--- | :--- |
| `CONFIG_ERROR` | 500 Internal Server Error | Failed to load or parse match configuration file. | File `match_config.txt` bị lỗi cú pháp, bị thiếu hoặc không đọc được. |
| `RATE_LIMIT_EXCEEDED` | 429 Too Many Requests | API request rate limit exceeded. | Một đội chơi gửi request vượt quá ngưỡng cấu hình trong 1 giây. |
| `INTERNAL_SERVER_ERROR`| 500 Internal Server Error | An unexpected server error occurred. | Lỗi hệ thống không xác định (DB lỗi, NPE...). |

---

## 5. Examples (Ví dụ sử dụng Enum trong Code)
*   **Định nghĩa Enum**:
    ```java
    public enum ErrorCode {
        TEAM_ALREADY_EXISTS("TEAM_ALREADY_EXISTS", "A team with the specified name is already registered."),
        INVALID_TARGET_TERRAIN("INVALID_TARGET_TERRAIN", "Agent cannot move onto inaccessible terrain (e.g. Pond).");
        // ...
    }
    ```
*   **Khi sử dụng**:
    ```java
    throw new GameRuleViolationException(ErrorCode.INVALID_TARGET_TERRAIN, "Cannot move agent to Pond cell at (5,3)");
    ```

---

## 6. Common Mistakes (Sai lầm thường gặp)
*   **Trùng mã chuỗi**: Định nghĩa hai Enum khác nhau nhưng lại gán chung một mã code string (ví dụ: gán `"TEAM_NOT_FOUND"` cho cả `TEAM_NOT_FOUND` và `AGENT_NOT_FOUND`). Điều này khiến Client không thể phân biệt lỗi.
*   **Thay đổi mã chuỗi**: Thay đổi chuỗi code (ví dụ từ `"TEAM_ALREADY_EXISTS"` thành `"TEAM_DUPLICATED"`) ở các bản cập nhật sau. Điều này sẽ làm hỏng logic xử lý của các Client/Bot cũ đã được deploy từ trước (Break backward compatibility).

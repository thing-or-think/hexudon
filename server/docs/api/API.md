# Tài liệu chuẩn hóa API Máy chủ HEXUDON

Tài liệu tham khảo API chuẩn hóa cho máy chủ HEXUDON.

- **Phiên bản:** 0.1.0
- **Phiên bản OAS:** OpenAPI 3.1
- **OpenAPI File:** `./openapi.yaml`

---

# Cấu trúc Phản hồi Lỗi chung (Error Response)

Tất cả các lỗi hệ thống, xác thực hoặc nghiệp vụ đều trả về định dạng JSON thống nhất dưới đây:

```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Request body validation failed.",
  "timestamp": 1690000000000,
  "errors": [
    {
      "field": "teamId",
      "rejectedValue": "",
      "message": "teamId must not be blank"
    }
  ]
}
```

### Các trường dữ liệu:

| Trường | Kiểu | Mô tả |
| :--- | :--- | :--- |
| `errorCode` | `string` | Mã lỗi định danh (ví dụ: `VALIDATION_ERROR`, `UNAUTH_001`, `MATCH_NOT_PLAYING`) |
| `message` | `string` | Thông báo lỗi chi tiết bằng tiếng Anh |
| `timestamp` | `integer` | Thời điểm xảy ra lỗi (Unix timestamp dạng mili giây) |
| `errors` | `array<object> \| null` | Chi tiết lỗi Validation của từng trường (chỉ xuất hiện đối với lỗi xác thực) |

### Chi tiết ValidationErrorDetail:

| Trường | Kiểu | Mô tả |
| :--- | :--- | :--- |
| `field` | `string` | Tên trường dữ liệu lỗi |
| `rejectedValue` | `string \| null` | Giá trị bị từ chối |
| `message` | `string` | Thông báo lỗi chi tiết của trường dữ liệu đó |

---

# POST /api/game/generate

## Mục đích

Tạo bản xem trước bản đồ hợp lệ theo luật (Public / Admin).
Tạo ngẫu nhiên một sơ đồ bảng chơi hợp lệ có kết nối, quy định vị trí các cửa hàng mì Udon mà không cần tạo trận đấu thực tế.

## Request

### URL

`/api/game/generate`

### Headers

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `Content-Type` | `string` | Có | `application/json` |

### Request Body

```json
{
  "width": 12,
  "height": 12,
  "teams": 2
}
```

| Trường | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `width` | `integer` | Có | Chiều rộng bản đồ (từ 5 đến 50) |
| `height` | `integer` | Có | Chiều cao bản đồ (từ 5 đến 50) |
| `teams` | `integer` | Có | Số lượng đội tham gia (từ 2 đến 10) |

## Response

### Thành công

**HTTP 200 OK**

```json
{
  "width": 12,
  "height": 12,
  "cells": [
    [1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
    [0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
  ],
  "spots": [
    {
      "brand": 0,
      "pos": 15,
      "stocks": 4
    }
  ]
}
```

### Lỗi thường gặp

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 400 | `VALIDATION_ERROR` | Lỗi kiểm tra hợp lệ dữ liệu đầu vào |

---

# POST /api/game/init

## Mục đích

Khởi tạo Game (Public / Admin).
Khởi tạo cấu hình và thiết lập một game đấu mới trên hệ thống.

## Request

### URL

`/api/game/init`

### Headers

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `Content-Type` | `string` | Có | `application/json` |

### Request Body

```json
{
  "gameId": "game-123",
  "startsAt": 1778227200,
  "agentSelectionTimeLimit": 60.0,
  "daySeconds": [5.0, 5.0],
  "daySteps": [50, 100],
  "map": {
    "width": 8,
    "height": 8,
    "cells": [
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0]
    ],
    "spots": [
      {
        "brand": 0,
        "pos": 1,
        "stocks": 4
      }
    ]
  },
  "fuelLimits": 20,
  "players": 8,
  "busyThreshold": 2.0,
  "jammedThreshold": 4.0,
  "agents": [4, 12, 20]
}
```

| Trường | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `gameId` | `string` | Có | Mã định danh trận đấu (camelCase) |
| `startsAt` | `integer` | Có | Thời điểm bắt đầu trận đấu (Unix timestamp) |
| `agentSelectionTimeLimit` | `number` | Có | Giới hạn thời gian chọn loại Agent (giây) |
| `daySeconds` | `array<number>` | Có | Thời gian (giây) tối đa dành cho mỗi ngày |
| `daySteps` | `array<integer>` | Có | Số bước di chuyển tối đa trong mỗi ngày |
| `map` | `object` | Có | Bản đồ thi đấu (chứa `width`, `height`, `cells` và `spots`) |
| `fuelLimits` | `integer` | Có | Giới hạn dung lượng nhiên liệu của Agent |
| `players` | `integer` | Có | Số lượng Agent mỗi đội |
| `busyThreshold` | `number` | Có | Ngưỡng ô giao thông ùn tắc nhẹ |
| `jammedThreshold` | `number` | Có | Ngưỡng ô giao thông tắc nghẽn nặng |
| `agents` | `array<integer>` | Có | Danh sách ô vị trí ban đầu của các Agent |

## Response

### Thành công

**HTTP 204 No Content**

Không có Response Body.

### Lỗi thường gặp

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 400 | `VALIDATION_ERROR` | Lỗi kiểm tra hợp lệ dữ liệu đầu vào |

---

# DELETE /api/game/{gameId}

## Mục đích

Xóa game và tất cả các cấu hình liên quan (Public / Admin).

## Request

### URL

`/api/game/{gameId}`

### Path Parameters

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `gameId` | `string` | Có | Mã định danh trận đấu cần xóa |

### Request Body

Không có Request Body.

## Response

### Thành công

**HTTP 204 No Content**

### Lỗi thường gặp

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 400 | `VALIDATION_ERROR` | Lỗi tham số đường dẫn |

---

# POST /api/game/teams

## Mục đích

Đăng ký một đội mới và lấy JWT token tương ứng (Public / Admin).

## Request

### URL

`/api/game/teams`

### Request Body

```json
{
  "teamId": "team-1"
}
```

| Trường | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `teamId` | `string` | Có | Mã định danh của đội muốn thêm (camelCase) |

## Response

### Thành công

**HTTP 201 Created**

```json
{
  "teamId": "team-1",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

| Trường | Kiểu | Mô tả |
| :--- | :--- | :--- |
| `teamId` | `string` | ID của đội |
| `token` | `string` | JWT Token của đội dùng để đưa vào Authorization Header trong các yêu cầu của đội |

### Lỗi thường gặp

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 400 | `VALIDATION_ERROR` | Trường `teamId` trống |

---

# GET /api/game/board

## Mục đích

Đọc cấu hình bảng/trận đấu độc lập với thông tin đội (Public / Khán giả).

## Request

### URL

`/api/game/board`

### Query Parameters

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `game_id` | `string` | Có | Mã định danh trận đấu (snake_case) |

### Headers

Không yêu cầu Authorization Header.

## Response

### Thành công

**HTTP 200 OK**

```json
{
  "gameId": "game-123",
  "startsAt": 1784034064,
  "daySeconds": [60.0, 76.0],
  "daySteps": [37, 36],
  "map": {
    "height": 12,
    "width": 12,
    "cells": [
      [1, 0, 0],
      [0, 1, 0],
      [0, 0, 0]
    ]
  },
  "spots": [
    {
      "brand": 3,
      "pos": 28,
      "stocks": 3
    }
  ],
  "fuelLimits": 85,
  "players": 1,
  "busyThreshold": 2.0,
  "jammedThreshold": 5.0
}
```

### Lỗi thường gặp

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 400 | `RESOURCE_NOT_FOUND` | Không tìm thấy cấu hình trận đấu tương ứng |

---

# GET /api/game/list

## Mục đích

Đọc danh sách tất cả các cấu hình trận đấu hiện có trên hệ thống (Public).

## Request

### URL

`/api/game/list`

### Query Parameters

Không có Query Parameters.

### Headers

Không yêu cầu Authorization Header.

## Response

### Thành công

**HTTP 200 OK**

```json
{
  "total": 1,
  "games": [
    {
      "gameId": "game-123",
      "startsAt": 1778227200,
      "players": 8,
      "fuelLimits": 20,
      "agentSelectionTimeLimit": 60.0,
      "busyThreshold": 2.0,
      "jammedThreshold": 4.0,
      "map": {
        "height": 8,
        "width": 8,
        "cells": [
          [3, 0, 1, 2, 0, 1, 2, 0]
        ]
      },
      "totalDays": 2
    }
  ]
}
```

### Ý nghĩa của từng trường trong response:

#### GameListResponse (Phản hồi chính)
- `total` (`integer`): Tổng số lượng trận đấu có trên hệ thống.
- `games` (`array<GameSummaryResponse>`): Danh sách tóm tắt cấu hình các trận đấu.

#### GameSummaryResponse (Chi tiết từng trận đấu)
- `gameId` (`string`): Mã định danh trận đấu.
- `startsAt` (`integer`): Thời điểm bắt đầu trận đấu (Unix timestamp).
- `players` (`integer`): Số lượng Agent mỗi đội.
- `fuelLimits` (`integer`): Giới hạn dung lượng nhiên liệu của Agent.
- `agentSelectionTimeLimit` (`number`): Giới hạn thời gian chọn loại Agent (giây).
- `busyThreshold` (`number`): Ngưỡng mật độ xe gây ùn tắc nhẹ.
- `jammedThreshold` (`number`): Ngưỡng mật độ xe gây tắc nghẽn nặng.
- `map` (`MapResponse`): Bản đồ thi đấu của game đấu (chứa `height`, `width`, `cells`).
  - `height` (`integer`): Chiều cao bản đồ.
  - `width` (`integer`): Chiều rộng bản đồ.
  - `cells` (`array<array<integer>>`): Ma trận biểu diễn địa hình bản đồ.
- `totalDays` (`integer`): Tổng số ngày thi đấu của trận đấu.

### Lỗi thường gặp

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 500 | `INTERNAL_SERVER_ERROR` | Lỗi hệ thống không mong muốn khi đọc cấu hình |

---

# GET /api/game/config

## Mục đích

Cấu hình trận đấu dành riêng cho đội gọi yêu cầu (CHỈ ĐỘI).

## Request

### URL

`/api/game/config`

### Headers

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `Authorization` | `string` | Có | `Bearer <token>` của Đội |

### Query Parameters

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `game_id` | `string` | Có | Mã định danh trận đấu |

## Response

### Thành công

**HTTP 200 OK**

```json
{
  "startsAt": 1778227200,
  "daySeconds": [5.0, 5.0],
  "daySteps": [50, 100],
  "map": {
    "height": 8,
    "width": 8,
    "cells": [
      [3, 0, 1, 2, 0, 1, 2, 0]
    ]
  },
  "spots": [
    {
      "brand": 0,
      "pos": 1,
      "stocks": 4
    }
  ],
  "agents": [4, 12, 20],
  "fuelLimits": 20,
  "players": 8,
  "busyThreshold": 2.0,
  "jammedThreshold": 4.0
}
```

### Lỗi thường gặp

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 401 | `UNAUTH_001` | Thiếu hoặc sai Token xác thực |
| 400 | `RESOURCE_NOT_FOUND` | Không tìm thấy cấu hình trận đấu |

---

# POST /api/game/agent-types

## Mục đích

Chọn loại tuần tra/nạp nhiên liệu cho từng Agent của đội mình trước trận đấu (CHỈ ĐỘI).

## Request

### URL

`/api/game/agent-types`

### Headers

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `Content-Type` | `string` | Có | `application/json` |
| `Authorization` | `string` | Có | `Bearer <token>` |

### Request Body

```json
{
  "game_id": "game-123",
  "types": [0, 0, 1, 0]
}
```

| Trường | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `game_id` | `string` | Có | Mã định danh trận đấu (snake_case) |
| `types` | `array<integer>` | Có | Danh sách loại agent (0: patrol, 1: refuel) |

## Response

### Thành công

**HTTP 204 No Content**

### Lỗi thường gặp

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 401 | `UNAUTH_001` | Chưa xác thực |
| 400 | `RESOURCE_NOT_FOUND` | Không tìm thấy trận đấu |

---

# GET /api/game/day

## Mục đích

Đọc thông tin diễn biến của ngày hiện tại: vị trí Agent của mình, Agent của đối thủ, trạng thái giao thông và thời gian còn lại (CHỈ ĐỘI).

## Request

### URL

`/api/game/day`

### Headers

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `Authorization` | `string` | Có | `Bearer <token>` |

### Query Parameters

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `game_id` | `string` | Có | Mã định danh trận đấu |

## Response

### Thành công

**HTTP 200 OK**

```json
{
  "endsAt": 365.0,
  "day": 4,
  "agents": [
    {
      "kind": 0,
      "pos": 77,
      "fuel": 83
    },
    {
      "kind": 1,
      "pos": 4,
      "fuel": 0
    }
  ],
  "others": [
    {
      "id": "team-2",
      "agents": [
        {
          "kind": 0,
          "pos": 1,
          "fuel": 2
        }
      ]
    }
  ],
  "traffics": [
    {
      "pos": 0,
      "status": 0
    },
    {
      "pos": 13,
      "status": 1
    }
  ]
}
```

### Lỗi thường gặp

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 401 | `UNAUTH_001` | Chưa xác thực |
| 400 | `MATCH_NOT_PLAYING` | Trận đấu chưa bắt đầu hoặc đã kết thúc |

---

# GET /api/game/result

## Mục đích

Bảng xếp hạng chung cuộc và chi tiết điểm số của từng đội sau trận đấu (Public).

## Request

### URL

`/api/game/result`

### Query Parameters

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `game_id` | `string` | Có | Mã định danh trận đấu |

## Response

### Thành công

**HTTP 200 OK**

```json
{
  "ranking": [
    "team-1",
    "team-2"
  ],
  "detail": {
    "team-1": {
      "distinct_types": 2,
      "cumulative_daily_types": 8,
      "total_servings": 12,
      "cumulative_response_time": 0.0
    }
  }
}
```

### Lỗi thường gặp

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 400 | `RESOURCE_NOT_FOUND` | Không tìm thấy trận đấu |

---

# [Not Implemented] POST /api/game/reset

## Mục đích

Đặt lại game về lúc bắt đầu để có thể phát lại (CHỈ ADMIN).
**Hiện tại chưa được implement ở lớp Controller.**

---

# [Not Implemented] POST /api/game/actions

## Mục đích

Nộp kế hoạch hành động di chuyển của các Agent trong một ngày (CHỈ ĐỘI).
**Hiện tại chưa được implement ở lớp Controller.**

---

# [Not Implemented] GET /api/game/actions

## Mục đích

Xem lại danh sách lịch sử các kế hoạch hành động đã nộp của các ngày.
**Hiện tại chưa được implement ở lớp Controller.**

---

# [Not Implemented] GET /api/game/replay

## Mục đích

Tải dữ liệu phát lại trận đấu theo từng bước (frame).
**Hiện tại chưa được implement ở lớp Controller.**

---

# GET /api/game/state

## Mục đích

Đọc toàn bộ trạng thái trực tiếp của trận đấu bao gồm trạng thái các đội, vị trí Agent, kho hàng và tình trạng đường xá (Public).

## Request

### URL

`/api/game/state`

### Query Parameters

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `game_id` | `string` | Có | Mã định danh trận đấu |

## Response

### Thành công

**HTTP 200 OK**

```json
{
  "status": "PLAYING",
  "currentDay": 2,
  "remainingTime": 120,
  "mapStatus": [
    {
      "pos": 5,
      "status": 2
    }
  ],
  "teams": [
    {
      "teamId": "team-123",
      "score": {
        "distinct_types": 1,
        "cumulative_daily_types": 2,
        "total_servings": 3,
        "cumulative_response_time": 50.0
      },
      "agents": [
        {
          "kind": 0,
          "pos": 5,
          "fuel": 100
        }
      ]
    }
  ]
}
```

#### Ý nghĩa các trường dữ liệu trong Response:
- **`status`** (`string`): Trạng thái của trận đấu (`NOT_STARTED`, `REGISTERING`, `PLAYING`, `FINISHED`).
- **`currentDay`** (`integer`): Ngày thi đấu hiện tại (bắt đầu từ 0).
- **`remainingTime`** (`integer`): Thời gian còn lại của ngày hiện tại tính bằng giây (`dayEndTime - thời gian hiện tại`).
- **`mapStatus`** (`array`): Danh sách trạng thái mật độ giao thông trên toàn bản đồ. Mỗi phần tử gồm:
  - `pos` (`integer`): Vị trí ô đường trên bản đồ (chỉ số 1D).
  - `status` (`integer`): Cấp độ tắc nghẽn (`0`: NORMAL, `1`: BUSY, `2`: CONGESTED).
- **`teams`** (`array`): Danh sách trạng thái chi tiết của các đội tham gia. Mỗi đội gồm:
  - `teamId` (`string`): ID của đội.
  - `score` (`object`): Điểm số chi tiết của đội:
    - `distinct_types` (`integer`): Số loại Udon độc bản đã phục vụ thành công.
    - `cumulative_daily_types` (`integer`): Tổng số loại Udon độc bản tích lũy qua từng ngày.
    - `total_servings` (`integer`): Tổng số bát Udon đã phục vụ.
    - `cumulative_response_time` (`number`): Thời gian phản hồi trung bình tích lũy (ms).
  - `agents` (`array`): Danh sách trạng thái các Agent của đội:
    - `kind` (`integer`): Loại Agent (`0`: PATROL - Tuần tra, `1`: REFUEL - Nạp nhiên liệu).
    - `pos` (`integer`): Vị trí hiện tại của Agent trên bản đồ (chỉ số 1D).
    - `fuel` (`integer`): Mức nhiên liệu còn lại (chỉ có ý nghĩa với PatrolAgent; với RefuelAgent trường này luôn bằng `0`).

### Lỗi thường gặp

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 400 | `MISSING_REQUEST_ATTRIBUTE` | Thiếu tham số bắt buộc `game_id` |
| 404 | `RESOURCE_NOT_FOUND` | Không tìm thấy trận đấu có ID chỉ định |

---

# [Not Implemented] POST /api/game/practice/actions

## Mục đích

Nộp kế hoạch của một ngày trong trận đấu luyện tập tự chọn tốc độ.
**Hiện tại chưa được implement ở lớp Controller.**

---

# [Not Implemented] GET /api/game/practice/peer

## Mục đích

Đọc nội dung phát lại luyện tập của đội đối thủ để đối chiếu.
**Hiện tại chưa được implement ở lớp Controller.**

---

# [Not Implemented] POST /api/game/practice/copy

## Mục đích

Sao chép (fork) tiến trình luyện tập của đội khác đến một ngày chỉ định.
**Hiện tại chưa được implement ở lớp Controller.**

---

# [Not Implemented] POST /api/game/practice/reset

## Mục đích

Đặt lại game luyện tập của riêng đội.
**Hiện tại chưa được implement ở lớp Controller.**

# Tài liệu chuẩn hóa API Máy chủ HEXUDON

Tài liệu tham khảo API chuẩn hóa cho máy chủ HEXUDON.

- **Phiên bản:** 0.1.0
- **Phiên bản OAS:** OpenAPI 3.1
- **OpenAPI File:** `./openapi.yaml`

---

# POST /api/game/generate

## Mục đích

Tạo bản xem trước bản đồ hợp lệ theo luật (CHỈ ADMIN).

Bao bọc `MapGenerator` của `map_gen.py` để UI admin có thể yêu cầu một bảng đã được xác minh tính kết nối và tinh chỉnh độ khó mà không cần lặp lại thuật toán đó trong JS. Chỉ thuần túy tạo -- không tạo game; người gọi vẫn thực hiện POST kết quả (với một `game_id` thực tế và danh sách đội của chính trận đấu được thay thế vào) đến `/api/game/init` một cách riêng biệt.

## Request

### URL

`/api/game/generate`

### Headers

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `Content-Type` | `string` | Có | `application/json` |

### Query Parameters

Không có.

### Path Parameters

Không có.

### Request Body

```json
{
  "difficulty": "easy",
  "teams": 2,
  "seed": 0
}
```

| Trường | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `difficulty` | `string` | Có | Mức độ khó của bản đồ |
| `teams` | `integer` | Có | Số lượng đội tham gia (từ 2 đến 10) |
| `seed` | `integer \| null` | Không | Hạt giống sinh ngẫu nhiên |

## Response

### Thành công

**HTTP 200 OK**

```json
{
  "map": {
    "height": 8,
    "width": 8,
    "cells": [
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0]
    ]
  },
  "spots": [
    {
      "brand": 0,
      "pos": 1,
      "stocks": 4
    },
    {
      "brand": 1,
      "pos": 9,
      "stocks": 1
    }
  ]
}
```

### Lỗi

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 422 | `unprocessable_entity` | Lỗi kiểm tra hợp lệ dữ liệu đầu vào |

## Ví dụ

### Request

```json
{
  "difficulty": "easy",
  "teams": 2,
  "seed": 0
}
```

### Response

```json
{
  "map": {
    "height": 8,
    "width": 8,
    "cells": [
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0]
    ]
  },
  "spots": [
    {
      "brand": 0,
      "pos": 1,
      "stocks": 4
    },
    {
      "brand": 1,
      "pos": 9,
      "stocks": 1
    }
  ]
}
```

## Ghi chú

Endpoint này chỉ thuộc quyền hạn Quản trị viên (CHỈ ADMIN).

---

# POST /api/game/init

## Mục đích

Khởi tạo Game (CHỈ ADMIN).

## Request

### URL

`/api/game/init`

### Headers

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `Content-Type` | `string` | Có | `application/json` |

### Query Parameters

Không có.

### Path Parameters

Không có.

### Request Body

```json
{
  "game_id": "game-123",
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
  "fuelLimits": 20,
  "players": 8,
  "busyThreshold": 2,
  "jammedThreshold": 4,
  "teams": [
    {
      "team_id": "team-1",
      "agents": [4, 12, 20]
    }
  ],
  "agent_selection_time_limit": 60.0,
  "is_practice": false
}
```

| Trường | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `game_id` | `string` | Có | Mã định danh trận đấu |
| `startsAt` | `number` | Có | Thời điểm bắt đầu trận đấu (Unix timestamp) |
| `daySeconds` | `array<number>` | Có | Thời gian (giây) tối đa dành cho mỗi ngày |
| `daySteps` | `array<integer>` | Có | Số bước di chuyển tối đa trong mỗi ngày |
| `map` | `object` | Có | Đối tượng cấu hình ma trận bản đồ (`MapInit`) |
| `spots` | `array<object>` | Có | Danh sách ô cửa hàng/thương hiệu (`SpotInit`) |
| `fuelLimits` | `integer` | Có | Giới hạn dung lượng nhiên liệu |
| `players` | `integer` | Có | Số lượng Agent mỗi đội |
| `busyThreshold` | `integer` | Có | Ngưỡng ô giao thông ùn tắc nhẹ |
| `jammedThreshold` | `integer` | Có | Ngưỡng ô giao thông tắc nghẽn nặng |
| `teams` | `array<object>` | Có | Danh sách cấu hình các đội (`TeamInit`) |
| `agent_selection_time_limit` | `number` | Có | Giới hạn thời gian chọn loại Agent |
| `is_practice` | `boolean` | Có | Cờ xác định game luyện tập |

## Response

### Thành công

**HTTP 204 No Content**

Không có Response Body.

### Lỗi

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 422 | `unprocessable_entity` | Lỗi kiểm tra hợp lệ dữ liệu đầu vào |

## Ví dụ

### Request

```json
{
  "game_id": "game-123",
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
  "fuelLimits": 20,
  "players": 8,
  "busyThreshold": 2,
  "jammedThreshold": 4,
  "teams": [
    {
      "team_id": "team-1",
      "agents": [4, 12, 20]
    }
  ],
  "agent_selection_time_limit": 60.0,
  "is_practice": false
}
```

### Response

*(Không có dữ liệu trả về đối với HTTP 204)*

## Ghi chú

Dành riêng cho Quản trị viên (CHỈ ADMIN).

---

# POST /api/game/teams

## Mục đích

Thêm một đội vào game giữa trận đấu (CHỈ ADMIN).

## Request

### URL

`/api/game/teams`

### Headers

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `Content-Type` | `string` | Có | `application/json` |

### Query Parameters

Không có.

### Path Parameters

Không có.

### Request Body

```json
{
  "game_id": "game-123",
  "team_id": "team-2",
  "agents": [4, 12, 20]
}
```

| Trường | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `game_id` | `string` | Có | Mã định danh trận đấu |
| `team_id` | `string \| integer` | Có | Mã định danh đội |
| `agents` | `array<integer>` | Có | Danh sách ô vị trí ban đầu của Agent |

## Response

### Thành công

**HTTP 204 No Content**

Không có Response Body.

### Lỗi

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 422 | `unprocessable_entity` | Lỗi kiểm tra hợp lệ dữ liệu đầu vào |

## Ví dụ

### Request

```json
{
  "game_id": "game-123",
  "team_id": "team-2",
  "agents": [4, 12, 20]
}
```

### Response

*(Không có dữ liệu trả về)*

## Ghi chú

CHỈ ADMIN.

---

# POST /api/game/reset

## Mục đích

Đặt lại game về lúc bắt đầu để có thể phát lại (CHỈ ADMIN).

## Request

### URL

`/api/game/reset`

### Headers

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `Content-Type` | `string` | Có | `application/json` |

### Query Parameters

Không có.

### Path Parameters

Không có.

### Request Body

```json
{
  "game_id": "game-123"
}
```

| Trường | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `game_id` | `string` | Có | Mã định danh trận đấu |

## Response

### Thành công

**HTTP 204 No Content**

Không có Response Body.

### Lỗi

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 422 | `unprocessable_entity` | Lỗi kiểm tra hợp lệ dữ liệu đầu vào |

## Ví dụ

### Request

```json
{
  "game_id": "game-123"
}
```

### Response

*(Không có dữ liệu trả về)*

## Ghi chú

CHỈ ADMIN.

---

# DELETE /api/game/{game_id}

## Mục đích

Xóa game và tất cả các lượt nộp của nó (CHỈ ADMIN).

## Request

### URL

`/api/game/{game_id}`

### Headers

Không bắt buộc Header bổ sung.

### Query Parameters

Không có.

### Path Parameters

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `game_id` | `string` | Có | Mã định danh trận đấu cần xóa |

### Request Body

Không có Request Body.

## Response

### Thành công

**HTTP 204 No Content**

Không có Response Body.

### Lỗi

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 422 | `unprocessable_entity` | Lỗi kiểm tra hợp lệ dữ liệu đầu vào |

## Ví dụ

### Request

`DELETE /api/game/game-123`

### Response

*(Không có dữ liệu trả về)*

## Ghi chú

CHỈ ADMIN.

---

# GET /api/game/config

## Mục đích

Cấu hình bảng một lần cho đội gọi yêu cầu (CHỈ ĐỘI).

Cấu hình trận đấu một lần cho đội gọi yêu cầu -- khớp với cấu trúc "Map Configuration Format Before the Match Starts".

## Request

### URL

`/api/game/config`

### Headers

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `Authorization` | `string` | Có | `Bearer <token>` |

### Query Parameters

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `game_id` | `string` | Có | Mã định danh trận đấu |

### Path Parameters

Không có.

### Request Body

Không có Request Body.

## Response

### Thành công

**HTTP 200 OK**

```json
{
  "startsAt": 1778227200,
  "daySeconds": [5.0, 5.0, 5.0, 10.0],
  "daySteps": [50, 100, 150, 200],
  "map": {
    "height": 8,
    "width": 8,
    "cells": [
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0]
    ]
  },
  "spots": [
    {
      "brand": 0,
      "pos": 1,
      "stocks": 4
    },
    {
      "brand": 1,
      "pos": 9,
      "stocks": 1
    }
  ],
  "agents": [4, 12, 20, 28],
  "fuelLimits": 20,
  "players": 8,
  "busyThreshold": 2.0,
  "jammedThreshold": 4.0
}
```

### Lỗi

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 422 | `unprocessable_entity` | Lỗi kiểm tra hợp lệ dữ liệu đầu vào |

## Ví dụ

### Request

`GET /api/game/config?game_id=game-123`

### Response

```json
{
  "startsAt": 1778227200,
  "daySeconds": [5.0, 5.0, 5.0, 10.0],
  "daySteps": [50, 100, 150, 200],
  "map": {
    "height": 8,
    "width": 8,
    "cells": [
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0],
      [3, 0, 1, 2, 0, 1, 2, 0]
    ]
  },
  "spots": [
    {
      "brand": 0,
      "pos": 1,
      "stocks": 4
    },
    {
      "brand": 1,
      "pos": 9,
      "stocks": 1
    }
  ],
  "agents": [4, 12, 20, 28],
  "fuelLimits": 20,
  "players": 8,
  "busyThreshold": 2.0,
  "jammedThreshold": 4.0
}
```

## Ghi chú

Yêu cầu phân quyền Đội (CHỈ ĐỘI).

---

# GET /api/game/board

## Mục đích

Cấu hình bảng/trận đấu: bản đồ, spots, ngày, bước, nhiên liệu, ngưỡng (đội hoặc admin).

Cấu hình bảng độc lập với đội. Không giống như `/game/config`, điểm cuối này phục vụ cho cả admin/người xem.

## Request

### URL

`/api/game/board`

### Headers

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `Authorization` | `string` | Có | `Bearer <token>` |

### Query Parameters

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `game_id` | `string` | Có | Mã định danh trận đấu |

### Path Parameters

Không có.

### Request Body

Không có Request Body.

## Response

### Thành công

**HTTP 200 OK**

```json
{
  "game_id": "d2d87157-9158-484f-be37-814a0cf44524:21",
  "is_practice": true,
  "no_reset": false,
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
      "pos": 28,
      "brand": 3,
      "stocks": 3
    },
    {
      "pos": 8,
      "brand": 3,
      "stocks": 1
    }
  ],
  "fuelLimits": 85,
  "players": 1,
  "busyThreshold": 2,
  "jammedThreshold": 5,
  "agent_selection_time_limit": 45.0
}
```

### Lỗi

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 422 | `unprocessable_entity` | Lỗi kiểm tra hợp lệ dữ liệu đầu vào |

## Ví dụ

### Request

`GET /api/game/board?game_id=d2d87157-9158-484f-be37-814a0cf44524:21`

### Response

```json
{
  "game_id": "d2d87157-9158-484f-be37-814a0cf44524:21",
  "is_practice": true,
  "no_reset": false,
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
      "pos": 28,
      "brand": 3,
      "stocks": 3
    },
    {
      "pos": 8,
      "brand": 3,
      "stocks": 1
    }
  ],
  "fuelLimits": 85,
  "players": 1,
  "busyThreshold": 2,
  "jammedThreshold": 5,
  "agent_selection_time_limit": 45.0
}
```

## Ghi chú

Dùng cho Đội hoặc Admin.

---

# POST /api/game/agent-types

## Mục đích

Chọn loại tuần tra/nạp nhiên liệu của Agent trước trận đấu (CHỈ ĐỘI).

## Request

### URL

`/api/game/agent-types`

### Headers

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `Content-Type` | `string` | Có | `application/json` |
| `Authorization` | `string` | Có | `Bearer <token>` |

### Query Parameters

Không có.

### Path Parameters

Không có.

### Request Body

```json
{
  "game_id": "game-123",
  "types": [0, 0, 1, 0]
}
```

| Trường | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `game_id` | `string` | Có | Mã định danh trận đấu |
| `types` | `array<integer>` | Có | Danh sách loại agent (0: patrol, 1: refuel) |

## Response

### Thành công

**HTTP 204 No Content**

Không có Response Body.

### Lỗi

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 422 | `unprocessable_entity` | Lỗi kiểm tra hợp lệ dữ liệu đầu vào |

## Ví dụ

### Request

```json
{
  "game_id": "game-123",
  "types": [0, 0, 1, 0]
}
```

### Response

*(Không có dữ liệu trả về)*

## Ghi chú

Yêu cầu phân quyền Đội (CHỈ ĐỘI).

---

# POST /api/game/actions

## Mục đích

Nộp kế hoạch của một ngày (CHỈ ĐỘI).

## Request

### URL

`/api/game/actions`

### Headers

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `Content-Type` | `string` | Có | `application/json` |
| `Authorization` | `string` | Có | `Bearer <token>` |

### Query Parameters

Không có.

### Path Parameters

Không có.

### Request Body

```json
{
  "game_id": "game-123",
  "day": 0,
  "actions": [
    [5, 5, -33],
    [4, -35]
  ]
}
```

| Trường | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `game_id` | `string` | Có | Mã định danh trận đấu |
| `day` | `integer` | Có | Chỉ số ngày (0..N-1) |
| `actions` | `array<array<integer>>` | Có | Ma trận các bước di chuyển cho từng Agent |

## Response

### Thành công

**HTTP 204 No Content**

Không có Response Body.

### Lỗi

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 422 | `unprocessable_entity` | Lỗi kiểm tra hợp lệ dữ liệu đầu vào |

## Ví dụ

### Request

```json
{
  "game_id": "game-123",
  "day": 0,
  "actions": [
    [5, 5, -33],
    [4, -35]
  ]
}
```

### Response

*(Không có dữ liệu trả về)*

## Ghi chú

Yêu cầu phân quyền Đội (CHỈ ĐỘI).

---

# GET /api/game/day

## Mục đích

Thông tin ngày hiện tại: vị trí, nhiên liệu, lưu lượng giao thông, thời hạn (CHỈ ĐỘI).

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

### Path Parameters

Không có.

### Request Body

Không có Request Body.

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
      "id": 0,
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

### Lỗi

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 422 | `unprocessable_entity` | Lỗi kiểm tra hợp lệ dữ liệu đầu vào |

## Ví dụ

### Request

`GET /api/game/day?game_id=game-123`

### Response

```json
{
  "endsAt": 365.0,
  "day": 4,
  "agents": [
    {
      "kind": 0,
      "pos": 77,
      "fuel": 83
    }
  ],
  "others": [],
  "traffics": []
}
```

## Ghi chú

Yêu cầu phân quyền Đội (CHỈ ĐỘI).

---

# GET /api/game/actions

## Mục đích

Kế hoạch đã nộp mỗi ngày (huấn luyện/kiểm tra -- không thuộc bộ luật chính thức).

## Request

### URL

`/api/game/actions`

### Headers

Không yêu cầu bắt buộc.

### Query Parameters

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `game_id` | `string` | Có | Mã định danh trận đấu |

### Path Parameters

Không có.

### Request Body

Không có Request Body.

## Response

### Thành công

**HTTP 200 OK**

```json
[
  {
    "actions": [
      {
        "day": 0,
        "team_id": "21",
        "plan": [
          [5, 5, -33],
          [4, -35]
        ],
        "submitted_at": 1784386989.8280413,
        "submit_count": 1
      }
    ]
  }
]
```

### Lỗi

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 422 | `unprocessable_entity` | Lỗi kiểm tra hợp lệ dữ liệu đầu vào |

## Ví dụ

### Request

`GET /api/game/actions?game_id=game-123`

### Response

```json
[
  {
    "actions": [
      {
        "day": 0,
        "team_id": "21",
        "plan": [
          [5, 5, -33]
        ],
        "submitted_at": 1784386989.8280413,
        "submit_count": 1
      }
    ]
  }
]
```

## Ghi chú

Dùng để tra cứu lịch sử hành động đã nộp.

---

# GET /api/game/replay

## Mục đích

Phát lại từng bước của các ngày đã giải quyết (huấn luyện/kiểm tra -- không thuộc bộ luật chính thức).

## Request

### URL

`/api/game/replay`

### Headers

Không yêu cầu.

### Query Parameters

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `game_id` | `string` | Có | Mã định danh trận đấu |

### Path Parameters

Không có.

### Request Body

Không có Request Body.

## Response

### Thành công

**HTTP 200 OK**

```json
{
  "days": [
    {
      "day": 0,
      "total_steps": 37,
      "road_condition": {
        "0": 0,
        "13": 0
      },
      "team": {
        "team_id": "21",
        "kinds": [0, 0, 1, 0],
        "final_servings": 3,
        "final_types": 2,
        "submitted": true,
        "significant_frames": [
          {
            "step": 0,
            "agents": [
              {
                "cell": 79,
                "fuel": 85,
                "type": "patrol"
              }
            ],
            "collected": [],
            "servings": 0,
            "types": 0
          }
        ]
      }
    }
  ]
}
```

### Lỗi

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 422 | `unprocessable_entity` | Lỗi kiểm tra hợp lệ dữ liệu đầu vào |

## Ví dụ

### Request

`GET /api/game/replay?game_id=game-123`

### Response

```json
{
  "days": [
    {
      "day": 0,
      "total_steps": 37,
      "road_condition": {
        "0": 0
      },
      "team": {
        "team_id": "21",
        "kinds": [0, 0, 1, 0],
        "final_servings": 3,
        "final_types": 2,
        "submitted": true,
        "significant_frames": []
      }
    }
  ]
}
```

## Ghi chú

Dùng để xem lại diễn biến trận đấu từng frame.

---

# GET /api/game/state

## Mục đích

Toàn bộ trạng thái game: các đội, đường đi, ngày (đội hoặc admin).

## Request

### URL

`/api/game/state`

### Headers

Không yêu cầu.

### Query Parameters

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `game_id` | `string` | Có | Mã định danh trận đấu |

### Path Parameters

Không có.

### Request Body

Không có Request Body.

## Response

### Thành công

**HTTP 200 OK**

```json
{
  "status": "in_progress",
  "day": 4,
  "steps_today": 53,
  "day_deadline_in": 0.0,
  "road_condition": {
    "36": 0,
    "69": 0
  },
  "teams": {
    "21": {
      "types_selected": true,
      "agents": [
        {
          "agent_id": "0",
          "type": "patrol",
          "cell": 77,
          "fuel": 83
        }
      ],
      "stock": {
        "28": 3
      },
      "total_servings": 12,
      "distinct_types": [0, 2],
      "submit_count": 0,
      "last_submitted_at": null
    }
  }
}
```

### Lỗi

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 422 | `unprocessable_entity` | Lỗi kiểm tra hợp lệ dữ liệu đầu vào |

## Ví dụ

### Request

`GET /api/game/state?game_id=game-123`

### Response

```json
{
  "status": "in_progress",
  "day": 4,
  "steps_today": 53,
  "day_deadline_in": 0.0,
  "road_condition": {},
  "teams": {}
}
```

## Ghi chú

Đội hoặc Admin đều có thể gọi.

---

# GET /api/game/result

## Mục đích

Bảng xếp hạng và chi tiết điểm số của từng đội (đội hoặc admin).

## Request

### URL

`/api/game/result`

### Headers

Không yêu cầu.

### Query Parameters

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `game_id` | `string` | Có | Mã định danh trận đấu |

### Path Parameters

Không có.

### Request Body

Không có Request Body.

## Response

### Thành công

**HTTP 200 OK**

```json
{
  "ranking": [
    "21"
  ],
  "detail": {
    "21": {
      "distinct_types": 2,
      "cumulative_daily_types": 8,
      "total_servings": 12,
      "cumulative_response_time": 0.0
    }
  }
}
```

### Lỗi

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 422 | `unprocessable_entity` | Lỗi kiểm tra hợp lệ dữ liệu đầu vào |

## Ví dụ

### Request

`GET /api/game/result?game_id=game-123`

### Response

```json
{
  "ranking": [
    "21"
  ],
  "detail": {
    "21": {
      "distinct_types": 2,
      "cumulative_daily_types": 8,
      "total_servings": 12,
      "cumulative_response_time": 0.0
    }
  }
}
```

## Ghi chú

Dùng cho Đội hoặc Admin.

---

# POST /api/game/practice/actions

## Mục đích

Nộp một ngày trong game luyện tập tự chọn tốc độ.

## Request

### URL

`/api/game/practice/actions`

### Headers

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `Content-Type` | `string` | Có | `application/json` |

### Query Parameters

Không có.

### Path Parameters

Không có.

### Request Body

```json
{
  "game_id": "game-123",
  "day": 0,
  "actions": [
    [5, 5, -33]
  ]
}
```

| Trường | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `game_id` | `string` | Có | Mã định danh trận đấu luyện tập |
| `day` | `integer` | Có | Chỉ số ngày (0..N-1) |
| `actions` | `array<array<integer>>` | Có | Kế hoạch di chuyển các bước |

## Response

### Thành công

**HTTP 204 No Content**

Không có Response Body.

### Lỗi

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 422 | `unprocessable_entity` | Lỗi kiểm tra hợp lệ dữ liệu đầu vào |

## Ví dụ

### Request

```json
{
  "game_id": "game-123",
  "day": 0,
  "actions": [
    [5, 5, -33]
  ]
}
```

### Response

*(Không có dữ liệu trả về)*

## Ghi chú

Dành riêng cho Chế độ Luyện tập (Practice Mode).

---

# GET /api/game/practice/peer

## Mục đích

Đọc nội dung phát lại luyện tập của đội khác (để so sánh).

## Request

### URL

`/api/game/practice/peer`

### Headers

Không yêu cầu.

### Query Parameters

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `game_id` | `string` | Có | Mã định danh trận đấu luyện tập |

### Path Parameters

Không có.

### Request Body

Không có Request Body.

## Response

### Thành công

**HTTP 200 OK**

```json
{
  "game_id": "game-123",
  "history": [
    {
      "day": 0,
      "state": {
        "endsAt": 1778227300,
        "day": 0,
        "steps_today": 50,
        "road_condition": {
          "10": 1
        },
        "teams": {
          "team-1": {
            "agents": [
              {
                "agent_id": "agent-1",
                "pos": 4,
                "cell": 4,
                "fuel": 20,
                "type": "patrol",
                "kind": 0
              }
            ],
            "distinct_types": [
              "BrandA"
            ]
          }
        },
        "status": "finished"
      },
      "actions": {
        "team-1": [
          [0, 1, 2]
        ]
      }
    }
  ]
}
```

### Lỗi

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 422 | `unprocessable_entity` | Lỗi kiểm tra hợp lệ dữ liệu đầu vào |

## Ví dụ

### Request

`GET /api/game/practice/peer?game_id=game-123`

### Response

```json
{
  "game_id": "game-123",
  "history": []
}
```

## Ghi chú

Chỉ dùng trong Chế độ Luyện tập.

---

# POST /api/game/practice/copy

## Mục đích

Sao chép (fork) tiến trình luyện tập của đội khác cho đến một ngày.

## Request

### URL

`/api/game/practice/copy`

### Headers

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `Content-Type` | `string` | Có | `application/json` |

### Query Parameters

Không có.

### Path Parameters

Không có.

### Request Body

```json
{
  "game_id": "game-123",
  "from_game_id": "game-456",
  "from_team_id": "team-2",
  "upto_day": 0
}
```

| Trường | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `game_id` | `string` | Có | ID trận luyện tập hiện tại của bạn |
| `from_game_id` | `string` | Có | ID trận nguồn cần sao chép |
| `from_team_id` | `string \| integer` | Có | ID đội nguồn cần sao chép |
| `upto_day` | `integer` | Có | Ngày giới hạn cần copy |

## Response

### Thành công

**HTTP 204 No Content**

Không có Response Body.

### Lỗi

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 422 | `unprocessable_entity` | Lỗi kiểm tra hợp lệ dữ liệu đầu vào |

## Ví dụ

### Request

```json
{
  "game_id": "game-123",
  "from_game_id": "game-456",
  "from_team_id": "team-2",
  "upto_day": 0
}
```

### Response

*(Không có dữ liệu trả về)*

## Ghi chú

Chỉ dùng trong Chế độ Luyện tập.

---

# POST /api/game/practice/reset

## Mục đích

Đặt lại game luyện tập của riêng bạn để bạn có thể chơi lại.

## Request

### URL

`/api/game/practice/reset`

### Headers

| Tên | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `Content-Type` | `string` | Có | `application/json` |

### Query Parameters

Không có.

### Path Parameters

Không có.

### Request Body

```json
{
  "game_id": "game-123"
}
```

| Trường | Kiểu | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `game_id` | `string` | Có | Mã định danh trận luyện tập của bạn |

## Response

### Thành công

**HTTP 204 No Content**

Không có Response Body.

### Lỗi

| HTTP | Code | Ý nghĩa |
| :--- | :--- | :--- |
| 422 | `unprocessable_entity` | Lỗi kiểm tra hợp lệ dữ liệu đầu vào |

## Ví dụ

### Request

```json
{
  "game_id": "game-123"
}
```

### Response

*(Không có dữ liệu trả về)*

## Ghi chú

Chỉ dùng trong Chế độ Luyện tập.

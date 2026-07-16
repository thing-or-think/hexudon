# Tài liệu tham khảo API Máy chủ HEXUDON

- **Phiên bản:** 0.1.0
- **Phiên bản OAS:** 3.1
- **OpenAPI Schema:** `/openapi.json`

---

## Mục lục

- [Phân quyền](#authorize)
- [Admin](#admin)
  - [POST /api/game/generate](#post-apigamegenerate)
  - [POST /api/game/init](#post-apigameinit)
  - [POST /api/game/teams](#post-apigameteams)
  - [POST /api/game/reset](#post-apigamereset)
  - [DELETE /api/game/{game_id}](#delete-apigamegame_id)
- [Cấu hình](#config)
  - [GET /api/game/config](#get-apigameconfig)
  - [GET /api/game/board](#get-apigameboard)
- [Chơi](#play)
  - [POST /api/game/agent-types](#post-apigameagent-types)
  - [POST /api/game/actions](#post-apigameactions)
  - [GET /api/game/day](#get-apigameday)
- [Trạng thái & Lịch sử](#state--history)
  - [GET /api/game/actions](#get-apigameactions)
  - [GET /api/game/replay](#get-apigamereplay)
  - [GET /api/game/state](#get-apigamestate)
  - [GET /api/game/result](#get-apigameresult)
- [Luyện tập](#practice)
  - [POST /api/game/practice/actions](#post-apigamepracticeactions)
  - [GET /api/game/practice/peer](#get-apigamepracticepeer)
  - [POST /api/game/practice/copy](#post-apigamepracticecopy)
  - [POST /api/game/practice/reset](#post-apigamepracticereset)
- [Schema](#schemas)
  - [AddTeamRequest](#addteamrequest)
  - [GameInitRequest](#gameinitrequest)
  - [GameRequest](#gamerequest)
  - [GenerateMapRequest](#generatemaprequest)
  - [HTTPValidationError](#httpvalidationerror)
  - [MapInit](#mapinit)
  - [PracticeCopyRequest](#practicecopyrequest)
  - [PracticeSubmitRequest](#practicesubmitrequest)
  - [SelectAgentTypesRequest](#selectagenttypesrequest)
  - [SpotInit](#spotinit)
  - [SubmitActionsRequest](#submitactionsrequest)
  - [TeamInit](#teaminit)
  - [ValidationError](#validationerror)

---

## Phân quyền

Yêu cầu phân quyền đối với các Endpoint được đánh dấu là bị hạn chế (`CHỈ ADMIN` hoặc `CHỈ ĐỘI`).

---

## Admin

Thiết lập & vòng đời trận đấu (chỉ admin): khởi tạo game, tạo bản đồ, thêm đội, reset về trạng thái ban đầu, xóa.

### `POST /api/game/generate`

**Mô tả:**
Tạo bản xem trước bản đồ hợp lệ theo luật (CHỈ ADMIN).

Bao bọc `MapGenerator` của `map_gen.py` để UI admin có thể yêu cầu một bảng đã được xác minh tính kết nối và tinh chỉnh độ khó mà không cần lặp lại thuật toán đó trong JS. Chỉ thuần túy tạo -- không tạo game; người gọi vẫn thực hiện POST kết quả (với một `game_id` thực tế và danh sách đội của chính trận đấu được thay thế vào) đến `/game/init` một cách riêng biệt.

**URL:** `/api/game/generate`
**Method:** `POST`

**Header:**
- `Content-Type: application/json`

**Yêu cầu:**
- **Tham số:** Không có
- **Nội dung** (`application/json`):
  ```json
  {
    "difficulty": "string",
    "teams": 2,
    "seed": 0
  }
  ```

**Phản hồi:**
- **200 (Phản hồi thành công)**
  - Nội dung (`application/json`):
    ```json
    "string"
    ```

**Lỗi:**
- **422 (Lỗi kiểm tra hợp lệ)**
  - Nội dung (`application/json`):
    ```json
    {
      "detail": [
        {
          "loc": [
            "string",
            0
          ],
          "msg": "string",
          "type": "string",
          "input": "string",
          "ctx": {}
        }
      ]
    }
    ```

---

### `POST /api/game/init`

**Mô tả:**
Khởi tạo Game (CHỈ ADMIN).

**URL:** `/api/game/init`
**Method:** `POST`

**Header:**
- `Content-Type: application/json`

**Yêu cầu:**
- **Tham số:** Không có
- **Nội dung** (`application/json`):
  ```json
  {
    "game_id": "string",
    "startsAt": 0,
    "daySeconds": [
      0
    ],
    "daySteps": [
      0
    ],
    "map": {
      "height": 0,
      "width": 0,
      "cells": [
        [
          0
        ]
      ]
    },
    "spots": [],
    "fuelLimits": 1,
    "players": 1,
    "busyThreshold": 0,
    "jammedThreshold": 0,
    "teams": [
      {
        "team_id": "string",
        "agents": [
          0,
          0,
          0
        ]
      }
    ],
    "agent_selection_time_limit": 60,
    "is_practice": false
  }
  ```

**Phản hồi:**
- **200 (Phản hồi thành công)**
  - Nội dung (`application/json`):
    ```json
    "string"
    ```

**Lỗi:**
- **422 (Lỗi kiểm tra hợp lệ)**
  - Nội dung (`application/json`):
    ```json
    {
      "detail": [
        {
          "loc": [
            "string",
            0
          ],
          "msg": "string",
          "type": "string",
          "input": "string",
          "ctx": {}
        }
      ]
    }
    ```

---

### `POST /api/game/teams`

**Mô tả:**
Thêm một đội vào game giữa trận đấu (CHỈ ADMIN).

**URL:** `/api/game/teams`
**Method:** `POST`

**Header:**
- `Content-Type: application/json`

**Yêu cầu:**
- **Tham số:** Không có
- **Nội dung** (`application/json`):
  ```json
  {
    "game_id": "string",
    "team_id": "string",
    "agents": [
      0,
      0,
      0
    ]
  }
  ```

**Phản hồi:**
- **200 (Phản hồi thành công)**
  - Nội dung (`application/json`):
    ```json
    "string"
    ```

**Lỗi:**
- **422 (Lỗi kiểm tra hợp lệ)**
  - Nội dung (`application/json`):
    ```json
    {
      "detail": [
        {
          "loc": [
            "string",
            0
          ],
          "msg": "string",
          "type": "string",
          "input": "string",
          "ctx": {}
        }
      ]
    }
    ```

---

### `POST /api/game/reset`

**Mô tả:**
Đặt lại game về lúc bắt đầu để có thể phát lại (CHỈ ADMIN).

**URL:** `/api/game/reset`
**Method:** `POST`

**Header:**
- `Content-Type: application/json`

**Yêu cầu:**
- **Tham số:** Không có
- **Nội dung** (`application/json`):
  ```json
  {
    "game_id": "string"
  }
  ```

**Phản hồi:**
- **200 (Phản hồi thành công)**
  - Nội dung (`application/json`):
    ```json
    "string"
    ```

**Lỗi:**
- **422 (Lỗi kiểm tra hợp lệ)**
  - Nội dung (`application/json`):
    ```json
    {
      "detail": [
        {
          "loc": [
            "string",
            0
          ],
          "msg": "string",
          "type": "string",
          "input": "string",
          "ctx": {}
        }
      ]
    }
    ```

---

### `DELETE /api/game/{game_id}`

**Mô tả:**
Xóa game và tất cả các lượt nộp của nó (CHỈ ADMIN).

**URL:** `/api/game/{game_id}`
**Method:** `DELETE`

**Yêu cầu:**
- **Tham số:**

| Tên | Kiểu | Vị trí | Mô tả |
| :---------- | :------- | :------- | :---------- |
| `game_id` * | `string` | path     | game_id     |

**Phản hồi:**
- **200 (Phản hồi thành công)**
  - Nội dung (`application/json`):
    ```json
    "string"
    ```

**Lỗi:**
- **422 (Lỗi kiểm tra hợp lệ)**
  - Nội dung (`application/json`):
    ```json
    {
      "detail": [
        {
          "loc": [
            "string",
            0
          ],
          "msg": "string",
          "type": "string",
          "input": "string",
          "ctx": {}
        }
      ]
    }
    ```

---

## Cấu hình

Cấu hình bảng / trận đấu mà máy khách đọc trước khi chơi.

### `GET /api/game/config`

**Mô tả:**
Cấu hình bảng một lần cho đội gọi yêu cầu (CHỈ ĐỘI).

Cấu hình trận đấu một lần cho đội gọi yêu cầu -- khớp với cấu trúc "Map Configuration Format Before the Match Starts" từ tài liệu kỹ thuật (spec).

**URL:** `/api/game/config`
**Method:** `GET`

**Yêu cầu:**
- **Tham số:**

| Tên | Kiểu | Vị trí | Mô tả |
| :---------- | :------- | :------- | :---------- |
| `game_id` * | `string` | query    | game_id     |

**Phản hồi:**
- **200 (Phản hồi thành công)**
  - Nội dung (`application/json`):
    ```json
    "string"
    ```

**Lỗi:**
- **422 (Lỗi kiểm tra hợp lệ)**
  - Nội dung (`application/json`):
    ```json
    {
      "detail": [
        {
          "loc": [
            "string",
            0
          ],
          "msg": "string",
          "type": "string",
          "input": "string",
          "ctx": {}
        }
      ]
    }
    ```

---

### `GET /api/game/board`

**Mô tả:**
Cấu hình bảng/trận đấu: bản đồ, spots, ngày, bước, nhiên liệu, ngưỡng (đội hoặc admin).

Cấu hình bảng độc lập với đội (bản đồ, spots, ngày, bước, nhiên liệu, ngưỡng). Không giống như `/game/config` (dành riêng cho đội), điểm cuối này cũng phục vụ cho admin/người xem.

> [!NOTE]
> **Các trận đấu luyện tập:** KHÔNG có game nào tại ID câu hỏi trần (bare question id) -- mỗi đội chơi game riêng của mình `"{game_id}:{team_id}"`, tất cả đều chia sẻ cùng một bảng. Với tư cách là một đội, chỉ cần truyền ID câu hỏi trần: ID đội của bạn được lấy từ bearer token và game sẽ tự động được giải quyết. Admin (không có ID đội trong token) phải truyền composite ID một cách rõ ràng, ví dụ: `1a5de476-28c5-4498-8708-e1e7efc20e3f:5`. Các trận đấu thông thường sử dụng ID trần.

**URL:** `/api/game/board`
**Method:** `GET`

**Yêu cầu:**
- **Tham số:**

| Tên | Kiểu | Vị trí | Mô tả |
| :---------- | :------- | :------- | :---------- |
| `game_id` * | `string` | query    | game_id     |

**Phản hồi:**
- **200 (Phản hồi thành công)**
  - Nội dung (`application/json`):
    ```json
    "string"
    ```

**Lỗi:**
- **422 (Lỗi kiểm tra hợp lệ)**
  - Nội dung (`application/json`):
    ```json
    {
      "detail": [
        {
          "loc": [
            "string",
            0
          ],
          "msg": "string",
          "type": "string",
          "input": "string",
          "ctx": {}
        }
      ]
    }
    ```

---

## Chơi

Lối chơi của đội: chọn loại Agent, nộp kế hoạch của mỗi ngày, và đọc thông tin của ngày hiện tại.

### `POST /api/game/agent-types`

**Mô tả:**
Chọn loại tuần tra/nạp nhiên liệu của Agent trước trận đấu (CHỈ ĐỘI).

**URL:** `/api/game/agent-types`
**Method:** `POST`

**Header:**
- `Content-Type: application/json`

**Yêu cầu:**
- **Tham số:** Không có
- **Nội dung** (`application/json`):
  ```json
  {
    "game_id": "string",
    "types": [
      0
    ]
  }
  ```

**Phản hồi:**
- **200 (Phản hồi thành công)**
  - Nội dung (`application/json`):
    ```json
    "string"
    ```

**Lỗi:**
- **422 (Lỗi kiểm tra hợp lệ)**
  - Nội dung (`application/json`):
    ```json
    {
      "detail": [
        {
          "loc": [
            "string",
            0
          ],
          "msg": "string",
          "type": "string",
          "input": "string",
          "ctx": {}
        }
      ]
    }
    ```

---

### `POST /api/game/actions`

**Mô tả:**
Nộp kế hoạch của một ngày (CHỈ ĐỘI).

**URL:** `/api/game/actions`
**Method:** `POST`

**Header:**
- `Content-Type: application/json`

**Yêu cầu:**
- **Tham số:** Không có
- **Nội dung** (`application/json`):
  ```json
  {
    "game_id": "string",
    "day": 0,
    "actions": [
      [
        0
      ]
    ]
  }
  ```

**Phản hồi:**
- **200 (Phản hồi thành công)**
  - Nội dung (`application/json`):
    ```json
    "string"
    ```

**Lỗi:**
- **422 (Lỗi kiểm tra hợp lệ)**
  - Nội dung (`application/json`):
    ```json
    {
      "detail": [
        {
          "loc": [
            "string",
            0
          ],
          "msg": "string",
          "type": "string",
          "input": "string",
          "ctx": {}
        }
      ]
    }
    ```

---

### `GET /api/game/day`

**Mô tả:**
Thông tin ngày hiện tại: vị trí, nhiên liệu, lưu lượng giao thông, thời hạn (CHỈ ĐỘI).

**URL:** `/api/game/day`
**Method:** `GET`

**Yêu cầu:**
- **Tham số:**

| Tên | Kiểu | Vị trí | Mô tả |
| :---------- | :------- | :------- | :---------- |
| `game_id` * | `string` | query    | game_id     |

**Phản hồi:**
- **200 (Phản hồi thành công)**
  - Nội dung (`application/json`):
    ```json
    "string"
    ```

**Lỗi:**
- **422 (Lỗi kiểm tra hợp lệ)**
  - Nội dung (`application/json`):
    ```json
    {
      "detail": [
        {
          "loc": [
            "string",
            0
          ],
          "msg": "string",
          "type": "string",
          "input": "string",
          "ctx": {}
        }
      ]
    }
    ```

---

## Trạng thái & Lịch sử

Trạng thái trực tiếp & bảng xếp hạng, cộng với lịch sử kế hoạch đã nộp và phát lại từng bước (kiểm tra/huấn luyện).

### `GET /api/game/actions`

**Mô tả:**
Kế hoạch đã nộp mỗi ngày (huấn luyện/kiểm tra -- không thuộc bộ luật chính thức).

**URL:** `/api/game/actions`
**Method:** `GET`

**Yêu cầu:**
- **Tham số:**

| Tên | Kiểu | Vị trí | Mô tả |
| :---------- | :------- | :------- | :---------- |
| `game_id` * | `string` | query    | game_id     |

**Phản hồi:**
- **200 (Phản hồi thành công)**
  - Nội dung (`application/json`):
    ```json
    "string"
    ```

**Lỗi:**
- **422 (Lỗi kiểm tra hợp lệ)**
  - Nội dung (`application/json`):
    ```json
    {
      "detail": [
        {
          "loc": [
            "string",
            0
          ],
          "msg": "string",
          "type": "string",
          "input": "string",
          "ctx": {}
        }
      ]
    }
    ```

---

### `GET /api/game/replay`

**Mô tả:**
Phát lại từng bước của các ngày đã giải quyết (huấn luyện/kiểm tra -- không thuộc bộ luật chính thức).

**URL:** `/api/game/replay`
**Method:** `GET`

**Yêu cầu:**
- **Tham số:**

| Tên | Kiểu | Vị trí | Mô tả |
| :---------- | :------- | :------- | :---------- |
| `game_id` * | `string` | query    | game_id     |

**Phản hồi:**
- **200 (Phản hồi thành công)**
  - Nội dung (`application/json`):
    ```json
    "string"
    ```

**Lỗi:**
- **422 (Lỗi kiểm tra hợp lệ)**
  - Nội dung (`application/json`):
    ```json
    {
      "detail": [
        {
          "loc": [
            "string",
            0
          ],
          "msg": "string",
          "type": "string",
          "input": "string",
          "ctx": {}
        }
      ]
    }
    ```

---

### `GET /api/game/state`

**Mô tả:**
Toàn bộ trạng thái game: các đội, đường đi, ngày (đội hoặc admin).

**URL:** `/api/game/state`
**Method:** `GET`

**Yêu cầu:**
- **Tham số:**

| Tên | Kiểu | Vị trí | Mô tả |
| :---------- | :------- | :------- | :---------- |
| `game_id` * | `string` | query    | game_id     |

**Phản hồi:**
- **200 (Phản hồi thành công)**
  - Nội dung (`application/json`):
    ```json
    "string"
    ```

**Lỗi:**
- **422 (Lỗi kiểm tra hợp lệ)**
  - Nội dung (`application/json`):
    ```json
    {
      "detail": [
        {
          "loc": [
            "string",
            0
          ],
          "msg": "string",
          "type": "string",
          "input": "string",
          "ctx": {}
        }
      ]
    }
    ```

---

### `GET /api/game/result`

**Mô tả:**
Bảng xếp hạng và chi tiết điểm số của từng đội (đội hoặc admin).

**URL:** `/api/game/result`
**Method:** `GET`

**Yêu cầu:**
- **Tham số:**

| Tên | Kiểu | Vị trí | Mô tả |
| :---------- | :------- | :------- | :---------- |
| `game_id` * | `string` | query    | game_id     |

**Phản hồi:**
- **200 (Phản hồi thành công)**
  - Nội dung (`application/json`):
    ```json
    "string"
    ```

**Lỗi:**
- **422 (Lỗi kiểm tra hợp lệ)**
  - Nội dung (`application/json`):
    ```json
    {
      "detail": [
        {
          "loc": [
            "string",
            0
          ],
          "msg": "string",
          "type": "string",
          "input": "string",
          "ctx": {}
        }
      ]
    }
    ```

---

## Luyện tập

Game luyện tập tự chọn tốc độ cho từng đội: nộp một ngày, xem trước / sao chép (fork) tiến trình của đội khác, đặt lại.

### `POST /api/game/practice/actions`

**Mô tả:**
Nộp một ngày trong game luyện tập tự chọn tốc độ.

**URL:** `/api/game/practice/actions`
**Method:** `POST`

**Header:**
- `Content-Type: application/json`

**Yêu cầu:**
- **Tham số:** Không có
- **Nội dung** (`application/json`):
  ```json
  {
    "game_id": "string",
    "day": 0,
    "actions": [
      [
        0
      ]
    ]
  }
  ```

**Phản hồi:**
- **200 (Phản hồi thành công)**
  - Nội dung (`application/json`):
    ```json
    "string"
    ```

**Lỗi:**
- **422 (Lỗi kiểm tra hợp lệ)**
  - Nội dung (`application/json`):
    ```json
    {
      "detail": [
        {
          "loc": [
            "string",
            0
          ],
          "msg": "string",
          "type": "string",
          "input": "string",
          "ctx": {}
        }
      ]
    }
    ```

---

### `GET /api/game/practice/peer`

**Mô tả:**
Đọc nội dung phát lại luyện tập của đội khác (để so sánh).

**URL:** `/api/game/practice/peer`
**Method:** `GET`

**Yêu cầu:**
- **Tham số:**

| Tên | Kiểu | Vị trí | Mô tả |
| :---------- | :------- | :------- | :---------- |
| `game_id` * | `string` | query    | game_id     |

**Phản hồi:**
- **200 (Phản hồi thành công)**
  - Nội dung (`application/json`):
    ```json
    "string"
    ```

**Lỗi:**
- **422 (Lỗi kiểm tra hợp lệ)**
  - Nội dung (`application/json`):
    ```json
    {
      "detail": [
        {
          "loc": [
            "string",
            0
          ],
          "msg": "string",
          "type": "string",
          "input": "string",
          "ctx": {}
        }
      ]
    }
    ```

---

### `POST /api/game/practice/copy`

**Mô tả:**
Sao chép (fork) tiến trình luyện tập của đội khác cho đến một ngày.

**URL:** `/api/game/practice/copy`
**Method:** `POST`

**Header:**
- `Content-Type: application/json`

**Yêu cầu:**
- **Tham số:** Không có
- **Nội dung** (`application/json`):
  ```json
  {
    "game_id": "string",
    "from_game_id": "string",
    "from_team_id": "string",
    "upto_day": 0
  }
  ```

**Phản hồi:**
- **200 (Phản hồi thành công)**
  - Nội dung (`application/json`):
    ```json
    "string"
    ```

**Lỗi:**
- **422 (Lỗi kiểm tra hợp lệ)**
  - Nội dung (`application/json`):
    ```json
    {
      "detail": [
        {
          "loc": [
            "string",
            0
          ],
          "msg": "string",
          "type": "string",
          "input": "string",
          "ctx": {}
        }
      ]
    }
    ```

---

### `POST /api/game/practice/reset`

**Mô tả:**
Đặt lại game luyện tập của riêng bạn để bạn có thể chơi lại.

**URL:** `/api/game/practice/reset`
**Method:** `POST`

**Header:**
- `Content-Type: application/json`

**Yêu cầu:**
- **Tham số:** Không có
- **Nội dung** (`application/json`):
  ```json
  {
    "game_id": "string"
  }
  ```

**Phản hồi:**
- **200 (Phản hồi thành công)**
  - Nội dung (`application/json`):
    ```json
    "string"
    ```

**Lỗi:**
- **422 (Lỗi kiểm tra hợp lệ)**
  - Nội dung (`application/json`):
    ```json
    {
      "detail": [
        {
          "loc": [
            "string",
            0
          ],
          "msg": "string",
          "type": "string",
          "input": "string",
          "ctx": {}
        }
      ]
    }
    ```

---

## Schema

### AddTeamRequest

- **Kiểu:** `object`
- **Các trường:**

| Trường | Kiểu | Mô tả / Ràng buộc |
| :-------- | :------------------ | :------------------------ |
| `game_id` | `string`            |                           |
| `team_id` | `string \| integer` |                           |
| `agents`  | `array<integer>`    | `[3, 8] items`            |

### GameInitRequest

- **Kiểu:** `object`
- **Các trường:**

| Trường | Kiểu | Mô tả / Ràng buộc |
| :--------------------------- | :--------------- | :------------------------ |
| `game_id`                    | `string`         |                           |
| `startsAt`                   | `number`         |                           |
| `daySeconds`                 | `array<number>`  | `≥ 1 items`               |
| `daySteps`                   | `array<integer>` | `≥ 1 items`               |
| `map`                        | `object`         |                           |
| `spots`                      | `array<object>`  |                           |
| `fuelLimits`                 | `integer`        | `> 0`                     |
| `players`                    | `integer`        | `≥ 1`                     |
| `busyThreshold`              | `integer`        |                           |
| `jammedThreshold`            | `integer`        |                           |
| `teams`                      | `array<object>`  | `≥ 1 items`               |
| `agent_selection_time_limit` | `number`         |                           |
| `is_practice`                | `boolean`        |                           |

### GameRequest

- **Kiểu:** `object`
- **Các trường:**

| Trường | Kiểu | Mô tả / Ràng buộc |
| :-------- | :------- | :------------------------ |
| `game_id` | `string` |                           |

### GenerateMapRequest

- **Kiểu:** `object`
- **Các trường:**

| Trường | Kiểu | Mô tả / Ràng buộc |
| :----------- | :---------------- | :------------------------ |
| `difficulty` | `string`          |                           |
| `teams`      | `integer`         | `[2, 10]`                 |
| `seed`       | `integer \| null` |                           |

### HTTPValidationError

- **Kiểu:** `object`
- **Các trường:**

| Trường | Kiểu | Mô tả / Ràng buộc |
| :------- | :-------------- | :------------------------ |
| `detail` | `array<object>` |                           |

### MapInit

- **Kiểu:** `object`
- **Các trường:**

| Trường | Kiểu | Mô tả / Ràng buộc |
| :------- | :---------------------- | :------------------------ |
| `height` | `integer`               |                           |
| `width`  | `integer`               |                           |
| `cells`  | `array<array<integer>>` |                           |

### PracticeCopyRequest

- **Kiểu:** `object`
- **Các trường:**

| Trường | Kiểu | Mô tả / Ràng buộc |
| :------------- | :------------------ | :------------------------ |
| `game_id`      | `string`            |                           |
| `from_game_id` | `string`            |                           |
| `from_team_id` | `string \| integer` |                           |
| `upto_day`     | `integer`           |                           |

### PracticeSubmitRequest

- **Kiểu:** `object`
- **Các trường:**

| Trường | Kiểu | Mô tả / Ràng buộc |
| :-------- | :---------------------- | :------------------------ |
| `game_id` | `string`                |                           |
| `day`     | `integer`               |                           |
| `actions` | `array<array<integer>>` |                           |

### SelectAgentTypesRequest

- **Kiểu:** `object`
- **Các trường:**

| Trường | Kiểu | Mô tả / Ràng buộc |
| :-------- | :--------------- | :------------------------ |
| `game_id` | `string`         |                           |
| `types`   | `array<integer>` |                           |

### SpotInit

- **Kiểu:** `object`
- **Các trường:**

| Trường | Kiểu | Mô tả / Ràng buộc |
| :------- | :-------- | :------------------------ |
| `brand`  | `integer` |                           |
| `pos`    | `integer` |                           |
| `stocks` | `integer` | `≥ 1`                     |

### SubmitActionsRequest

- **Kiểu:** `object`
- **Các trường:**

| Trường | Kiểu | Mô tả / Ràng buộc |
| :-------- | :---------------------- | :------------------------ |
| `game_id` | `string`                |                           |
| `day`     | `integer`               |                           |
| `actions` | `array<array<integer>>` |                           |

### TeamInit

- **Kiểu:** `object`
- **Các trường:**

| Trường | Kiểu | Mô tả / Ràng buộc |
| :-------- | :------------------ | :------------------------ |
| `team_id` | `string \| integer` |                           |
| `agents`  | `array<integer>`    | `[3, 8] items`            |

### ValidationError

- **Kiểu:** `object`
- **Các trường:**

| Trường | Kiểu | Mô tả / Ràng buộc |
| :------ | :-------------------------- | :------------------------ |
| `loc`   | `array<string \| integer>`  |                           |
| `msg`   | `string`                    |                           |
| `type`  | `string`                    |                           |
| `input` | `any`                       |                           |
| `ctx`   | `object`                    |                           |
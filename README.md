# hexudon-server

`hexudon-server` là một ứng dụng máy chủ backend viết bằng Spring Boot triển khai luật chơi và cơ chế mô phỏng cho trò chơi đấu trí theo lượt "Hexudon". Trò chơi được diễn ra trên một bản đồ lưới lục giác (Odd-R offset grid), nơi các đội chơi đăng ký đội và lập trình điều khiển các tác tử (Agent) đi thu hoạch các loại mì Udon và hỗ trợ tiếp nhiên liệu cho nhau để đạt điểm số cao nhất trên bảng xếp hạng.

Dự án được xây dựng theo mô hình phát triển hướng domain (**Domain-Driven Design - DDD**) kết hợp kiến trúc lục giác (**Hexagonal Architecture / Ports and Adapters**), giúp tách biệt hoàn toàn phần lõi nghiệp vụ trò chơi khỏi các công nghệ và framework bên ngoài.

---

## Overview

Dự án này giải quyết bài toán mô phỏng một trận đấu đa tác tử, đa quốc gia/đội chơi cạnh tranh trên bản đồ lưới lục giác. Cụ thể, hệ thống cung cấp các chức năng chính hiện có sau:

*   **Tự động khởi tạo trận đấu và bản đồ (Map Generation):** Sinh ngẫu nhiên lưới lục giác kích thước rộng/cao cấu hình được. Bản đồ chứa các ô địa hình (Đất trống, Núi, Đường đi, Ao nước) và bố trí ngẫu nhiên các điểm chứa mì Udon (Spots) cách nhau một khoảng cách tối thiểu đảm bảo tính phân tán.
*   **Đăng ký Đội chơi & Sinh Tác tử (Team & Agent Registration):** Hỗ trợ tối đa một số lượng đội chơi đăng ký. Khi đăng ký, hệ thống tự động sinh ngẫu nhiên vị trí xuất phát cho hai loại Agent của mỗi đội:
    *   **PatrolAgent (Agent tuần tra):** Có nhiệm vụ di chuyển trên bản đồ và thu thập các gói mì Udon tại các Spot. Di chuyển của PatrolAgent tiêu tốn nhiên liệu và điểm bước đi.
    *   **RefuelAgent (Agent tiếp nhiên liệu):** Di chuyển trên bản đồ và hỗ trợ nạp đầy nhiên liệu cho PatrolAgent đứng chung ô tọa độ.
*   **Thẩm định hành động (Action Validation):** Kiểm tra tính hợp lệ của chuỗi hành động di chuyển/chờ do đội chơi gửi lên thông qua giả lập bước chạy trước khi cập nhật chính thức.
*   **Quản lý lưu lượng giao thông (Traffic Tracker):** Mô phỏng tình trạng kẹt xe trên các ô đường đi (ROAD). Khi có nhiều Agent của các đội cùng đi qua hoặc đứng tại một ô đường đi, mức độ ùn tắc tăng lên (NORMAL -> BUSY -> CONGESTED) làm tăng chi phí di chuyển (bước đi) qua ô đó ở lượt kế tiếp.
*   **Bảng điểm & Xếp hạng (ScoreBoard):** Theo dõi số lượng mì Udon thu thập được của từng đội theo từng lượt chơi dựa trên các loại mì (TANUKI, KITSUNE, TEMPURA, BEEF), tính toán số lượng bát mì phục vụ và tổng thời gian phản hồi của đội chơi.
*   **Lưu trữ tạm thời (In-Memory Database):** Toàn bộ trạng thái trận đấu được quản lý và lưu giữ ngay trên bộ nhớ RAM để đảm bảo tốc độ phản hồi nhanh.

---

## Tech Stack

Dự án sử dụng các công nghệ chính sau:

*   **Language:** Java 21
*   **Framework:** Spring Boot 3.5.4 (sử dụng `spring-boot-starter-web` cho REST API)
*   **Validation:** Jakarta Bean Validation (`spring-boot-starter-validation`)
*   **Utilities:** Lombok (rút gọn code boilerplate)
*   **Database / Storage:** In-Memory (lưu trữ trong bộ nhớ RAM thông qua các Java Collection)
*   **Build Tool:** Maven
*   **Testing Framework:** JUnit 5, Mockito, Spring Boot Test, ArchUnit 1.3.0 (dùng để kiểm thử kiến trúc lục giác)

---

## Architecture

Dự án áp dụng **Kiến trúc lục giác (Hexagonal Architecture)** để phân tách các mối quan tâm:

*   **Domain Layer:** Nằm ở lõi trong cùng, chứa các thực thể (Entities), Value Objects, Domain Services và logic nghiệp vụ thuần túy của trò chơi Hexudon. Tầng này độc lập hoàn toàn và không phụ thuộc vào bất kỳ framework nào (kể cả Spring).
*   **Application Layer:** Chứa các cổng giao tiếp nghiệp vụ (Inbound/Outbound Ports), Dịch vụ ứng dụng (`MatchApplicationService` điều phối các cổng) và các lớp vận chuyển dữ liệu (DTOs, Mappers).
*   **Adapter Layer:** Chứa các Adapter triển khai chi tiết kỹ thuật:
    *   *Inbound Adapters (Driving)*: `MatchController` (REST API), `MatchInitializerRunner` (tự khởi tạo bản đồ ở startup).
    *   *Outbound Adapters (Driven)*: `InMemoryMatchStateRepository` (lưu trữ In-memory), `FileMatchConfigLoader` (đọc file text cấu hình).
*   **Infrastructure Layer:** Chứa cấu hình hạ tầng như chính sách CORS (`WebConfig`), cấu hình Spring (`AppConfig`) và các tiện ích dùng chung (`FileUtils`).

### Sơ đồ kiến trúc (Architecture Diagram)

```text
         +---------------------------------------------------------+
         |                      ADAPTER LAYER                      |
         |                                                         |
         |  [Inbound Adapters]              [Outbound Adapters]    |
         |  - MatchController               - InMemoryMatchRepo    |
         |  - MatchInitializerRunner        - FileMatchConfigLoader|
         |                                  - DomainBeanConfig     |
         +------------|------------------------------^-------------+
                      |                              |
         +------------v------------------------------|-------------+
         |                     APPLICATION LAYER                   |
         |                                                         |
         |  [Inbound Ports]                 [Outbound Ports]       |
         |  - RegisterTeamUseCase           - MatchConfigLoaderPort|
         |  - SubmitActionsUseCase          - MatchStateStorePort  |
         |  - GetMatchStateUseCase                                 |
         |                                                         |
         |               [MatchApplicationService]                 |
         +---------------------------|-----------------------------+
                                     |
         +---------------------------v-----------------------------+
         |                       DOMAIN LAYER                      |
         |                                                         |
         |  [Entities & Aggregates]         [Domain Services]      |
         |  - MatchState (Aggregate Root)   - HexGridGenerator     |
         |  - Team, Agent (Patrol, Refuel)  - ActionValidator      |
         |  - GameMap, Spot, ScoreBoard     - AgentSpawnService    |
         |                                                         |
         |  [Value Objects]                 [Exceptions]           |
         |  - Coordinate, Direction, Cell   - BusinessException    |
         |  - Action, MovementCost          - GameRuleViolation... |
         +---------------------------------------------------------+
```

---

## Project Structure

Cấu trúc cây thư mục mã nguồn của dự án như sau:

```text
.
├── .gitignore
├── pom.xml
└── src
    └── main
        ├── java
        │   └── com
        │       └── naprock
        │           └── hexudon
        │               ├── HexudonApplication.java
        │               ├── adapter
        │               │   ├── in
        │               │   │   ├── initializer
        │               │   │   │   └── MatchInitializerRunner.java
        │               │   │   └── rest
        │               │   │       ├── MatchController.java
        │               │   │       └── advice
        │               │   │           ├── ErrorResponse.java
        │               │   │           ├── GlobalExceptionHandler.java
        │               │   │           └── ValidationErrorDetail.java
        │               │   └── out
        │               │       ├── configuration
        │               │       │   └── DomainBeanConfig.java
        │               │       ├── loader
        │               │       │   └── FileMatchConfigLoader.java
        │               │       └── persistence
        │               │           └── InMemoryMatchStateRepository.java
        │               ├── application
        │               │   ├── dto
        │               │   │   ├── agent
        │               │   │   │   └── AgentResponse.java
        │               │   │   ├── match
        │               │   │   │   ├── ActionRequest.java
        │               │   │   │   ├── CellResponse.java
        │               │   │   │   ├── CoordinateRequest.java
        │               │   │   │   ├── CoordinateResponse.java
        │               │   │   │   ├── MatchConfigResponse.java
        │               │   │   │   ├── MatchStateResponse.java
        │               │   │   │   ├── SpotResponse.java
        │               │   │   │   ├── SubmitActionRequest.java
        │               │   │   │   └── TrafficResponse.java
        │               │   │   └── team
        │               │   │       ├── TeamRegisterRequest.java
        │               │   │       ├── TeamResponse.java
        │               │   │       └── TeamScoreResponse.java
        │               │   ├── mapper
        │               │   │   └── MatchMapper.java
        │               │   ├── model
        │               │   │   ├── match
        │               │   │   │   └── MatchStateData.java
        │               │   │   └── team
        │               │   │       └── TeamRegistrationData.java
        │               │   ├── port
        │               │   │   ├── in
        │               │   │   │   ├── GetMatchConfigUseCase.java
        │               │   │   │   ├── GetMatchStateUseCase.java
        │               │   │   │   ├── InitializeMatchUseCase.java
        │               │   │   │   └── RegisterTeamUseCase.java
        │               │   │   │   └── SubmitActionsUseCase.java
        │               │   │   └── out
        │               │   │       ├── MatchConfigLoaderPort.java
        │               │   │       └── MatchStateStorePort.java
        │               │   └── service
        │               │       └── MatchApplicationService.java
        │               ├── domain
        │               │   ├── exception
        │               │   │   ├── base
        │               │   │   │   ├── BusinessException.java
        │               │   │   │   └── SystemException.java
        │               │   │   ├── business
        │               │   │   │   ├── GameRuleViolationException.java
        │               │   │   │   ├── MatchStateConflictException.java
        │               │   │   │   ├── RateLimitExceededException.java
        │               │   │   │   └── ResourceNotFoundException.java
        │               │   │   ├── code
        │               │   │   │   └── ErrorCode.java
        │               │   │   └── system
        │               │   │       └── ConfigLoadException.java
        │               │   ├── model
        │               │   │   ├── agent
        │               │   │   │   ├── Agent.java
        │               │   │   │   ├── AgentType.java
        │               │   │   │   ├── PatrolAgent.java
        │               │   │   │   └── RefuelAgent.java
        │               │   │   ├── geometry
        │               │   │   │   ├── Coordinate.java
        │               │   │   │   └── Direction.java
        │               │   │   ├── map
        │               │   │   │   ├── Cell.java
        │               │   │   │   ├── GameMap.java
        │               │   │   │   ├── Spot.java
        │               │   │   │   ├── TerrainType.java
        │               │   │   │   ├── TrafficLevel.java
        │               │   │   │   └── UdonType.java
        │               │   │   ├── match
        │               │   │   │   ├── MatchConfig.java
        │               │   │   │   ├── MatchState.java
        │               │   │   │   └── MatchStatus.java
        │               │   │   ├── movement
        │               │   │   │   ├── Action.java
        │               │   │   │   ├── ActionType.java
        │               │   │   │   ├── MovementCost.java
        │               │   │   │   └── MoveResult.java
        │               │   │   ├── score
        │               │   │   │   ├── ScoreBoard.java
        │               │   │   │   └── TeamScore.java
        │               │   │   ├── team
        │               │   │   │   ├── CollectResult.java
        │               │   │   │   └── Team.java
        │               │   │   └── traffic
        │               │   │       ├── TrafficFlow.java
        │               │   │       ├── TrafficHistory.java
        │               │   │       ├── TrafficLevel.java
        │               │   │       └── TrafficTracker.java
        │               │   └── service
        │               │       ├── ActionValidator.java
        │               │       ├── AgentSpawnService.java
        │               │       ├── GeneratedMap.java
        │               │       └── HexGridGenerator.java
        │               └── infrastructure
        │                   ├── configuration
        │                   │   ├── AppConfig.java
        │                   │   └── WebConfig.java
        │                   └── util
        │                       └── FileUtils.java
        └── resources
            ├── application.yml
            ├── match_config.txt
            └── sample.txt
```

### Giải thích các package chính:
*   `adapter`: Triển khai các thành phần cấu hình và giao diện kết nối cụ thể (REST endpoints, persistence memory, loaders).
*   `application`: Quản lý các logic chuyển đổi dữ liệu DTO, định nghĩa UseCases (in/out ports) và cài đặt lớp nghiệp vụ ứng dụng điều phối chính.
*   `domain`: Mô hình hóa các thực thể cốt lõi, Value Objects và thuật toán vận hành trò chơi (di chuyển, luật chơi kẹt xe, tính toán điểm số).
*   `infrastructure`: Chứa cấu hình CORS, Spring Context và các helper class đọc ghi file chung.

---

## Core Concepts

Dưới đây là mô tả chi tiết các thành phần chính thực sự tồn tại trong mã nguồn:

### Domain Model / Entity
*   **MatchState (Aggregate Root):** Quản lý trạng thái vòng đời trận đấu (`status`), vòng lặp các lượt chơi (`currentTurn`), thời gian, danh sách các đội chơi, bản đồ trò chơi, lịch sử giao thông và bảng điểm. Cung cấp phương thức `finishTurn(config)` để thực thi từng bước đi cho tất cả Agent của các đội, cập nhật bảng điểm, mật độ giao thông và chuyển lượt chơi.
*   **Team:** Đại diện cho một đội chơi đăng ký tham gia, quản lý danh sách Agent thuộc đội. Cung cấp phương thức điều khiển Agent thực thi hành động (`executeStep`) và tính năng tự động tiếp nhiên liệu cho PatrolAgent (`autoRefuel`).
*   **Agent (Abstract Class) & Subclasses (`PatrolAgent`, `RefuelAgent`):** Thực thể đại diện cho các nhân vật di động trên bản đồ.
    *   `Agent` quản lý vị trí hiện tại, lượng nhiên liệu (`fuel`), số bước đi còn lại (`remainingSteps`), và danh sách các hành động dự kiến thực hiện trong lượt.
    *   `PatrolAgent` có khả năng thu hoạch mì Udon (`collectUdon`) và tiêu hao cả nhiên liệu lẫn bước đi khi di chuyển.
    *   `RefuelAgent` không tiêu hao nhiên liệu khi đi, có khả năng nạp đầy nhiên liệu cho PatrolAgent đứng cùng ô tọa độ.
*   **Spot:** Điểm lưu trữ mì Udon trên bản đồ. Để đảm bảo tính công bằng, lượng tồn kho của Spot được quản lý theo từng đội chơi riêng biệt (`teamUdonStocks`), đội này lấy Udon không làm hao hụt Udon của đội khác.
*   **TeamScore:** Theo dõi kết quả thi đấu của mỗi đội chơi gồm: danh sách các loại mì đã thu hoạch được, lịch sử thu hoạch theo lượt, tổng số bát mì phục vụ và tổng thời gian phản hồi.

### Value Object
*   **Coordinate:** Biểu diễn tọa độ của lưới lục giác trong hệ tọa độ Odd-R offset. Cung cấp thuật toán xác định ô kề cạnh (`isAdjacentTo`) và đo khoảng cách hex grid (`distanceTo`) bằng cách chuyển đổi sang hệ tọa độ Cube (`CubeCoordinate`).
*   **Direction:** Enum biểu diễn 6 hướng di chuyển hợp lệ trên lưới lục giác ngang ngang (Odd-R).
*   **Cell:** Đại diện cho một ô lưới có vị trí `Coordinate` và loại địa hình `TerrainType`. Ô có địa hình là `POND` thì Agent không thể đi vào (`isWalkable() == false`).
*   **MovementCost:** Mô tả chi phí bước đi và nhiên liệu cần tiêu thụ khi Agent di chuyển vào ô đích.
*   **MoveResult:** Lưu kết quả thực thi một bước đi (Thành công/Thất bại) cùng tọa độ cuối cùng của Agent.
*   **Action:** Hành động di chuyển hoặc chờ của Agent kèm tọa độ đích tương ứng.
*   **UdonType:** Record loại mì Udon (`TANUKI`, `KITSUNE`, `TEMPURA`, `BEEF`).
*   **TrafficFlow:** Bản ghi lưu giữ số lượt Agent di chuyển qua hoặc đứng lại ở ô đường đi (`ROAD`) ở lượt hiện tại và lượt trước đó.

### Domain Service
*   **HexGridGenerator:** Tạo ngẫu nhiên lưới lục giác với tỷ lệ địa hình phân bổ: PLAIN (65%), MOUNTAIN (20%), ROAD (5%), POND (10%). Bố trí ngẫu nhiên các điểm chứa mì Udon (Spot) trên các ô không phải POND hay MOUNTAIN với khoảng cách kề nhau tối thiểu là 3.
*   **AgentSpawnService:** Sinh ngẫu nhiên tọa độ xuất phát hợp lệ (chỉ trên các ô `isWalkable() == true`) và đảm bảo các Agent không xuất phát trùng vị trí.
*   **ActionValidator:** Thẩm định danh sách chuỗi hành động mà đội chơi gửi lên. Sử dụng một tác tử giả lập để chạy thử hành động nhằm phát hiện hành vi lỗi (ví dụ: đi vào ô không đi được, đi không kề cạnh) trước khi cho phép gán chính thức các hành động vào Agent thật.

### Use Case (Inbound Port) & Outbound Port
*   **Use Cases:** `InitializeMatchUseCase` (Khởi tạo bản đồ và trận đấu), `GetMatchConfigUseCase` (Lấy cấu hình trận đấu), `GetMatchStateUseCase` (Lấy trạng thái lượt hiện tại), `RegisterTeamUseCase` (Đăng ký đội chơi mới), `SubmitActionsUseCase` (Gửi hành động cho các Agent).
*   **Outbound Ports:** `MatchConfigLoaderPort` (Tải cấu hình), `MatchStateStorePort` (Lưu/đọc trạng thái trận đấu).

### Controller / API
*   `MatchController`: Cung cấp các REST endpoints để tương tác với game.

---

## API Documentation

Mã nguồn `MatchController.java` cung cấp các API RESTful sau đây phục vụ cho việc thi đấu:

### 1. Đăng ký đội chơi
*   **HTTP Method:** `POST`
*   **Path:** `/api/match/register`
*   **Request Body (`TeamRegisterRequest`):**
    ```json
    {
      "teamName": "TeamA",
      "amountPatrol": 1,
      "amountRefuel": 1
    }
    ```
*   **Phản hồi:** `201 Created`
*   **Mục đích:** Đăng ký đội chơi tham gia vào trận đấu. Hệ thống sẽ sinh ngẫu nhiên vị trí xuất phát cho các Agent của đội dựa trên số lượng Patrol và Refuel đã yêu cầu.

### 2. Xem cấu hình trận đấu
*   **HTTP Method:** `GET`
*   **Path:** `/api/match/config`
*   **Phản hồi (`MatchConfigResponse`):** Trả về kích thước bản đồ, thông tin chi tiết của tất cả các ô lưới (`cells`) kèm địa hình, vị trí các điểm Udon (`spots`), số lượng Agent tối đa, dung tích bình nhiên liệu và số bước di chuyển tối đa trong một lượt.
*   **Mục đích:** Giúp client tải thông tin cấu trúc bản đồ ban đầu để chuẩn bị chiến thuật.

### 3. Xem trạng thái trận đấu hiện tại
*   **HTTP Method:** `GET`
*   **Path:** `/api/match/state`
*   **Request Header:** `X-Team-Name: <tên_đội_chơi>` (Bắt buộc)
*   **Phản hồi (`MatchStateResponse`):** Trả về trạng thái trận đấu (`WAITING`, `PLAYING`, `FINISHED`), lượt chơi hiện tại, thông tin chi tiết các Agent thuộc sở hữu của đội yêu cầu, mức độ giao thông trên các ô ROAD ở lượt trước, thông số tồn kho Udon tại các Spot của đội, và điểm số của toàn bộ các đội.
*   **Mục đích:** Đội chơi lấy thông tin cập nhật sau từng lượt để tính toán hành động tiếp theo.

### 4. Gửi hành động của các Agent trong lượt hiện tại
*   **HTTP Method:** `POST`
*   **Path:** `/api/match/actions`
*   **Request Header:** `X-Team-Name: <tên_đội_chơi>` (Bắt buộc)
*   **Request Body (`SubmitActionRequest`):**
    ```json
    {
      "actions": [
        {
          "agentId": "A1",
          "actionType": "MOVE",
          "coordinate": {
            "x": 2,
            "y": 3
          }
        }
      ]
    }
    ```
*   **Phản hồi:** `202 Accepted`
*   **Mục đích:** Đội chơi gửi chuỗi hành động di chuyển/chờ cho từng Agent của mình trong lượt đi hiện tại.

---

## Configuration

*   **Cấu hình môi trường Spring Boot:** Thiết lập trong file `src/main/resources/application.yml` (ví dụ: cổng mạng HTTP mặc định là `8080`).
*   **Cấu hình thông số trận đấu:** Thiết lập trong file `src/main/resources/match_config.txt`. Các thông số bao gồm:
    *   `mapWidth`, `mapHeight`: Kích thước bản đồ.
    *   `maxTurns`: Số lượt đi tối đa.
    *   `maxTeams`: Số lượng đội tối đa tham gia.
    *   `agentsPerTeam`: Số lượng tác tử tối đa của mỗi đội.
    *   `initialFuel`, `maxFuel`: Lượng xăng khởi động và xăng tối đa của tác tử.
    *   `plainStepCost`, `mountainStepCost`, `roadStepCost`: Chi phí di chuyển bằng điểm bước đi theo địa hình.
    *   `plainFuelCost`, `mountainFuelCost`, `roadFuelCost`: Chi phí di chuyển tiêu hao xăng theo địa hình của PatrolAgent.
    *   `maxStepsPerTurn`: Số bước đi tối đa mỗi lượt đấu.
    *   `initialSpotUdonStock`: Lượng Udon khởi tạo ban đầu tại mỗi Spot.

---

## How To Run

### Yêu cầu hệ thống (Requirement)
*   Java Development Kit (JDK) phiên bản 21 hoặc mới hơn.
*   Apache Maven 3.9.x hoặc mới hơn.

### Các lệnh thực thi thực tế từ project:
*   **Lệnh dọn dẹp và đóng gói dự án (Build):**
    ```bash
    mvn clean package
    ```
*   **Lệnh chạy server cục bộ (Run):**
    ```bash
    mvn spring-boot:run
    ```
*   **Lệnh chạy các bài kiểm thử tự động (Test):**
    ```bash
    mvn test
    ```

---

## Testing

*   **Testing Framework:** Dự án đã cấu hình thư viện JUnit 5, Mockito và ArchUnit (kiểm thử kiến trúc) trong file `pom.xml`.
*   **Cách chạy kiểm thử:** Sử dụng lệnh `mvn test` trên terminal.
*   **Các nhóm test hiện có:** Thư mục `src/test/java` hiện tại đang trống (chưa có lớp kiểm thử cụ thể nào được viết).

---

## Development Notes

### Luồng xử lý chính (Main Processing Flow)
1.  **Giai đoạn khởi chạy ứng dụng:** `MatchInitializerRunner` gọi UseCase `initializeMatch()`. Bản đồ lục giác được sinh ngẫu nhiên bởi `HexGridGenerator`, các chi phí di chuyển ban đầu và lưu lượng giao thông kề cạnh trên các ô ROAD được thiết lập bằng 0 trong `TrafficHistory`.
2.  **Đăng ký đội chơi:** API `registerTeam` nhận tên đội, sinh ngẫu nhiên tọa độ xuất phát cách biệt cho các Agent thông qua `AgentSpawnService` và lưu thông tin đội chơi vào bộ nhớ.
3.  **Bắt đầu trận đấu:** Khi trận đấu bắt đầu (`start`), trạng thái đổi sang `PLAYING`, toàn bộ Agent được nạp đầy xăng và cấp đầy đủ số bước đi tối đa cho lượt 1. Tồn kho Udon tại các Spot được khôi phục.
4.  **Vòng lặp lượt chơi (Turn Loop):**
    *   Các đội chơi gọi API `getMatchState` để xem hiện trạng, sau đó lập trình tính toán hành động và gửi lên qua API `submitActions`.
    *   Khi lượt đi hoàn tất (kết thúc thời gian phản hồi hoặc kích hoạt chuyển lượt), `MatchState.finishTurn` sẽ chạy mô phỏng di chuyển của tất cả tác tử qua từng bước từ bước lớn nhất về bước 1.
    *   Hệ thống kiểm tra tiếp nhiên liệu tự động (`autoRefuel`) khi PatrolAgent đứng chung tọa độ với RefuelAgent.
    *   Thực hiện hành động di chuyển của Agent, tiêu hao tài nguyên (bước đi và nhiên liệu).
    *   PatrolAgent thực hiện thu thập mì Udon tại các ô Spot chứa Udon hợp lệ (nếu chưa thu hoạch Spot đó trong ngày), cập nhật lượng mì thu hoạch được.
    *   Cập nhật điểm số thu hoạch được vào `ScoreBoard`.
    *   Cập nhật lịch sử giao thông (`TrafficHistory`) trên các ô ROAD dựa trên số lượng Agent đã đi qua hoặc đứng lại ở ô đó, từ đó tính toán lại chi phí di chuyển (bước đi) cho các lượt chơi tiếp theo.
    *   Reset bước đi của các Agent, khôi phục tồn kho Udon tại các Spot và tăng số lượt chơi hiện tại (`currentTurn`). Khi vượt quá `maxTurns`, trạng thái đổi sang `FINISHED`.

### Những điểm cần lưu ý khi đọc mã nguồn
*   **Vị trí kề cạnh trên lưới lục giác ngang Odd-R:** Cách xác định ô láng giềng kề cạnh phụ thuộc vào dòng chẵn hay dòng lẻ của ô hiện tại (`y % 2 != 0`). Việc tính toán được thực hiện chi tiết trong phương thức `isAdjacentTo` và `getNeighbor` của lớp `Coordinate`, và `getDx` / `getDy` của enum `Direction`.
*   **Ràng buộc logic tại Action.java:** Hành động chờ (`ActionType.WAIT`) trong hàm kiểm tra hợp lệ `validate` của `Action` quy định `targetCoordinate` phải là `null`. Tuy nhiên, phương thức static factory khởi tạo hành động chờ là `Action.stay(Coordinate targetCoordinate)` lại truyền tọa độ đích (không null) vào constructor. Điều này có thể dẫn đến ngoại lệ `GameRuleViolationException` nếu sử dụng phương thức `stay` để khởi tạo hành động chờ.
*   **Lưu lượng giao thông kẹt xe:** Chỉ có các ô có địa hình là `ROAD` mới được theo dõi lưu lượng giao thông. Việc tính toán lưu lượng giao thông ở một ô dựa trên tổng số lượt Agent dừng lại hoặc đi qua ô đó ở lượt hiện tại và lượt trước đó, chia cho tổng số đội chơi tối đa.

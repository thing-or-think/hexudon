# 01. TỔNG QUAN DỰ ÁN (PROJECT OVERVIEW)

## Mục lục
1. [Mục tiêu dự án](#1-mục-tiêu-dự-án)
2. [Nguyên tắc refactor](#2-nguyên-tắc-refactor)
3. [Tổng quan Domain-Driven Design (DDD)](#3-tổng-quan-domain-driven-design-ddd)
4. [Tổng quan Hexagonal Architecture (Ports & Adapters)](#4-tổng-quan-hexagonal-architecture-ports--adapters)
5. [Sơ đồ dòng phụ thuộc (Dependency Flow Diagram)](#5-sơ-đồ-dòng-phụ-thuộc-dependency-flow-diagram)
6. [Danh sách và mô tả chức năng của toàn bộ package](#6-danh-sách-và-mô-tả-chức-năng-của-toàn-bộ-package)
7. [Luồng xử lý yêu cầu (Request Processing Flow)](#7-luồng-xử-lý-yêu-cầu-request-processing-flow)

---

## 1. Mục tiêu dự án

### Mục đích project
Hexudon Game Server là hệ thống điều phối các trận đấu mô phỏng theo lượt (Turn-based Simulation) trên lưới bản đồ lục giác. Các đội chơi lập trình client gửi kế hoạch hành động cho các Agents (Patrol Agent để đi thu thập bánh Udon, Refuel Agent để tiếp nhiên liệu). 

### Kiến trúc hiện tại
Dự án hiện tại được tổ chức theo các package phẳng, thiếu sự phân tách rõ ràng giữa nghiệp vụ cốt lõi (Domain Core) và hạ tầng (Spring Boot, Filesystem, HTTP REST). 
- **Cấu trúc hiện tại:** `config`, `controller`, `manager`, `engine`, `model`, `dto`, `loader`, `exception`, `interceptor`, `util`.
- **Hạn chế:** Các class điều phối (`MatchManager`) phụ thuộc trực tiếp vào các adapter kỹ thuật (`MatchConfigLoader`), gây khó khăn khi thay đổi nguồn dữ liệu hoặc tích hợp Database (Persistence). Sự phân tách giữa nghiệp vụ thuần túy và ứng dụng (Application Service) chưa rõ ràng.

### Kiến trúc mục tiêu
Chuyển đổi toàn diện hệ thống sang mô hình kiến trúc **Domain-Driven Design (DDD)** kết hợp **Hexagonal Architecture (Ports & Adapters)**.
- Phân biệt rõ ràng giữa logic nghiệp vụ cốt lõi (Domain), logic điều phối ứng dụng (Application), giao tiếp cổng ngoài (Adapters) và cấu hình kỹ thuật (Infrastructure).
- Cô lập Domain Core hoàn toàn khỏi các framework bên ngoài (bao gồm cả Spring Framework và các thư viện Serialization).
- Chuẩn bị sẵn sàng cấu trúc để dễ dàng tích hợp các hệ quản trị cơ sở dữ liệu (RDBMS/NoSQL) thay cho việc lưu trữ in-memory hiện tại.

### Phạm vi refactor
- **Đối tượng thay đổi:** Vị trí tệp tin (package structure), cấu trúc imports, cách khai báo phụ thuộc (Dependency Injection thông qua Constructor), cách phân chia vai trò class (từ `MatchManager` phân tách thành Use Cases, Application Services, Ports và Repository).
- **Tuyệt đối KHÔNG thay đổi:**
  - Thuật toán di chuyển, tính nhiên liệu, tính điểm hay luật chơi.
  - Cấu trúc dữ liệu REST API (giữ nguyên JSON request/response).
  - Hành vi lưu trữ dữ liệu (tạm thời vẫn duy trì lưu trữ in-memory nhưng cấu trúc thông qua Repository Interface).
  - Không thêm mới bất kỳ tính năng nghiệp vụ nào.

---

## 2. Nguyên tắc refactor

Trong suốt quá trình tái cấu trúc, toàn bộ đội ngũ lập trình viên phải tuân thủ nghiêm ngặt các nguyên tắc sau:

| Nguyên tắc | Mô tả chi tiết | Kiểm soát bằng |
| :--- | :--- | :--- |
| **Bảo toàn logic nghiệp vụ 100%** | Không thay đổi thuật toán của `MovementSimulator`, `FuelManager`, `TrafficCalculator` và `ScoringEngine`. | Hệ thống Unit Test hiện tại phải Pass 100% |
| **Không đổi API REST** | Các Endpoint HTTP (`/api/match/...`), HTTP Method (GET, POST), và cấu trúc JSON Request/Response DTO phải giữ nguyên. | Integration Test / Controller Test |
| **Không đổi cấu hình trận đấu** | Giữ nguyên định dạng và quy tắc đọc của file `match_config.txt`. | Config Loader Test |
| **Nguyên tắc phụ thuộc một chiều** | Lớp Domain không được phụ thuộc vào bất kỳ lớp nào bên ngoài nó (Application, Adapter, Infrastructure). | ArchUnit Test (`ArchitectureTest.java`) |
| **Không sử dụng Spring Bean trong Domain** | Không dùng các annotation của Spring (`@Service`, `@Component`, `@Autowired`) trong Domain Model, Value Object và Domain Service. | Rà soát thủ công & ArchUnit |
| **Refactor từng bước nhỏ (Iterative)** | Chia nhỏ quá trình refactor thành các Phase độc lập. Mỗi Phase có thể compile, chạy test và commit/rollback độc lập. | Quy trình Git Commit & CI/CD |

---

## 3. Tổng quan Domain-Driven Design (DDD)

Kiến trúc mới chia nhỏ thế giới trò chơi Hexudon thành các khái niệm DDD rõ ràng:

- **Domain (Nghiệp vụ):** Toàn bộ luật chơi, trạng thái bản đồ lục giác, năng lượng của điệp viên, cách thức thu thập Udon và mô phỏng trận đấu.
- **Entity (Thực thể):** Đối tượng có định danh duy nhất (Identity) và vòng đời thay đổi.
  - *Ví dụ:* `Agent` (định danh bằng Agent ID), `Team` (định danh bằng Team Name), `Spot` (tọa độ vị trí).
- **Aggregate & Aggregate Root (Cụm thực thể và Gốc cụm):** 
  - `MatchState` là **Aggregate Root**. Toàn bộ các thực thể khác như `Team`, `Agent`, `Spot` đều thuộc quản lý của cụm này. Mọi thay đổi trạng thái bên trong cụm phải thông qua Aggregate Root (`MatchState`).
- **Value Object (Đối tượng giá trị):** Đối tượng không có định danh độc lập, thuộc tính định nghĩa giá trị của nó, có tính bất biến (Immutable).
  - *Ví dụ:* `Cell` (Tọa độ X, Y và TerrainType), `Road` (Kết nối giữa 2 Cell), `Action` (Hành động đơn lẻ của Agent), `Submission` (Kế hoạch nộp cho lượt chơi), `MatchConfig` (Cấu hình luật chơi).
- **Domain Service (Dịch vụ nghiệp vụ):** Các logic chứa thuật toán phức tạp liên quan đến nhiều Entity/Value Object mà không thuộc về riêng lẻ một đối tượng nào.
  - *Ví dụ:* `MovementSimulator` (Mô phỏng bước đi), `FuelManager` (Quản lý nạp nhiên liệu), `TrafficCalculator` (Tính mật độ giao thông), `ScoringEngine` (Tính điểm), `MapValidator` (Kiểm tra liên thông bản đồ).
- **Repository (Kho lưu trữ - Interface):** Cổng giao tiếp nghiệp vụ để lưu trữ và truy xuất Aggregate Root.
  - *Ví dụ:* `MatchStateRepository` cung cấp các phương thức lưu trữ/lấy ra trạng thái trận đấu hiện tại.
- **Application Service (Dịch vụ ứng dụng):** Lớp điều phối (Orchestrator), nhận yêu cầu từ Use Case, tương tác với Repository để lấy Aggregate Root, ủy quyền xử lý cho Aggregate hoặc Domain Service, và lưu lại trạng thái mới.

---

## 4. Tổng quan Hexagonal Architecture (Ports & Adapters)

Kiến trúc Hexagonal cô lập lõi nghiệp vụ (Domain + Application) khỏi tác động của công nghệ bên ngoài thông qua các Ports (Cổng giao tiếp) và Adapters (Bộ chuyển đổi):

- **Inbound Port (Cổng vào):** Các Interface định nghĩa các ca sử dụng (Use Cases) mà hệ thống cung cấp cho thế giới bên ngoài gọi vào.
  - *Ví dụ:* `RegisterTeamUseCase`, `StartMatchUseCase`, `SubmitActionsUseCase`, `GetMatchStateUseCase`.
- **Inbound Adapter (Bộ chuyển đổi vào):** Các tác nhân bên ngoài hoặc framework kích hoạt hệ thống thông qua Inbound Port.
  - *Ví dụ:* `MatchController` nhận HTTP Request REST, chuyển đổi dữ liệu DTO thành cấu trúc nghiệp vụ và gọi Use Case tương ứng.
- **Outbound Port (Cổng ra):** Các Interface định nghĩa các dịch vụ kỹ thuật mà ứng dụng cần từ môi trường bên ngoài để hoàn thành nghiệp vụ.
  - *Ví dụ:* `MatchStateStorePort` (lưu trữ trạng thái), `MatchConfigLoaderPort` (đọc file cấu hình).
- **Outbound Adapter (Bộ chuyển đổi ra):** Triển khai (Implementation) cụ thể các Outbound Port sử dụng công nghệ hay thư viện hạ tầng.
  - *Ví dụ:* `InMemoryMatchStateRepository` (lưu trạng thái trong RAM), `FileMatchConfigLoader` (đọc config từ file text).
- **Dependency Rule (Quy tắc phụ thuộc):** Chiều phụ thuộc luôn hướng từ ngoài vào trong. Lớp bên ngoài biết lớp bên trong, lớp bên trong tuyệt đối không biết gì về lớp bên ngoài.
- **Dependency Inversion (Đảo ngược phụ thuộc):** Lớp Application định nghĩa các Outbound Port (Interface). Lớp Adapter (nằm ngoài) sẽ implement các Port này. Nhờ đó, Application Service không phụ thuộc vào hạ tầng kỹ thuật cụ thể.

---

## 5. Sơ đồ dòng phụ thuộc (Dependency Flow Diagram)

Dưới đây là sơ đồ ASCII thể hiện dòng phụ thuộc giữa các tầng kiến trúc. Hướng mũi tên `-->` thể hiện chiều phụ thuộc (`A --> B` nghĩa là A import/biết B).

```text
               +-----------------------------------------------------------+
               |                       ADAPTER LAYER                       |
               |                                                           |
               |  +--------------------+           +--------------------+  |
               |  |     INBOUND        |           |     OUTBOUND       |  |
               |  |  MatchController   |           | InMemoryMatchRepo  |  |
               |  +---------+----------+           +---------+----------+  |
               +------------|--------------------------------|-------------+
                            | (implements)                   | (implements)
                            v                                v
               +------------|--------------------------------|-------------+
               |            |            APPLICATION LAYER   |             |
               |            v                                |             |
               |  +--------------------+                     |             |
               |  |    INBOUND PORT    |                     v             |
               |  |  (RegisterTeamUC)  |           +--------------------+  |
               |  +---------+----------+           |   OUTBOUND PORT    |  |
               |            ^                      | (MatchStateStoreP) |  |
               |            | (implements)         +---------+----------+  |
               |  +---------+----------+                     ^             |
               |  |    APPLICATION     |                     |             |
               |  |      SERVICE       +---------------------+ (calls)     |
               |  | (MatchAppService)  |                                   |
               |  +---------+----------+                                   |
               +------------|----------------------------------------------+
                            | (calls & orchestrates)
                            v
               +-----------------------------------------------------------+
               |                        DOMAIN LAYER                       |
               |                                                           |
               |  +--------------------+           +--------------------+  |
               |  |   DOMAIN SERVICE   |---------->|   AGGREGATE ROOT   |  |
               |  | (MovementSimulator)|           |    (MatchState)    |  |
               |  +---------+----------+           +---------+----------+  |
               |            |                                |             |
               |            v                                v             |
               |  +--------------------+           +--------------------+  |
               |  |    VALUE OBJECT    |<----------|    DOMAIN ENTITY   |  |
               |  |   (Cell / Action)  |           |   (Agent / Team)   |  |
               |  +--------------------+           +--------------------+  |
               +-----------------------------------------------------------+
```

---

## 6. Danh sách và mô tả chức năng của toàn bộ package

Sau khi refactor, cấu trúc thư mục dự án sẽ nằm dưới package gốc `com.naprock.hexudon` và được phân bổ như sau:

| Tầng (Layer) | Gói (Package) | Vai trò & Chức năng |
| :--- | :--- | :--- |
| **Domain** | `domain.model` | Chứa các thực thể chính của game có định danh như `Agent`, `Team`. |
| | `domain.valueobject` | Chứa các đối tượng bất biến biểu diễn giá trị: `Cell`, `Road`, `Spot`, `Action`, `Submission`, `MatchConfig`, `MatchStatus`, `TerrainType`, `AgentType`. |
| | `domain.event` | Chứa các sự kiện phát sinh trong domain (phục vụ mở rộng, hiện tại chưa sử dụng). |
| | `domain.service` | Chứa các engine tính toán nghiệp vụ thuần túy: `MovementSimulator`, `FuelManager`, `TrafficCalculator`, `ScoringEngine`, `UdonCollectionEngine`, `MapValidator`, `HexGridUtils`, `TerrainGenerator`. |
| | `domain.repository` | Chứa các Interface định nghĩa lưu trữ Aggregate Root (ví dụ: `MatchStateRepository`). |
| | `domain.exception` | Chứa các ngoại lệ nghiệp vụ cốt lõi: `BusinessException`, `GameRuleViolationException`, `MatchStateConflictException`, `ResourceNotFoundException`. |
| **Application** | `application.usecase` | Định nghĩa các ca sử dụng (Inbound Ports) dưới dạng Interface độc lập. |
| | `application.port.in` | Gói chứa các port đầu vào (nơi khai báo các Interface UseCase chi tiết). |
| | `application.port.out` | Định nghĩa các cổng kết nối hạ tầng đầu ra: `MatchStateStorePort`, `MatchConfigLoaderPort`. |
| | `application.dto` | Chứa các Record DTO trao đổi dữ liệu phục vụ riêng cho các ca sử dụng. |
| | `application.mapper` | Chứa bộ ánh xạ dữ liệu giữa DTO và Domain Objects (`ActionMapper`). |
| | `application.service` | Triển khai các Inbound Ports, điều phối luồng xử lý bằng cách kết hợp Domain và Outbound Ports. |
| **Adapter** | `adapter.in.rest` | Chứa các REST Controller (`MatchController`) tiếp nhận HTTP request. |
| | `adapter.out.persistence` | Triển khai `MatchStateStorePort` bằng cách quản lý bộ nhớ tạm (In-memory) hoặc Database sau này. |
| | `adapter.out.loader` | Triển khai `MatchConfigLoaderPort` để đọc và phân tích cấu hình từ hệ thống file. |
| | `adapter.out.configuration` | Cấu hình Beans kết nối giữa Ports và Adapters trong ngữ cảnh Spring Boot. |
| **Infrastructure** | `infrastructure.configuration` | Cấu hình kỹ thuật của hệ thống: CORS, Scheduler (chuyển ngày tự động), Web MVC. |
| | `infrastructure.interceptor` | Các bộ lọc hạ tầng kỹ thuật: `RateLimiterInterceptor`. |
| | `infrastructure.scheduler` | Bộ định thời Scheduler chạy ngầm để tự động hóa việc chuyển ngày (`nextDay`). |
| | `infrastructure.util` | Các tiện ích hệ thống như `FileUtils` hỗ trợ đọc ghi tệp vật lý. |

---

## 7. Luồng xử lý yêu cầu (Request Processing Flow)

Dưới đây là luồng xử lý chi tiết đi qua các lớp kiến trúc mới khi nhận một request từ Client gửi kế hoạch hành động (`POST /api/match/actions`):

```text
[Client]
   |
   | (1) POST /api/match/actions (JSON)
   v
[MatchController] (Inbound REST Adapter)
   |
   | (2) Tiếp nhận Request & Validation cơ bản (Spring @Valid)
   | (3) Chuyển đổi HTTP Headers (X-Team-Name) và JSON sang Application DTO
   v
[SubmitActionsUseCase] (Inbound Port Interface)
   |
   | (4) Gọi hàm triển khai trong Application Service
   v
[MatchApplicationService] (Application Service)
   |
   | (5) Gọi Outbound Port: MatchStateStorePort.loadState() để lấy MatchState hiện tại
   | (6) Nhận về đối tượng MatchState (Aggregate Root) từ In-Memory Store
   | (7) Gọi ActionMapper để ánh xạ DTO thành Domain Value Objects (List<Action>)
   | (8) Ủy quyền kiểm tra hợp lệ cho Domain Service: ActionValidatorEngine.validate()
   | (9) Gọi Domain Service: MovementSimulator.simulateTeamTurn() thực hiện di chuyển,
   |     tiếp nhiên liệu (FuelManager), thu thập udon (UdonCollectionEngine).
   | (10) Cập nhật trạng thái mới trực tiếp trên Aggregate Root (MatchState)
   | (11) Gọi Outbound Port: MatchStateStorePort.saveState(matchState) để lưu lại trạng thái
   | (12) Ánh xạ kết quả mô phỏng (TurnSimulationResult) thành DTO phản hồi
   v
[MatchController]
   |
   | (13) Trả về HTTP Response (200 OK) kèm JSON kết quả
   v
[Client]
```

### Các quy tắc luồng dữ liệu cần ghi nhớ:
- Luồng dữ liệu đi vào hệ thống qua các **Inbound Adapters** rồi chuyển ngay thành **Application DTO**.
- Không được truyền trực tiếp đối tượng Domain Model ra ngoài API để tránh rò rỉ cấu trúc nghiệp vụ trong tương lai và giữ độc lập phiên bản API.
- Tầng **Domain Core** hoạt động hoàn toàn đồng bộ (Synchronous) và không có bất kỳ nhận thức nào về cơ chế lưu trữ (Database), giao diện (HTTP/REST) hay cơ chế lập lịch (Scheduler). Mọi điều phối đều do **Application Service** chịu trách nhiệm.

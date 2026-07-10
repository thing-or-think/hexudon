# 02. CẤU TRÚC GÓI (PACKAGE STRUCTURE)

## Mục lục
1. [Sơ đồ cây cấu trúc thư mục mục tiêu](#1-sơ-đồ-cây-cấu-trúc-thư-mục-mục-tiêu)
2. [Chi tiết các Package thuộc Tầng Domain (Domain Layer)](#2-chi-tiết-các-package-thuộc-tầng-domain-domain-layer)
3. [Chi tiết các Package thuộc Tầng Application (Application Layer)](#3-chi-tiết-các-package-thuộc-tầng-application-application-layer)
4. [Chi tiết các Package thuộc Tầng Adapter (Adapter Layer)](#4-chi-tiết-các-package-thuộc-tầng-adapter-adapter-layer)
5. [Chi tiết các Package thuộc Tầng Infrastructure (Infrastructure Layer)](#5-chi-tiết-các-package-thuộc-tầng-infrastructure-infrastructure-layer)

---

## 1. Sơ đồ cây cấu trúc thư mục mục tiêu

Dưới đây là sơ đồ cây đầy đủ của dự án sau khi refactor:

```text
src/main/java/com/naprock/hexudon/
│
├── domain/                                  # Tầng nghiệp vụ cốt lõi (Domain Core)
│   ├── model/                               # Thực thể nghiệp vụ (Entities) có định danh
│   │   ├── Agent.java
│   │   └── Team.java
│   ├── valueobject/                         # Đối tượng giá trị (Value Objects) bất biến
│   │   ├── Cell.java
│   │   ├── Road.java
│   │   ├── Spot.java
│   │   ├── Action.java
│   │   ├── ActionType.java
│   │   ├── AgentType.java
│   │   ├── MatchStatus.java
│   │   ├── TerrainType.java
│   │   ├── MatchConfig.java
│   │   ├── MatchState.java                 # Aggregate Root (lưu ý: chứa các Entities và Value Objects)
│   │   ├── AgentExecutionResult.java
│   │   └── TurnSimulationResult.java
│   ├── event/                               # Sự kiện nghiệp vụ (Domain Events)
│   ├── service/                             # Dịch vụ nghiệp vụ (Domain Services) chứa thuật toán
│   │   ├── MovementSimulator.java
│   │   ├── FuelManager.java
│   │   ├── TrafficCalculator.java
│   │   ├── ScoringEngine.java
│   │   ├── UdonCollectionEngine.java
│   │   ├── ActionValidatorEngine.java
│   │   ├── MapValidator.java
│   │   ├── HexGridUtils.java
│   │   └── TerrainGenerator.java
│   ├── repository/                          # Giao diện lưu trữ Aggregate Root (Repository Interfaces)
│   │   └── MatchStateRepository.java
│   └── exception/                           # Ngoại lệ nghiệp vụ thuần túy
│       ├── BusinessException.java
│       ├── GameRuleViolationException.java
│       ├── MatchStateConflictException.java
│       └── ResourceNotFoundException.java
│
├── application/                             # Tầng logic điều phối ứng dụng (Application Layer)
│   ├── port/                                # Các Cổng giao tiếp của Application
│   │   ├── in/                              # Inbound Ports (Use Cases định nghĩa dịch vụ vào)
│   │   │   ├── RegisterTeamUseCase.java
│   │   │   ├── StartMatchUseCase.java
│   │   │   ├── SubmitActionsUseCase.java
│   │   │   └── GetMatchStateUseCase.java
│   │   └── out/                             # Outbound Ports (Giao diện dịch vụ ra bên ngoài)
│   │       ├── MatchStateStorePort.java
│   │       └── MatchConfigLoaderPort.java
│   ├── service/                             # Application Services implement Inbound Ports
│   │   └── MatchApplicationService.java
│   ├── dto/                                 # Đối tượng DTO giao tiếp tầng Application
│   │   ├── ActionRequest.java
│   │   ├── ActionResponse.java
│   │   ├── AgentActionPlanRequest.java
│   │   ├── AgentActionPlanResponse.java
│   │   ├── AgentResponse.java
│   │   ├── CellResponse.java
│   │   ├── DayActionRequest.java
│   │   ├── DayActionResponse.java
│   │   ├── MatchStateResponse.java
│   │   ├── TeamActionRequest.java
│   │   ├── TeamActionResponse.java
│   │   ├── TeamRegisterRequest.java
│   │   └── TeamResponse.java
│   └── mapper/                              # Bộ chuyển đổi DTO sang Domain Models và ngược lại
│       └── ActionMapper.java
│
├── adapter/                                 # Tầng kết nối công nghệ bên ngoài (Adapter Layer)
│   ├── in/                                  # Adapters đầu vào (Inbound Adapters)
│   │   └── rest/                            # HTTP REST Controllers
│   │       └── MatchController.java
│   └── out/                                 # Adapters đầu ra (Outbound Adapters)
│       ├── persistence/                     # Quản lý lưu trữ thực tế (In-Memory hoặc DB)
│       │   └── InMemoryMatchStateRepository.java
│       ├── loader/                          # Đọc dữ liệu vật lý cấu hình
│       │   └── FileMatchConfigLoader.java
│       └── configuration/                   # Beans config để liên kết Port với Adapter
│           └── AdapterBeanConfig.java
│
├── infrastructure/                          # Tầng hạ tầng kỹ thuật và cấu hình Spring Boot
│   ├── configuration/                       # Cấu hình Spring (CORS, MVC, Web, Bean)
│   │   ├── AppConfig.java
│   │   └── WebConfig.java
│   ├── interceptor/                         # Interceptors lọc request hạ tầng
│   │   └── RateLimiterInterceptor.java
│   ├── scheduler/                           # Bộ lập lịch scheduler của Spring
│   │   └── SchedulerConfig.java
│   └── util/                                # Lớp tiện ích hạ tầng vật lý
│       └── FileUtils.java
│
└── HexudonApplication.java                  # File Spring Boot Bootstrapping
```

---

## 2. Chi tiết các Package thuộc Tầng Domain (Domain Layer)

Tầng Domain chứa toàn bộ tri thức nghiệp vụ cốt lõi, không chứa bất kỳ framework phụ thuộc nào ngoài Lombok.

### 2.1. `domain.model`
- **Vai trò:** Chứa các Entity (thực thể) có định danh và trạng thái thay đổi theo thời gian.
- **Chứa class:** `Agent`, `Team`.
- **Không được chứa:** Value Object, Domain Services, Use Cases, Controller, các thư viện database/Spring.
- **Được phép phụ thuộc:** `domain.valueobject`, `domain.exception`.
- **Không được phụ thuộc:** Toàn bộ các package ngoài `domain` (`application`, `adapter`, `infrastructure`).

### 2.2. `domain.valueobject`
- **Vai trò:** Chứa các đối tượng bất biến biểu diễn giá trị, định nghĩa bằng các thuộc tính của nó. Đặc biệt, `MatchState` là Aggregate Root quản lý trạng thái, chứa các Entity và Value Object.
- **Chứa class:** `Cell`, `Road`, `Spot`, `Action`, `ActionType`, `AgentType`, `MatchStatus`, `TerrainType`, `MatchConfig`, `MatchState`, `AgentExecutionResult`, `TurnSimulationResult`.
- **Không được chứa:** Entity có định danh thay đổi độc lập, Domain Services, Interfaces Repository.
- **Được phép phụ thuộc:** `domain.exception`.
- **Không được phụ thuộc:** Bất kỳ package nào nằm ngoài `domain`.

### 2.3. `domain.service`
- **Vai trò:** Chứa các Domain Service thực thi các thuật toán trò chơi và luật chơi. Các class này không lưu trạng thái (Stateless).
- **Chứa class:** `MovementSimulator`, `FuelManager`, `TrafficCalculator`, `ScoringEngine`, `UdonCollectionEngine`, `ActionValidatorEngine`, `MapValidator`, `HexGridUtils`, `TerrainGenerator`.
- **Không được chứa:** Thực thể lưu trạng thái, annotation `@Service` hay `@Component` của Spring.
- **Được phép phụ thuộc:** `domain.model`, `domain.valueobject`, `domain.exception`.
- **Không được phụ thuộc:** `domain.repository` (để tránh rò rỉ cơ chế lưu trữ), `application`, `adapter`, `infrastructure`.

### 2.4. `domain.repository`
- **Vai trò:** Định nghĩa các Interface lưu trữ và tìm kiếm Aggregate Root (`MatchState`).
- **Chứa class:** `MatchStateRepository` (Interface).
- **Không được chứa:** Class triển khai chi tiết (Implementation), annotation `@Repository` của Spring Boot.
- **Được phép phụ thuộc:** `domain.valueobject` (`MatchState`), `domain.model`.
- **Không được phụ thuộc:** Bất kỳ package nào nằm ngoài `domain`.

### 2.5. `domain.exception`
- **Vai trò:** Định nghĩa các Business Exception của trò chơi.
- **Chứa class:** `BusinessException`, `GameRuleViolationException`, `MatchStateConflictException`, `ResourceNotFoundException`.
- **Không được chứa:** HTTP Exception, `@ResponseStatus`, Global exception handler.
- **Được phép phụ thuộc:** Không phụ thuộc gì ngoài các thư viện chuẩn Java.
- **Không được phụ thuộc:** Các package ngoài `domain`.

---

## 3. Chi tiết các Package thuộc Tầng Application (Application Layer)

Tầng Application đóng vai trò điều phối luồng ứng dụng và các ca sử dụng.

### 3.1. `application.port.in`
- **Vai trò:** Định nghĩa các Inbound Port (Interface Use Cases) cung cấp giao tiếp đầu vào cho ứng dụng.
- **Chứa class:** `RegisterTeamUseCase`, `StartMatchUseCase`, `SubmitActionsUseCase`, `GetMatchStateUseCase`.
- **Không được chứa:** Implementations, DTO (DTO nên nằm ở `application.dto`).
- **Được phép phụ thuộc:** `application.dto`, `domain.valueobject`, `domain.model`.
- **Không được phụ thuộc:** Tầng `adapter`, `infrastructure`.

### 3.2. `application.port.out`
- **Vai trò:** Định nghĩa các Outbound Port (Interface) kết nối hạ tầng.
- **Chứa class:** `MatchStateStorePort`, `MatchConfigLoaderPort`.
- **Không được chứa:** Implementations, Spring Boot Configurations.
- **Được phép phụ thuộc:** `domain.valueobject` (`MatchState`, `MatchConfig`), `domain.model`.
- **Không được phụ thuộc:** Tầng `adapter`, `infrastructure`.

### 3.3. `application.service`
- **Vai trò:** Triển khai các Inbound Ports, điều phối việc gọi Repository, Domain Services và cập nhật Aggregate Root.
- **Chứa class:** `MatchApplicationService`.
- **Không được chứa:** REST Controller, logic nạp config từ file, annotation `@Service` (nếu cấu hình Java Bean tập trung ở Adapter).
- **Được phép phụ thuộc:** `application.port.in`, `application.port.out`, `application.dto`, `application.mapper`, `domain.*`.
- **Không được phụ thuộc:** `adapter.*`, `infrastructure.*`.

### 3.4. `application.dto` & `application.mapper`
- **Vai trò:** Chứa các đối tượng Record truyền nhận dữ liệu REST API và các mapper chuyển đổi dữ liệu.
- **Chứa class:** `ActionRequest`, `ActionResponse`, `ActionMapper`, v.v.
- **Không được chứa:** Nghiệp vụ game, logic Controller.
- **Được phép phụ thuộc:** `domain.model`, `domain.valueobject`.
- **Không được phụ thuộc:** Tầng `adapter`, `infrastructure`.

---

## 4. Chi tiết các Package thuộc Tầng Adapter (Adapter Layer)

Tầng Adapter kết nối hệ thống với các công nghệ bên ngoài (Spring Boot MVC, File System).

### 4.1. `adapter.in.rest`
- **Vai trò:** Tiếp nhận HTTP requests, điều hướng vào Inbound Ports của Application.
- **Chứa class:** `MatchController`.
- **Không được chứa:** Logic nghiệp vụ game, thuật toán mô phỏng di chuyển.
- **Được phép phụ thuộc:** `application.port.in`, `application.dto`, `application.mapper`.
- **Không được phụ thuộc:** `application.service` (phải giao tiếp qua Port), `adapter.out.*`, `domain.service`.

### 4.2. `adapter.out.persistence`
- **Vai trò:** Triển khai lưu trữ in-memory hoặc database cho `MatchStateStorePort` và `MatchStateRepository`.
- **Chứa class:** `InMemoryMatchStateRepository` (triển khai đồng thời `MatchStateStorePort` và `MatchStateRepository`).
- **Không được chứa:** Business logic.
- **Được phép phụ thuộc:** `application.port.out`, `domain.repository`, `domain.valueobject` (`MatchState`).
- **Không được phụ thuộc:** `adapter.in.*`, `application.service`.

### 4.3. `adapter.out.loader`
- **Vai trò:** Triển khai `MatchConfigLoaderPort` để đọc file từ ổ đĩa vật lý.
- **Chứa class:** `FileMatchConfigLoader`.
- **Không được chứa:** Triển khai database.
- **Được phép phụ thuộc:** `application.port.out`, `domain.valueobject` (`MatchConfig`), `infrastructure.util` (`FileUtils`).
- **Không được phụ thuộc:** Tầng domain model trực tiếp ngoài `MatchConfig`.

### 4.4. `adapter.out.configuration`
- **Vai trò:** Định nghĩa các Spring Configuration để đăng ký các bean của application service và adapter trong Application Context.
- **Chứa class:** `AdapterBeanConfig`.
- **Không được chứa:** Config Web MVC, CORS.
- **Được phép phụ thuộc:** `application.service`, `adapter.out.persistence`, `adapter.out.loader`.
- **Không được phụ thuộc:** `adapter.in.rest` trực tiếp.

---

## 5. Chi tiết các Package thuộc Tầng Infrastructure (Infrastructure Layer)

Tầng Infrastructure chứa cấu hình kỹ thuật của Spring Boot Framework, lập lịch, interceptor và các thư viện tiện ích dùng chung.

### 5.1. `infrastructure.configuration`
- **Vai trò:** Chứa cấu hình Web, CORS, Spring Scheduler.
- **Chứa class:** `AppConfig`, `WebConfig`.
- **Không được chứa:** Config khởi tạo Application Service Bean.
- **Được phép phụ thuộc:** `infrastructure.interceptor` (`RateLimiterInterceptor`).
- **Không được phụ thuộc:** Lớp Domain Core hay Use Case nghiệp vụ trực tiếp.

### 5.2. `infrastructure.interceptor`
- **Vai trò:** Chặn request để thực thi các tác vụ kỹ thuật như giới hạn tần suất (Rate Limiting).
- **Chứa class:** `RateLimiterInterceptor`.
- **Không được chứa:** Logic nghiệp vụ game.
- **Được phép phụ thuộc:** Các thư viện Spring Web Servlet, Java Standard Libraries.
- **Không được phụ thuộc:** `domain.*`, `application.service`.

### 5.3. `infrastructure.scheduler`
- **Vai trò:** Tự động điều phối chuyển ngày (`nextDay`) thông qua lập lịch thời gian thực.
- **Chứa class:** `SchedulerConfig`.
- **Không được chứa:** Thuật toán tính toán game.
- **Được phép phụ thuộc:** `application.port.in` (`StartMatchUseCase`, `SubmitActionsUseCase` hoặc interface quản lý lượt).
- **Không được phụ thuộc:** Lớp Domain Core directly hoặc details adapter persistence.

### 5.4. `infrastructure.util`
- **Vai trò:** Các tiện ích hệ thống dùng chung.
- **Chứa class:** `FileUtils`.
- **Được phép phụ thuộc:** Chỉ phụ thuộc vào Java Core Libraries.
- **Không được phụ thuộc:** Bất kỳ package nào của ứng dụng.

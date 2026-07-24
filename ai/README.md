# Modular Cooperative Multi-Agent Deep Q-Network (DQN) Framework

Dự án Java 21 triển khai kiến trúc **Clean Architecture / Hexagonal Architecture (Ports & Adapters)** cho thuật toán **Cooperative Multi-Agent Deep Q-Network (DQN)**. Framework hỗ trợ các hệ thống nhiều Agent tương tác trong môi trường phức tạp, tiêu biểu là tính năng môi trường **HexWorld**.

---

## 1. Project Overview

Framework DQN cung cấp giải pháp Reinforcement Learning (RL) độc lập, có khả năng tích hợp linh hoạt với nhiều môi trường mô phỏng khác nhau.

* **Thuần Java 21**: Tận dụng triệt để các tính năng hiện đại như Records, Sealed Interfaces, Pattern Matching, và Concurrency APIs.
* **Cooperative Multi-Agent RL (MARL)**: Quản lý và huấn luyện đồng thời nhiều loại Agent (ví dụ: `PatrolAgent`, `RefuelAgent`) phối hợp thực hiện nhiệm vụ chung.
* **Độc lập với Environment**: Core DQN Module hoàn toàn không bị ràng buộc bởi các quy tắc môi trường cụ thể.
* **HexWorld Domain**: Môi trường ô lục giác mô phỏng hành vi di chuyển, tiêu thụ nhiên liệu, thu thập tài nguyên (Udon) và tiếp nhiên liệu giữa các Agent.

### Sơ đồ phân tầng kiến trúc (Architectural Layers)

```text
Generic DQN Core
        │
        ▼
DQN Algorithm
        │
        ▼
Application Services / Ports
        │
        ▼
Feature Environment
        │
        ▼
HexWorld
```

---

## 2. Architecture

Hệ thống được tổ chức chặt chẽ theo các tầng của Clean Architecture:

```text
src/main/java/com/example/dqn
├── core                # Abstractions & nền tảng cốt lõi
├── algorithm/dqn       # Triển khai thuật toán DQN & Session Management
├── application         # Ports, Use Cases & Application Services
├── adapter             # Infrastructure Adapters (DJL, File Storage, Replay Buffer)
├── config              # Dependency Injection & Application Wiring
└── feature/hexworld    # Domain Environment cụ thể (HexWorld)
```

### Chi tiết vai trò các package:

#### `core`
Tập hợp các giao diện và lớp cơ sở nền tảng của Reinforcement Learning:
* `Agent`, `AgentId`, `AgentType`, `AgentRegistry`: Định danh và quản lý các đại lượng đại diện.
* `State`, `StateEncoder`, `StateSynchronizer`, `StateVersion`: Trạng thái môi trường và cơ chế mã hóa thành Tensor.
* `Action`, `ActionSpace`, `EnumActionSpace`: Không gian hành vi của Agent.
* `Environment`, `MultiAgentEnvironment`, `StepResult`: Giao diện tương tác môi trường.
* `Experience`, `AgentExperience`, `ReplayBuffer`, `MultiAgentReplayBuffer`: Lưu trữ trải nghiệm phục vụ Replay Sampling.
* `RewardCalculator`, `RewardProfile`, `RewardSignal`: Hệ thống tính toán điểm thưởng đa thành phần.
* `EpsilonSchedule`, `EpsilonProfile`: Quản lý chiến lược khám phá (Exploration strategy).
* `QNetwork`: Interface trừu tượng hóa mạng thần kinh nhân tạo.

#### `algorithm/dqn`
Chứa các thành phần thuật toán và điều phối mô phỏng:
* `DqnModule`: Façade chính cung cấp các API công khai cho Client.
* `DqnAgent`: Đóng gói Q-Network (Online & Target), Policy và State Encoder của một loại Agent.
* `DqnTrainer`: Thực hiện thuật toán Gradient Descent và cập nhật Target Network định kỳ.
* `session/`: Quản lý lifecycle huấn luyện chạy ngầm (`DqnTrainingSessionImpl`, `TrainingSessionWorker`, `StateSynchronizer`).
* `action/ActionCoordinator`: Lựa chọn hành động đồng thời cho tập hợp các Agent.
* `transition/LocalTransitionSimulator`: Mô phỏng bước chuyển trạng thái dự đoán nội bộ.
* `evolution/`: Động cơ tiến hóa thuật toán di truyền cho Reward Profile và Epsilon Schedule.

#### `application`
Ứng dụng Clean Architecture với kiến trúc Ports & Adapters:
* `port/in`: Các Use Case interfaces (`InitializeDqnModuleUseCase`, `RequestActionsUseCase`, `UpdateEnvironmentStateUseCase`, `StopDqnModuleUseCase`, `TrainAgentUseCase`, `EvaluateAgentUseCase`).
* `port/out`: Các Persistence interfaces (`ModelStore`, `TrainingMetricsStore`, `RewardProfileStore`, `EpsilonProfileStore`).
* `service`: Triển khai Use Cases (`DqnModuleService`, `ActionRequestService`, `EnvironmentStateUpdateService`, `TrainingService`, `EvolutionCoordinator`).

#### `adapter/out`
Triển khai hạ tầng kỹ thuật:
* `network/djl/DjlQNetwork`: Triển khai Deep Java Library (DJL) tích hợp engine PyTorch để huấn luyện Mạng Neural.
* `persistence/`: Lưu trữ Model checkpoint, Metrics và Profiles dạng File JSON/Text.
* `replay/InMemoryReplayBuffer`: Bộ đệm trải nghiệm lưu trữ trên RAM.

#### `feature/hexworld`
Môi trường thực tế minh họa:
* Domain Models: `HexMap`, `HexCell`, `HexPosition`, `TerrainType`, `TrafficLevel`, `UdonSpot`.
* Agents: `PatrolAgent`, `RefuelAgent`.
* Services: `HexMovement`, `MultiAgentInteractionService`, `RefuelService`, `UdonCollectionService`.

---

## 3. DQN Module Lifecycle

`DqnModule` được điều khiển thông qua một lifecycle chặt chẽ nhằm đảm bảo việc huấn luyện trực tuyến diễn ra liên tục trên Background Thread mà không làm nghẽn Main Thread của ứng dụng.

```text
DQN Module
│
├── initialize(initialState)
│       └── Tạo session + khởi tạo background training thread
│
├── requestActions()
│       ├── Đọc state hiện tại
│       ├── Chọn action cho từng agent (Epsilon-Greedy)
│       ├── Cập nhật predicted state
│       ├── Tạo experience
│       └── Train Q-Network (Background worker)
│
├── updateEnvironmentState(authoritativeState)
│       └── Thay thế predicted state bằng authoritative state từ môi trường thực
│
└── stop()
        └── Dừng background worker, ngắt thread & giải phóng tài nguyên
```

### Chi tiết các giai đoạn:

```text
initialize(initialState)
        │
        ▼
Create Training Session & StateSynchronizer
        │
        ▼
Start Background Training SessionWorker Thread
        │
        ▼
      READY
        │
        ▼
┌────────────────────────────────────────────────────────┐
│ Simulation / Execution Loop                            │
│                                                        │
│   requestActions()                                     │
│        │ (1. Đọc current state)                        │
│        │ (2. Chọn joint actions qua Policy)            │
│        │ (3. Dự đoán next state qua LocalSimulator)    │
│        │ (4. Tạo experience & đẩy vào ReplayBuffer)   │
│        │ (5. Background Thread chạy trainStep())       │
│        ▼                                               │
│   External Environment step(actions)                   │
│        │                                               │
│        ▼                                               │
│   updateEnvironmentState(authoritativeState)           │
│        │ (Đồng bộ lại state thực tế & sửa sai)         │
└────────────────────────────────────────────────────────┘
        │
        ▼
     stop()
        │
        ├── Stop worker thread
        └── Release DJL / Thread resources
```

---

## 4. Public API Usage

Mã nguồn Client/Application tương tác với `DQN Module` thông qua các Port Interfaces tiêu chuẩn:

```java
// 1. Khởi tạo các Use Case Services từ Session
DqnTrainingSession session = ...; // Đã bootstrap qua ApplicationConfig
InitializeDqnModuleUseCase initializeUseCase = new DqnModuleService(session);
RequestActionsUseCase requestActionsUseCase = new ActionRequestService(session);
UpdateEnvironmentStateUseCase updateStateUseCase = new EnvironmentStateUpdateService(session);
StopDqnModuleUseCase stopUseCase = new DqnModuleService(session);

// 2. Khởi tạo Module với trạng thái ban đầu
MultiAgentState initialState = environment.getInitialState();
initializeUseCase.initialize(initialState);

// 3. Vòng lặp mô phỏng
while (!environment.isDone()) {
    // Yêu cầu Module chọn hành động cho tất cả Agent
    List<AgentAction> actions = requestActionsUseCase.requestActions();

    // Chuyển đổi và thực thi hành động trên Môi trường thực tế
    Map<AgentId, Action> actionsMap = convertToMap(actions);
    MultiAgentStepResult result = environment.step(actionsMap);

    // Thu thập trạng thái chính thức từ Môi trường (Authoritative State)
    MultiAgentState authoritativeState = result.toMultiAgentState();

    // Cập nhật ngược lại cho DQN Module để đồng bộ trạng thái
    updateStateUseCase.updateEnvironmentState(authoritativeState);
}

// 4. Dừng Module khi hoàn tất
stopUseCase.stop();
```

---

## 5. Real Usage Example

Dự án cung cấp một file ví dụ thực tế hoàn chỉnh có thể chạy trực tiếp:

📍 **File path**: [`src/main/java/com/example/dqn/example/DqnModuleExample.java`](file:///d:/Documents/GitHub/v23-07-26/src/main/java/com/example/dqn/example/DqnModuleExample.java)

### Lệnh chạy ví dụ:

```bash
mvn exec:java "-Dexec.mainClass=com.example.dqn.example.DqnModuleExample"
```

Ví dụ này minh họa toàn bộ các bước:
1. Nạp cấu hình Reward Profile & Epsilon Profile.
2. Dựng bản đồ ô lục giác `HexWorld` với các địa hình (`ROAD`, `MOUNTAIN`, `POND`), mức độ kẹt xe (`TRAFFIC_JAM`), các điểm thưởng Udon và trạm khởi tạo.
3. Khởi tạo mạng neural DJL PyTorch cho `PatrolAgent` và `RefuelAgent`.
4. Kích hoạt `DqnTrainingSessionImpl` và `TrainingSessionWorker` chạy ngầm.
5. Chạy vòng lặp 5 bước mô phỏng: lấy hành động -> thực thi trên `HexWorld` -> cập nhật `authoritativeState` -> tự động tối ưu hóa mạng neural qua Replay Buffer.
6. Dừng dọn dẹp Thread an toàn.

---

## 6. Example Scenario (HexWorld)

Kịch bản mô phỏng trong `HexWorld` bao gồm 2 loại Agent cùng hợp tác:

```text
HexWorld Domain
│
├── PatrolAgent (Agent tuần tra)
│   ├── Di chuyển trên lưới hexagon (6 hướng)
│   ├── Tiêu thụ nhiên liệu (Fuel) phụ thuộc vào TerrainType & TrafficLevel
│   └── Thu thập tài nguyên Udon tại các điểm UdonSpot
│
└── RefuelAgent (Agent tiếp nhiên liệu)
    ├── Di chuyển trên lưới hexagon (không tiêu hao fuel)
    └── Tiếp nhiên liệu cho PatrolAgent khi đứng cùng ô HexPosition
```

### Phân tách trách nhiệm (Separation of Concerns):

```text
┌──────────────────────────────────────┐     ┌──────────────────────────────────────┐
│             DQN Module               │     │               HexWorld               │
├──────────────────────────────────────┤     ├──────────────────────────────────────┤
│ • Lựa chọn hành động (Policy)        │     │ • Validate luật di chuyển            │
│ • Dự đoán bước chuyển (Simulate)     │ ──► │ • Áp dụng game rules (Fuel/Udon)     │
│ • Xây dựng Experience (s, a, r, s')  │     │ • Tính toán va chạm & Refuel         │
│ • Huấn luyện Q-Network (Background)  │ ◄── │ • Trả về Authoritative State chính xác│
└──────────────────────────────────────┘     └──────────────────────────────────────┘
```

---

## 7. Predicted State vs Authoritative State

Hệ thống phân biệt rõ ràng giữa hai khái niệm Trạng thái:

### Predicted State (Trạng thái dự đoán)
* **Nguồn gốc**: Được tạo ra bởi `LocalTransitionSimulator` bên trong `TrainingSessionWorker` dựa trên trạng thái hiện tại và hành động đã chọn.
* **Mục đích**: Cho phép DQN Module lập tức tính toán chuyển giao $(s, a, r, s')$, tạo `AgentExperience` và đưa vào `ReplayBuffer` để huấn luyện trực tuyến không bị nghẽn.

### Authoritative State (Trạng thái chính thức)
* **Nguồn gốc**: Trả về bởi môi trường thực tế (`HexWorld` hoặc Server external).
* **Mục đích**: Sửa lỗi lệch dự đoán (Prediction divergence), đồng bộ hóa vị trí thực, lượng fuel thực, trạng thái tài nguyên đã thu thập và các tương tác vật lý phức tạp (như 2 Agent đụng độ hoặc được bơm xăng).

### Luồng xử lý phân nhánh & Sửa lệch (Divergence Resolution):

```text
        Sₜ (Trạng thái hiện tại)
         │
         ├── DQN selects Aₜ
         │
         ▼
┌──────────────────────────┐             ┌──────────────────────────┐
│ LocalTransitionSimulator │             │   External Environment   │
└──────────────────────────┘             └──────────────────────────┘
         │                                            │
         ▼                                            ▼
  Predicted Sₜ₊₁                              Authoritative Sₜ₊₁
         │                                            │
         ├── Build Experience & Train                 │
         │                                            │
         └─────────────► updateEnvironmentState() ◄───┘
                                   │
                                   ▼
                       Ghi đè & Sửa lệch State
```

---

## 8. Threading Model

Để đảm bảo hiệu năng và khả năng phản hồi thời gian thực, hệ thống áp dụng mô hình Đa luồng bất đồng bộ:

```text
Main / Application Thread
        │
        ├── initialize()              ──► Khởi tạo Worker Thread
        ├── requestActions()          ──► Lấy hành động nhanh từ Policy
        ├── updateEnvironmentState()  ──► Đẩy Authoritative State vào StateSynchronizer
        └── stop()                    ──► Gửi ngắt Thread & join()

Background Training Worker Thread (TrainingSessionWorker)
        │
        ├── Continuously polls StateSynchronizer.getAndClearNextState()
        ├── LocalTransitionSimulator.simulate()
        ├── ExperienceBuilder.build() ──► MultiAgentReplayBuffer.add()
        ├── DqnTrainer.trainStep()    ──► Cập nhật Weights của DjlQNetwork
        └── updatePredictedState()
```

### Cơ chế Thread-Safety:
* `StateSynchronizer`: Sử dụng `synchronized` blocks và cơ chế `wait()/notifyAll()` kết hợp với lớp `StateVersion` có đánh số phiên bản (`versionCounter`) để loại bỏ các bản cập nhật dự đoán cũ nếu có Authoritative State mới xuất hiện.
* `TrainingSessionWorker`: Chạy dưới dạng Daemon thread, cho phép ngắt an toàn với `workerThread.join(500)`.

---

## 9. Training Flow

Chi tiết luồng huấn luyện trong một chu kỳ DQN:

```text
State (s) ──► Policy (Epsilon-Greedy) ──► Action (a)
                                              │
                                              ▼
                                   Local Transition Simulation
                                              │
                                              ▼
Experience Builder ◄── Reward Calculator ◄── Step Result (s', r, done)
        │
        ▼
MultiAgentReplayBuffer
        │
        ▼ (Batch Sampling)
DqnTrainer.trainStep() ──► Loss Computation ──► Q-Network Update (DJL PyTorch)
```

Các thành phần tham gia:
* `DqnAgent`: Chứa cặp Mạng Online $Q(s, a; \theta)$ và Target $Q(s, a; \theta^-)$.
* `DqnTrainer`: Lấy ngẫu nhiên các mẫu mini-batch từ `ReplayBuffer`, tính MSE Loss giữa $Q(s, a)$ và $r + \gamma \max_{a'} Q_{target}(s', a')$, sau đó thực hiện Backpropagation qua PyTorch Engine.

---

## 10. Multi-Agent Training

Hệ thống triển khai mô hình **Centralized Training with Decentralized Execution (CTDE)**:

```text
Global Environment State (MultiAgentState)
        │
        ├───────────────────────────────┐
        ▼                               ▼
  PatrolAgent                     RefuelAgent
   ├── PatrolStateEncoder (38-dim) ├── RefuelStateEncoder (35-dim)
   ├── Patrol ActionSpace (7 actions)├── Refuel ActionSpace (7 actions)
   ├── Online & Target DjlQNetwork ├── Online & Target DjlQNetwork
   └── EpsilonGreedyPolicy         └── EpsilonGreedyPolicy
```

* `AgentNetworkRegistry`: Đăng ký và tra cứu các `DqnAgent` theo `AgentType`.
* `ActionCoordinator`: Tập hợp trạng thái của từng Agent, mã hóa qua `StateEncoder` tương ứng và sinh ra danh sách `AgentAction`.

---

## 11. Reward and Epsilon Evolution

Bên cạnh việc huấn luyện trọng số Mạng Neural bằng DQN, framework tích hợp thuật toán tiến hóa (Genetic Evolution) để tự động tối ưu hóa Hyperparameters:

```text
Training Sessions
       │
       ▼
Performance Statistics
       │
       ├── Reward Evolution Engine  ──► Đột biến hệ số Reward Profile
       │
       └── Epsilon Evolution Engine ──► Đột biến Epsilon Schedule (Decay/Min Epsilon)
```

### Phân biệt 3 cấp độ học tập:

1. **DQN Weight Learning**: Cập nhật trọng số liên tục của Q-Network thông qua Gradient Descent trên Replay Buffer.
2. **Reward Profile Evolution**: Tiến hóa định kỳ các hệ số điểm thưởng (Penalty kẹt xe, Bonus nhặt Udon, Penalty hết xăng) dựa trên kết quả Fitness Evaluation.
3. **Epsilon Schedule Evolution**: Tối ưu hóa tốc độ suy giảm khám phá ($\epsilon$-decay) để tìm ra sự cân bằng tối ưu giữa Exploration & Exploitation.

---

## 12. Project Structure

Cấu trúc thư mục rút gọn của dự án:

```text
com.example.dqn
├── adapter
│   ├── in.cli                  # CLI Interface runner (DqnCli)
│   └── out
│       ├── network.djl         # Deep Java Library PyTorch QNetwork
│       ├── persistence         # File Stores for Models, Metrics, Profiles
│       └── replay              # InMemoryReplayBuffer
├── algorithm.dqn
│   ├── action                  # ActionCoordinator & AgentAction
│   ├── agent                   # AgentNetworkRegistry & AgentPolicy
│   ├── evolution               # Reward & Epsilon Evolution Engines
│   ├── policy                  # EpsilonGreedyPolicy
│   ├── session                 # DqnTrainingSession & TrainingSessionWorker
│   ├── training                # DqnTrainer & TrainingBatch
│   └── transition              # LocalTransitionSimulator & ExperienceBuilder
├── application
│   ├── port.in                 # Use Case Interfaces
│   ├── port.out                # Persistence Interfaces
│   └── service                 # Application Services
├── config                      # ApplicationConfig IoC Wiring
├── core                        # Core RL abstractions (Agent, State, Action, Reward, Epsilon)
├── example                     # Runnable usage examples (DqnModuleExample)
└── feature.hexworld            # HexWorld Multi-Agent Environment domain & adapters
```

---

## 13. How to Run

### Yêu cầu môi trường:
* Java 21 LTS trở lên.
* Apache Maven 3.8+.

### Build dự án:
```bash
mvn clean compile
```

### Chạy ứng dụng chính (DQN CLI):
```bash
# Chạy huấn luyện mặc định (100 episodes)
mvn exec:java -Dexec.mainClass="com.example.dqn.DqnApplication"

# Chạy tiến hóa Reward Profile
mvn exec:java -Dexec.mainClass="com.example.dqn.DqnApplication" -Dexec.args="evolve-reward 50"

# Chạy tiến hóa Epsilon Schedule
mvn exec:java -Dexec.mainClass="com.example.dqn.DqnApplication" -Dexec.args="evolve-epsilon 50"

# Chạy đánh giá (Evaluation)
mvn exec:java -Dexec.mainClass="com.example.dqn.DqnApplication" -Dexec.args="evaluate 10"
```

### Chạy file ví dụ minh họa (`DqnModuleExample`):
```bash
mvn exec:java "-Dexec.mainClass=com.example.dqn.example.DqnModuleExample"
```

---

## 14. Implementation Status

Bảng đánh giá mức độ hoàn thiện của các thành phần dựa trên mã nguồn thực tế:

| Component | Status | Notes |
| :--- | :---: | :--- |
| **DQN Module (`DqnModule`)** | Completed | Façade hoàn chỉnh cung cấp các API `initialize`, `requestActions`, `updateEnvironmentState`, `stop`. |
| **Training Session (`DqnTrainingSessionImpl`)** | Completed | Quản lý Thread chạy ngầm, trạng thái vòng đời session (`RUNNING`, `STOPPED`). |
| **Multi-Agent Architecture** | Completed | Hỗ trợ đăng ký và xử lý song song nhiều Agent Type (`PATROL`, `REFUEL`). |
| **Replay Buffer (`InMemoryReplayBuffer`)** | Completed | Lưu trữ bộ đệm trải nghiệm đa Agent, hỗ trợ lấy mẫu ngẫu nhiên mini-batch. |
| **DJL Network (`DjlQNetwork`)** | Completed | Tích hợp Deep Java Library (DJL) với PyTorch Engine để huấn luyện Q-Network. |
| **Reward Evolution** | Completed | Có động cơ di truyền (`RewardEvolutionEngine`) đột biến và đánh giá fitness của Reward Profile. |
| **Epsilon Evolution** | Completed | Động cơ di truyền (`EpsilonEvolutionEngine`) tối ưu hóa lịch trình $\epsilon$-decay. |
| **HexWorld Environment** | Completed | Mô phỏng đầy đủ bản đồ lục giác, địa hình, kẹt xe, tiêu thụ nhiên liệu, Udon và Refuel. |
| **State Synchronization (`StateSynchronizer`)** | Completed | Quản lý đồng bộ hóa đa luồng, hỗ trợ cơ chế đánh số phiên bản `StateVersion` giải quyết lệch dự đoán. |

---
*Framework DQN được thiết kế và xây dựng tuân thủ nghiêm ngặt các nguyên tắc Clean Architecture và Clean Code trên nền tảng Java 21.*
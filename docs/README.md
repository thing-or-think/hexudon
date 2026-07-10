# DEVELOPMENT_ROADMAP.md

# Lộ trình phát triển HEXUDON Game Server

## Mục tiêu

HEXUDON Game Server được phát triển theo phương pháp **từ đơn giản đến hoàn chỉnh**. Mỗi giai đoạn đều tạo ra một phiên bản có thể chạy được trước khi bổ sung thêm chức năng mới.

Mục tiêu của cách phát triển này là:

* Dễ kiểm thử.
* Dễ phát hiện lỗi.
* Dễ mở rộng.
* Giữ kiến trúc ổn định trong suốt quá trình phát triển.

---

# Cấu trúc thư mục dự án

```text
hexudon-server/
│
├── docs/
│   ├── README.md
│   ├── ARCHITECTURE.md
│   ├── API_SPECS.md
│   ├── GAME_RULES.md
│   ├── DATA_MODEL.md
│   └── DEVELOPMENT_ROADMAP.md
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/naprock/hexudon/
│   │   │
│   │   ├── config/
│   │   │   ├── AppConfig.java
│   │   │   ├── WebConfig.java
│   │   │   └── SchedulerConfig.java
│   │   │
│   │   ├── controller/
│   │   │   └── MatchController.java
│   │   │
│   │   ├── manager/
│   │   │   └── MatchManager.java
│   │   │
│   │   ├── engine/
│   │   │   ├── MovementSimulator.java
│   │   │   ├── FuelManager.java
│   │   │   ├── TrafficCalculator.java
│   │   │   ├── ScoringEngine.java
│   │   │   └── HexGridUtils.java
│   │   │
│   │   ├── model/
│   │   │   ├── Agent.java
│   │   │   ├── PatrolAgent.java
│   │   │   ├── RefuelingAgent.java
│   │   │   ├── Team.java
│   │   │   ├── MatchState.java
│   │   │   ├── MatchConfig.java
│   │   │   ├── Cell.java
│   │   │   ├── Road.java
│   │   │   ├── Spot.java
│   │   │   ├── Action.java
│   │   │   └── Submission.java
│   │   │
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   └── response/
│   │   │
│   │   ├── loader/
│   │   │   └── MatchConfigLoader.java
│   │   │
│   │   ├── exception/
│   │   │   ├── GameRuleViolationException.java
│   │   │   └── GlobalExceptionHandler.java
│   │   │
│   │   ├── interceptor/
│   │   │   └── RateLimiterInterceptor.java
│   │   │
│   │   ├── util/
│   │   │   └── JsonUtils.java
│   │   │
│   │   └── HexudonApplication.java
│   │
│   └── resources/
│       ├── application.yml
│       ├── match_config.txt
│       └── logback.xml
│
├── src/test/
│
└── pom.xml
```

---

# Vai trò của từng package

## config

Chứa các lớp cấu hình của Spring Boot.

Ví dụ:

* AppConfig
* WebConfig
* SchedulerConfig

---

## controller

Cung cấp các REST API để client giao tiếp với server.

Ví dụ:

* Đăng ký đội.
* Gửi hành động.
* Lấy trạng thái trận đấu.

---

## manager

Là trung tâm điều phối toàn bộ trận đấu.

Manager chịu trách nhiệm:

* Khởi tạo trận đấu.
* Quản lý MatchState.
* Nhận Action.
* Gọi các Engine xử lý.
* Đồng bộ hóa dữ liệu.

Đây là thành phần quan trọng nhất của hệ thống.

---

## engine

Chứa toàn bộ logic của trò chơi.

Mỗi Engine chỉ xử lý một nhiệm vụ riêng.

Ví dụ:

* MovementSimulator
* FuelManager
* TrafficCalculator
* ScoringEngine
* HexGridUtils

Engine không xử lý HTTP và cũng không quản lý dữ liệu trực tiếp.

---

## model

Chứa toàn bộ dữ liệu của trò chơi.

Ví dụ:

* Agent
* Team
* Cell
* Road
* Spot
* MatchState
* Action
* Submission

Model chỉ biểu diễn dữ liệu, không chứa logic nghiệp vụ phức tạp.

---

## dto

Chứa các đối tượng dùng để trao đổi dữ liệu qua API.

Bao gồm:

* request/
* response/

DTO giúp tách biệt dữ liệu API và Model nội bộ.

---

## loader

Đọc và khởi tạo dữ liệu từ file cấu hình.

Ví dụ:

* MatchConfigLoader

---

## exception

Định nghĩa các ngoại lệ của hệ thống và xử lý lỗi tập trung.

Ví dụ:

* GameRuleViolationException
* GlobalExceptionHandler

---

## interceptor

Các thành phần xử lý trước hoặc sau khi request đi vào Controller.

Ví dụ:

* RateLimiterInterceptor

---

## util

Các lớp tiện ích dùng chung.

Ví dụ:

* JsonUtils

---

## resources

Chứa toàn bộ tài nguyên của ứng dụng.

Ví dụ:

* application.yml
* match_config.txt
* logback.xml

---

# Lộ trình phát triển

## Giai đoạn 1 – Khởi tạo Server

### Mục tiêu

Xây dựng phiên bản đầu tiên có thể hoạt động.

### Công việc

* Tạo project Spring Boot.
* Xây dựng cấu trúc package.
* Đọc file `match_config.txt`.
* Khởi tạo MatchState.
* Đăng ký đội.
* Khởi động trận đấu.
* API lấy trạng thái trận đấu.

### Kết quả

Server chạy được và client có thể xem trạng thái hiện tại.

---

## Giai đoạn 2 – Tiếp nhận hành động

### Mục tiêu

Cho phép client gửi lệnh.

### Công việc

* API Submit Action.
* Lưu Action.
* Kiểm tra định dạng.
* Kiểm tra luật chơi.
* Trả lỗi nếu Action không hợp lệ.

### Kết quả

Server có thể nhận lệnh nhưng chưa mô phỏng.

---

## Giai đoạn 3 – Mô phỏng

### Mục tiêu

Thực hiện các hành động của người chơi.

### Công việc

* Di chuyển Agent.
* Trừ nhiên liệu.
* Tiếp nhiên liệu.
* Thu thập Udon.
* Cập nhật MatchState.

### Kết quả

Server có thể mô phỏng hoàn chỉnh từng ngày thi đấu.

---

## Giai đoạn 4 – Hoàn thiện

### Mục tiêu

Hoàn thiện toàn bộ luật chơi.

### Công việc

* Tính giao thông.
* Tính điểm.
* Xếp hạng.
* Logging.
* Xử lý lỗi.
* Tối ưu hiệu năng.
* Hoàn thiện API.

### Kết quả

Server đáp ứng đầy đủ yêu cầu của đề bài.

---

# Thứ tự triển khai các package

Để giảm độ phức tạp, nên xây dựng theo thứ tự sau:

1. model
2. loader
3. manager
4. controller
5. dto
6. engine
7. exception
8. interceptor
9. util
10. config

Thứ tự này giúp tạo được một phiên bản chạy được sớm, sau đó bổ sung dần các tính năng và tối ưu hệ thống.

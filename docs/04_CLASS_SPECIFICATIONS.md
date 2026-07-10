# 04. ĐẶC TẢ CHI TIẾT CÁC LỚP (CLASS SPECIFICATIONS)

## Mục lục
1. [Lớp MatchState (Aggregate Root)](#1-lớp-matchstate-aggregate-root)
2. [Lớp Team (Entity)](#2-lớp-team-entity)
3. [Lớp Agent (Entity)](#3-lớp-agent-entity)
4. [Lớp MovementSimulator (Domain Service)](#4-lớp-movementsimulator-domain-service)
5. [Lớp FuelManager (Domain Service)](#5-lớp-fuelmanager-domain-service)
6. [Lớp ActionValidatorEngine (Domain Service)](#6-lớp-actionvalidatorengine-domain-service)
7. [Cổng SubmitActionsUseCase (Inbound Port)](#7-cổng-submitactionsusecase-inbound-port)
8. [Cổng MatchStateStorePort (Outbound Port)](#8-cổng-matchstatestoreport-outbound-port)
9. [Lớp MatchApplicationService (Application Service)](#9-lớp-matchapplicationservice-application-service)
10. [Lớp MatchController (Inbound REST Adapter)](#10-lớp-matchcontroller-inbound-rest-adapter)
11. [Lớp InMemoryMatchStateRepository (Outbound Persistence Adapter)](#11-lớp-inmemorymatchstaterepository-outbound-persistence-adapter)
12. [Lớp FileMatchConfigLoader (Outbound Loader Adapter)](#12-lớp-filematchconfigloader-outbound-loader-adapter)

---

## 1. Lớp MatchState (Aggregate Root)

### 1.1. Tổng quan
- **Vai trò:** Quản lý trạng thái vòng đời toàn bộ trận đấu game lục giác, là Aggregate Root của hệ thống.
- **Trách nhiệm:** Lưu giữ thông tin về các ô bản đồ (`Cell`), liên kết đường đi (`Road`), vị trí bánh Udon (`Spot`), trạng thái các đội chơi (`Team`), các điệp viên (`Agent`), lượt đấu hiện tại và điều phối việc đăng ký đội chơi.
- **Layer:** Domain
- **Package:** `com.naprock.hexudon.domain.valueobject` (hoặc `com.naprock.hexudon.domain.model` tùy thuộc cách phân loại).

### 1.2. Quan hệ
- **Ai gọi class này:** `MatchApplicationService`, `MovementSimulator`, `FuelManager`, `ScoringEngine`, `UdonCollectionEngine`, `MapValidator`.
- **Class này gọi ai:** `Team`, `Agent`, `Cell`, `Road`, `Spot`, `MatchStatus`, các exception trong `domain.exception`.
- **Không được gọi ai:** Tầng `application`, tầng `adapter`, tầng `infrastructure`.

### 1.3. Thuộc tính

| Thuộc tính | Kiểu dữ liệu | Ý nghĩa | Ghi chú |
| :--- | :--- | :--- | :--- |
| `status` | `MatchStatus` (Enum) | Trạng thái hiện tại của trận đấu. | `WAITING`, `PLAYING`, `FINISHED`. |
| `currentTurn` | `int` | Số lượt/ngày (Day) hiện tại. | Bắt đầu từ 0 (chưa chơi) đến `maxTurns`. |
| `turnStartTime` | `long` | Timestamp khi bắt đầu lượt hiện tại. | Đơn vị Mili giây. |
| `teams` | `List<Team>` | Danh sách các đội đã đăng ký. | Tối đa 2 đội chơi. |
| `cells` | `List<Cell>` | Lưới ô bản đồ lục giác. | Chứa tọa độ và loại địa hình. |
| `roads` | `List<Road>` | Danh sách liên kết giữa các ô. | Để xác định đường đi hợp lệ. |
| `spots` | `List<Spot>` | Các điểm chứa udon của bản đồ. | |
| `currentTurnActions` | `Map<String, Action>`| Lưu trữ tạm các hành động trong lượt của agent. | Key là Agent ID. |
| `cellIndex` | `Map<String, Cell>` | Chỉ mục tìm nhanh Cell dựa trên tọa độ. | Key định dạng `X_Y`. |

### 1.4. Phương thức

| Tên | Input | Output | Trách nhiệm |
| :--- | :--- | :--- | :--- |
| `registerTeam` | `Team` (team), `int` (maxTeams) | `void` | Đăng ký một đội chơi vào danh sách nếu trận đấu đang ở trạng thái WAITING. |
| `start` | `int` (maxFuel), `int` (maxSteps), `int` (initialUdon) | `void` | Chuyển trạng thái trận sang PLAYING, thiết lập turn = 1, khởi tạo tài nguyên cho các team và spot. |
| `ensurePlaying`| Không | `void` | Kiểm tra trạng thái trận đấu. Ném exception nếu trận đấu chưa bắt đầu hoặc đã kết thúc. |
| `requireTeam` | `String` (teamName) | `Team` | Lấy thông tin Team theo tên, ném exception ResourceNotFoundException nếu không có. |
| `addCell` | `Cell` (cell) | `void` | Thêm ô bản đồ vào danh sách và cập nhật chỉ mục cellIndex. |
| `getCell` | `int` (x), `int` (y) | `Cell` | Lấy đối tượng Cell tại tọa độ tương ứng từ index. |
| `clearTurnActions`| Không | `void` | Reset danh sách hành động tạm thời của lượt chơi. |

### 1.5. Luồng hoạt động
Khi khởi tạo trận đấu, `status` là `WAITING`. Các đội gọi `registerTeam` để đăng ký. Khi đủ đội hoặc được admin kích hoạt, `start` được gọi, thiết lập thời gian bắt đầu, nạp đầy bình nhiên liệu và số bước đi tối đa trong ngày cho tất cả Agents của các đội chơi, reset kho Udon của từng Spot. Trong quá trình chơi, `ensurePlaying` được gọi trước mỗi hành động của Client để đảm bảo an toàn trạng thái.

### 1.6. Quan hệ với Domain
- Là **Aggregate Root** quản lý toàn bộ vòng đời của `Team`, `Agent`, `Cell`, `Road`, `Spot` trong phạm vi Match. Mọi biến đổi trạng thái nghiệp vụ bắt buộc phải thực hiện thông qua lớp này.

### 1.7. Quy tắc thiết kế
- **Được phép:** Ném các Business Exception khi trạng thái trận đấu không hợp lệ hoặc vi phạm luật chơi. Chứa logic tự biến đổi dữ liệu nội bộ.
- **Không được phép:** Tự lưu dữ liệu của chính mình vào File hay Database. Không chứa annotation Spring hay cấu hình HTTP.

---

## 2. Lớp Team (Entity)

### 2.1. Tổng quan
- **Vai trò:** Thực thể đại diện cho một đội thi đấu trong trận đấu.
- **Trách nhiệm:** Quản lý thông tin định danh của đội, danh sách Agent thuộc đội, trạng thái vi phạm tần suất gửi tin, trạng thái nộp bài và tổng điểm Udon đã thu thập.
- **Layer:** Domain
- **Package:** `com.naprock.hexudon.domain.model`

### 2.2. Quan hệ
- **Ai gọi class này:** `MatchState`, `MovementSimulator`, `ScoringEngine`, `UdonCollectionEngine`, `MatchApplicationService`.
- **Class này gọi ai:** `Agent`, các exception trong `domain.exception`.
- **Không được gọi ai:** Tầng `application`, tầng `adapter`, tầng `infrastructure`.

### 2.3. Thuộc tính

| Thuộc tính | Kiểu dữ liệu | Ý nghĩa | Ghi chú |
| :--- | :--- | :--- | :--- |
| `teamName` | `String` | Tên của đội thi đấu. | Thuộc tính định danh (Identity). |
| `agents` | `List<Agent>` | Danh sách các điệp viên thuộc đội. | Tối đa 3 Agent (2 Patrol, 1 Refuel). |
| `disqualified` | `boolean` | Trạng thái đội có bị loại khỏi giải đấu hay không. | `true` nếu bị vi phạm nặng. |
| `spamViolationCount` | `int` | Số lần vi phạm rate limit. | Dùng để phạt hoặc truất quyền thi đấu. |
| `collectedUdon` | `int` | Tổng số bánh Udon đội chơi đã thu thập được. | Dùng làm điểm số xếp hạng. |
| `submittedPlan` | `boolean` | Đội chơi đã nộp kế hoạch hành động cho ngày hiện tại chưa. | Giúp Scheduler biết để chạy ngay simulator. |

### 2.4. Phương thức

| Tên | Input | Output | Trách nhiệm |
| :--- | :--- | :--- | :--- |
| `findAgentById`| `String` (id) | `Agent` | Tìm kiếm Agent trong đội theo ID. Trả về null nếu không thấy. |
| `requireAgent` | `String` (id) | `Agent` | Yêu cầu lấy Agent theo ID, ném ResourceNotFoundException nếu không thấy. |
| `resetTurnResources`| `int` (maxFuel), `int` (maxSteps) | `void` | Reset năng lượng, số bước đi của các Agent và đặt lại submittedPlan = false. |
| `ensureEligible`| Không | `void` | Ném exception nếu đội chơi đang bị trạng thái disqualified. |
| `addCollectedUdon`| `int` (amount) | `void` | Cộng dồn bánh Udon thu thập được. |
| `incrementSpamViolation`| Không | `void` | Tăng số lần vi phạm rate limit lên 1. |

### 2.5. Luồng hoạt động
Khi đội đăng ký thành công, object `Team` được tạo kèm 3 Agent xuất phát ở tọa độ (0,0). Vào mỗi ngày mới, `resetTurnResources` được gọi để sạc lại bình năng lượng và cấp quota bước đi cho Agents của đội. Khi Agent thu thập được Udon, `addCollectedUdon` được gọi để tăng điểm số. Nếu đội vi phạm rate limit (bị phát hiện ở tầng interceptor), `incrementSpamViolation` sẽ được kích hoạt.

### 2.6. Quan hệ với Domain
- Là **Domain Entity** chịu sự quản lý trực tiếp bởi Aggregate Root `MatchState`.

### 2.7. Quy tắc thiết kế
- **Được phép:** Tự quản lý trạng thái nội bộ của Agent thuộc quyền sở hữu của Team.
- **Không được phép:** Đăng ký trực tiếp với Spring Container. Không trực tiếp thay đổi trạng thái của các Team khác.

---

## 3. Lớp Agent (Entity)

### 3.1. Tổng quan
- **Vai trò:** Thực thể đại diện cho một điệp viên (Agent) trên lưới bản đồ.
- **Trách nhiệm:** Lưu giữ thông tin vị trí tọa độ, lượng nhiên liệu còn lại, số bước chân còn lại trong ngày, danh sách các địa điểm Spot đã ghé thăm trong ngày và danh sách hành động đã lập kế hoạch cho lượt đi hiện tại.
- **Layer:** Domain
- **Package:** `com.naprock.hexudon.domain.model`

### 3.2. Quan hệ
- **Ai gọi class này:** `Team`, `MovementSimulator`, `FuelManager`, `UdonCollectionEngine`, `MatchApplicationService`.
- **Class này gọi ai:** `Action`, `AgentType`, `Spot`, các exception trong `domain.exception`.
- **Không được gọi ai:** Tầng `application`, tầng `adapter`, tầng `infrastructure`.

### 3.3. Thuộc tính

| Thuộc tính | Kiểu dữ liệu | Ý nghĩa | Ghi chú |
| :--- | :--- | :--- | :--- |
| `id` | `String` | Mã định danh duy nhất của điệp viên. | Định dạng: `A` + số thứ tự tự tăng. |
| `type` | `AgentType` (Enum) | Loại điệp viên. | `PATROL` (Tuần tra/Thu thập), `REFUEL` (Tiếp xăng). |
| `posX` | `int` | Tọa độ X hiện tại của Agent. | |
| `posY` | `int` | Tọa độ Y hiện tại của Agent. | |
| `fuel` | `int` | Lượng nhiên liệu hiện có trong bình. | Không được vượt quá `maxFuel`. |
| `remainingSteps` | `int` | Số bước chân đi tối đa còn lại của ngày. | Không được âm. |
| `visitedSpotsToday` | `List<Spot>` | Các điểm Spot chứa Udon mà Agent đã lấy Udon trong ngày. | Tránh Agent lấy Udon nhiều lần tại một điểm trong một lượt. |
| `actions` | `List<Action>` | Danh sách các hành động đã lập kế hoạch cho ngày. | Bao gồm chuỗi các bước di chuyển hoặc chờ. |
| `action` | `Action` | Hành động đang được mô phỏng tại bước chạy hiện tại. | |

### 3.4. Phương thức

| Tên | Input | Output | Trách nhiệm |
| :--- | :--- | :--- | :--- |
| `consumeStep` | `int` (cost) | `void` | Trừ số bước đi còn lại, ném exception STEPS_LIMIT_EXCEEDED nếu không đủ. |
| `consumeFuel` | `int` (cost) | `void` | Trừ lượng xăng hiện có, ném exception AGENT_OUT_OF_FUEL nếu không đủ xăng. |
| `addVisitedSpotToday`| `Spot` (spot) | `void` | Ghi nhận Agent đã thu thập Udon tại Spot này trong ngày hôm nay. |
| `hasVisitedSpotToday`| `Spot` (spot) | `boolean` | Kiểm tra Agent đã lấy Udon tại Spot này trong ngày chưa. |
| `clearVisitedSpotsToday`| Không | `void` | Reset danh sách Spot đã ghé thăm khi sang ngày mới. |
| `clearAction` | Không | `void` | Đặt lại hành động hiện tại về null để sẵn sàng nhận lệnh mới. |

### 3.5. Luồng hoạt động
Trong quá trình mô phỏng một Turn, `MovementSimulator` sẽ duyệt qua từng hành động (`Action`) trong thuộc tính `actions` của Agent. Tại mỗi bước di chuyển, Agent tiêu hao nhiên liệu thông qua `consumeFuel` và tiêu hao lượt đi thông qua `consumeStep`. Nếu Agent là Patrol đi vào ô chứa Spot Udon, hệ thống kiểm tra `hasVisitedSpotToday` để quyết định cho thu hoạch Udon hay không.

### 3.6. Quan hệ với Domain
- Là **Domain Entity** thuộc quyền sở hữu của Entity `Team`, gián tiếp quản lý bởi Aggregate Root `MatchState`.

### 3.7. Quy tắc thiết kế
- **Được phép:** Chứa các phương thức kiểm tra và trừ tài nguyên của chính mình kèm ném exception.
- **Không được phép:** Tự cập nhật vị trí mà không qua kiểm tra địa hình của bản đồ ngoài đời thực (logic này phải do Domain Service điều phối).

---

## 4. Lớp MovementSimulator (Domain Service)

### 4.1. Tổng quan
- **Vai trò:** Dịch vụ nghiệp vụ mô phỏng quá trình di chuyển của các Agent trong lượt.
- **Trách nhiệm:** Duyệt và xử lý từng bước hành động của các Agent thuộc một đội trong ngày, thực thi việc thay đổi vị trí, phối hợp tiêu hao nhiên liệu và phối hợp tiếp xăng, thu thập bánh Udon.
- **Layer:** Domain
- **Package:** `com.naprock.hexudon.domain.service`

### 4.2. Quan hệ
- **Ai gọi class này:** `MatchApplicationService`.
- **Class này gọi ai:** `Team`, `Agent`, `MatchState`, `MatchConfig`, `FuelManager`, `UdonCollectionEngine`, `HexGridUtils`, `Cell`, `Road`, `Action`, `AgentExecutionResult`.
- **Không được gọi ai:** Tầng `application`, tầng `adapter`, tầng `infrastructure`.

### 4.3. Thuộc tính
- Không có thuộc tính trạng thái (Stateless).

### 4.4. Phương thức

| Tên | Input | Output | Trách nhiệm |
| :--- | :--- | :--- | :--- |
| `simulateTeamTurn` | `Team` (team), `MatchState` (state), `MatchConfig` (config), `FuelManager` (fuelMgr), `UdonCollectionEngine` (udonEng) | `List<AgentExecutionResult>` | Thực hiện mô phỏng toàn bộ chuỗi hành động tối đa 5 bước trong ngày của tất cả Agent thuộc đội chơi, trả về báo cáo kết quả chi tiết. |

### 4.5. Luồng hoạt động
Phương thức `simulateTeamTurn` lặp qua số bước tối đa (mặc định 5 bước). Trong mỗi bước, duyệt qua toàn bộ Agent của đội.
- Lấy hành động hiện tại tương ứng với số bước.
- Nếu hành động là `MOVE`: Tính khoảng cách tới ô đích (phải bằng 1 qua `HexGridUtils`). Kiểm tra ô đích có thông với ô hiện tại (`roads`). Tính chi phí nhiên liệu và bước đi theo địa hình ô đích. Gọi `consumeFuel` và `consumeStep` trên Agent. Cập nhật vị trí của Agent sang ô đích.
- Nếu hành động là `WAIT`: Giữ nguyên vị trí, trừ nhiên liệu tối thiểu theo quy định chờ.
- Sau khi tất cả Agent di chuyển trong bước hiện tại, gọi `fuelManager` để tự động tiếp xăng (nếu Refuel đứng chung ô với Patrol). Gọi `udonCollectionEngine` để Patrol tự động thu thập Udon từ Spot.
- Lưu lại lịch sử tọa độ và kết quả của bước đó vào danh sách trả về.

### 4.6. Quan hệ với Domain
- Là **Domain Service** thực thi logic nghiệp vụ phức tạp, điều phối nhiều Entity và Value Object liên quan đến cơ học chuyển động trò chơi.

### 4.7. Quy tắc thiết kế
- **Được phép:** Sử dụng các tiện ích tính toán khoảng cách lục giác. Đọc thuộc tính của MatchConfig.
- **Không được phép:** Lưu giữ bất kỳ biến trạng thái toàn cục nào trong instance (phải stateless). Không tương tác trực tiếp với cơ sở dữ liệu.

---

## 5. Lớp FuelManager (Domain Service)

### 5.1. Tổng quan
- **Vai trò:** Dịch vụ nghiệp vụ quản lý năng lượng và tiếp nhiên liệu.
- **Trách nhiệm:** Tính toán chi phí tiêu thụ năng lượng của Agent khi đi qua các loại địa hình khác nhau và thực hiện việc nạp đầy nhiên liệu cho Patrol Agent từ Refuel Agent khi họ gặp nhau trên cùng một ô bản đồ.
- **Layer:** Domain
- **Package:** `com.naprock.hexudon.domain.service`

### 5.2. Quan hệ
- **Ai gọi class này:** `MovementSimulator`.
- **Class này gọi ai:** `Agent`, `Cell`, `MatchConfig`, `TerrainType`.
- **Không được gọi ai:** Tầng `application`, tầng `adapter`, tầng `infrastructure`.

### 5.3. Thuộc tính
- Không có thuộc tính trạng thái (Stateless).

### 5.4. Phương thức

| Tên | Input | Output | Trách nhiệm |
| :--- | :--- | :--- | :--- |
| `calculateFuelCost`| `Cell` (target), `MatchConfig` (config) | `int` | Tính toán chi phí xăng cần tiêu thụ dựa vào loại địa hình của ô đích. |
| `autoRefuel` | `Team` (team), `MatchConfig` (config) | `void` | Quét tất cả Agent trong đội, nếu Refuel Agent đứng cùng ô với Patrol Agent thì tự động nạp đầy nhiên liệu (fuel = maxFuel) cho Patrol Agent đó. |

### 5.5. Luồng hoạt động
Trong mỗi bước mô phỏng, `MovementSimulator` gọi `calculateFuelCost` để biết cần trừ bao nhiêu xăng của Agent chuẩn bị di chuyển vào ô địa hình cụ thể (Plain, Road, Mountain). Khi kết thúc bước đó, `MovementSimulator` gọi `autoRefuel`. Hàm `autoRefuel` duyệt cặp chéo các Agent: nếu tìm thấy một `PATROL` Agent và một `REFUEL` Agent có cùng tọa độ `(posX, posY)`, nạp đầy bình nhiên liệu của `PATROL` Agent lên mức tối đa `maxFuel`.

### 5.6. Quan hệ với Domain
- Là **Domain Service** đóng vai trò hỗ trợ giải quyết bài toán nghiệp vụ năng lượng của điệp viên.

### 5.7. Quy tắc thiết kế
- **Được phép:** Đọc các tham số định mức năng lượng của `MatchConfig`.
- **Không được phép:** Đăng ký trực tiếp với Spring framework (hàm này hoàn toàn là Java thuần túy).

---

## 6. Lớp ActionValidatorEngine (Domain Service)

### 6.1. Tổng quan
- **Vai trò:** Dịch vụ nghiệp vụ kiểm tra tính hợp lệ của kế hoạch hành động.
- **Trách nhiệm:** Xác thực cấu trúc chuỗi hành động nộp lên từ các đội thi đấu trước khi chạy mô phỏng, đảm bảo số bước không vượt quá quy định, thứ tự bước liên tục từ 1 và tọa độ hành động không bị rỗng.
- **Layer:** Domain
- **Package:** `com.naprock.hexudon.domain.service`

### 6.2. Quan hệ
- **Ai gọi class này:** `MatchApplicationService`.
- **Class này gọi ai:** `Action`, `MatchConfig`, các exception trong `domain.exception`.
- **Không được gọi ai:** Tầng `application`, tầng `adapter`, tầng `infrastructure`.

### 6.3. Thuộc tính
- Không có thuộc tính trạng thái (Stateless).

### 6.4. Phương thức

| Tên | Input | Output | Trách nhiệm |
| :--- | :--- | :--- | :--- |
| `validate` | `Map<String, List<Action>>` (plans), `MatchConfig` (config) | `void` | Kiểm duyệt toàn bộ danh sách hành động nộp của tất cả Agent. Ném GameRuleViolationException nếu phát hiện sai sót. |

### 6.5. Luồng hoạt động
Khi Application Service tiếp nhận request gửi hành động, nó gọi `validate`.
- Kiểm tra xem số lượng Agent gửi kế hoạch có khớp hoặc không vượt quá số Agent quy định.
- Với mỗi Agent: danh sách hành động không được rỗng, số bước không vượt quá `maxStepsPerTurn`.
- Kiểm tra thứ tự thuộc tính `order` của từng `Action` phải chạy liên tục từ 1 đến hết.
- Nếu hành động là `MOVE`: hai thuộc tính tọa độ đích `targetX` và `targetY` không được phép null.
- Nếu phát hiện bất kỳ lỗi nào, ném ngay `GameRuleViolationException` kèm mã lỗi tương ứng để hệ thống dừng xử lý và báo về client.

### 6.6. Quan hệ với Domain
- Là **Domain Service** chịu trách nhiệm bảo vệ tính toàn vẹn của dữ liệu đầu vào theo đúng luật chơi trò chơi.

### 6.7. Quy tắc thiết kế
- **Được phép:** Kiểm tra tính logic của dữ liệu đầu vào.
- **Không được phép:** Thay đổi dữ liệu trạng thái trận đấu trong khi validate.

---

## 7. Cổng SubmitActionsUseCase (Inbound Port)

### 7.1. Tổng quan
- **Vai trò:** Cổng vào (Inbound Port) biểu diễn ca sử dụng gửi kế hoạch hành động và mô phỏng lượt chơi.
- **Trách nhiệm:** Định nghĩa giao diện hợp đồng nghiệp vụ để các Adapter bên ngoài (REST API) tương tác với lõi hệ thống.
- **Layer:** Application
- **Package:** `com.naprock.hexudon.application.port.in`

### 7.2. Quan hệ
- **Ai gọi class này:** `MatchController`.
- **Class này gọi ai:** Không gọi ai (đây là Interface).
- **Không được gọi ai:** Bất kỳ ai.

### 7.3. Thuộc tính
- Không có thuộc tính (đây là Interface).

### 7.4. Phương thức

| Tên | Input | Output | Trách nhiệm |
| :--- | :--- | :--- | :--- |
| `submitActions`| `String` (teamName), `int` (day), `Map<String, List<Action>>` (plans) | `TurnSimulationResult` | Tiếp nhận và thực thi mô phỏng lượt chơi của đội dựa trên kế hoạch nộp lên. |

### 7.5. Luồng hoạt động
Không có luồng hoạt động trực tiếp vì đây là Interface. Đối tượng thực tế chạy sẽ là `MatchApplicationService` triển khai nó.

### 7.6. Quan hệ với Domain
- Là cổng giao tiếp thuộc tầng Application, gián tiếp đưa dữ liệu vào xử lý trong Domain Core.

### 7.7. Quy tắc thiết kế
- **Được phép:** Định nghĩa các tham số đầu vào và đầu ra sử dụng các đối tượng Value Object của Domain hoặc Application DTO.
- **Không được phép:** Triển khai mã nguồn chi tiết hay phụ thuộc vào Spring annotation.

---

## 8. Cổng MatchStateStorePort (Outbound Port)

### 8.1. Tổng quan
- **Vai trò:** Cổng ra (Outbound Port) định nghĩa cơ chế lưu trữ và nạp trạng thái trận đấu.
- **Trách nhiệm:** Cho phép tầng Application yêu cầu lưu trữ trạng thái trận đấu mà không cần quan tâm dữ liệu được ghi vào RAM, SQL database hay File.
- **Layer:** Application
- **Package:** `com.naprock.hexudon.application.port.out`

### 8.2. Quan hệ
- **Ai gọi class này:** `MatchApplicationService`.
- **Class này gọi ai:** Không gọi ai (Interface).
- **Không được gọi ai:** Bất kỳ ai.

### 8.3. Thuộc tính
- Không có thuộc tính.

### 8.4. Phương thức

| Tên | Input | Output | Trách nhiệm |
| :--- | :--- | :--- | :--- |
| `loadState` | Không | `MatchState` | Nạp trạng thái trận đấu hiện tại từ bộ lưu trữ. |
| `saveState` | `MatchState` (state) | `void` | Ghi đè trạng thái trận đấu mới vào bộ lưu trữ. |

### 8.5. Luồng hoạt động
Không có luồng hoạt động trực tiếp. Lớp Adapter `InMemoryMatchStateRepository` ở ngoài cùng sẽ hiện thực hóa các phương thức này.

### 8.6. Quy tắc thiết kế
- **Được phép:** Định nghĩa hợp đồng lưu trữ Aggregate Root.
- **Không được phép:** Phụ thuộc vào thư viện cụ thể như Hibernate hay JDBC.

---

## 9. Lớp MatchApplicationService (Application Service)

### 9.1. Tổng quan
- **Vai trò:** Dịch vụ ứng dụng (Application Service) thực thi các ca sử dụng của hệ thống Hexudon.
- **Trách nhiệm:** Thực thi tất cả các Inbound Ports (`RegisterTeamUseCase`, `StartMatchUseCase`, `SubmitActionsUseCase`, `GetMatchStateUseCase`). Điều phối vòng đời trận đấu bằng cách tương tác với các Outbound Ports để lấy dữ liệu, gọi Domain Services xử lý, cập nhật trạng thái Aggregate Root và lưu lại trạng thái mới.
- **Layer:** Application
- **Package:** `com.naprock.hexudon.application.service`

### 9.2. Quan hệ
- **Ai gọi class này:** `MatchController`, `SchedulerConfig`.
- **Class này gọi ai:** `MatchStateStorePort`, `MatchConfigLoaderPort`, `ActionValidatorEngine`, `MovementSimulator`, `FuelManager`, `UdonCollectionEngine`, `MatchState`, `Team`, `Agent`.
- **Không được gọi ai:** Tầng `adapter` (như `MatchController` hay `InMemoryMatchStateRepository` trực tiếp), tầng `infrastructure`.

### 9.3. Thuộc tính

| Thuộc tính | Kiểu dữ liệu | Ý nghĩa | Ghi chú |
| :--- | :--- | :--- | :--- |
| `stateStorePort` | `MatchStateStorePort` | Cổng lưu trữ trạng thái. | Được tiêm qua Constructor. |
| `configLoaderPort`| `MatchConfigLoaderPort` | Cổng tải file cấu hình. | Được tiêm qua Constructor. |
| `actionValidator` | `ActionValidatorEngine` | Động cơ kiểm tra hành động. | Khởi tạo trực tiếp hoặc tiêm. |
| `movementSimulator`| `MovementSimulator` | Bộ mô phỏng di chuyển. | Khởi tạo trực tiếp hoặc tiêm. |
| `fuelManager` | `FuelManager` | Bộ quản lý xăng. | Khởi tạo trực tiếp hoặc tiêm. |
| `udonEngine` | `UdonCollectionEngine` | Bộ xử lý udon. | Khởi tạo trực tiếp hoặc tiêm. |

### 9.4. Phương thức

| Tên | Input | Output | Trách nhiệm |
| :--- | :--- | :--- | :--- |
| `registerTeam` | `String` (teamName) | `Team` | Lấy MatchState hiện có, gọi logic đăng ký đội, lưu lại trạng thái mới. |
| `startMatch` | Không | `void` | Gọi nạp cấu hình thông qua configLoaderPort, khởi tạo trạng thái bắt đầu và lưu lại. |
| `submitActions`| `String` (teamName), `int` (day), `Map<String, List<Action>>` (plans) | `TurnSimulationResult` | Lấy MatchState, validate kế hoạch hành động, chạy mô phỏng di chuyển, xăng dầu, udon của đội chơi, lưu lại state và trả kết quả. |
| `getMatchState`| Không | `MatchState` | Tải trạng thái trận đấu hiện tại từ stateStorePort và trả về. |
| `nextDay` | Không | `void` | Tiến hành tăng ngày, sạc lại tài nguyên Agents, reset kho udon và ghi đè trạng thái. |

### 9.5. Luồng hoạt động
Khi thực thi `submitActions`:
1. Gọi `stateStorePort.loadState()` lấy `MatchState`.
2. Gọi `configLoaderPort.loadConfig()` lấy cấu hình trận đấu `MatchConfig`.
3. Kiểm tra xem trận đấu đang diễn ra thông qua `matchState.ensurePlaying()`.
4. Tìm và kiểm tra điều kiện đội thi đấu qua `matchState.requireTeam(teamName)`.
5. Gọi `actionValidator.validate()` để kiểm duyệt kế hoạch hành động.
6. Thiết lập kế hoạch vào các Agent của đội.
7. Gọi `movementSimulator.simulateTeamTurn()` để bắt đầu chạy mô phỏng di chuyển thực tế của các Agent, tự động tiếp xăng và thu hoạch bánh Udon.
8. Đánh dấu đội chơi đã hoàn tất nộp bài bằng cách đặt cờ `team.setSubmittedPlan(true)`.
9. Lưu trạng thái sửa đổi bằng cách gọi `stateStorePort.saveState(matchState)`.
10. Trả về đối tượng `TurnSimulationResult`.

### 9.6. Quan hệ với Domain
- Hoạt động bên ngoài Domain Core, điều phối các Domain Service và Aggregate Root cập nhật trạng thái nhưng không trực tiếp thực hiện thuật toán nghiệp vụ.

### 9.7. Quy tắc thiết kế
- **Được phép:** Điều phối nghiệp vụ, ném các exception nghiệp vụ.
- **Không được phép:** Chứa thuật toán mô phỏng di chuyển hay tính xăng trực tiếp. Không sử dụng các class cụ thể của tầng Adapter.

---

## 10. Lớp MatchController (Inbound REST Adapter)

### 10.1. Tổng quan
- **Vai trò:** Bộ chuyển đổi đầu vào (Inbound REST Adapter) tiếp nhận các yêu cầu HTTP.
- **Trách nhiệm:** Định nghĩa các Endpoint REST API của game server, xác thực định dạng dữ liệu đầu vào (Spring Web Validation), chuyển đổi dữ liệu HTTP (Headers, JSON Body) thành các Application DTO và gọi Use Cases tương ứng.
- **Layer:** Adapter (In)
- **Package:** `com.naprock.hexudon.adapter.in.rest`

### 10.2. Quan hệ
- **Ai gọi class này:** Spring MVC Container (khi nhận HTTP request từ Client).
- **Class này gọi ai:** `RegisterTeamUseCase`, `StartMatchUseCase`, `SubmitActionsUseCase`, `GetMatchStateUseCase`, `ActionMapper`, các DTO của tầng Application.
- **Không được gọi ai:** `MatchApplicationService` trực tiếp (phải qua Interface Port), tầng domain model trực tiếp (không dùng Entity làm request/response body).

### 10.3. Thuộc tính

| Thuộc tính | Kiểu dữ liệu | Ý nghĩa | Ghi chú |
| :--- | :--- | :--- | :--- |
| `registerTeamUseCase` | `RegisterTeamUseCase` | Use case đăng ký đội chơi. | Tiêm qua Constructor. |
| `startMatchUseCase` | `StartMatchUseCase` | Use case bắt đầu trận đấu. | Tiêm qua Constructor. |
| `submitActionsUseCase`| `SubmitActionsUseCase`| Use case nộp hành động. | Tiêm qua Constructor. |
| `getMatchStateUseCase`| `GetMatchStateUseCase`| Use case truy vấn trạng thái. | Tiêm qua Constructor. |
| `actionMapper` | `ActionMapper` | Chuyển đổi dữ liệu DTO/Domain. | Tiêm qua Constructor. |

### 10.4. Phương thức

| Tên | Input | Output | Trách nhiệm |
| :--- | :--- | :--- | :--- |
| `registerTeam` | `TeamRegisterRequest` | `TeamResponse` | Endpoint `POST /api/match/register`. Nhận thông tin đăng ký đội, gọi usecase đăng ký và trả kết quả. |
| `startMatch` | Không | `void` | Endpoint `POST /api/match/start`. Gọi usecase bắt đầu trận đấu. |
| `getMatchState`| Không | `MatchStateResponse` | Endpoint `GET /api/match/state`. Trả trạng thái hiện tại của trận đấu. |
| `submitActions`| `String` (X-Team-Name Header), `DayActionRequest` (Body) | `DayActionResponse` | Endpoint `POST /api/match/actions`. Nhận kế hoạch, gọi usecase chạy mô phỏng và trả kết quả chi tiết. |

### 10.5. Luồng hoạt động
Khi Client gửi request `POST /api/match/actions`:
- Spring Boot đón nhận request, kiểm tra header `X-Team-Name` có tồn tại không.
- Kiểm tra dữ liệu Body khớp định dạng `DayActionRequest`.
- Chuyển đổi thông tin sang Domain actions map bằng cách gọi `actionMapper.toDomainActionPlanMap()`.
- Gọi hàm xử lý của Port `submitActionsUseCase.submitActions()`.
- Nhận về `TurnSimulationResult` từ Application, tiếp tục gọi `actionMapper.toDayActionResponse()` để đóng gói thành DTO đầu ra.
- Trả về HTTP Status 200 OK kèm JSON response.

### 10.6. Quy tắc thiết kế
- **Được phép:** Khai báo Spring MVC Annotations (`@RestController`, `@PostMapping`, `@GetMapping`, `@RequestHeader`, `@RequestBody`, `@Valid`).
- **Không được phép:** Chứa bất kỳ lô-gích nghiệp vụ game nào. Không tự ý bỏ qua cổng Port để gọi trực tiếp các Service hay Model.

---

## 11. Lớp InMemoryMatchStateRepository (Outbound Persistence Adapter)

### 11.1. Tổng quan
- **Vai trò:** Bộ chuyển đổi đầu ra lưu trữ (Outbound Persistence Adapter).
- **Trách nhiệm:** Hiện thực hóa các phương thức lưu trữ của cổng `MatchStateStorePort` và `MatchStateRepository` bằng cách ghi dữ liệu trực tiếp lên bộ nhớ RAM (In-Memory) để đảm bảo tốc độ phản hồi tối ưu trong quá trình thi đấu.
- **Layer:** Adapter (Out)
- **Package:** `com.naprock.hexudon.adapter.out.persistence`

### 11.2. Quan hệ
- **Ai gọi class này:** `MatchApplicationService` (thông qua Port Interface).
- **Class này gọi ai:** `MatchState`.
- **Không được gọi ai:** Tầng `application` services, REST controllers.

### 11.3. Thuộc tính

| Thuộc tính | Kiểu dữ liệu | Ý nghĩa | Ghi chú |
| :--- | :--- | :--- | :--- |
| `matchState` | `MatchState` | Biến in-memory duy nhất lưu trữ trạng thái trận đấu hiện tại. | Trực quan hóa thay thế DB. |

### 11.4. Phương thức

| Tên | Input | Output | Trách nhiệm |
| :--- | :--- | :--- | :--- |
| `loadState` | Không | `MatchState` | Trả về tham chiếu đối tượng `MatchState` đang lưu trong bộ nhớ. |
| `saveState` | `MatchState` (state) | `void` | Ghi đè tham chiếu trạng thái trận đấu mới vào biến tĩnh/nội bộ. |

### 11.5. Luồng hoạt động
Đối tượng này được khởi tạo duy nhất một lần (Singleton) khi ứng dụng chạy. Khi `loadState` được gọi, trả về đối tượng `matchState` duy nhất hiện tại. Khi `saveState` được gọi, ghi đè trạng thái mới lên biến chứa. Dữ liệu này sẽ mất hoàn toàn khi hệ thống bị tắt hoặc khởi động lại.

### 11.6. Quy tắc thiết kế
- **Được phép:** Quản lý cơ chế đồng bộ dữ liệu bằng `synchronized` (nếu cần) để tránh xung đột luồng khi scheduler và API tương tác cùng lúc.
- **Không được phép:** Đưa logic kiểm tra luật chơi vào trong adapter này.

---

## 12. Lớp FileMatchConfigLoader (Outbound Loader Adapter)

### 12.1. Tổng quan
- **Vai trò:** Bộ chuyển đổi đầu ra đọc tệp tin cấu hình (Outbound Config Loader Adapter).
- **Trách nhiệm:** Hiện thực hóa cổng `MatchConfigLoaderPort` để đọc, phân tích tệp cấu hình văn bản `match_config.txt` từ thư mục tài nguyên và trả về đối tượng cấu hình nghiệp vụ `MatchConfig` cho hệ thống.
- **Layer:** Adapter (Out)
- **Package:** `com.naprock.hexudon.adapter.out.loader`

### 12.2. Quan hệ
- **Ai gọi class này:** `MatchApplicationService` (thông qua Port Interface).
- **Class này gọi ai:** `MatchConfig`, `FileUtils`, `ConfigLoadException`.
- **Không được gọi ai:** Tầng domain model trực tiếp (ngoại trừ `MatchConfig`).

### 12.3. Thuộc tính

| Thuộc tính | Kiểu dữ liệu | Ý nghĩa | Ghi chú |
| :--- | :--- | :--- | :--- |
| `configFilePath` | `String` | Đường dẫn đến tệp tin cấu hình vật lý. | Mặc định trỏ vào `match_config.txt`. |

### 12.4. Phương thức

| Tên | Input | Output | Trách nhiệm |
| :--- | :--- | :--- | :--- |
| `loadConfig` | Không | `MatchConfig` | Đọc file cấu hình thông qua FileUtils, phân tích cú pháp từng dòng và ánh xạ thành đối tượng MatchConfig. |

### 12.5. Luồng hoạt động
Khi được gọi, `loadConfig` yêu cầu `FileUtils` đọc toàn bộ nội dung file cấu hình dưới dạng text. Tiếp tục phân tách các dòng theo định dạng key-value (ví dụ: `map.width=20`). Nếu dòng nào thiếu hoặc sai định dạng số, ném `ConfigLoadException`. Nếu nạp đầy đủ, khởi tạo và trả về đối tượng `MatchConfig` chứa toàn bộ cấu hình trận đấu.

### 12.6. Quy tắc thiết kế
- **Được phép:** Bắt các ngoại lệ I/O của Java và bọc lại thành domain ConfigLoadException.
- **Không được phép:** Chứa giá trị mặc định của cấu hình cứng nhắc (các giá trị mặc định phải được định nghĩa trong tài liệu hoặc cấu trúc fallback của file nạp).

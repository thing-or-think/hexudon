# 04. ĐẶC TẢ CHI TIẾT CÁC LỚP (CLASS SPECIFICATIONS)

## Giải trình mâu thuẫn giữa Class List ban đầu và Build Order

Sau khi đối chiếu giữa `03_CLASS_LIST.md` ban đầu và lộ trình `10_BUILD_ORDER.md` (được coi là nguồn chuẩn thông tin), chúng tôi phát hiện một số mâu thuẫn sau:
1. **Các lớp chưa có mã nguồn:** `ScoringEngine` và `TrafficCalculator` được đề xuất trong danh sách ban đầu là các Domain Service. Tuy nhiên, theo `BUILD_ORDER.md`, dự án hiện tại chưa triển khai mã nguồn cho hai lớp này (chỉ là các stub/placeholder và chưa có logic thực tế trong project). Do đó, để tuân thủ nguyên tắc không viết thêm class mới và giữ nguyên 100% logic hiện tại, hai lớp này sẽ không được đưa vào đặc tả chi tiết bên dưới.
2. **Lớp Submission:** `Submission` từng được đề xuất là một Value Object độc lập. Tuy nhiên, `BUILD_ORDER.md` chỉ di chuyển các thực thể hiện hữu là `Action` và danh sách hành động trực tiếp thuộc `Agent` / `Team` mà không tạo thêm tệp `Submission.java`. Kế hoạch nộp của đội chơi được đại diện trực tiếp bằng `Map<String, List<Action>>` (hoặc `DayActionRequest` ở API DTO). Do đó, lớp `Submission` sẽ không được tạo thêm và không xuất hiện trong tài liệu này.

Dưới đây là đặc tả chi tiết cho tất cả các lớp thực tế theo đúng lộ trình refactor của `10_BUILD_ORDER.md`.

---
# Agent

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.model
- Vai trò: Thực thể điệp viên hoạt động trên lưới bản đồ.
- Trách nhiệm: Quản lý vị trí tọa độ, năng lượng, số bước đi còn lại, danh sách địa điểm đã ghé thăm trong ngày và kế hoạch hành động.
- Phạm vi sử dụng: Sử dụng trong nội bộ Team, các Domain Services và Application Services.
- Quan hệ với các class khác: Thuộc sở hữu của Team, chứa danh sách các Action.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `nextId` | `int` | Biến static để tự động sinh mã số Agent tiếp theo. | 1 | Có |
| `id` | `String` | ID định danh duy nhất của Agent. | null | Có |
| `type` | `AgentType` | Loại Agent (PATROL hoặc REFUEL). | null | Có |
| `posX` | `int` | Tọa độ X hiện tại của Agent. | 0 | Có |
| `posY` | `int` | Tọa độ Y hiện tại của Agent. | 0 | Có |
| `fuel` | `int` | Lượng nhiên liệu còn lại của Agent. | 0 | Có |
| `remainingSteps` | `int` | Số bước đi tối đa còn lại trong ngày. | 0 | Có |
| `visitedSpotsToday` | `List<Spot>` | Danh sách các Spot Udon đã thu thập trong ngày. | [] | Có |
| `actions` | `List<Action>` | Kế hoạch hành động nộp cho ngày hiện tại. | [] | Có |
| `action` | `Action` | Hành động hiện tại đang mô phỏng. | null | Không |

## 3. Constructor
- Constructor mặc định: Không có tham số. Tự động gán id bằng cách nối chuỗi 'A' với nextId và tăng nextId lên 1.
- Constructor đầy đủ: Nhận type, posX, posY. Tự động sinh id và gán type, posX, posY.
- Constructor nạp xăng: Nhận type, posX, posY, fuel. Tự động sinh id và gán các giá trị tương ứng.

## 4. Getter / Setter
- getId(): Lấy ID của Agent (Bất biến, không có setter).
- getType() / setType(AgentType): Lấy/đặt loại Agent.
- getPosX() / setPosX(int): Lấy/đặt tọa độ X.
- getPosY() / setPosY(int): Lấy/đặt tọa độ Y.
- getFuel() / setFuel(int): Lấy/đặt nhiên liệu.
- getRemainingSteps() / setRemainingSteps(int): Lấy/đặt số bước đi còn lại.
- getVisitedSpotsToday(): Lấy danh sách Spot đã ghé thăm trong ngày.
- getActions() / setActions(List<Action>): Lấy/đặt danh sách hành động.
- getAction() / setAction(Action): Lấy/đặt hành động hiện tại.

## 5. Phương thức
### resetTurnResources
- **Mục đích:** Thiết lập lại lượng xăng và số bước đi còn lại khi sang ngày mới.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** int maxFuel, int maxSteps
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Gán fuel bằng maxFuel và remainingSteps bằng maxSteps.
- **Validation:** Các tham số truyền vào phải lớn hơn hoặc bằng 0.
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Cập nhật thuộc tính fuel và remainingSteps của Agent.
- **Được gọi bởi:** Team.resetTurnResources
- **Gọi tới class nào:** Không

### consumeStep
- **Mục đích:** Trừ bớt số bước đi của Agent.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** int cost
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Trừ remainingSteps đi lượng cost.
- **Validation:** cost phải lớn hơn hoặc bằng 0 và không được vượt quá remainingSteps.
- **Ngoại lệ:** GameRuleViolationException (Mã lỗi STEPS_LIMIT_EXCEEDED)
- **Ảnh hưởng tới trạng thái object:** Trừ remainingSteps.
- **Được gọi bởi:** MovementSimulator
- **Gọi tới class nào:** Không

### consumeFuel
- **Mục đích:** Tiêu thụ xăng của Agent.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** int cost
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Trừ fuel đi lượng cost.
- **Validation:** cost phải lớn hơn hoặc bằng 0 và không được vượt quá fuel.
- **Ngoại lệ:** GameRuleViolationException (Mã lỗi AGENT_OUT_OF_FUEL)
- **Ảnh hưởng tới trạng thái object:** Trừ fuel.
- **Được gọi bởi:** MovementSimulator
- **Gọi tới class nào:** Không

### addVisitedSpotToday
- **Mục đích:** Thêm Spot vào danh sách đã ghé thăm trong ngày.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** Spot spot
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Thêm spot vào danh sách visitedSpotsToday.
- **Validation:** spot không được null.
- **Ngoại lệ:** GameRuleViolationException (Mã lỗi VALIDATION_ERROR)
- **Ảnh hưởng tới trạng thái object:** Thêm phần tử vào danh sách visitedSpotsToday.
- **Được gọi bởi:** UdonCollectionEngine
- **Gọi tới class nào:** Không

### clearVisitedSpotsToday
- **Mục đích:** Reset danh sách spot đã ghé thăm hôm nay về rỗng.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** Không
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Gọi clear() trên danh sách visitedSpotsToday.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Xóa sạch visitedSpotsToday.
- **Được gọi bởi:** MatchApplicationService.nextDay
- **Gọi tới class nào:** Không

### hasVisitedSpotToday
- **Mục đích:** Kiểm tra Agent đã ghé thăm Spot này chưa.
- **Kiểu trả về:** `boolean`
- **Tham số đầu vào:** Spot spot
- **Giá trị trả về:** Trả về `boolean`.
- **Business Logic:** Trả về true nếu visitedSpotsToday chứa spot, ngược lại false.
- **Validation:** spot không được null.
- **Ngoại lệ:** GameRuleViolationException (Mã lỗi VALIDATION_ERROR)
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** UdonCollectionEngine
- **Gọi tới class nào:** Không

### clearAction
- **Mục đích:** Xóa hành động hiện tại.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** Không
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Gán action = null.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Đặt action về null.
- **Được gọi bởi:** MatchApplicationService.nextDay
- **Gọi tới class nào:** Không

## 6. Quan hệ
- Team chứa danh sách các Agent. Agent chứa danh sách các Action. Agent tham chiếu tới Spot.

## 7. Ghi chú triển khai
- Các thuộc tính posX, posY phải được kiểm soát cẩn thận bởi MovementSimulator để không nhảy tọa độ bất hợp pháp. Invariant: remainingSteps >= 0 và fuel >= 0 tại mọi thời điểm.

---
# Team

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.model
- Vai trò: Thực thể đội chơi trong trận đấu.
- Trách nhiệm: Lưu thông tin đội, quản lý các Agent của đội, quản lý tổng điểm số thu hoạch Udon, trạng thái nộp kế hoạch và số lần vi phạm rate limit.
- Phạm vi sử dụng: Được quản lý bởi MatchState, sử dụng trong Application Services và REST Controllers.
- Quan hệ với các class khác: Thuộc MatchState, quản lý Agent.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `teamName` | `String` | Tên của đội chơi (định danh độc nhất). | null | Có |
| `agents` | `List<Agent>` | Danh sách các Agent của đội. | [] | Có |
| `disqualified` | `boolean` | Trạng thái bị loại của đội. | false | Có |
| `spamViolationCount` | `int` | Số lần vi phạm Rate Limit. | 0 | Có |
| `collectedUdon` | `int` | Điểm số Udon thu thập được. | 0 | Có |
| `submittedPlan` | `boolean` | Đánh dấu đội chơi đã hoàn tất nộp bài cho Turn này chưa. | false | Có |

## 3. Constructor
- Constructor mặc định: Không tham số.
- Constructor theo tên: Nhận teamName.
- Constructor đầy đủ: Nhận teamName và agents.

## 4. Getter / Setter
- getTeamName() / setTeamName(String): Lấy/đặt tên đội.
- getAgents() / setAgents(List<Agent>): Lấy/đặt danh sách Agent.
- isDisqualified() / setDisqualified(boolean): Lấy/đặt trạng thái disqualified.
- getSpamViolationCount() / setSpamViolationCount(int): Lấy/đặt số lần spam.
- getCollectedUdon() / setCollectedUdon(int): Lấy/đặt số Udon đã thu thập.
- isSubmittedPlan() / setSubmittedPlan(boolean): Lấy/đặt trạng thái nộp bài.

## 5. Phương thức
### findAgentById
- **Mục đích:** Tìm Agent thuộc đội theo ID.
- **Kiểu trả về:** `Agent`
- **Tham số đầu vào:** String id
- **Giá trị trả về:** Trả về `Agent`.
- **Business Logic:** Duyệt danh sách agents lọc theo id.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** requireAgent
- **Gọi tới class nào:** Agent

### requireAgent
- **Mục đích:** Lấy Agent của đội, ném lỗi nếu không tồn tại.
- **Kiểu trả về:** `Agent`
- **Tham số đầu vào:** String id
- **Giá trị trả về:** Trả về `Agent`.
- **Business Logic:** Gọi findAgentById(id). Nếu null, ném ResourceNotFoundException.
- **Validation:** Không
- **Ngoại lệ:** ResourceNotFoundException (Mã lỗi AGENT_NOT_FOUND)
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** MatchApplicationService
- **Gọi tới class nào:** Agent

### resetTurnResources
- **Mục đích:** Khởi tạo lại xăng và bước đi cho Agent khi sang lượt mới.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** int maxFuel, int maxSteps
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Duyệt qua danh sách agents gọi resetTurnResources(maxFuel, maxSteps). Đặt submittedPlan thành false.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Reset các Agent thuộc đội và đổi submittedPlan thành false.
- **Được gọi bởi:** MatchState.initializeTeams, MatchApplicationService.nextDay
- **Gọi tới class nào:** Agent

### ensureEligible
- **Mục đích:** Đảm bảo đội không bị loại.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** Không
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Nếu disqualified là true, ném GameRuleViolationException.
- **Validation:** Không
- **Ngoại lệ:** GameRuleViolationException (Mã lỗi TEAM_DISABLED)
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** MatchApplicationService.submitActions
- **Gọi tới class nào:** Không

### incrementSpamViolation
- **Mục đích:** Tăng số lần vi phạm Rate Limit.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** Không
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** spamViolationCount tăng lên 1.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** spamViolationCount tăng lên 1.
- **Được gọi bởi:** RateLimiterInterceptor
- **Gọi tới class nào:** Không

### addCollectedUdon
- **Mục đích:** Cộng điểm Udon thu hoạch.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** int amount
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Tăng collectedUdon thêm một lượng amount.
- **Validation:** amount không được nhỏ hơn 0.
- **Ngoại lệ:** GameRuleViolationException (Mã lỗi VALIDATION_ERROR)
- **Ảnh hưởng tới trạng thái object:** Tăng collectedUdon.
- **Được gọi bởi:** UdonCollectionEngine
- **Gọi tới class nào:** Không

### resetScore
- **Mục đích:** Reset điểm số về 0.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** Không
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Đặt collectedUdon về 0.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Đặt collectedUdon về 0.
- **Được gọi bởi:** MatchApplicationService
- **Gọi tới class nào:** Không

## 6. Quan hệ
- MatchState chứa danh sách các Team. Team chứa danh sách các Agent.

## 7. Ghi chú triển khai
- Không được expose trực tiếp mutable list. Trả về Collections.unmodifiableList nếu cần.

---
# Cell

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.valueobject
- Vai trò: Ô lưới lục giác trên bản đồ.
- Trách nhiệm: Lưu tọa độ x, y và loại địa hình.
- Phạm vi sử dụng: Sử dụng làm ô lưới địa lý bản đồ game.
- Quan hệ với các class khác: Chứa trong MatchState.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `x` | `int` | Tọa độ cột trên lưới. | 0 | Có |
| `y` | `int` | Tọa độ dòng trên lưới. | 0 | Có |
| `terrainType` | `TerrainType` | Loại địa hình. | PLAIN | Có |

## 3. Constructor
- Constructor tọa độ: Nhận x, y. terrainType mặc định là PLAIN.
- Constructor đầy đủ: Nhận x, y, terrainType.

## 4. Getter / Setter
- getX() / getY(): Lấy tọa độ x, y (Bất biến).
- getTerrainType() / setTerrainType(TerrainType): Lấy/đặt loại địa hình.

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Lưới cells nằm trong MatchState. Cell kết nối với Cell khác qua Road.

## 7. Ghi chú triển khai
- Lớp này bất biến (Immutable) đối với tọa độ x và y để tránh thay đổi bản đồ khi đang chơi.

---
# Road

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.valueobject
- Vai trò: Đường đi nối hai ô kề nhau.
- Trách nhiệm: Xác định đường đi hợp lệ giữa 2 ô bản đồ.
- Phạm vi sử dụng: Lớp nghiệp vụ bất biến.
- Quan hệ với các class khác: Liên kết 2 Cell.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `cell1` | `Cell` | Ô bản đồ thứ nhất. | null | Có |
| `cell2` | `Cell` | Ô bản đồ thứ hai. | null | Có |

## 3. Constructor
- Constructor đầy đủ: Nhận cell1 và cell2.

## 4. Getter / Setter
- getCell1() / setCell1(Cell): Lấy/đặt ô thứ nhất.
- getCell2() / setCell2(Cell): Lấy/đặt ô thứ hai.

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Road liên kết cell1 và cell2 trong MatchState.

## 7. Ghi chú triển khai
- Road là vô hướng (undirected). MovementSimulator sử dụng Road để xác thực đường đi.

---
# Spot

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.valueobject
- Vai trò: Điểm đặc biệt trên bản đồ.
- Trách nhiệm: Lưu giữ thông tin lượng Udon của từng Team tại ô tương ứng.
- Phạm vi sử dụng: Được quản lý bởi MatchState.
- Quan hệ với các class khác: Liên kết tới 1 Cell.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `cell` | `Cell` | Ô bản đồ chứa Spot. | null | Có |
| `spotType` | `String` | Loại Spot (FUEL_STATION hoặc UDON_SPOT). | null | Có |
| `teamUdonStocks` | `Map<String, Integer>` | Map lưu số lượng Udon còn lại của mỗi đội. | {} | Có |

## 3. Constructor
- Constructor đầy đủ: Nhận cell và spotType.

## 4. Getter / Setter
- getCell() / setCell(Cell): Lấy/đặt Cell.
- getSpotType() / setSpotType(String): Lấy/đặt loại Spot.
- getTeamUdonStocks() / setTeamUdonStocks(Map): Lấy/đặt map chứa kho Udon.

## 5. Phương thức
### getUdonStock
- **Mục đích:** Lấy lượng Udon còn lại của một đội tại Spot.
- **Kiểu trả về:** `int`
- **Tham số đầu vào:** String teamName
- **Giá trị trả về:** Trả về `int`.
- **Business Logic:** Lấy value trong teamUdonStocks theo teamName, nếu rỗng trả về 0.
- **Validation:** teamName không trống.
- **Ngoại lệ:** GameRuleViolationException (Mã lỗi VALIDATION_ERROR)
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** UdonCollectionEngine
- **Gọi tới class nào:** Không

### setUdonStock
- **Mục đích:** Thiết lập lượng Udon của một đội.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** String teamName, int amount
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Ghi nhận key-value vào teamUdonStocks.
- **Validation:** teamName không rỗng, amount không âm.
- **Ngoại lệ:** GameRuleViolationException
- **Ảnh hưởng tới trạng thái object:** Cập nhật teamUdonStocks.
- **Được gọi bởi:** MatchState.initializeSpots, UdonCollectionEngine
- **Gọi tới class nào:** Không

### decrementUdonStock
- **Mục đích:** Trừ 1 bánh Udon của đội thi đấu tại Spot.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** String teamName
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Lấy lượng hiện tại trừ 1 và lưu lại.
- **Validation:** Lượng hiện tại phải lớn hơn 0.
- **Ngoại lệ:** GameRuleViolationException (Mã lỗi INVALID_TARGET_TERRAIN)
- **Ảnh hưởng tới trạng thái object:** Trừ 1 Udon.
- **Được gọi bởi:** UdonCollectionEngine
- **Gọi tới class nào:** Không

### resetUdonStocks
- **Mục đích:** Đặt lại lượng Udon ban đầu cho mọi đội tại Spot.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** int initialAmount
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** replaceAll toàn bộ map với value = initialAmount.
- **Validation:** initialAmount không âm.
- **Ngoại lệ:** GameRuleViolationException
- **Ảnh hưởng tới trạng thái object:** Khôi phục kho Udon.
- **Được gọi bởi:** MatchState.initializeSpots, MatchApplicationService.nextDay
- **Gọi tới class nào:** Không

## 6. Quan hệ
- Spot tham chiếu Cell. Spot thuộc MatchState.

## 7. Ghi chú triển khai
- Thread-safe map là cần thiết nếu nộp bài chạy song song.

---
# Action

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.valueobject
- Vai trò: Hành động đơn lẻ của Agent.
- Trách nhiệm: Lưu thứ tự, loại hành động, tọa độ mục tiêu và timestamp.
- Phạm vi sử dụng: Bất biến (Value Object) lưu trong Agent.
- Quan hệ với các class khác: Nằm trong Agent.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `order` | `int` | Thứ tự bước đi (1 đến 5). | 0 | Có |
| `actionType` | `ActionType` | Loại hành động (MOVE hoặc WAIT). | null | Có |
| `targetX` | `Integer` | Tọa độ X ô đích. | null | Không |
| `targetY` | `Integer` | Tọa độ Y ô đích. | null | Không |
| `timestamp` | `long` | Timestamp ghi nhận. | 0 | Có |

## 3. Constructor
- Constructor đầy đủ: Nhận order, actionType, targetX, targetY, timestamp.

## 4. Getter / Setter
- Getter đầy đủ cho toàn bộ các thuộc tính. Lớp này bất biến, không có Setter.

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Agent chứa danh sách Action.

## 7. Ghi chú triển khai
- Immutable Value Object.

---
# MatchConfig

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.valueobject
- Vai trò: Tham số cấu hình trận đấu.
- Trách nhiệm: Lưu trữ cấu hình bất biến của game.
- Phạm vi sử dụng: Truyền vào các Domain Service để làm căn cứ tính toán.
- Quan hệ với các class khác: Lưu trong MatchApplicationService.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `mapWidth` | `int` | Chiều rộng. | 20 | Có |
| `mapHeight` | `int` | Chiều cao. | 15 | Có |
| `initialFuel` | `int` | Xăng đầu ngày. | 100 | Có |
| `maxFuel` | `int` | Xăng tối đa bình. | 100 | Có |
| `maxTurns` | `int` | Số ngày tối đa. | 1 | Có |
| `maxStepsPerTurn` | `int` | Số bước đi một turn. | 5 | Có |
| `maxTeams` | `int` | Số đội tối đa. | 2 | Có |
| `agentsPerTeam` | `int` | Số agent mỗi đội. | 3 | Có |
| `patrolAgents` | `int` | Số Patrol Agent. | 2 | Có |
| `refuelAgents` | `int` | Số Refuel Agent. | 1 | Có |
| `turnTimeLimitMs` | `int` | Thời gian turn. | 1000 | Có |
| `maxRequestsPerSecond` | `int` | Tần suất request. | 10 | Có |
| `maxSpamViolations` | `int` | Số lần spam tối đa. | 3 | Có |
| `roadStepCost` | `int` | Bước trên ROAD. | 1 | Có |
| `roadFuelCost` | `int` | Xăng trên ROAD. | 2 | Có |
| `plainStepCost` | `int` | Bước trên PLAIN. | 2 | Có |
| `plainFuelCost` | `int` | Xăng trên PLAIN. | 1 | Có |
| `mountainStepCost` | `int` | Bước trên MOUNTAIN. | 3 | Có |
| `mountainFuelCost` | `int` | Xăng trên MOUNTAIN. | 2 | Có |
| `initialSpotUdonStock` | `int` | Lượng Udon ban đầu Spot. | 5 | Có |

## 3. Constructor
- Constructor mặc định: Khởi tạo giá trị mặc định.
- Constructor đầy đủ: Nhận tất cả các trường cấu hình.

## 4. Getter / Setter
- Đầy đủ Getter/Setter cho toàn bộ các thuộc tính.

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Sở hữu bởi MatchApplicationService.

## 7. Ghi chú triển khai
- Các giá trị chi phí địa hình không được phép âm.

---
# MatchState

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.valueobject
- Vai trò: Aggregate Root quản lý trạng thái trận đấu.
- Trách nhiệm: Lưu trạng thái trận, điều phối đăng ký team, start trận, và dọn dẹp ngày cũ.
- Phạm vi sử dụng: Nghiệp vụ Aggregate Root cốt lõi.
- Quan hệ với các class khác: Quản lý Team, Cell, Road, Spot.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `status` | `MatchStatus` | Trạng thái trận. | WAITING | Có |
| `currentTurn` | `int` | Turn hiện tại. | 0 | Có |
| `turnStartTime` | `long` | Thời điểm bắt đầu turn. | 0 | Có |
| `teams` | `List<Team>` | Danh sách đội. | [] | Có |
| `cells` | `List<Cell>` | Danh sách Cell. | [] | Có |
| `roads` | `List<Road>` | Danh sách Road. | [] | Có |
| `spots` | `List<Spot>` | Danh sách Spot. | [] | Có |
| `currentTurnActions` | `Map<String, Action>` | Map lưu hành động. | {} | Có |
| `cellIndex` | `Map<String, Cell>` | Index tọa độ tìm nhanh Cell. | {} | Có |

## 3. Constructor
- Constructor mặc định: status = WAITING, currentTurn = 0.

## 4. Getter / Setter
- Getter/Setter đầy đủ cho toàn bộ thuộc tính.

## 5. Phương thức
### registerTeam
- **Mục đích:** Đăng ký đội chơi mới.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** Team team, int maxTeams
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Kiểm tra WAITING, kiểm tra trùng tên đội, kiểm tra maxTeams. Thêm team vào danh sách.
- **Validation:** Trạng thái là WAITING, teamName chưa tồn tại, kích thước teams chưa vượt maxTeams.
- **Ngoại lệ:** MatchStateConflictException (Mã lỗi MATCH_NOT_WAITING, TEAM_ALREADY_EXISTS, MAX_TEAMS_REACHED)
- **Ảnh hưởng tới trạng thái object:** Thêm team mới vào teams list.
- **Được gọi bởi:** MatchApplicationService.registerTeam
- **Gọi tới class nào:** Team

### start
- **Mục đích:** Khởi động trận đấu.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** int maxFuel, int maxSteps, int initialUdonStock
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Gán PLAYING, currentTurn = 1, khởi tạo turnStartTime, gọi initializeTeams và initializeSpots.
- **Validation:** status không phải PLAYING, status là WAITING, teams không rỗng.
- **Ngoại lệ:** MatchStateConflictException (Mã lỗi MATCH_ALREADY_STARTED, MATCH_NOT_READY)
- **Ảnh hưởng tới trạng thái object:** Chuyển status sang PLAYING, cập nhật năng lượng đội chơi và reset Spot.
- **Được gọi bởi:** MatchApplicationService.startMatch
- **Gọi tới class nào:** Team, Spot

### ensurePlaying
- **Mục đích:** Đảm bảo trận đấu đang PLAYING.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** Không
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Nếu status không phải PLAYING, ném MatchStateConflictException.
- **Validation:** Không
- **Ngoại lệ:** MatchStateConflictException (Mã lỗi MATCH_NOT_PLAYING)
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** MatchApplicationService.submitActions
- **Gọi tới class nào:** Không

### addCell
- **Mục đích:** Thêm Cell vào lưới.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** Cell cell
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Thêm cell vào cells và cập nhật cellIndex với key = x_y.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Cập nhật cells và cellIndex.
- **Được gọi bởi:** HexGridUtils
- **Gọi tới class nào:** Cell

### getCell
- **Mục đích:** Lấy Cell theo tọa độ.
- **Kiểu trả về:** `Cell`
- **Tham số đầu vào:** int x, int y
- **Giá trị trả về:** Trả về `Cell`.
- **Business Logic:** Lấy Cell từ cellIndex theo key x_y.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** MovementSimulator, FuelManager, UdonCollectionEngine
- **Gọi tới class nào:** Không

### requireTeam
- **Mục đích:** Yêu cầu lấy thông tin Team theo tên.
- **Kiểu trả về:** `Team`
- **Tham số đầu vào:** String teamName
- **Giá trị trả về:** Trả về `Team`.
- **Business Logic:** Duyệt tìm Team. Nếu null, ném ResourceNotFoundException.
- **Validation:** Không
- **Ngoại lệ:** ResourceNotFoundException (Mã lỗi TEAM_NOT_FOUND)
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** MatchApplicationService
- **Gọi tới class nào:** Team

### clearTurnActions
- **Mục đích:** Xóa sạch hành động của Turn.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** Không
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Xóa map currentTurnActions.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** currentTurnActions = {}
- **Được gọi bởi:** MatchApplicationService.nextDay
- **Gọi tới class nào:** Không

## 6. Quan hệ
- Aggregate Root quản lý Team, Cell, Road, Spot.

## 7. Ghi chú triển khai
- Đảm bảo tính Thread-safe khi cập nhật các thuộc tính in-memory.

---
# AgentExecutionResult

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.valueobject
- Vai trò: Báo cáo kết quả chạy của Agent.
- Trách nhiệm: Lưu ID Agent và danh sách hành động thực tế đã chạy.
- Phạm vi sử dụng: Bất biến.
- Quan hệ với các class khác: Nằm trong TurnSimulationResult.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `agentId` | `String` | ID của Agent. | null | Có |
| `actions` | `List<Action>` | Các hành động thực tế. | [] | Có |

## 3. Constructor
- Constructor đầy đủ: Nhận agentId, actions.

## 4. Getter / Setter
- Getter đầy đủ cho các trường (Bất biến).

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Chứa trong TurnSimulationResult.

## 7. Ghi chú triển khai
- Bất biến.

---
# TurnSimulationResult

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.valueobject
- Vai trò: Báo cáo kết quả Turn.
- Trách nhiệm: Lưu ngày và kết quả của các Agent.
- Phạm vi sử dụng: Bất biến.
- Quan hệ với các class khác: Chứa danh sách AgentExecutionResult.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `day` | `int` | Ngày thi đấu. | 0 | Có |
| `agentResults` | `List<AgentExecutionResult>` | Danh sách kết quả Agent. | [] | Có |

## 3. Constructor
- Constructor đầy đủ: Nhận day, agentResults.

## 4. Getter / Setter
- Getter đầy đủ cho các trường.

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Chứa AgentExecutionResult.

## 7. Ghi chú triển khai
- Bất biến.

---
# ActionType

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.valueobject
- Vai trò: Enum hành động Agent.
- Trách nhiệm: Định nghĩa MOVE, WAIT.
- Phạm vi sử dụng: Toàn hệ thống.
- Quan hệ với các class khác: Sử dụng bởi Action.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor

## 4. Getter / Setter

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Enum

---
# AgentType

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.valueobject
- Vai trò: Enum loại Agent.
- Trách nhiệm: Định nghĩa PATROL, REFUEL.
- Phạm vi sử dụng: Toàn hệ thống.
- Quan hệ với các class khác: Sử dụng bởi Agent.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor

## 4. Getter / Setter

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Enum

---
# MatchStatus

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.valueobject
- Vai trò: Enum trạng thái trận.
- Trách nhiệm: Định nghĩa WAITING, PLAYING, FINISHED.
- Phạm vi sử dụng: Toàn hệ thống.
- Quan hệ với các class khác: Sử dụng bởi MatchState.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor

## 4. Getter / Setter

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Enum

---
# TerrainType

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.valueobject
- Vai trò: Enum địa hình ô.
- Trách nhiệm: Định nghĩa PLAIN, MOUNTAIN, ROAD, POND.
- Phạm vi sử dụng: Toàn hệ thống.
- Quan hệ với các class khác: Sử dụng bởi Cell.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor

## 4. Getter / Setter

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Enum

---
# BusinessException

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.exception.base
- Vai trò: Ngoại lệ nghiệp vụ.
- Trách nhiệm: Đại diện lỗi BusinessException.
- Phạm vi sử dụng: Ném trong Core Domain, bắt ở GlobalExceptionHandler.
- Quan hệ với các class khác: Kế thừa RuntimeException hoặc BusinessException.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `errorCode` | `ErrorCode` | Mã lỗi định danh. | null | Có |

## 3. Constructor
- Constructor đầy đủ: Nhận ErrorCode và message.

## 4. Getter / Setter
- getErrorCode(): Lấy mã lỗi.

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Liên kết ErrorCode.

## 7. Ghi chú triển khai
- Được bắt và dịch bởi GlobalExceptionHandler sang HTTP Response.

---
# SystemException

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.exception.base
- Vai trò: Ngoại lệ nghiệp vụ.
- Trách nhiệm: Đại diện lỗi SystemException.
- Phạm vi sử dụng: Ném trong Core Domain, bắt ở GlobalExceptionHandler.
- Quan hệ với các class khác: Kế thừa RuntimeException hoặc BusinessException.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `errorCode` | `ErrorCode` | Mã lỗi định danh. | null | Có |

## 3. Constructor
- Constructor đầy đủ: Nhận ErrorCode và message.

## 4. Getter / Setter
- getErrorCode(): Lấy mã lỗi.

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Liên kết ErrorCode.

## 7. Ghi chú triển khai
- Được bắt và dịch bởi GlobalExceptionHandler sang HTTP Response.

---
# GameRuleViolationException

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.exception.business
- Vai trò: Ngoại lệ nghiệp vụ.
- Trách nhiệm: Đại diện lỗi GameRuleViolationException.
- Phạm vi sử dụng: Ném trong Core Domain, bắt ở GlobalExceptionHandler.
- Quan hệ với các class khác: Kế thừa RuntimeException hoặc BusinessException.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `errorCode` | `ErrorCode` | Mã lỗi định danh. | null | Có |

## 3. Constructor
- Constructor đầy đủ: Nhận ErrorCode và message.

## 4. Getter / Setter
- getErrorCode(): Lấy mã lỗi.

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Liên kết ErrorCode.

## 7. Ghi chú triển khai
- Được bắt và dịch bởi GlobalExceptionHandler sang HTTP Response.

---
# MatchStateConflictException

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.exception.business
- Vai trò: Ngoại lệ nghiệp vụ.
- Trách nhiệm: Đại diện lỗi MatchStateConflictException.
- Phạm vi sử dụng: Ném trong Core Domain, bắt ở GlobalExceptionHandler.
- Quan hệ với các class khác: Kế thừa RuntimeException hoặc BusinessException.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `errorCode` | `ErrorCode` | Mã lỗi định danh. | null | Có |

## 3. Constructor
- Constructor đầy đủ: Nhận ErrorCode và message.

## 4. Getter / Setter
- getErrorCode(): Lấy mã lỗi.

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Liên kết ErrorCode.

## 7. Ghi chú triển khai
- Được bắt và dịch bởi GlobalExceptionHandler sang HTTP Response.

---
# RateLimitExceededException

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.exception.business
- Vai trò: Ngoại lệ nghiệp vụ.
- Trách nhiệm: Đại diện lỗi RateLimitExceededException.
- Phạm vi sử dụng: Ném trong Core Domain, bắt ở GlobalExceptionHandler.
- Quan hệ với các class khác: Kế thừa RuntimeException hoặc BusinessException.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `errorCode` | `ErrorCode` | Mã lỗi định danh. | null | Có |

## 3. Constructor
- Constructor đầy đủ: Nhận ErrorCode và message.

## 4. Getter / Setter
- getErrorCode(): Lấy mã lỗi.

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Liên kết ErrorCode.

## 7. Ghi chú triển khai
- Được bắt và dịch bởi GlobalExceptionHandler sang HTTP Response.

---
# ResourceNotFoundException

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.exception.business
- Vai trò: Ngoại lệ nghiệp vụ.
- Trách nhiệm: Đại diện lỗi ResourceNotFoundException.
- Phạm vi sử dụng: Ném trong Core Domain, bắt ở GlobalExceptionHandler.
- Quan hệ với các class khác: Kế thừa RuntimeException hoặc BusinessException.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `errorCode` | `ErrorCode` | Mã lỗi định danh. | null | Có |

## 3. Constructor
- Constructor đầy đủ: Nhận ErrorCode và message.

## 4. Getter / Setter
- getErrorCode(): Lấy mã lỗi.

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Liên kết ErrorCode.

## 7. Ghi chú triển khai
- Được bắt và dịch bởi GlobalExceptionHandler sang HTTP Response.

---
# ConfigLoadException

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.exception.system
- Vai trò: Ngoại lệ nghiệp vụ.
- Trách nhiệm: Đại diện lỗi ConfigLoadException.
- Phạm vi sử dụng: Ném trong Core Domain, bắt ở GlobalExceptionHandler.
- Quan hệ với các class khác: Kế thừa RuntimeException hoặc BusinessException.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `errorCode` | `ErrorCode` | Mã lỗi định danh. | null | Có |

## 3. Constructor
- Constructor đầy đủ: Nhận ErrorCode và message.

## 4. Getter / Setter
- getErrorCode(): Lấy mã lỗi.

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Liên kết ErrorCode.

## 7. Ghi chú triển khai
- Được bắt và dịch bởi GlobalExceptionHandler sang HTTP Response.

---
# ErrorCode

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.exception.code
- Vai trò: Enum mã lỗi.
- Trách nhiệm: Định nghĩa các mã lỗi hệ thống.
- Phạm vi sử dụng: Toàn hệ thống.
- Quan hệ với các class khác: Sử dụng bởi Exception.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `code` | `String` | Mã ký tự. | null | Có |
| `message` | `String` | Mô tả tiếng Anh. | null | Có |

## 3. Constructor
- Constructor đầy đủ: Nhận code, message.

## 4. Getter / Setter
- getCode(), getMessage()

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Enum

---
# ValidationErrorDetail

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.exception.response
- Vai trò: DTO lưu chi tiết lỗi validation.
- Trách nhiệm: Đóng gói tên field lỗi, rejectedValue và message.
- Phạm vi sử dụng: REST Response DTO.
- Quan hệ với các class khác: Nằm trong ErrorResponse.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `field` | `String` | Tên trường lỗi. | null | Có |
| `rejectedValue` | `Object` | Giá trị bị từ chối. | null | Không |
| `message` | `String` | Thông điệp báo lỗi. | null | Có |

## 3. Constructor
- Constructor đầy đủ: Nhận field, rejectedValue, message.

## 4. Getter / Setter
- Chỉ có getter (Record).

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Record

---
# ErrorResponse

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.exception.response
- Vai trò: DTO phản hồi lỗi API.
- Trách nhiệm: Đóng gói mã lỗi, message và validationErrors.
- Phạm vi sử dụng: REST Response DTO.
- Quan hệ với các class khác: Chứa ValidationErrorDetail.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `errorCode` | `String` | Mã lỗi nghiệp vụ. | null | Có |
| `message` | `String` | Mô tả lỗi. | null | Có |
| `validationErrors` | `List<ValidationErrorDetail>` | Danh sách lỗi chi tiết. | [] | Không |

## 3. Constructor
- Constructor đầy đủ: Nhận errorCode, message, validationErrors.

## 4. Getter / Setter
- Chỉ có getter (Record).

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Chứa ValidationErrorDetail.

## 7. Ghi chú triển khai
- Record

---
# GlobalExceptionHandler

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.exception.handler
- Vai trò: Bộ bắt và xử lý lỗi REST API.
- Trách nhiệm: Chuyển các Exception của Domain thành JSON ErrorResponse và HTTP status tương ứng.
- Phạm vi sử dụng: Tầng REST Controller.
- Quan hệ với các class khác: Bắt Exception, trả ErrorResponse.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor
- Constructor mặc định.

## 4. Getter / Setter

## 5. Phương thức
### handleBusinessException
- **Mục đích:** Bắt và dịch BusinessException.
- **Kiểu trả về:** `ResponseEntity`
- **Tham số đầu vào:** BusinessException ex
- **Giá trị trả về:** Trả về `ResponseEntity`.
- **Business Logic:** Đọc mã lỗi ErrorCode. Trả HTTP 400, 404, 409, hoặc 429 kèm ErrorResponse body.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** Spring Boot MVC Context
- **Gọi tới class nào:** ErrorResponse

### handleMethodArgumentNotValid
- **Mục đích:** Xử lý lỗi validation của Spring DTO.
- **Kiểu trả về:** `ResponseEntity`
- **Tham số đầu vào:** MethodArgumentNotValidException ex
- **Giá trị trả về:** Trả về `ResponseEntity`.
- **Business Logic:** Duyệt qua list BindingResult errors, map sang ValidationErrorDetail. Trả HTTP 400 kèm ErrorResponse body.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** Spring Boot MVC Context
- **Gọi tới class nào:** ErrorResponse, ValidationErrorDetail

### handleGeneralException
- **Mục đích:** Bắt lỗi hệ thống chưa bắt được.
- **Kiểu trả về:** `ResponseEntity`
- **Tham số đầu vào:** Exception ex
- **Giá trị trả về:** Trả về `ResponseEntity`.
- **Business Logic:** Trả HTTP 500 kèm ErrorResponse body.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** Spring Boot
- **Gọi tới class nào:** ErrorResponse

## 6. Quan hệ
- Chuyển đổi Exception thành ErrorResponse.

## 7. Ghi chú triển khai
- Spring `@RestControllerAdvice`.

---
# HexGridUtils

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.service
- Vai trò: Tiện ích lưới.
- Trách nhiệm: generateGrid, isAdjacent.
- Phạm vi sử dụng: Domain Service.
- Quan hệ với các class khác: Thao tác trên MatchState.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor

## 4. Getter / Setter

## 5. Phương thức
### generateGrid
- **Mục đích:** Tạo lưới ô lục giác.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** int width, int height, MatchState matchState
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Clear Cell/Road/Spot cũ. Duyệt width, height để tạo Cell và thêm vào MatchState. Sinh Road và đặt Fuel Station.
- **Validation:** width, height > 0.
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Lưới cells, roads, spots của MatchState bị thay đổi hoàn toàn.
- **Được gọi bởi:** MatchApplicationService.startMatch
- **Gọi tới class nào:** MatchState, Cell, Road, Spot, TerrainGenerator

### isAdjacent
- **Mục đích:** Kiểm tra kề nhau.
- **Kiểu trả về:** `boolean`
- **Tham số đầu vào:** int x1, int y1, int x2, int y2
- **Giá trị trả về:** Trả về `boolean`.
- **Business Logic:** So sánh chênh lệch dx, dy theo quy luật lưới chẵn lẻ.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** MovementSimulator
- **Gọi tới class nào:** Không

## 6. Quan hệ
- Tính toán trên Cell.

## 7. Ghi chú triển khai
- Stateless Utility.

---
# TerrainGenerator

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.service
- Vai trò: Bộ tạo địa hình.
- Trách nhiệm: Phân bổ ngẫu nhiên địa hình Cell.
- Phạm vi sử dụng: Domain Service.
- Quan hệ với các class khác: MatchState.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor

## 4. Getter / Setter

## 5. Phương thức
### generateTerrain
- **Mục đích:** Sinh địa hình ngẫu nhiên cho Cell.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** MatchState matchState
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Duyệt cells. Đặt ô (0,0) là PLAIN, các ô khác random theo tỷ lệ PLAIN 65%, MOUNTAIN 20%, ROAD 5%, POND 10%.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Cập nhật terrainType của các Cell.
- **Được gọi bởi:** HexGridUtils
- **Gọi tới class nào:** MatchState, Cell

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Stateless.

---
# MapValidator

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.service
- Vai trò: Xác thực bản đồ.
- Trách nhiệm: isMapConnected.
- Phạm vi sử dụng: Domain Service.
- Quan hệ với các class khác: MatchState.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor

## 4. Getter / Setter

## 5. Phương thức
### isMapConnected
- **Mục đích:** Kiểm tra BFS liên thông bản đồ.
- **Kiểu trả về:** `boolean`
- **Tham số đầu vào:** MatchState matchState
- **Giá trị trả về:** Trả về `boolean`.
- **Business Logic:** BFS từ ô đầu tiên không phải POND, đếm số lượng ô duyệt được có bằng tổng số ô không phải POND không.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** MatchApplicationService
- **Gọi tới class nào:** MatchState, Cell

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Stateless.

---
# ActionValidatorEngine

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.service
- Vai trò: Validate kế hoạch.
- Trách nhiệm: validate.
- Phạm vi sử dụng: Domain Service.
- Quan hệ với các class khác: MatchConfig.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor

## 4. Getter / Setter

## 5. Phương thức
### validate
- **Mục đích:** Kiểm tra tính hợp lệ của Action plans.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** Map<String, List<Action>> plans, MatchConfig config
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Duyệt plans. Kiểm tra số bước tối đa, thứ tự order liên tục từ 1, MOVE phải có targetX, targetY.
- **Validation:** Các bước đi phải liên tục.
- **Ngoại lệ:** GameRuleViolationException (Mã lỗi VALIDATION_ERROR)
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** MatchApplicationService.submitActions
- **Gọi tới class nào:** Action, MatchConfig

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Stateless.

---
# FuelManager

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.service
- Vai trò: Quản lý xăng dầu.
- Trách nhiệm: Tiếp xăng tự động, tính chi phí.
- Phạm vi sử dụng: Domain Service.
- Quan hệ với các class khác: Agent, Team, MatchState.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor

## 4. Getter / Setter

## 5. Phương thức
### autoRefuel (Theo bước)
- **Mục đích:** Tiếp xăng tự động tại bước di chuyển hiện tại.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** int step, Team team, MatchConfig config
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Tìm Refuel và Patrol cùng step. Nếu đứng chung ô, set fuel = config.maxFuel cho Patrol.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Patrol Agent được sạc xăng.
- **Được gọi bởi:** MovementSimulator
- **Gọi tới class nào:** Agent, Team, MatchConfig

### autoRefuel (Toàn bộ state)
- **Mục đích:** Tiếp xăng tự động qua MatchState.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** MatchState state, MatchConfig config
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Duyệt các team, tìm Refuel và Patrol đứng chung ô, set fuel = config.maxFuel.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Sạc đầy xăng cho các Patrol Agent hợp lệ.
- **Được gọi bởi:** MovementSimulator
- **Gọi tới class nào:** MatchState, Team, Agent

### consumeFuel
- **Mục đích:** Trừ xăng di chuyển.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** MatchState state, MatchConfig config
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Duyệt các Agent thực hiện MOVE, tính fuelCost theo địa hình ô đích, trừ fuel của Agent.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Giảm xăng Agent.
- **Được gọi bởi:** MovementSimulator
- **Gọi tới class nào:** MatchState, Agent

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Stateless.

---
# UdonCollectionEngine

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.service
- Vai trò: Động cơ thu udon.
- Trách nhiệm: collectUdon.
- Phạm vi sử dụng: Domain Service.
- Quan hệ với các class khác: Agent, Team, Spot, MatchState.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor

## 4. Getter / Setter

## 5. Phương thức
### collectUdon
- **Mục đích:** Thu hoạch bánh Udon.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** Team team, Agent agent, MatchState state
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Nếu Agent không phải PATROL -> bỏ qua. Tìm Spot tại vị trí Agent. Nếu có Spot, chưa thu hoạch hôm nay và Spot còn Udon -> cộng 1 collectedUdon cho Team, trừ 1 Udon của Spot, thêm Spot vào visitedSpotsToday của Agent.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Cập nhật Team collectedUdon, Spot Udon stock, Agent visitedSpotsToday.
- **Được gọi bởi:** MovementSimulator
- **Gọi tới class nào:** Team, Agent, Spot, MatchState

### findSpotAt
- **Mục đích:** Tìm Spot theo tọa độ.
- **Kiểu trả về:** `Spot`
- **Tham số đầu vào:** int x, int y, MatchState state
- **Giá trị trả về:** Trả về `Spot`.
- **Business Logic:** Duyệt state.getSpots() trả về Spot khớp tọa độ.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** collectUdon
- **Gọi tới class nào:** MatchState, Spot

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Stateless.

---
# MovementSimulator

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.service
- Vai trò: Động cơ di chuyển.
- Trách nhiệm: Mô phỏng turn.
- Phạm vi sử dụng: Domain Service.
- Quan hệ với các class khác: Team, MatchState, FuelManager, UdonCollectionEngine.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor

## 4. Getter / Setter

## 5. Phương thức
### simulateTeamTurn
- **Mục đích:** Giả lập toàn bộ di chuyển trong ngày của đội.
- **Kiểu trả về:** `List<AgentExecutionResult>`
- **Tham số đầu vào:** Team team, MatchState state, MatchConfig config, FuelManager fuelMgr, UdonCollectionEngine udonEng
- **Giá trị trả về:** Trả về `List<AgentExecutionResult>`.
- **Business Logic:** Lặp step từ config.maxStepsPerTurn xuống 1. Tại mỗi step, gọi fuelMgr.autoRefuel. Duyệt qua Agent, gọi simulateStep, gọi udonEng.collectUdon. Lưu kết quả chạy.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Cập nhật tọa độ, xăng, bước đi và Udon của các Agent / Team.
- **Được gọi bởi:** MatchApplicationService.submitActions
- **Gọi tới class nào:** Team, Agent, MatchState, FuelManager, UdonCollectionEngine, AgentExecutionResult

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Lõi thuật toán mô phỏng.

---
# MatchStateRepository

## 1. Tổng quan
- Package: com.naprock.hexudon.domain.repository
- Vai trò: Cổng giao tiếp lưu trữ.
- Trách nhiệm: Interface Repository.
- Phạm vi sử dụng: Domain Layer.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor

## 4. Getter / Setter

## 5. Phương thức
### loadState
- **Mục đích:** Nạp state.
- **Kiểu trả về:** `MatchState`
- **Tham số đầu vào:** Không
- **Giá trị trả về:** Trả về `MatchState`.
- **Business Logic:** Định nghĩa hợp đồng.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** MatchApplicationService
- **Gọi tới class nào:** Không

### saveState
- **Mục đích:** Lưu state.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** MatchState state
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Định nghĩa hợp đồng.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** MatchApplicationService
- **Gọi tới class nào:** Không

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Interface.

---
# RegisterTeamUseCase

## 1. Tổng quan
- Package: com.naprock.hexudon.application.port.in
- Vai trò: Inbound Port (Interface).
- Trách nhiệm: Định nghĩa ca sử dụng RegisterTeamUseCase.
- Phạm vi sử dụng: Application Layer.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor

## 4. Getter / Setter

## 5. Phương thức
### registerTeam
- **Mục đích:** Thực thi ca sử dụng RegisterTeamUseCase.
- **Kiểu trả về:** `Team`
- **Tham số đầu vào:** String teamName
- **Giá trị trả về:** Trả về `Team`.
- **Business Logic:** Định nghĩa hợp đồng.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** MatchController
- **Gọi tới class nào:** Không

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Interface.

---
# StartMatchUseCase

## 1. Tổng quan
- Package: com.naprock.hexudon.application.port.in
- Vai trò: Inbound Port (Interface).
- Trách nhiệm: Định nghĩa ca sử dụng StartMatchUseCase.
- Phạm vi sử dụng: Application Layer.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor

## 4. Getter / Setter

## 5. Phương thức
### startMatch
- **Mục đích:** Thực thi ca sử dụng StartMatchUseCase.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** Không
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Định nghĩa hợp đồng.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** MatchController
- **Gọi tới class nào:** Không

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Interface.

---
# SubmitActionsUseCase

## 1. Tổng quan
- Package: com.naprock.hexudon.application.port.in
- Vai trò: Inbound Port (Interface).
- Trách nhiệm: Định nghĩa ca sử dụng SubmitActionsUseCase.
- Phạm vi sử dụng: Application Layer.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor

## 4. Getter / Setter

## 5. Phương thức
### submitActions
- **Mục đích:** Thực thi ca sử dụng SubmitActionsUseCase.
- **Kiểu trả về:** `TurnSimulationResult`
- **Tham số đầu vào:** String teamName, int day, Map<String, List<Action>> agentPlans
- **Giá trị trả về:** Trả về `TurnSimulationResult`.
- **Business Logic:** Định nghĩa hợp đồng.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** MatchController
- **Gọi tới class nào:** Không

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Interface.

---
# GetMatchStateUseCase

## 1. Tổng quan
- Package: com.naprock.hexudon.application.port.in
- Vai trò: Inbound Port (Interface).
- Trách nhiệm: Định nghĩa ca sử dụng GetMatchStateUseCase.
- Phạm vi sử dụng: Application Layer.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor

## 4. Getter / Setter

## 5. Phương thức
### getMatchState
- **Mục đích:** Thực thi ca sử dụng GetMatchStateUseCase.
- **Kiểu trả về:** `MatchState`
- **Tham số đầu vào:** Không
- **Giá trị trả về:** Trả về `MatchState`.
- **Business Logic:** Định nghĩa hợp đồng.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** MatchController
- **Gọi tới class nào:** Không

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Interface.

---
# MatchStateStorePort

## 1. Tổng quan
- Package: com.naprock.hexudon.application.port.out
- Vai trò: Outbound Port (Interface).
- Trách nhiệm: Giao diện lưu trữ trạng thái.
- Phạm vi sử dụng: Application Layer.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor

## 4. Getter / Setter

## 5. Phương thức
### loadState
- **Mục đích:** Nạp state.
- **Kiểu trả về:** `MatchState`
- **Tham số đầu vào:** Không
- **Giá trị trả về:** Trả về `MatchState`.
- **Business Logic:** Hợp đồng.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** MatchApplicationService
- **Gọi tới class nào:** Không

### saveState
- **Mục đích:** Lưu state.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** MatchState state
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Hợp đồng.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** MatchApplicationService
- **Gọi tới class nào:** Không

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Interface.

---
# MatchConfigLoaderPort

## 1. Tổng quan
- Package: com.naprock.hexudon.application.port.out
- Vai trò: Outbound Port (Interface).
- Trách nhiệm: Giao diện nạp cấu hình.
- Phạm vi sử dụng: Application Layer.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor

## 4. Getter / Setter

## 5. Phương thức
### loadConfig
- **Mục đích:** Tải cấu hình game.
- **Kiểu trả về:** `MatchConfig`
- **Tham số đầu vào:** Không
- **Giá trị trả về:** Trả về `MatchConfig`.
- **Business Logic:** Hợp đồng.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** MatchApplicationService
- **Gọi tới class nào:** Không

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Interface.

---
# MatchApplicationService

## 1. Tổng quan
- Package: com.naprock.hexudon.application.service
- Vai trò: Lớp điều phối chính tầng Application.
- Trách nhiệm: Triển khai toàn bộ Inbound Ports, phối hợp Outbound Ports và Domain Services.
- Phạm vi sử dụng: Application Layer.
- Quan hệ với các class khác: Triển khai Inbound Ports, gọi Outbound Ports.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `stateStorePort` | `MatchStateStorePort` | Cổng lưu trữ state. | null | Có |
| `configLoaderPort` | `MatchConfigLoaderPort` | Cổng nạp config. | null | Có |

## 3. Constructor
- Constructor đầy đủ: Nhận stateStorePort và configLoaderPort.

## 4. Getter / Setter

## 5. Phương thức
### registerTeam
- **Mục đích:** Đăng ký đội chơi.
- **Kiểu trả về:** `Team`
- **Tham số đầu vào:** String teamName
- **Giá trị trả về:** Trả về `Team`.
- **Business Logic:** Lấy state, gọi matchState.registerTeam, tạo 3 Agent, lưu state, trả về Team.
- **Validation:** teamName không trống.
- **Ngoại lệ:** MatchStateConflictException
- **Ảnh hưởng tới trạng thái object:** Thêm team mới vào lưu trữ.
- **Được gọi bởi:** MatchController
- **Gọi tới class nào:** MatchStateStorePort, MatchState, Team, Agent

### startMatch
- **Mục đích:** Bắt đầu trận đấu.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** Không
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Nạp config qua configLoaderPort, nạp state, gọi matchState.start, lưu state.
- **Validation:** Không
- **Ngoại lệ:** MatchStateConflictException
- **Ảnh hưởng tới trạng thái object:** Cập nhật status trận sang PLAYING.
- **Được gọi bởi:** MatchController
- **Gọi tới class nào:** MatchConfigLoaderPort, MatchStateStorePort, MatchState

### submitActions
- **Mục đích:** Nộp kế hoạch hành động.
- **Kiểu trả về:** `TurnSimulationResult`
- **Tham số đầu vào:** String teamName, int day, Map<String, List<Action>> agentPlans
- **Giá trị trả về:** Trả về `TurnSimulationResult`.
- **Business Logic:** Nạp config, nạp state, kiểm tra state PLAYING, validate kế hoạch hành động, nạp kế hoạch vào Agent, chạy MovementSimulator.simulateTeamTurn, set submittedPlan = true, lưu state, trả kết quả.
- **Validation:** Tên đội phải hợp lệ, day phải khớp currentTurn.
- **Ngoại lệ:** GameRuleViolationException, MatchStateConflictException, ResourceNotFoundException
- **Ảnh hưởng tới trạng thái object:** Cập nhật tọa độ/xăng Agent, cộng điểm Team, cập nhật submittedPlan.
- **Được gọi bởi:** MatchController
- **Gọi tới class nào:** MatchStateStorePort, MatchConfigLoaderPort, MatchState, ActionValidatorEngine, MovementSimulator, TurnSimulationResult

### getMatchState
- **Mục đích:** Lấy trạng thái trận đấu hiện tại.
- **Kiểu trả về:** `MatchState`
- **Tham số đầu vào:** Không
- **Giá trị trả về:** Trả về `MatchState`.
- **Business Logic:** Lấy state từ stateStorePort và trả về.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** MatchController
- **Gọi tới class nào:** MatchStateStorePort

### nextDay
- **Mục đích:** Tiến hành sang ngày chơi mới.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** Không
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Tăng currentTurn thêm 1. Nếu vượt quá maxTurns -> FINISHED. Reset xăng, bước đi Agent, reset Spots Udon, dọn dẹp actions cũ, cập nhật turnStartTime, lưu state.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Reset tài nguyên các Agent, Spots, actions.
- **Được gọi bởi:** SchedulerConfig
- **Gọi tới class nào:** MatchStateStorePort, MatchConfigLoaderPort, MatchState, Team, Agent, Spot

## 6. Quan hệ
- Triển khai Inbound Ports, phụ thuộc Outbound Ports.

## 7. Ghi chú triển khai
- Lớp điều phối chính.

---
# ActionMapper

## 1. Tổng quan
- Package: com.naprock.hexudon.application.mapper
- Vai trò: Mapper.
- Trách nhiệm: toDomainActionPlanMap, toDayActionResponse.
- Phạm vi sử dụng: Application Layer.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor
- Constructor mặc định.

## 4. Getter / Setter

## 5. Phương thức
### toDomainActionPlanMap
- **Mục đích:** Ánh xạ DTO sang domain map.
- **Kiểu trả về:** `Map<String, List<Action>>`
- **Tham số đầu vào:** DayActionRequest request
- **Giá trị trả về:** Trả về `Map<String, List<Action>>`.
- **Business Logic:** Duyệt request.agentPlans, map sang Action list.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** MatchController
- **Gọi tới class nào:** DayActionRequest, Action

### toDayActionResponse
- **Mục đích:** Ánh xạ kết quả sang DTO Response.
- **Kiểu trả về:** `DayActionResponse`
- **Tham số đầu vào:** TurnSimulationResult result
- **Giá trị trả về:** Trả về `DayActionResponse`.
- **Business Logic:** Map kết quả sang DayActionResponse.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** MatchController
- **Gọi tới class nào:** TurnSimulationResult, DayActionResponse

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Mapper.

---
# ActionRequest

## 1. Tổng quan
- Package: com.naprock.hexudon.application.dto
- Vai trò: DTO Record.
- Trách nhiệm: Đóng gói dữ liệu ActionRequest.
- Phạm vi sử dụng: Application Layer.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `order` | `int` | Trường dữ liệu order. | null | Có |
| `actionType` | `ActionType` | Trường dữ liệu actionType. | null | Có |
| `targetX` | `Integer` | Trường dữ liệu targetX. | null | Có |
| `targetY` | `Integer` | Trường dữ liệu targetY. | null | Có |

## 3. Constructor
- Constructor Record đầy đủ: Nhận tất cả các trường.

## 4. Getter / Setter
- Chỉ có Getter (Bất biến Record).

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Record

---
# ActionResponse

## 1. Tổng quan
- Package: com.naprock.hexudon.application.dto
- Vai trò: DTO Record.
- Trách nhiệm: Đóng gói dữ liệu ActionResponse.
- Phạm vi sử dụng: Application Layer.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `order` | `int` | Trường dữ liệu order. | null | Có |
| `actionType` | `ActionType` | Trường dữ liệu actionType. | null | Có |
| `targetX` | `Integer` | Trường dữ liệu targetX. | null | Có |
| `targetY` | `Integer` | Trường dữ liệu targetY. | null | Có |
| `timestamp` | `Long` | Trường dữ liệu timestamp. | null | Có |

## 3. Constructor
- Constructor Record đầy đủ: Nhận tất cả các trường.

## 4. Getter / Setter
- Chỉ có Getter (Bất biến Record).

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Record

---
# AgentActionPlanRequest

## 1. Tổng quan
- Package: com.naprock.hexudon.application.dto
- Vai trò: DTO Record.
- Trách nhiệm: Đóng gói dữ liệu AgentActionPlanRequest.
- Phạm vi sử dụng: Application Layer.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `agentId` | `String` | Trường dữ liệu agentId. | null | Có |
| `actions` | `List<ActionRequest>` | Trường dữ liệu actions. | null | Có |

## 3. Constructor
- Constructor Record đầy đủ: Nhận tất cả các trường.

## 4. Getter / Setter
- Chỉ có Getter (Bất biến Record).

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Record

---
# AgentActionPlanResponse

## 1. Tổng quan
- Package: com.naprock.hexudon.application.dto
- Vai trò: DTO Record.
- Trách nhiệm: Đóng gói dữ liệu AgentActionPlanResponse.
- Phạm vi sử dụng: Application Layer.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `agentId` | `String` | Trường dữ liệu agentId. | null | Có |
| `actions` | `List<ActionResponse>` | Trường dữ liệu actions. | null | Có |

## 3. Constructor
- Constructor Record đầy đủ: Nhận tất cả các trường.

## 4. Getter / Setter
- Chỉ có Getter (Bất biến Record).

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Record

---
# AgentResponse

## 1. Tổng quan
- Package: com.naprock.hexudon.application.dto
- Vai trò: DTO Record.
- Trách nhiệm: Đóng gói dữ liệu AgentResponse.
- Phạm vi sử dụng: Application Layer.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `String` | Trường dữ liệu id. | null | Có |
| `type` | `AgentType` | Trường dữ liệu type. | null | Có |
| `posX` | `int` | Trường dữ liệu posX. | null | Có |
| `posY` | `int` | Trường dữ liệu posY. | null | Có |
| `fuel` | `int` | Trường dữ liệu fuel. | null | Có |
| `remainingSteps` | `int` | Trường dữ liệu remainingSteps. | null | Có |

## 3. Constructor
- Constructor Record đầy đủ: Nhận tất cả các trường.

## 4. Getter / Setter
- Chỉ có Getter (Bất biến Record).

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Record

---
# CellResponse

## 1. Tổng quan
- Package: com.naprock.hexudon.application.dto
- Vai trò: DTO Record.
- Trách nhiệm: Đóng gói dữ liệu CellResponse.
- Phạm vi sử dụng: Application Layer.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `x` | `int` | Trường dữ liệu x. | null | Có |
| `y` | `int` | Trường dữ liệu y. | null | Có |
| `terrainType` | `TerrainType` | Trường dữ liệu terrainType. | null | Có |

## 3. Constructor
- Constructor Record đầy đủ: Nhận tất cả các trường.

## 4. Getter / Setter
- Chỉ có Getter (Bất biến Record).

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Record

---
# DayActionRequest

## 1. Tổng quan
- Package: com.naprock.hexudon.application.dto
- Vai trò: DTO Record.
- Trách nhiệm: Đóng gói dữ liệu DayActionRequest.
- Phạm vi sử dụng: Application Layer.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `day` | `int` | Trường dữ liệu day. | null | Có |
| `agentPlans` | `List<AgentActionPlanRequest>` | Trường dữ liệu agentPlans. | null | Có |

## 3. Constructor
- Constructor Record đầy đủ: Nhận tất cả các trường.

## 4. Getter / Setter
- Chỉ có Getter (Bất biến Record).

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Record

---
# DayActionResponse

## 1. Tổng quan
- Package: com.naprock.hexudon.application.dto
- Vai trò: DTO Record.
- Trách nhiệm: Đóng gói dữ liệu DayActionResponse.
- Phạm vi sử dụng: Application Layer.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `day` | `int` | Trường dữ liệu day. | null | Có |
| `agentPlans` | `List<AgentActionPlanResponse>` | Trường dữ liệu agentPlans. | null | Có |

## 3. Constructor
- Constructor Record đầy đủ: Nhận tất cả các trường.

## 4. Getter / Setter
- Chỉ có Getter (Bất biến Record).

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Record

---
# MatchStateResponse

## 1. Tổng quan
- Package: com.naprock.hexudon.application.dto
- Vai trò: DTO Record.
- Trách nhiệm: Đóng gói dữ liệu MatchStateResponse.
- Phạm vi sử dụng: Application Layer.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `status` | `MatchStatus` | Trường dữ liệu status. | null | Có |
| `currentTurn` | `int` | Trường dữ liệu currentTurn. | null | Có |
| `teams` | `List<TeamResponse>` | Trường dữ liệu teams. | null | Có |
| `cells` | `List<CellResponse>` | Trường dữ liệu cells. | null | Có |
| `currentTurnActions` | `Map<String, ActionResponse>` | Trường dữ liệu currentTurnActions. | null | Có |
| `spots` | `List<Spot>` | Trường dữ liệu spots. | null | Có |

## 3. Constructor
- Constructor Record đầy đủ: Nhận tất cả các trường.

## 4. Getter / Setter
- Chỉ có Getter (Bất biến Record).

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Record

---
# TeamActionRequest

## 1. Tổng quan
- Package: com.naprock.hexudon.application.dto
- Vai trò: DTO Record.
- Trách nhiệm: Đóng gói dữ liệu TeamActionRequest.
- Phạm vi sử dụng: Application Layer.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `teamName` | `String` | Trường dữ liệu teamName. | null | Có |
| `day` | `int` | Trường dữ liệu day. | null | Có |
| `agentPlans` | `List<AgentActionPlanRequest>` | Trường dữ liệu agentPlans. | null | Có |

## 3. Constructor
- Constructor Record đầy đủ: Nhận tất cả các trường.

## 4. Getter / Setter
- Chỉ có Getter (Bất biến Record).

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Record

---
# TeamActionResponse

## 1. Tổng quan
- Package: com.naprock.hexudon.application.dto
- Vai trò: DTO Record.
- Trách nhiệm: Đóng gói dữ liệu TeamActionResponse.
- Phạm vi sử dụng: Application Layer.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `teamName` | `String` | Trường dữ liệu teamName. | null | Có |
| `day` | `int` | Trường dữ liệu day. | null | Có |
| `agentPlans` | `List<AgentActionPlanResponse>` | Trường dữ liệu agentPlans. | null | Có |

## 3. Constructor
- Constructor Record đầy đủ: Nhận tất cả các trường.

## 4. Getter / Setter
- Chỉ có Getter (Bất biến Record).

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Record

---
# TeamRegisterRequest

## 1. Tổng quan
- Package: com.naprock.hexudon.application.dto
- Vai trò: DTO Record.
- Trách nhiệm: Đóng gói dữ liệu TeamRegisterRequest.
- Phạm vi sử dụng: Application Layer.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `teamName` | `String` | Trường dữ liệu teamName. | null | Có |

## 3. Constructor
- Constructor Record đầy đủ: Nhận tất cả các trường.

## 4. Getter / Setter
- Chỉ có Getter (Bất biến Record).

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Record

---
# TeamResponse

## 1. Tổng quan
- Package: com.naprock.hexudon.application.dto
- Vai trò: DTO Record.
- Trách nhiệm: Đóng gói dữ liệu TeamResponse.
- Phạm vi sử dụng: Application Layer.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `teamName` | `String` | Trường dữ liệu teamName. | null | Có |
| `agents` | `List<AgentResponse>` | Trường dữ liệu agents. | null | Có |

## 3. Constructor
- Constructor Record đầy đủ: Nhận tất cả các trường.

## 4. Getter / Setter
- Chỉ có Getter (Bất biến Record).

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Record

---
# MatchController

## 1. Tổng quan
- Package: com.naprock.hexudon.adapter.in.rest
- Vai trò: REST API Controller.
- Trách nhiệm: Định nghĩa endpoint REST API, tiếp nhận requests, gọi UseCases tương ứng.
- Phạm vi sử dụng: REST API Layer.
- Quan hệ với các class khác: Gọi UseCases, sử dụng ActionMapper.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `registerTeamUseCase` | `RegisterTeamUseCase` | Usecase đăng ký team. | null | Có |
| `startMatchUseCase` | `StartMatchUseCase` | Usecase start match. | null | Có |
| `submitActionsUseCase` | `SubmitActionsUseCase` | Usecase submit actions. | null | Có |
| `getMatchStateUseCase` | `GetMatchStateUseCase` | Usecase get state. | null | Có |
| `actionMapper` | `ActionMapper` | Bộ mapper chuyển đổi. | null | Có |

## 3. Constructor
- Constructor đầy đủ: Nhận các Use Cases và ActionMapper để thực hiện tiêm phụ thuộc.

## 4. Getter / Setter

## 5. Phương thức
### registerTeam
- **Mục đích:** Đăng ký đội chơi mới.
- **Kiểu trả về:** `TeamResponse`
- **Tham số đầu vào:** @Valid @RequestBody TeamRegisterRequest request
- **Giá trị trả về:** Trả về `TeamResponse`.
- **Business Logic:** Gọi registerTeamUseCase, map kết quả sang TeamResponse.
- **Validation:** @Valid check teamName không trống.
- **Ngoại lệ:** MatchStateConflictException
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** Spring Boot REST Engine
- **Gọi tới class nào:** RegisterTeamUseCase, ActionMapper, TeamResponse

### startMatch
- **Mục đích:** Khởi động trận đấu.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** Không
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Gọi startMatchUseCase.startMatch().
- **Validation:** Không
- **Ngoại lệ:** MatchStateConflictException
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** Spring Boot REST Engine
- **Gọi tới class nào:** StartMatchUseCase

### getMatchState
- **Mục đích:** Lấy trạng thái trận đấu.
- **Kiểu trả về:** `MatchStateResponse`
- **Tham số đầu vào:** Không
- **Giá trị trả về:** Trả về `MatchStateResponse`.
- **Business Logic:** Gọi getMatchStateUseCase, map sang MatchStateResponse.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** Spring Boot REST Engine
- **Gọi tới class nào:** GetMatchStateUseCase, MatchStateResponse

### submitActions
- **Mục đích:** Nộp kế hoạch hành động.
- **Kiểu trả về:** `DayActionResponse`
- **Tham số đầu vào:** @RequestHeader("X-Team-Name") String teamName, @Valid @RequestBody DayActionRequest request
- **Giá trị trả về:** Trả về `DayActionResponse`.
- **Business Logic:** Map request sang domain plans, gọi submitActionsUseCase, map kết quả trả về DayActionResponse.
- **Validation:** @RequestHeader bắt buộc, @Valid request body.
- **Ngoại lệ:** GameRuleViolationException, MatchStateConflictException, ResourceNotFoundException
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** Spring Boot REST Engine
- **Gọi tới class nào:** SubmitActionsUseCase, ActionMapper, DayActionResponse

## 6. Quan hệ
- Phụ thuộc Inbound Ports.

## 7. Ghi chú triển khai
- Spring `@RestController`.

---
# InMemoryMatchStateRepository

## 1. Tổng quan
- Package: com.naprock.hexudon.adapter.out.persistence
- Vai trò: Persistence Adapter in-memory.
- Trách nhiệm: Triển khai lưu trữ MatchState in-memory.
- Phạm vi sử dụng: Adapter Layer.
- Quan hệ với các class khác: Triển khai MatchStateStorePort và MatchStateRepository.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `matchState` | `MatchState` | Đối tượng lưu trạng thái in-memory. | null | Có |

## 3. Constructor
- Constructor mặc định: Khởi tạo matchState mới (status = WAITING).

## 4. Getter / Setter

## 5. Phương thức
### loadState
- **Mục đích:** Nạp trạng thái trận.
- **Kiểu trả về:** `MatchState`
- **Tham số đầu vào:** Không
- **Giá trị trả về:** Trả về `MatchState`.
- **Business Logic:** Trả về tham chiếu matchState hiện tại.
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** MatchApplicationService
- **Gọi tới class nào:** Không

### saveState
- **Mục đích:** Lưu trạng thái trận đấu.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** MatchState state
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Gán matchState = state (sử dụng synchronized để an toàn đa luồng).
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Cập nhật biến in-memory matchState.
- **Được gọi bởi:** MatchApplicationService
- **Gọi tới class nào:** Không

## 6. Quan hệ
- Triển khai MatchStateStorePort và MatchStateRepository.

## 7. Ghi chú triển khai
- Singleton Bean.

---
# FileMatchConfigLoader

## 1. Tổng quan
- Package: com.naprock.hexudon.adapter.out.loader
- Vai trò: Outbound Config Loader Adapter.
- Trách nhiệm: Đọc file match_config.txt từ tài nguyên.
- Phạm vi sử dụng: Adapter Layer.
- Quan hệ với các class khác: Triển khai MatchConfigLoaderPort, sử dụng FileUtils.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `configFilePath` | `String` | Đường dẫn file cấu hình. | match_config.txt | Có |

## 3. Constructor
- Constructor mặc định.
- Constructor với đường dẫn file: Nhận configFilePath.

## 4. Getter / Setter

## 5. Phương thức
### loadConfig
- **Mục đích:** Đọc file cấu hình game.
- **Kiểu trả về:** `MatchConfig`
- **Tham số đầu vào:** Không
- **Giá trị trả về:** Trả về `MatchConfig`.
- **Business Logic:** Gọi FileUtils.readLinesFromResource. Phân tích cú pháp key=value từng dòng, nạp vào MatchConfig.
- **Validation:** Không
- **Ngoại lệ:** ConfigLoadException nếu file lỗi hoặc I/O error
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** MatchApplicationService.startMatch
- **Gọi tới class nào:** FileUtils, MatchConfig

## 6. Quan hệ
- Triển khai MatchConfigLoaderPort.

## 7. Ghi chú triển khai
- Singleton Bean.

---
# AdapterBeanConfig

## 1. Tổng quan
- Package: com.naprock.hexudon.adapter.out.configuration
- Vai trò: Java Config Bean.
- Trách nhiệm: Khởi tạo bean MatchApplicationService.
- Phạm vi sử dụng: Adapter Layer.
- Quan hệ với các class khác: Khởi tạo MatchApplicationService.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor

## 4. Getter / Setter

## 5. Phương thức
### matchApplicationService
- **Mục đích:** Khởi tạo Service Bean.
- **Kiểu trả về:** `MatchApplicationService`
- **Tham số đầu vào:** MatchStateStorePort storePort, MatchConfigLoaderPort loaderPort
- **Giá trị trả về:** Trả về `MatchApplicationService`.
- **Business Logic:** new MatchApplicationService(storePort, loaderPort).
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** Spring Container
- **Gọi tới class nào:** MatchApplicationService

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Spring `@Configuration`.

---
# AppConfig

## 1. Tổng quan
- Package: com.naprock.hexudon.infrastructure.configuration
- Vai trò: Spring Java Config.
- Trách nhiệm: Khởi tạo các repository và loader bean.
- Phạm vi sử dụng: Infrastructure Layer.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor

## 4. Getter / Setter

## 5. Phương thức
Không có phương thức nghiệp vụ đặc biệt.

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Spring `@Configuration`.

---
# WebConfig

## 1. Tổng quan
- Package: com.naprock.hexudon.infrastructure.configuration
- Vai trò: Web MVC Config.
- Trách nhiệm: Cấu hình CORS, đăng ký RateLimiterInterceptor.
- Phạm vi sử dụng: Infrastructure Layer.
- Quan hệ với các class khác: Đăng ký RateLimiterInterceptor.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `rateLimiterInterceptor` | `RateLimiterInterceptor` | Interceptor giới hạn request. | null | Có |

## 3. Constructor
- Constructor tiêm rateLimiterInterceptor.

## 4. Getter / Setter

## 5. Phương thức
### addInterceptors
- **Mục đích:** Đăng ký Interceptor.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** InterceptorRegistry registry
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** registry.addInterceptor(rateLimiterInterceptor).addPathPatterns("/api/match/actions").
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Đăng ký interceptor vào Web Engine.
- **Được gọi bởi:** Spring Framework
- **Gọi tới class nào:** RateLimiterInterceptor

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Đăng ký khớp path /api/match/actions (có s).

---
# RateLimiterInterceptor

## 1. Tổng quan
- Package: com.naprock.hexudon.infrastructure.interceptor
- Vai trò: Interceptor kiểm soát rate-limit.
- Trách nhiệm: Đo số lượng request gửi lên của team. Nếu vi phạm, tăng vi phạm và ném ngoại lệ.
- Phạm vi sử dụng: Infrastructure Layer.
- Quan hệ với các class khác: Bảo vệ endpoint API.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `submitActionsUseCase` | `SubmitActionsUseCase` | UseCase kiểm tra team. | null | Có |
| `requestCounts` | `Map<String, List<Long>>` | Map lưu các timestamp request của các team. | {} | Có |

## 3. Constructor
- Constructor tiêm submitActionsUseCase.

## 4. Getter / Setter

## 5. Phương thức
### preHandle
- **Mục đích:** Chặn request kiểm tra rate limit.
- **Kiểu trả về:** `boolean`
- **Tham số đầu vào:** HttpServletRequest request, HttpServletResponse response, Object handler
- **Giá trị trả về:** Trả về `boolean`.
- **Business Logic:** Lấy X-Team-Name. Đếm số request trong 1 giây qua map. Nếu quá, tăng spamViolationCount của team và ném RateLimitExceededException.
- **Validation:** Không
- **Ngoại lệ:** RateLimitExceededException (Mã lỗi RATE_LIMIT_EXCEEDED)
- **Ảnh hưởng tới trạng thái object:** Có thể cập nhật spamViolationCount của team.
- **Được gọi bởi:** Spring Web MVC
- **Gọi tới class nào:** SubmitActionsUseCase, Team

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Thread-safe map is required.

---
# SchedulerConfig

## 1. Tổng quan
- Package: com.naprock.hexudon.infrastructure.scheduler
- Vai trò: Lập lịch chạy ngầm chuyển lượt.
- Trách nhiệm: Định kỳ kích hoạt chuyển ngày đấu tự động.
- Phạm vi sử dụng: Infrastructure Layer.
- Quan hệ với các class khác: Gọi UseCase.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |
| `getMatchStateUseCase` | `GetMatchStateUseCase` | Lấy state. | null | Có |
| `matchAppService` | `MatchApplicationService` | Application Service điều phối. | null | Có |

## 3. Constructor
- Constructor tiêm getMatchStateUseCase, matchAppService.

## 4. Getter / Setter

## 5. Phương thức
### checkAndTriggerNextDay
- **Mục đích:** Kích hoạt chuyển ngày tự động.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** Không
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** Chạy mỗi giây. Nếu state PLAYING và: (currentTime - turnStartTime > turnTimeLimitMs HOẶC cả hai team đã submittedPlan) -> gọi matchAppService.nextDay().
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Có thể chuyển ngày đấu sang ngày tiếp theo.
- **Được gọi bởi:** Spring Task Scheduler
- **Gọi tới class nào:** GetMatchStateUseCase, MatchApplicationService

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Spring `@Scheduled(fixedRate = 1000)`.

---
# FileUtils

## 1. Tổng quan
- Package: com.naprock.hexudon.infrastructure.util
- Vai trò: Tiện ích tệp tin.
- Trách nhiệm: Đọc tệp tin cấu hình vật lý.
- Phạm vi sử dụng: Infrastructure Layer.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor

## 4. Getter / Setter

## 5. Phương thức
### readLinesFromResource
- **Mục đích:** Đọc tất cả các dòng của file trong resource folder.
- **Kiểu trả về:** `List<String>`
- **Tham số đầu vào:** String filePath
- **Giá trị trả về:** Trả về `List<String>`.
- **Business Logic:** Đọc file từ classpath, trả về List các dòng.
- **Validation:** filePath không rỗng.
- **Ngoại lệ:** ConfigLoadException nếu nạp file lỗi
- **Ảnh hưởng tới trạng thái object:** Không
- **Được gọi bởi:** FileMatchConfigLoader
- **Gọi tới class nào:** Không

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Stateless Utility.

---
# HexudonApplication

## 1. Tổng quan
- Package: com.naprock.hexudon
- Vai trò: Bootstrap Class.
- Trách nhiệm: Khởi chạy ứng dụng Spring Boot.
- Phạm vi sử dụng: Application root.
- Quan hệ với các class khác: Không.

## 2. Thuộc tính
| Tên | Kiểu | Mô tả | Giá trị mặc định | Bắt buộc |
| :--- | :--- | :--- | :--- | :--- |

## 3. Constructor

## 4. Getter / Setter

## 5. Phương thức
### main
- **Mục đích:** Khởi chạy main.
- **Kiểu trả về:** `void`
- **Tham số đầu vào:** String[] args
- **Giá trị trả về:** Trả về `void`.
- **Business Logic:** SpringApplication.run(HexudonApplication.class, args).
- **Validation:** Không
- **Ngoại lệ:** Không
- **Ảnh hưởng tới trạng thái object:** Khởi chạy Spring Boot context.
- **Được gọi bởi:** JVM
- **Gọi tới class nào:** Không

## 6. Quan hệ
- Không

## 7. Ghi chú triển khai
- Spring Boot entry point.

---

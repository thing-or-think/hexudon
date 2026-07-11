# ĐẶC TẢ KỸ THUẬT CHI TIẾT CÁC LỚP (CLASS SPECIFICATIONS) - GIAI ĐOẠN 4

Tài liệu này chứa thông số đặc tả kỹ thuật chi tiết của tất cả các thực thể, đối tượng giá trị, cổng giao tiếp và dịch vụ miền được phát triển trong Giai đoạn 4.

---

## 1. Hệ thống giao thông động (Traffic Flow)

### 1.1. Lớp liệt kê: RoadTrafficState (Enum)
*   **Package:** `com.naprock.hexudon.domain.model.traffic`
*   **Phạm vi truy cập:** Public
*   **Ý nghĩa:** Trạng thái giao thông của một ô đường đi.
*   **Các giá trị định nghĩa:**
    *   `SMOOTH`: Thông thoáng (lưu lượng thấp).
    *   `CONGESTED`: Ùn ứ (lưu lượng trung bình).
    *   `TRAFFIC_JAM`: Kẹt xe (lưu lượng cao).

### 1.2. Đối tượng giá trị: TrafficFlow (Value Object)
*   **Package:** `com.naprock.hexudon.domain.model.traffic`
*   **Phạm vi truy cập:** Public
*   **Ý nghĩa:** Đại diện cho trạng thái giao thông tính toán tại một tọa độ ô đường.
*   **Các thuộc tính (Fields):**
    *   `coordinate` (Kiểu: `Coordinate`): Tọa độ của ô đường đi trên bản đồ.
    *   `flowValue` (Kiểu: `double`): Lưu lượng giao thông tính toán thực tế.
    *   `trafficState` (Kiểu: `RoadTrafficState`): Trạng thái giao thông được phân định.

### 1.3. Đối tượng giá trị: TrafficThreshold (Value Object)
*   **Package:** `com.naprock.hexudon.domain.model.traffic`
*   **Phạm vi truy cập:** Public
*   **Ý nghĩa:** Cấu hình ngưỡng để phân chia các mức độ giao thông.
*   **Các thuộc tính (Fields):**
    *   `congestionThreshold` (Kiểu: `double`): Ngưỡng để kích hoạt trạng thái ùn ứ (`CONGESTED`).
    *   `trafficJamThreshold` (Kiểu: `double`): Ngưỡng để kích hoạt trạng thái kẹt xe (`TRAFFIC_JAM`).
*   **Các phương thức (Methods):**
    *   `determineState`: Trả về `RoadTrafficState`. Tham số: `flowValue` (double). Ý nghĩa: Phân loại trạng thái giao thông dựa trên giá trị lưu lượng đầu vào và các ngưỡng.

### 1.4. Thực thể: TrafficHistory (Entity)
*   **Package:** `com.naprock.hexudon.domain.model.traffic`
*   **Phạm vi truy cập:** Public
*   **Ý nghĩa:** Lưu trữ số lượt dừng chân thực tế của Agent tại một ô đường trong một lượt cụ thể.
*   **Các thuộc tính (Fields):**
    *   `id` (Kiểu: `String`): Mã định danh duy nhất của bản ghi lịch sử.
    *   `turnNumber` (Kiểu: `int`): Thứ tự lượt chơi (Turn) diễn ra sự kiện.
    *   `coordinate` (Kiểu: `Coordinate`): Tọa độ ô đường đi được ghi nhận.
    *   `staySteps` (Kiểu: `int`): Tổng số bước mà các Agent của mọi đội dừng chân tại ô này.

### 1.5. Dịch vụ miền: TrafficCalculator (Domain Service)
*   **Package:** `com.naprock.hexudon.domain.service.traffic`
*   **Phạm vi truy cập:** Public
*   **Mối quan hệ:** Phụ thuộc vào `TrafficThreshold` và `TrafficHistory`.
*   **Các phương thức (Methods):**
    *   `calculateFlow`: Trả về `TrafficFlow`. Tham số đầu vào:
        *   `currentTurn` (int): Lượt chơi hiện tại cần tính toán giao thông.
        *   `coord` (Coordinate): Tọa độ ô đường cần tính.
        *   `histories` (List của `TrafficHistory`): Danh sách dữ liệu lịch sử các lượt trước.
        *   `teamCount` (int): Tổng số đội chơi tham gia vòng đấu.
        *   `threshold` (TrafficThreshold): Ngưỡng giao thông cấu hình.
*   **Thuật toán từng bước tính toán Calculated Flow:**
    1.  Kiểm tra điều kiện biên: Nếu số lượng đội chơi (`teamCount`) nhỏ hơn hoặc bằng 0, gán giá trị lưu lượng (`flowValue`) bằng 0.0, thiết lập trạng thái là `SMOOTH` và trả về đối tượng `TrafficFlow` ngay lập tức.
    2.  Xác định hai lượt đấu trong quá khứ cần lấy dữ liệu:
        *   Lượt thứ nhất liền trước: `T_1 = currentTurn - 1`.
        *   Lượt thứ hai liền trước: `T_2 = currentTurn - 2`.
    3.  Tìm kiếm trong danh sách `histories` số lượt dừng chân tại tọa độ `coord` ở lượt `T_1`. Nếu tìm thấy, gán giá trị vào biến `stepsT1`. Nếu không tìm thấy, gán `stepsT1 = 0`.
    4.  Xác định số lượt dừng chân tại tọa độ `coord` ở lượt `T_2`. Tiến hành kiểm tra:
        *   Nếu lượt hiện tại `currentTurn` bằng 2: Do lượt `T_2` (Lượt 0) không tồn tại, áp dụng nguyên tắc "ngày thứ 2 lùi lại chỉ lấy dữ liệu của ngày thứ 1 liền trước". Nghĩa là lấy giá trị của Lượt 1. Gán `stepsT2 = stepsT1`.
        *   Nếu lượt hiện tại `currentTurn` lớn hơn hoặc bằng 3: Tìm kiếm dữ liệu lịch sử ở lượt `T_2`. Nếu tìm thấy, gán giá trị vào biến `stepsT2`. Nếu không tìm thấy, gán `stepsT2 = 0`.
    5.  Tính tổng số bước dừng chân của cả 2 lượt trước đó: `totalSteps = stepsT1 + stepsT2`.
    6.  Tính toán giá trị mật độ giao thông trung bình trên mỗi đội chơi: `calculatedFlow = totalSteps / (2.0 * teamCount)`.
    7.  Gọi phương thức `threshold.determineState(calculatedFlow)` để tìm ra trạng thái giao thông tương ứng (`RoadTrafficState`).
    8.  Khởi tạo đối tượng `TrafficFlow` với tọa độ `coord`, giá trị `calculatedFlow` và trạng thái giao thông vừa xác định, sau đó trả về.

---

## 2. Chi phí di chuyển địa hình (Terrain & Movement Cost)

### 2.1. Đối tượng giá trị: MovementCost (Value Object)
*   **Package:** `com.naprock.hexudon.domain.model.cost`
*   **Phạm vi truy cập:** Public
*   **Ý nghĩa:** Đại diện cho chi phí di chuyển đã chốt cố định của Agent cho một bước di chuyển.
*   **Các thuộc tính (Fields):**
    *   `fuelCost` (Kiểu: `int`): Lượng xăng hao phí cần thiết.
    *   `stepCost` (Kiểu: `int`): Số bước đi bị tiêu hao trong lượt chơi.

### 2.2. Dịch vụ miền: MovementCostCalculator (Domain Service)
*   **Package:** `com.naprock.hexudon.domain.service.cost`
*   **Phạm vi truy cập:** Public
*   **Các phương thức (Methods):**
    *   `calculateMovementCost`: Trả về `MovementCost`. Tham số đầu vào:
        *   `targetCell` (Cell): Ô bản đồ đích đến của Agent.
        *   `trafficState` (RoadTrafficState): Trạng thái giao thông hiện tại của ô bản đồ đích đến (chỉ có tác dụng với địa hình `ROAD`).
        *   `config` (MatchConfig): Cấu hình của trận đấu.
*   **Thuật toán từng bước tính toán chi phí di chuyển:**
    1.  Trích xuất kiểu địa hình (`TerrainType`) của ô đích đến `targetCell`.
    2.  Kiểm tra điều kiện đi lại: Nếu kiểu địa hình là `POND` (Hồ nước), ném ra ngoại lệ `GameRuleViolationException` với mã lỗi tương ứng báo hiệu ô đích không thể đi vào.
    3.  Thực hiện phân nhánh tính toán theo kiểu địa hình:
        *   **Nếu địa hình là PLAIN (Đồng bằng):**
            *   Gán `fuelCost = config.plainFuelCost()`.
            *   Gán `stepCost = config.plainStepCost()`.
        *   **Nếu địa hình là MOUNTAIN (Núi):**
            *   Gán `fuelCost = config.mountainFuelCost()`.
            *   Gán `stepCost = config.mountainStepCost()`.
        *   **Nếu địa hình là ROAD (Đường đi):**
            *   Lấy chi phí cơ sở từ cấu hình: `baseFuel = config.roadFuelCost()`, `baseStep = config.roadStepCost()`.
            *   Kiểm tra `trafficState` của ô đường để áp dụng hệ số phạt ùn tắc giao thông:
                *   Nếu `trafficState` là `SMOOTH` (Thông thoáng): Hệ số phạt bằng 1. Tính toán: `fuelCost = baseFuel`, `stepCost = baseStep`.
                *   Nếu `trafficState` là `CONGESTED` (Ùn ứ): Hệ số phạt bằng 2. Tính toán: `fuelCost = baseFuel * 2`, `stepCost = baseStep * 2`.
                *   Nếu `trafficState` là `TRAFFIC_JAM` (Kẹt xe): Hệ số phạt nhiên liệu bằng 4, bước đi bằng 3. Tính toán: `fuelCost = baseFuel * 4`, `stepCost = baseStep * 3`.
    4.  Khởi tạo đối tượng `MovementCost` mới chứa các giá trị `fuelCost` và `stepCost` vừa tính toán và trả về đối tượng này.

---

## 3. Hệ thống tính điểm (Scoring System)

### 3.1. Đối tượng giá trị: UdonType (Value Object)
*   **Package:** `com.naprock.hexudon.domain.model.scoring`
*   **Phạm vi truy cập:** Public
*   **Ý nghĩa:** Loại Udon độc nhất mà Agent có thể thu thập được.
*   **Các thuộc tính (Fields):**
    *   `id` (Kiểu: `String`): Mã định danh duy nhất của loại Udon.

### 3.2. Thực thể: TeamScore (Entity)
*   **Package:** `com.naprock.hexudon.domain.model.scoring`
*   **Phạm vi truy cập:** Public
*   **Ý nghĩa:** Lưu trữ và theo dõi các chỉ số điểm số chi tiết của một đội chơi.
*   **Các thuộc tính (Fields):**
    *   `teamName` (Kiểu: `String`): Tên của đội chơi được tính điểm.
    *   `collectedUdonTypes` (Kiểu: Set của `UdonType`): Tập hợp lưu trữ các loại Udon độc nhất đội chơi đã thu thập được từ trước đến nay.
    *   `dailyUdonVolumes` (Kiểu: Map của `Integer` và `Integer`): Lưu trữ khối lượng Udon thu thập theo từng ngày. Key là số thứ tự lượt (Turn), Value là số lượng Udon thu thập được trong lượt đó.
    *   `totalServings` (Kiểu: `int`): Tổng số lượt phục vụ Udon thành công.
    *   `totalResponseTimeMs` (Kiểu: `long`): Tổng thời gian phản hồi API tích lũy của đội chơi (mili-giây).
*   **Các phương thức (Methods):**
    *   `getUniqueUdonCount`: Trả về `int`. Ý nghĩa: Trả về số lượng phần tử của tập hợp `collectedUdonTypes`.
    *   `getAccumulatedDailyUdon`: Trả về `int`. Ý nghĩa: Cộng tổng tất cả các giá trị (Value) có trong bản đồ `dailyUdonVolumes`.
    *   `addUdonCollection`: Trả về `void`. Tham số: `type` (UdonType), `turn` (int), `amount` (int). Ý nghĩa: Thêm loại Udon mới thu thập và cập nhật khối lượng Udon tích lũy trong ngày.
    *   `incrementServings`: Trả về `void`. Ý nghĩa: Tăng tổng số lượt phục vụ thành công lên 1 đơn vị.
    *   `addResponseTime`: Trả về `void`. Tham số: `responseTimeMs` (long). Ý nghĩa: Cộng dồn thời gian phản hồi của request vào biến tích lũy.

### 3.3. Thực thể: MatchScore (Entity)
*   **Package:** `com.naprock.hexudon.domain.model.scoring`
*   **Phạm vi truy cập:** Public
*   **Ý nghĩa:** Tổng hợp điểm số của toàn trận đấu.
*   **Các thuộc tính (Fields):**
    *   `matchId` (Kiểu: `String`): Mã định danh trận đấu.
    *   `teamScores` (Kiểu: Map của `String` và `TeamScore`): Điểm số chi tiết các đội. Key là tên đội.

### 3.4. Luồng xử lý và Lưu trữ Điểm số (Persistence Routing)
1.  **Thực thi hành động:** Khi Agent hoàn thành một hành động hợp lệ (như thu thập Udon từ Spot), thực thể `PatrolAgent` gọi phương thức cập nhật tương ứng trên `Team` và `TeamScore`.
2.  **Thời gian cập nhật:**
    *   Cập nhật thời gian phản hồi: Thực hiện ngay khi kết thúc xử lý yêu cầu API gửi hành động từ đội chơi.
    *   Cập nhật loại Udon và Servings: Thực hiện ngay sau khi bước đi của Agent tại vị trí Spot được giả lập thành công.
    *   Cập nhật tích lũy ngày: Khi kết thúc Turn, hệ thống tổng kết toàn bộ số Udon thu hoạch trong ngày và chốt giá trị vào bản đồ `dailyUdonVolumes`.
3.  **Lưu trữ dữ liệu:** Tầng Application kích hoạt Outbound Port `TeamScoreRepositoryPort` để đồng bộ thực thể `MatchScore` và `TeamScore` xuống cơ sở dữ liệu ở cuối mỗi lượt chơi (Turn).

---

## 4. Hệ thống xếp hạng đấu trường (Ranking System)

### 4.1. Đối tượng giá trị: RankingCriteria (Value Object)
*   **Package:** `com.naprock.hexudon.domain.service.ranking`
*   **Phạm vi truy cập:** Public
*   **Ý nghĩa:** Nhóm các thông số xếp hạng làm tiêu chí so sánh.
*   **Các thuộc tính (Fields):**
    *   `uniqueUdonCount` (Kiểu: `int`): Số lượng loại Udon độc nhất đã thu thập.
    *   `accumulatedDailyUdon` (Kiểu: `int`): Tổng tích lũy Udon theo ngày.
    *   `totalServings` (Kiểu: `int`): Tổng số lượt phục vụ thành công.
    *   `totalResponseTimeMs` (Kiểu: `long`): Tổng thời gian phản hồi tích lũy (ms).
    *   `diceValue` (Kiểu: `int`): Giá trị tung xúc xắc ngẫu nhiên (sử dụng khi hòa điểm).

### 4.2. Dịch vụ miền: RankingService (Domain Service)
*   **Package:** `com.naprock.hexudon.domain.service.ranking`
*   **Phạm vi truy cập:** Public
*   **Các phương thức (Methods):**
    *   `compareTeams`: Trả về `int` (1 nếu tiêu chí 1 xếp trên, -1 nếu tiêu chí 2 xếp trên, 0 nếu hòa). Tham số: `c1` (RankingCriteria), `c2` (RankingCriteria).
    *   `resolveTie`: Trả về `int`. Tham số: `c1` (RankingCriteria), `c2` (RankingCriteria).
*   **Thuật toán compareTeams() phân định thứ tự ưu tiên tuyệt đối (Anti-tie-break):**
    1.  So sánh số loại Udon độc nhất:
        *   Nếu `c1.uniqueUdonCount` lớn hơn `c2.uniqueUdonCount`, trả về 1.
        *   Nếu `c1.uniqueUdonCount` nhỏ hơn `c2.uniqueUdonCount`, trả về -1.
        *   Nếu bằng nhau, chuyển sang bước 2.
    2.  So sánh tổng tích lũy Udon theo ngày:
        *   Nếu `c1.accumulatedDailyUdon` lớn hơn `c2.accumulatedDailyUdon`, trả về 1.
        *   Nếu `c1.accumulatedDailyUdon` nhỏ hơn `c2.accumulatedDailyUdon`, trả về -1.
        *   Nếu bằng nhau, chuyển sang bước 3.
    3.  So sánh tổng số lần phục vụ thành công:
        *   Nếu `c1.totalServings` lớn hơn `c2.totalServings`, trả về 1.
        *   Nếu `c1.totalServings` nhỏ hơn `c2.totalServings`, trả về -1.
        *   Nếu bằng nhau, chuyển sang bước 4.
    4.  So sánh tổng thời gian phản hồi API (đội phản hồi nhanh hơn xếp trên):
        *   Nếu `c1.totalResponseTimeMs` nhỏ hơn `c2.totalResponseTimeMs`, trả về 1.
        *   Nếu `c1.totalResponseTimeMs` lớn hơn `c2.totalResponseTimeMs`, trả về -1.
        *   Nếu bằng nhau, chuyển sang bước 5.
    5.  Xảy ra hòa điểm tuyệt đối. Kích hoạt cơ chế tung xúc xắc: Gọi phương thức giải quyết hòa điểm `resolveTie(c1, c2)` và trả về kết quả của phương thức này.

*   **Thuật toán giải quyết hòa điểm resolveTie():**
    1.  Trích xuất giá trị xúc xắc `c1.diceValue` và `c2.diceValue`.
    2.  Nếu `c1.diceValue` lớn hơn `c2.diceValue`, trả về 1.
    3.  Nếu `c1.diceValue` nhỏ hơn `c2.diceValue`, trả về -1.
    4.  Nếu hai giá trị xúc xắc bằng nhau: Thực hiện tung xúc xắc ngẫu nhiên mới cho cả 2 tiêu chí. Gán giá trị mới và lặp lại việc so sánh cho đến khi phân định được bên có giá trị lớn hơn. Trả về kết quả so sánh cuối cùng.

---

## 5. Lịch sử trận đấu & Log sự kiện (Game Event History)

### 5.1. Thực thể: GameEvent (Entity)
*   **Package:** `com.naprock.hexudon.domain.model.history`
*   **Phạm vi truy cập:** Public
*   **Ý nghĩa:** Ghi nhận một sự kiện nghiệp vụ chi tiết xảy ra trong game.
*   **Các thuộc tính (Fields):**
    *   `eventId` (Kiểu: `String`): Mã định danh duy nhất của sự kiện.
    *   `turn` (Kiểu: `int`): Lượt xảy ra sự kiện.
    *   `timestamp` (Kiểu: `long`): Dấu thời gian hệ thống tại thời điểm ghi nhận (ms).
    *   `teamId` (Kiểu: `String`): Mã đội chơi liên quan (có thể rỗng).
    *   `agentId` (Kiểu: `String`): Mã Agent thực hiện hành động (có thể rỗng).
    *   `eventType` (Kiểu: `String`): Loại sự kiện (chỉ nhận: `Movement`, `Collection`, `ScoreUpdate`, `TrafficUpdate`).
    *   `payload` (Kiểu: `String`): Dữ liệu chi tiết đính kèm dạng chuỗi cấu trúc chứa thông số tọa độ, năng lượng, hoặc điểm số tăng thêm.

### 5.2. Thực thể: TurnHistory (Entity)
*   **Package:** `com.naprock.hexudon.domain.model.history`
*   **Phạm vi truy cập:** Public
*   **Ý nghĩa:** Gom nhóm các sự kiện lịch sử theo từng lượt chơi để kết xuất dòng thời gian cho Visualizer.
*   **Các thuộc tính (Fields):**
    *   `turnNumber` (Kiểu: `int`): Số thứ tự lượt đấu.
    *   `events` (Kiểu: List của `GameEvent`): Danh sách sự kiện đã được lưu trữ trong lượt này.
    *   `startTime` (Kiểu: `long`): Thời điểm bắt đầu lượt chơi.
    *   `endTime` (Kiểu: `long`): Thời điểm kết thúc lượt chơi.

---

## 6. Giám sát giao tiếp mạng (Communication Logging)

### 6.1. Thực thể: ApiCommunicationLog (Entity)
*   **Package:** `com.naprock.hexudon.domain.model.logging`
*   **Phạm vi truy cập:** Public
*   **Ý nghĩa:** Nhật ký kỹ thuật lưu vết hiệu năng truyền tải mạng của các đội chơi.
*   **Các thuộc tính (Fields):**
    *   `requestId` (Kiểu: `String`): Mã định danh duy nhất của request.
    *   `teamId` (Kiểu: `String`): Tên của đội chơi thực hiện gửi yêu cầu.
    *   `endpoint` (Kiểu: `String`): Địa chỉ API endpoint được gọi.
    *   `requestTime` (Kiểu: `long`): Dấu thời gian server tiếp nhận request.
    *   `responseTime` (Kiểu: `long`): Dấu thời gian server hoàn thành trả dữ liệu.
    *   `durationMs` (Kiểu: `long`): Độ trễ xử lý (tính bằng: `responseTime` - `requestTime`).
    *   `payloadSize` (Kiểu: `long`): Kích thước dữ liệu request nhận được (Byte).
    *   `status` (Kiểu: `int`): Mã trạng thái HTTP trả về cho Client.

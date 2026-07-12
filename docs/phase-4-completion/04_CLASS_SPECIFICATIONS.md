# ĐẶC TẢ KỸ THUẬT CHI TIẾT CÁC LỚP GIAI ĐOẠN 4

Tài liệu này đặc tả chi tiết thuộc tính, phương thức, mối quan hệ và thuật toán vận hành của các lớp trong Giai đoạn 4, được nhóm theo các phân hệ nghiệp vụ cốt lõi.

---

## 1. Phân hệ Hệ thống giao thông động (Traffic Flow)

### 1.1. Lớp TrafficFlow (Domain Entity)
*   **Mô tả**: Đại diện cho lượng giao thông thực tế đã được tính toán tại một tọa độ xác định trong trận đấu. Đối tượng này đóng vai trò chứa dữ liệu trạng thái giao thông và hoàn toàn không chứa logic tính toán.
*   **Thuộc tính**:
    *   `coordinate` (Kiểu: `Coordinate`, Phạm vi: private final): Tọa độ ô bản đồ.
    *   `calculatedFlow` (Kiểu: `double`, Phạm vi: private final): Giá trị lưu lượng giao thông trung bình đã tính.
    *   `trafficState` (Kiểu: `RoadTrafficState`, Phạm vi: private final): Trạng thái kẹt xe tương ứng.
*   **Phương thức**:
    *   Cung cấp các phương thức getter để truy xuất thuộc tính. Lớp này không chứa logic tính toán hay phương thức thay đổi trạng thái (Setter).

### 1.2. Lớp RoadTrafficState (Value Object - Bất biến)
*   **Mô tả**: Lưu trạng thái giao thông chi tiết của từng ô đường nhựa, chứa thông tin mức độ giao thông (TrafficLevel) hiện thời và lưu lượng số thực (calculatedFlow).
*   **Thuộc tính**:
    *   `trafficLevel` (Kiểu: `TrafficLevel`, Phạm vi: private final): Cấp độ giao thông hiện thời (NORMAL, BUSY, CONGESTED).
    *   `flowValue` (Kiểu: `double`, Phạm vi: private final): Giá trị lưu lượng thực tế.
*   **Phương thức**:
    *   `getTrafficLevel()`: Trả về `TrafficLevel`.
    *   `getFlowValue()`: Trả về double.

### 1.3. Lớp TrafficThreshold (Value Object - Bất biến)
*   **Mô tả**: Chứa các ngưỡng số thực dùng làm căn cứ chuyển đổi phân định TrafficLevel cho hệ thống giao thông động.
*   **Thuộc tính**:
    *   `busyLimit` (Kiểu: `double`, Phạm vi: private final): Ngưỡng bắt đầu chuyển sang mức BUSY.
    *   `congestedLimit` (Kiểu: `double`, Phạm vi: private final): Ngưỡng bắt đầu chuyển sang mức CONGESTED.
*   **Phương thức**:
    *   Constructor nhận `busyLimit` và `congestedLimit`. Không có setter.
    *   `getBusyLimit()`: Trả về double.
    *   `getCongestedLimit()`: Trả về double.

### 1.4. Enum TrafficLevel
*   **Mô tả**: Enum biểu diễn các mức độ giao thông trên đường nhựa. Hoàn toàn không chứa logic nghiệp vụ.
*   **Mức độ**:
    *   `NORMAL`: Giao thông bình thường, thông suốt.
    *   `BUSY`: Giao thông đông đúc, bắt đầu tăng nhẹ chi phí.
    *   `CONGESTED`: Kẹt xe nghiêm trọng, chi phí di chuyển tăng mạnh.

### 1.5. Lớp TrafficSnapshot (Value Object - Bất biến)
*   **Mô tả**: Ảnh chụp bất biến trạng thái giao thông của toàn bộ các ô đường tại một lượt chơi (Turn) xác định, được lưu lại phục vụ lịch sử.
*   **Thuộc tính**:
    *   `turn` (Kiểu: `int`, Phạm vi: private final): Chỉ số lượt chơi.
    *   `flows` (Kiểu: `Map<Coordinate, TrafficFlow>`, Phạm vi: private final): Bản đồ lưu lượng kẹt xe tại từng tọa độ.
*   **Phương thức**:
    *   `getTurn()`: Trả về int.
    *   `getFlowAt(Coordinate coord)`: Trả về `Optional<TrafficFlow>`.

### 1.6. Lớp TrafficCalculator (Domain Service)
*   **Mô tả**: Dịch vụ miền chứa toàn bộ thuật toán cốt lõi để tính toán giao thông động. Hoàn toàn không truy cập repository và không phụ thuộc framework Spring.
*   **Phương thức**:
    *   `calculateFlow(int stayStepsTurn1, int stayStepsTurn2, int totalTeams)`:
        *   Kiểu trả về: `double`.
        *   *Quy tắc biên*: Nếu `totalTeams` bằng 0, trả về giá trị mặc định là `0.0` để tránh ngoại lệ chia cho 0 (`ArithmeticException`).
    *   `determineState(double calculatedFlow, TrafficThreshold threshold)`:
        *   Kiểu trả về: `RoadTrafficState` (phân loại dựa trên so sánh calculatedFlow với thresholds).

### 1.7. Giao diện CalculateTrafficUseCase (Inbound Port)
*   **Mô tả**: Khai báo ca sử dụng (use case) cập nhật và tính toán lưu lượng giao thông động khi kết thúc lượt chơi.
*   **Phương thức**:
    *   `updateTrafficForNextTurn(MatchState matchState, MatchConfig config)`: Trả về void.

### 1.8. Giao diện TrafficRepository (Outbound Port)
*   **Mô tả**: Khai báo các thao tác lưu trữ và đọc TrafficSnapshot. Hoàn toàn là abstraction và không phụ thuộc công nghệ lưu trữ.
*   **Phương thức**:
    *   `save(TrafficSnapshot snapshot)`: Trả về void.
    *   `findByTurn(int turn)`: Trả về `Optional<TrafficSnapshot>`.

### 1.9. Lớp TrafficCalculationService (Application Service)
*   **Mô tả**: Hiện thực hóa `CalculateTrafficUseCase`, chịu trách nhiệm điều phối use case tính toán giao thông, thu thập dữ liệu đầu vào, gọi `TrafficCalculator` và lưu kết quả thông qua `TrafficRepository`.
*   **Thuộc tính**:
    *   `trafficRepository` (Kiểu: `TrafficRepository`, Phạm vi: private final)
    *   `trafficCalculator` (Kiểu: `TrafficCalculator`, Phạm vi: private final)
*   **Phương thức**:
    *   `updateTrafficForNextTurn(MatchState matchState, MatchConfig config)`: Điều phối gom stay steps, tính lưu lượng và lưu snapshot.

### 1.10. Lớp TrafficPersistenceAdapter (Adapter)
*   **Mô tả**: Adapter triển khai Outbound Port `TrafficRepository`, thực hiện lưu trữ và truy vấn thông tin giao thông hoàn toàn trên bộ nhớ RAM (In-Memory). Không chứa bất kỳ mô tả hay cấu hình nào liên quan đến Spring Data JPA hay cơ sở dữ liệu vật lý.
*   **Thuộc tính**:
    *   `inMemoryDb` (Kiểu: `ConcurrentMap<Integer, TrafficEntity>`, Phạm vi: private final): Bản đồ RAM (Turn -> Entity).
*   **Phương thức**:
    *   `save(TrafficSnapshot snapshot)`: Ánh xạ sang Entity và lưu trên RAM.
    *   `findByTurn(int turn)`: Tìm kiếm và ánh xạ ngược lại.

### 1.11. Lớp TrafficEntity (Persistence Model)
*   **Mô tả**: Mô hình dữ liệu dùng trong adapter lưu trữ In-Memory. Hoàn toàn không chứa JPA annotations hay cấu hình bảng cơ sở dữ liệu vật lý.
*   **Thuộc tính**:
    *   `turn` (Kiểu: `int`, Phạm vi: private): Chỉ số lượt chơi.
    *   `serializedState` (Kiểu: `String`, Phạm vi: private): Chuỗi dữ liệu trạng thái giao thông serialize để tránh rò rỉ tham chiếu của Domain.
    *   `timestamp` (Kiểu: `long`, Phạm vi: private): Thời điểm ghi nhận.

---

## 2. Phân hệ Chi phí di chuyển địa hình (Terrain & Movement Cost)

### 2.1. Lớp MovementCost (Value Object - Bất biến)
*   **Mô tả**: Đại diện cho chi phí thực tế mà Agent phải trả để thực hiện di chuyển.
*   **Thuộc tính**:
    *   `fuelNeeded` (Kiểu: `int`, Phạm vi: private final): Số lượng nhiên liệu tiêu hao.
    *   `stepsNeeded` (Kiểu: `int`, Phạm vi: private final): Số bước hành động trong hàng đợi cần tiêu tốn (số lượt chờ để hoàn thành bước đi).
*   **Phương thức**:
    *   Constructor nhận đầy đủ 2 thuộc tính trên. Không setter.
    *   `getFuelNeeded()`, `getStepsNeeded()`.

### 2.2. Lớp MovementCostCalculator (Domain Service)
*   **Mô tả**: Công thức tính toán chi phí di chuyển động.
*   **Phương thức**:
    *   `calculate(TerrainType terrain, RoadTrafficState trafficState, MatchConfig config)`:
        *   Tham số: `terrain` (TerrainType), `trafficState` (RoadTrafficState), `config` (MatchConfig).
        *   Kiểu trả về: `MovementCost`.
        *   **Thuật toán từng bước**:
            1.  Lấy hệ số chi phí nhiên liệu cơ bản (`baseFuel`) và số bước cơ bản (`baseSteps`) từ cấu hình `MatchConfig` cho loại `terrain` đó.
                *   Mặc định: Plain (`baseFuel=1`, `baseSteps=1`), Mountain (`baseFuel=3`, `baseSteps=2`), Road (`baseFuel=1`, `baseSteps=1`).
            2.  Nếu `terrain` là `TerrainType.ROAD`:
                a. Tra cứu hệ số nhân giao thông từ cấu hình dựa trên trạng thái `trafficState`:
                    *   `SMOOTH`: Hệ số nhiên liệu = `1.0`, Hệ số bước = `1.0`.
                    *   `CONGESTED`: Hệ số nhiên liệu = `config.getCongestedFuelMultiplier()` (ví dụ: `1.5`), Hệ số bước = `config.getCongestedStepsMultiplier()` (ví dụ: `2`).
                    *   `TRAFFIC_JAM`: Hệ số nhiên liệu = `config.getJamFuelMultiplier()` (ví dụ: `3.0`), Hệ số bước = `config.getJamStepsMultiplier()` (ví dụ: `4`).
                b. Tính toán chi phí cuối cùng:
                    *   `finalFuel = baseFuel * fuelMultiplier` (làm tròn lên số nguyên gần nhất).
                    *   `finalSteps = baseSteps * stepsMultiplier` (làm tròn lên số nguyên gần nhất).
            3.  Nếu không phải là `ROAD`, chi phí bằng chi phí cơ bản của địa hình đó.
            4.  Trả về đối tượng `MovementCost(finalFuel, finalSteps)`.

*   **Quy tắc biên chốt chi phí**:
    *   Chi phí di chuyển được tính toán và khóa lại ngay tại thời điểm Agent bắt đầu lệnh di chuyển ở bước đầu tiên của lượt. Sự thay đổi trạng thái kẹt xe xảy ra giữa Turn không làm thay đổi chi phí của bước đi đang thực hiện.

---

## 3. Phân hệ Hệ thống tính điểm (Scoring System)

### 3.1. Lớp TeamScore (Entity)
*   **Mô tả**: Lưu giữ thông tin điểm và chỉ số hiệu năng của một đội chơi.
*   **Thuộc tính**:
    *   `teamId` (Kiểu: `String`, Phạm vi: private): ID hoặc tên của đội.
    *   `collectedUdonTypes` (Kiểu: `Set<UdonType>`, Phạm vi: private): Danh sách các loại Udon độc nhất thu thập được trong trận đấu.
    *   `dailyUdonTypesHistory` (Kiểu: `Map<Integer, Set<UdonType>>`, Phạm vi: private): Danh sách các loại Udon độc nhất thu thập được phân tách theo từng lượt chơi (`Turn` -> `Set<UdonType>`).
    *   `totalServings` (Kiểu: `int`, Phạm vi: private): Tổng số lượt phục vụ thành công.
    *   `totalResponseTimeMs` (Kiểu: `long`, Phạm vi: private): Tổng thời gian phản hồi API của đội trong suốt trận đấu (tính bằng mili-giây).
    *   `requestCount` (Kiểu: `int`, Phạm vi: private): Số lượng yêu cầu hợp lệ đã thực hiện để tính thời gian trung bình.
*   **Phương thức**:
    *   `getTeamId()`: Trả về String.
    *   `getUniqueUdonTypesCount()`: Trả về `int` (kích thước của `collectedUdonTypes`).
    *   `getAccumulatedDailyUdonTypes()`: Trả về `int`.
        *   *Công thức*: Tổng số loại Udon thu thập của mỗi ngày cộng lại. Ví dụ: Ngày 1 thu được loại A và B (2 loại), ngày 2 thu được loại B và C (2 loại) -> Tích lũy = 2 + 2 = 4 loại.
    *   `getTotalServings()`: Trả về int.
    *   `getTotalResponseTimeMs()`: Trả về long.
    *   `addUdonCollection(int turn, UdonType udon)`: Cập nhật Udon loại mới vào `collectedUdonTypes` và `dailyUdonTypesHistory` cho lượt chơi tương ứng. Trả về void.
    *   `incrementServings()`: Tăng `totalServings` thêm 1. Trả về void.
    *   `addResponseTime(long durationMs)`: Cộng dồn `durationMs` vào `totalResponseTimeMs` và tăng `requestCount` thêm 1. Trả về void.

---

## 4. Phân hệ Hệ thống xếp hạng đấu trường (Ranking System)

### 4.1. Lớp RankingCriteria (Value Object - Bất biến)
*   **Mô tả**: Chứa cấu hình các trọng số và cơ chế so sánh thứ hạng.
*   **Thuộc tính**:
    *   `maxTurns` (Kiểu: `int`): Số lượt tối đa phục vụ tính tích lũy ngày.

### 4.2. Lớp RankingService (Domain Service)
*   **Mô tả**: Chứa logic so sánh thứ hạng tuyệt đối (Anti-tie-break) giữa hai thực thể `TeamScore`.
*   **Phương thức**:
    *   `compareTeams(TeamScore t1, TeamScore t2)`:
        *   Tham số: `t1` (TeamScore), `t2` (TeamScore).
        *   Kiểu trả về: `int` (âm nếu `t1` xếp trên `t2`, dương nếu `t1` xếp dưới `t2`, 0 nếu bằng nhau tuyệt đối trước khi tung xúc xắc).
        *   **Thuật toán so sánh tuần tự (Anti-tie-break)**:
            1.  **Cấp độ 1 - Độ đa dạng Udon**: So sánh số lượng Udon độc nhất `t1.getUniqueUdonTypesCount()` và `t2.getUniqueUdonTypesCount()` (Giảm dần). Nếu khác nhau, trả về kết quả so sánh.
            2.  **Cấp độ 2 - Tích lũy theo ngày**: So sánh `t1.getAccumulatedDailyUdonTypes()` và `t2.getAccumulatedDailyUdonTypes()` (Giảm dần). Nếu khác nhau, trả về kết quả.
            3.  **Cấp độ 3 - Tổng số servings**: So sánh `t1.getTotalServings()` và `t2.getTotalServings()` (Giảm dần). Nếu khác nhau, trả về kết quả.
            4.  **Cấp độ 4 - Thời gian phản hồi**: So sánh tổng thời gian phản hồi `t1.getTotalResponseTimeMs()` và `t2.getTotalResponseTimeMs()` (Tăng dần - ít thời gian hơn xếp trên). Nếu khác nhau, trả về kết quả.
            5.  **Cấp độ 5 - Xử lý hòa (Tie-breaker)**: Gọi phương thức hỗ trợ `resolveTie(t1, t2)` để quyết định.
    *   `resolveTie(TeamScore t1, TeamScore t2)`:
        *   Tham số: `t1` (TeamScore), `t2` (TeamScore).
        *   Kiểu trả về: `int`.
        *   **Thuật toán ngẫu nhiên (Random Dice)**:
            1.  Sử dụng một hàm băm giả ngẫu nhiên dựa trên sự kết hợp giữa mã định danh trận đấu (`matchId`) và tên của hai đội để đảm bảo tính nhất quán của kết quả khi khôi phục (nếu chạy lại vẫn ra cùng một kết quả xúc xắc).
            2.  Băm chuỗi `matchId + t1.getTeamId() + t2.getTeamId()` để sinh số ngẫu nhiên.
            3.  Nếu số ngẫu nhiên là số lẻ, chọn `t1` thắng (trả về -1), ngược lại chọn `t2` thắng (trả về 1).

---

## 5. Phân hệ Lịch sử trận đấu & Log sự kiện (Game Event History)

### 5.1. Lớp GameEvent (Entity)
*   **Mô tả**: Mô tả chi tiết một hành động hoặc biến cố vật lý xảy ra trong trận đấu.
*   **Thuộc tính**:
    *   `eventId` (Kiểu: `String`, Phạm vi: private): Khóa chính duy nhất.
    *   `turn` (Kiểu: `int`, Phạm vi: private): Lượt xảy ra sự kiện.
    *   `timestamp` (Kiểu: `long`, Phạm vi: private): Thời điểm xảy ra sự kiện (mili-giây).
    *   `teamId` (Kiểu: `String`, Phạm vi: private): Đội liên quan (có thể rỗng nếu là sự kiện hệ thống).
    *   `agentId` (Kiểu: `String`, Phạm vi: private): Agent liên quan (có thể rỗng).
    *   `eventType` (Kiểu: `String`, Phạm vi: private): Loại sự kiện (`MOVEMENT`, `COLLECTION`, `SCORE_UPDATE`, `TRAFFIC_UPDATE`).
    *   `payload` (Kiểu: `Map<String, Object>`, Phạm vi: private): Thông tin động dưới dạng Key-Value (ví dụ: tọa độ đi/đến, loại Udon, điểm cộng thêm).
*   **Phương thức**:
    *   `getEventId()`, `getTurn()`, `getTimestamp()`, `getEventType()`, `getPayload()`.
    *   Constructor nhận đầy đủ tham số để tạo mới sự kiện bất biến.

---

## 6. Phân hệ Giám sát giao tiếp mạng (Api Communication Log)

### 6.1. Lớp ApiCommunicationLog (Entity)
*   **Mô tả**: Nhật ký kỹ thuật ghi lại hiệu năng của các truy cập REST API.
*   **Thuộc tính**:
    *   `requestId` (Kiểu: `String`, Phạm vi: private): ID ngẫu nhiên định danh yêu cầu.
    *   `teamId` (Kiểu: `String`, Phạm vi: private): Tên đội thực hiện request.
    *   `endpoint` (Kiểu: `String`, Phạm vi: private): Đường dẫn API (URI).
    *   `requestTime` (Kiểu: `long`, Phạm vi: private): Thời gian tiếp nhận (mili-giây).
    *   `responseTime` (Kiểu: `long`, Phạm vi: private): Thời gian phản hồi (mili-giây).
    *   `durationMs` (Kiểu: `long`, Phạm vi: private): Độ trễ xử lý thực tế (`responseTime - requestTime`).
    *   `payloadSize` (Kiểu: `long`, Phạm vi: private): Kích thước gói tin request (bytes).
    *   `status` (Kiểu: `int`, Phạm vi: private): Mã phản hồi HTTP (ví dụ: `200`, `400`, `500`).
*   **Phương thức**:
    *   Các phương thức getter chuẩn cho toàn bộ thuộc tính. Không có phương thức thay đổi trạng thái (Setter).

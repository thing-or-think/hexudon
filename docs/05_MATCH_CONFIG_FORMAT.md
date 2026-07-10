# 05. THIẾT KẾ CẤU HÌNH TRẬN ĐẤU (MATCH CONFIG FORMAT)

## Mục lục
1. [Ý nghĩa các trường dữ liệu trong MatchConfig](#1-ý-nghĩa-các-trường-dữ-liệu-trong-matchconfig)
2. [Cấu trúc lưu trữ và nạp cấu hình (Resource Loader)](#2-cấu-trúc-lưu-trữ-và-nạp-cấu-hình-resource-loader)
3. [Quy tắc kiểm tra tính hợp lệ (Validation Rules)](#3-quy-tắc-kiểm-tra-tính-hợp-lệ-validation-rules)
4. [Thiết kế định dạng JSON cho cấu hình](#4-thiết-kế-định-dạng-json-cho-cấu-hình)
5. [Tích hợp với Domain (Domain Integration)](#5-tích-hợp-với-domain-domain-integration)

---

## 1. Ý nghĩa các trường dữ liệu trong MatchConfig

Cấu hình trận đấu (`MatchConfig`) chứa toàn bộ luật chơi cơ học và thông số của một trận đấu. Dưới đây là bảng giải thích chi tiết ý nghĩa từng trường dữ liệu:

| Tên trường (Key) | Đơn vị / Kiểu | Ý nghĩa nghiệp vụ | Giá trị mặc định |
| :--- | :--- | :--- | :--- |
| `mapWidth` | Số nguyên (ô) | Chiều rộng của lưới bản đồ lục giác. | 20 |
| `mapHeight` | Số nguyên (ô) | Chiều cao của lưới bản đồ lục giác. | 15 |
| `initialFuel` | Số nguyên (xăng)| Lượng xăng mặc định được sạc cho Agent khi bắt đầu ngày mới. | 100 |
| `maxFuel` | Số nguyên (xăng)| Dung tích bình xăng tối đa mà Patrol Agent có thể chứa sau tiếp xăng. | 100 |
| `maxTurns` | Số nguyên (lượt) | Tổng số ngày (Turn) tối đa của trận đấu trước khi kết thúc. | 1 |
| `maxStepsPerTurn` | Số nguyên (bước) | Số bước hành động tối đa mà mỗi Agent có thể đi trong một Turn. | 5 |
| `maxTeams` | Số nguyên (đội) | Số lượng đội thi đấu tối đa được phép đăng ký. | 2 |
| `agentsPerTeam` | Số nguyên (agent)| Số lượng Agent tối đa mỗi đội sở hữu. | 3 |
| `patrolAgents` | Số nguyên (agent)| Số lượng Agent tuần tra có nhiệm vụ thu thập Udon. | 2 |
| `refuelAgents` | Số nguyên (agent)| Số lượng Agent tiếp nhiên liệu. | 1 |
| `turnTimeLimitMs` | Mili giây | Thời gian tối đa của một lượt đấu trước khi tự động chuyển ngày. | 1000 |
| `maxRequestsPerSecond` | Số nguyên | Giới hạn số lượng request API tối đa từ một client trong 1 giây. | 10 |
| `maxSpamViolations` | Số nguyên (lần) | Số lần tối đa vi phạm rate limit trước khi bị truất quyền thi đấu. | 3 |
| `initialSpotUdonStock` | Số nguyên (bánh) | Số lượng bánh Udon mặc định ban đầu tại mỗi Spot. | 5 |
| `roadStepCost` | Số nguyên (bước) | Chi phí bước đi tiêu hao khi Agent đi vào ô địa hình ROAD. | 1 |
| `roadFuelCost` | Số nguyên (xăng)| Chi phí nhiên liệu tiêu hao khi Agent đi vào ô địa hình ROAD. | 2 |
| `plainStepCost` | Số nguyên (bước) | Chi phí bước đi tiêu hao khi Agent đi vào ô địa hình PLAIN. | 2 |
| `plainFuelCost` | Số nguyên (xăng)| Chi phí nhiên liệu tiêu hao khi Agent đi vào ô địa hình PLAIN. | 1 |
| `mountainStepCost` | Số nguyên (bước) | Chi phí bước đi tiêu hao khi Agent đi vào ô địa hình MOUNTAIN. | 3 |
| `mountainFuelCost` | Số nguyên (xăng)| Chi phí nhiên liệu tiêu hao khi Agent đi vào ô địa hình MOUNTAIN. | 2 |

---

## 2. Cấu trúc lưu trữ và nạp cấu hình (Resource Loader)

### Cơ chế nạp hiện tại (Text-based file)
Cấu hình trận đấu được viết dưới dạng tệp văn bản phẳng `match_config.txt` nằm trong tài nguyên hệ thống. Từng dòng được khai báo theo cặp khóa giá trị phân tách bằng dấu bằng (`tên_khóa=giá_trị`). Hệ thống bỏ qua các dòng trống hoặc dòng bắt đầu bằng dấu thăng (`#`) làm chú thích.

### Thiết kế theo Hexagonal
Tầng Application định nghĩa Interface `MatchConfigLoaderPort` cung cấp hàm `loadConfig`. 
Lớp Adapter hạ tầng `FileMatchConfigLoader` ở tầng ngoài triển khai Interface này. Nó sẽ gọi `FileUtils` để truy xuất file, duyệt qua từng dòng để phân tích cú pháp và nạp dữ liệu vào đối tượng Value Object `MatchConfig`. 
Nếu gặp bất kỳ dòng nào sai cú pháp hoặc giá trị không phải số nguyên, adapter sẽ bọc lỗi kỹ thuật thành ngoại lệ nghiệp vụ `ConfigLoadException` và ném lên tầng trên.

---

## 3. Quy tắc kiểm tra tính hợp lệ (Validation Rules)

Để tránh trận đấu bị lỗi mô phỏng hoặc treo hệ thống, các giá trị cấu hình khi nạp vào bắt buộc phải thỏa mãn các ràng buộc logic sau:

- **Kích thước bản đồ:** `mapWidth` và `mapHeight` phải lớn hơn hoặc bằng 5 để đảm bảo không gian hoạt động tối thiểu cho 2 đội (mỗi đội 3 agent).
- **Ràng buộc số lượng Agent:** 
  - Tổng số `patrolAgents` và `refuelAgents` phải bằng chính xác `agentsPerTeam`.
  - Cả `patrolAgents` và `refuelAgents` phải lớn hơn hoặc bằng 1 (Đảm bảo mỗi đội đều có ít nhất 1 Agent đi lấy udon và 1 Agent đi tiếp xăng).
- **Tài nguyên nhiên liệu:** 
  - `initialFuel` phải nhỏ hơn hoặc bằng `maxFuel`.
  - Mọi giá trị nhiên liệu (`maxFuel`, `initialFuel`) và chi phí địa hình (`plainFuelCost`, `roadFuelCost`, `mountainFuelCost`) phải lớn hơn 0.
- **Quota lượt đi:**
  - `maxStepsPerTurn` phải lớn hơn hoặc bằng chi phí bước chân lớn nhất của địa hình (`mountainStepCost`) để đảm bảo Agent không bị kẹt không thể di chuyển ở bất kỳ ô nào.
- **Giới hạn thời gian lượt:** `turnTimeLimitMs` phải tối thiểu là 500ms để đảm bảo client kịp nhận phản hồi và tính toán hành động tiếp theo.
- **Quy định hình phạt:** `maxSpamViolations` phải lớn hơn hoặc bằng 1.

---

## 4. Thiết kế định dạng JSON cho cấu hình

Nhằm mục đích chuẩn bị cho việc cấu hình trận đấu linh hoạt qua Dashboard hoặc qua các API admin trong tương lai, mô hình `MatchConfig` được thiết kế tương thích hoàn toàn với cấu trúc JSON phân cấp.

### Cấu trúc mô tả các nhóm thuộc tính JSON:
1. **Nhóm Bản đồ (map):**
   - Chiều rộng (`width`)
   - Chiều cao (`height`)
2. **Nhóm Vòng đấu (gameplay):**
   - Tổng số ngày tối đa (`maxTurns`)
   - Số bước đi tối đa của lượt (`maxStepsPerTurn`)
   - Thời gian giới hạn của lượt (`turnTimeLimitMs`)
   - Số lượng bánh Udon tại mỗi điểm (`initialSpotUdonStock`)
3. **Nhóm Đội & Đội hình (team):**
   - Số đội tối đa (`maxTeams`)
   - Số Agent mỗi đội (`agentsPerTeam`)
   - Số Agent tuần tra (`patrolAgents`)
   - Số Agent nạp xăng (`refuelAgents`)
4. **Nhóm Nhiên liệu (fuel):**
   - Lượng xăng ban đầu (`initialFuel`)
   - Dung tích bình tối đa (`maxFuel`)
5. **Nhóm Chi phí Địa hình (terrainCosts):**
   - Cấu hình cho Plain: Chi phí xăng (`plainFuelCost`), chi phí bước (`plainStepCost`).
   - Cấu hình cho Road: Chi phí xăng (`roadFuelCost`), chi phí bước (`roadStepCost`).
   - Cấu hình cho Mountain: Chi phí xăng (`mountainFuelCost`), chi phí bước (`mountainStepCost`).
6. **Nhóm Bảo mật & Tần suất (security):**
   - Giới hạn request mỗi giây (`maxRequestsPerSecond`)
   - Số lần vi phạm rate limit cho phép (`maxSpamViolations`)

---

## 5. Tích hợp với Domain (Domain Integration)

Đối tượng `MatchConfig` sau khi được nạp từ Adapter sẽ được coi là một **Domain Value Object**. 
- Nó có tính chất bất biến (Immutable), không có ID định danh và chỉ mang thông tin luật chơi.
- Nó được truyền vào các Domain Service (`MovementSimulator`, `FuelManager`, `ActionValidatorEngine`) dưới dạng tham số đầu vào trong các hàm tính toán.
- Sự bất biến của `MatchConfig` đảm bảo luật chơi không bị sửa đổi bất hợp pháp giữa chừng khi trận đấu đang diễn ra. Mọi thay đổi cấu hình chỉ có hiệu lực khi bắt đầu một trận đấu mới hoàn toàn.

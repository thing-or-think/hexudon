# CẤU TRÚC CẤU HÌNH TRẬN ĐẤU (MATCH CONFIGURATION FORMAT) - GIAI ĐOẠN 4

Tài liệu này đặc tả toàn bộ các tham số cấu hình hệ thống bằng bảng dữ liệu chi tiết, định nghĩa các ngưỡng hoạt động, hệ số phạt, trọng số điểm và giới hạn thời gian cho Giai đoạn 4.

---

## 1. Tham số Cấu hình Giao thông động (Traffic Calculation Parameters)

| Tên tham số | Kiểu dữ liệu | Giá trị mặc định | Ý nghĩa chức năng | Ràng buộc kiểm tra |
| :--- | :--- | :--- | :--- | :--- |
| `trafficWeightT1` | Double | 1.0 | Trọng số áp dụng cho số bước dừng chân ở lượt đấu liền trước (Turn T-1). | Lớn hơn 0 |
| `trafficWeightT2` | Double | 1.0 | Trọng số áp dụng cho số bước dừng chân ở 2 lượt đấu trước (Turn T-2). | Lớn hơn 0 |
| `congestionThreshold` | Double | 1.5 | Ngưỡng lưu lượng trung bình để ô đường bộ rơi vào trạng thái ùn ứ (`CONGESTED`). | Lớn hơn 0, nhỏ hơn `trafficJamThreshold` |
| `trafficJamThreshold` | Double | 3.0 | Ngưỡng lưu lượng trung bình để ô đường bộ rơi vào trạng thái kẹt xe (`TRAFFIC_JAM`). | Lớn hơn `congestionThreshold` |

---

## 2. Hệ số chi phí di chuyển địa hình và giao thông (Movement Cost Multipliers)

Các hệ số dưới đây được nhân trực tiếp với chi phí nhiên liệu cơ sở (`roadFuelCost`, `plainFuelCost`, `mountainFuelCost`) và bước đi cơ sở (`roadStepCost`, `plainStepCost`, `mountainStepCost`) được định nghĩa trong cấu hình.

| Tên tham số | Kiểu dữ liệu | Giá trị mặc định | Địa hình áp dụng | Ý nghĩa chức năng |
| :--- | :--- | :--- | :--- | :--- |
| `roadSmoothFuelMultiplier` | Double | 1.0 | ROAD (Thông thoáng) | Hệ số nhân chi phí xăng của đường khi thông thoáng. |
| `roadSmoothStepMultiplier` | Double | 1.0 | ROAD (Thông thoáng) | Hệ số nhân số bước tiêu hao của đường khi thông thoáng. |
| `roadCongestedFuelMultiplier`| Double | 2.0 | ROAD (Ùn ứ) | Hệ số nhân chi phí xăng của đường khi bị ùn ứ. |
| `roadCongestedStepMultiplier`| Double | 2.0 | ROAD (Ùn ứ) | Hệ số nhân số bước tiêu hao của đường khi bị ùn ứ. |
| `roadJamFuelMultiplier` | Double | 4.0 | ROAD (Kẹt xe) | Hệ số nhân chi phí xăng của đường khi bị kẹt xe. |
| `roadJamStepMultiplier` | Double | 3.0 | ROAD (Kẹt xe) | Hệ số nhân số bước tiêu hao của đường khi bị kẹt xe. |
| `plainFuelMultiplier` | Double | 1.0 | PLAIN | Hệ số nhân chi phí xăng trên địa hình đồng bằng. |
| `plainStepMultiplier` | Double | 1.0 | PLAIN | Hệ số nhân bước đi tiêu hao trên địa hình đồng bằng. |
| `mountainFuelMultiplier` | Double | 1.0 | MOUNTAIN | Hệ số nhân chi phí xăng trên địa hình núi. |
| `mountainStepMultiplier` | Double | 1.0 | MOUNTAIN | Hệ số nhân bước đi tiêu hao trên địa hình núi. |

---

## 3. Tham số cấu hình Trọng số tính điểm (Scoring Weights)

| Tên tham số | Kiểu dữ liệu | Giá trị mặc định | Ý nghĩa chức năng |
| :--- | :--- | :--- | :--- |
| `uniqueUdonScoreWeight` | Integer | 100 | Điểm thưởng cho mỗi chủng loại mì Udon độc nhất thu thập được. |
| `dailyUdonVolumeWeight` | Integer | 10 | Điểm cộng thêm trên từng đơn vị Udon thu hoạch hàng ngày. |
| `servingScoreWeight` | Integer | 50 | Điểm thưởng cho mỗi lượt phục vụ mì Udon thành công. |

---

## 4. Tham số giới hạn thời gian và tần suất API (Timeouts & Rate Limits)

| Tên tham số | Kiểu dữ liệu | Giá trị mặc định | Ý nghĩa chức năng | Ràng buộc kiểm tra |
| :--- | :--- | :--- | :--- | :--- |
| `turnTimeLimitMs` | Integer | 1000 | Thời gian tối đa (mili-giây) của một lượt đấu. Server tự động đóng Turn khi hết thời gian này. | Lớn hơn 100 |
| `apiTimeoutMs` | Integer | 200 | Thời gian tối đa cho phép một HTTP API request xử lý (mili-giây). Quá thời gian này sẽ báo lỗi Timeout. | Lớn hơn 10 |
| `maxRequestsPerSecond` | Integer | 10 | Số lượng request API tối đa một đội chơi được phép gửi lên trong 1 giây. | Lớn hơn 0 |
| `maxSpamViolations` | Integer | 3 | Số lần vi phạm gửi request vượt hạn mức tối đa cho phép trước khi đội chơi bị truất quyền thi đấu (`disqualified`). | Lớn hơn 0 |

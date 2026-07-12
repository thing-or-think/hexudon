# CẤU TRÚC CẤU HÌNH TRẬN ĐẤU (MATCH CONFIGURATION FORMAT)

Tài liệu này đặc tả toàn bộ các tham số cấu hình hệ thống phục vụ cho các tính năng mới trong Giai đoạn 4. Toàn bộ cấu hình được lưu trữ ở dạng bảng dữ liệu mô tả thuộc tính.

---

## 1. Bảng cấu hình các tham số hệ thống Giai đoạn 4

| Nhóm tham số | Mã tham số | Kiểu dữ liệu | Giá trị mặc định | Ý nghĩa & Quy tắc nghiệp vụ |
| :--- | :--- | :--- | :--- | :--- |
| **Giao thông** | `traffic.threshold.congested` | Double | `2.0` | Ngưỡng tính toán (calculated flow) để chuyển đường nhựa sang trạng thái `CONGESTED`. |
| **Giao thông** | `traffic.threshold.jam` | Double | `5.0` | Ngưỡng tính toán để chuyển đường nhựa sang trạng thái `TRAFFIC_JAM`. |
| **Chi phí cơ bản**| `terrain.plain.base.fuel` | Integer | `1` | Nhiên liệu tiêu hao cơ bản khi Agent đi qua ô Đất bằng (`PLAIN`). |
| **Chi phí cơ bản**| `terrain.plain.base.steps` | Integer | `1` | Số bước hành động bị tiêu tốn khi Agent đi qua ô Đất bằng (`PLAIN`). |
| **Chi phí cơ bản**| `terrain.mountain.base.fuel` | Integer | `3` | Nhiên liệu tiêu hao cơ bản khi Agent đi qua ô Núi (`MOUNTAIN`). |
| **Chi phí cơ bản**| `terrain.mountain.base.steps`| Integer | `2` | Số bước hành động bị tiêu tốn khi Agent đi qua ô Núi (`MOUNTAIN`). |
| **Chi phí cơ bản**| `terrain.road.base.fuel` | Integer | `1` | Nhiên liệu tiêu hao cơ bản khi Agent đi qua ô Đường nhựa (`ROAD`). |
| **Chi phí cơ bản**| `terrain.road.base.steps` | Integer | `1` | Số bước hành động bị tiêu tốn khi Agent đi qua ô Đường nhựa (`ROAD`). |
| **Hệ số giao thông**| `movement.multiplier.congested.fuel`| Double | `1.5` | Hệ số nhân nhiên liệu áp dụng khi ô đường có trạng thái `CONGESTED`. |
| **Hệ số giao thông**| `movement.multiplier.congested.steps`| Integer | `2` | Số bước tiêu tốn thực tế áp dụng khi ô đường có trạng thái `CONGESTED`. |
| **Hệ số giao thông**| `movement.multiplier.jam.fuel` | Double | `3.0` | Hệ số nhân nhiên liệu áp dụng khi ô đường có trạng thái `TRAFFIC_JAM`. |
| **Hệ số giao thông**| `movement.multiplier.jam.steps` | Integer | `4` | Số bước tiêu tốn thực tế áp dụng khi ô đường có trạng thái `TRAFFIC_JAM`. |
| **Tính điểm** | `score.points.unique.udon` | Integer | `1000` | Số điểm cộng thêm cho mỗi loại Udon độc nhất (`Unique Udon Type`) trong cả trận. |
| **Tính điểm** | `score.points.accumulated.daily`| Integer | `500` | Số điểm cộng thêm cho mỗi loại Udon tích lũy theo từng ngày. |
| **Tính điểm** | `score.points.serving` | Integer | `200` | Số điểm cộng thêm cho mỗi lượt phục vụ Udon thành công. |
| **Đồng thời & Trễ**| `concurrency.turn.timeout.ms` | Long | `2000` | Thời gian tối đa của một lượt chơi (Turn) trước khi Server tự động đóng Turn (mili-giây). |
| **Đồng thời & Trễ**| `concurrency.agent.action.timeout.ms`| Long | `1000` | Thời gian chờ tối đa cho một Request hành động từ Agent của đội chơi (mili-giây). |
| **Đồng thời & Trễ**| `concurrency.max.queued.requests`| Integer | `500` | Dung lượng hàng đợi chứa Request hành động của các đội chơi. |

---

## 2. Quy tắc áp dụng Cấu hình trong các Module nghiệp vụ

1.  **Tính Giao thông**: Ngưỡng kẹt xe `congested` và `jam` là số thực, cho phép cấu hình linh hoạt. Công thức kiểm tra lưu lượng sẽ so sánh số thực thu được sau phép chia trung bình với hai ngưỡng này để xếp hạng giao thông.
2.  **Tính Chi phí di chuyển**: 
    *   Đối với địa hình Đất bằng (`PLAIN`) và Núi (`MOUNTAIN`), chi phí di chuyển luôn cố định và bằng chi phí cơ bản đã cấu hình.
    *   Đối với Đường nhựa (`ROAD`), chi phí được tính bằng công thức:
        *   `Nhiên liệu tiêu hao = terrain.road.base.fuel * Hệ số nhân nhiên liệu tương ứng` (làm tròn lên số nguyên kế tiếp).
        *   `Số bước hành động tiêu hao = Hệ số bước đi tương ứng` (thay thế hoàn toàn số bước đi cơ bản).
3.  **Tính Điểm**:
    *   Điểm tổng của mỗi đội tại bất kỳ thời điểm nào được tính theo công thức: 
        $$\text{Tổng Điểm} = (\text{Số Udon độc nhất} \times \text{score.points.unique.udon}) + (\text{Tổng Udon tích lũy ngày} \times \text{score.points.accumulated.daily}) + (\text{Tổng số servings} \times \text{score.points.serving})$$
4.  **Kiểm soát thời gian phản hồi**: Khi quá thời gian `concurrency.agent.action.timeout.ms` mà đội không phản hồi hành động hợp lệ, hệ thống sẽ tự động coi như đội đó chọn hành động `WAIT` cho Agent trong bước đó và ghi nhận thời gian phản hồi bằng đúng ngưỡng timeout.

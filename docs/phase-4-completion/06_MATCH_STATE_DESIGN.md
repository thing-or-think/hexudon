# QUẢN LÝ TRẠNG THÁI VÀ PHỤC HỒI TRẬN ĐẤU (MATCH STATE DESIGN & RECOVERY) - GIAI ĐOẠN 4

Tài liệu này đặc tả thiết kế cơ chế lưu trữ, quản lý trạng thái trận đấu, chiến lược chụp ảnh snapshot và quy trình khôi phục tự động khi xảy ra lỗi hệ thống hoặc yêu cầu tái đấu.

---

## 1. Thành phần Cốt lõi của Hệ thống Phục hồi

### 1.1. Thực thể: MatchSnapshot (Entity)
*   **Package:** `com.naprock.hexudon.domain.model.recovery`
*   **Ý nghĩa:** Chứa bản sao lưu toàn bộ thông tin cần thiết của trận đấu tại một lượt cụ thể.
*   **Các thuộc tính chi tiết:**
    *   `snapshotId` (Kiểu: `String`): Mã định danh duy nhất của bản chụp.
    *   `matchId` (Kiểu: `String`): Mã trận đấu tương ứng.
    *   `turnNumber` (Kiểu: `int`): Lượt đấu được sao lưu.
    *   `timestamp` (Kiểu: `long`): Thời điểm chụp trạng thái (ms).
    *   `matchStatus` (Kiểu: `MatchStatus`): Trạng thái vòng đấu lúc chụp (`PLAYING`, `FINISHED`).
    *   `teamsData` (Kiểu: Danh sách đối tượng): Chứa trạng thái các đội (Tên đội, trạng thái xăng, bước đi còn lại, danh sách hành động hiện tại của từng Agent).
    *   `spotsData` (Kiểu: Danh sách đối tượng): Chứa tọa độ các Spot và số lượng Udon tồn kho thực tế của từng đội tại thời điểm chụp.
    *   `trafficData` (Kiểu: Danh sách đối tượng): Danh sách các ô đường kèm theo lưu lượng `flowValue` và trạng thái `RoadTrafficState` hiện hành.
    *   `scoresData` (Kiểu: Danh sách đối tượng): Điểm số chi tiết hiện tại của tất cả các đội.

### 1.2. Đối tượng giá trị: RecoveryPoint (Value Object)
*   **Package:** `com.naprock.hexudon.domain.model.recovery`
*   **Ý nghĩa:** Điểm mốc phục hồi an toàn đã được hệ thống kiểm tra và chốt lưu trữ thành công.
*   **Các thuộc tính chi tiết:**
    *   `turnNumber` (Kiểu: `int`): Số lượt đấu tối đa đã hoàn thành an toàn.
    *   `snapshotId` (Kiểu: `String`): Mã định danh bản chụp trạng thái tương ứng.
    *   `validated` (Kiểu: `boolean`): Trạng thái xác nhận điểm khôi phục hợp lệ (chỉ đặt là true khi snapshot đã ghi xuống DB thành công).

---

## 2. Chiến lược Chụp trạng thái (Snapshotting Strategy)

*   **Thời điểm kích hoạt:** Chụp snapshot được thực hiện tự động ở **cuối mỗi lượt đấu (End of Turn)**. Quy trình này diễn ra sau khi kết thúc chu kỳ xử lý hành động (`simulateTurn()`) và tính toán giao thông mới, nhưng trước khi hàm `nextDay()` tăng số thứ tự lượt đấu lên `currentTurn + 1`.
*   **Dữ liệu thu thập:**
    *   Trạng thái bản đồ: Tọa độ, loại địa hình và trạng thái giao thông động của tất cả các ô.
    *   Trạng thái Agent: Tọa độ hiện tại, lượng nhiên liệu còn lại, số bước đi còn lại.
    *   Trạng thái Đội chơi: Tên đội, tình trạng truất quyền thi đấu, điểm số.
    *   Trạng thái Điểm Udon: Số lượng Udon còn lại tại các ô Spot cho mỗi đội.

---

## 3. Quy trình Khôi phục tự động khi gặp sự cố (Rollback Flow)

Quy trình xử lý khôi phục trạng thái trận đấu khi máy chủ gặp sự cố sập nguồn đột ngột hoặc lỗi phần cứng được thực hiện theo thuật toán sau:

1.  **Bước 1 - Quét điểm khôi phục:** Khi ứng dụng khởi động lại, dịch vụ khôi phục (`RecoveryApplicationService`) thực hiện gọi Outbound Port `MatchSnapshotRepositoryPort` để tìm kiếm thực thể `RecoveryPoint` mới nhất có thuộc tính `validated` bằng `true`.
2.  **Bước 2 - Kiểm tra tính sẵn sàng:**
    *   Nếu không tìm thấy bất kỳ điểm khôi phục hợp lệ nào: Hệ thống khởi tạo một trận đấu mới hoàn toàn ở Turn 1 với trạng thái ban đầu được định nghĩa trong tệp cấu hình hệ thống.
    *   Nếu tìm thấy `RecoveryPoint`: Lấy mã `snapshotId` liên kết.
3.  **Bước 3 - Tải ảnh chụp trạng thái:** Thực hiện truy vấn thực thể `MatchSnapshot` tương ứng với `snapshotId`.
4.  **Bước 4 - Phục hồi trạng thái bộ nhớ:**
    *   Nạp lại toàn bộ ô địa hình và gán lại trạng thái giao thông từ `trafficData` vào bộ nhớ đệm bản đồ của `MatchState`.
    *   Khởi tạo lại danh sách đội chơi, gán lại chính xác tọa độ, nhiên liệu và bước đi của từng Agent từ `teamsData`.
    *   Cập nhật điểm số thực tế của các đội chơi từ `scoresData`.
    *   Thiết lập lại số Udon tồn kho tại các Spot từ `spotsData`.
5.  **Bước 5 - Đặt lại đồng hồ lượt đấu:** Gán giá trị lượt chơi hiện tại của hệ thống: `currentTurn = RecoveryPoint.turnNumber`. Thiết lập trạng thái trận đấu thành `PLAYING`. Gán dấu thời gian bắt đầu lượt mới: `turnStartTime = thời gian hệ thống hiện tại`.
6.  **Bước 6 - Kích hoạt hệ thống:** Mở cổng tiếp nhận yêu cầu gửi hành động cho lượt kế tiếp (`currentTurn + 1`) và gửi thông báo sẵn sàng tới các đội chơi.

---

## 4. Quy trình Tái đấu từ lượt chỉ định (Rematch Flow)

Quy trình cho phép ban tổ chức kích hoạt đấu lại trận đấu từ lượt chơi thứ `X` bất kỳ được thực hiện theo thuật toán sau:

1.  **Bước 1 - Nhận yêu cầu:** Quản trị viên gửi yêu cầu tái đấu kèm tham số lượt bắt đầu lại là `X` thông qua REST API chuyên biệt.
2.  **Bước 2 - Xác thực tính hợp lệ:** Hệ thống kiểm tra điều kiện lượt chơi: `X` phải lớn hơn 0 và nhỏ hơn hoặc bằng lượt đấu hiện tại (`currentTurn`).
3.  **Bước 3 - Truy vết trạng thái quá khứ:**
    *   Nếu `X` bằng 1: Hệ thống khôi phục lại trạng thái bắt đầu trận đấu (lượt chuẩn bị ban đầu).
    *   Nếu `X` lớn hơn 1: Truy vấn `MatchSnapshot` của lượt trước đó, tức là lượt `X - 1`. Nếu không tồn tại bản chụp của lượt `X - 1` trong database, hệ thống trả về lỗi "Không tìm thấy snapshot phù hợp để tái đấu" và hủy quy trình.
4.  **Bước 4 - Dọn dẹp dữ liệu tương lai:** Thực hiện xóa toàn bộ các bản ghi lịch sử sự kiện `GameEvent` và dữ liệu lịch sử giao thông `TrafficHistory` của tất cả các lượt đấu lớn hơn hoặc bằng `X` khỏi cơ sở dữ liệu để loại bỏ các dữ liệu rác của lượt đấu lỗi.
5.  **Bước 5 - Khôi phục trạng thái bộ nhớ:** Áp dụng dữ liệu ảnh chụp `Snapshot(X-1)` vào thực thể `MatchState` đang chạy trong bộ nhớ đệm (thực hiện tương tự bước 4 của quy trình khôi phục tự động).
6.  **Bước 6 - Thiết lập lượt đấu hiện tại:** Gán `currentTurn = X`.
7.  **Bước 7 - Bắt đầu lại lượt:** Thiết lập trạng thái trận đấu thành `PLAYING`, ghi nhận `turnStartTime = thời gian hệ thống hiện tại`.
8.  **Bước 8 - Phát tín hiệu tái đấu:** Gửi tín hiệu thông báo tái đấu đến tất cả client của các đội chơi, yêu cầu gửi lại kế hoạch hành động cho lượt đấu `X`.

# TỔNG QUAN CHIẾN LƯỢC HOÀN THIỆN GIAI ĐOẠN 4 (NAPROCK 18th HEXUDON)

Tài liệu này trình bày định hướng chiến lược và các nguyên tắc thiết kế cốt lõi để nâng cấp hệ thống **hexudon** từ một nền tảng giả lập ngoại tuyến (offline simulation) thành một hệ thống thi đấu thời gian thực (real-time), chịu tải đồng thời cao (concurrency), có khả năng tự phục hồi (recovery) và tính toán các yếu tố cơ chế động phức tạp phục vụ giải đấu NAPROCK lần thứ 18.

---

## 1. Tóm tắt trạng thái hệ thống hiện tại

Hệ thống hiện tại đã xây dựng được nền móng vững chắc dựa trên kiến trúc **Hexagonal Architecture** kết hợp với **Domain-Driven Design (DDD)**. Các thành phần cốt lõi đã hoàn thành bao gồm:

*   **MatchState (Aggregate Root):** Quản lý trạng thái vòng đấu, danh sách đội chơi (Team), danh sách các ô bản đồ (Cell), và các điểm phân phối Udon (Spot).
*   **Agent (Entity):** Thực thể đại diện cho các tác nhân di chuyển trên bản đồ, bao gồm hai phân loại là `PatrolAgent` (thu thập Udon) và `RefuelAgent` (nạp năng lượng cho PatrolAgent).
*   **Map & Cell:** Hệ thống bản đồ dạng lưới ô lục giác (Hexagonal Grid), phân loại địa hình cơ bản (`PLAIN` - đồng bằng, `MOUNTAIN` - núi, `ROAD` - đường đi, và `POND` - hồ nước không thể đi qua).
*   **Simulation Loop (Turn-based):** Cơ chế giả lập di chuyển của Agent theo từng lượt (Turn) một cách tuần tự, tính toán chi phí năng lượng và bước đi dựa trên cấu hình cố định trong `MatchConfig`.

---

## 2. Mục tiêu Giai đoạn 4

Giai đoạn 4 đặt ra yêu cầu đưa hệ thống lên mức độ sẵn sàng vận hành giải đấu chính thức. Các mục tiêu cụ thể bao gồm:

1.  **Chuyển đổi sang thời gian thực (Real-time):** Quản lý chu kỳ lượt đấu tự động bằng đồng hồ hệ thống, giới hạn thời gian phản hồi nghiêm ngặt từ các đội chơi.
2.  **Xử lý đồng thời (Concurrency):** Cho phép nhiều đội gửi yêu cầu hành động (Request Actions) cùng lúc trong một lượt mà không gây ra xung đột ghi dữ liệu (Race Conditions) hoặc khóa chết hệ thống (Deadlocks).
3.  **Hệ thống giao thông động (Dynamic Traffic Flow):** Tính toán mật độ giao thông trên các ô đường đi dựa trên lịch sử dừng chân của Agent từ các lượt trước, từ đó làm thay đổi chi phí di chuyển động.
4.  **Hệ thống tính điểm & xếp hạng phức hợp:** Theo dõi chi tiết số loại Udon độc nhất, tích lũy theo ngày, số lượt phục vụ thành công và thời gian phản hồi. Áp dụng quy tắc phân định thắng thua nghiêm ngặt khi bằng điểm (Anti-tie-break).
5.  **Nhật ký trận đấu & Giao tiếp mạng phục vụ Visualizer:** Lưu trữ dòng thời gian sự kiện (timeline) chi tiết và ghi nhận độ trễ, kích thước dữ liệu mạng để truyền tải trực quan lên giao diện người dùng.
6.  **Tự phục hồi và tái đấu (Match Recovery & Rematch):** Cho phép hệ thống lưu trữ ảnh chụp trạng thái (Snapshot) sau mỗi lượt để khôi phục chính xác khi gặp sự cố phần cứng hoặc kích hoạt đấu lại từ một lượt cụ thể.

---

## 3. Phân tích rủi ro kỹ thuật

| Rủi ro kỹ thuật | Mức độ ảnh hưởng | Giải pháp giảm thiểu rủi ro |
| :--- | :--- | :--- |
| **Xung đột luồng (Race Condition):** Nhiều đội gửi request thay đổi hành động của Agent đồng thời trong cùng một lượt. | Cao | Thiết kế hàng đợi xử lý hành động (`TurnExecutionQueue`) bất biến và bất đồng bộ, sắp xếp thứ tự dựa trên dấu thời gian tại server (`RequestOrderingService`). |
| **Sai lệch thời gian do độ trễ mạng (Network Latency):** Đội chơi có đường truyền tốt hơn chiếm ưu thế trong các tích tắc cuối cùng của lượt đấu. | Trung bình | Sử dụng cơ chế bù trừ độ trễ mạng (Network Latency Compensation) bằng cách tính dấu thời gian gửi từ phía Client kết hợp kiểm tra tính hợp lệ trên Server. |
| **Sập nguồn/Lỗi hệ thống giữa trận:** Trận đấu bị gián đoạn và mất toàn bộ dữ liệu trạng thái hiện tại. | Cao | Thiết lập cơ chế chụp trạng thái tự động (`MatchSnapshot`) và ghi nhận điểm phục hồi (`RecoveryPoint`) xuống cơ sở dữ liệu bền vững ở cuối mỗi lượt. |
| **Phép chia cho 0 trong công thức Traffic:** Số lượng đội chơi tham gia giải đấu bằng 0 hoặc không có Agent nào hoạt động. | Thấp | Bổ sung kiểm tra điều kiện biên nghiêm ngặt trong `TrafficCalculator`, trả về giá trị mặc định là 0 nếu mẫu số bằng 0. |

---

## 4. Ràng buộc kiến trúc (Dependency Rule)

Để đảm bảo tính mở rộng và khả năng bảo trì, hệ thống phải tuân thủ nghiêm ngặt mô hình **Hexagonal Architecture** kết hợp với **DDD**:

*   **Tính độc lập của Domain:** Lớp `domain` là trung tâm của hệ thống, chứa toàn bộ quy tắc nghiệp vụ (Business Rules). Nó tuyệt đối không được phép import hoặc phụ thuộc vào bất kỳ class nào thuộc lớp `application`, `adapter` hoặc các thư viện bên ngoài (ngoại trừ các thư viện JDK chuẩn như `java.util`, `java.lang`).
*   **Giao tiếp qua Port:** Lớp `application` điều phối các luồng nghiệp vụ nhưng phải độc lập với các công nghệ bên ngoài. Mọi giao tiếp ra ngoài (lưu trữ DB, gọi API ngoại vi) phải đi qua các Outbound Ports (SPIs). Mọi kích hoạt từ bên ngoài vào ứng dụng phải đi qua các Inbound Ports (Use Cases).
*   **Đóng gói dữ liệu:** Không chia sẻ Entity trực tiếp ra ngoài API. Sử dụng DTOs riêng biệt ở lớp REST Adapter và thực hiện mapping thông qua các `Mappers` chuyên biệt ở lớp Application.

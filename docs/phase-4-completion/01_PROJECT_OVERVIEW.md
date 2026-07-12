# TỔNG QUAN CHIẾN LƯỢC HOÀN THIỆN GIAI ĐOẠN 4 (NAPROCK 18th HEXUDON)

Tài liệu này cung cấp cái nhìn tổng quan về chiến lược phát triển và hoàn thiện Giai đoạn 4 của giải đấu NAPROCK 18th HEXUDON cho dự án Hexudon, được viết dưới góc nhìn của một Kiến trúc sư trưởng Phần mềm.

---

## 1. Bối cảnh & Trạng thái hiện tại của Hệ thống

Hệ thống Hexudon hiện tại đã hoàn thành các cấu phần cốt lõi của một hệ thống mô phỏng trận đấu offline cơ bản:
*   **Mô hình Trạng thái Trận đấu (MatchState)**: Cho phép lưu trữ và quản lý thông tin vòng chơi (Turn), trạng thái trận đấu, danh sách các đội (Teams), ô bản đồ (Cells), các điểm cấp Udon (Spots) và chỉ mục tìm kiếm nhanh ô bản đồ theo tọa độ.
*   **Hệ thống Thực thể Agent**: Hỗ trợ Agent (bao gồm PatrolAgent) di chuyển, thu thập tài nguyên và thực hiện các hành động cơ bản (Action, MoveResult, Direction).
*   **Cấu hình Trận đấu (MatchConfig)**: Lưu trữ các tham số tĩnh cho mô phỏng như nhiên liệu tối đa, số bước tối đa mỗi lượt, lượng Udon ban đầu tại các Spot.
*   **Mô phỏng Lượt (Simulation)**: Vòng lặp mô phỏng lượt chạy đơn luồng (offline) cho phép Agent tiêu thụ tài nguyên và thực thi hành động tuần tự theo cấu hình tĩnh.

Tuy nhiên, hệ thống hiện tại chưa thể đáp ứng môi trường thi đấu thực tế - nơi nhiều đội thi đấu kết nối qua mạng đồng thời, hạ tầng chịu tải cao, môi trường biến động liên tục và yêu cầu tính minh bạch, khả năng khôi phục sau sự cố cực kỳ khắt khe.

---

## 2. Mục tiêu Giai đoạn 4: Hệ thống thi đấu thời gian thực và chịu tải cao

Giai đoạn 4 hướng tới việc nâng cấp toàn diện hệ thống từ một công cụ mô phỏng offline đơn giản thành một **Đấu trường trực tuyến thời gian thực (Real-time Competitive Arena)**. Các mục tiêu cốt lõi bao gồm:

### A. Hệ thống Giao thông Động (Dynamic Traffic Flow)
*   Mô phỏng tình trạng nghẽn mạng giao thông trên các ô đường đi (`ROAD`) dựa trên mật độ hoạt động thực tế của Agent trong quá khứ gần (2 lượt chơi trước đó).
*   Phân cấp độ nghẽn (Smooth, Congested, Traffic Jam) để tác động trực tiếp tới hành vi của các đội chơi.

### B. Chi phí Di chuyển Địa hình (Terrain & Movement Cost)
*   Tính toán chi phí di chuyển động tại thời điểm Agent bắt đầu di chuyển, kết hợp giữa đặc tính vật lý của địa hình (`PLAIN`, `MOUNTAIN`, `ROAD`) và trạng thái giao thông động tại thời điểm đó.
*   Cho phép nhiều Agent đứng chung trên một ô bản đồ mà không giới hạn số lượng, loại bỏ hoàn toàn các xung đột chiếm dụng vật lý thô bạo nhưng vẫn giữ tính cạnh tranh thông qua chi phí thời gian/nhiên liệu.

### C. Hệ thống Tính điểm & Xếp hạng Phức hợp (Scoring & Ranking)
*   Tính toán điểm số theo thời gian thực dựa trên độ đa dạng của loại Udon đã thu thập (Unique Udon Types), tích lũy theo ngày, tổng số lượt phục vụ thành công (Total Servings), và tối ưu hóa thời gian phản hồi (Response Time).
*   Áp dụng cơ chế phân định thứ tự ưu tiên tuyệt đối (Anti-tie-break) nhiều cấp độ để đảm bảo luôn tìm ra nhà vô địch duy nhất mà không có tranh chấp hòa.

### D. Nhật ký trực quan & Giám sát (History, API Logging & Visualizer support)
*   Ghi nhận chi tiết từng sự kiện game (Game Event History) theo dạng dòng thời gian (Timeline Payload) để cung cấp cho hệ thống Visualizer bên ngoài.
*   Thiết kế hệ thống Interceptor giám sát hiệu năng mạng (Api Communication Log) ghi nhận chi tiết độ trễ, kích thước dữ liệu và trạng thái của các yêu cầu gửi đến từ các đội.

### E. Tự phục hồi sau sự cố (Match Recovery & Rematch)
*   Cơ chế Snapshotting tự động lưu trữ toàn bộ trạng thái hệ thống tại các ranh giới an toàn (đầu/cuối lượt chơi).
*   Khả năng Rollback trạng thái khi xảy ra sự cố phần cứng hoặc mạng, đồng thời hỗ trợ trọng tài kích hoạt Rematch (đấu lại từ một Turn bất kỳ).

### F. Xử lý Đồng thời & Bù trễ Mạng (Concurrency & Latency Compensation)
*   Xếp hàng yêu cầu công bằng (Fair ordering queue) dựa trên thời gian máy chủ tiếp nhận request, bù đắp sai số do đường truyền mạng của các đội chơi ở các khoảng cách địa lý khác nhau.
*   Đảm bảo an toàn đa luồng (Thread-safety) tuyệt đối khi nhiều luồng đồng thời ghi nhận hành động trước khi tiến hành xử lý lượt chơi tập trung.

---

## 3. Phân tích Rủi ro Kỹ thuật & Ràng buộc Kiến trúc

### A. Rủi ro Kỹ thuật
1.  **Tranh chấp luồng dữ liệu (Race Conditions) trong ghi nhận hành động**: Nhiều đội chơi gửi hành động cùng lúc ở những mili-giây cuối cùng của Turn có thể gây ra hiện tượng mất mát dữ liệu hoặc xử lý không nhất quán.
2.  **Chia cho 0 (Division by Zero)**: Khi tính toán lưu lượng giao thông trung bình dựa trên số lượng đội chơi, nếu trận đấu thử nghiệm chỉ có 0 đội đăng ký hoặc cấu hình sai, hệ thống có thể bị sập do lỗi toán học.
3.  **Không đồng nhất trạng thái khi Rollback**: Việc khôi phục từ snapshot nếu thực hiện không sâu (shallow copy) sẽ dẫn đến việc các tham chiếu cũ bị rò rỉ, gây sai lệch điểm số hoặc vị trí Agent sau khi khôi phục.
4.  **Độ trễ tích lũy (Latency Accumulation)**: Quá trình ghi log mạng (Api Communication Logging) và ghi sự kiện game nếu thực hiện đồng bộ (Synchronous) trên luồng xử lý chính sẽ làm tăng thời gian phản hồi API và giảm trải nghiệm thời gian thực.

### B. Ràng buộc Kiến trúc (Architectural Constraints)
1.  **Quy tắc Phụ thuộc Hexagonal (Dependency Rule)**:
    *   Tầng **Domain** phải hoàn toàn cô lập: Không import bất kỳ thư viện Spring Framework, JPA, MongoDB hay thư viện tiện ích bên ngoài nào. Mọi logic tính toán Traffic, Cost, Scoring, Ranking phải nằm trọn trong Domain.
    *   Tầng **Application** chỉ phối hợp nghiệp vụ thông qua các Port. Không được phép gọi trực tiếp các Adapter từ Service.
    *   Tầng **Adapter** (Rest Controller, JPA Repository) chịu trách nhiệm chuyển đổi dữ liệu và giao tiếp ngoại vi, không chứa logic nghiệp vụ.
2.  **Tính Bất biến của Value Object (Immutability)**:
    *   Các Value Object như `Coordinate`, `MovementCost`, `UdonType`, `TrafficThreshold` phải là bất biến (Immutable - final fields, không setter, deep copy khi khởi tạo).
3.  **Nguyên tắc Không viết Code trong Tài liệu**:
    *   Tất cả tài liệu thiết kế chỉ được sử dụng bảng dữ liệu, danh sách và ngôn từ thuật toán mô tả từng bước bằng tiếng Việt, tuyệt đối không chèn mã nguồn hoặc pseudo-code.

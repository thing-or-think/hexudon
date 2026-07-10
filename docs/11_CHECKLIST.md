# 11. DANH SÁCH KIỂM TRA CHẤT LƯỢNG (CHECKLIST)

## Mục lục
1. [Hướng dẫn sử dụng Checklist](#1-hướng-dẫn-sử-dụng-checklist)
2. [Checklist triển khai chi tiết](#2-checklist-triển-khai-chi-tiết)
   - [2.1. Cấu trúc và Vị trí Thư mục (Package Checks)](#21-cấu-trúc-và-vị-trí-thư-mục-package-checks)
   - [2.2. Kiểm soát Phụ thuộc và Nhập khẩu (Import & Dependency Checks)](#22-kiểm-soát-phụ-thuộc-và-nhập-khẩu-import--dependency-checks)
   - [2.3. Kiểm thử Biên dịch và Tránh Phụ thuộc Vòng (Compile & Circular Checks)](#23-kiểm-thử-biên-dịch-và-tránh-phụ-thuộc-vòng-compile--circular-checks)
   - [2.4. Tiêu chuẩn Spring Beans & Lõi Ứng dụng (Spring Bean Checks)](#24-tiêu-chuẩn-spring-beans--lõi-ứng-dụng-spring-bean-checks)
   - [2.5. Thiết kế Port & Use Case (Port & Use Case Checks)](#25-thiết-kế-port--use-case-port--use-case-checks)
   - [2.6. Đặc tả DTO & Mapper (DTO & Mapper Checks)](#26-đặc-tả-dto--mapper-dto--mapper-checks)
   - [2.7. Ràng buộc các Adapters (Adapter Checks)](#27-ràng-buộc-các-adapters-adapter-checks)
   - [2.8. Kiểm tra Cấu hình & Lập lịch ngầm (Config & Scheduler Checks)](#28-kiểm-tra-cấu-hình--lập-lịch-ngầm-config--scheduler-checks)
   - [2.9. Kiểm chứng Độ bền vững & Chất lượng Kiểm thử (Testing Checks)](#29-kiểm-chứng-độ-bền-vững--chất-lượng-kiểm-thử-testing-checks)
   - [2.10. Vá các lỗi kỹ thuật hiện hữu (Bug Fix Checks)](#210-vá-các-lỗi-kỹ-thuật-hiện hữu-bug-fix-checks)
3. [Cam kết Chất lượng Kiến trúc (ArchUnit Validation)](#3-cam-kết-chất-lượng-kiến-trúc-archunit-validation)

---

## 1. Hướng dẫn sử dụng Checklist

Lập trình viên thực hiện refactor sử dụng tài liệu này để kiểm tra chéo (Double-Check) chất lượng mã nguồn ở mỗi Phase hoặc trước khi tạo Pull Request (PR) hòa vào nhánh chính. Đánh dấu `[x]` vào các checkbox sau khi đã xác nhận hoàn tất yêu cầu tương ứng.

---

## 2. Checklist triển khai chi tiết

### 2.1. Cấu trúc và Vị trí Thư mục (Package Checks)
- [ ] Mọi tệp tin lớp cũ đã được di dời ra khỏi thư mục phẳng ban đầu.
- [ ] Không còn thư mục rác nào thuộc cấu trúc cũ tồn tại trong project.
- [ ] Package Domain chỉ chứa đúng 6 thư mục con: `model`, `valueobject`, `event`, `service`, `repository`, `exception`.
- [ ] Package Application chỉ chứa đúng 5 thư mục con: `port/in`, `port/out`, `service`, `dto`, `mapper`.
- [ ] Package Adapter chỉ chứa đúng 3 thư mục con: `in/rest`, `out/persistence`, `out/loader`, `out/configuration`.
- [ ] Package Infrastructure chỉ chứa đúng 4 thư mục con: `configuration`, `interceptor`, `scheduler`, `util`.

### 2.2. Kiểm soát Phụ thuộc và Nhập khẩu (Import & Dependency Checks)
- [ ] Các lớp trong package `domain` không có bất kỳ lệnh import nào trỏ tới `com.naprock.hexudon.application.*`, `adapter.*`, hoặc `infrastructure.*`.
- [ ] Các lớp trong package `application` không import bất kỳ lớp nào thuộc `com.naprock.hexudon.adapter.*` hoặc `infrastructure.*`.
- [ ] Các lớp trong `adapter.in.rest` không import trực tiếp các class trong `adapter.out.*` hoặc `application.service` (phải thông qua các Use Case Interfaces).
- [ ] Lớp `HexudonApplication.java` (Bootstrap) nằm ở package gốc bên ngoài 4 layer chính.

### 2.3. Kiểm thử Biên dịch và Tránh Phụ thuộc Vòng (Compile & Circular Checks)
- [ ] Dự án biên dịch (compile) thành công thông qua lệnh Maven (`mvn clean compile`) mà không tạo ra bất kỳ cảnh báo deprecation nào liên quan đến package.
- [ ] Không có hiện tượng phụ thuộc vòng (Circular Dependency) giữa các Spring Beans (Ví dụ: Service A gọi Service B và ngược lại).
- [ ] Không có sự phụ thuộc vòng giữa các package ở mức độ kiểm thử của ArchUnit.

### 2.4. Tiêu chuẩn Spring Beans & Lõi Ứng dụng (Spring Bean Checks)
- [ ] Không sử dụng bất kỳ Annotation Spring nào (`@Component`, `@Service`, `@Repository`, `@Autowired`) trong toàn bộ tầng Domain Core (bao gồm cả Domain Service và Model).
- [ ] Các Domain Engine (`MovementSimulator`, `FuelManager`, v.v.) được khởi tạo thông qua Java thuần túy hoặc cấu hình Java Bean tập trung.
- [ ] Các Interface Port của Application được tiêm vào Controller bằng cơ chế tiêm Constructor của Spring (Constructor Injection) thay vì dùng `@Autowired` trực tiếp lên field.

### 2.5. Thiết kế Port & Use Case (Port & Use Case Checks)
- [ ] Toàn bộ Use Cases ở cổng đầu vào (`application.port.in`) được định nghĩa dưới dạng Interface độc lập.
- [ ] Toàn bộ Outbound Ports ở cổng đầu ra (`application.port.out`) được định nghĩa dưới dạng Interface độc lập.
- [ ] `MatchApplicationService` triển khai (implement) đầy đủ tất cả các Use Case Interfaces ở cổng vào và chỉ tương tác với cổng ra qua các Interface Outbound Port.

### 2.6. Đặc tả DTO & Mapper (DTO & Mapper Checks)
- [ ] Tất cả các DTO gửi/nhận qua REST API được thiết kế dưới dạng Java Record để bảo đảm tính bất biến (Immutable).
- [ ] Các DTO không chứa bất kỳ logic nghiệp vụ game nào.
- [ ] `ActionMapper` chịu trách nhiệm chuyển đổi hoàn toàn dữ liệu giữa DTO và Domain Objects. Tầng Domain tuyệt đối không trực tiếp nhận hoặc trả về các lớp DTO.

### 2.7. Ràng buộc các Adapters (Adapter Checks)
- [ ] `MatchController` không chứa bất kỳ lô-gích nghiệp vụ game nào ngoài việc chuyển tiếp request và xử lý HTTP status.
- [ ] `InMemoryMatchStateRepository` triển khai đồng thời cả `MatchStateStorePort` và `MatchStateRepository`, cô lập hoàn toàn cơ chế lưu trữ RAM vật lý.
- [ ] `FileMatchConfigLoader` bọc kín logic đọc file cấu hình bằng `FileUtils` và trả về domain config bất biến.

### 2.8. Kiểm tra Cấu hình & Lập lịch ngầm (Config & Scheduler Checks)
- [ ] `WebConfig.java` đăng ký chính xác Interceptor cho các tiến trình kỹ thuật.
- [ ] `SchedulerConfig.java` được cấu hình chạy ngầm độc lập với luồng xử lý HTTP REST API của người dùng.
- [ ] Cấu hình CORS được tách biệt rõ ràng ở lớp hạ tầng kỹ thuật.

### 2.9. Kiểm chứng Độ bền vững & Chất lượng Kiểm thử (Testing Checks)
- [ ] Toàn bộ suite test 57 trường hợp hiện tại chạy thành công 100% bằng lệnh `mvn test`.
- [ ] Không có test case nào bị bỏ qua hoặc vô hiệu hóa (`@Disabled`) để đối phó với lỗi refactor package.
- [ ] Viết bổ sung hoặc cập nhật `ArchitectureTest.java` sử dụng thư viện ArchUnit để ràng buộc kiến trúc Hexagonal tự động.

### 2.10. Vá các lỗi kỹ thuật hiện hữu (Bug Fix Checks)
- [ ] **Sửa lỗi Rate Limiter:** Đảm bảo interceptor trong `WebConfig` đăng ký khớp đường dẫn `/api/match/actions` (có chữ 's').
- [ ] **Sửa lỗi Scheduler kẹt:** Đảm bảo gọi phương thức cập nhật trạng thái nộp bài `team.setSubmittedPlan(true)` trong luồng `submitActions` của Application Service để scheduler có thể mô phỏng tự động mà không bị kẹt chờ hết giờ.
- [ ] **Dọn dẹp DTO thừa:** Loại bỏ hoặc chuẩn hóa việc sử dụng `TeamActionRequest` và `TeamActionResponse` trong mã nguồn.

---

## 3. Cam kết Chất lượng Kiến trúc (ArchUnit Validation)

Để PR refactor được phê duyệt, lớp kiểm thử kiến trúc (`ArchitectureTest.java`) phải chứa các xác nhận luật kiểm thử sau chạy thành công:

- [ ] **Luật 1:** Tầng Domain không được truy cập vào các lớp thuộc tầng Application, Adapter, Infrastructure.
- [ ] **Luật 2:** Tầng Application không được truy cập vào các lớp thuộc tầng Adapter, Infrastructure.
- [ ] **Luật 3:** Các Inbound Adapters chỉ được giao tiếp với tầng Application qua các Interface Use Case.
- [ ] **Luật 4:** Các Outbound Adapters phải triển khai các Interface Port tương ứng định nghĩa ở tầng Application hoặc Domain Repository.

# Tài liệu Thiết kế Exception Handling - 12_SECURITY_GUIDELINES

## 1. Purpose (Mục đích)
Tài liệu này định nghĩa các nguyên tắc và hướng dẫn bảo mật thông tin (Security Guidelines) trong quá trình xử lý ngoại lệ và trả lỗi của **HEXUDON Server**. Mục tiêu cốt lõi là ngăn chặn tối đa việc rò rỉ thông tin nội bộ của hệ thống (Information Disclosure) ra ngoài môi trường Internet thông qua các thông báo lỗi thô, từ đó bảo vệ hệ thống trước các cuộc tấn công khai thác lỗ hổng bảo mật.

---

## 2. Scope (Phạm vi)
Áp dụng đối với toàn bộ các lớp cấu tạo nên hệ thống Exception Handling, Logging và các API Responses của dự án.

---

## 3. Threat Model (Mô hình đe dọa từ lỗi thô)
Khi một hệ thống trả về thông báo lỗi thô (raw error messages), kẻ tấn công (hackers) có thể thu thập các thông tin sau để lên kế hoạch tấn công nâng cao:
*   **Database Schema Leakage**: Lỗi SQL tiết lộ tên bảng, tên cột, kiểu dữ liệu và mối quan hệ khóa ngoại (hỗ trợ tấn công SQL Injection).
*   **Stacktrace Leakage**: Tiết lộ cấu trúc phân lớp, tên thư viện bên thứ ba đang dùng và phiên bản cụ thể (hỗ trợ tìm kiếm lỗ hổng CVE của thư viện).
*   **System Directory Leakage**: Tiết lộ cấu trúc thư mục vật lý trên máy chủ của server (ví dụ: `/home/ubuntu/app/hexudon/`), hỗ trợ tấn công đọc file tùy ý (Directory Traversal).
*   **Package Structure**: Lộ cấu trúc package nội bộ (ví dụ: `com.naprock.hexudon.engine.MovementSimulator`), hỗ trợ phân tích ngược mã nguồn.

---

## 4. Security Principles (Các nguyên tắc bảo mật khi xử lý lỗi)

### 4.1. Nguyên tắc "Quyền hạn thông tin tối thiểu" (Least Privilege of Information)
Client chỉ cần biết lỗi đó là gì ở mức độ nghiệp vụ để khắc phục (ví dụ: gửi thiếu tham số, sai lượt đi). Client hoàn toàn không cần biết máy chủ gặp lỗi lập trình gì ở dòng code nào. 

### 4.2. Nguyên tắc "Mặc định che giấu" (Default Masking)
Bất kỳ ngoại lệ nào nằm ngoài danh sách `BusinessException` được khai báo trước đều được xem là lỗi hệ thống nhạy cảm. Chúng phải được tự động ánh xạ về mã lỗi chung `INTERNAL_SERVER_ERROR` và thông điệp chung `"An unexpected error occurred. Please contact the administrator."` trước khi gửi ra ngoài.

---

## 5. Items to Hide / Mask (Danh sách các thông tin tuyệt đối không expose)

| Loại thông tin | Lý do cấm | Cơ chế che giấu tại GlobalExceptionHandler |
| :--- | :--- | :--- |
| **SQL/JDBC Exceptions** | Lộ cấu trúc bảng, tên cột, loại database đang dùng. | Bắt tại lớp cha `SQLException.class` hoặc `DataAccessException.class`, ghi log error và trả về `INTERNAL_SERVER_ERROR`. |
| **Java Stacktrace** | Lộ các class, hàm, số dòng và cấu trúc logic code. | Không bao giờ đưa đối tượng `StackTraceElement[]` vào JSON `ErrorResponse`. |
| **NullPointerException** | Lộ điểm yếu trong logic xử lý rỗng của mã nguồn. | Trả về thông báo lỗi hệ thống chung. |
| **Đường dẫn File vật lý**| Lộ cấu trúc thư mục hệ điều hành máy chủ. | Lọc bỏ các thông điệp của `IOException` chứa đường dẫn file trước khi trả về. |
| **Tên Package/Class** | Lộ cấu trúc mã nguồn nội bộ. | Tuyệt đối không đưa tên package `com.naprock...` vào thông điệp lỗi gửi cho client. |

---

## 6. Security Handling in Logging (Bảo mật trong Logging)
Mặc dù thông tin lỗi chi tiết bị che giấu đối với Client, nhưng chúng bắt buộc phải được ghi lại đầy đủ và an toàn ở Log file nội bộ trên máy chủ.
*   **Giới hạn quyền truy cập log**: File log lưu trên đĩa cứng máy chủ phải được phân quyền truy cập nghiêm ngặt (chỉ có user chạy ứng dụng và admin hệ thống có quyền đọc).
*   **Ngăn chặn Log Injection**: Không ghi trực tiếp các tham số chưa qua kiểm định do client gửi lên vào log mà không có định dạng hoặc làm sạch, tránh kẻ tấn công chèn mã độc (như ký tự xuống dòng `\n` hoặc mã khai thác `Log4Shell`) vào log file.

---

## 7. Common Mistakes (Sai lầm thường gặp)
*   **Sử dụng `e.printStackTrace()` trong code**: Phương thức này in trực tiếp stacktrace ra console tiêu chuẩn (System.out/err), không đi qua cấu hình log file, không thể tắt được ở môi trường Production và gây tắc nghẽn I/O.
*   **Trả về `ex.getMessage()` của các Exception hệ thống**: Ví dụ trả về `ex.getMessage()` khi bắt `NullPointerException` hoặc `SQLException`. Lỗi này lập tức để lộ thông tin cấu trúc code cho client.

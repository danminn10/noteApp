# NoteApp

NoteApp là một ứng dụng ghi chú đơn giản và hiệu quả, giúp bạn quản lý các ghi chú cá nhân một cách dễ dàng. Ứng dụng cho phép bạn tạo, chỉnh sửa, xóa, và tìm kiếm ghi chú. Ngoài ra, bạn có thể thêm các danh mục để phân loại ghi chú, đặt lời nhắc, và thậm chí xuất ghi chú ra file Word.

## Tính năng chính

- **Tạo và quản lý ghi chú**: Tạo ghi chú mới, chỉnh sửa hoặc xóa ghi chú hiện có.
- **Phân loại ghi chú**: Thêm danh mục để phân loại ghi chú, giúp bạn dễ dàng tìm kiếm và quản lý.
- **Tìm kiếm ghi chú**: Tìm kiếm ghi chú theo tiêu đề hoặc nội dung.
- **Đặt lời nhắc**: Đặt lời nhắc cho ghi chú để không bỏ lỡ các công việc quan trọng.
- **Xuất ghi chú**: Xuất ghi chú ra file Word để chia sẻ hoặc lưu trữ.
- **Widget**: Thêm widget trên màn hình chính để truy cập nhanh vào ghi chú.

## Công nghệ sử dụng

- **Ngôn ngữ lập trình**: Java
- **Cơ sở dữ liệu**: Room Database
- **Giao diện người dùng**: XML Layout
- **Thư viện hỗ trợ**: 
  - `RecyclerView` để hiển thị danh sách ghi chú.
  - `AlarmManager` để quản lý các lời nhắc.
  - `Apache POI` để xuất ghi chú ra file Word.

## Cài đặt

1. **Clone repository**:
   ```
   bash
   git clone https://github.com/danminn10/NoteApp.git
   ```
3. **Mở project bằng Android Studio**:
   - Mở Android Studio và chọn "Open an existing project".
   - Dẫn đến thư mục chứa project vừa clone và chọn `build.gradle`.

4. **Chạy ứng dụng**:
   - Kết nối thiết bị Android hoặc sử dụng máy ảo.
   - Nhấn `Run` trong Android Studio để cài đặt và chạy ứng dụng.

## Cấu trúc project

- **`MainActivity.java`**: Activity chính, hiển thị danh sách ghi chú và xử lý các tương tác chính.
- **`NotesTakerActivity.java`**: Activity để tạo và chỉnh sửa ghi chú.
- **`NotesListAdapter.java`**: Adapter để hiển thị danh sách ghi chú trong RecyclerView.
- **`NoteWidgetProvider.java`**: Widget để hiển thị ghi chú trên màn hình chính.
- **`ReminderReceiver.java`**: BroadcastReceiver để xử lý các lời nhắc.
- **`RoomDB.java`**: Cơ sở dữ liệu Room để lưu trữ ghi chú, danh mục và lời nhắc.
- **`MainDAO.java`, `CategoryDAO.java`, `ReminderDAO.java`**: Các DAO (Data Access Object) để tương tác với cơ sở dữ liệu.

## Hướng dẫn sử dụng

1. **Tạo ghi chú mới**:
   - Nhấn vào nút dấu cộng (+) ở góc dưới bên phải màn hình.
   - Nhập tiêu đề và nội dung ghi chú, sau đó nhấn nút lưu.

2. **Chỉnh sửa hoặc xóa ghi chú**:
   - Nhấn vào ghi chú để chỉnh sửa.
   - Nhấn và giữ ghi chú để hiển thị menu, chọn xóa hoặc đặt lời nhắc.

3. **Thêm danh mục**:
   - Trong màn hình tạo hoặc chỉnh sửa ghi chú, nhấn vào biểu tượng thêm danh mục để tạo danh mục mới.

4. **Đặt lời nhắc**:
   - Nhấn và giữ ghi chú, chọn "Set Reminder" để đặt lời nhắc.

5. **Xuất ghi chú**:
   - Trong màn hình tạo hoặc chỉnh sửa ghi chú, nhấn vào biểu tượng xuất để xuất ghi chú ra file Word.

6. **Thêm widget**:
   - Nhấn và giữ ghi chú, chọn "Add Widget" để thêm widget trên màn hình chính.

## Đóng góp

Nếu bạn muốn đóng góp vào dự án, vui lòng tạo một pull request. Mọi đóng góp đều được hoan nghênh!

## Giấy phép

Dự án này được phân phối dưới giấy phép MIT. Xem file [LICENSE](LICENSE) để biết thêm chi tiết.

---

**Lưu ý**: Đây là một dự án mẫu và có thể chứa một số lỗi hoặc hạn chế. Vui lòng báo cáo các vấn đề nếu bạn gặp phải.

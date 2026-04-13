# Đánh giá hiện trạng app ThucTapTotNghiep

## 1) Điểm mạnh hiện tại
- Đã có luồng cơ bản khá đầy đủ: đăng ký/đăng nhập, home, tìm kiếm, upload, hồ sơ, chi tiết tài liệu.
- Có tích hợp Firebase Auth + Firebase Cloud Messaging.
- Có API contract cho nhiều chức năng backend (documents, profile, favorite, watch-later).
- Có dùng Jetpack Compose, Navigation, và bước đầu dùng Hilt.

## 2) Các thiếu sót quan trọng cần ưu tiên

### A. Kiến trúc & DI chưa đồng nhất
- Hiện tại đang tồn tại **2 cách gọi API song song**:
  1) `RetrofitClient` dùng `http://10.0.2.2:3000/` (local).
  2) Hilt `NetworkModule` dùng `https://api.vidu-thuctap.com/`.
- Nhiều ViewModel đang gọi trực tiếp `RetrofitClient` thay vì đi qua Repository + Hilt.
- Hệ quả: khó đổi môi trường dev/staging/prod, khó test unit, dễ lỗi không đồng nhất dữ liệu.

**Khuyến nghị:** chuẩn hóa về 1 luồng `ViewModel -> Repository -> ApiService (inject từ Hilt)`.

### B. Bảo mật cấu hình mạng còn yếu
- `android:usesCleartextTraffic="true"` đang cho phép HTTP cleartext toàn app.
- Base URL local HTTP hardcode trong mã nguồn.

**Khuyến nghị:**
- Dùng HTTPS cho môi trường production.
- Tách base URL theo build variant (`debug`/`release`) và `BuildConfig`.
- Giới hạn cleartext chỉ cho debug nếu thực sự cần.

### C. Quản trị lỗi & trạng thái chưa đủ sâu
- Nhiều màn hình mới bắt `Exception` chung, thiếu mapping lỗi theo HTTP code/timeout/network.
- Chưa có chiến lược retry, empty state chuẩn, skeleton/loading nhất quán toàn app.

**Khuyến nghị:**
- Tạo `Result`/`UiState` dùng chung (Success, Error, Loading, Empty).
- Chuẩn hóa thông điệp lỗi thân thiện cho người dùng.

### D. Test coverage gần như chưa có
- Hiện repo mới có test mẫu mặc định (`ExampleUnitTest`, `ExampleInstrumentedTest`).
- Chưa có test cho ViewModel/Repository/Navigation.

**Khuyến nghị:**
- Bổ sung unit test cho auth, home fetch, upload validation.
- Bổ sung UI test cho luồng login -> home -> detail.

### E. Build/Release readiness chưa sẵn sàng
- `release` đang `isMinifyEnabled = false`.
- Chưa thấy chiến lược CI/CD, quality gates (lint, detekt/ktlint, unit test bắt buộc).
- Chưa có crash monitoring/analytics rõ ràng.

**Khuyến nghị:**
- Bật minify cho release sau khi hoàn thiện rule.
- Thêm pipeline CI chạy lint + test + assemble.
- Tích hợp Crashlytics/Sentry.

## 3) Các tính năng sản phẩm còn thiếu (góc nhìn người dùng)
- Chưa thấy download/offline đọc PDF.
- Chưa thấy phân quyền vai trò (admin/mod/user) nếu hệ thống cần quản trị nội dung.
- Chưa thấy flow quên mật khẩu/reset mật khẩu.
- Chưa thấy onboarding + hướng dẫn người dùng mới.
- Chưa thấy reporting/spam moderation cho tài liệu vi phạm.

## 4) Lộ trình đề xuất 4 tuần (thực tế, ít rủi ro)

### Tuần 1: Chuẩn hóa nền tảng
- Gộp Retrofit về Hilt, bỏ gọi trực tiếp `RetrofitClient` ở ViewModel.
- Tách config base URL theo build variant.
- Chuẩn hóa `UiState` + error handling.

### Tuần 2: Ổn định chất lượng
- Viết unit test cho ViewModel/Repository quan trọng.
- Thêm lint + formatting + kiểm tra trong CI.

### Tuần 3: Nâng UX cốt lõi
- Hoàn thiện empty/error/loading states.
- Bổ sung quên mật khẩu + cải thiện form validation.

### Tuần 4: Sẵn sàng release
- Bật minify release, rà soát Proguard.
- Tích hợp crash monitoring.
- Chạy test hồi quy luồng chính trước phát hành.

## 5) Checklist ưu tiên ngắn gọn (P0/P1/P2)

### P0 (làm ngay)
- Đồng nhất Retrofit/Hilt/Repository.
- Tách base URL & siết cleartext cho production.
- Bổ sung test cho luồng đăng nhập và tải danh sách tài liệu.

### P1
- Hoàn thiện xử lý lỗi chuẩn và UX trạng thái rỗng.
- Bổ sung quên mật khẩu.

### P2
- Offline mode, analytics sâu, moderation nội dung.

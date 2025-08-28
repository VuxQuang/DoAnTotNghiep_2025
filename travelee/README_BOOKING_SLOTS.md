# Hệ thống Quản lý Số chỗ còn lại cho Tour

## Tổng quan

Hệ thống này tự động cập nhật số chỗ còn lại (`availableSlots`) trong `TourSchedule` dựa trên trạng thái của `Booking`. Điều này đảm bảo rằng số chỗ còn lại luôn chính xác và người dùng không thể đặt tour khi đã hết chỗ.

## Cách hoạt động

### 1. Khi tạo Booking mới

- **Trạng thái mặc định**: `PENDING`
- **Số chỗ**: Không bị trừ (chỉ giữ chỗ tạm thời)
- **Khi xác nhận**: Số chỗ sẽ bị trừ từ `availableSlots`

### 2. Các trường hợp cập nhật số chỗ

#### Trừ số chỗ (Decrease Slots)
- `PENDING` → `CONFIRMED`: Xác nhận giữ chỗ
- `PENDING` → `PAID`: Thanh toán trực tiếp
- `CANCELLED` → `CONFIRMED`: Khôi phục booking đã hủy
- `CANCELLED` → `PAID`: Khôi phục và thanh toán trực tiếp

#### Hoàn tác số chỗ (Increase Slots)
- `CONFIRMED` → `CANCELLED`: Hủy booking đã xác nhận
- `PAID` → `CANCELLED`: Hủy booking đã thanh toán
- `COMPLETED` → `CANCELLED`: Hủy booking đã hoàn thành
- `PAID` → `REFUNDED`: Hoàn tiền
- `CONFIRMED` → `REFUNDED`: Hoàn tiền trước khi thanh toán

#### Không thay đổi số chỗ
- `CONFIRMED` → `PAID`: Thanh toán sau khi xác nhận
- `PAID` → `COMPLETED`: Hoàn thành tour
- `CONFIRMED` → `COMPLETED`: Hoàn thành tour mà chưa thanh toán

### 3. Validation

- Kiểm tra số chỗ còn lại trước khi trừ
- Nếu không đủ chỗ, throw `IllegalStateException`
- Logging chi tiết cho việc debug

## Các phương thức chính

### `updateAvailableSlots(booking, oldStatus, newStatus)`
- Phương thức private xử lý logic cập nhật số chỗ
- Được gọi từ các phương thức thay đổi trạng thái

### `updateBookingStatus(bookingId, status)`
- Cập nhật trạng thái booking
- Tự động cập nhật số chỗ còn lại
- Kiểm tra validation trước khi cập nhật

### `cancelBooking(bookingId, reason)`
- Hủy booking của người dùng
- Hoàn tác số chỗ nếu cần

### `cancelBookingByAdmin(bookingId, reason)`
- Admin hủy booking
- Hoàn tác số chỗ nếu cần

### `refundBooking(bookingId, amount, reason)`
- Hoàn tiền booking
- Hoàn tác số chỗ nếu cần

## Ví dụ sử dụng

### Xác nhận booking
```java
// Từ PENDING -> CONFIRMED
bookingService.updateBookingStatus(bookingId, BookingStatus.CONFIRMED);
// Số chỗ sẽ bị trừ
```

### Hủy booking
```java
// Hủy booking đã xác nhận
bookingService.cancelBooking(bookingId, "Lý do hủy");
// Số chỗ sẽ được hoàn tác
```

### Hoàn tiền
```java
// Hoàn tiền booking đã thanh toán
bookingService.refundBooking(bookingId, amount, "Lý do hoàn tiền");
// Số chỗ sẽ được hoàn tác
```

## Logging

Hệ thống log chi tiết các thay đổi:
- `INFO`: Khi trừ hoặc hoàn tác số chỗ thành công
- `WARN`: Khi không đủ chỗ để trừ
- `DEBUG`: Khi không cần thay đổi số chỗ

## Lưu ý quan trọng

1. **Transaction Safety**: Tất cả các thay đổi được thực hiện trong transaction
2. **Validation**: Kiểm tra số chỗ trước khi thực hiện thay đổi
3. **Logging**: Ghi log đầy đủ để dễ dàng debug
4. **Error Handling**: Xử lý lỗi gracefully với thông báo rõ ràng

## Testing

File test `BookingServiceTest.java` bao gồm các test case cho:
- Trừ số chỗ khi xác nhận
- Hoàn tác số chỗ khi hủy
- Không thay đổi số chỗ khi không cần thiết
- Xử lý trường hợp không đủ chỗ
- Xử lý trường hợp không giới hạn số chỗ

## Cập nhật Frontend (HTML/CSS)

### 1. **Tour Detail Page (`tour-detail.html`)**

#### **Availability Alert (Cảnh báo tình trạng chỗ)**
- Hiển thị thông báo tổng quan về tình trạng chỗ của tour
- 3 trạng thái: **Available** (Còn nhiều chỗ), **Limited** (Sắp hết chỗ), **Full** (Đã đầy)
- Vị trí: Phía trên tiêu đề tour

#### **Enhanced Schedule Display (Hiển thị lịch trình nâng cao)**
- **Số chỗ còn lại**: Hiển thị rõ ràng với icon và số lượng
- **Trạng thái chỗ**: Badge màu sắc cho từng trạng thái
- **Button trạng thái**: Thay đổi màu sắc và text dựa trên số chỗ còn lại

#### **Booking Information (Thông tin đặt tour)**
- **Tổng số chỗ trống**: Hiển thị trong meta của tour
- **Ghi chú đặt tour**: Cảnh báo khi sắp hết chỗ
- **Button đặt tour**: Thay đổi trạng thái dựa trên availability

### 2. **CSS Styling (`tour-detail.css`)**

#### **Availability Alert Styles**
```css
.availability-alert .alert.available { /* Xanh lá - Còn nhiều chỗ */ }
.availability-alert .alert.limited  { /* Vàng - Sắp hết chỗ */ }
.availability-alert .alert.full     { /* Đỏ - Đã đầy */ }
```

#### **Schedule Slots Styles**
```css
.schedule-slots { /* Container cho thông tin chỗ */ }
.slots-status .status { /* Badge trạng thái */ }
```

#### **Button States**
```css
.btn-select-date.available { /* Xanh - Có thể đặt */ }
.btn-select-date.limited   { /* Vàng - Sắp hết chỗ */ }
.btn-select-date.full      { /* Xám - Đã đầy */ }
```

### 3. **Các trạng thái hiển thị**

#### **Số chỗ còn lại:**
- **> 10 chỗ**: "Còn nhiều chỗ" (Xanh lá)
- **1-10 chỗ**: "Sắp hết chỗ" (Vàng)
- **0 chỗ**: "Đã đầy" (Đỏ)
- **null**: "Không giới hạn chỗ" (Xanh dương)

#### **Button trạng thái:**
- **Available**: "Chọn ngày này" (Xanh, có thể click)
- **Limited**: "Sắp hết chỗ" (Vàng, có thể click)
- **Full**: "Đã đầy" (Xám, không thể click)

### 4. **Responsive Design**
- Tất cả các phần đều responsive
- Mobile-friendly với layout thích ứng
- Touch-friendly buttons và alerts

### 5. **Accessibility**
- Icon và màu sắc có ý nghĩa rõ ràng
- Text mô tả đầy đủ cho screen readers
- Contrast ratio phù hợp với WCAG guidelines

## Tóm tắt

Với những cập nhật này, người dùng sẽ:
✅ **Thấy rõ ràng** số chỗ còn lại của từng lịch trình  
✅ **Nhận được cảnh báo** khi tour sắp hết chỗ  
✅ **Hiểu trạng thái** của từng ngày khởi hành  
✅ **Có trải nghiệm tốt** trên mọi thiết bị  
✅ **Được thông báo** khi không thể đặt tour  

Hệ thống frontend giờ đây hoàn toàn đồng bộ với backend để hiển thị thông tin số chỗ còn lại chính xác và real-time.

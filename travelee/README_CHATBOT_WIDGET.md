# Chatbot Widget - Hướng dẫn sử dụng

## Tổng quan
Chatbot Widget là một component JavaScript độc lập có thể được tích hợp vào bất kỳ trang web nào. Widget tự động tạo HTML, CSS và xử lý logic tương tác với Google Gemini AI.

## Tính năng
- ✅ Tự động tạo HTML và CSS
- ✅ Tích hợp trực tiếp với Google Gemini API
- ✅ Giao diện đẹp, responsive
- ✅ Không phụ thuộc vào backend Spring Boot
- ✅ Có thể import vào bất kỳ trang nào

## Cách sử dụng

### 1. Cấu hình API Key
Thêm meta tag vào `<head>` của trang HTML:
```html
<meta name="gemini-api-key" content="YOUR_GEMINI_API_KEY">
```

### 2. Import JavaScript
Thêm script vào cuối trang:
```html
<script src="/page/js/chatbot-widget.js"></script>
```

### 3. Sử dụng trong Spring Boot với Thymeleaf
```html
<meta name="gemini-api-key" th:content="${@environment.getProperty('google.gemini.api-key')}">
<script th:src="@{/page/js/chatbot-widget.js}"></script>
```

### 4. Sử dụng tùy chỉnh
```javascript
// Tạo instance tùy chỉnh
const chatbot = new ChatbotWidget({
    geminiApiKey: 'your-api-key',
    geminiModel: 'gemini-1.5-flash'
});

// Khởi tạo
chatbot.init();
```

## Cấu hình

### Options
- `geminiApiKey`: API key của Google Gemini (bắt buộc)
- `geminiModel`: Model Gemini sử dụng (mặc định: 'gemini-1.5-flash')

### System Prompt
Widget sử dụng system prompt được tối ưu cho chatbot du lịch Travelee:
- Hỗ trợ tư vấn tour du lịch
- Hướng dẫn đặt tour
- Tư vấn giá cả và dịch vụ
- Giới thiệu điểm đến
- Giải đáp thắc mắc

## API Endpoints (không cần thiết)
Widget hoạt động độc lập, không cần các endpoint sau:
- ❌ `/api/chatbot/chat`
- ❌ `/api/chatbot/welcome`
- ❌ `/api/chatbot/test`

## Files liên quan
- `chatbot-widget.js`: File chính chứa toàn bộ logic
- `home.html`: Ví dụ sử dụng trong Spring Boot
- `application.properties`: Chứa cấu hình Gemini API key

## Lưu ý bảo mật
- API key được truyền qua meta tag, đảm bảo không expose trong source code
- Sử dụng HTTPS trong production
- Có thể implement rate limiting nếu cần

## Troubleshooting
1. **Chatbot không hiển thị**: Kiểm tra console log, đảm bảo API key đúng
2. **API lỗi**: Kiểm tra network tab, đảm bảo API key có quyền truy cập Gemini
3. **Styling bị lỗi**: Đảm bảo Font Awesome và Google Fonts được load

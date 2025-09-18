// Chatbot Widget - Tự động tạo HTML và xử lý logic với Spring Boot API
class ChatbotWidget {
    constructor(options = {}) {
        this.isInitialized = false;
        this.chatButton = null;
        this.chatPopup = null;
        this.messageInput = null;
        this.sendButton = null;
        this.chatMessages = null;
        this.welcomeMessage = null;
        this.closeChat = null;
        
        // API endpoints configuration
        this.chatEndpoint = options.chatEndpoint || '/api/chatbot/chat';
        this.welcomeEndpoint = options.welcomeEndpoint || '/api/chatbot/welcome';
        this.testEndpoint = options.testEndpoint || '/api/chatbot/test';
    }

    // Tạo HTML cho chatbot widget
    createHTML() {
        // Tạo chat widget container
        const chatWidget = document.createElement('div');
        chatWidget.className = 'chat-widget';
        chatWidget.innerHTML = `
            <div class="chat-button" id="chatButton">
                <i class="fas fa-comments"></i>
            </div>
            <div class="chat-popup" id="chatPopup">
                <div class="chat-header">
                    <div class="chat-agent">
                        <img src="https://images.unsplash.com/photo-1494790108755-2616b612b786?w=40&h=40&fit=crop&crop=face" alt="Tổng đài viên">
                        <div class="agent-info">
                            <h4>TravelGo Support</h4>
                            <span class="status online">Đang hoạt động</span>
                        </div>
                    </div>
                    <button class="close-chat" id="closeChat">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
                <div class="chat-messages" id="chatMessages">
                    <div class="message agent">
                        <div class="message-content">
                            <p id="welcomeMessage">👋 Chào mừng bạn đến với Travelee!</p>
                            <span class="time" id="welcomeTime">Bây giờ</span>
                        </div>
                    </div>
                </div>
                <div class="chat-input">
                    <input type="text" id="messageInput" placeholder="Nhập tin nhắn...">
                    <button id="sendMessage">
                        <i class="fas fa-paper-plane"></i>
                    </button>
                </div>
            </div>
        `;

        // Thêm CSS nếu chưa có
        this.addCSS();

        // Thêm vào body
        document.body.appendChild(chatWidget);

        // Lấy các elements
        this.chatButton = document.getElementById('chatButton');
        this.chatPopup = document.getElementById('chatPopup');
        this.closeChat = document.getElementById('closeChat');
        this.messageInput = document.getElementById('messageInput');
        this.sendButton = document.getElementById('sendMessage');
        this.chatMessages = document.getElementById('chatMessages');
        this.welcomeMessage = document.getElementById('welcomeMessage');

        return true;
    }

    // Thêm CSS cho chatbot
    addCSS() {
        // Kiểm tra xem CSS đã được thêm chưa
        if (document.getElementById('chatbot-widget-css')) {
            return;
        }

        const style = document.createElement('style');
        style.id = 'chatbot-widget-css';
        style.textContent = `
            .chat-widget {
                position: fixed;
                bottom: 20px;
                right: 20px;
                z-index: 1000;
                font-family: 'Inter', sans-serif;
            }

            .chat-button {
                width: 60px;
                height: 60px;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                cursor: pointer;
                box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
                transition: all 0.3s ease;
                color: white;
                font-size: 24px;
            }

            .chat-button:hover {
                transform: scale(1.1);
                box-shadow: 0 6px 25px rgba(102, 126, 234, 0.6);
            }

            .chat-popup {
                position: absolute;
                bottom: 80px;
                right: 0;
                width: 350px;
                height: 500px;
                background: white;
                border-radius: 20px;
                box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
                display: none;
                flex-direction: column;
                overflow: hidden;
                transform: translateY(20px);
                opacity: 0;
                transition: all 0.3s ease;
            }

            .chat-popup.active {
                display: flex;
                transform: translateY(0);
                opacity: 1;
            }

            .chat-header {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                padding: 20px;
                display: flex;
                align-items: center;
                justify-content: space-between;
            }

            .chat-agent {
                display: flex;
                align-items: center;
                gap: 12px;
            }

            .chat-agent img {
                width: 40px;
                height: 40px;
                border-radius: 50%;
                object-fit: cover;
            }

            .agent-info h4 {
                margin: 0;
                font-size: 16px;
                font-weight: 600;
            }

            .status {
                font-size: 12px;
                opacity: 0.9;
            }

            .status.online::before {
                content: '●';
                color: #4ade80;
                margin-right: 6px;
            }

            .close-chat {
                background: none;
                border: none;
                color: white;
                font-size: 18px;
                cursor: pointer;
                padding: 5px;
                border-radius: 50%;
                transition: background 0.2s ease;
            }

            .close-chat:hover {
                background: rgba(255, 255, 255, 0.2);
            }

            .chat-messages {
                flex: 1;
                padding: 20px;
                overflow-y: auto;
                background: #f8fafc;
            }

            .message {
                margin-bottom: 15px;
                display: flex;
            }

            .message.user {
                justify-content: flex-end;
            }

            .message.agent {
                justify-content: flex-start;
            }

            .message-content {
                max-width: 80%;
                padding: 12px 16px;
                border-radius: 18px;
                position: relative;
            }

            .message.user .message-content {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                border-bottom-right-radius: 4px;
            }

            .message.agent .message-content {
                background: white;
                color: #374151;
                border: 1px solid #e5e7eb;
                border-bottom-left-radius: 4px;
            }

            .message.typing .message-content {
                background: #f3f4f6;
                color: #6b7280;
                font-style: italic;
            }

            .message-content p {
                margin: 0;
                line-height: 1.4;
            }

            .time {
                font-size: 11px;
                opacity: 0.7;
                margin-top: 4px;
                display: block;
            }

            .chat-input {
                padding: 20px;
                background: white;
                border-top: 1px solid #e5e7eb;
                display: flex;
                gap: 10px;
                align-items: center;
            }

            .chat-input input {
                flex: 1;
                padding: 12px 16px;
                border: 1px solid #d1d5db;
                border-radius: 25px;
                outline: none;
                font-size: 14px;
                transition: border-color 0.2s ease;
            }

            .chat-input input:focus {
                border-color: #667eea;
            }

            .chat-input button {
                width: 40px;
                height: 40px;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                border: none;
                border-radius: 50%;
                color: white;
                cursor: pointer;
                display: flex;
                align-items: center;
                justify-content: center;
                transition: transform 0.2s ease;
            }

            .chat-input button:hover {
                transform: scale(1.05);
            }

            .chat-input button:active {
                transform: scale(0.95);
            }

            /* Responsive */
            @media (max-width: 480px) {
                .chat-popup {
                    width: calc(100vw - 40px);
                    right: -10px;
                }
            }
        `;

        document.head.appendChild(style);
    }

    // Khởi tạo chatbot
    init() {
        if (this.isInitialized) {
            console.log('Chatbot already initialized');
            return;
        }

        console.log('Initializing chatbot widget...');

        // Tạo HTML
        if (!this.createHTML()) {
            console.error('Failed to create chatbot HTML');
            return;
        }

        // Kiểm tra elements
        if (!this.chatButton || !this.chatPopup || !this.welcomeMessage) {
            console.error('Chatbot elements not found!');
            return;
        }

        console.log('All chatbot elements found!');

        // Load welcome message
        this.loadWelcomeMessage();

        // Thêm event listeners
        this.addEventListeners();

        this.isInitialized = true;
        console.log('Chatbot widget initialized successfully!');
    }

    // Thêm event listeners
    addEventListeners() {
        // Toggle chat popup
        this.chatButton.addEventListener('click', (e) => {
            e.preventDefault();
            console.log('Chat button clicked!');
            
            this.chatPopup.classList.toggle('active');
            
            if (this.chatPopup.classList.contains('active')) {
                this.messageInput.focus();
                console.log('Popup should be visible now');
            } else {
                console.log('Popup should be hidden now');
            }
        });

        // Close chat popup
        this.closeChat.addEventListener('click', () => {
            this.chatPopup.classList.remove('active');
        });

        // Send message
        this.sendButton.addEventListener('click', () => {
            this.sendMessage();
        });

        this.messageInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                this.sendMessage();
            }
        });
    }

    // Gửi tin nhắn
    sendMessage() {
        const message = this.messageInput.value.trim();
        if (message === '') return;

        console.log('Sending message:', message);
        
        // Thêm tin nhắn user
        this.addMessage(message, 'user');
        this.messageInput.value = '';

        // Hiển thị typing indicator
        this.showTypingIndicator();

        // Gửi đến API
        this.sendToAPI(message);
    }

    // Thêm tin nhắn vào chat
    addMessage(text, sender) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${sender}`;
        
        const now = new Date();
        const timeString = now.toLocaleTimeString('vi-VN', { 
            hour: '2-digit', 
            minute: '2-digit' 
        });

        messageDiv.innerHTML = `
            <div class="message-content">
                <p>${text}</p>
                <span class="time">${timeString}</span>
            </div>
        `;

        this.chatMessages.appendChild(messageDiv);
        this.scrollToBottom();
    }

    // Hiển thị typing indicator
    showTypingIndicator() {
        const typingDiv = document.createElement('div');
        typingDiv.className = 'message agent typing';
        typingDiv.id = 'typingIndicator';
        typingDiv.innerHTML = `
            <div class="message-content">
                <p>Đang trả lời...</p>
            </div>
        `;
        this.chatMessages.appendChild(typingDiv);
        this.scrollToBottom();
    }

    // Xóa typing indicator
    removeTypingIndicator() {
        const typingIndicator = document.getElementById('typingIndicator');
        if (typingIndicator) {
            typingIndicator.remove();
        }
    }

    // Gửi tin nhắn đến Spring Boot API
    async sendToAPI(message) {
        try {
            console.log('Sending message to Spring Boot API:', message);
            
            const response = await fetch(this.chatEndpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ message: message })
            });

            const data = await response.json();
            console.log('Spring Boot API response:', data);
            this.removeTypingIndicator();

            if (response.ok) {
                // Hiển thị response từ AI, thay thế \n bằng <br>
                const formattedResponse = data.response.replace(/\n/g, '<br>');
                this.addMessage(formattedResponse, 'agent');
            } else {
                console.error('API error:', data);
                this.addMessage(data.error || 'Có lỗi xảy ra, vui lòng thử lại', 'agent');
            }
        } catch (error) {
            console.error('Error calling Spring Boot API:', error);
            this.removeTypingIndicator();
            this.addMessage('Có lỗi xảy ra, vui lòng thử lại', 'agent');
        }
    }

    // Load welcome message từ Spring Boot API
    async loadWelcomeMessage() {
        try {
            console.log('Loading welcome message from Spring Boot API...');
            const response = await fetch(this.welcomeEndpoint);
            const data = await response.json();
            
            if (response.ok) {
                console.log('Welcome message loaded:', data.message);
                // Chỉ cập nhật nội dung, không thay đổi time
                this.welcomeMessage.innerHTML = data.message.replace(/\n/g, '<br>');
            } else {
                console.error('Failed to load welcome message from API');
                // Fallback welcome message nếu API lỗi
                this.loadFallbackWelcomeMessage();
            }
        } catch (error) {
            console.error('Error loading welcome message:', error);
            // Fallback welcome message nếu có lỗi
            this.loadFallbackWelcomeMessage();
        }
    }

    // Fallback welcome message nếu API không hoạt động
    loadFallbackWelcomeMessage() {
        const welcomeText = `👋 Chào mừng bạn đến với Travelee!

Tôi là AI chatbot hỗ trợ của bạn, được hỗ trợ bởi Google Gemini. Tôi có thể giúp bạn:
✈️ Tìm hiểu về các tour du lịch
📅 Hướng dẫn đặt tour
💰 Tư vấn về giá cả và dịch vụ
🏖️ Giới thiệu các điểm đến hấp dẫn
❓ Giải đáp mọi thắc mắc

Hãy cho tôi biết bạn cần hỗ trợ gì nhé! 😊`;
        
        console.log('Using fallback welcome message');
        this.welcomeMessage.innerHTML = welcomeText.replace(/\n/g, '<br>');
    }

    // Scroll xuống cuối
    scrollToBottom() {
        this.chatMessages.scrollTop = this.chatMessages.scrollHeight;
    }
}

// Tự động khởi tạo khi DOM load xong
document.addEventListener('DOMContentLoaded', function() {
    // Tạo instance chatbot với Spring Boot API endpoints
    const chatbot = new ChatbotWidget({
        chatEndpoint: '/api/chatbot/chat',
        welcomeEndpoint: '/api/chatbot/welcome',
        testEndpoint: '/api/chatbot/test'
    });
    
    // Khởi tạo chatbot
    chatbot.init();
    
    // Export để có thể sử dụng từ bên ngoài nếu cần
    window.ChatbotWidget = ChatbotWidget;
    window.chatbot = chatbot;
});

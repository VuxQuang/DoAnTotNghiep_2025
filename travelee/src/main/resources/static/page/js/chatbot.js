// Simple Chatbot JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('Chatbot script loaded!');
    
    const chatButton = document.getElementById('chatButton');
    const chatPopup = document.getElementById('chatPopup');
    const closeChat = document.getElementById('closeChat');
    const messageInput = document.getElementById('messageInput');
    const sendButton = document.getElementById('sendMessage');
    const chatMessages = document.getElementById('chatMessages');
    const welcomeMessage = document.getElementById('welcomeMessage');

    console.log('Elements found:', {
        chatButton: !!chatButton,
        chatPopup: !!chatPopup,
        closeChat: !!closeChat,
        messageInput: !!messageInput,
        sendButton: !!sendButton,
        chatMessages: !!chatMessages,
        welcomeMessage: !!welcomeMessage
    });

    // Check if elements exist
    if (!chatButton || !chatPopup || !welcomeMessage) {
        console.error('Chatbot elements not found!');
        return;
    }

    console.log('All chatbot elements found!');

    // Load welcome message
    loadWelcomeMessage();

    // Toggle chat popup
    chatButton.addEventListener('click', function(e) {
        e.preventDefault();
        console.log('Chat button clicked!');
        console.log('Current popup classes:', chatPopup.className);
        
        chatPopup.classList.toggle('active');
        
        console.log('After toggle - popup classes:', chatPopup.className);
        console.log('Is active?', chatPopup.classList.contains('active'));
        
        if (chatPopup.classList.contains('active')) {
            messageInput.focus();
            console.log('Popup should be visible now');
        } else {
            console.log('Popup should be hidden now');
        }
    });

    // Close chat popup
    closeChat.addEventListener('click', function() {
        chatPopup.classList.remove('active');
    });

    // Send message
    function sendMessage() {
        const message = messageInput.value.trim();
        if (message === '') return;

        console.log('Sending message:', message);
        
        // Add user message
        addMessage(message, 'user');
        messageInput.value = '';

        // Show typing indicator
        showTypingIndicator();

        // Send to API
        sendToAPI(message);
    }

    // Event listeners
    sendButton.addEventListener('click', sendMessage);
    messageInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });

    // Add message to chat
    function addMessage(text, sender) {
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

        chatMessages.appendChild(messageDiv);
        scrollToBottom();
    }

    // Show typing indicator
    function showTypingIndicator() {
        const typingDiv = document.createElement('div');
        typingDiv.className = 'message agent typing';
        typingDiv.id = 'typingIndicator';
        typingDiv.innerHTML = `
            <div class="message-content">
                <p>Đang trả lời...</p>
            </div>
        `;
        chatMessages.appendChild(typingDiv);
        scrollToBottom();
    }

    // Remove typing indicator
    function removeTypingIndicator() {
        const typingIndicator = document.getElementById('typingIndicator');
        if (typingIndicator) {
            typingIndicator.remove();
        }
    }

    // Send message to API
    async function sendToAPI(message) {
        try {
            console.log('Sending message to API:', message);
            const response = await fetch('/api/chatbot/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ message: message })
            });

            const data = await response.json();
            console.log('API response:', data);
            removeTypingIndicator();

            if (response.ok) {
                // Hiển thị response từ AI, thay thế \n bằng <br>
                const formattedResponse = data.response.replace(/\n/g, '<br>');
                addMessage(formattedResponse, 'agent');
            } else {
                addMessage(data.error || 'Có lỗi xảy ra, vui lòng thử lại', 'agent');
            }
        } catch (error) {
            console.error('Error:', error);
            removeTypingIndicator();
            addMessage('Có lỗi xảy ra, vui lòng thử lại', 'agent');
        }
    }

    // Load welcome message from API
    async function loadWelcomeMessage() {
        try {
            console.log('Loading welcome message...');
            const response = await fetch('/api/chatbot/welcome');
            const data = await response.json();
            
            if (response.ok) {
                console.log('Welcome message loaded:', data.message);
                // Chỉ cập nhật nội dung, không thay đổi time
                welcomeMessage.innerHTML = data.message.replace(/\n/g, '<br>');
            } else {
                console.error('Failed to load welcome message');
                // Giữ lời chào mặc định nếu API lỗi
            }
        } catch (error) {
            console.error('Error loading welcome message:', error);
            // Giữ lời chào mặc định nếu có lỗi
        }
    }

    // Scroll to bottom
    function scrollToBottom() {
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }
});
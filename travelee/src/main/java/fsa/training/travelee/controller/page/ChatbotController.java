package fsa.training.travelee.controller.page;

import fsa.training.travelee.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            if (message == null || message.trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Message cannot be empty");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            log.info("Received message: {}", message);
            String response = chatbotService.chat(message);
            
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("response", response);
            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            log.error("Error processing chat message", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Có lỗi xảy ra, vui lòng thử lại");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Chatbot API is working!");
    }

    @GetMapping("/welcome")
    public ResponseEntity<Map<String, String>> getWelcomeMessage() {
        try {
            String message = chatbotService.getWelcomeMessage();
            Map<String, String> response = new HashMap<>();
            response.put("message", message);
            log.info("Welcome message sent successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting welcome message", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Xin chào! Tôi có thể giúp gì cho bạn? 😊");
            return ResponseEntity.ok(errorResponse);
        }
    }
}

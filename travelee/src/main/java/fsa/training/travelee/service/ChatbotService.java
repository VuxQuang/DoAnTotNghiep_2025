package fsa.training.travelee.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import fsa.training.travelee.entity.Tour;
import fsa.training.travelee.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final ChatLanguageModel geminiChatModel;
    private final TourRepository tourRepository;
    public String getWelcomeMessage() {
        return """
            👋 Chào mừng bạn đến với Travelee!

            Tôi là AI chatbot hỗ trợ của bạn, được hỗ trợ bởi Google Gemini. Tôi có thể giúp bạn:
            ✈️ Tìm hiểu về các tour du lịch
            📅 Hướng dẫn đặt tour
            💰 Tư vấn về giá cả và dịch vụ
            🏖️ Giới thiệu các điểm đến hấp dẫn
            ❓ Giải đáp mọi thắc mắc

            Hãy cho tôi biết bạn cần hỗ trợ gì nhé! 😊
            """;
    }

    public String chat(String message) {
        try {
            log.info("User message: {}", message);

            // Nếu người dùng hỏi về tour đặc sắc
            if (message.toLowerCase().contains("tour") &&
                    (message.toLowerCase().contains("đặc sắc") || message.toLowerCase().contains("gợi ý"))) {

                List<Tour> randomTours = tourRepository.findRandom3Tours();
                if (randomTours.isEmpty()) {
                    return "Hiện tại chưa có tour nào khả dụng. Bạn vui lòng quay lại sau nhé!";
                }

                String toursList = randomTours.stream()
                        .map(t -> "🌍 " + t.getTitle() + " - Giá: " + t.getAdultPrice() + " VND")
                        .collect(Collectors.joining("\n"));

                return "✨ Đây là 3 tour đặc sắc mà bạn có thể quan tâm:\n\n" + toursList;
            }

            // Mặc định dùng Gemini AI
            String systemPrompt = """
                Bạn là một chatbot hỗ trợ khách hàng cho công ty du lịch Travelee.
                Nhiệm vụ của bạn là cung cấp thông tin về các tour du lịch,
                hướng dẫn đặt tour, tư vấn về giá cả, dịch vụ, và giới thiệu các điểm đến hấp dẫn.
                Luôn trả lời bằng tiếng Việt và giữ giọng điệu thân thiện, nhiệt tình.
                Nếu không biết câu trả lời, hãy đề xuất khách hàng liên hệ trực tiếp với chúng tôi.
                """;

            String fullMessage = systemPrompt + "\n\nKhách hàng hỏi: " + message;
            return geminiChatModel.generate(fullMessage);

        } catch (Exception e) {
            log.error("Error generating chatbot response: {}", e.getMessage());
            return "Xin lỗi, tôi gặp sự cố kỹ thuật. Vui lòng thử lại sau hoặc liên hệ trực tiếp với chúng tôi qua hotline: 1900-xxxx.";
        }
    }
}

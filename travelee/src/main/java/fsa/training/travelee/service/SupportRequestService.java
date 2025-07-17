    package fsa.training.travelee.service;

    import fsa.training.travelee.entity.SupportRequest;
    import fsa.training.travelee.entity.SupportStatus;
    import fsa.training.travelee.repository.SupportRequestRepository;
    import fsa.training.travelee.repository.UserRepository;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;

    import java.time.LocalDateTime;
    import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class SupportRequestService {

        private final SupportRequestRepository supportRequestRepository;
        private final UserRepository userRepository;

        public List<SupportRequest> getAllRequests() {
            return supportRequestRepository.findAll();
        }

        public List<SupportRequest> searchByKeyword(String keyword) {
            return supportRequestRepository.findAllByTitleContainingIgnoreCase(keyword);
        }

        public SupportRequest getById(Long id) {
            return supportRequestRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu hỗ trợ"));
        }

        public void replyToSupportRequest(Long requestId, String replyContent) {
            SupportRequest supportRequest = supportRequestRepository.findById(requestId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu hỗ trợ"));

            supportRequest.setReply(replyContent);
            supportRequest.setRepliedAt(LocalDateTime.now());
            supportRequest.setStatus(SupportStatus.RESOLVED);

            supportRequestRepository.save(supportRequest);
        }

        public SupportRequest saveSupportRequest(SupportRequest request) {
            // Kiểm tra nếu người dùng đã đăng nhập (có thông tin người dùng hệ thống)
            if (request.getUser() != null) {
                // Nếu người dùng đã đăng nhập, xoá thông tin người lạ (senderName, senderEmail, senderPhone)
                request.setSenderName(null);
                request.setSenderEmail(null);
                request.setSenderPhone(null);
            } else {
                request.setSenderName(request.getSenderName());
                request.setSenderEmail(request.getSenderEmail());
                request.setSenderPhone(request.getSenderPhone());
            }

            // Gán trạng thái ban đầu là PENDING
            request.setStatus(SupportStatus.PENDING);

            // Gán thời gian tạo yêu cầu (ngày gửi)
            request.setCreatedAt(LocalDateTime.now());

            // Lưu yêu cầu vào cơ sở dữ liệu
            return supportRequestRepository.save(request);
        }

    }

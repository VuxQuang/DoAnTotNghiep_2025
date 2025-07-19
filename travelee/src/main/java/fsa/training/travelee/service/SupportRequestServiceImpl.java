    package fsa.training.travelee.service;

    import fsa.training.travelee.entity.SupportRequest;
    import fsa.training.travelee.entity.SupportStatus;
    import fsa.training.travelee.repository.SupportRequestRepository;
    import lombok.RequiredArgsConstructor;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.data.domain.Pageable;
    import org.springframework.stereotype.Service;

    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.Optional;

    @Service
    @RequiredArgsConstructor
    public class SupportRequestServiceImpl implements SupportRequestService{

        private final SupportRequestRepository supportRequestRepository;

        @Override
        public List<SupportRequest> getAllRequests() {
            return supportRequestRepository.findAll();
        }

        @Override
        public List<SupportRequest> findAllByUserId(Long userId) {
            return supportRequestRepository.findAllByUser_Id(userId);
        }


        @Override
        public Page<SupportRequest> getSupportRequestsPage(String keyword, int page, int size) {
            Pageable pageable = PageRequest.of(page, size);
            if (keyword != null && !keyword.isEmpty()) {
                return supportRequestRepository.findAllByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                        keyword, keyword, pageable);
            } else {
                return supportRequestRepository.findAll(pageable);
            }
        }


        @Override
        public SupportRequest getById(Long id) {
            return supportRequestRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu hỗ trợ"));
        }

        @Override
        public void replyToSupportRequest(Long requestId, String replyContent,String replyBy) {
            SupportRequest supportRequest = supportRequestRepository.findById(requestId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu hỗ trợ"));

            supportRequest.setReply(replyContent);
            supportRequest.setRepliedAt(LocalDateTime.now());
            supportRequest.setStatus(SupportStatus.RESOLVED);
            supportRequest.setReplyBy(replyBy);

            supportRequestRepository.save(supportRequest);
        }

        @Override
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
            System.out.println("==> [Service] Trước khi set: reply = " + request.getReply());
            request.setReply(null);
            System.out.println("==> [Service] Sau khi set: reply = " + request.getReply());

            // Gán thời gian tạo yêu cầu (ngày gửi)
            request.setCreatedAt(LocalDateTime.now());
            request.setContent(request.getContent());
            request.setTitle(request.getTitle());

            // Lưu yêu cầu vào cơ sở dữ liệu
            return supportRequestRepository.save(request);
        }

    }

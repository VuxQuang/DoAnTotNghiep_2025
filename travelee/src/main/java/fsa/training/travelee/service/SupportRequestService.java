package fsa.training.travelee.service;

import fsa.training.travelee.entity.SupportRequest;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface SupportRequestService {

    List<SupportRequest> getAllRequests();

    List<SupportRequest> findAllByUserId(Long userId);

    Page<SupportRequest> getSupportRequestsPage(String keyword, int page, int size);

    SupportRequest getById(Long id);

    void replyToSupportRequest(Long requestId, String replyContent, String replyBy);

    SupportRequest saveSupportRequest(SupportRequest request);


}

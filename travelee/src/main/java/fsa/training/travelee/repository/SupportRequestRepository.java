package fsa.training.travelee.repository;

import fsa.training.travelee.entity.SupportRequest;
import fsa.training.travelee.entity.SupportStatus;
import fsa.training.travelee.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {

    List<SupportRequest> findAll();

    Optional<SupportRequest> findById(Long id);

    Page<SupportRequest> findAllByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String title, String content, Pageable pageable);
    List<SupportRequest> findAllByUser_Id(Long userId);
}

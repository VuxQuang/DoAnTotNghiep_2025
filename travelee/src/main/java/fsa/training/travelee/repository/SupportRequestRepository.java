package fsa.training.travelee.repository;

import fsa.training.travelee.entity.SupportRequest;
import fsa.training.travelee.entity.SupportStatus;
import fsa.training.travelee.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {

    List<SupportRequest> findAll();

    Optional<SupportRequest> findById(Long id);

    List<SupportRequest> findAllByTitleContainingIgnoreCase(String keyword);
}

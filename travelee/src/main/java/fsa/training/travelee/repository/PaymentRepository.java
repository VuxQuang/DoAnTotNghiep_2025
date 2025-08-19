package fsa.training.travelee.repository;

import fsa.training.travelee.entity.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByPaymentCode(String paymentCode);

	@Query("SELECT p FROM Payment p JOIN p.booking b WHERE b.bookingCode = :bookingCode")
	Optional<Payment> findByBookingCode(@Param("bookingCode") String bookingCode);
}



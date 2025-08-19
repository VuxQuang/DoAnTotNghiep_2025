package fsa.training.travelee.service;

import fsa.training.travelee.dto.payment.SepayWebhookDto;
import fsa.training.travelee.entity.booking.Booking;

public interface PaymentService {
	Booking handleSepayWebhook(SepayWebhookDto payload);
}



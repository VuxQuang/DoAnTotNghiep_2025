package fsa.training.travelee.controller.page;

import fsa.training.travelee.dto.booking.BookingRequestDto;
import fsa.training.travelee.dto.promotion.PromotionValidationResponse;
import fsa.training.travelee.entity.Tour;
import fsa.training.travelee.entity.TourSchedule;
import fsa.training.travelee.entity.User;
import fsa.training.travelee.service.PromotionService;
import fsa.training.travelee.service.TourClientService;
import fsa.training.travelee.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequestMapping("/page/promotions")
@RequiredArgsConstructor
@Slf4j
public class PromotionPageController {

    private final PromotionService promotionService;
    private final TourClientService tourClientService;
    private final UserService userService;

    @PostMapping("/apply")
    public String applyPromotionCode(@RequestParam("promotionCode") String promotionCode,
                                     @ModelAttribute BookingRequestDto bookingRequest,
                                     Model model) {
        // Lấy lại tour và schedule như trang booking form
        Tour tour = tourClientService.getTourById(bookingRequest.getTourId());
        if (tour == null) {
            return "redirect:/page/tours/newest";
        }

        TourSchedule selectedSchedule = null;
        if (bookingRequest.getScheduleId() != null) {
            selectedSchedule = tour.getSchedules().stream()
                    .filter(s -> s.getId().equals(bookingRequest.getScheduleId()))
                    .findFirst()
                    .orElse(null);
        }
        if (selectedSchedule == null && !tour.getSchedules().isEmpty()) {
            selectedSchedule = tour.getSchedules().iterator().next();
        }

        User user = userService.getCurrentUser();

        // Tính subtotal server-side
        BigDecimal adultUnitPrice = (selectedSchedule != null && selectedSchedule.getSpecialPrice() != null)
                ? selectedSchedule.getSpecialPrice()
                : tour.getAdultPrice();
        BigDecimal childUnitPrice = tour.getChildPrice();

        int adultCount = bookingRequest.getAdultCount() != null ? bookingRequest.getAdultCount() : 1;
        int childCount = bookingRequest.getChildCount() != null ? bookingRequest.getChildCount() : 0;

        BigDecimal subtotal = adultUnitPrice.multiply(BigDecimal.valueOf(adultCount))
                .add(childUnitPrice.multiply(BigDecimal.valueOf(childCount)));

        // Validate promotion
        PromotionValidationResponse validation = promotionService
                .validatePromotionCode(promotionCode, tour.getId(), subtotal);

        // Chuẩn bị model
        model.addAttribute("tour", tour);
        model.addAttribute("selectedSchedule", selectedSchedule);
        model.addAttribute("user", user);
        model.addAttribute("bookingRequest", bookingRequest);

        model.addAttribute("subtotal", subtotal);
        if (validation.isSuccess()) {
            model.addAttribute("appliedPromotion", validation.getPromotion());
            model.addAttribute("discountAmount", validation.getDiscountAmount());
            model.addAttribute("finalTotal", subtotal.subtract(validation.getDiscountAmount()));
            model.addAttribute("promotionMessage", validation.getMessage());
            model.addAttribute("promotionSuccess", true);

            // Giữ lại vào form để submit sang createBooking
            bookingRequest.setPromotionCode(validation.getPromotion().getCode());
            bookingRequest.setDiscountAmount(validation.getDiscountAmount());
        } else {
            model.addAttribute("promotionMessage", validation.getMessage());
            model.addAttribute("promotionSuccess", false);
        }

        return "page/booking/booking-form";
    }
}



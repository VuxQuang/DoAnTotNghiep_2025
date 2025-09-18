package fsa.training.travelee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityDto {
    private String type; // "USER_REGISTER", "BOOKING_CREATED", "BOOKING_COMPLETED", "REVIEW_CREATED"
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private String icon; // FontAwesome icon class
    private String timeAgo; // "2 phút trước", "1 giờ trước", etc.
}

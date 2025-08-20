    package fsa.training.travelee.entity;

    import jakarta.persistence.*;
    import lombok.*;
    import org.springframework.format.annotation.DateTimeFormat;

    import java.time.LocalDateTime;

    @Entity
    @Table(name = "support_requests")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class SupportRequest {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String title;               // Tiêu đề yêu cầu
        private String content;             // Nội dung yêu cầu

        @Column(length = 1000)
        private String reply;               // Nội dung trả lời (nếu có)

        @Enumerated(EnumType.STRING)
        private SupportStatus status;       // PENDING, RESOLVED...

        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime createdAt;
        private LocalDateTime repliedAt;

        private String replyBy;
        // --- Nếu là người dùng hệ thống ---
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = true)
        private User user;

        // --- Nếu là người lạ gửi (khách không đăng nhập) ---
        private String senderName;
        private String senderEmail;
        private String senderPhone;

        @PrePersist
        public void prePersist() {
            createdAt = LocalDateTime.now();
            if (status == null) status = SupportStatus.PENDING;
        }

        @PreUpdate
        public void preUpdate() {
            if (reply != null && !reply.trim().isEmpty()) {
                repliedAt = LocalDateTime.now();
                status = SupportStatus.RESOLVED;
            }
        }
    }

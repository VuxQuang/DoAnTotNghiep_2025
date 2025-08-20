package fsa.training.travelee.entity;

import fsa.training.travelee.entity.promotion.Promotion;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String title;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String highlights;

    @Column(name = "adult_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal adultPrice;

    @Column(name = "child_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal childPrice;

    private Integer duration;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String departure;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String destination;

    private Integer maxParticipants;
    private Integer availableSlots;

    @Column(nullable = false)
    private String status;

    private Boolean featured;

    @Column(name = "is_hot")
    private Boolean isHot;
    private Boolean hasPromotion;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String includes;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String excludes;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String terms;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToMany
    @JoinTable(
            name = "promotion_tours",
            joinColumns = @JoinColumn(name = "tour_id"),
            inverseJoinColumns = @JoinColumn(name = "promotion_id")
    )
    @Builder.Default
    private Set<Promotion> promotions = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TourImage> images = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TourItinerary> itineraries = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TourSchedule> schedules = new HashSet<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

package fsa.training.travelee.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tour_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "NVARCHAR(500)")
    private String imageUrl;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String caption;

    private Boolean isPrimary;
    private Integer sortOrder;

    @ManyToOne
    @JoinColumn(name = "tour_id")
    private Tour tour;
}

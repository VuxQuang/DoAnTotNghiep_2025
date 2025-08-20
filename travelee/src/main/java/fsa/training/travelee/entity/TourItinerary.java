package fsa.training.travelee.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tour_itineraries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourItinerary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer dayNumber;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String title;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String activities;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String meals;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String accommodation;

    @ManyToOne
    @JoinColumn(name = "tour_id")
    private Tour tour;
}

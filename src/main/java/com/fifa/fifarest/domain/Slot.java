package com.fifa.fifarest.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    // Nullable so existing rows created before per-slot pricing (which fall back to
    // Court.pricePerSlot) remain valid; every slot created going forward has this set.
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    // Only set by bulk month-generation (the owner's own label for the time window that
    // produced this slot); null for manually-added slots, which fall back to a best-guess
    // classification by start time (see TimeOfDay.fromStartTime).
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TimeOfDay category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SlotStatus status = SlotStatus.AVAILABLE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}

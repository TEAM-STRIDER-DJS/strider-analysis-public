package com.strider.strider_analysis.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "health_data",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "steps_today", nullable = false)
    @Builder.Default
    private int stepsToday = 0;

    @Column(name = "steps_goal", nullable = false)
    @Builder.Default
    private int stepsGoal = 10000;

    @Column(name = "active_minutes", nullable = false)
    @Builder.Default
    private int activeMinutes = 0;

    @Column(name = "calories", nullable = false)
    @Builder.Default
    private int calories = 0;

    @UpdateTimestamp
    @Column(name = "last_sync_at", nullable = false)
    private LocalDateTime lastSyncAt;
}

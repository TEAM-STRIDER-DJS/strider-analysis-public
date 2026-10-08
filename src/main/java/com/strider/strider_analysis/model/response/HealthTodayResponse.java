package com.strider.strider_analysis.model.response;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record HealthTodayResponse(
        String userId,
        LocalDate date,
        int stepsToday,
        int stepsGoal,
        int activeMinutes,
        int calories,
        LocalDateTime lastSyncAt
) {}

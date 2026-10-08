package com.strider.strider_analysis.model.request;

import java.time.LocalDate;

public record HealthSyncRequest(
        String userId,
        LocalDate date,
        int stepsToday,
        int stepsGoal,
        int activeMinutes,
        int calories
) {}

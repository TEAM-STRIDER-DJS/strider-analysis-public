package com.strider.strider_analysis.model.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record HealthWeeklyResponse(
        String userId,
        int weeklyAvg,
        int lastWeekAvg,
        List<DailyRecord> daily
) {
    @Builder
    public record DailyRecord(
            LocalDate date,
            int stepsToday,
            int stepsGoal,
            int activeMinutes,
            int calories
    ) {}
}

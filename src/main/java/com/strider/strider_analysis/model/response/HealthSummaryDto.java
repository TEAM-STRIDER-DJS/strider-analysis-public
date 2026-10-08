package com.strider.strider_analysis.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthSummaryDto {
    private String userId;
    private int stepsToday;
    private int stepsGoal;
    private int weeklyAvg;
    private int lastWeekAvg;
    private int activeMinutes;
    private int calories;
    private int myPostCount;
    private int totalLikes;
}

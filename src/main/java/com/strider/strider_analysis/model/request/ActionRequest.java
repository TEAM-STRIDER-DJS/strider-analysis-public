package com.strider.strider_analysis.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionRequest {
    private String userId;
    private int stepsToday;
    private int activeMinutes;
    private int currentHour;
    private List<String> trendingActivities;
}

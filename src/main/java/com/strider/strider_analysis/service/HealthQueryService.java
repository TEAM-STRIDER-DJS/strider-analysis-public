package com.strider.strider_analysis.service;

import com.strider.strider_analysis.model.response.HealthSummaryDto;
import com.strider.strider_analysis.model.response.HealthWeeklyResponse;
import com.strider.strider_analysis.repository.HealthDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Slf4j
@RequiredArgsConstructor
public class HealthQueryService {

    private final HealthDataRepository healthDataRepository;

    public HealthSummaryDto getSummary(String userId) {
        LocalDate today = LocalDate.now();

        var todayData = healthDataRepository.findByUserIdAndDate(userId, today);
        var weekly = healthDataRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(userId, today.minusDays(6), today);
        var lastWeek = healthDataRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(userId, today.minusDays(13), today.minusDays(7));

        int weeklyAvg   = weekly.isEmpty()   ? 0 : (int) weekly.stream().mapToInt(d -> d.getStepsToday()).average().orElse(0);
        int lastWeekAvg = lastWeek.isEmpty() ? 0 : (int) lastWeek.stream().mapToInt(d -> d.getStepsToday()).average().orElse(0);

        return todayData.map(d -> HealthSummaryDto.builder()
                        .userId(userId)
                        .stepsToday(d.getStepsToday())
                        .stepsGoal(d.getStepsGoal())
                        .weeklyAvg(weeklyAvg)
                        .lastWeekAvg(lastWeekAvg)
                        .activeMinutes(d.getActiveMinutes())
                        .calories(d.getCalories())
                        .build())
                .orElse(HealthSummaryDto.builder()
                        .userId(userId)
                        .stepsGoal(10000)
                        .weeklyAvg(weeklyAvg)
                        .lastWeekAvg(lastWeekAvg)
                        .build());
    }
}

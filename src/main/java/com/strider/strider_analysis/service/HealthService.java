package com.strider.strider_analysis.service;

import com.strider.strider_analysis.model.entity.HealthData;
import com.strider.strider_analysis.model.request.HealthSyncRequest;
import com.strider.strider_analysis.model.response.HealthTodayResponse;
import com.strider.strider_analysis.model.response.HealthWeeklyResponse;
import com.strider.strider_analysis.repository.HealthDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class HealthService {

    private final HealthDataRepository healthDataRepository;

    public HealthTodayResponse getToday(String userId) {
        HealthData data = healthDataRepository
                .findByUserIdAndDate(userId, LocalDate.now())
                .orElse(HealthData.builder().userId(userId).date(LocalDate.now()).build());

        return HealthTodayResponse.builder()
                .userId(data.getUserId())
                .date(data.getDate())
                .stepsToday(data.getStepsToday())
                .stepsGoal(data.getStepsGoal())
                .activeMinutes(data.getActiveMinutes())
                .calories(data.getCalories())
                .lastSyncAt(data.getLastSyncAt())
                .build();
    }

    public HealthWeeklyResponse getWeekly(String userId) {
        LocalDate today = LocalDate.now();
        List<HealthData> thisWeek = healthDataRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(userId, today.minusDays(6), today);
        List<HealthData> lastWeek = healthDataRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(userId, today.minusDays(13), today.minusDays(7));

        int weeklyAvg  = average(thisWeek);
        int lastWeekAvg = average(lastWeek);

        List<HealthWeeklyResponse.DailyRecord> daily = thisWeek.stream()
                .map(d -> HealthWeeklyResponse.DailyRecord.builder()
                        .date(d.getDate())
                        .stepsToday(d.getStepsToday())
                        .stepsGoal(d.getStepsGoal())
                        .activeMinutes(d.getActiveMinutes())
                        .calories(d.getCalories())
                        .build())
                .toList();

        return HealthWeeklyResponse.builder()
                .userId(userId)
                .weeklyAvg(weeklyAvg)
                .lastWeekAvg(lastWeekAvg)
                .daily(daily)
                .build();
    }

    @Transactional
    public void sync(HealthSyncRequest req) {
        HealthData data = healthDataRepository
                .findByUserIdAndDate(req.userId(), req.date())
                .orElse(HealthData.builder()
                        .userId(req.userId())
                        .date(req.date())
                        .build());

        data.setStepsToday(req.stepsToday());
        data.setStepsGoal(req.stepsGoal());
        data.setActiveMinutes(req.activeMinutes());
        data.setCalories(req.calories());

        healthDataRepository.save(data);
    }

    private int average(List<HealthData> list) {
        if (list.isEmpty()) return 0;
        return (int) list.stream().mapToInt(HealthData::getStepsToday).average().orElse(0);
    }
}

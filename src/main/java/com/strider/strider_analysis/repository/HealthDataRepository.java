package com.strider.strider_analysis.repository;

import com.strider.strider_analysis.model.entity.HealthData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HealthDataRepository extends JpaRepository<HealthData, Long> {

    Optional<HealthData> findByUserIdAndDate(String userId, LocalDate date);

    List<HealthData> findByUserIdAndDateBetweenOrderByDateDesc(String userId, LocalDate start, LocalDate end);
}

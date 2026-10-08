package com.strider.strider_analysis.repository;

import com.strider.strider_analysis.model.entity.ActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionLogRepository extends JpaRepository<ActionLog, Long> {
}

package com.strider.strider_analysis.controller;

import com.strider.strider_analysis.model.request.HealthSyncRequest;
import com.strider.strider_analysis.model.response.HealthTodayResponse;
import com.strider.strider_analysis.model.response.HealthWeeklyResponse;
import com.strider.strider_analysis.service.HealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/analysis/health")
public class HealthRestController {

    private final HealthService healthService;

    // GET /api/v1/analysis/health/today?userId=
    @GetMapping("/today")
    public ResponseEntity<HealthTodayResponse> getToday(@RequestParam String userId) {
        return ResponseEntity.ok(healthService.getToday(userId));
    }

    // GET /api/v1/analysis/health/weekly?userId=
    @GetMapping("/weekly")
    public ResponseEntity<HealthWeeklyResponse> getWeekly(@RequestParam String userId) {
        return ResponseEntity.ok(healthService.getWeekly(userId));
    }

    // POST /api/v1/analysis/health/sync
    @PostMapping("/sync")
    public ResponseEntity<Void> sync(@RequestBody HealthSyncRequest req) {
        healthService.sync(req);
        return ResponseEntity.ok().build();
    }
}

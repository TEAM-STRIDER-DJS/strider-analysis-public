package com.strider.strider_analysis.controller;

import com.strider.strider_analysis.model.request.ActionRequest;
import com.strider.strider_analysis.model.response.AiActionsResponse;
import com.strider.strider_analysis.model.response.AiReportResponse;
import com.strider.strider_analysis.service.AiService;
import com.strider.strider_analysis.service.FeedQueryService;
import com.strider.strider_analysis.service.HealthQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/analysis/ai")
public class AiRestController {

    private final AiService aiService;
    private final HealthQueryService healthQueryService;
    private final FeedQueryService feedQueryService;

    // GET /api/v1/analysis/ai/report?userId=
    @GetMapping("/report")
    public ResponseEntity<AiReportResponse> getReport(
            @RequestParam String userId,
            @RequestHeader HttpHeaders headers) throws Exception {
        var health = healthQueryService.getSummary(userId);
        var feed = feedQueryService.getWeeklySummary(userId, headers);
        health.setMyPostCount(feed.postCount());
        health.setTotalLikes(feed.totalLikes());
        return ResponseEntity.ok(aiService.generateReport(health));
    }

    // POST /api/v1/analysis/ai/actions
    @PostMapping("/actions")
    public ResponseEntity<AiActionsResponse> getActions(
            @RequestBody ActionRequest req,
            @RequestHeader HttpHeaders headers) throws Exception {
        req.setTrendingActivities(feedQueryService.getTrendingActivities(req.getUserId(), headers));
        req.setCurrentHour(LocalTime.now().getHour());
        return ResponseEntity.ok(aiService.generateActions(req));
    }

    // POST /api/v1/analysis/ai/actions/{id}/complete
    @PostMapping("/actions/{id}/complete")
    public ResponseEntity<Void> completeAction(@PathVariable String id, @RequestParam String userId) {
        aiService.logActionComplete(userId, id);
        return ResponseEntity.ok().build();
    }
}

package com.strider.strider_analysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strider.strider_analysis.infra.GeminiClient;
import com.strider.strider_analysis.model.entity.ActionLog;
import com.strider.strider_analysis.model.request.ActionRequest;
import com.strider.strider_analysis.model.response.AiActionsResponse;
import com.strider.strider_analysis.model.response.AiReportResponse;
import com.strider.strider_analysis.model.response.HealthSummaryDto;
import com.strider.strider_analysis.repository.ActionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiService {

    private final GeminiClient geminiClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ActionLogRepository actionLogRepository;

    // ── AI 리포트 (S1) ────────────────────────────────────────────
    public AiReportResponse generateReport(HealthSummaryDto data) throws Exception {
        String cacheKey    = "ai:report:" + data.getUserId() + ":" + LocalDate.now();
        String backupKey   = "ai:report:last:" + data.getUserId();

        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) return objectMapper.readValue(cached, AiReportResponse.class);

        try {
            String raw = geminiClient.generate(reportSystemPrompt(), reportUserPrompt(data));
            AiReportResponse result = objectMapper.readValue(raw, AiReportResponse.class);
            redisTemplate.opsForValue().set(cacheKey, raw, 25, TimeUnit.HOURS);
            redisTemplate.opsForValue().set(backupKey, raw, 72, TimeUnit.HOURS); // 백업 캐시
            return result;
        } catch (Exception e) {
            log.warn("[AiService] report 생성 실패, 백업 캐시 조회: {}", e.getMessage());
            String backup = redisTemplate.opsForValue().get(backupKey);
            if (backup != null) return objectMapper.readValue(backup, AiReportResponse.class);
            log.warn("[AiService] 백업 캐시 없음, 기본값 반환");
            return defaultReport();
        }
    }

    // ── AI 추천 액션 (S3) ─────────────────────────────────────────
    public AiActionsResponse generateActions(ActionRequest data) throws Exception {
        String cacheKey  = "ai:actions:v2:" + data.getUserId() + ":" + LocalDate.now();
        String backupKey = "ai:actions:last:" + data.getUserId();

        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) return objectMapper.readValue(cached, AiActionsResponse.class);

        try {
            String raw = geminiClient.generate(actionsSystemPrompt(), actionsUserPrompt(data));
            AiActionsResponse result = objectMapper.readValue(raw, AiActionsResponse.class);
            redisTemplate.opsForValue().set(cacheKey, raw, 25, TimeUnit.HOURS); // 6h → 25h
            redisTemplate.opsForValue().set(backupKey, raw, 72, TimeUnit.HOURS); // 백업 캐시
            return result;
        } catch (Exception e) {
            log.warn("[AiService] actions 생성 실패, 백업 캐시 조회: {}", e.getMessage());
            String backup = redisTemplate.opsForValue().get(backupKey);
            if (backup != null) return objectMapper.readValue(backup, AiActionsResponse.class);
            log.warn("[AiService] 백업 캐시 없음, 기본값 반환");
            return defaultActions();
        }
    }

    // ── 액션 완료 로그 ────────────────────────────────────────────
    public void logActionComplete(String userId, String actionId) {
        actionLogRepository.save(
                ActionLog.builder()
                        .userId(userId)
                        .actionId(actionId)
                        .build()
        );
    }

    // ── 기본값 fallback ───────────────────────────────────────────
    private AiReportResponse defaultReport() {
        return new AiReportResponse(
            "오늘도 건강한 하루를 보내고 있어요. 꾸준한 활동이 큰 힘이 됩니다.",
            "하루 30분 이상 걷기를 목표로 해보세요.",
            "작은 습관이 큰 변화를 만들어요. 오늘도 파이팅!",
            "stable"
        );
    }

    private AiActionsResponse defaultActions() {
        return new AiActionsResponse(List.of(
            new AiActionsResponse.ActionItem("1", "10분 산책하기", "가볍게 몸을 움직여요", 10),
            new AiActionsResponse.ActionItem("2", "스트레칭", "근육을 풀어주세요", 5),
            new AiActionsResponse.ActionItem("3", "물 한 잔 마시기", "수분을 보충해요", 1)
        ));
    }

    // ── 프롬프트 ─────────────────────────────────────────────────
    private String reportSystemPrompt() {
        return """
                당신은 건강 코치입니다. 반드시 아래 JSON만 응답하세요.
                마크다운, 코드블록, 추가 텍스트 절대 금지.
                {
                  "summary":    "2문장 이내 한국어 요약",
                  "advice":     "1문장 개선 조언",
                  "motivation": "1문장 동기부여 문구",
                  "trend":      "up | down | stable"
                }
                """;
    }

    private String reportUserPrompt(HealthSummaryDto d) {
        return String.format("""
                오늘 걸음: %d / 목표: %d
                이번 주 평균: %d / 지난 주 평균: %d
                활동 시간: %d분 / 칼로리: %dkcal
                이번 주 피드 게시 수: %d / 받은 좋아요: %d
                """,
                d.getStepsToday(), d.getStepsGoal(),
                d.getWeeklyAvg(), d.getLastWeekAvg(),
                d.getActiveMinutes(), d.getCalories(),
                d.getMyPostCount(), d.getTotalLikes()
        );
    }

    private String actionsSystemPrompt() {
        return """
                당신은 건강 코치입니다. 반드시 아래 JSON만 응답하세요.
                마크다운, 코드블록, 추가 텍스트 절대 금지.
                title과 description은 반드시 한국어로 작성하세요.
                {
                  "actions": [
                    {"id":"1","title":"10자 이내 한국어 제목","description":"15자 이내 한국어 서브 문구","durationMin":10},
                    {"id":"2",...},
                    {"id":"3",...}
                  ]
                }
                """;
    }

    private String actionsUserPrompt(ActionRequest d) {
        return String.format("""
                오늘 걸음: %d / 활동 시간: %d분
                현재 시각: %d시
                팔로우 피드 인기 활동: %s
                """,
                d.getStepsToday(), d.getActiveMinutes(),
                d.getCurrentHour(),
                String.join(", ", d.getTrendingActivities())
        );
    }
}

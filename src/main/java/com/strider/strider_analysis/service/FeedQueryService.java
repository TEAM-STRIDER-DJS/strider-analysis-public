package com.strider.strider_analysis.service;

import com.strider.strider_analysis.infra.FeedClient;
import com.strider.strider_analysis.model.response.FeedPostSummary;
import com.strider.strider_analysis.model.response.FeedSummaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedQueryService {

    private final FeedClient feedClient;

    public FeedSummaryDto getWeeklySummary(String userId, HttpHeaders headers) {
        try {
            List<FeedPostSummary> posts = feedClient.getUserFeeds(headers, userId, 100);

            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            List<FeedPostSummary> weekly = posts.stream()
                    .filter(p -> p.createdAt() != null && p.createdAt().isAfter(weekAgo))
                    .toList();

            int postCount  = weekly.size();
            int totalLikes = weekly.stream().mapToInt(p -> p.likeCount() != null ? p.likeCount() : 0).sum();
            List<String> trending = extractTrending(weekly, 3);

            return new FeedSummaryDto(postCount, totalLikes, trending);
        } catch (Exception e) {
            log.warn("strider-feed 호출 실패, 빈 피드 데이터로 대체: {}", e.getMessage());
            return new FeedSummaryDto(0, 0, List.of());
        }
    }

    public List<String> getTrendingActivities(String userId, HttpHeaders headers) {
        try {
            List<FeedPostSummary> posts = feedClient.getFollowFeeds(headers, 50);
            return extractTrending(posts, 3);
        } catch (Exception e) {
            log.warn("strider-feed 팔로우 피드 호출 실패: {}", e.getMessage());
            return List.of();
        }
    }

    private List<String> extractTrending(List<FeedPostSummary> posts, int limit) {
        return posts.stream()
                .filter(p -> p.exerciseId() != null)
                .collect(Collectors.groupingBy(FeedPostSummary::exerciseId, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }
}

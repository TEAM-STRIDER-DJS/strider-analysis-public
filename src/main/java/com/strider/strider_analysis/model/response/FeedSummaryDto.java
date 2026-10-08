package com.strider.strider_analysis.model.response;

import java.util.List;

public record FeedSummaryDto(
        int postCount,
        int totalLikes,
        List<String> trendingActivities
) {}

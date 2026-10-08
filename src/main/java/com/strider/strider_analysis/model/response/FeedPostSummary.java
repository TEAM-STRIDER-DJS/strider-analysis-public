package com.strider.strider_analysis.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FeedPostSummary(
        String feedPostId,
        String exerciseId,
        Integer likeCount,
        LocalDateTime createdAt
) {}

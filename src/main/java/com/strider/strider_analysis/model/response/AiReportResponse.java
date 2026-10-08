package com.strider.strider_analysis.model.response;

public record AiReportResponse(
        String summary,
        String advice,
        String motivation,
        String trend       // "up" | "down" | "stable"
) {}

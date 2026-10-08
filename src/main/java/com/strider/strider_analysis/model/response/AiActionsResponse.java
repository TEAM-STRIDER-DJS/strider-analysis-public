package com.strider.strider_analysis.model.response;

import java.util.List;

public record AiActionsResponse(List<ActionItem> actions) {

    public record ActionItem(
            String id,
            String title,
            String description,
            int durationMin
    ) {}
}

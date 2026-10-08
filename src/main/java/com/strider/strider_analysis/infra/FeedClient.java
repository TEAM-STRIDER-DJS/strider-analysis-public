package com.strider.strider_analysis.infra;

import com.strider.strider_analysis.model.response.FeedPostSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "feedClient", url = "http://strider-feed:8004")
public interface FeedClient {

    @GetMapping("/api/v1/feed/user/{feedUserId}")
    List<FeedPostSummary> getUserFeeds(
            @RequestHeader HttpHeaders headers,
            @PathVariable String feedUserId,
            @RequestParam(defaultValue = "100") int size
    );

    @GetMapping("/api/v1/feed/follow")
    List<FeedPostSummary> getFollowFeeds(
            @RequestHeader HttpHeaders headers,
            @RequestParam(defaultValue = "50") int size
    );
}

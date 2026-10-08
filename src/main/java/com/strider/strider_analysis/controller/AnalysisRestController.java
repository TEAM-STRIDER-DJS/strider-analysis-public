package com.strider.strider_analysis.controller;

import com.strider.strider_analysis.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/analysis")
public class AnalysisRestController {
    private final AnalysisService analysisService;
}

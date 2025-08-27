package com.zerocode.langGraph4j.service;

import com.zerocode.langGraph4j.image.state.ImageCollectorPlan;
import dev.langchain4j.service.SystemMessage;

/**
 * 图片收集服务
 */
public interface ImageCollectorPlanService {

    @SystemMessage(fromResource = "prompt/image-collector-plan-system-prompt.txt")
    ImageCollectorPlan collectImages(String userPrompt);

}

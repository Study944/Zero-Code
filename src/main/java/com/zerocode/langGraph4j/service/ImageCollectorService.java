package com.zerocode.langGraph4j.service;

import com.zerocode.langGraph4j.image.state.ImageResource;
import dev.langchain4j.service.SystemMessage;

import java.util.List;

/**
 * 图片收集服务
 */
public interface ImageCollectorService {

    @SystemMessage(fromResource = "prompt/image-collector-system-prompt.txt")
    String collectImages(String userPrompt);

}

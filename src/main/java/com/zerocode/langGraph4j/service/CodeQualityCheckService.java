package com.zerocode.langGraph4j.service;

import com.zerocode.langGraph4j.image.state.QualityResult;
import dev.langchain4j.service.SystemMessage;

public interface CodeQualityCheckService {


    @SystemMessage(fromResource = "prompt/code-quality-check-system-prompt.txt")
    QualityResult checkCodeQuality(String userPrompt);

}

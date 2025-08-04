package com.zerocode.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiGeneratorServiceFactoryTest {

    @Resource
    private AiGeneratorService aiGeneratorService;

    @Test
    void AiGenerateHTMLService() {
        HtmlCodeResult res = aiGeneratorService.generateHtml("生成一个简单的登录页面");
        Assertions.assertNotNull(res);
    }

    @Test
    void AiGenerateMultiFileService() {
        MultiFileCodeResult res = aiGeneratorService.generateMultiFile("生成一个简单的登录页面，代码控制在20行");
        Assertions.assertNotNull(res);
    }

}
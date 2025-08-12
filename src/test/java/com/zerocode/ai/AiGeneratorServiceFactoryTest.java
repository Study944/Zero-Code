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
        HtmlCodeResult res = aiGeneratorService.generateHtml(1L,"你好，不要生成代码");
        HtmlCodeResult res2 = aiGeneratorService.generateHtml(1L,"我上一句话说了什么");
        Assertions.assertNotNull(res);
        HtmlCodeResult res3 = aiGeneratorService.generateHtml(2L,"你好，不要生成代码");
        HtmlCodeResult res4 = aiGeneratorService.generateHtml(2L,"我上一句话说了什么");
    }

    @Test
    void AiGenerateMultiFileService() {
        MultiFileCodeResult res = aiGeneratorService.generateMultiFile("生成一个简单的登录页面，代码控制在20行");
        Assertions.assertNotNull(res);
    }

}
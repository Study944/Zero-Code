package com.zerocode.ai;

import com.zerocode.ai.aiservices.AiGeneratorService;
import com.zerocode.ai.entity.HtmlCodeResult;
import com.zerocode.ai.entity.MultiFileCodeResult;
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
        HtmlCodeResult res = aiGeneratorService.generateHtml("你好，不要生成代码");
        HtmlCodeResult res2 = aiGeneratorService.generateHtml("我上一句话说了什么");
        Assertions.assertNotNull(res);
        HtmlCodeResult res3 = aiGeneratorService.generateHtml("你好，不要生成代码");
        HtmlCodeResult res4 = aiGeneratorService.generateHtml("我上一句话说了什么");
    }

    @Test
    void AiGenerateMultiFileService() {
        MultiFileCodeResult res = aiGeneratorService.generateMultiFile("生成一个简单的登录页面，代码控制在20行");
        Assertions.assertNotNull(res);
    }

}
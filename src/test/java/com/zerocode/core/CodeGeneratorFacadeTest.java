package com.zerocode.core;

import com.zerocode.ai.GeneratorTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class CodeGeneratorFacadeTest {

    @Resource
    private CodeGeneratorFacade codeGeneratorFacade;

    @Test
    void generateAndSaveCode() {
        File file = codeGeneratorFacade.generateAndSaveCode("生成一个简易的个人博客网站，博主名字为：马牛", GeneratorTypeEnum.HTML);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateAndSaveMultiCode() {
        File file = codeGeneratorFacade.generateAndSaveCode("生成一个简易的个人博客网站，代码控制在20行", GeneratorTypeEnum.MULTI_FILE);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateAndSaveStreamCode() {
        Flux<String> stringFlux = codeGeneratorFacade.generateAndSaveStreamCode("生成一个个人博客网站，博主为ZXC，github：https://github.com/Study944 ，主项目为ZeroCode前端生成器和build-your-agent智能体平台", GeneratorTypeEnum.MULTI_FILE);
        List<String> block = stringFlux.collectList().block();
        Assertions.assertNotNull(stringFlux);
    }
}
package com.zerocode.langGraph4j.service;

import com.zerocode.langGraph4j.image.state.ImageResource;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiGeneratorRoutingServiceFactoryTest {

    @Resource
    ImageCollectorService imageCollectorService;

    @Test
    void imageCollectorService() {
        String imageResourceList = imageCollectorService.collectImages("做一个关于猫的网站");
        assertNotNull(imageResourceList);
    }
}
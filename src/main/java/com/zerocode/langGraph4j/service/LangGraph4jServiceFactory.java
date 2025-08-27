package com.zerocode.langGraph4j.service;

import com.zerocode.langGraph4j.tool.ImageSearchTool;
import com.zerocode.langGraph4j.tool.MermaidDiagramTool;
import com.zerocode.langGraph4j.tool.UnDrawIllustrationTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.apache.bcel.classfile.Code;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangGraph4j服务工厂
 */
@Slf4j
@Configuration
public class LangGraph4jServiceFactory {

    @Resource
    private ChatModel dashscopeChatModel;
    @Resource
    private ImageSearchTool imageSearchTool;
    @Resource
    private MermaidDiagramTool mermaidDiagramTool;
    @Resource
    private UnDrawIllustrationTool unDrawIllustrationTool;

    /**
     * 创建AI代码生成类型路由服务实例
     */
    @Bean
    public AiGeneratorRoutingService aiCodeGenTypeRoutingService() {
        return AiServices.builder(AiGeneratorRoutingService.class)
                .chatModel(dashscopeChatModel)
                .build();
    }

    /**
     * 创建AI图片收集服务实例
     */
    @Bean
    public ImageCollectorService imageCollectorService() {
        return AiServices.builder(ImageCollectorService.class)
                .chatModel(dashscopeChatModel)
                .tools(imageSearchTool, mermaidDiagramTool, unDrawIllustrationTool)
                .build();
    }

    @Bean
    public CodeQualityCheckService codeQualityCheckService() {
        return AiServices.builder(CodeQualityCheckService.class)
                .chatModel(dashscopeChatModel)
                .build();
    }

    @Bean
    public ImageCollectorPlanService imageCollectorPlanService() {
        return AiServices.builder(ImageCollectorPlanService.class)
                .chatModel(dashscopeChatModel)
                .build();
    }

}

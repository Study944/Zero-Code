package com.zerocode.langGraph4j.image.node;

import com.zerocode.ai.entity.GeneratorTypeEnum;
import com.zerocode.langGraph4j.image.state.WorkflowContext;
import com.zerocode.langGraph4j.service.AiGeneratorRoutingService;
import com.zerocode.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 智能路由节点
 */
@Slf4j
public class RouterNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 智能路由");

            GeneratorTypeEnum generationType;
            try {
                // 获取AI路由服务
                AiGeneratorRoutingService routingService = SpringContextUtil.getBean(AiGeneratorRoutingService.class);
                // 根据原始提示词进行智能路由
                generationType = routingService.generateRouting(context.getOriginalPrompt());
                log.info("AI智能路由完成，选择类型: {} ({})", generationType.getValue(), generationType.getText());
            } catch (Exception e) {
                log.error("AI智能路由失败，使用默认HTML类型: {}", e.getMessage());
                generationType = GeneratorTypeEnum.HTML;
            }

            // 更新状态
            context.setCurrentStep("智能路由");
            context.setGeneratorType(generationType);
            return WorkflowContext.saveContext(context);
        });
    }
}

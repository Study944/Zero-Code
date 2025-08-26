package com.zerocode.langGraph4j.image.node;

import com.zerocode.langGraph4j.image.state.WorkflowContext;
import com.zerocode.langGraph4j.service.ImageCollectorService;
import com.zerocode.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 图片收集节点
 */
@Slf4j
public class ImageCollectorNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 图片收集");
            // 调用上下文工具类获取服务
            ImageCollectorService imageCollectorService = SpringContextUtil.getBean(ImageCollectorService.class);
            String imageListStr = imageCollectorService.collectImages(context.getOriginalPrompt());
            // 更新状态
            context.setCurrentStep("图片收集");
            context.setImageListStr(imageListStr);
            return WorkflowContext.saveContext(context);
        });
    }
}

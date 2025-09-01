package com.zerocode.core.handler;

import com.zerocode.ai.entity.GeneratorTypeEnum;
import com.zerocode.domain.entity.User;
import com.zerocode.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 流处理器执行器
 */
@Slf4j
@Component
public class StreamHandlerExecutor {

    @Resource
    @Lazy
    private JsonMessageStreamHandler jsonMessageStreamHandler;
    @Resource
    @Lazy
    private SimpleTextStreamHandler simpleTextStreamHandler;

    /**
     * 创建流处理器并处理聊天历史记录
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @return 处理后的流
     */
    public Flux<String> doExecute(Flux<String> originFlux,
                                  ChatHistoryService chatHistoryService,
                                  long appId, User loginUser, GeneratorTypeEnum generatorTypeEnum) {
        // 根据代码生成类型选择对应的流处理器
        return switch (generatorTypeEnum) {
            case VUE_PROJECT ,REACT_PROJECT-> // 使用注入的组件实例
                    jsonMessageStreamHandler.handle(originFlux, chatHistoryService, appId, loginUser);
            case HTML, MULTI_FILE -> // 简单文本处理器不需要依赖注入
                    simpleTextStreamHandler.handle(originFlux, chatHistoryService, appId, loginUser);
        };
    }
}

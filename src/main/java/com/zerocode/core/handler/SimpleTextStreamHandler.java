package com.zerocode.core.handler;

import com.zerocode.common.ThrowUtil;
import com.zerocode.domain.entity.App;
import com.zerocode.domain.entity.User;
import com.zerocode.domain.enums.ChatHistoryMessageTypeEnum;
import com.zerocode.domain.vo.AppVO;
import com.zerocode.exception.ErrorCode;
import com.zerocode.service.AppService;
import com.zerocode.service.ChatHistoryService;
import com.zerocode.service.ScreenshotService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.File;

import static com.zerocode.constant.AppConstant.APP_PATH;

/**
 * 简单文本流处理器
 * 处理 HTML 和 MULTI_FILE 类型的流式响应
 */
@Slf4j
@Component
public class SimpleTextStreamHandler {

    @Resource
    private ScreenshotService screenshotService;
    @Resource
    private AppService appService;


    /**
     * 处理传统流（HTML, MULTI_FILE）
     * 直接收集完整的文本响应
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User loginUser) {
        StringBuilder aiResponseBuilder = new StringBuilder();
        return originFlux
                .map(chunk -> {
                    // 收集AI响应内容
                    aiResponseBuilder.append(chunk);
                    return chunk;
                })
                .doOnComplete(() -> {
                    // 流式响应完成后，添加AI消息到对话历史
                    String aiResponse = aiResponseBuilder.toString();
                    chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                    Thread.startVirtualThread(() -> {
                        // 构建项目URL
                        AppVO appById = appService.getAppById(appId);
                        String projectPath = String.format("%s/%s_%s", APP_PATH, appById.getGenerateType(), appById.getId());
                        File projectDir = new File(projectPath);
                        ThrowUtil.throwIf(!projectDir.exists(), ErrorCode.SYSTEM_ERROR,"项目目录不存在：" + projectPath);
                        String projectUrl = "http://localhost:8111/static/" + appById.getGenerateType()+"_" + appId + File.separator;
                        // 截图
                        String screenshotUrl = screenshotService.takeScreenshot(projectUrl);
                        // 更新应用封面
                        App newApp = new App();
                        newApp.setId(appId);
                        newApp.setAppIcon(screenshotUrl);
                        boolean update = appService.updateById(newApp);
                        ThrowUtil.throwIf(!update, ErrorCode.OPERATION_ERROR, "更新应用封面失败");
                    });
                })
                .doOnError(error -> {
                    // 如果AI回复失败，也要记录错误消息
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                });

    }



}

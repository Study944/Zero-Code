package com.zerocode.core.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import com.zerocode.ai.message.*;
import com.zerocode.ai.tool.BaseTool;
import com.zerocode.ai.tool.ToolManager;
import com.zerocode.common.ThrowUtil;
import com.zerocode.core.builder.VueProjectBuilder;
import com.zerocode.domain.entity.App;
import com.zerocode.domain.entity.User;
import com.zerocode.domain.enums.ChatHistoryMessageTypeEnum;
import com.zerocode.exception.ErrorCode;
import com.zerocode.service.AppService;
import com.zerocode.service.ChatHistoryService;
import com.zerocode.service.ScreenshotService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static com.zerocode.constant.AppConstant.APP_PATH;

/**
 * JSON 消息流处理器
 * 处理 VUE_PROJECT 类型的复杂流式响应，包含工具调用信息
 */
@Slf4j
@Component
public class JsonMessageStreamHandler {

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ScreenshotService screenshotService;

    @Resource
    private AppService appService;

    @Resource
    private ToolManager toolManager;

    /**
     * 处理 TokenStream（VUE_PROJECT）
     * 解析 JSON 消息并重组为完整的响应格式
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
        // 收集数据用于生成后端记忆格式
        StringBuilder chatHistoryStringBuilder = new StringBuilder();
        // 用于跟踪已经见过的工具ID，判断是否是第一次调用
        Set<String> seenToolIds = new HashSet<>();
        return originFlux
                .map(chunk -> {
                    // 解析每个 JSON 消息块
                    return handleJsonMessageChunk(chunk, chatHistoryStringBuilder, seenToolIds);
                })
                .filter(StrUtil::isNotEmpty) // 过滤空字串
                .doOnComplete(() -> {
                    // 流式响应完成后，添加 AI 消息到对话历史
                    String aiResponse = chatHistoryStringBuilder.toString();
                    chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                    // 异步生成前端VUE项目
                    String vueProjectPath = APP_PATH+"/vue_project_"+appId;
                    vueProjectBuilder.buildProjectAsync(vueProjectPath,success->{
                        if (success) {
                            // 项目构建成功，进行截图
                            takeScreenshotOfProject(appId, vueProjectPath);
                        } else {
                            log.error("Vue项目构建失败，无法进行截图，项目路径: {}", vueProjectPath);
                        }
                    });
                })
                .doOnError(error -> {
                    // 如果AI回复失败，也要记录错误消息
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                });
    }

    /**
     * 对生成的项目进行截图
     *
     * @param appId 项目ID
     * @param projectPath 项目路径
     */
    private void takeScreenshotOfProject(long appId, String projectPath) {
        Thread.startVirtualThread(() -> {
            try {
                // 构建项目访问URL
                String path = projectPath + "/dist/index.html";
                File indexFile = new File(path);
                ThrowUtil.throwIf(indexFile == null || !indexFile.exists(), ErrorCode.SYSTEM_ERROR,"项目目录中没有 index.html 文件：" + projectPath);
                String projectUrl = "http://localhost:8111/static/"+"vue_project_"+appId+File.separator;
                // 调用截图服务
                String screenshotUrl = screenshotService.takeScreenshot(projectUrl);
                // 更新应用封面
                App newApp = new App();
                newApp.setId(appId);
                newApp.setAppIcon(screenshotUrl);
                boolean update = appService.updateById(newApp);
                ThrowUtil.throwIf(!update, ErrorCode.OPERATION_ERROR, "更新应用封面失败");
            } catch (Exception e) {
                log.error("截图过程中发生错误", e);
            }
        });
    }

    /**
     * 解析并收集 TokenStream 数据
     */
    private String handleJsonMessageChunk(String chunk, StringBuilder chatHistoryStringBuilder, Set<String> seenToolIds) {
        // 解析 JSON
        StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
        StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getByType(streamMessage.getType());
        switch (typeEnum) {
            case AI_RESPONSE -> {
                AiResponseMessage aiMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                String data = aiMessage.getContent();
                // 直接拼接响应
                chatHistoryStringBuilder.append(data);
                return data;
            }
            case TOOL_REQUEST -> {
                ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                String toolId = toolRequestMessage.getId();
                // 检查是否是第一次看到这个工具 ID
                if (toolId != null && !seenToolIds.contains(toolId)) {
                    // 第一次调用这个工具，记录 ID 并完整返回工具信息
                    seenToolIds.add(toolId);
                    return String.format("\n\n[选择工具] %s \n\n", toolRequestMessage.getName());
                } else {
                    // 不是第一次调用这个工具，直接返回空
                    return "";
                }
            }
            case TOOL_EXECUTED -> {
                ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
                JSONObject jsonObject = JSONUtil.parseObj(toolExecutedMessage.getArguments());
                BaseTool tool = toolManager.getTool(toolExecutedMessage.getName());
                String res = tool.toolResponse(jsonObject);
                // 输出前端和要持久化的内容
                String output = String.format("\n\n%s\n\n", res);
                chatHistoryStringBuilder.append(output);
                return output;
            }
            default -> {
                log.error("不支持的消息类型: {}", typeEnum);
                return "";
            }
        }
    }
}
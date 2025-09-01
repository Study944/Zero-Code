package com.zerocode.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zerocode.common.ThrowUtil;
import com.zerocode.constant.UserConstant;
import com.zerocode.domain.entity.App;
import com.zerocode.domain.entity.ChatHistory;
import com.zerocode.domain.entity.User;
import com.zerocode.domain.enums.ChatHistoryMessageTypeEnum;
import com.zerocode.exception.ErrorCode;
import com.zerocode.mapper.ChatHistoryMapper;
import com.zerocode.service.AppService;
import com.zerocode.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史服务层实现
 */
@Slf4j
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    @Lazy
    private AppService appService;

    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {
        ThrowUtil.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtil.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtil.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtil.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        // 验证消息类型是否有效
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtil.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的消息类型: " + messageType);
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageType)
                .userId(userId)
                .build();
        return this.save(chatHistory);
    }

    @Override
    public boolean removeChatHistory(Long appId) {
        ThrowUtil.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("app_id", appId);
        ChatHistory chatHistory = this.getById(appId);
        if (chatHistory == null) return true;
        return this.remove(queryWrapper);
    }

    @Override
    public Page<ChatHistory> listChatHistory(Long appId, int pageSize, LocalDateTime lastCreateTime, User loginUser) {
        // 参数校验
        ThrowUtil.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtil.throwIf(pageSize <= 0, ErrorCode.PARAMS_ERROR, "分页大小不能小于等于0");
        ThrowUtil.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        App app = appService.getById(appId);
        ThrowUtil.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        ThrowUtil.throwIf(!app.getUserId().equals(loginUser.getId()) &&
                !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole()), ErrorCode.NO_AUTH_ERROR);
        // 查询条件
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(ChatHistory::getAppId, appId);
        queryWrapper.lt(ChatHistory::getCreateTime, lastCreateTime, lastCreateTime != null);
        queryWrapper.orderBy(ChatHistory::getCreateTime, false);
        // 分页查询
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    @Override
    public void loadChatHistory(Long appId, MessageWindowChatMemory messageWindowChatMemory) {
        try {
            // 查询对话历史表
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .limit(1,20)
                    .orderBy(ChatHistory::getCreateTime, true);
            List<ChatHistory> chatHistoryList = this.list(queryWrapper);
            if (chatHistoryList == null) return;
            // 清空
            messageWindowChatMemory.clear();
            // 添加
            chatHistoryList.stream()
                    .forEach(chatHistory -> {
                        if (ChatHistoryMessageTypeEnum.USER.getValue().equals(chatHistory.getMessageType())) {
                            messageWindowChatMemory.add(new UserMessage(chatHistory.getMessage()));
                        } else {
                            messageWindowChatMemory.add(new AiMessage(chatHistory.getMessage()));
                        }
                    });
            log.info("加载成功{}条消息", chatHistoryList.size());
        } catch (Exception e) {
            log.error("加载失败: {}", e.getMessage());
        }
    }

}

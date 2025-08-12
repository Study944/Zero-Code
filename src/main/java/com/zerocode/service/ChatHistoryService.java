package com.zerocode.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.zerocode.domain.entity.ChatHistory;
import com.zerocode.domain.entity.User;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史服务层
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    boolean removeChatHistory(Long appId);

    Page<ChatHistory> listChatHistory(Long appId, int pageSize, LocalDateTime lastCreateTime, User loginUser);

    void loadChatHistory(Long appId, MessageWindowChatMemory messageWindowChatMemory);
}

package com.zerocode.service;

import com.mybatisflex.core.service.IService;
import com.zerocode.domain.entity.ChatHistory;

/**
 * 对话历史服务层
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    boolean addChatMessage(Long appId, String message, String messageType, Long userId);
}

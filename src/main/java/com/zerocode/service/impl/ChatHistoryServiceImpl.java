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
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 对话历史服务层实现
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

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
                .eq("appId", appId);
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
        queryWrapper.lt(ChatHistory::getCreateTime, lastCreateTime,lastCreateTime!=null);
        queryWrapper.orderBy(ChatHistory::getCreateTime,false);
        // 分页查询
        return this.page(Page.of(1, pageSize), queryWrapper);
    }


}

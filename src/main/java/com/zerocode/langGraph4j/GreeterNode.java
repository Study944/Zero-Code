package com.zerocode.langGraph4j;

import org.bsc.langgraph4j.action.NodeAction;
import java.util.List;
import java.util.Map;

// 添加问候语的节点
class GreeterNode implements NodeAction<SimpleState> {
    @Override
    public Map<String, Object> apply(SimpleState state) {
        System.out.println("GreeterNode 正在执行。当前消息: " + state.messages());
        return Map.of(SimpleState.MESSAGES_KEY, "来自 GreeterNode 的您好！");
    }
}

// 添加响应的节点
class ResponderNode implements NodeAction<SimpleState> {
    @Override
    public Map<String, Object> apply(SimpleState state) {
        System.out.println("ResponderNode 正在执行。当前消息： " + state.messages());
        List<String> currentMessages = state.messages();
        if (currentMessages.contains("来自 GreeterNode 的您好！")) {
            return Map.of(SimpleState.MESSAGES_KEY, "确认问候！");
        }
        return Map.of(SimpleState.MESSAGES_KEY, "找不到问候语.");
    }
}
package com.zerocode.langGraph4j;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channels;
import org.bsc.langgraph4j.state.Channel;

import java.util.*;

/**
 * 定义图表的状态
 */
class SimpleState extends AgentState {
    public static final String MESSAGES_KEY = "messages";

    // 定义状态的架构。
    // MESSAGES_KEY将保存字符串列表，并将附加新消息。
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            MESSAGES_KEY, Channels.appender(ArrayList::new)
    );

    public SimpleState(Map<String, Object> initData) {
        super(initData);
    }

    public List<String> messages() {
        return this.<List<String>>value("messages")
                .orElse( List.of() );
    }
}
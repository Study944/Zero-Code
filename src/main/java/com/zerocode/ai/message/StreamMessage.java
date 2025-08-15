package com.zerocode.ai.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流式响应消息基类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StreamMessage {

    // 消息类型
    private String type;

}

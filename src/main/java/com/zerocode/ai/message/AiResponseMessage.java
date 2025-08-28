package com.zerocode.ai.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.zerocode.ai.message.StreamMessageTypeEnum.AI_RESPONSE;

/**
 * AI响应消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiResponseMessage extends StreamMessage{

    private String content;

    public AiResponseMessage(String content) {
        super(AI_RESPONSE.getType());
        this.content = content;
    }

}

package com.zerocode.domain.dto;

import lombok.Data;

@Data
public class ChatHistoryAddDTO {

    private Long appId;

    private String message;

    private String messageType;

    private Long userId;
}

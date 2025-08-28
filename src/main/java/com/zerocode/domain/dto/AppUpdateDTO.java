package com.zerocode.domain.dto;

import lombok.Data;

@Data
public class AppUpdateDTO {

    /**
     * 应用ID
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;


}

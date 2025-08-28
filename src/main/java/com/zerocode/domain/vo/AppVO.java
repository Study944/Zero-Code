package com.zerocode.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AppVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用描述
     */
    private String appDesc;

    /**
     * 应用图标
     */
    private String appIcon;

    /**
     * 应用初始化提示词
     */
    private String initPrompt;

    /**
     * 生成模式：text单文件/多文件
     */
    private String generateType;

    /**
     * 创建者id
     */
    private Long userId;

    /**
     * 应用优先级
     */
    private Integer priority;

    /**
     * 部署密钥
     */
    private String deployKey;

    /**
     * 部署时间
     */
    private LocalDateTime deployTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 应用创建者信息
     */
    private UserVO userVO;
}

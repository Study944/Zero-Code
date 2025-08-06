package com.zerocode.domain.dto;

import com.zerocode.domain.vo.UserVO;
import lombok.Data;

@Data
public class AppListMyAppDTO {

    /**
     * 当前页
     */
    private Integer current = 1;

    /**
     * 页面大小
     */
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = "descend";

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
     * 应用初始化提示词
     */
    private String initPrompt;

    /**
     * 创建人id
     */
    private Long userId;

    /**
     * 生成模式：text单文件/多文件
     */
    private String generateType;

    /**
     * 应用优先级
     */
    private Integer priority;

    /**
     * 部署密钥
     */
    private String deployKey;

}

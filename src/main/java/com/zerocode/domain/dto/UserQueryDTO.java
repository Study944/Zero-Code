package com.zerocode.domain.dto;

import lombok.Data;

@Data
public class UserQueryDTO {
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
     * 账号
     */
    private String userAccount;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 角色
     */
    private String userRole;

}

package com.zerocode.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginDTO {

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;
}

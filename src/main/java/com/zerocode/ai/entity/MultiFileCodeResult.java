package com.zerocode.ai.entity;

import lombok.Data;

/**
 * 多文件代码结果
 */
@Data
public class MultiFileCodeResult {

    private String htmlCode;

    private String cssCode;

    private String jsCode;

    private String description;
}

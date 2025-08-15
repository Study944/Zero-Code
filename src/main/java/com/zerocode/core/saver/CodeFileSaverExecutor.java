package com.zerocode.core.saver;

import com.zerocode.ai.entity.GeneratorTypeEnum;
import com.zerocode.ai.entity.HtmlCodeResult;
import com.zerocode.ai.entity.MultiFileCodeResult;
import com.zerocode.exception.BusinessException;
import com.zerocode.exception.ErrorCode;

import java.io.File;

/**
 * 代码文件保存执行器
 * 根据代码生成类型执行相应的保存逻辑
 */
public class CodeFileSaverExecutor {

    private static final HtmlCodeFileSaverTemplate htmlCodeFileSaver = new HtmlCodeFileSaverTemplate();

    private static final MultiFileCodeFileSaverTemplate multiFileCodeFileSaver = new MultiFileCodeFileSaverTemplate();

    /**
     * 执行代码保存
     *
     * @param codeResult  代码结果对象
     * @param codeGenType 代码生成类型
     * @return 保存的目录
     */
    public static File executeSaver(Object codeResult, GeneratorTypeEnum codeGenType, Long appId) {
        return switch (codeGenType) {
            case HTML -> htmlCodeFileSaver.saveCodeFile((HtmlCodeResult) codeResult, codeGenType, appId);
            case MULTI_FILE -> multiFileCodeFileSaver.saveCodeFile((MultiFileCodeResult) codeResult, codeGenType, appId);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType);
        };
    }
}

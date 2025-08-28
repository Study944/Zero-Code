package com.zerocode.core.parser;

import com.zerocode.ai.entity.GeneratorTypeEnum;
import com.zerocode.exception.BusinessException;
import com.zerocode.exception.ErrorCode;

/**
 * 代码解析执行器
 */
public class CodeParserExecutor {

    private static final HtmlCodeParserStrategy htmlCodeParser = new HtmlCodeParserStrategy();

    private static final MultiFileCodeParserStrategy multiFileCodeParser = new MultiFileCodeParserStrategy();

    /**
     * 执行代码解析
     *
     * @param codeContent 代码内容
     * @param codeGenType 代码生成类型
     * @return 解析结果（HtmlCodeResult 或 MultiFileCodeResult）
     */
    public static Object executeParser(String codeContent, GeneratorTypeEnum generatorTypeEnum) {
        return switch (generatorTypeEnum) {
            case HTML -> htmlCodeParser.parseCode(codeContent);
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + generatorTypeEnum);
        };
    }
}

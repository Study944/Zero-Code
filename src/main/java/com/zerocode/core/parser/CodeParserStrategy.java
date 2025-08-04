package com.zerocode.core.parser;

/**
 * 代码解析器策略接口
 * @param <T> 解析结果类型
 */
public interface CodeParserStrategy<T> {

    T parseCode(String codeContent);

}

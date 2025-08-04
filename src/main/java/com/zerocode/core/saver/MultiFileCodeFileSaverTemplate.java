package com.zerocode.core.saver;

import com.zerocode.ai.GeneratorTypeEnum;
import com.zerocode.ai.MultiFileCodeResult;

/**
 * 多文件代码保存模板
 */
public class MultiFileCodeFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {
    @Override
    protected void saveFile(String uniqueFilePath, MultiFileCodeResult codeResult, GeneratorTypeEnum generatorTypeEnum) {
        writeFile(uniqueFilePath, "index.html", codeResult.getHtmlCode());
        writeFile(uniqueFilePath, "style.css", codeResult.getCssCode());
        writeFile(uniqueFilePath, "script.js", codeResult.getJsCode());
    }
}

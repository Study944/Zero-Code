package com.zerocode.core.saver;

import com.zerocode.ai.GeneratorTypeEnum;
import com.zerocode.ai.HtmlCodeResult;

/**
 * html代码保存模板
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult>{
    @Override
    protected void saveFile(String uniqueFilePath, HtmlCodeResult htmlCodeResult, GeneratorTypeEnum generatorTypeEnum) {
        writeFile(uniqueFilePath, "index.html", htmlCodeResult.getHtmlCode());
    }
}

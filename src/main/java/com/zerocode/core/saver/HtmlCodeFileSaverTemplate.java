package com.zerocode.core.saver;

import cn.hutool.core.util.StrUtil;
import com.zerocode.ai.entity.GeneratorTypeEnum;
import com.zerocode.ai.entity.HtmlCodeResult;

/**
 * html代码保存模板
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult>{
    @Override
    protected void saveFile(String uniqueFilePath, HtmlCodeResult htmlCodeResult, GeneratorTypeEnum generatorTypeEnum) {
        String htmlCode = htmlCodeResult.getHtmlCode();
        if (StrUtil.isNotBlank(htmlCode)) {
            writeFile(uniqueFilePath, "index.html", htmlCode);
        }
    }
}

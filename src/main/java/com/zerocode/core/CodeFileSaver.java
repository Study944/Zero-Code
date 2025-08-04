package com.zerocode.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.zerocode.ai.GeneratorTypeEnum;
import com.zerocode.ai.HtmlCodeResult;
import com.zerocode.ai.MultiFileCodeResult;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 文件保存器
 */
public class CodeFileSaver {
    // 根路径
    private static final String ROOT_PATH = System.getProperty("user.dir") + File.separator + "tmp";

    /**
     * 保存HTML代码
     *
     * @param htmlCodeResult    HTML代码结果
     * @param generatorTypeEnum 生成类型
     * @return 文件
     */
    public static File saveHtmlCode(HtmlCodeResult htmlCodeResult, GeneratorTypeEnum generatorTypeEnum) {
        String uniqueFilePath = buildUniqueFilePath(generatorTypeEnum);
        writeFile(uniqueFilePath, "index.html", htmlCodeResult.getHtmlCode());
        return new File(uniqueFilePath);
    }

    /**
     * 保存多文件代码
     *
     * @param multiFileCodeResult 多文件代码结果
     * @param generatorTypeEnum   生成类型
     * @return 文件
     */
    public static File saveMultiFileCode(MultiFileCodeResult multiFileCodeResult, GeneratorTypeEnum generatorTypeEnum) {
        String uniqueFilePath = buildUniqueFilePath(generatorTypeEnum);
        writeFile(uniqueFilePath, "index.html", multiFileCodeResult.getHtmlCode());
        writeFile(uniqueFilePath, "style.css", multiFileCodeResult.getCssCode());
        writeFile(uniqueFilePath, "script.js", multiFileCodeResult.getJsCode());
        return new File(uniqueFilePath);
    }

    /**
     * 写入文件
     *
     * @param path     文件根路径
     * @param fileName 文件名
     * @param content  文件内容
     */
    private static void writeFile(String path, String fileName, String content) {
        String filePath = path + File.separator + fileName;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }

    /**
     * 生成唯一文件路径 跟路径/生成类型_雪花ID
     *
     * @param generatorTypeEnum 生成类型
     * @return 文件路径
     */
    private static String buildUniqueFilePath(GeneratorTypeEnum generatorTypeEnum) {
        String generatorType = generatorTypeEnum.getValue();
        String uniqueFilePath = ROOT_PATH + File.separator + generatorType +"_"+ IdUtil.getSnowflakeNextIdStr();
        FileUtil.mkdir(uniqueFilePath);
        return uniqueFilePath;
    }
}

package com.zerocode.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.zerocode.ai.GeneratorTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 文件保存器模板
 *
 * @param <T> 代码参数类型
 */
public abstract class CodeFileSaverTemplate<T> {

    // 根路径
    private static final String ROOT_PATH = System.getProperty("user.dir") + File.separator + "tmp";

    /**
     * 保存代码文件模板（使用final禁止子类重写）
     * @param codeResult 代码结果
     * @param generatorTypeEnum 生成类型
     * @return 文件
     */
    public final File saveCodeFile(T codeResult, GeneratorTypeEnum generatorTypeEnum) {
        // 获取唯一文件名
        String uniqueFilePath = buildUniqueFilePath(generatorTypeEnum);
        // 文件保存
        saveFile(uniqueFilePath, codeResult, generatorTypeEnum);
        // 返回文件
        return new File(uniqueFilePath);
    }

    protected abstract void saveFile(String uniqueFilePath, T codeContent, GeneratorTypeEnum generatorTypeEnum);

    /**
     * 写入文件
     *
     * @param path     文件根路径
     * @param fileName 文件名
     * @param content  文件内容
     */
    protected static void writeFile(String path, String fileName, String content) {
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
        String uniqueFilePath = ROOT_PATH + File.separator + generatorType + "_" + IdUtil.getSnowflakeNextIdStr();
        FileUtil.mkdir(uniqueFilePath);
        return uniqueFilePath;
    }

}

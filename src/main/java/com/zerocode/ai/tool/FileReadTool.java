package com.zerocode.ai.tool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.zerocode.constant.AppConstant.APP_PATH;

/**
 * 文件内容读取工具
 */
@Slf4j
@Component
public class FileReadTool extends BaseTool{

    @Tool("读取指定目录下的文件内容")
    public String readFileContent(
            @P("文件的相对路径")
            String relativeFilePath,
            @ToolMemoryId Long appId
    ) {
        try {
            // 1.根据相对路径得到其文件绝对路径
            Path path = Paths.get(relativeFilePath);
            if (!path.isAbsolute()) {
                String projectDirName = "vue_project_" + appId;
                Path projectRoot = Paths.get(APP_PATH, projectDirName);
                path = projectRoot.resolve(relativeFilePath);
            }
            // 2.判断文件是否存在
            File targetFile = path.toFile();
            if (!targetFile.exists() || !targetFile.isFile()) {
                return "错误：读取文件不存在 - " + relativeFilePath;
            }
            // 3.读取文件内容
            String fileContent = FileUtil.readString(targetFile, StandardCharsets.UTF_8);
            String res = String.format("成功读取 %s 文件内容: %s", relativeFilePath, fileContent);
            return res;
        } catch (Exception e) {
            String errorMessage = "读取文件失败: " + relativeFilePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    @Override
    public String getToolName() {
        return "readFileContent";
    }

    @Override
    public String toolResponse(JSONObject arguments) {
        String relativeDirPath = arguments.getStr("relativeFilePath");
        String toolName = getToolName();
        return String.format("""
                        [工具调用] %s %s
                        """, toolName,relativeDirPath);
    }
}

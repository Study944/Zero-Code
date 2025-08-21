package com.zerocode.ai.tool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.zerocode.constant.AppConstant.APP_PATH;

/**
 * 文件删除工具
 */
@Slf4j
@Component
public class FileDeleteTool extends BaseTool{

    // 不可删除的文件列表
    private static final List<String> PROTECTED_FILES = Arrays.asList(
            "package.json",
            "package-lock.json",
            "yarn.lock",
            "pnpm-lock.yaml",
            "index.html",
            "vite.config.js",
            "vue.config.js",
            "webpack.config.js",
            "tsconfig.json",
            "babel.config.js",
            "postcss.config.js",
            "tailwind.config.js",
            ".gitignore",
            "README.md",
            "public/index.html"
    );

    @Tool("删除指定目录下的文件")
    public String deleteFile(
            @P("需要删除的文件的相对路径")
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
                if (!path.startsWith(projectRoot)) {
                    return "错误：不允许删除项目目录外的文件 - " + relativeFilePath;
                }
            }
            // 2.判断文件是否存在
            File targetFile = path.toFile();
            if (!targetFile.exists() || !targetFile.isFile()) {
                return "错误：删除文件不存在 - " + relativeFilePath;
            }
            // 3.判断文件是否可删除
            if (PROTECTED_FILES.contains(targetFile.getName())){
                return "错误：删除文件失败 - " + relativeFilePath + "，文件不可删除";
            }
            // 4.检查文件是否是目录
            if (targetFile.isDirectory()){
                boolean containsProtectedFiles = containsProtectedFiles(targetFile);
                if (containsProtectedFiles){
                    return "错误：删除文件失败 - " + relativeFilePath + "，目录下包含受保护的文件";
                }
            }
            // 5.删除文件
            boolean delete = FileUtil.del(targetFile);
            String res = delete ? "成功删除文件: " + relativeFilePath : "删除文件失败: " + relativeFilePath;
            return res;
        } catch (Exception e) {
            String errorMessage = "删除文件失败: " + relativeFilePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }
    /**
     * 检查目录是否包含受保护的文件
     */
    private boolean containsProtectedFiles(File directory) {
        File[] files = directory.listFiles();
        if (files == null) return false;
        // 遍历目录下的所有文件和子目录
        for (File file : files) {
            if (file.isDirectory()) {
                if (containsProtectedFiles(file)) {
                    return true;
                }
            } else {
                if (PROTECTED_FILES.contains(file.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getToolName() {
        return "deleteFile";
    }

    @Override
    public String toolResponse(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        String toolName = getToolName();
        return String.format("""
                        [工具调用] %s %s
                        """, toolName,relativeFilePath);
    }
}

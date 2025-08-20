package com.zerocode.service.impl;

import cn.hutool.core.util.ZipUtil;
import com.zerocode.common.ThrowUtil;
import com.zerocode.exception.BusinessException;
import com.zerocode.exception.ErrorCode;
import com.zerocode.service.ProjectDownloadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Set;

/**
 * 文件下载服务实现类
 */
@Slf4j
@Service
public class ProjectDownloadServiceImpl implements ProjectDownloadService {

    // 初始化过滤文件集合
    private static final Set<String> IGNORE_FILES = Set.of(
            // 包管理器相关
            "node_modules",
            "package-lock.json",
            "yarn.lock",
            "pnpm-lock.yaml",
            "npm-debug.log",
            "yarn-error.log",
            // 构建产物和缓存
            "dist",
            "build",
            ".next",
            "out",
            ".cache",
            ".vite",
            // 版本控制
            ".git",
            ".gitignore",
            // IDE 和编辑器配置
            ".idea",
            ".vscode",
            // 测试和日志
            "coverage",
            "temp",
            "tmp",
            // 操作系统相关
            ".DS_Store",
            "Thumbs.db"
    );
    // 初始化过滤文件拓展名集合
    private static final Set<String> IGNORE_EXTENSIONS = Set.of(
            ".log",
            ".tmp",
            ".cache"
    );

    /**
     * 判断路径是否允许下载
     * @param rootPath 根路径
     * @param fullPath 完整路径
     * @return 是否允许下载
     */
    @Override
    public boolean isPathAllowed(Path rootPath, Path fullPath) {
        // 获取相对路径
        Path relativizePath = rootPath.relativize(fullPath);
        for (Path path : relativizePath) {
            String pathName = path.toString();
            // 判断文件名是否在过滤集合中
            if (IGNORE_FILES.contains(pathName)) {
                log.debug("忽略文件名: {}", pathName);
                return false;
            }
        }
        // 对路径中的最后一个元素（文件名）检查文件拓展名
        String fileName = fullPath.getFileName().toString();
        if (IGNORE_EXTENSIONS.stream().anyMatch(fileName::endsWith)){
            log.debug("忽略文件拓展名: {}", fileName);
            return false;
        }
        return true;
    }

    /**
     * 下载项目
     * @param projectPath
     * @param projectName
     * @param response
     */
    @Override
    public void downloadProject(String projectPath, String projectName, HttpServletResponse response) {
        // 校验项目路径
        ThrowUtil.throwIf(projectPath == null || projectPath.isEmpty(), ErrorCode.PARAMS_ERROR);
        File projectFile = new File(projectPath);
        ThrowUtil.throwIf(projectFile == null || !projectFile.exists(), ErrorCode.NOT_FOUND_ERROR);
        // 设置Http响应头
        response.setCharacterEncoding("utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" + projectName + ".zip");
        // 文件过滤
        FileFilter fileFilter = file -> isPathAllowed(projectFile.toPath(), file.toPath());
        try {
            // 创建Zip文件
            ZipUtil.zip(response.getOutputStream(), StandardCharsets.UTF_8,false,fileFilter,projectFile);
            log.info("项目压缩完成：{}", projectFile.getAbsolutePath());
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "项目压缩失败");
        }
    }


}

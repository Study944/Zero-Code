package com.zerocode.service;

import jakarta.servlet.http.HttpServletResponse;

import java.nio.file.Path;

/**
 * 源码下载服务
 */
public interface ProjectDownloadService {

    boolean isPathAllowed(Path rootPath, Path fullPath);

    void downloadProject(String projectPath, String projectName, HttpServletResponse response);
}

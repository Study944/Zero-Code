package com.zerocode.service.impl;

import cn.hutool.core.util.StrUtil;
import com.zerocode.common.ThrowUtil;
import com.zerocode.exception.ErrorCode;
import com.zerocode.manager.CosManager;
import com.zerocode.service.ScreenshotService;
import com.zerocode.utils.WebScreenshotUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 截图服务实现类
 */
@Service
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private CosManager cosManager;

    @Override
    public String takeScreenshot(String url) {
        // 参数校验
        ThrowUtil.throwIf(StrUtil.isBlank(url), ErrorCode.PARAMS_ERROR, "URL不能为空");
        // 截图
        String screenshotPath = WebScreenshotUtil.takeScreenshot(url);
        ThrowUtil.throwIf(StrUtil.isBlank(screenshotPath), ErrorCode.OPERATION_ERROR, "截图失败");
        // 上传截图到云存储
        String screenshotUrl = uploadScreenshotToCos(screenshotPath);
        // 清除本地文件
        deleteScreenshot(screenshotPath);
        return screenshotUrl;
    }

    /**
     * 上传截图到云存储
     * @param screenshotPath 本地文件路径
     * @return 云存储URL
     */
    private String uploadScreenshotToCos(String screenshotPath) {
        // 判断文件是否存在
        File file = new File(screenshotPath);
        ThrowUtil.throwIf(!file.exists(), ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        // 设置上传文件路径
        String format = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String key = String.format("/screenshot/%s/%s",format , file.getName());
        // 上传文件
        return cosManager.uploadScreenshot(file, key);
    }

    private Boolean deleteScreenshot(String screenshotPath){
        // 判断文件是否存在
        File file = new File(screenshotPath);
        ThrowUtil.throwIf(!file.exists(), ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        // 删除本地截图文件
        return file.delete();
    }

}

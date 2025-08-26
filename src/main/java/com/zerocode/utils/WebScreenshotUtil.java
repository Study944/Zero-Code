package com.zerocode.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.StrUtil;
import com.zerocode.common.ThrowUtil;
import com.zerocode.exception.BusinessException;
import com.zerocode.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

/**
 * 截图工具类
 */
@Slf4j
public class WebScreenshotUtil {

    private static final WebDriver webDriver;

    static {
        final int DEFAULT_WIDTH = 1600;
        final int DEFAULT_HEIGHT = 900;
        webDriver = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @PreDestroy
    public void destroy() {
        webDriver.quit();
    }

    public static String takeScreenshot(String url) {
        // WebDriver截图
        webDriver.get(url);
        // 等待页面加载完成
        new WebDriverWait(webDriver, Duration.ofSeconds(10))
                .until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
        byte[] screenshot = ((TakesScreenshot) webDriver).getScreenshotAs(org.openqa.selenium.OutputType.BYTES);
        // 保存原始截图
        String rootPath = System.getProperty("user.dir")+ File.separator+"tmp"+File.separator+"screenshot";
        String screenshotPath = rootPath + File.separator + UUID.randomUUID().toString().substring(0, 8) +"_screenshot.png";
        File file = saveScreenshot(screenshot, screenshotPath);
        // 压缩截图
        String compressPath = rootPath + File.separator + UUID.randomUUID().toString().substring(0, 8) +"_compress.png";
        compressScreenshot(file, compressPath);
        // 删除原始截图
        FileUtil.del(screenshotPath);
        // 返回压缩截图路劲
        return compressPath;
    }

    /**
     * 压缩图片
     * @param screenshotFile
     * @param compressPath
     */
    private static void compressScreenshot(File screenshotFile,String compressPath) {
        try {
            float quality = 0.5f;
            ImgUtil.compress(screenshotFile, new File(compressPath),quality);
            log.info("压缩图片成功：{}", compressPath);
        } catch (IORuntimeException e) {
            log.error("压缩图片失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }

    /**
     * 保存截图到本地
     * @param screenshot 截图
     * @param savePath 保存路径
     * @return
     */
    private static File saveScreenshot(byte[] screenshot, String savePath) {
        File file = new File(savePath);
        try {
            FileUtils.writeByteArrayToFile(file, screenshot);
            log.info("保存图片成功：{}", savePath);
            return file;
        } catch (Exception e) {
            log.error("保存图片失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    /**
     * 初始化 Chrome 浏览器驱动
     */
    private static WebDriver initChromeDriver(int width, int height) {
        try {
            // 自动化管理 ChromeDriver 驱动程序
            // WebDriverManager.chromedriver().setup();
            // 配置 Chrome 镜像源
            WebDriverManager.chromedriver()
                    .config()
                    .setChromeDriverMirrorUrl(new URL("https://registry.npmmirror.com/binary/chromedriver/"));
            // 配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();
            // 无头模式
            options.addArguments("--headless");
            // 禁用GPU（在某些环境下避免问题）
            options.addArguments("--disable-gpu");
            // 禁用沙盒模式（Docker环境需要）
            options.addArguments("--no-sandbox");
            // 禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            // 禁用扩展
            options.addArguments("--disable-extensions");
            // 设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            // 创建驱动
            WebDriver driver = new ChromeDriver(options);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }
}

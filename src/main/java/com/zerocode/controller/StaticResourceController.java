package com.zerocode.controller;

import com.zerocode.domain.entity.App;
import com.zerocode.domain.vo.AppVO;
import com.zerocode.service.AppService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;



import java.io.File;

import static com.zerocode.constant.AppConstant.APP_PATH;

@RestController
@RequestMapping("/static")
public class StaticResourceController {

    @Autowired
    private AppService appService;


    // 应用生成根目录（用于浏览）
    private static final String PREVIEW_ROOT_DIR = APP_PATH;

    /**
     * 提供静态资源访问，支持目录重定向
     * 访问格式：http://localhost:8111/static/{codePath}
     */
    @GetMapping("/{codePath}/**")
    public ResponseEntity<Resource> serveStaticResource(
            @PathVariable String codePath,
            HttpServletRequest request) {
        try {
            // 获取资源路径
            String resourcePath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            resourcePath = resourcePath.substring(("/static/" + codePath).length());
            // 如果是目录访问（不带斜杠），重定向到带斜杠的URL
            if (resourcePath.isEmpty()) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Location", request.getRequestURI() + "/");
                return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
            }
            // 判断是否是工程化项目
            boolean contains = codePath.contains("project");
            if (contains && resourcePath.equals("/")) {
                resourcePath = "/dist/index.html";
            }
            // 默认返回 index.html
            else if (resourcePath.equals("/")) {
                resourcePath = "/index.html";
            }
            // 增加对 /assets/ 的特殊处理
            String filePath;
            if (resourcePath.startsWith("/assets/")) {
                // 对于 /assets/ 路径，加上 /dist
                filePath = PREVIEW_ROOT_DIR + "/" + codePath + "/dist" + resourcePath;
            } else {
                // 对于其他路径（例如 index.html），保持原样
                filePath = PREVIEW_ROOT_DIR + "/" + codePath + resourcePath;
            }
            File file = new File(filePath);
            // 检查文件是否存在
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            // 返回文件资源
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header("Content-Type", getContentTypeWithCharset(filePath))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根据文件扩展名返回带字符编码的 Content-Type
     */
    private String getContentTypeWithCharset(String filePath) {
        if (filePath.endsWith(".html")) return "text/html; charset=UTF-8";
        if (filePath.endsWith(".css")) return "text/css; charset=UTF-8";
        if (filePath.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (filePath.endsWith(".png")) return "image/png";
        if (filePath.endsWith(".jpg")) return "image/jpeg";
        return "application/octet-stream";
    }
}

package com.zerocode.langGraph4j.tool;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zerocode.langGraph4j.image.state.ImageCategoryEnum;
import com.zerocode.langGraph4j.image.state.ImageResource;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜索内容图片工具
 */
@Slf4j
@Component
public class ImageSearchTool {

    @Value("${pexels.apiKey}")
    private String API_KEY;

    // Pexels 搜索接口
    private static final String API_URL = "https://api.pexels.com/v1/search";

    @Tool("search image from web Pexels")
    public List<ImageResource> searchContentImages(@P("Search query keyword") String query) {
        try {
            // 设置图片属性
            ImageCategoryEnum category = ImageCategoryEnum.CONTENT;
            String description = query;
            // 搜索图片
            List<String> urlList = searchMediumImages(query);
            // 拼接图片信息
            List<ImageResource> res = urlList.stream()
                    .map(url -> ImageResource.builder()
                            .category(category)
                            .description(description)
                            .url(url)
                            .build())
                    .toList();
            // 返回结果
            return res;
        } catch (Exception e) {
            log.error("搜索内容图片错误：{}",e.getMessage());
        }
        return null;
    }

    /**
     * 搜索中等尺寸的图片列表
     *
     * @param query
     * @return
     */
    public List<String> searchMediumImages(String query) {
        // 设置请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", API_KEY);
        // 设置请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("query", query);
        params.put("per_page", 10);
        params.put("page", 1);
        // 发送 GET 请求
        String response = HttpUtil.createGet(API_URL)
                .addHeaders(headers)
                .form(params)
                .execute()
                .body();
        // 解析响应JSON
        return JSONUtil.parseObj(response)
                .getJSONArray("photos")
                .stream()
                .map(photoObj -> (JSONObject) photoObj)
                .map(photoObj -> photoObj.getJSONObject("src"))
                .map(photo -> photo.getStr("medium"))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }


}

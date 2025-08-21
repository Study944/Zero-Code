package com.zerocode.ai.tool;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 工具管理器
 */
@Slf4j
@Component
public class ToolManager {

    // 工具列表
    private static final Map<String, BaseTool> TOOL_LIST = new HashMap<>();

    // 注入工具
    @Resource
    private BaseTool[] tools;

    // 初始化工具列表
    @PostConstruct
    public void initToolList() {
        for (BaseTool tool : tools) {
            TOOL_LIST.put(tool.getToolName(), tool);
            log.info("已加载工具: {}", tool.getToolName());
        }
    }

    // 获取所有工具
    public BaseTool[] getAllTools() {
        return tools;
    }

    // 获取工具
    public BaseTool getTool(String toolName) {
        return TOOL_LIST.get(toolName);
    }
}

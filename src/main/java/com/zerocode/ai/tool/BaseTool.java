package com.zerocode.ai.tool;

import cn.hutool.json.JSONObject;

/**
 * 工具基类
 */
public abstract class BaseTool {

    public abstract String getToolName();

    public abstract String toolResponse(JSONObject arguments);

}

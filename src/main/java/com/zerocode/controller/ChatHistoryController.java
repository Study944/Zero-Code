package com.zerocode.controller;

import com.mybatisflex.core.paginate.Page;
import com.zerocode.common.BaseResponse;
import com.zerocode.common.ResultUtil;
import com.zerocode.domain.dto.ChatHistoryAddDTO;
import com.zerocode.domain.entity.User;
import com.zerocode.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.zerocode.domain.entity.ChatHistory;
import com.zerocode.service.ChatHistoryService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;
    @Resource
    private UserService userService;

    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * @param appId          应用ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param request        请求
     * @return 对话历史分页
     */
    @GetMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistory>> listChatHistory(@PathVariable Long appId,
                                                              @RequestParam(defaultValue = "10") int pageSize,
                                                              @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                              HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<ChatHistory> result = chatHistoryService.listChatHistory(appId, pageSize, lastCreateTime, loginUser);
        return ResultUtil.success(result);
    }


}

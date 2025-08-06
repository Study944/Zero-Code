package com.zerocode.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.zerocode.common.BaseResponse;
import com.zerocode.common.ResultUtil;
import com.zerocode.common.ThrowUtil;
import com.zerocode.domain.dto.*;
import com.zerocode.domain.entity.App;
import com.zerocode.domain.entity.User;
import com.zerocode.domain.vo.AppVO;
import com.zerocode.domain.vo.UserVO;
import com.zerocode.exception.ErrorCode;
import com.zerocode.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import com.zerocode.service.AppService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zerocode.constant.AppConstant.GOOD_PRIORITY;

/**
 * 应用 控制层。
 *
 * @author zxc
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;
    @Resource
    private UserService userService;

    /**
     * 添加应用
     *
     * @param appAddDTO
     * @param request
     * @return
     */
    @PostMapping("/addApp")
    public BaseResponse<Long> addApp(@RequestBody AppAddDTO appAddDTO, HttpServletRequest request) {
        // 参数校验
        ThrowUtil.throwIf(appAddDTO == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        // 添加应用
        Long appId = appService.addApp(appAddDTO, loginUser);
        return ResultUtil.success(appId);
    }

    /**
     * 删除应用
     *
     * @param appRemoveDTO
     * @param request
     * @return
     */
    @DeleteMapping("/removeApp")
    public BaseResponse<String> removeApp(@RequestBody AppRemoveDTO appRemoveDTO, HttpServletRequest request) {
        // 参数校验
        ThrowUtil.throwIf(appRemoveDTO == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        // 删除应用
        String res = appService.removeApp(appRemoveDTO, loginUser);
        return ResultUtil.success(res);
    }

    /**
     * 修改应用
     *
     * @param appUpdateDTO
     * @param request
     * @return
     */
    @PostMapping("/updateApp")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateDTO appUpdateDTO, HttpServletRequest request) {
        // 参数校验
        ThrowUtil.throwIf(appUpdateDTO == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        // 更新应用
        boolean res = appService.updateApp(appUpdateDTO, loginUser);
        return ResultUtil.success(res);
    }

    /**
     * 根据id查询应用
     *
     * @param id
     * @return
     */
    @GetMapping("/getAppById")
    public BaseResponse<AppVO> getAppById(@RequestParam("id") Long id) {
        // 参数校验
        ThrowUtil.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        // 获取应用
        AppVO appVO = appService.getAppById(id);
        return ResultUtil.success(appVO);
    }

    /**
     * 获取我的应用
     *
     * @param appListMyAppDTO
     * @param request
     * @return
     */
    @PostMapping("/list/my/app")
    public BaseResponse<Page<AppVO>> listMyApp(@RequestBody AppListMyAppDTO appListMyAppDTO, HttpServletRequest request) {
        // 参数校验
        ThrowUtil.throwIf(appListMyAppDTO == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        appListMyAppDTO.setUserId(loginUser.getId());
        // 获取应用
        Page<App> appPage = appService.page(
                new Page<>(appListMyAppDTO.getCurrent(), appListMyAppDTO.getPageSize()),
                appService.getQueryWrapper(appListMyAppDTO)
        );
        // 设置返回值
        Page<AppVO> appVOPage = new Page<>(appPage.getPageNumber(), appPage.getPageSize(), appPage.getTotalRow());
        List<AppVO> appVOList = appPage.getRecords().stream()
                .map(app -> {
                            // 构建创建的用户信息
                            AppVO appVO = BeanUtil.copyProperties(app, AppVO.class);
                            appVO.setUserVO(BeanUtil.copyProperties(loginUser, UserVO.class));
                            return appVO;
                        }
                )
                .toList();
        appVOPage.setRecords(appVOList);
        return ResultUtil.success(appVOPage);
    }

    /**
     * 获取公开应用列表
     *
     * @param appListMyAppDTO
     * @return
     */
    @PostMapping("/list/good/app")
    public BaseResponse<Page<AppVO>> listGoodAppVOByPage(@RequestBody AppListMyAppDTO appListMyAppDTO) {
        // 参数校验
        ThrowUtil.throwIf(appListMyAppDTO == null, ErrorCode.PARAMS_ERROR);
        appListMyAppDTO.setPriority(GOOD_PRIORITY);
        // 获取应用
        Page<App> appPage = appService.page(
                new Page<>(appListMyAppDTO.getCurrent(), appListMyAppDTO.getPageSize()),
                appService.getQueryWrapper(appListMyAppDTO)
        );
        // 设置返回值
        Page<AppVO> appVOPage = new Page<>(appPage.getPageNumber(), appPage.getPageSize(), appPage.getTotalRow());
        List<AppVO> appVOList = appPage.getRecords().stream()
                .map(app -> {
                            // 构建创建的用户信息
                            AppVO appVO = BeanUtil.copyProperties(app, AppVO.class);
                            User user = userService.getById(app.getUserId());
                            appVO.setUserVO(BeanUtil.copyProperties(user, UserVO.class));
                            return appVO;
                        }
                )
                .toList();
        appVOPage.setRecords(appVOList);
        return ResultUtil.success(appVOPage);
    }

    /**
     * 生成代码
     *
     * @return
     */
    @GetMapping(value = "/generate/code",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> generateCode(@RequestParam Long appId,
                                                      @RequestParam String userPrompt,
                                                      HttpServletRequest request) {
        // 参数校验
        User loginUser = userService.getLoginUser(request);
        // 生成代码
        AppGenerateCodeDTO appGenerateCodeDTO = new AppGenerateCodeDTO();
        appGenerateCodeDTO.setAppId(appId);
        appGenerateCodeDTO.setUserPrompt(userPrompt);
        Flux<String> stringFlux = appService.generateCode(appGenerateCodeDTO, loginUser);
        // 数据封装解决空格和换行丢失问题
        return stringFlux
                .map(chunk -> {
                            // 将内容封装为Json
                            Map<String, String> chunkMap = new HashMap<>();
                            chunkMap.put("d", chunk);
                            return ServerSentEvent.<String>builder()
                                    .data(JSONUtil.toJsonStr(chunkMap))
                                    .build();
                        }
                ).concatWith(Mono.just(
                        ServerSentEvent.<String>builder()
                                .event("done")
                                .data("")
                                .build()
                ));
    }

    /**
     * 部署应用
     * @param appDeployDTO
     * @param request
     * @return
     */
    @PostMapping("/deploy/my/app")
    public BaseResponse<String> deployMyApp(@RequestBody AppDeployDTO appDeployDTO, HttpServletRequest request) {
        ThrowUtil.throwIf(appDeployDTO == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        return ResultUtil.success(appService.deployMyApp(appDeployDTO, loginUser));
    }
}

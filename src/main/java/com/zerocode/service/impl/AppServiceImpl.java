package com.zerocode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zerocode.ai.entity.GeneratorTypeEnum;
import com.zerocode.common.ThrowUtil;
import com.zerocode.core.CodeGeneratorFacade;
import com.zerocode.core.builder.VueProjectBuilder;
import com.zerocode.core.handler.StreamHandlerExecutor;
import com.zerocode.domain.dto.*;
import com.zerocode.domain.entity.App;
import com.zerocode.domain.entity.User;
import com.zerocode.domain.enums.ChatHistoryMessageTypeEnum;
import com.zerocode.domain.vo.AppVO;
import com.zerocode.domain.vo.UserVO;
import com.zerocode.exception.ErrorCode;
import com.zerocode.mapper.AppMapper;
import com.zerocode.service.AppService;
import com.zerocode.service.ChatHistoryService;
import com.zerocode.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;

import static com.zerocode.constant.AppConstant.*;
import static com.zerocode.constant.UserConstant.ADMIN_ROLE;

/**
 * 应用服务层实现
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;
    @Resource
    private CodeGeneratorFacade codeGeneratorFacade;
    @Resource
    private ChatHistoryService chatHistoryService;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;
    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Override
    public Long addApp(AppAddDTO appAddDTO, User loginUser) {
        // 参数校验
        String initPrompt = appAddDTO.getInitPrompt();
        ThrowUtil.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化提示语不能为空");
        // 设置应用信息
        App app = new App();
        // 初始化应用名称为初始提示词的前15个字
        app.setAppName(initPrompt.substring(0, Math.min(14, initPrompt.length())));
        app.setInitPrompt(initPrompt);
        app.setUserId(loginUser.getId());
        app.setGenerateType(appAddDTO.getGenerateType().getValue());
        // 添加应用
        boolean save = this.save(app);
        ThrowUtil.throwIf(!save, ErrorCode.SYSTEM_ERROR, "添加应用失败");
        return app.getId();
    }

    @Override
    public String removeApp(AppRemoveDTO appRemoveDTO, User loginUser) {
        // 参数校验
        Long appId = appRemoveDTO.getId();
        ThrowUtil.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        App app = this.getById(appId);
        ThrowUtil.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtil.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR);
        // 删除应用及其对话历史
        transactionTemplate.execute(res->{
            boolean remove = this.removeById(appId);
            ThrowUtil.throwIf(!remove, ErrorCode.OPERATION_ERROR, "删除应用失败");
            boolean removeChatHistory = chatHistoryService.removeChatHistory(appId);
            ThrowUtil.throwIf(!removeChatHistory, ErrorCode.OPERATION_ERROR, "删除应用对话历史失败");
            return true;
        });
        return "删除成功";
    }

    @Override
    public boolean updateApp(AppUpdateDTO appUpdateDTO, User loginUser) {
        // 参数校验
        Long appId = appUpdateDTO.getId();
        String appName = appUpdateDTO.getAppName();
        ThrowUtil.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtil.throwIf(appName.isEmpty(), ErrorCode.PARAMS_ERROR);
        App app = this.getById(appId);
        ThrowUtil.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtil.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR);
        // 更新应用
        App newApp = new App();
        newApp.setId(appId);
        newApp.setAppName(appName);
        if (loginUser.getUserRole().equals(ADMIN_ROLE)) newApp.setPriority(appUpdateDTO.getPriority());
        boolean update = this.updateById(newApp);
        return update;
    }

    @Override
    public AppVO getAppById(Long id) {
        // 参数校验
        App app = this.getById(id);
        ThrowUtil.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 设置返回数据
        User user = userService.getById(app.getUserId());
        AppVO appVO = BeanUtil.copyProperties(app, AppVO.class);
        appVO.setUserVO(BeanUtil.copyProperties(user, UserVO.class));
        return appVO;
    }


    @Override
    public QueryWrapper getQueryWrapper(AppListMyAppDTO appListMyAppDTO) {
        // 查询信息
        String sortField = appListMyAppDTO.getSortField();
        String sortOrder = appListMyAppDTO.getSortOrder();
        Long id = appListMyAppDTO.getId();
        String appName = appListMyAppDTO.getAppName();
        String appDesc = appListMyAppDTO.getAppDesc();
        String initPrompt = appListMyAppDTO.getInitPrompt();
        Long userId = appListMyAppDTO.getUserId();
        String generateType = appListMyAppDTO.getGenerateType();
        Integer priority = appListMyAppDTO.getPriority();
        String deployKey = appListMyAppDTO.getDeployKey();
        // 构建查询条件
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(App::getId, id, id != null)
                .like(App::getAppName, appName, StrUtil.isNotBlank(appName))
                .like(App::getAppDesc, appDesc, StrUtil.isNotBlank(appDesc))
                .like(App::getInitPrompt, initPrompt, StrUtil.isNotBlank(initPrompt))
                .eq(App::getUserId, userId, userId != null)
                .like(App::getGenerateType, generateType, StrUtil.isNotBlank(generateType))
                .eq(App::getPriority, priority, priority != null)
                .like(App::getDeployKey, deployKey, StrUtil.isNotBlank(deployKey));
        // 只有当排序字段不为空时才添加排序条件
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        }
        return queryWrapper;
    }


    @Override
    public Flux<String> generateCode(AppGenerateCodeDTO appGenerateCodeDTO, User loginUser) {
        // 参数校验
        Long appId = appGenerateCodeDTO.getAppId();
        ThrowUtil.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        String userPrompt = appGenerateCodeDTO.getUserPrompt();
        ThrowUtil.throwIf(StrUtil.isBlank(userPrompt), ErrorCode.PARAMS_ERROR, "用户提示语不能为空");
        GeneratorTypeEnum generatorTypeEnum = appGenerateCodeDTO.getGeneratorTypeEnum();
        //身份校验
        App app = this.getById(appId);
        ThrowUtil.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        ThrowUtil.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR);
        // 保存对话历史--用户输入userPrompt
        chatHistoryService.addChatMessage(appId, userPrompt,
                ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
        // AI生成应用
        Flux<String> stringFlux = codeGeneratorFacade.generateAndSaveStreamCode(userPrompt, generatorTypeEnum, appId);
        // 保存对话历史--AI生成代码
        Flux<String> doneExecute = streamHandlerExecutor.doExecute(stringFlux, chatHistoryService, appId, loginUser, generatorTypeEnum);
        return doneExecute;
    }

    @Override
    public String deployMyApp(AppDeployDTO appDeployDTO, User loginUser) {
        // 参数校验
        Long appId = appDeployDTO.getAppId();
        ThrowUtil.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        App app = this.getById(appId);
        ThrowUtil.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        ThrowUtil.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR);
        // 寻找应用路径
        String generateType = app.getGenerateType();
        String appPath = APP_PATH + File.separator + generateType + "_" + appId;
        File appFile = new File(appPath);
        ThrowUtil.throwIf(!appFile.exists(), ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 判断应用是否已部署
        String appDeployKey = app.getDeployKey();
        if (appDeployKey != null){
            // 判断部署路径是否存在
            File deployFile = new File(DEPLOY_PATH + File.separator + appDeployKey);
            if (deployFile.exists()) return String.format("%s/%s", DEPLOY_HOST, appDeployKey);
        }
        // 生成部署密钥 -- 6位字母数字随机数
        String deployKey = RandomUtil.randomString(6);
        // 文件复制
        GeneratorTypeEnum generatorType = GeneratorTypeEnum.getByValue(app.getGenerateType());
        if (generatorType == GeneratorTypeEnum.VUE_PROJECT) {
            // Vue 项目需要构建
            boolean buildSuccess = vueProjectBuilder.buildProject(appPath);
            ThrowUtil.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败，请重试");
            // 检查 dist 目录是否存在
            File distDir = new File(appPath, "dist");
            ThrowUtil.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");
            // 构建完成后，需要将构建后的文件复制到部署目录
            appFile = distDir;
        }
        FileUtil.copyContent(appFile, new File(DEPLOY_PATH + File.separator + deployKey), true);
        // 更新应用部署字段
        App newApp = new App();
        newApp.setId(appId);
        newApp.setDeployKey(deployKey);
        newApp.setDeployTime(LocalDateTime.now());
        boolean update = this.updateById(newApp);
        ThrowUtil.throwIf(!update, ErrorCode.OPERATION_ERROR, "更新应用部署字段失败");
        // 返回部署路径
        return String.format("%s/%s", DEPLOY_HOST, deployKey);
    }

}
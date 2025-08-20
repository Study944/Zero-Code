package com.zerocode.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.zerocode.domain.dto.*;
import com.zerocode.domain.entity.App;
import com.zerocode.domain.entity.User;
import com.zerocode.domain.vo.AppVO;
import jakarta.servlet.http.HttpServletResponse;
import reactor.core.publisher.Flux;

/**
 * 应用 服务层。
 *
 * @author zxc
 */
public interface AppService extends IService<App> {

    Long addApp(AppAddDTO appAddDTO, User loginUser);

    String removeApp(AppRemoveDTO appRemoveDTO, User loginUser);

    boolean updateApp(AppUpdateDTO appUpdateDTO, User loginUser);

    AppVO getAppById(Long id);

    QueryWrapper getQueryWrapper(AppListMyAppDTO appListMyAppDTO);

    Flux<String> generateCode(AppGenerateCodeDTO appGenerateCodeDTO, User loginUser);

    String deployMyApp(AppDeployDTO appDeployDTO, User loginUser);

    Boolean downloadProject(Long appId, User loginUser, HttpServletResponse response);
}

package com.zerocode.aop;

import com.zerocode.annotation.UserRole;
import com.zerocode.common.ThrowUtil;
import com.zerocode.domain.entity.User;
import com.zerocode.domain.enums.UserEnums;
import com.zerocode.exception.ErrorCode;
import com.zerocode.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 角色权限切面
 */
@Aspect
@Component
public class UserRoleAOP {

    @Resource
    private UserService userService;

    /**
     * 环绕通知--在拦截点执行方法前后执行
     * @param joinPoint
     */
    @Around("@annotation(userRole)")
    public Object authCheck(ProceedingJoinPoint joinPoint, UserRole userRole) throws Throwable {
        // 1.获取当前登录用户，判断是否登录
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User loginUser = userService.getLoginUser(request);
        ThrowUtil.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 2.获取当前用户权限
        UserEnums enumsByValue = UserEnums.getEnumByValue(loginUser.getUserRole());
        UserEnums enumByParam = UserEnums.getEnumByValue(userRole.role());
        // 3.注解上权限为管理员，校验用户权限
        if (enumByParam != null) {
            ThrowUtil.throwIf(enumByParam == UserEnums.ADMIN && enumsByValue != UserEnums.ADMIN,
                    ErrorCode.NO_AUTH_ERROR);
        }
        // 4.注解上权限为普通用户，只要用户已登录，则通过
        return joinPoint.proceed();
    }
}

package com.zerocode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zerocode.common.ThrowUtil;
import com.zerocode.domain.dto.UserAddDTO;
import com.zerocode.domain.dto.UserQueryDTO;
import com.zerocode.domain.dto.UserUpdateDTO;
import com.zerocode.domain.entity.User;
import com.zerocode.domain.vo.UserLoginVO;
import com.zerocode.domain.vo.UserVO;
import com.zerocode.exception.ErrorCode;
import com.zerocode.mapper.UserMapper;
import com.zerocode.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import static com.zerocode.constant.UserConstant.ADMIN_ROLE;
import static com.zerocode.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务层实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public Long register(String account, String password, String checkPassword) {
        //1.参数校验
        ThrowUtil.throwIf(account == null || account.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号错误");
        ThrowUtil.throwIf(password == null || password.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码错误");
        ThrowUtil.throwIf(checkPassword == null || checkPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码错误");
        ThrowUtil.throwIf(!password.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次密码不一致");
        //2.查询用户账号是否存在
        QueryWrapper eq = query().eq(User::getUserAccount, account);
        ThrowUtil.throwIf(this.exists(eq), ErrorCode.PARAMS_ERROR, "用户已存在");
        //3.创建用户
        User user = new User();
        user.setUserAccount(account);
        user.setUserPassword(getEncryptPassword(password));
        boolean save = this.save(user);
        ThrowUtil.throwIf(!save, ErrorCode.SYSTEM_ERROR, "注册失败");
        //4.返回用户id
        return user.getId();
    }

    @Override
    public UserLoginVO login(String account, String password, HttpServletRequest request) {
        // 1. 校验
        ThrowUtil.throwIf(account == null || account.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号错误");
        ThrowUtil.throwIf(password == null || password.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码错误");
        // 2. 加密
        String encryptPassword = getEncryptPassword(password);
        // 查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_account", account);
        queryWrapper.eq("user_password", encryptPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        // 用户不存在
        ThrowUtil.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户不存在");
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 4. 获得脱敏后的用户信息
        return BeanUtil.copyProperties(user, UserLoginVO.class);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 获取Session会话信息
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        // 获取用户信息
        User user = (User) userObj;
        ThrowUtil.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        // 获取最新的用户信息
        user = this.getById(user.getId());
        ThrowUtil.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户不存在");
        return user;
    }

    @Override
    public boolean logout(HttpServletRequest request) {
        // 判断登录状态
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        ThrowUtil.throwIf(userObj == null, ErrorCode.NOT_LOGIN_ERROR);
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public User addUser(UserAddDTO userAddDTO) {
        User user = BeanUtil.copyProperties(userAddDTO, User.class);
        user.setUserPassword(getEncryptPassword(user.getUserPassword()));
        boolean save = this.save(user);
        ThrowUtil.throwIf(!save, ErrorCode.SYSTEM_ERROR, "添加用户失败");
        return this.getById(user.getId());
    }

    @Override
    public UserVO updateUser(UserUpdateDTO userUpdateDTO, User loginUser) {
        // 参数校验
        Long userId = userUpdateDTO.getId();
        ThrowUtil.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR);
        // 获取用户
        User user = this.getById(userId);
        ThrowUtil.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户不存在");
        // 只有管理员或者当前用户可以修改
        ThrowUtil.throwIf(!userId.equals(loginUser.getId()) && !loginUser.getUserRole().equals(ADMIN_ROLE),
                ErrorCode.NO_AUTH_ERROR, "无权限");
        // 更新用户
        BeanUtil.copyProperties(userUpdateDTO, user);
        boolean update = this.updateById(user);
        ThrowUtil.throwIf(!update, ErrorCode.SYSTEM_ERROR, "更新用户失败");
        return BeanUtil.copyProperties(user, UserVO.class);
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryDTO userQueryDTO) {
        // 查询信息
        String sortField = userQueryDTO.getSortField();
        String sortOrder = userQueryDTO.getSortOrder();
        Long id = userQueryDTO.getId();
        String userAccount = userQueryDTO.getUserAccount();
        String userProfile = userQueryDTO.getUserProfile();
        String userName = userQueryDTO.getUserName();
        String userRole = userQueryDTO.getUserRole();
        // 创建查询条件
        return QueryWrapper.create()
                .eq(User::getId, id)
                .eq(User::getUserAccount, userAccount)
                .eq(User::getUserRole, userRole)
                .like(User::getUserProfile, userProfile)
                .like(User::getUserName, userName)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    /**
     * 加密
     *
     * @param password 原始密码
     */
    public String getEncryptPassword(String password) {
        String salt = "zxc";
        return DigestUtils.md5DigestAsHex((salt + password).getBytes());
    }
}

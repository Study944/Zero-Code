package com.zerocode.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.zerocode.domain.dto.UserAddDTO;
import com.zerocode.domain.dto.UserQueryDTO;
import com.zerocode.domain.dto.UserUpdateDTO;
import com.zerocode.domain.entity.User;
import com.zerocode.domain.vo.UserLoginVO;
import com.zerocode.domain.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户服务层
 */
public interface UserService extends IService<User> {

    Long register(String account, String password, String checkPassword);

    UserLoginVO login(String account, String password, HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);

    boolean logout(HttpServletRequest request);

    User addUser(UserAddDTO userAddDTO);

    UserVO updateUser(UserUpdateDTO userUpdateDTO, User loginUser);

    QueryWrapper getQueryWrapper(UserQueryDTO userQueryDto);
}

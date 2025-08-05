package com.zerocode.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.zerocode.annotation.UserRole;
import com.zerocode.common.BaseResponse;
import com.zerocode.common.ResultUtil;
import com.zerocode.common.ThrowUtil;
import com.zerocode.domain.dto.*;
import com.zerocode.domain.vo.UserLoginVO;
import com.zerocode.domain.vo.UserVO;
import com.zerocode.exception.ErrorCode;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import com.zerocode.domain.entity.User;
import com.zerocode.service.UserService;

import java.util.List;

/**
 * 用户控制层
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterDto 注册信息
     * @return 注册成功用户id
     */
    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody UserRegisterDto userRegisterDto) {
        // 参数校验
        ThrowUtil.throwIf(userRegisterDto == null, ErrorCode.PARAMS_ERROR);
        String account = userRegisterDto.getUserAccount();
        String password = userRegisterDto.getUserPassword();
        String checkPassword = userRegisterDto.getCheckPassword();
        // 用户注册
        Long userId = userService.register(account, password, checkPassword);
        return ResultUtil.success(userId);
    }

    /**
     * 用户登录
     *
     * @param userLoginDTO 登录信息
     * @param request      请求
     * @return 登录成功用户信息(脱敏)
     */
    @PostMapping("/login")
    public BaseResponse<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO, HttpServletRequest request) {
        // 参数校验
        ThrowUtil.throwIf(userLoginDTO == null, ErrorCode.PARAMS_ERROR);
        String account = userLoginDTO.getUserAccount();
        String password = userLoginDTO.getUserPassword();
        // 用户登录
        UserLoginVO result = userService.login(account, password, request);
        return ResultUtil.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     */
    @GetMapping("/getLoginUser")
    public BaseResponse<UserLoginVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        UserLoginVO result = BeanUtil.copyProperties(user, UserLoginVO.class);
        return ResultUtil.success(result);
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @GetMapping("/logout")
    public BaseResponse<String> logout(HttpServletRequest request) {
        boolean result = userService.logout(request);
        return ResultUtil.success(result ? "注销成功" : "注销失败");
    }

    /**
     * 添加用户
     * @param userAddDTO
     * @return
     */
    @PostMapping("/admin/add")
    @UserRole(role = "admin")
    public BaseResponse<User> addUser(@RequestBody UserAddDTO userAddDTO) {
        // 参数校验
        ThrowUtil.throwIf(userAddDTO == null, ErrorCode.PARAMS_ERROR, "参数为空");
        User result = userService.addUser(userAddDTO);
        return ResultUtil.success(result);
    }

    /**
     * 删除用户
     *
     * @param id
     */
    @PostMapping("/admin/delete")
    @UserRole(role = "admin")
    public BaseResponse<Boolean> deleteUser(@RequestParam Long id) {
        // 参数校验
        ThrowUtil.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.removeById(id);
        return ResultUtil.success(result);
    }


    /**
     * 更新用户
     *
     * @param userUpdateDTO 修改信息
     * @param request       请求
     * @return 修改用户信息
     */
    @PostMapping("/update")
    @UserRole(role = "user")
    public BaseResponse<UserVO> updateUser(@RequestBody UserUpdateDTO userUpdateDTO, HttpServletRequest request) {
        // 参数校验
        ThrowUtil.throwIf(userUpdateDTO == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        // 更新
        UserVO result = userService.updateUser(userUpdateDTO, loginUser);
        return ResultUtil.success(result);
    }

    /**
     * 根据id获取用户
     *
     * @param id
     */
    @PostMapping("/getById")
    public BaseResponse<UserVO> getUser(@RequestParam Long id) {
        // 判空校验
        ThrowUtil.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        User userById = userService.getById(id);
        ThrowUtil.throwIf(userById == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        UserVO userVO = BeanUtil.copyProperties(userById, UserVO.class);
        return ResultUtil.success(userVO);
    }


    /**
     * 分页获取用户列表
     * @param userQueryDTO
     * @return
     */
    @PostMapping("/admin/page")
    @UserRole(role = "admin")
    public BaseResponse<Page<UserVO>> getUserPage(@RequestBody UserQueryDTO userQueryDTO) {
        Page<User> page = userService.page(
                new Page<>(userQueryDTO.getCurrent(), userQueryDTO.getPageSize()),
                userService.getQueryWrapper(userQueryDTO));
        // 数据脱敏
        Page<UserVO> userVOPage = new Page<>(page.getPageSize(), page.getPageSize(), page.getTotalRow());
        List<UserVO> userVOList = page.getRecords().stream()
                .map(user -> BeanUtil.copyProperties(user, UserVO.class))
                .toList();
        userVOPage.setRecords(userVOList);
        return ResultUtil.success(userVOPage);
    }

}

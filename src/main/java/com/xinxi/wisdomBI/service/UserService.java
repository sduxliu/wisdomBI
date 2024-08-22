package com.xinxi.wisdomBI.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xinxi.wisdomBI.common.BaseResponse;
import com.xinxi.wisdomBI.model.dto.user.UserEditPasswordRequest;
import com.xinxi.wisdomBI.model.dto.user.UserQueryRequest;
import com.xinxi.wisdomBI.model.dto.user.UserRegisterRequest;
import com.xinxi.wisdomBI.model.entity.User;
import com.xinxi.wisdomBI.model.vo.LoginUserVO;
import com.xinxi.wisdomBI.model.vo.UserVO;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求体
     * @return 新用户 id
     */
    long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);



    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     *用户手机号验证登录
     */
    LoginUserVO userLoginByPhone(String userPhone, String captcha, HttpServletRequest request);

    /**
     * 发送验证码
     *
     * @param phone 手机号
     * @return 返回结果
     */
    BaseResponse<String> sendCaptcha(String phone);

    /**
     * 更新密码
     * @param id userid
     * @param        userEditPasswordRequest 更新信息
     * @return boolean
     */
    boolean updatePassword(Long id, UserEditPasswordRequest userEditPasswordRequest);

    /**
     * 根据手机号获取用户信息
     * @param userPhone 手机号
     * @return User
     */
    User getByUserPhone(String userPhone);
}

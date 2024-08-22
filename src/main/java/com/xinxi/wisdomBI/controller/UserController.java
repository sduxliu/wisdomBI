package com.xinxi.wisdomBI.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinxi.wisdomBI.common.BaseResponse;
import com.xinxi.wisdomBI.common.DeleteRequest;
import com.xinxi.wisdomBI.common.ErrorCode;
import com.xinxi.wisdomBI.common.ResultUtils;
import com.xinxi.wisdomBI.constant.RedisKeyPrefixConstant;
import com.xinxi.wisdomBI.constant.UserConstant;
import com.xinxi.wisdomBI.exception.BusinessException;
import com.xinxi.wisdomBI.exception.ThrowUtils;
import com.xinxi.wisdomBI.model.dto.user.*;
import com.xinxi.wisdomBI.model.entity.User;
import com.xinxi.wisdomBI.model.enums.UserRoleEnum;
import com.xinxi.wisdomBI.model.vo.LoginUserVO;
import com.xinxi.wisdomBI.model.vo.UserVO;
import com.xinxi.wisdomBI.service.UserService;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import static com.sun.javafx.font.FontResource.SALT;
import static com.xinxi.wisdomBI.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author 蒲月理想
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    // region 登录相关

    /**
     * 发送验证码
     */
    @PostMapping("/sendCaptcha")
    public BaseResponse<String> sendCaptcha(@RequestBody UserSendCaptchaRequest userSendCaptchaRequest,
            HttpServletRequest request) {
//        // 查看登录
//        // 先判断是否已登录
//        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
//        User currentUser = (User) userObj;
//        if(currentUser !=null){
//            if(!Objects.equals(currentUser.getUserPhone(),userSendCaptchaRequest.getUserPhone())){
//                throw new BusinessException(ErrorCode.PARAMS_ERROR,"该手机号未与当前账户绑定");
//            }
//        }
        // 手机号校验
        String phone = userSendCaptchaRequest.getUserPhone();
        // 手机号格式校验
        log.info("phone:{}",phone);
        String phoneRegex = "1([3-9])[0-9]{9}";
        if (!phone.matches(phoneRegex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
        }
        //  发送验证码
        return userService.sendCaptcha(phone);

    }

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String phone = userRegisterRequest.getUserPhone();
        String captcha = userRegisterRequest.getCaptcha();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, phone,captcha)) {
          throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        long result = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录--账号密码
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        if(Objects.equals(loginUserVO.getUserRole(), "banned")){
            // 封号
            throw new BusinessException(ErrorCode.INVALID_USER);
        }
        return ResultUtils.success(loginUserVO);
    }
    /**
     * 用户登录--手机验证登录
     *
     * @param userLoginByPhoneRequest
     * @param request
     * @return
     */
    @PostMapping("/login/phone")
    public BaseResponse<LoginUserVO> userLoginByPhone(@RequestBody UserLoginByPhoneRequest userLoginByPhoneRequest, HttpServletRequest request) {
        if (userLoginByPhoneRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userPhone = userLoginByPhoneRequest.getUserPhone();
        String captcha = userLoginByPhoneRequest.getCaptcha();
        if (StringUtils.isAnyBlank(userPhone, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLoginByPhone(userPhone, captcha, request);
        if(Objects.equals(loginUserVO.getUserRole(), "banned")){
            // 封号
            throw new BusinessException(ErrorCode.INVALID_USER);
        }
        return ResultUtils.success(loginUserVO);
    }


    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    // endregion

    // region 增删改查

    /**
     * 创建用户
     *
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 默认密码设置为12345678
        String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + "12345678").getBytes());
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除单个用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 校验用户权限：
         if(!userService.isAdmin(request)){
             throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
         }
        // 需要加锁: 防止多个用户同时删除一个用户——使用Redisson
        User user = userService.getLoginUser(request);
        RLock lock = redissonClient.getLock(RedisKeyPrefixConstant.MUTEX_LOCK_KEY);
        try {
            if(lock.tryLock(0, -1, TimeUnit.MILLISECONDS)){
                log.info("获取到锁的用户：" + user.getId()+" 进程："+Thread.currentThread().getId());
                // 执行删除操作
                boolean b = userService.removeById(deleteRequest.getId());
                return ResultUtils.success(b);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            // 释放锁
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
        // 删除失败；获取锁失败
        return ResultUtils.success(false);
    }

    /**
     * 删除多个用户
     */
    @PostMapping("/delete/ids")
    @Transactional
    public BaseResponse<Boolean> deleteUserBatch(@RequestBody UsersDeleteRequest usersDeleteRequest, HttpServletRequest request) {
        if (usersDeleteRequest == null || usersDeleteRequest.getIds().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 校验用户权限：
        if(!userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 批量删除用户:
        // 需要加锁: 防止多个用户同时删除一个用户——使用Redisson
        User user = userService.getLoginUser(request);
        RLock lock = redissonClient.getLock(RedisKeyPrefixConstant.MUTEX_LOCK_KEY);
        try {
            if(lock.tryLock(0, -1, TimeUnit.MILLISECONDS)){
                log.info("获取到锁的用户：" + user.getId()+" 进程："+Thread.currentThread().getId());
            // 执行删除操作
                boolean b = userService.removeByIds(usersDeleteRequest.getIds());
                return ResultUtils.success(b);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            // 释放锁
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
        // 删除失败；获取锁失败
        return ResultUtils.success(false);
    }
    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
                                            HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 从请求中获取用户信息
        if(!userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        User user = new User();
        RLock lock = redissonClient.getLock(RedisKeyPrefixConstant.MUTEX_LOCK_KEY);
        try {
            if(lock.tryLock(0, -1, TimeUnit.MILLISECONDS)){
                log.info("获取到锁的进程："+Thread.currentThread().getId());
                BeanUtils.copyProperties(userUpdateRequest, user);
                boolean result = userService.updateById(user);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
                return ResultUtils.success(true);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            // 释放锁
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
       // 添加失败
        throw new BusinessException(ErrorCode.OPERATION_ERROR,"修改失败,请稍后再试");
    }
    /**
     * 用户修改M密码
     */
    @PostMapping("/update/password")
    public BaseResponse<Boolean> updatePassword(@RequestBody UserEditPasswordRequest userEditPasswordRequest,
                                                 HttpServletRequest request){
        if (userEditPasswordRequest == null || userEditPasswordRequest.getUserPassword() == null
                || StringUtils.isAnyBlank(userEditPasswordRequest.getUserPassword(), userEditPasswordRequest.getCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码有误");
        }
        User user = userService.getLoginUser(request);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if(!Objects.equals(user.getUserPhone(), userEditPasswordRequest.getUserPhone())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"非本账号绑定手机号，无法验证身份");
        }
        log.info("user:{}",user);
        boolean result = userService.updatePassword(user.getId(), userEditPasswordRequest);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
    /**
     * 用户忘记密码
     */
    @ApiOperation("用户忘记密码")
    @PostMapping("/forget/password")
    public BaseResponse<Boolean> forgetPassword
            (@RequestBody UserEditPasswordRequest userEditPasswordRequest, HttpServletRequest request) {

        if (userEditPasswordRequest == null || userEditPasswordRequest.getUserPassword() == null
                || StringUtils.isAnyBlank(userEditPasswordRequest.getUserPassword(), userEditPasswordRequest.getCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码有误");
        }
        User user = userService.getByUserPhone(userEditPasswordRequest.getUserPhone());
        if(user == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"该手机未注册！");
        }
        boolean result = userService.updatePassword(user.getId(), userEditPasswordRequest);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
        BaseResponse<User> response = getUserById(id, request);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                   HttpServletRequest request) {
       if(!userService.isAdmin(request)){
           // 非管理员用户不能查询全部用户得所有信息
           throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
       }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        // 查询结果去除自己：
        User loginUser = userService.getLoginUser(request);
        userPage.getRecords().removeIf(user -> user.getId().equals(loginUser.getId()));
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                       HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }

    // endregion

    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest
     * @param request
     * @return
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
                                              HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
}

package com.xinxi.wisdomBI.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xinxi.wisdomBI.common.BaseResponse;
import com.xinxi.wisdomBI.common.ErrorCode;
import com.xinxi.wisdomBI.common.ResultUtils;
import com.xinxi.wisdomBI.constant.CommonConstant;
import com.xinxi.wisdomBI.exception.BusinessException;
import com.xinxi.wisdomBI.mapper.UserMapper;
import com.xinxi.wisdomBI.model.dto.user.UserEditPasswordRequest;
import com.xinxi.wisdomBI.model.dto.user.UserQueryRequest;
import com.xinxi.wisdomBI.model.dto.user.UserRegisterRequest;
import com.xinxi.wisdomBI.model.entity.User;
import com.xinxi.wisdomBI.model.enums.UserRoleEnum;
import com.xinxi.wisdomBI.model.vo.LoginUserVO;
import com.xinxi.wisdomBI.model.vo.UserVO;
import com.xinxi.wisdomBI.service.UserService;
import com.xinxi.wisdomBI.utils.AliSMSUtils;
import com.xinxi.wisdomBI.utils.RegexUtils;
import com.xinxi.wisdomBI.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.xinxi.wisdomBI.constant.RedisKeyPrefixConstant.CAPTCHA_KEY;
import static com.xinxi.wisdomBI.constant.UserConstant.SALT;
import static com.xinxi.wisdomBI.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现
 *
 * @author 蒲月理想
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private AliSMSUtils aliSMSUtils;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    /**
     * 用户注册
     * @param userRegisterRequest   用户账户
     * @return long
     */
    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        // 1. 校验
        if (userRegisterRequest.getUserAccount().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userRegisterRequest.getUserPassword().length() < 8 || userRegisterRequest.getCheckPassword().length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 手机号格式校验
        String phoneRegex = "1([3-9])[0-9]{9}";
        if (!userRegisterRequest.getUserPhone().matches(phoneRegex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
        }
        // 密码和校验密码不相同
        if (!userRegisterRequest.getUserPassword().equals(userRegisterRequest.getCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        //2. 验证码功能实现——校验:
        String checkCaptcha = (String) redisTemplate.opsForValue().get(CAPTCHA_KEY + userRegisterRequest.getUserPhone());
        String captcha = userRegisterRequest.getCaptcha();
        if (!captcha.equals(checkCaptcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        synchronized (userRegisterRequest.getUserAccount().intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userRegisterRequest.getUserAccount());
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            //手机号不能重复注册
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userPhone", userRegisterRequest.getUserPhone());
            count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号已被注册");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userRegisterRequest.getUserPassword()).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userRegisterRequest.getUserAccount());
            user.setUserPhone(userRegisterRequest.getUserPhone());
            user.setUserPassword(encryptPassword);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    /**
     * 用户登录
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        return getLoginUserVO(request, queryWrapper);
    }

    /**
     * 用户手机号验证登录
     * @param userPhone
     * @param captcha
     * @param request
     * @return
     */
    @Override
    public LoginUserVO userLoginByPhone(String userPhone, String captcha, HttpServletRequest request) {
        // 1. 校验
        // 校验手机号格式
        if (!RegexUtils.isPhone(userPhone)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
        }
        // 校验验证码
        if (!RegexUtils.isVerificationCode(captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码格式错误");
        }
        // 2. 校验验证码
        // 2.1 到Redis获取验证码
        String checkCaptcha = (String) redisTemplate.opsForValue().get(CAPTCHA_KEY + userPhone);
        // 2.2 校验验证码
        if (checkCaptcha != null && !checkCaptcha.equals(captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        // 验证码核对成功；删除
        redisTemplate.delete(CAPTCHA_KEY + userPhone);
        // 3. 查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userPhone", userPhone);
        return getLoginUserVO(request, queryWrapper);
    }

    @Override
    public BaseResponse<String> sendCaptcha(String phone) {
            // 1. 从Redis中获取验证码
            String code = (String) redisTemplate.opsForValue().get(CAPTCHA_KEY+ phone);
            log.info("获取到的验证码,{}", code);
            if (code == null) {
                // 2. 如果Redis中没有验证码记录，生成一个新的六位随机验证码
                String random = RandomUtil.randomNumbers(6);
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("code", random);
                // 3. 将新生成的验证码存储在Redis中，设置有效期为5分钟
                redisTemplate.opsForValue().set(CAPTCHA_KEY+ phone, random, 5, TimeUnit.MINUTES);
                // 4. 调用阿里云接口发送短信，将验证码发送给指定的手机号码
                boolean b = aliSMSUtils.sendSms(phone, hashMap);
                // 测试时打开
//                boolean b = true;
                log.info("短信发送状态,{},发送的验证码为:{}", b,random);
                if(!b) {
                    // 4.1 发送失败删除：
                    redisTemplate.delete(CAPTCHA_KEY+ phone);
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "短信发送失败");
                }
              return ResultUtils.success("发送成功");
            } else {
                // 5. 如果Redis中已存在验证码，说明已经发送过验证码，返回一个成功的响应，提示用户不要重复发送
                return ResultUtils.success("请不要重复发送验证码");
            }
    }

    @Override
    public boolean updatePassword(Long id, UserEditPasswordRequest userEditPasswordRequest) {
        // 1.1 判断两次输入的密码是否一致
       String userPassword = userEditPasswordRequest.getUserPassword();
       String checkPassword = userEditPasswordRequest.getCheckPassword();
       String captcha = userEditPasswordRequest.getCaptcha();
       String phone = userEditPasswordRequest.getUserPhone();
       if(!userPassword.equals(checkPassword)) {
           throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
       }
       if(userPassword.length() < 8){
           throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能小于8位");
       }
       // 1.2  验证码校验
       if(!RegexUtils.isVerificationCode(captcha)){
           throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码格式错误");
       }
       // 1.3 手机号校验
        if(!RegexUtils.isPhone(phone)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
        }
        //
        // 2. 查询Redis中的验证码并校验
        String checkCaptcha = (String) redisTemplate.opsForValue().get(CAPTCHA_KEY+ phone);
        if (checkCaptcha != null && !checkCaptcha.equals(captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        // 3. 获取用户
        User user = this.getById(id);
        // 4. 加密密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        if(encryptPassword.equals(user.getUserPassword())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码不能与旧密码相同");
        }
        user.setUserPassword(encryptPassword);
        // 5 . 更新密码
       return updateById(user);
    }

    /**
     * 根据手机号获取用户
     * @param userPhone 手机号
     * @return 账户
     */
    @Override
    public User getByUserPhone(String userPhone) {
       return this.getOne(new QueryWrapper<User>().eq("userPhone", userPhone));
    }

    private LoginUserVO getLoginUserVO(HttpServletRequest request, QueryWrapper<User> queryWrapper) {
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        if(user== null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


}

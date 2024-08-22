package com.xinxi.wisdomBI.aop;

import com.xinxi.wisdomBI.annotation.AuthCheck;
import com.xinxi.wisdomBI.common.ErrorCode;
import com.xinxi.wisdomBI.exception.BusinessException;
import com.xinxi.wisdomBI.model.entity.User;
import com.xinxi.wisdomBI.model.enums.UserRoleEnum;
import com.xinxi.wisdomBI.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 权限校验 AOP
 *
 * @author 蒲月理想
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param joinPoint
     * @param authCheck
     * @return
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 可任意访问页面排除：登录页(手机登录和密码登录），注册页，忘记密码页
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/user/login")){
            return joinPoint.proceed();
        }
        if (requestURI.startsWith("/user/register")){
            return joinPoint.proceed();
        }
        if (requestURI.startsWith("/user/forget")){
            return joinPoint.proceed();
        }
        if (requestURI.startsWith("/user/sendCaptcha")){
            return joinPoint.proceed();
        }
        // 当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 未登录
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 如果被封号，直接拒绝
        if (UserRoleEnum.BAN.getValue().equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.INVALID_USER);
        }
        // 必须有该权限才通过
        if (StringUtils.isNotBlank(mustRole)) {
            UserRoleEnum mustUserRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
            if (mustUserRoleEnum == null) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            String userRole = loginUser.getUserRole();
            // 如果被封号，直接拒绝
            if (UserRoleEnum.BAN.equals(mustUserRoleEnum)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            // 必须有管理员权限
            if (UserRoleEnum.ADMIN.equals(mustUserRoleEnum)) {
                if (!mustRole.equals(userRole)) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
                }
            }
        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}


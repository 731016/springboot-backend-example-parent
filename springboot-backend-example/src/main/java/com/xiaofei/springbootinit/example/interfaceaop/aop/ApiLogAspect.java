package com.xiaofei.springbootinit.example.interfaceaop.aop;

import cn.hutool.json.JSONUtil;
import com.xiaofei.springbootinit.example.interfaceaop.mapper.ApiLogMapper;
import com.xiaofei.springbootinit.example.interfaceaop.model.ApiLog;
import com.xiaofei.springbootinit.model.entity.User;
import com.xiaofei.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

import static com.xiaofei.springbootinit.utils.NetUtils.getIpAddress;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/23
 */
@Aspect
@Component
@Slf4j
public class ApiLogAspect {

    @Resource
    private ApiLogMapper apiLogMapper;

    @Resource
    private UserService userService;

    @Pointcut("@annotation(com.xiaofei.springbootinit.example.interfaceaop.annotation.ApiLog)")
    public void apiLogPointcut() {
    }

    @Around("apiLogPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        User loginUser = userService.getLoginUser(request);

        // 记录请求信息
        String url = request.getRequestURL().toString();
        String httpMethod = request.getMethod();
        String ip = getIpAddress(request);
        String classMethod = point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName();
        // 记录请求参数
        Object[] args = point.getArgs();
        String requestParams = null;
        if (args != null && args.length > 0) {
            // 检查参数类型并正确序列化
            if (args[0] instanceof List) {
                requestParams = JSONUtil.toJsonStr(args[0]);  // 数组格式
            } else {
                requestParams = JSONUtil.toJsonStr(args[0]);  // 对象格式
            }
        }

        // 执行目标方法
        Object result = null;
        try {
            result = point.proceed();
            return result;
        } finally {
            // 记录响应信息
            long endTime = System.currentTimeMillis();
            long timeConsumed = endTime - startTime;

            // 保存日志到数据库
            ApiLog apiLog = new ApiLog();
            apiLog.setRequestId(requestId);
            apiLog.setUrl(url);
            apiLog.setHttpMethod(httpMethod);
            apiLog.setIp(ip);
            apiLog.setClassMethod(classMethod);
            apiLog.setRequestParams(requestParams);
            apiLog.setResponseData(result != null ? JSONUtil.toJsonStr(result) : null);
            apiLog.setTimeConsumed(timeConsumed);
            apiLog.setUserId(String.valueOf(loginUser.getId()));

            apiLogMapper.insert(apiLog);
        }
    }
}
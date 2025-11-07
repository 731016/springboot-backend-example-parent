package com.xiaofei.springbootinit.example.interfaceaop.aop;

import cn.hutool.json.JSONUtil;
import com.xiaofei.springbootinit.example.interfaceaop.mapper.ApiRequestRecordMapper;
import com.xiaofei.springbootinit.example.interfaceaop.model.ApiRequestRecord;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/23
 */
@Aspect
@Component
@Slf4j
public class ReplayableAspect {

    @Resource
    private ApiRequestRecordMapper apiRequestRecordMapper;

    @Resource
    private UserService userService;

    @Pointcut("@annotation(com.xiaofei.springbootinit.example.interfaceaop.annotation.Replayable)")
    public void replayablePointcut() {}

    @Around("replayablePointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        User loginUser = userService.getLoginUser(request);

        // 记录请求信息
        ApiRequestRecord record = new ApiRequestRecord();
        record.setUrl(request.getRequestURL().toString());
        record.setHttpMethod(request.getMethod());
        record.setHeaders(getHeadersJson(request));
        record.setRequestParams(getRequestParams(request, point));
        record.setUserId(String.valueOf(loginUser.getId()));

        Object result = null;
        try {
            // 执行目标方法
            result = point.proceed();
            record.setStatus(200);
            record.setResponseData(JSONUtil.toJsonStr(result));
        } catch (Exception e) {
            record.setStatus(500);
            record.setResponseData(e.getMessage());
            throw e;
        } finally {
            // 记录耗时
            record.setTimeConsumed(System.currentTimeMillis() - startTime);
            // 保存记录
            apiRequestRecordMapper.insert(record);
        }

        return result;
    }

    private String getHeadersJson(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return JSONUtil.toJsonStr(headers);
    }

    private String getRequestParams(HttpServletRequest request, ProceedingJoinPoint point) {
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            Object[] args = point.getArgs();
            // 如果只有一个参数，直接转换该参数对象
            if (args != null && args.length == 1) {
                return JSONUtil.toJsonStr(args[0]);
            }
            // 如果有多个参数，保持数组格式
            return JSONUtil.toJsonStr(args);
        } else {
            return JSONUtil.toJsonStr(request.getParameterMap());
        }
    }
}
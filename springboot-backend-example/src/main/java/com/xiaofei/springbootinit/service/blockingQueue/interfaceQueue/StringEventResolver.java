package com.xiaofei.springbootinit.service.blockingQueue.interfaceQueue;

import cn.hutool.json.JSONUtil;
import com.xiaofei.springbootinit.service.blockingQueue.EventContext;
import com.xiaofei.springbootinit.service.blockingQueue.EventResolver;
import com.xiaofei.springbootinit.model.dto.blockingQueue.RequestData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author tuaofei
 * @description TODO
 * @date 2025/3/3
 */
@Component
@Slf4j
public class StringEventResolver implements EventResolver {

    @Override
    public boolean support(RequestData requestData) {
        return true;
    }

    @Override
    public void resolve(RequestData requestData, EventContext eventContext) {
        log.info("已接收数据" + JSONUtil.toJsonStr(requestData));
    }
}

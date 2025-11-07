package com.xiaofei.springbootinit.example.blockingQueue.test;

import com.xiaofei.springbootinit.example.blockingQueue.EventAbstractResolver;
import com.xiaofei.springbootinit.example.blockingQueue.EventContext;
import com.xiaofei.springbootinit.example.blockingQueue.data.RequestData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author tuaofei
 * @description TODO
 * @date 2025/3/10
 */
@Component
@Slf4j
public class StringEventAbstractResolver extends EventAbstractResolver<StringData> {
    @Override
    public boolean support(RequestData requestData) {
        return "string".equals(requestData.getType());
    }

    @Override
    public void resolve(RequestData requestData, EventContext eventContext) {
        String data = (String) requestData.getData();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("已接收数据" + data);
    }
}

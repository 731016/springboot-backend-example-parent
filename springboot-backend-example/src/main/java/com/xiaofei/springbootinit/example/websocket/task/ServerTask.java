package com.xiaofei.springbootinit.example.websocket.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.json.JSONUtil;
import com.xiaofei.springbootinit.common.ErrorCode;
import com.xiaofei.springbootinit.example.quartz.job.base.BaseJob;
import com.xiaofei.springbootinit.example.websocket.common.WebSocketConsts;
import com.xiaofei.springbootinit.example.websocket.model.Server;
import com.xiaofei.springbootinit.example.websocket.payload.ServerVO;
import com.xiaofei.springbootinit.example.websocket.utils.ServerUtil;
import com.xiaofei.springbootinit.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author tuaofei
 * @description 服务器定时执行任务
 * @date 2024/12/25
 */
@Component
@Slf4j
public class ServerTask implements BaseJob {

    @Autowired
    private SimpMessagingTemplate wsTemplate;

    @Override
    public void execute(JobExecutionContext context) {
        try {
            log.info("【推送消息】开始执行：{}", DateUtil.formatDateTime(new Date()));
            //查询服务器状态
            Server server = new Server();
            server.copyTo();
            ServerVO serverVO = ServerUtil.wrapServerVO(server);
            Dict dict = ServerUtil.wrapServerDict(serverVO);
            String jsonStr = JSONUtil.toJsonStr(dict);
            log.info(jsonStr);
            wsTemplate.convertAndSend(WebSocketConsts.PUSH_SERVER, jsonStr);
            log.info("【推送消息】执行结束：{}", DateUtil.formatDateTime(new Date()));
        } catch (Exception e) {
            log.error("【推送消息】出现错误", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }
}

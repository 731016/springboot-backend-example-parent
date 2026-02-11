package com.xiaofei.springbootbackendwebsocket.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.json.JSONUtil;
import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.exception.BusinessException;
import com.xiaofei.springbootbackendwebsocket.common.WebSocketConsts;
import com.xiaofei.springbootbackendwebsocket.model.Server;
import com.xiaofei.springbootbackendwebsocket.payload.ServerVO;
import com.xiaofei.springbootbackendwebsocket.utils.ServerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author tuaofei
 * @description 服务器定时执行任务
 * @date 2024/12/25
 */
@Component
@Slf4j
public class ServerTask {

    @Autowired
    private SimpMessagingTemplate wsTemplate;

    @Autowired(required = false)
    private SimpUserRegistry simpUserRegistry;

//    /**
//     * 想启动该定时任务，需要调用接口/api/job/addJob，参数【cronExpression：0/8 * * * * ?，jobClassName：com.xiaofei.springbootinit.example.websocket.task.ServerTask，jobGroupName：websocket】
//     * 注册为定时任务
//     * @param context 上下文
//     */
//    @Override
//    public void execute(JobExecutionContext context) {
//        try {
//            log.info("【推送消息】开始执行：{}", DateUtil.formatDateTime(new Date()));
//            //查询服务器状态
//            Server server = new Server();
//            server.copyTo();
//            ServerVO serverVO = ServerUtil.wrapServerVO(server);
//            Dict dict = ServerUtil.wrapServerDict(serverVO);
//            String jsonStr = JSONUtil.toJsonStr(dict);
//            log.info(jsonStr);
//            wsTemplate.convertAndSend(WebSocketConsts.PUSH_SERVER, jsonStr);
//            log.info("【推送消息】执行结束：{}", DateUtil.formatDateTime(new Date()));
//        } catch (Exception e) {
//            log.error("【推送消息】出现错误", e);
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
//        }
//    }

    /**
     * 不依赖quartz定时任务框架
     */
    @Scheduled(cron = "0/8 * * * * ?")
    public void pushServerInfo() {   // 没有任何参数
        try {
            // 如果还没有任何 WebSocket 连接，则不推送数据，避免无效推送
            if (simpUserRegistry == null || simpUserRegistry.getUserCount() == 0) {
                log.debug("【推送消息】当前没有 WebSocket 连接，跳过本次推送");
                return;
            }

            log.info("【推送消息】开始执行：{}", DateUtil.formatDateTime(new Date()));
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

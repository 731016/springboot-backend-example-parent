package com.xiaofei.springbootbackendwebsocket.controller;

import cn.hutool.core.lang.Dict;
import com.xiaofei.springbootbackendwebsocket.model.Server;
import com.xiaofei.springbootbackendwebsocket.payload.ServerVO;
import com.xiaofei.springbootbackendwebsocket.utils.ServerUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 服务器监控Controller
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2018-12-17 10:22
 */
@RestController
@RequestMapping("/websocket")
public class ServerController {

    @GetMapping("/server")
    public Dict serverInfo() throws Exception {
        Server server = new Server();
        server.copyTo();
        ServerVO serverVO = ServerUtil.wrapServerVO(server);
        return ServerUtil.wrapServerDict(serverVO);
    }

}

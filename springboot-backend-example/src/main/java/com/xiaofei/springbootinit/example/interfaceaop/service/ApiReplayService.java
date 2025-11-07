package com.xiaofei.springbootinit.example.interfaceaop.service;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/23
 */
public interface ApiReplayService {

    /**
     * 重放日志
     * @param recordId
     * @return
     */
    Object replayRequest(Long recordId);
}
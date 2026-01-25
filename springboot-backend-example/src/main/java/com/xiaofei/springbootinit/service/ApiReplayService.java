package com.xiaofei.springbootinit.service;

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
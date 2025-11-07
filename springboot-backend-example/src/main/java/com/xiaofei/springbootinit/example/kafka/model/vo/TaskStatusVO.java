package com.xiaofei.springbootinit.example.kafka.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/26
 */
@Data
@Builder
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class TaskStatusVO {
    /**
     * 点位编码
     */
    private String pointCode;

    /**
     * 点位名称
     */
    private String pointName;

    /**
     * 是否运行中
     */
    private boolean running;

    /**
     * 最后采集时间
     */
    private Date lastCollectTime;
}

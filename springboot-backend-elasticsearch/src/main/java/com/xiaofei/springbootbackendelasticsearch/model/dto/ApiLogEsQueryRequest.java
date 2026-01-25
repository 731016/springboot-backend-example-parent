package com.xiaofei.springbootbackendelasticsearch.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaofei.springbootbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * API 日志查询请求
 *
 * @author <a href="http://xiaofei.site>计算机知识杂货铺</a>
 * @from 
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ApiLogEsQueryRequest extends PageRequest implements Serializable {

    /**
     * 搜索关键词（搜索 url、classMethod、requestParams、responseData）
     */
    private String searchText;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * HTTP方法
     */
    private String httpMethod;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 开始时间
     * ES 存储格式：yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    /**
     * 结束时间
     * ES 存储格式：yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    /**
     * 最小耗时（毫秒）
     */
    private Long minTimeConsumed;

    /**
     * 最大耗时（毫秒）
     */
    private Long maxTimeConsumed;

    private static final long serialVersionUID = 1L;
}

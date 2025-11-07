package com.xiaofei.springbootinit.example.interfaceaop.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 接口请求记录
 */
@Data
@TableName(value = "api_request_record")
public class ApiRequestRecord implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 请求URL
     */
    @TableField(value = "url")
    private String url;

    /**
     * HTTP方法
     */
    @TableField(value = "http_method")
    private String httpMethod;

    /**
     * 请求头
     */
    @TableField(value = "headers")
    private String headers;

    /**
     * 请求参数
     */
    @TableField(value = "request_params")
    private String requestParams;

    /**
     * 请求内容类型
     */
    @TableField(value = "content_type")
    private String contentType;

    /**
     * 是否是数组请求
     */
    @TableField(value = "is_array_request")
    private Boolean isArrayRequest;

    /**
     * 响应数据
     */
    @TableField(value = "response_data")
    private String responseData;

    /**
     * 请求状态
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 耗时(ms)
     */
    @TableField(value = "time_consumed")
    private Long timeConsumed;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private String userId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableField(value = "is_deleted")
    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
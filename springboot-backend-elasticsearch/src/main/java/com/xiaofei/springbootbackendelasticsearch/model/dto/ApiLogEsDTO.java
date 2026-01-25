package com.xiaofei.springbootbackendelasticsearch.model.dto;

import com.xiaofei.springbootinit.example.interfaceaop.model.ApiLog;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.util.Date;

/**
 * API 日志 ES 文档
 * 注意：索引名使用固定前缀，实际索引名会根据日期动态生成（如：api_log_2025-01-25）
 */
@Document(indexName = "api_log")
@Data
public class ApiLogEsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Field(type = FieldType.Keyword)
    private String requestId;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String url;

    @Field(type = FieldType.Keyword)
    private String httpMethod;

    @Field(type = FieldType.Keyword)
    private String ip;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String classMethod;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String requestParams;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String responseData;

    @Field(type = FieldType.Long)
    private Long timeConsumed;

    @Field(type = FieldType.Keyword)
    private String userId;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    private Date createTime;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    private Date updateTime;

    /**
     * 高亮字段 - 用于存储高亮后的文本（不映射到 ES）
     */
    @Transient
    private String highlightUrl;
    @Transient
    private String highlightClassMethod;
    @Transient
    private String highlightRequestParams;
    @Transient
    private String highlightResponseData;

    /**
     * 对象转包装类
     *
     * @param apiLog
     * @return
     */
    public static ApiLogEsDTO objToDto(ApiLog apiLog) {
        if (apiLog == null) {
            return null;
        }
        ApiLogEsDTO apiLogEsDTO = new ApiLogEsDTO();
        // 手动映射所有字段，确保数据正确转换
        apiLogEsDTO.setId(apiLog.getId());
        apiLogEsDTO.setRequestId(apiLog.getRequestId());
        apiLogEsDTO.setUrl(apiLog.getUrl());
        apiLogEsDTO.setHttpMethod(apiLog.getHttpMethod());
        apiLogEsDTO.setIp(apiLog.getIp());
        apiLogEsDTO.setClassMethod(apiLog.getClassMethod());
        apiLogEsDTO.setRequestParams(apiLog.getRequestParams());
        apiLogEsDTO.setResponseData(apiLog.getResponseData());
        apiLogEsDTO.setTimeConsumed(apiLog.getTimeConsumed());
        apiLogEsDTO.setUserId(apiLog.getUserId());
        apiLogEsDTO.setCreateTime(apiLog.getCreateTime());
        apiLogEsDTO.setUpdateTime(apiLog.getUpdateTime());
        return apiLogEsDTO;
    }

    /**
     * 包装类转对象
     *
     * @param apiLogEsDTO
     * @return
     */
    public static ApiLog dtoToObj(ApiLogEsDTO apiLogEsDTO) {
        if (apiLogEsDTO == null) {
            return null;
        }
        ApiLog apiLog = new ApiLog();
        BeanUtils.copyProperties(apiLogEsDTO, apiLog);
        return apiLog;
    }

    /**
     * 根据日期生成索引名
     *
     * @param date 日期
     * @return 索引名，格式：api_log_yyyy-MM-dd
     */
    public static String getIndexName(Date date) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        return "api_log_" + sdf.format(date);
    }

    /**
     * 根据当前日期生成索引名
     *
     * @return 索引名
     */
    public static String getIndexName() {
        return getIndexName(new Date());
    }
}

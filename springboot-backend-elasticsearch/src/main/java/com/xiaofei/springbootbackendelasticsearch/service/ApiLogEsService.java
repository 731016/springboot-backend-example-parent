package com.xiaofei.springbootbackendelasticsearch.service;

import com.xiaofei.springbootbackendelasticsearch.model.dto.ApiLogEsDTO;
import com.xiaofei.springbootbackendelasticsearch.model.dto.ApiLogEsQueryRequest;

import java.util.List;

/**
 * API 日志 ES 服务接口
 *
 * @author <a href="http://xiaofei.site>计算机知识杂货铺</a>
 * @from 
 */
public interface ApiLogEsService {

    /**
     * 同步日志到 ES
     *
     * @param apiLogEsDTO 日志DTO
     * @return 是否成功
     */
    Boolean syncApiLog(ApiLogEsDTO apiLogEsDTO);

    /**
     * 搜索日志
     *
     * @param queryRequest 查询请求
     * @return 日志列表
     */
    List<ApiLogEsDTO> search(ApiLogEsQueryRequest queryRequest);
}

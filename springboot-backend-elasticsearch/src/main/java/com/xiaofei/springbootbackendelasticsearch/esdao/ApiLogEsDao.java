package com.xiaofei.springbootbackendelasticsearch.esdao;

import com.xiaofei.springbootbackendelasticsearch.model.dto.ApiLogEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * API 日志 ES 操作
 *
 * @author <a href="http://xiaofei.site>计算机知识杂货铺</a>
 * @from 
 */
public interface ApiLogEsDao extends ElasticsearchRepository<ApiLogEsDTO, Long> {
}

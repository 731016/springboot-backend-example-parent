package com.xiaofei.springbootbackendelasticsearch.service.impl;

import com.xiaofei.springbootbackendelasticsearch.model.dto.ApiLogEsDTO;
import com.xiaofei.springbootbackendelasticsearch.model.dto.ApiLogEsQueryRequest;
import com.xiaofei.springbootbackendelasticsearch.service.ApiLogEsService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

;

/**
 * API 日志 ES 服务实现
 *
 * @author <a href="http://xiaofei.site>计算机知识杂货铺</a>
 * @from 
 */
@Service
@Slf4j
public class ApiLogEsServiceImpl implements ApiLogEsService {

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public Boolean syncApiLog(ApiLogEsDTO apiLogEsDTO) {
        try {
            // 根据创建时间生成索引名（按日期动态创建索引）
            String indexName = ApiLogEsDTO.getIndexName(apiLogEsDTO.getCreateTime() != null 
                    ? apiLogEsDTO.getCreateTime() 
                    : new Date());
            
            IndexCoordinates indexCoordinates = IndexCoordinates.of(indexName);
            
            // 确保索引存在且映射正确
            if (!elasticsearchRestTemplate.indexOps(indexCoordinates).exists()) {
                // 创建动态索引
                elasticsearchRestTemplate.indexOps(indexCoordinates).create();
                
                // 从 ApiLogEsDTO 类获取映射信息并应用到动态索引
                // 这样可以确保 @Field 注解中的 date 类型映射被正确应用
                Document mapping = elasticsearchRestTemplate.indexOps(ApiLogEsDTO.class).createMapping();
                elasticsearchRestTemplate.indexOps(indexCoordinates).putMapping(mapping);
                
                log.info("创建索引并设置映射: {}", indexName);
            }
            
            // 使用 ElasticsearchRestTemplate 保存到指定索引
            elasticsearchRestTemplate.save(apiLogEsDTO, indexCoordinates);
            
            return true;
        } catch (Exception e) {
            throw new RuntimeException("同步日志到ES失败", e);
        }
    }

    @Override
    public List<ApiLogEsDTO> search(ApiLogEsQueryRequest queryRequest) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        
        String searchText = queryRequest.getSearchText();
        if (StringUtils.hasText(searchText)) {
            // 搜索关键词：在 url、classMethod、requestParams、responseData 中搜索
            BoolQueryBuilder shouldQuery = QueryBuilders.boolQuery();
            shouldQuery.should(QueryBuilders.matchQuery("url", searchText));
            shouldQuery.should(QueryBuilders.matchQuery("classMethod", searchText));
            shouldQuery.should(QueryBuilders.matchQuery("requestParams", searchText));
            shouldQuery.should(QueryBuilders.matchQuery("responseData", searchText));
            shouldQuery.minimumShouldMatch(1);
            boolQueryBuilder.must(shouldQuery);
        }

        // 精确匹配条件
        if (StringUtils.hasText(queryRequest.getRequestId())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("requestId", queryRequest.getRequestId()));
        }
        if (StringUtils.hasText(queryRequest.getHttpMethod())) {
            boolQueryBuilder.filter(QueryBuilders.matchQuery("httpMethod", queryRequest.getHttpMethod()));
        }
        if (StringUtils.hasText(queryRequest.getIp())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("ip", queryRequest.getIp()));
        }
        if (StringUtils.hasText(queryRequest.getUserId())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("userId", queryRequest.getUserId()));
        }

        // 时间范围查询
        // 注意：createTime 字段的 pattern 支持：yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis
        // 使用时间戳（毫秒）进行查询，这是最可靠的方式，兼容 date 类型字段
        if (queryRequest.getStartTime() != null || queryRequest.getEndTime() != null) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("createTime");
            if (queryRequest.getStartTime() != null) {
                // 使用时间戳（毫秒）进行查询，确保与 date 类型字段兼容
                rangeQuery.gte(queryRequest.getStartTime().getTime());
            }
            if (queryRequest.getEndTime() != null) {
                // 使用时间戳（毫秒）进行查询
                rangeQuery.lte(queryRequest.getEndTime().getTime());
            }
            boolQueryBuilder.filter(rangeQuery);
            log.debug("时间范围查询: startTime={}, endTime={}", 
                    queryRequest.getStartTime() != null ? queryRequest.getStartTime().getTime() : null,
                    queryRequest.getEndTime() != null ? queryRequest.getEndTime().getTime() : null);
        }

        // 耗时范围查询
        if (queryRequest.getMinTimeConsumed() != null || queryRequest.getMaxTimeConsumed() != null) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("timeConsumed");
            if (queryRequest.getMinTimeConsumed() != null) {
                rangeQuery.gte(queryRequest.getMinTimeConsumed());
            }
            if (queryRequest.getMaxTimeConsumed() != null) {
                rangeQuery.lte(queryRequest.getMaxTimeConsumed());
            }
            boolQueryBuilder.filter(rangeQuery);
        }

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withPageable(PageRequest.of(queryRequest.getCurrent(), queryRequest.getPageSize()));

        // 配置高亮（只有在有搜索关键词时才配置）
        if (StringUtils.hasText(searchText)) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("url");
            highlightBuilder.field("classMethod");
            highlightBuilder.field("requestParams");
            highlightBuilder.field("responseData");
            highlightBuilder.preTags("<em style='color: red; font-weight: bold;'>");
            highlightBuilder.postTags("</em>");
            highlightBuilder.fragmentSize(200);
            highlightBuilder.numOfFragments(1);
            queryBuilder.withHighlightBuilder(highlightBuilder);
        }

        NativeSearchQuery searchQuery = queryBuilder.build();

        // 根据时间范围确定要查询的索引
        List<String> indexNames = getIndexNames(queryRequest.getStartTime(), queryRequest.getEndTime());
        log.debug("根据时间范围确定的索引列表: {}", indexNames);
        
        // 过滤掉不存在的索引，避免查询时抛出异常
        List<String> existingIndexNames = indexNames.stream()
                .filter(indexName -> {
                    try {
                        boolean exists = elasticsearchRestTemplate.indexOps(IndexCoordinates.of(indexName)).exists();
                        if (!exists) {
                            log.debug("索引不存在，跳过: {}", indexName);
                        }
                        return exists;
                    } catch (Exception e) {
                        // 如果检查索引存在性时出错，记录日志但不抛出异常
                        log.warn("检查索引存在性失败: {}, error: {}", indexName, e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toList());
        
        // 如果所有索引都不存在，直接返回空列表
        if (existingIndexNames.isEmpty()) {
            log.info("所有索引都不存在，返回空结果。查询的索引: {}", indexNames);
            return new ArrayList<>();
        }
        
        // 记录实际查询的索引
        if (existingIndexNames.size() < indexNames.size()) {
            log.info("部分索引不存在，实际查询索引: {}，原始索引: {}", existingIndexNames, indexNames);
        }
        
        IndexCoordinates indexCoordinates = IndexCoordinates.of(existingIndexNames.toArray(new String[0]));

        SearchHits<ApiLogEsDTO> searchHits = null;
        try {
            searchHits = elasticsearchRestTemplate.search(searchQuery, ApiLogEsDTO.class, indexCoordinates);
        } catch (Exception e) {
            log.error("ES 查询失败，索引: {}, 查询条件: {}, 错误: {}", 
                    existingIndexNames, boolQueryBuilder.toString(), e.getMessage(), e);
        }

        // 处理高亮结果
        List<ApiLogEsDTO> resultList = searchHits.stream().map(hit -> {
            ApiLogEsDTO apiLogEsDTO = hit.getContent();

            if (hit.getHighlightFields() != null && !hit.getHighlightFields().isEmpty()) {
                if (hit.getHighlightFields().containsKey("url")) {
                    List<String> highlights = hit.getHighlightFields().get("url");
                    if (highlights != null && !highlights.isEmpty()) {
                        apiLogEsDTO.setHighlightUrl(highlights.get(0));
                    }
                }
                if (hit.getHighlightFields().containsKey("classMethod")) {
                    List<String> highlights = hit.getHighlightFields().get("classMethod");
                    if (highlights != null && !highlights.isEmpty()) {
                        apiLogEsDTO.setHighlightClassMethod(highlights.get(0));
                    }
                }
                if (hit.getHighlightFields().containsKey("requestParams")) {
                    List<String> highlights = hit.getHighlightFields().get("requestParams");
                    if (highlights != null && !highlights.isEmpty()) {
                        apiLogEsDTO.setHighlightRequestParams(highlights.get(0));
                    }
                }
                if (hit.getHighlightFields().containsKey("responseData")) {
                    List<String> highlights = hit.getHighlightFields().get("responseData");
                    if (highlights != null && !highlights.isEmpty()) {
                        apiLogEsDTO.setHighlightResponseData(highlights.get(0));
                    }
                }
            }

            return apiLogEsDTO;
        }).collect(Collectors.toList());

        return resultList;
    }

    /**
     * 根据时间范围获取索引名列表
     * 如果时间范围跨多天，需要查询多个索引
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 索引名列表
     */
    private List<String> getIndexNames(Date startTime, Date endTime) {
        List<String> indexNames = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();

        if (startTime != null && endTime != null) {
            // 有明确的时间范围，查询范围内的所有索引
            calendar.setTime(startTime);
            Date current = calendar.getTime();
            while (!current.after(endTime)) {
                indexNames.add("api_log_" + sdf.format(current));
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                current = calendar.getTime();
            }
        } else if (startTime != null) {
            // 只有开始时间，查询从开始时间到今天的索引
            calendar.setTime(startTime);
            Date current = calendar.getTime();
            Date today = new Date();
            while (!current.after(today)) {
                indexNames.add("api_log_" + sdf.format(current));
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                current = calendar.getTime();
            }
        } else if (endTime != null) {
            // 只有结束时间，查询结束时间当天的索引
            indexNames.add("api_log_" + sdf.format(endTime));
        } else {
            // 没有时间范围，默认查询最近7天的索引
            calendar.setTime(new Date());
            for (int i = 0; i < 7; i++) {
                indexNames.add("api_log_" + sdf.format(calendar.getTime()));
                calendar.add(Calendar.DAY_OF_MONTH, -1);
            }
        }

        // 如果没有找到任何索引，至少查询今天的索引
        if (indexNames.isEmpty()) {
            indexNames.add(ApiLogEsDTO.getIndexName());
        }

        return indexNames;
    }
}

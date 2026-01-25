package com.xiaofei.springbootbackendelasticsearch.service.impl;

import com.xiaofei.springbootbackendelasticsearch.model.dto.UserEsDTO;
import com.xiaofei.springbootbackendelasticsearch.model.dto.UserEsQueryRequest;
import com.xiaofei.springbootbackendelasticsearch.service.UserEsService;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserEsServiceImpl implements UserEsService {

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public List<UserEsDTO> search(UserEsQueryRequest userEsQueryRequest) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        String searchText = userEsQueryRequest.getSearchText();
        if (StringUtils.hasText(searchText)) {
            // userAccount 是 Keyword 类型，使用 wildcard 查询支持模糊匹配
            // 注意：wildcard 查询性能较低，如果数据量大建议使用其他方案
            // 设置 case_insensitive: true 支持大小写不敏感搜索
            WildcardQueryBuilder wildcardQuery = QueryBuilders.wildcardQuery("userAccount", "*" + searchText + "*");
            wildcardQuery.caseInsensitive(true);
            boolQueryBuilder.should(wildcardQuery);
            // userName 和 userProfile 是 Text 类型，使用 match 查询（支持 IK 分词）
            boolQueryBuilder.should(QueryBuilders.matchQuery("userName", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("userProfile", searchText));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 过滤已删除
        boolQueryBuilder.filter(QueryBuilders.termQuery("isDelete", 0));

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withPageable(PageRequest.of(userEsQueryRequest.getCurrent(), userEsQueryRequest.getPageSize()));

        // 只有在有搜索关键词时才配置高亮
        if (StringUtils.hasText(searchText)) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("userAccount");
            highlightBuilder.field("userName");
            highlightBuilder.field("userProfile");
            highlightBuilder.preTags("<em style='color: red; font-weight: bold;'>");
            highlightBuilder.postTags("</em>");
            highlightBuilder.fragmentSize(200); // 高亮片段大小
            highlightBuilder.numOfFragments(1); // 只返回一个片段
            
            queryBuilder.withHighlightBuilder(highlightBuilder);
        }

        NativeSearchQuery searchQuery = queryBuilder.build();

        SearchHits<UserEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, UserEsDTO.class);
        
        // 处理高亮结果
        List<UserEsDTO> resultList = searchHits.stream().map(hit -> {
            UserEsDTO userEsDTO = hit.getContent();
            
            // 提取高亮字段
            if (hit.getHighlightFields() != null && !hit.getHighlightFields().isEmpty()) {
                // userAccount 高亮
                if (hit.getHighlightFields().containsKey("userAccount")) {
                    List<String> highlights = hit.getHighlightFields().get("userAccount");
                    if (highlights != null && !highlights.isEmpty()) {
                        userEsDTO.setHighlightUserAccount(highlights.get(0));
                    }
                }
                
                // userName 高亮
                if (hit.getHighlightFields().containsKey("userName")) {
                    List<String> highlights = hit.getHighlightFields().get("userName");
                    if (highlights != null && !highlights.isEmpty()) {
                        userEsDTO.setHighlightUserName(highlights.get(0));
                    }
                }
                
                // userProfile 高亮
                if (hit.getHighlightFields().containsKey("userProfile")) {
                    List<String> highlights = hit.getHighlightFields().get("userProfile");
                    if (highlights != null && !highlights.isEmpty()) {
                        userEsDTO.setHighlightUserProfile(highlights.get(0));
                    }
                }
            }
            
            return userEsDTO;
        }).collect(Collectors.toList());
        
        return resultList;
    }
}

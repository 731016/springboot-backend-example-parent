package com.xiaofei.springbootbackendelasticsearch.controller;

import com.xiaofei.springbootbackendcommon.common.BaseResponse;
import com.xiaofei.springbootbackendcommon.common.ResultUtils;
import com.xiaofei.springbootbackendelasticsearch.esdao.UserEsDao;
import com.xiaofei.springbootbackendelasticsearch.model.dto.UserEsDTO;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/es/user")
@Slf4j
public class UserEsController {

    @Resource
    private UserEsDao userEsDao;
    
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 同步/保存用户到 ES
     */
    @PostMapping("/sync")
    public BaseResponse<Boolean> syncUser(@RequestBody UserEsDTO userEsDTO) {
        userEsDao.save(userEsDTO);
        return ResultUtils.success(true);
    }

    /**
     * 批量同步
     */
    @PostMapping("/sync/batch")
    public BaseResponse<Boolean> syncUserBatch(@RequestBody List<UserEsDTO> userEsDTOList) {
        userEsDao.saveAll(userEsDTOList);
        return ResultUtils.success(true);
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestParam Long id) {
        userEsDao.deleteById(id);
        return ResultUtils.success(true);
    }

    /**
     * 搜索用户
     */
    @GetMapping("/search")
    public BaseResponse<List<UserEsDTO>> searchUser(@RequestParam(required = false) String searchText,
                                                    @RequestParam(defaultValue = "0") int current,
                                                    @RequestParam(defaultValue = "10") int pageSize) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (StringUtils.hasText(searchText)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("userName", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("userProfile", searchText));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 过滤已删除
        boolQueryBuilder.filter(QueryBuilders.termQuery("isDelete", 0));

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withPageable(PageRequest.of(current, pageSize))
                .build();

        SearchHits<UserEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, UserEsDTO.class);
        List<UserEsDTO> resultList = searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
        
        return ResultUtils.success(resultList);
    }
}

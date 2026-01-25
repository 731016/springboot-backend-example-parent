package com.xiaofei.springbootbackendelasticsearch.controller;

import com.xiaofei.springbootbackendcommon.common.BaseResponse;
import com.xiaofei.springbootbackendcommon.common.ResultUtils;
import com.xiaofei.springbootbackendelasticsearch.esdao.UserEsDao;
import com.xiaofei.springbootbackendelasticsearch.model.dto.UserEsDTO;
import com.xiaofei.springbootbackendelasticsearch.model.dto.UserEsQueryRequest;
import com.xiaofei.springbootbackendelasticsearch.service.UserEsService;
import com.xiaofei.springbootinit.annotation.ApiLog;
import com.xiaofei.springbootinit.model.dto.user.UserQueryRequest;
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
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/es/user")
@Slf4j
public class UserEsController {

    @Resource
    private UserEsDao userEsDao;

    @Resource
    private UserEsService userEsService;

    /**
     * 同步/保存用户到 ES
     */
    @ApiLog(value = "同步/保存用户到 ES")
    @PostMapping("/sync")
    public BaseResponse<Boolean> syncUser(@RequestBody UserEsDTO userEsDTO) {
        userEsDao.save(userEsDTO);
        return ResultUtils.success(true);
    }

    /**
     * 批量同步
     */
    @ApiLog(value = "批量同步")
    @PostMapping("/sync/batch")
    public BaseResponse<Boolean> syncUserBatch(@RequestBody List<UserEsDTO> userEsDTOList) {
        userEsDao.saveAll(userEsDTOList);
        return ResultUtils.success(true);
    }

    /**
     * 删除用户
     */
    @ApiLog(value = "删除用户")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestParam Long id) {
        userEsDao.deleteById(id);
        return ResultUtils.success(true);
    }

    /**
     * 搜索用户
     */
    @ApiLog(value = "搜索用户")
    @PostMapping("/search")
    public BaseResponse<List<UserEsDTO>> searchUser(@RequestBody UserEsQueryRequest userEsQueryRequest,
                                                    HttpServletRequest request) {
        List<UserEsDTO> resultList = userEsService.search(userEsQueryRequest);
        return ResultUtils.success(resultList);
    }
}

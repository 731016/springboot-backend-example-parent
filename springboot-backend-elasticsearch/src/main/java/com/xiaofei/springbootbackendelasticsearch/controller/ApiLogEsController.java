package com.xiaofei.springbootbackendelasticsearch.controller;

import com.xiaofei.springbootbackendcommon.common.BaseResponse;
import com.xiaofei.springbootbackendcommon.common.ResultUtils;
import com.xiaofei.springbootbackendelasticsearch.model.dto.ApiLogEsDTO;
import com.xiaofei.springbootbackendelasticsearch.model.dto.ApiLogEsQueryRequest;
import com.xiaofei.springbootbackendelasticsearch.service.ApiLogEsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * API 日志 ES 控制器
 *
 * @author <a href="http://xiaofei.site>计算机知识杂货铺</a>
 * @from 
 */
@RestController
@RequestMapping("/es/api-log")
@Slf4j
public class ApiLogEsController {

    @Resource
    private ApiLogEsService apiLogEsService;

    /**
     * 同步日志到 ES
     */
    @PostMapping("/sync")
    public BaseResponse<Boolean> syncApiLog(@RequestBody ApiLogEsDTO apiLogEsDTO) {
        Boolean result = apiLogEsService.syncApiLog(apiLogEsDTO);
        return ResultUtils.success(result);
    }

    /**
     * 搜索日志
     */
    @PostMapping("/search")
    public BaseResponse<List<ApiLogEsDTO>> searchApiLog(@RequestBody ApiLogEsQueryRequest queryRequest,
                                                         HttpServletRequest request) {
        List<ApiLogEsDTO> resultList = apiLogEsService.search(queryRequest);
        return ResultUtils.success(resultList);
    }
}

package com.xiaofei.springbootbackendkafka.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaofei.springbootbackendcommon.common.BaseResponse;
import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.common.ResultUtils;
import com.xiaofei.springbootbackendcommon.exception.BusinessException;
import com.xiaofei.springbootbackendkafka.model.dto.CollectorSourceQueryRequest;
import com.xiaofei.springbootbackendkafka.model.dto.PointBindingDTO;
import com.xiaofei.springbootbackendkafka.model.dto.SaveCollectorSourceRequest;
import com.xiaofei.springbootbackendkafka.model.dto.TestCollectorSourceRequest;
import com.xiaofei.springbootbackendkafka.model.vo.CollectorSourceTestVO;
import com.xiaofei.springbootbackendkafka.model.vo.CollectorSourceVO;
import com.xiaofei.springbootbackendkafka.service.CollectorPointBindingService;
import com.xiaofei.springbootbackendkafka.service.CollectorSourceService;
import com.xiaofei.springbootbackendkafka.service.DataCollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 采集数据源管理
 *
 * @author tuaofei
 */
@RestController
@RequestMapping("/kafka/collectorSource")
@Slf4j
public class CollectorSourceController {

    @Autowired
    private CollectorSourceService collectorSourceService;

    @Autowired
    private CollectorPointBindingService collectorPointBindingService;

    @Autowired
    private DataCollectionService dataCollectionService;

    @PostMapping("/source/add")
    public BaseResponse<Long> addSource(@RequestBody @Valid SaveCollectorSourceRequest request) {
        if (request.getId() != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新增数据源时不能携带ID");
        }
        return ResultUtils.success(collectorSourceService.save(request));
    }

    @PostMapping("/source/update")
    public BaseResponse<Boolean> updateSource(@RequestBody @Valid SaveCollectorSourceRequest request) {
        if (request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "更新数据源时ID不能为空");
        }
        collectorSourceService.save(request);
        dataCollectionService.restartBySource(request.getId());
        return ResultUtils.success(true);
    }

    @PostMapping("/source/delete/{id}")
    public BaseResponse<Boolean> deleteSource(@PathVariable Long id) {
        collectorSourceService.delete(id);
        return ResultUtils.success(true);
    }

    @PostMapping("/source/list/page")
    public BaseResponse<Page<CollectorSourceVO>> listSourceByPage(
            @RequestBody CollectorSourceQueryRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        return ResultUtils.success(collectorSourceService.listByPage(request));
    }

    @GetMapping("/source/list/all")
    public BaseResponse<List<CollectorSourceVO>> listAllSource() {
        return ResultUtils.success(collectorSourceService.listAll());
    }

    @GetMapping("/source/detail/{id}")
    public BaseResponse<CollectorSourceVO> getSourceDetail(@PathVariable Long id) {
        return ResultUtils.success(collectorSourceService.detail(id));
    }

    @PostMapping("/source/test")
    public BaseResponse<CollectorSourceTestVO> testSource(@RequestBody TestCollectorSourceRequest request) {
        if (request == null || request.getSourceId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "数据源ID不能为空");
        }
        return ResultUtils.success(collectorSourceService.test(request));
    }

    @GetMapping("/point/binding/{pointId}")
    public BaseResponse<PointBindingDTO> getPointBinding(@PathVariable Long pointId) {
        return ResultUtils.success(collectorPointBindingService.getBindingDto(pointId));
    }
}

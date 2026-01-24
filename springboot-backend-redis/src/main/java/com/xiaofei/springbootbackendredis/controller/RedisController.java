package com.xiaofei.springbootbackendredis.controller;

import com.xiaofei.springbootbackendcommon.common.BaseResponse;
import com.xiaofei.springbootbackendcommon.common.ErrorCode;
import com.xiaofei.springbootbackendcommon.common.ResultUtils;
import com.xiaofei.springbootbackendcommon.exception.BusinessException;
import com.xiaofei.springbootbackendredis.model.dto.CodeDictionaryDto;
import com.xiaofei.springbootbackendredis.model.entity.CodeDictionary;
import com.xiaofei.springbootbackendredis.service.impl.CodeDictionaryServiceImpl;
import com.xiaofei.springbootinit.example.interfaceaop.annotation.ApiLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/27
 */
@RestController
@RequestMapping("/cache")
public class RedisController {

    @Autowired
    private CodeDictionaryServiceImpl codeDictionaryService;

    @ApiLog(value = "加载缓存")
    @PostMapping("/loadCache")
    public BaseResponse<String> loadCache() {
        codeDictionaryService.loadCache();
        return ResultUtils.success("加载缓存操作成功");
    }

    @ApiLog(value = "清除缓存")
    @PostMapping("/clearCache")
    public BaseResponse<String> clearCache() {
        codeDictionaryService.clearCache();
        return ResultUtils.success("清除缓存操作成功");
    }

    @ApiLog(value = "根据类型获取字典")
    @PostMapping("/getByType")
    public BaseResponse<List<CodeDictionary>> getByType(@RequestBody CodeDictionary codeDictionary) {
        List<CodeDictionary> user = codeDictionaryService.getByType(codeDictionary);
        return ResultUtils.success(user);
    }

    @ApiLog(value = "新增字典")
    @PostMapping("/addCodeDictionary")
    public BaseResponse<String> addCodeDictionary(@RequestBody CodeDictionaryDto codeDictionaryDto) {
        if (codeDictionaryDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Date curDate = new Date();
        CodeDictionary codeDictionary = conversion(codeDictionaryDto);
        codeDictionary.setCreateTime(curDate);
        codeDictionary.setUpdateTime(curDate);
        boolean status = codeDictionaryService.save(codeDictionary);
        if (status) {
            return ResultUtils.success("新增成功");
        }
        return ResultUtils.error(ErrorCode.OPERATION_ERROR, "新增失败");
    }

    @ApiLog(value = "批量新增字典")
    @PostMapping("/addCodeDictionaryList")
    public BaseResponse<String> addCodeDictionaryList(@RequestBody List<CodeDictionaryDto> codeDictionaryDtoList) {
        if (codeDictionaryDtoList == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<CodeDictionary> codeDictionaryList = new ArrayList<>(codeDictionaryDtoList.size());
        Date curDate = new Date();
        for (CodeDictionaryDto codeDictionaryDto : codeDictionaryDtoList) {
            CodeDictionary codeDictionary = conversion(codeDictionaryDto);
            codeDictionary.setCreateTime(curDate);
            codeDictionary.setUpdateTime(curDate);
            codeDictionaryList.add(codeDictionary);
        }
        boolean status = codeDictionaryService.saveBatch(codeDictionaryList);
        if (status) {
            return ResultUtils.success("批量新增成功");
        }
        return ResultUtils.error(ErrorCode.OPERATION_ERROR, "批量新增失败");
    }

    private CodeDictionary conversion(CodeDictionaryDto codeDictionaryDto) {
        CodeDictionary codeDictionary = new CodeDictionary();
        codeDictionary.setType(codeDictionaryDto.getType());
        codeDictionary.setCode(codeDictionaryDto.getCode());
        codeDictionary.setName(codeDictionaryDto.getName());
        codeDictionary.setAttr1(codeDictionaryDto.getAttr1());
        codeDictionary.setAttr2(codeDictionaryDto.getAttr2());
        codeDictionary.setAttr3(codeDictionaryDto.getAttr3());
        codeDictionary.setAttr4(codeDictionaryDto.getAttr4());
        codeDictionary.setAttr5(codeDictionaryDto.getAttr5());
        codeDictionary.setAttr6(codeDictionaryDto.getAttr6());
        codeDictionary.setAttr7(codeDictionaryDto.getAttr7());
        codeDictionary.setAttr8(codeDictionaryDto.getAttr8());
        codeDictionary.setAttr9(codeDictionaryDto.getAttr9());
        codeDictionary.setAttr10(codeDictionaryDto.getAttr10());
        codeDictionary.setAttr11(codeDictionaryDto.getAttr11());
        codeDictionary.setAttr12(codeDictionaryDto.getAttr12());
        return codeDictionary;
    }

}

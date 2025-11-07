package com.xiaofei.springbootinit.example.redis.controller;

import com.xiaofei.springbootinit.common.BaseResponse;
import com.xiaofei.springbootinit.common.ErrorCode;
import com.xiaofei.springbootinit.common.ResultUtils;
import com.xiaofei.springbootinit.example.redis.model.dto.CodeDictionaryDto;
import com.xiaofei.springbootinit.example.redis.model.entity.CodeDictionary;
import com.xiaofei.springbootinit.example.redis.service.impl.CodeDictionaryServiceImpl;
import com.xiaofei.springbootinit.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("/loadCache")
    public BaseResponse<String> loadCache() {
        codeDictionaryService.loadCache();
        return ResultUtils.success("加载缓存操作成功");
    }

    @PostMapping("/clearCache")
    public BaseResponse<String> clearCache() {
        codeDictionaryService.clearCache();
        return ResultUtils.success("清除缓存操作成功");
    }

    @PostMapping("/getByAndTypeCode")
    public BaseResponse<CodeDictionary> getByAndTypeCode() {
        CodeDictionary byAndTypeCode = codeDictionaryService.getByAndTypeCode("USER", "TUAOFEI");
        return ResultUtils.success(byAndTypeCode);
    }

    @PostMapping("/getByType")
    public BaseResponse<List<CodeDictionary>> getByType() {
        List<CodeDictionary> user = codeDictionaryService.getByType("USER");
        return ResultUtils.success(user);
    }

    @PostMapping("/addCodeDictionary")
    public BaseResponse<String> addCodeDictionary(CodeDictionaryDto codeDictionaryDto) {
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

    @PostMapping("/addCodeDictionaryList")
    public BaseResponse<String> addCodeDictionaryList(List<CodeDictionaryDto> codeDictionaryDtoList) {
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

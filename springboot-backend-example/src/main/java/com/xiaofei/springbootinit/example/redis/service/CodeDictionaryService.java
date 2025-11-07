package com.xiaofei.springbootinit.example.redis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaofei.springbootinit.example.redis.model.entity.CodeDictionary;

import java.util.List;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/27
 */
public interface CodeDictionaryService extends IService<CodeDictionary> {

    void loadCache();

    void clearCache();

    CodeDictionary getByAndTypeCode(String type, String code);

    List<CodeDictionary> getByType(String type);

}

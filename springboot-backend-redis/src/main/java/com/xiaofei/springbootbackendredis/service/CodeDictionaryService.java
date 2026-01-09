package com.xiaofei.springbootbackendredis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaofei.springbootbackendredis.model.entity.CodeDictionary;

import java.util.List;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/27
 */
public interface CodeDictionaryService extends IService<CodeDictionary> {

    void loadCache();

    void clearCache();

    List<CodeDictionary> getByType(CodeDictionary codeDictionary);

}

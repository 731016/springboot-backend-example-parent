package com.xiaofei.springbootinit.example.redis.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaofei.springbootinit.example.redis.constants.RedisKeyConstants;
import com.xiaofei.springbootinit.example.redis.mapper.CodeDictionaryMapper;
import com.xiaofei.springbootinit.example.redis.model.entity.CodeDictionary;
import com.xiaofei.springbootinit.example.redis.service.CodeDictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/27
 */
@Service
public class CodeDictionaryServiceImpl extends ServiceImpl<CodeDictionaryMapper, CodeDictionary> implements CodeDictionaryService {

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    @Autowired
    private CodeDictionaryMapper codeDictionaryMapper;

    @Override
    public void loadCache() {
        List<CodeDictionary> codeDictionaryList = (List<CodeDictionary>) redisTemplate.opsForValue().get(RedisKeyConstants.CACHE_KEY_PREFIX);
        if (CollectionUtil.isEmpty(codeDictionaryList)) {
            List<CodeDictionary> list = list();
            if (list != null && list.size() > 0) {
                Map<String, List<CodeDictionary>> typeMap = list.stream().collect(Collectors.groupingBy(item -> RedisKeyConstants.CACHE_KEY_PREFIX + item.getType()));
                for (Map.Entry<String, List<CodeDictionary>> entry : typeMap.entrySet()) {
                    redisTemplate.opsForValue().set(entry.getKey(), (Serializable) entry.getValue());
                    redisTemplate.expireAt(entry.getKey(), RedisKeyConstants.EXPIRY_DATE);
                }
            }
        }
    }

    @Override
    public void clearCache() {
        Set<String> keys = redisTemplate.keys(RedisKeyConstants.CACHE_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public CodeDictionary getByAndTypeCode(String type, String code) {
        List<CodeDictionary> codeDictionarieList = (List<CodeDictionary>) redisTemplate.opsForValue().get(RedisKeyConstants.CACHE_KEY_PREFIX + type);
        if (CollectionUtil.isNotEmpty(codeDictionarieList)) {
            for (CodeDictionary codeDictionary : codeDictionarieList) {
                if (code.equals(codeDictionary.getCode())) {
                    return codeDictionary;
                }
            }
        }
        QueryWrapper<CodeDictionary> wrapper = new QueryWrapper<>();
        wrapper.eq("type", type);
        wrapper.eq("code", code);
        CodeDictionary codeDictionary = codeDictionaryMapper.selectOne(wrapper);
        if (codeDictionary != null) {
            return codeDictionary;
        }
        return new CodeDictionary();
    }

    @Override
    public List<CodeDictionary> getByType(String type) {
        List<CodeDictionary> codeDictionarieList = (List<CodeDictionary>) redisTemplate.opsForValue().get(RedisKeyConstants.CACHE_KEY_PREFIX + type);
        if (CollectionUtil.isNotEmpty(codeDictionarieList)) {
            return codeDictionarieList;
        }
        QueryWrapper<CodeDictionary> wrapper = new QueryWrapper<>();
        wrapper.eq("type", type);
        List<CodeDictionary> codeDictionaries = codeDictionaryMapper.selectList(wrapper);
        if (CollectionUtil.isNotEmpty(codeDictionaries)) {
            return codeDictionaries;
        }
        return new ArrayList<>();
    }
}

package com.xiaofei.springbootbackendredis.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.mustachejava.Code;
import com.xiaofei.springbootbackendredis.constants.RedisKeyConstants;
import com.xiaofei.springbootbackendredis.mapper.CodeDictionaryMapper;
import com.xiaofei.springbootbackendredis.model.entity.CodeDictionary;
import com.xiaofei.springbootbackendredis.service.CodeDictionaryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;
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

    public Set<String> scanKeys(String prefix) {
        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
        if (factory == null) {
            return Collections.emptySet();
        }

        // 集群还是单机都统一用同一个方法签名
        return factory.getConnection().scan(
                        ScanOptions.scanOptions().match(prefix + "*").count(1000).build())
                .stream()
                .map(b -> new String(b, StandardCharsets.UTF_8))
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
    }

    @Override
    public void loadCache() {
        Set<String> keys = scanKeys(RedisKeyConstants.CACHE_KEY_PREFIX);
        List<Serializable> codeDictionaryList = redisTemplate.opsForValue().multiGet(keys);
        if (CollectionUtil.isEmpty(codeDictionaryList)) {
            List<CodeDictionary> list = list();
            if (list != null && list.size() > 0) {
                Map<String, CodeDictionary> typeMap = list.stream().collect(Collectors.toMap(item -> RedisKeyConstants.CACHE_KEY_PREFIX + item.getType() + ":" + item.getCode(), item -> item));
                for (Map.Entry<String, CodeDictionary> entry : typeMap.entrySet()) {
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
    public List<CodeDictionary> getByType(@RequestBody CodeDictionary codeDictionary) {
        String type = codeDictionary.getType();
        String code = codeDictionary.getCode();
        String name = codeDictionary.getName();
        String queryType = Optional.ofNullable(type).orElse("");
        String queryCode = Optional.ofNullable(code).orElse("");
        String queryName = Optional.ofNullable(name).orElse("");
        List<CodeDictionary> codeDictionarieList = new ArrayList<>();
        if (StringUtils.isNotBlank(queryType) || StringUtils.isNotBlank(queryCode) || StringUtils.isNotBlank(queryName)) {
            codeDictionarieList = (List<CodeDictionary>) redisTemplate.opsForValue().get(RedisKeyConstants.CACHE_KEY_PREFIX + queryType + ":" + queryCode);
        } else {
            Set<String> keys = scanKeys(RedisKeyConstants.CACHE_KEY_PREFIX);
            List<Serializable> serializableList = redisTemplate.opsForValue().multiGet(keys);
            codeDictionarieList = serializableList.stream()
                    .filter(Objects::nonNull)
                    .map(obj -> (CodeDictionary) obj)   // 强转
                    .collect(Collectors.toList());
        }
        if (CollectionUtil.isNotEmpty(codeDictionarieList)) {
            List<CodeDictionary> filterCodeDictionarieList = codeDictionarieList.stream().filter(item -> item.getCode().contains(queryCode) && item.getName().contains(queryName)).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(filterCodeDictionarieList)) {
                filterCodeDictionarieList.forEach(item -> item.setSourceType(RedisKeyConstants.SOURCE_TYPE_REDIS));
            }
            return filterCodeDictionarieList;
        }
        LambdaQueryWrapper<CodeDictionary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(queryType), CodeDictionary::getType, queryType);
        wrapper.eq(StringUtils.isNotBlank(codeDictionary.getCode()), CodeDictionary::getCode, queryCode);
        wrapper.eq(StringUtils.isNotBlank(codeDictionary.getName()), CodeDictionary::getName, queryName);
        List<CodeDictionary> codeDictionaries = codeDictionaryMapper.selectList(wrapper);
        if (CollectionUtil.isNotEmpty(codeDictionaries)) {
            codeDictionaries.forEach(item -> item.setSourceType(RedisKeyConstants.SOURCE_TYPE_DB));
            return codeDictionaries;
        }
        return new ArrayList<>();
    }


}

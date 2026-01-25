package com.xiaofei.springbootbackendelasticsearch.esdao;

import com.xiaofei.springbootbackendelasticsearch.model.dto.UserEsDTO;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 帖子 ES 操作
 *
 * @author <a href="http://xiaofei.site>计算机知识杂货铺</a>
 * @from 
 */
@Mapper
public interface UserEsDao extends ElasticsearchRepository<UserEsDTO, Long> {

    Optional<UserEsDTO> findById(Long userId);
}
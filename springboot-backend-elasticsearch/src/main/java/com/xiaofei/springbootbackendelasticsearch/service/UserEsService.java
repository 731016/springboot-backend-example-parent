package com.xiaofei.springbootbackendelasticsearch.service;

import com.xiaofei.springbootbackendelasticsearch.model.dto.UserEsDTO;
import com.xiaofei.springbootbackendelasticsearch.model.dto.UserEsQueryRequest;

import java.util.List;

public interface UserEsService{

    /**
     * 关键字搜索
     * @param userEsQueryRequest
     * @return
     */
    List<UserEsDTO> search(UserEsQueryRequest userEsQueryRequest);
}

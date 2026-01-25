package com.xiaofei.springbootbackendelasticsearch.model.dto;

import com.xiaofei.springbootbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户查询请求
 *
 * @author <a href="http://xiaofei.site>计算机知识杂货铺</a>
 * @from 
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserEsQueryRequest extends PageRequest implements Serializable {

    private String searchText;

    private static final long serialVersionUID = 1L;
}
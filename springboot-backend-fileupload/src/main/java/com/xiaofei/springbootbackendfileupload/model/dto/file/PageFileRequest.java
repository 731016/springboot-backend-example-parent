package com.xiaofei.springbootbackendfileupload.model.dto.file;


import com.xiaofei.springbootbackendcommon.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页请求
 *
 * @author <a href="http://xiaofei.site>计算机知识杂货铺</a>
 * @from 
 */
@Data
public class PageFileRequest extends PageRequest implements Serializable {

    /**
     * 目录名，不带/
     */
    private String preFix;

    /**
     * 每次查询最大数量
     */
    private Integer maxKeys = 1000;

    /**
     * 文件分页查询，下一次查询的游标
     */
    private String nextMarker;

    /**
     * 唯一标识
     */
    private String key;

    private static final long serialVersionUID = 1L;
}

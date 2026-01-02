package com.xiaofei.springbootbackendfileupload.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户头像
 *
 * @author <a href="http://xiaofei.site>计算机知识杂货铺</a>
 * @from 
 **/
@Data
public class PageFileVO implements Serializable {

    private List<FileVO> files;

    /**
     * 文件分页查询，下一次查询的游标
     */
    private String nextMarker;

    /**
     * 当前页的游标，向前翻页时使用
     */
    private String currentMarker;

    /**
     * 是否还有下一页
     */
    private boolean hasNext;

    /**
     * 目录路径
     */
    private String path;

    /**
     * 总数
     */
    private int total;

    private static final long serialVersionUID = 1L;
}
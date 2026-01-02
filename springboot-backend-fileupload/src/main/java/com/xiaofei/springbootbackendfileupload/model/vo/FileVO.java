package com.xiaofei.springbootbackendfileupload.model.vo;

import com.qcloud.cos.model.Tag.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用户头像
 *
 * @author <a href="http://xiaofei.site>计算机知识杂货铺</a>
 * @from 
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileVO implements Serializable {

    /**
     * 用户头像，拼接后的地址
     */
    private String userAvatar;

    /**
     * 文件名
     */
    private String name;

    /**
     * 文件的 etag
     */
    private String etag;

    /**
     * 文件的长度
     */
    private Long fileSize;

    /**
     * 文件的路径
     */
    private String key;

    /**
     * 文件的存储类型
     */
    private String storageClasses;

    /**
     * 修改日期
     */
    private Date lastModified;

    /**
     * 文件类型：文件：file，目录：dir
     */
    private String fileType;

    /**
     * 目录路径
     */
    private String path;

    /**
     * 标签
     */
    private List<Tag> tags;


    private static final long serialVersionUID = 1L;
}
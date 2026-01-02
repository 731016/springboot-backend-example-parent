package com.xiaofei.springbootbackendfileupload.model.dto.file;

import lombok.Data;

import java.io.Serializable;

/**
 * cos文件删除请求
 */
@Data
public class FileDeleteRequest implements Serializable {

    private String key;

    private static final long serialVersionUID = 1L;
}

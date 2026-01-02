package com.xiaofei.springbootbackendfileupload.model.dto.file;

import lombok.Data;

import java.io.Serializable;

/**
 * cos文件下载请求
 */
@Data
public class FileDownloadRequest implements Serializable {

    private String key;

    private static final long serialVersionUID = 1L;
}

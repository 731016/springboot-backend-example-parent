package com.xiaofei.springbootinit.example.commonserviceExec.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/24
 */
@Data
@TableName("service_processor_config")
public class ServiceProcessorConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

   
    private String serviceName;

   
    private String methodName;

   
    private String processorName;

   
    private String timing;

   
    private Boolean isAsync;

   
    private Boolean ignoreError;

   
    private Boolean status;

   
    private Date createTime;

   
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

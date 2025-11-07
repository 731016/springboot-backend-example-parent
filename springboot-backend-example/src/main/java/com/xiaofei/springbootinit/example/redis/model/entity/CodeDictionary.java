package com.xiaofei.springbootinit.example.redis.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/27
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("code_dictionary")
public class CodeDictionary implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDeleted;

    private Date createTime;

    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    private String type;

    private String code;

    private String name;

    private String attr1;

    private String attr2;

    private String attr3;

    private String attr4;

    private String attr5;

    private String attr6;

    private String attr7;

    private String attr8;

    private String attr9;

    private String attr10;

    private String attr11;

    private String attr12;
}

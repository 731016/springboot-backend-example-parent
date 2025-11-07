package com.xiaofei.springbootinit.example.redis.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;

import javax.validation.constraints.NotBlank;

/**
 * @author tuaofei
 * @description TODO
 * @date 2024/12/27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeDictionaryDto {

    @NotBlank(message = "类型不能为空")
    private String type;

    @NotBlank(message = "编码不能为空")
    private String code;

    @NotBlank(message = "名称不能为空")
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

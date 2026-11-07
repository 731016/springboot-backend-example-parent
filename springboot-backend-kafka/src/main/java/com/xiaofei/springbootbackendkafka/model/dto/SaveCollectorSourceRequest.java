package com.xiaofei.springbootbackendkafka.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 新增/更新采集源请求
 *
 * @author tuaofei
 */
@Data
public class SaveCollectorSourceRequest implements Serializable {

    /**
     * 主键 ID，为空表示新增
     */
    private Long id;

    @NotBlank(message = "数据源编码不能为空")
    private String sourceCode;

    @NotBlank(message = "数据源名称不能为空")
    private String sourceName;

    @NotBlank(message = "协议不能为空")
    private String protocol;

    private Integer status = 1;

    private String remark;

    private CollectorHttpConfigDTO httpConfig;

    private CollectorModbusConfigDTO modbusConfig;

    private CollectorOpcUaConfigDTO opcUaConfig;

    private static final long serialVersionUID = 1L;
}

package com.xiaofei.springbootbackendkafka.model.vo;

import com.xiaofei.springbootbackendkafka.model.dto.CollectorHttpConfigDTO;
import com.xiaofei.springbootbackendkafka.model.dto.CollectorModbusConfigDTO;
import com.xiaofei.springbootbackendkafka.model.dto.CollectorOpcUaConfigDTO;
import lombok.Data;

import java.util.Date;

/**
 * 采集源展示对象
 *
 * @author tuaofei
 */
@Data
public class CollectorSourceVO {

    private Long id;

    private String sourceCode;

    private String sourceName;

    private String protocol;

    private Integer status;

    private String remark;

    private Date createTime;

    private Date updateTime;

    /**
     * 引用的点位数
     */
    private Long pointCount;

    private CollectorHttpConfigDTO httpConfig;

    private CollectorModbusConfigDTO modbusConfig;

    private CollectorOpcUaConfigDTO opcUaConfig;
}

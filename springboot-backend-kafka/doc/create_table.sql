CREATE TABLE `shift_group` (
                               `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                               `shiftCode` VARCHAR(50) NOT NULL COMMENT '班次编码，如 A/B/C',
                               `shiftName` VARCHAR(100) NOT NULL COMMENT '班次名称，如 早班/中班/晚班',
                               `shiftStartTime` TIME NOT NULL COMMENT '班次开始时间',
                               `shiftEndTime` TIME NOT NULL COMMENT '班次结束时间',
                               `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-停用 1-启用',
                               `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
                               `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `isDeleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-删除',
                               PRIMARY KEY (`id`),
                               KEY `idx_shift_code` (`shiftCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班次班组基础表';


CREATE TABLE `work_calendar` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `workDate` DATE NOT NULL COMMENT '工作日期',
                                 `shiftCode` VARCHAR(50) NOT NULL COMMENT '班次编码，如 A/B/C',
                                 `shiftName` VARCHAR(100) NOT NULL COMMENT '班次名称，如 早班/中班/晚班',
                                 `shiftStartTime` TIME NOT NULL COMMENT '班次开始时间',
                                 `shiftEndTime` TIME NOT NULL COMMENT '班次结束时间',
                                 `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-停用 1-启用',
                                 `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
                                 `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 `isDeleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-删除',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_workDate` (`workDate`),
                                 KEY `idx_workDate_shift` (`workDate`, `shiftStartTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作日历（班次排班）';
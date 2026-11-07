# 数据库初始化
# @author
# @from

-- 创建库
create database if not exists my_db;

-- 切换库
use my_db;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_unionId (unionId)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 帖子表
create table if not exists post
(
    id         bigint auto_increment comment 'id' primary key,
    title      varchar(512)                       null comment '标题',
    content    text                               null comment '内容',
    tags       varchar(1024)                      null comment '标签列表（json 数组）',
    thumbNum   int      default 0                 not null comment '点赞数',
    favourNum  int      default 0                 not null comment '收藏数',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
) comment '帖子' collate = utf8mb4_unicode_ci;

-- 帖子点赞表（硬删除）
create table if not exists post_thumb
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '帖子点赞';

-- 帖子收藏表（硬删除）
create table if not exists post_favour
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '帖子收藏';

-- 点位配置表
CREATE TABLE point_config (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              pointCode VARCHAR(50) NOT NULL COMMENT '点位编码',
                              pointName VARCHAR(100) NOT NULL COMMENT '点位名称',
                              unit VARCHAR(20) NULL COMMENT '计量单位',
                              validUrl VARCHAR(200) NOT NULL COMMENT '数据有效性检查接口',
                              dataUrl VARCHAR(200) NOT NULL COMMENT '数据采集接口',
                              minLimit DECIMAL(10,2) COMMENT '数据下限',
                              maxLimit DECIMAL(10,2) COMMENT '数据上限',
                              intervalSeconds INT NOT NULL COMMENT '采集间隔(秒)',
                              isMainPoint TINYINT NOT NULL COMMENT '是否主点位(0-否,1-是)',
                              status TINYINT DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
                              createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              isDeleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除(0-未删除,1-已删除)',
                              runningStatus TINYINT NOT NULL DEFAULT 0 COMMENT '运行状态(0-停止,1-运行中)',
                              spcGroupSize INT NOT NULL DEFAULT 5 COMMENT 'SPC子组大小(2-25)',
                              spcRuleConfig VARCHAR(200) NULL COMMENT 'SPC判异规则(JSON数组，如["R1","R2"])'
);

-- 已有点位配置表升级脚本（SPC 子组大小与判异规则，仅在旧库手动执行一次）
-- ALTER TABLE point_config
--     ADD COLUMN spcGroupSize INT NOT NULL DEFAULT 5 COMMENT 'SPC子组大小(2-25)',
--     ADD COLUMN spcRuleConfig VARCHAR(200) NULL COMMENT 'SPC判异规则(JSON数组，如["R1","R2"])';

-- 数据统计主表
CREATE TABLE data_statistics (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                 pointCode VARCHAR(50) NOT NULL COMMENT '点位编码',
                                 startTime DATETIME NOT NULL COMMENT '开始时间',
                                 endTime DATETIME COMMENT '结束时间',
                                 maximumValue DECIMAL(18,6) COMMENT '最大值',
                                 minimumValue DECIMAL(18,6) COMMENT '最小值',
                                 averageValue DECIMAL(18,6) COMMENT '平均值',
                                 standardDeviation DECIMAL(18,6) COMMENT '标准差(样本标准差)',
                                 sampleCount INT DEFAULT 0 COMMENT '参与统计的样本数',
                                 outOfLimitCount INT DEFAULT 0 COMMENT '超限次数',
                                 status TINYINT DEFAULT 1 COMMENT '状态(1-进行中,2-已完成)',
                                 createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 isDeleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除(0-未删除,1-已删除)',
                                 KEY idx_pointCode (pointCode),
                                 KEY idx_startTime (startTime)
);

-- 数据采集明细表
CREATE TABLE data_detail (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             pointCode VARCHAR(50) NOT NULL COMMENT '点位编码',
                             collectTime DATETIME NOT NULL COMMENT '采集时间',
                             value DECIMAL(18,6) NOT NULL COMMENT '采集值',
                             attributeName VARCHAR(50) COMMENT '属性名',
                             statisticsId BIGINT COMMENT '关联的统计ID',
                             createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             isDeleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除(0-未删除,1-已删除)',
                             KEY idx_pointCode (pointCode),
                             KEY idx_collectTime (collectTime),
                             KEY idx_statisticsId (statisticsId)
);

ALTER TABLE data_detail ADD COLUMN dataType INT NULL COMMENT '数据类型：1-正常数据，2-非统计数据';

-- 采集值精度提升，满足 SPC 计算需要（已存在的表执行以下升级语句）
ALTER TABLE data_detail MODIFY COLUMN value DECIMAL(18,6) NOT NULL COMMENT '采集值';

-- data_statistics 扩展字段（已存在的表执行以下升级语句，新库无需执行）
ALTER TABLE data_statistics MODIFY COLUMN maximumValue DECIMAL(18,6) COMMENT '最大值';
ALTER TABLE data_statistics MODIFY COLUMN minimumValue DECIMAL(18,6) COMMENT '最小值';
ALTER TABLE data_statistics MODIFY COLUMN averageValue DECIMAL(18,6) COMMENT '平均值';
ALTER TABLE data_statistics ADD COLUMN standardDeviation DECIMAL(18,6) COMMENT '标准差(样本标准差)';
ALTER TABLE data_statistics ADD COLUMN sampleCount INT DEFAULT 0 COMMENT '参与统计的样本数';
ALTER TABLE data_statistics ADD COLUMN outOfLimitCount INT DEFAULT 0 COMMENT '超限次数';

-- 按明细回填历史统计数据（仅升级已有数据时执行一次）
UPDATE data_statistics s
JOIN (
    SELECT statisticsId,
           COUNT(CASE WHEN dataType IS NULL OR dataType = 1 THEN 1 END) AS sampleCount,
           COUNT(CASE WHEN dataType = 3 THEN 1 END) AS outOfLimitCount,
           MAX(CASE WHEN dataType IS NULL OR dataType = 1 THEN value END) AS maximumValue,
           MIN(CASE WHEN dataType IS NULL OR dataType = 1 THEN value END) AS minimumValue,
           ROUND(AVG(CASE WHEN dataType IS NULL OR dataType = 1 THEN value END), 6) AS averageValue,
           ROUND(STDDEV_SAMP(CASE WHEN dataType IS NULL OR dataType = 1 THEN value END), 6) AS standardDeviation,
           MIN(collectTime) AS firstTime,
           MAX(collectTime) AS lastTime
    FROM data_detail
    WHERE statisticsId IS NOT NULL
    GROUP BY statisticsId
) d ON s.id = d.statisticsId
SET s.sampleCount = d.sampleCount,
    s.outOfLimitCount = d.outOfLimitCount,
    s.maximumValue = d.maximumValue,
    s.minimumValue = d.minimumValue,
    s.averageValue = d.averageValue,
    s.standardDeviation = d.standardDeviation,
    s.startTime = COALESCE(d.firstTime, s.startTime),
    s.endTime = COALESCE(s.endTime, d.lastTime);

-- 关闭历史遗留的进行中记录
UPDATE data_statistics SET status = 2 WHERE status = 1 AND endTime IS NOT NULL AND endTime < CURDATE();

-- ==================== 采集数据源（多协议） ====================

-- 采集源主表
CREATE TABLE IF NOT EXISTS collector_source (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sourceCode VARCHAR(64) NOT NULL COMMENT '数据源编码',
    sourceName VARCHAR(100) NOT NULL COMMENT '数据源名称',
    protocol VARCHAR(20) NOT NULL COMMENT '协议：HTTP/MODBUS_TCP/OPC_UA',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
    remark VARCHAR(255) NULL COMMENT '备注',
    createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    isDeleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除(0-未删除,1-已删除)',
    UNIQUE KEY uk_sourceCode (sourceCode)
) COMMENT '采集数据源';

-- HTTP 采集源配置
CREATE TABLE IF NOT EXISTS collector_http_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sourceId BIGINT NOT NULL COMMENT '采集源ID',
    baseUrl VARCHAR(500) NOT NULL COMMENT '采集接口地址',
    validUrl VARCHAR(500) NULL COMMENT '连通性测试地址（为空时使用 baseUrl）',
    method VARCHAR(10) NOT NULL DEFAULT 'POST' COMMENT '请求方法',
    contentType VARCHAR(100) NOT NULL DEFAULT 'application/json' COMMENT 'Content-Type',
    headers TEXT NULL COMMENT '附加请求头 JSON 对象',
    requestTemplate TEXT NULL COMMENT '请求体模板，支持 ${pointCode} ${collectTime}',
    responseDataPath VARCHAR(200) NULL DEFAULT 'data' COMMENT '响应数据节点路径，如 data.result',
    valueField VARCHAR(50) NOT NULL DEFAULT 'value' COMMENT '值字段名',
    attributeNameField VARCHAR(50) NULL DEFAULT 'attributeName' COMMENT '属性名字段名',
    timeoutMillis INT NOT NULL DEFAULT 5000 COMMENT '超时时间(毫秒)',
    UNIQUE KEY uk_sourceId (sourceId)
) COMMENT 'HTTP采集源配置';

-- Modbus TCP 采集源配置
CREATE TABLE IF NOT EXISTS collector_modbus_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sourceId BIGINT NOT NULL COMMENT '采集源ID',
    host VARCHAR(100) NOT NULL COMMENT '从站地址',
    port INT NOT NULL DEFAULT 502 COMMENT '从站端口',
    slaveId INT NOT NULL DEFAULT 1 COMMENT '从站地址(UnitId)',
    connectTimeoutMillis INT NOT NULL DEFAULT 3000 COMMENT '连接超时(毫秒)',
    readTimeoutMillis INT NOT NULL DEFAULT 3000 COMMENT '读取超时(毫秒)',
    UNIQUE KEY uk_sourceId (sourceId)
) COMMENT 'Modbus TCP采集源配置';

-- OPC UA 采集源配置
CREATE TABLE IF NOT EXISTS collector_opcua_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sourceId BIGINT NOT NULL COMMENT '采集源ID',
    endpointUrl VARCHAR(500) NOT NULL COMMENT 'Endpoint URL',
    securityPolicy VARCHAR(50) NOT NULL DEFAULT 'None' COMMENT '安全策略',
    username VARCHAR(100) NULL COMMENT '用户名',
    password VARCHAR(200) NULL COMMENT '密码',
    requestTimeoutMillis INT NOT NULL DEFAULT 5000 COMMENT '请求超时(毫秒)',
    UNIQUE KEY uk_sourceId (sourceId)
) COMMENT 'OPC UA采集源配置';

-- Modbus 点位绑定
CREATE TABLE IF NOT EXISTS collector_modbus_point (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pointId BIGINT NOT NULL COMMENT '点位ID(point_config.id)',
    sourceId BIGINT NOT NULL COMMENT '采集源ID',
    registerType VARCHAR(20) NOT NULL COMMENT 'COIL/DISCRETE_INPUT/HOLDING_REGISTER/INPUT_REGISTER',
    address INT NOT NULL COMMENT '起始寄存器地址(0基)',
    quantity INT NOT NULL DEFAULT 1 COMMENT '读取数量',
    dataType VARCHAR(20) NOT NULL DEFAULT 'INT16' COMMENT 'BOOLEAN/INT16/UINT16/INT32/UINT32/FLOAT32',
    byteOrder VARCHAR(10) NOT NULL DEFAULT 'ABCD' COMMENT 'ABCD/CDAB/BADC/DCBA',
    scale DECIMAL(18,6) NOT NULL DEFAULT 1 COMMENT '换算系数：采集值 * scale',
    UNIQUE KEY uk_pointId (pointId),
    KEY idx_sourceId (sourceId)
) COMMENT 'Modbus点位绑定';

-- OPC UA 点位绑定
CREATE TABLE IF NOT EXISTS collector_opcua_point (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pointId BIGINT NOT NULL COMMENT '点位ID(point_config.id)',
    sourceId BIGINT NOT NULL COMMENT '采集源ID',
    nodeId VARCHAR(255) NOT NULL COMMENT 'NodeId，如 ns=2;s=Machine.Temp',
    attributeName VARCHAR(50) NULL COMMENT '写入 data_detail.attributeName',
    UNIQUE KEY uk_pointId (pointId),
    KEY idx_sourceId (sourceId)
) COMMENT 'OPC UA点位绑定';

-- 点位表关联采集源（已存在的表执行一次）
ALTER TABLE point_config
    ADD COLUMN sourceId BIGINT NULL COMMENT '采集数据源ID',
    ADD KEY idx_sourceId (sourceId),
    MODIFY COLUMN validUrl VARCHAR(200) NULL COMMENT '已废弃：原数据有效性检查接口',
    MODIFY COLUMN dataUrl VARCHAR(200) NULL COMMENT '已废弃：原数据采集接口';

-- 点位计量单位（已存在的表执行一次）
ALTER TABLE point_config ADD COLUMN unit VARCHAR(20) NULL COMMENT '计量单位';

-- 存量迁移：按原 dataUrl 去重生成 HTTP 采集源
INSERT INTO collector_source (sourceCode, sourceName, protocol, status, remark)
SELECT CONCAT('LEGACY_HTTP_', LEFT(MD5(t.dataUrl), 8)),
       CONCAT('历史HTTP源-', LEFT(MD5(t.dataUrl), 8)),
       'HTTP',
       1,
       '由原 point_config.dataUrl 迁移'
FROM (SELECT DISTINCT dataUrl FROM point_config WHERE dataUrl IS NOT NULL AND dataUrl <> '') t;

INSERT INTO collector_http_config (sourceId, baseUrl, validUrl, method, contentType, requestTemplate,
                                   responseDataPath, valueField, attributeNameField, timeoutMillis)
SELECT s.id,
       MIN(p.dataUrl),
       MAX(p.validUrl),
       'POST',
       'application/json',
       '{"pointCode":"${pointCode}","collectTime":"${collectTime}"}',
       'data',
       'value',
       'attributeName',
       5000
FROM point_config p
JOIN collector_source s ON s.sourceCode = CONCAT('LEGACY_HTTP_', LEFT(MD5(p.dataUrl), 8))
WHERE p.dataUrl IS NOT NULL AND p.dataUrl <> ''
GROUP BY s.id;

UPDATE point_config p
JOIN collector_source s ON s.sourceCode = CONCAT('LEGACY_HTTP_', LEFT(MD5(p.dataUrl), 8))
SET p.sourceId = s.id
WHERE p.sourceId IS NULL;

-- 迁移校验：应返回 0
-- SELECT COUNT(*) FROM point_config WHERE sourceId IS NULL;

CREATE TABLE `code_dictionary` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `code` varchar(50) NOT NULL COMMENT '编码',
                                   `name` varchar(100) NOT NULL COMMENT '名称',
                                   `type` varchar(50) NOT NULL COMMENT '类型',
                                   `attr1` varchar(200) DEFAULT NULL COMMENT '属性1',
                                   `attr2` varchar(200) DEFAULT NULL COMMENT '属性2',
                                   `attr3` varchar(200) DEFAULT NULL COMMENT '属性3',
                                   `attr4` varchar(200) DEFAULT NULL COMMENT '属性4',
                                   `attr5` varchar(200) DEFAULT NULL COMMENT '属性5',
                                   `attr6` varchar(200) DEFAULT NULL COMMENT '属性6',
                                   `attr7` varchar(200) DEFAULT NULL COMMENT '属性7',
                                   `attr8` varchar(200) DEFAULT NULL COMMENT '属性8',
                                   `attr9` varchar(200) DEFAULT NULL COMMENT '属性9',
                                   `attr10` varchar(200) DEFAULT NULL COMMENT '属性10',
                                   `attr11` varchar(200) DEFAULT NULL COMMENT '属性11',
                                   `attr12` varchar(200) DEFAULT NULL COMMENT '属性12',
                                   `isDeleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除(0-未删, 1-已删)',
                                   `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_code_type` (`code`,`type`,`isDeleted`) COMMENT '编码类型唯一索引',
                                   KEY `idx_type` (`type`) COMMENT '类型索引',
                                   KEY `idx_create_time` (`createTime`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='编码字典表';

create table if not exists api_log
(
    id bigint auto_increment
    primary key,
    request_id varchar(64) not null comment '请求唯一标识',
    url varchar(255) not null comment '请求URL',
    http_method varchar(10) not null comment 'HTTP方法',
    ip varchar(64) not null comment '请求IP',
    class_method varchar(255) not null comment '调用方法',
    request_params text null comment '请求参数',
    response_data text null comment '响应数据',
    time_consumed bigint null comment '耗时(ms)',
    user_id varchar(64) null comment '用户ID',
    create_time datetime default CURRENT_TIMESTAMP not null,
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    is_deleted tinyint default 0 not null comment '是否删除'
    );
create table if not exists api_request_record
(
    id bigint auto_increment comment '主键'
    primary key,
    url varchar(255) not null comment '请求URL',
    http_method varchar(10) not null comment 'HTTP方法',
    headers text null comment '请求头',
    content_type varchar(128) null comment '请求内容类型',
    request_params text null comment '请求参数',
    response_data text null comment '响应数据',
    status int null comment '请求状态',
    time_consumed bigint null comment '耗时(ms)',
    is_array_request tinyint(1) default 0 null comment '是否数组请求',
    user_id varchar(64) null comment '用户ID',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted tinyint default 0 not null comment '是否删除'
    )
    comment '接口请求记录表' collate=utf8mb4_general_ci;

create index idx_create_time
    on my_db.api_request_record (create_time);

create table if not exists code_dictionary
(
    id bigint auto_increment comment '主键ID'
    primary key,
    code varchar(50) not null comment '编码',
    name varchar(100) not null comment '名称',
    type varchar(50) not null comment '类型',
    attr1 varchar(200) null comment '属性1',
    attr2 varchar(200) null comment '属性2',
    attr3 varchar(200) null comment '属性3',
    attr4 varchar(200) null comment '属性4',
    attr5 varchar(200) null comment '属性5',
    attr6 varchar(200) null comment '属性6',
    attr7 varchar(200) null comment '属性7',
    attr8 varchar(200) null comment '属性8',
    attr9 varchar(200) null comment '属性9',
    attr10 varchar(200) null comment '属性10',
    attr11 varchar(200) null comment '属性11',
    attr12 varchar(200) null comment '属性12',
    isDeleted tinyint default 0 not null comment '是否删除(0-未删, 1-已删)',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_code_type
    unique (code, type, isDeleted) comment '编码类型唯一索引'
    )
    comment '编码字典表' collate=utf8mb4_general_ci;

create index idx_create_time
    on my_db.code_dictionary (createTime)
    comment '创建时间索引';

create index idx_type
    on my_db.code_dictionary (type)
    comment '类型索引';

create table if not exists service_processor_config
(
    id bigint auto_increment
    primary key,
    serviceName varchar(100) not null comment '服务名称',
    methodName varchar(100) not null comment '方法名称',
    processorName varchar(100) not null comment '处理器名称',
    timing varchar(10) not null comment '执行时机(before/after)',
    isAsync tinyint(1) default 0 not null comment '是否异步(0-同步,1-异步)',
    ignoreError tinyint(1) default 1 not null comment '是否忽略错误(0-不忽略,1-忽略)',
    status tinyint(1) default 1 not null comment '是否启用(0-禁用,1-启用)',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete tinyint default 0 not null comment '是否删除'
    )
    comment '服务处理器配置表';

DROP TABLE IF EXISTS QRTZ_FIRED_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_PAUSED_TRIGGER_GRPS;
DROP TABLE IF EXISTS QRTZ_SCHEDULER_STATE;
DROP TABLE IF EXISTS QRTZ_LOCKS;
DROP TABLE IF EXISTS QRTZ_SIMPLE_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_SIMPROP_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_CRON_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_BLOB_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_JOB_DETAILS;
DROP TABLE IF EXISTS QRTZ_CALENDARS;


CREATE TABLE QRTZ_JOB_DETAILS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    JOB_NAME  VARCHAR(200) NOT NULL,
    JOB_GROUP VARCHAR(200) NOT NULL,
    DESCRIPTION VARCHAR(250) NULL,
    JOB_CLASS_NAME   VARCHAR(250) NOT NULL,
    IS_DURABLE VARCHAR(1) NOT NULL,
    IS_NONCONCURRENT VARCHAR(1) NOT NULL,
    IS_UPDATE_DATA VARCHAR(1) NOT NULL,
    REQUESTS_RECOVERY VARCHAR(1) NOT NULL,
    JOB_DATA BLOB NULL,
    PRIMARY KEY (SCHED_NAME,JOB_NAME,JOB_GROUP)
);

CREATE TABLE QRTZ_TRIGGERS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    JOB_NAME  VARCHAR(200) NOT NULL,
    JOB_GROUP VARCHAR(200) NOT NULL,
    DESCRIPTION VARCHAR(250) NULL,
    NEXT_FIRE_TIME BIGINT(13) NULL,
    PREV_FIRE_TIME BIGINT(13) NULL,
    PRIORITY INTEGER NULL,
    TRIGGER_STATE VARCHAR(16) NOT NULL,
    TRIGGER_TYPE VARCHAR(8) NOT NULL,
    START_TIME BIGINT(13) NOT NULL,
    END_TIME BIGINT(13) NULL,
    CALENDAR_NAME VARCHAR(200) NULL,
    MISFIRE_INSTR SMALLINT(2) NULL,
    JOB_DATA BLOB NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,JOB_NAME,JOB_GROUP)
        REFERENCES QRTZ_JOB_DETAILS(SCHED_NAME,JOB_NAME,JOB_GROUP)
);

CREATE TABLE QRTZ_SIMPLE_TRIGGERS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    REPEAT_COUNT BIGINT(7) NOT NULL,
    REPEAT_INTERVAL BIGINT(12) NOT NULL,
    TIMES_TRIGGERED BIGINT(10) NOT NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
        REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE QRTZ_CRON_TRIGGERS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    CRON_EXPRESSION VARCHAR(200) NOT NULL,
    TIME_ZONE_ID VARCHAR(80),
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
        REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE QRTZ_SIMPROP_TRIGGERS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    STR_PROP_1 VARCHAR(512) NULL,
    STR_PROP_2 VARCHAR(512) NULL,
    STR_PROP_3 VARCHAR(512) NULL,
    INT_PROP_1 INT NULL,
    INT_PROP_2 INT NULL,
    LONG_PROP_1 BIGINT NULL,
    LONG_PROP_2 BIGINT NULL,
    DEC_PROP_1 NUMERIC(13,4) NULL,
    DEC_PROP_2 NUMERIC(13,4) NULL,
    BOOL_PROP_1 VARCHAR(1) NULL,
    BOOL_PROP_2 VARCHAR(1) NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
    REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE QRTZ_BLOB_TRIGGERS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    BLOB_DATA BLOB NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
        REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE QRTZ_CALENDARS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    CALENDAR_NAME  VARCHAR(200) NOT NULL,
    CALENDAR BLOB NOT NULL,
    PRIMARY KEY (SCHED_NAME,CALENDAR_NAME)
);

CREATE TABLE QRTZ_PAUSED_TRIGGER_GRPS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_GROUP  VARCHAR(200) NOT NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_GROUP)
);

CREATE TABLE QRTZ_FIRED_TRIGGERS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    ENTRY_ID VARCHAR(95) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    INSTANCE_NAME VARCHAR(200) NOT NULL,
    FIRED_TIME BIGINT(13) NOT NULL,
    SCHED_TIME BIGINT(13) NOT NULL,
    PRIORITY INTEGER NOT NULL,
    STATE VARCHAR(16) NOT NULL,
    JOB_NAME VARCHAR(200) NULL,
    JOB_GROUP VARCHAR(200) NULL,
    IS_NONCONCURRENT VARCHAR(1) NULL,
    REQUESTS_RECOVERY VARCHAR(1) NULL,
    PRIMARY KEY (SCHED_NAME,ENTRY_ID)
);

CREATE TABLE QRTZ_SCHEDULER_STATE
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    INSTANCE_NAME VARCHAR(200) NOT NULL,
    LAST_CHECKIN_TIME BIGINT(13) NOT NULL,
    CHECKIN_INTERVAL BIGINT(13) NOT NULL,
    PRIMARY KEY (SCHED_NAME,INSTANCE_NAME)
);

CREATE TABLE QRTZ_LOCKS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    LOCK_NAME  VARCHAR(40) NOT NULL,
    PRIMARY KEY (SCHED_NAME,LOCK_NAME)
);


commit;






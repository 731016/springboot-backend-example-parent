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
                              runningStatus TINYINT NOT NULL DEFAULT 0 COMMENT '运行状态(0-停止,1-运行中)'
);

-- 数据统计主表
CREATE TABLE data_statistics (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                 pointCode VARCHAR(50) NOT NULL COMMENT '点位编码',
                                 startTime DATETIME NOT NULL COMMENT '开始时间',
                                 endTime DATETIME COMMENT '结束时间',
                                 maximumValue DECIMAL(10,2) COMMENT '最大值',
                                 minimumValue DECIMAL(10,2) COMMENT '最小值',
                                 averageValue DECIMAL(10,2) COMMENT '平均值',
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
                             value DECIMAL(10,2) NOT NULL COMMENT '采集值',
                             attributeName VARCHAR(50) COMMENT '属性名',
                             statisticsId BIGINT COMMENT '关联的统计ID',
                             createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             isDeleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除(0-未删除,1-已删除)',
                             KEY idx_pointCode (pointCode),
                             KEY idx_collectTime (collectTime),
                             KEY idx_statisticsId (statisticsId)
);

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






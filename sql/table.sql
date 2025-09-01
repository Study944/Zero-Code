create database if not exists zero_code;

use zero_code;
-- 用户表
create table if not exists user
(
    id            bigint comment 'id' primary key,
    user_account  varchar(256)                           not null comment '账号',
    user_password varchar(512)                           not null comment '密码',
    user_name     varchar(256)                           null comment '用户昵称',
    user_avatar   varchar(1024)                          null comment '用户头像',
    user_profile  varchar(512)                           null comment '用户简介',
    user_role     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    create_time   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    UNIQUE KEY uk_userAccount (user_account),
    INDEX idx_userName (user_name)
) comment '用户';

-- 应用表
create table if not exists app
(
    id            bigint primary key comment 'id',
    app_name      varchar(256)                       not null comment '应用名称',
    app_desc      varchar(512)                       null comment '应用描述',
    app_icon      varchar(1024)                      null comment '应用图标',
    init_prompt   text                               not null comment '应用初始化提示词',
    generate_type varchar(256)                       null comment '生成模式：text单文件/多文件',
    user_id       bigint                             not null comment '创建者id',
    priority      int      default 0 comment '应用优先级',
    deploy_key    varchar(256) comment '部署密钥',
    deploy_time   datetime comment '部署时间',
    create_time   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_appName (app_name),
    index idx_userId (user_id)
) comment '应用';

-- 对话历史表
create table if not exists chat_history
(
    id           bigint auto_increment comment 'id' primary key,
    app_id       bigint                             not null comment '应用id',
    user_id      bigint                             not null comment '用户id',
    message      text                               not null comment '消息内容',
    message_type varchar(64)                        null comment '消息类型：ai/user',
    create_time  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_appId (app_id),
    index idx_appId_createTime (app_id, create_time)
) comment '对话历史';
DROP INDEX `uqe_client_id` ON `jmqtt_session`;
DROP INDEX `idx_client_id` ON `jmqtt_subscription`;
DROP INDEX `idx_topic` ON `jmqtt_subscription`;
DROP INDEX `idx_client_id` ON `jmqtt_inflow_message`;
DROP INDEX `idx_msg_id` ON `jmqtt_inflow_message`;
DROP INDEX `idx_client_id` ON `jmqtt_outflow_message`;
DROP INDEX `idx_msg_id` ON `jmqtt_outflow_message`;
DROP INDEX `idx_client_id` ON `jmqtt_offline_message`;
DROP INDEX `idx_gmt_create` ON `jmqtt_offline_message`;
DROP INDEX `uqe_topic` ON `jmqtt_retain_message`;
DROP INDEX `idx_client_id` ON `jmqtt_outflow_sec_message`;
DROP INDEX `idx_msg_id` ON `jmqtt_outflow_sec_message`;

DROP TABLE `jmqtt_session`;
DROP TABLE `jmqtt_subscription`;
DROP TABLE `jmqtt_inflow_message`;
DROP TABLE `jmqtt_outflow_message`;
DROP TABLE `jmqtt_offline_message`;
DROP TABLE `jmqtt_retain_message`;
DROP TABLE `jmqtt_outflow_sec_message`;

CREATE TABLE `jmqtt_session` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
`client_id` varchar(64) NOT NULL COMMENT '客户端id',
`state` varchar(12) NOT NULL COMMENT '状态：ONLINE,OFFLINE两种',
`offline_time` bigint(20) NOT NULL COMMENT 'OFFLINE状态时对应的离线时间戳（只有cleanStart为0时候离线才有该数据）',
PRIMARY KEY (`id`) ,
UNIQUE INDEX `uqe_client_id` (`client_id` ASC)
)
COMMENT = '客户端会话状态';


CREATE TABLE `jmqtt_subscription` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
`client_id` varchar(64) NOT NULL COMMENT '客户端id',
`topic` varchar(128) NOT NULL COMMENT '订阅的topic',
`qos` tinyint(4) NOT NULL COMMENT '对应的qos',
PRIMARY KEY (`id`) ,
INDEX `idx_client_id` (`client_id` ASC),
INDEX `idx_topic` (`topic` ASC)
)
COMMENT = '客户端订阅关系';


CREATE TABLE `jmqtt_inflow_message` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
`client_id` varchar(64) NOT NULL COMMENT '设备id',
`msg_id` int(11) NOT NULL COMMENT '消息id',
`content` text NOT NULL COMMENT '消息体内容',
`gmt_create` datetime(6) NOT NULL COMMENT '消息保存时间（对应消息接收时间）',
PRIMARY KEY (`id`) ,
INDEX `idx_client_id` (`client_id` ASC),
INDEX `idx_msg_id` (`msg_id` ASC)
)
COMMENT = '入栈消息表';


CREATE TABLE `jmqtt_outflow_message` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
`client_id` varchar(64) NOT NULL COMMENT '目标客户端id',
`msg_id` int(11) NOT NULL COMMENT '消息id',
`content` text NOT NULL COMMENT '消息内容',
`gmt_create` datetime(6) NOT NULL COMMENT '消息缓存时间',
PRIMARY KEY (`id`) ,
INDEX `idx_client_id` (`client_id` ASC),
INDEX `idx_msg_id` (`msg_id` ASC)
)
COMMENT = '出栈消息表';


CREATE TABLE `jmqtt_offline_message` (
`id` bigint(20) NOT NULL AUTO_INCREMENT,
`client_id` varchar(64) NOT NULL COMMENT '客户端id',
`content` text NOT NULL COMMENT '消息体',
`gmt_create` datetime(6) NOT NULL COMMENT '创建时间',
PRIMARY KEY (`id`) ,
INDEX `idx_client_id` (`client_id` ASC),
INDEX `idx_gmt_create` (`gmt_create` ASC)
)
COMMENT = '离线消息表';


CREATE TABLE `jmqtt_retain_message` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
`topic` varchar(128) NOT NULL COMMENT '所属topic',
`content` text NOT NULL COMMENT '消息体',
PRIMARY KEY (`id`) ,
UNIQUE INDEX `uqe_topic` (`topic` ASC)
)
COMMENT = '保留消息表';


CREATE TABLE `jmqtt_event` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键：也是集群节点批量拉消息的offset',
`content` text NOT NULL COMMENT '消息体',
`gmt_create` datetime(6) NOT NULL COMMENT '创建时间',
`jmqtt_ip` varchar(24) NOT NULL COMMENT 'jmqtt服务器ip，发送该消息到集群中的broker ip',
`event_code` varchar(24) NOT NULL COMMENT '事件码',
PRIMARY KEY (`id`)
)
COMMENT = 'jmqtt 集群事件转发表：由发送端将消息发送到该表中，其他节点批量拉取该表中的事件进行处理';



CREATE TABLE `jmqtt_outflow_sec_message` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
`client_id` varchar(64) NOT NULL COMMENT '目标客户端id',
`msg_id` int(11) NOT NULL COMMENT '消息id',
`gmt_create` datetime(6) NOT NULL COMMENT '消息缓存时间',
PRIMARY KEY (`id`) ,
INDEX `idx_client_id` (`client_id` ASC),
INDEX `idx_msg_id` (`msg_id` ASC)
)
COMMENT = '发送qos2消息后的第二阶段的消息缓存表：接收pubRec后保留clientId，msgId等报文。设备重连时候进行重发';

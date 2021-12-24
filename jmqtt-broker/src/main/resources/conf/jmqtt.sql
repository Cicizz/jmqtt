SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


-- ----------------------------
-- Table structure for jmqtt_event
-- ----------------------------
DROP TABLE IF EXISTS `jmqtt_event`;
CREATE TABLE `jmqtt_event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键：也是集群节点批量拉消息的offset',
`tenant_code` varchar(32) NOT NULL COMMENT '所属租户code',
`biz_code` varchar(32) NOT NULL COMMENT '所属业务code',
`content` text NOT NULL COMMENT '消息体',
`gmt_create` bigint(20) NOT NULL COMMENT '创建时间',
`jmqtt_ip` varchar(24) NOT NULL COMMENT 'jmqtt服务器ip，发送该消息到集群中的broker ip',
`event_code` int(4) NOT NULL COMMENT '事件码',
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='jmqtt 集群事件转发表：由发送端将消息发送到该表中，其他节点批量拉取该表中的事件进行处理';

-- ----------------------------
-- Table structure for jmqtt_inflow_message
-- ----------------------------
DROP TABLE IF EXISTS `jmqtt_inflow_message`;
CREATE TABLE `jmqtt_inflow_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
`tenant_code` varchar(32) NOT NULL COMMENT '所属租户code',
`biz_code` varchar(32) NOT NULL COMMENT '所属业务code',
`client_id` varchar(64) NOT NULL COMMENT '设备id',
`msg_id` int(11) NOT NULL COMMENT '消息id',
`content` text NOT NULL COMMENT '消息体内容',
`gmt_create` bigint(20) NOT NULL COMMENT '消息保存时间（对应消息接收时间）',
PRIMARY KEY (`id`),
UNIQUE KEY `uqe_client_id_msg_id` (`client_id`,`msg_id`) USING BTREE,
KEY `idx_client_id` (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入栈消息表';

-- ----------------------------
-- Table structure for jmqtt_offline_message
-- ----------------------------
DROP TABLE IF EXISTS `jmqtt_offline_message`;
CREATE TABLE `jmqtt_offline_message` (
  `id` bigint(20) AUTO_INCREMENT NOT NULL,
`tenant_code` varchar(32) NOT NULL COMMENT '所属租户code',
`biz_code` varchar(32) NOT NULL COMMENT '所属业务code',
`client_id` varchar(64) NOT NULL COMMENT '客户端id',
`content` text NOT NULL COMMENT '消息体',
`gmt_create` bigint(20) NOT NULL COMMENT '创建时间',
PRIMARY KEY (`id`),
KEY `idx_client_id` (`client_id`),
KEY `idx_gmt_create` (`gmt_create`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='离线消息表';

-- ----------------------------
-- Table structure for jmqtt_outflow_message
-- ----------------------------
DROP TABLE IF EXISTS `jmqtt_outflow_message`;
CREATE TABLE `jmqtt_outflow_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
`tenant_code` varchar(32) NOT NULL COMMENT '所属租户code',
`biz_code` varchar(32) NOT NULL COMMENT '所属业务code',
`client_id` varchar(64) NOT NULL COMMENT '目标客户端id',
`msg_id` int(11) NOT NULL COMMENT '消息id',
`content` text NOT NULL COMMENT '消息内容',
`gmt_create` bigint(20) NOT NULL COMMENT '消息缓存时间',
PRIMARY KEY (`id`),
UNIQUE KEY `uqe_client_id_msg_id` (`msg_id`,`client_id`) USING BTREE,
KEY `idx_client_id` (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出栈消息表';

-- ----------------------------
-- Table structure for jmqtt_outflow_sec_message
-- ----------------------------
DROP TABLE IF EXISTS `jmqtt_outflow_sec_message`;
CREATE TABLE `jmqtt_outflow_sec_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
`tenant_code` varchar(32) NOT NULL COMMENT '所属租户code',
`biz_code` varchar(32) NOT NULL COMMENT '所属业务code',
`client_id` varchar(64) NOT NULL COMMENT '目标客户端id',
`msg_id` int(11) NOT NULL COMMENT '消息id',
`gmt_create` bigint(20) NOT NULL COMMENT '消息缓存时间',
PRIMARY KEY (`id`),
UNIQUE KEY `uqe_client_id_msg_id` (`msg_id`,`client_id`) USING BTREE,
KEY `idx_client_id` (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发送qos2消息后的第二阶段的消息缓存表：接收pubRec后保留clientId，msgId等报文。设备重连时候进行重发';

-- ----------------------------
-- Table structure for jmqtt_retain_message
-- ----------------------------
DROP TABLE IF EXISTS `jmqtt_retain_message`;
CREATE TABLE `jmqtt_retain_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
`tenant_code` varchar(32) NOT NULL COMMENT '所属租户code',
`biz_code` varchar(32) NOT NULL COMMENT '所属业务code',
`topic` varchar(128) NOT NULL COMMENT '所属topic',
`content` text NOT NULL COMMENT '消息体',
PRIMARY KEY (`id`),
UNIQUE KEY `uqe_topic` (`topic`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='保留消息表';

-- ----------------------------
-- Table structure for jmqtt_session
-- ----------------------------
DROP TABLE IF EXISTS `jmqtt_session`;
CREATE TABLE `jmqtt_session` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
`tenant_code` varchar(32) NOT NULL COMMENT '所属租户code',
`biz_code` varchar(32) NOT NULL COMMENT '所属业务code',
`client_id` varchar(64) NOT NULL COMMENT '客户端id',
`state` varchar(12) NOT NULL COMMENT '状态：ONLINE,OFFLINE两种',
`offline_time` bigint(20) DEFAULT NULL COMMENT 'OFFLINE状态时对应的离线时间戳（只有cleanStart为0时候离线才有该数据）',
PRIMARY KEY (`id`),
UNIQUE KEY `uqe_client_id` (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户端会话状态';

-- ----------------------------
-- Table structure for jmqtt_subscription
-- ----------------------------
DROP TABLE IF EXISTS `jmqtt_subscription`;
CREATE TABLE `jmqtt_subscription` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
`tenant_code` varchar(32) NOT NULL COMMENT '所属租户code',
`biz_code` varchar(32) NOT NULL COMMENT '所属业务code',
`client_id` varchar(64) NOT NULL COMMENT '客户端id',
`topic` varchar(128) NOT NULL COMMENT '订阅的topic',
`qos` tinyint(4) NOT NULL COMMENT '对应的qos',
PRIMARY KEY (`id`),
KEY `idx_client_id` (`client_id`),
KEY `idx_topic` (`topic`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户端订阅关系';

-- ----------------------------
-- Table structure for jmqtt_will_message
-- ----------------------------
DROP TABLE IF EXISTS `jmqtt_will_message`;
CREATE TABLE `jmqtt_will_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
`tenant_code` varchar(32) NOT NULL COMMENT '所属租户code',
`biz_code` varchar(32) NOT NULL COMMENT '所属业务code',
`client_id` varchar(64) NOT NULL COMMENT '客户端id',
`content` text NOT NULL COMMENT '消息体',
`gmt_create` bigint(20) NOT NULL COMMENT '创建时间',
PRIMARY KEY (`id`),
UNIQUE KEY `uqe_client_id` (`client_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='遗嘱消息表';

SET FOREIGN_KEY_CHECKS = 1;

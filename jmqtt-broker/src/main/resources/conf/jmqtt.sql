DROP INDEX `uk_client_id` ON `jmqtt_session`;
DROP INDEX `uk_client_id_topic` ON `jmqtt_subscription`;
DROP INDEX `idx_client_id` ON `jmqtt_message`;
DROP INDEX `idx_client_id_ack` ON `jmqtt_client_inbox`;
DROP INDEX `uk_topic` ON `jmqtt_retain_message`;

DROP TABLE `jmqtt_session`;
DROP TABLE `jmqtt_subscription`;
DROP TABLE `jmqtt_message`;
DROP TABLE `jmqtt_client_inbox`;
DROP TABLE `jmqtt_retain_message`;
DROP TABLE `jmqtt_cluster_event`;

CREATE TABLE `jmqtt_session` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
`client_id` varchar(64) NOT NULL COMMENT '客户端id',
`online` varchar(12) NOT NULL COMMENT '状态：ONLINE,OFFLINE两种',
`transport_protocol` varchar(20) NOT NULL COMMENT '传输协议：MQTT,TCP,COAP等',
`client_ip` varchar(32) NOT NULL COMMENT '客户端ip',
`server_ip` varchar(32) NOT NULL COMMENT '连接的服务端ip',
`last_offline_time` timestamp NULL COMMENT '上一次离线时间',
`online_time` timestamp NOT NULL COMMENT '最近连接在线时间',
`properties` text NULL COMMENT '扩展信息',
PRIMARY KEY (`id`) ,
UNIQUE INDEX `uk_client_id` (`client_id` ASC)
)
COMMENT = '客户端会话状态';
CREATE TABLE `jmqtt_subscription` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
`client_id` varchar(64) NOT NULL COMMENT '客户端id',
`topic` varchar(255) NOT NULL COMMENT '订阅的topic',
`subscribe_time` timestamp NOT NULL COMMENT '订阅时间',
`properties` text NULL COMMENT '订阅的额外属性',
PRIMARY KEY (`id`) ,
UNIQUE INDEX `uk_client_id_topic` (`client_id` ASC, `topic` ASC)
)
COMMENT = '客户端订阅关系';
CREATE TABLE `jmqtt_message` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
`source` varchar(32) NOT NULL COMMENT '消息来源：DEVICE/PLATFORM',
`content` mediumblob NOT NULL COMMENT '消息体内容：字节',
`topic` varchar(64) NULL COMMENT '发送的目标topic',
`from_client_id` varchar(64) NULL COMMENT '消息来源若是设备，从属设备id',
`stored_time` timestamp NOT NULL COMMENT '消息落库时间',
`properties` text NULL COMMENT '消息额外属性',
PRIMARY KEY (`id`) ,
INDEX `idx_client_id` (`source` ASC)
)
COMMENT = '入栈消息表';
CREATE TABLE `jmqtt_client_inbox` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
`client_id` varchar(64) NOT NULL COMMENT '客户端id',
`message_id` bigint(20) NOT NULL COMMENT '消息id',
`ack` tinyint(2) NOT NULL COMMENT '客户端是否收到消息：0未收到，1到达',
`stored_time` timestamp NOT NULL COMMENT '收件箱时间',
`ack_time` timestamp NULL,
PRIMARY KEY (`id`) ,
INDEX `idx_client_id_ack` (`client_id` ASC, `ack` ASC)
)
COMMENT = '设备消息收件箱表';
CREATE TABLE `jmqtt_retain_message` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
`topic` varchar(255) NOT NULL COMMENT '所属topic',
`content` mediumblob NOT NULL COMMENT '消息体内容',
`from_client_id` varchar(64) NOT NULL COMMENT 'retain消息来源设备id',
`stored_time` timestamp NOT NULL COMMENT '存储事件',
`properties` text NOT NULL COMMENT '额外属性',
PRIMARY KEY (`id`) ,
UNIQUE INDEX `uk_topic` (`topic` ASC)
)
COMMENT = 'mqtt协议的retain 消息表';
CREATE TABLE `jmqtt_cluster_event` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键：也是集群节点批量拉消息的offset',
`content` longtext NOT NULL COMMENT '消息体',
`gmt_create` timestamp(6) NOT NULL COMMENT '创建时间',
`node_ip` varchar(32) NOT NULL COMMENT 'jmqtt集群节点ip',
`event_code` varchar(64) NOT NULL COMMENT '事件码：参考代码',
PRIMARY KEY (`id`)
)
COMMENT = 'jmqtt 集群事件转发表：由发送端将消息发送到该表中，其他节点批量拉取该表中的事件进行处理';

package org.jmqtt.mqtt.subscription;

import org.apache.commons.lang3.StringUtils;
import org.jmqtt.mqtt.model.Subscription;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Subscription tree
 * 订阅树处理
 *  TODO 1.支持共享订阅
 *  TODO 2.支持 p2p消息(jmqtt 特有)
 *  TODO 2.支持系统topic $SYS -> 需要定时采集本机节点等信息下发
 */
public interface SubscriptionMatcher {

    /**
     * add subscribe
     * @return  true：new subscribe,dispatcher retain message
     *           false：no need to dispatcher retain message
     */
    boolean subscribe(Subscription subscription);

    boolean unSubscribe(String topic,String clientId);

    /**
     * 获取匹配该topic下的非共享订阅者
     * @param topic
     * @param clientId
     * @return
     */
    Set<Subscription> match(String topic, String clientId);

    /**
     * 发布消息的Topic与订阅的topic是否匹配
     */
    boolean isMatch(String pubTopic,String subTopic);

    static boolean isGroupTopic(String topic) {
        if (StringUtils.isNotEmpty(groupTopic(topic))) {
            return true;
        }
        return false;
    }

    /**
     * 获取指定共享主题中的订阅主题
     *
     * @param topic 待解析主题
     * @return 非共享主题，返回null
     */
    static String groupTopic(String topic) {
        Pattern pattern = Pattern.compile("(\\$share/[a-zA-Z0-9]+/)(\\S+)");
        Matcher m = pattern.matcher(topic);
        if (m.find()) {
            return m.group(2);
        }
        return null;
    }

}

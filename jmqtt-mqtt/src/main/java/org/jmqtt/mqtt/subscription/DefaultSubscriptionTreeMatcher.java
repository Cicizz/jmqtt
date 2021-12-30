package org.jmqtt.mqtt.subscription;

import org.apache.commons.lang3.StringUtils;
import org.jmqtt.mqtt.model.Subscription;
import org.jmqtt.support.log.JmqttLogger;
import org.jmqtt.support.log.LogUtil;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DefaultSubscriptionTreeMatcher implements SubscriptionMatcher {

    private static final Logger log = JmqttLogger.mqttLog;

    private static final String GROUP_STR = "$share";
    private final Object lock = new Object();
    private TreeNode root = new TreeNode(new Token("root"));
    private Token EMPTY = new Token("");
    private Token SINGLE = new Token("+");
    private Token MULTY = new Token("#");
    private Token GROUP = new Token(GROUP_STR);


    public DefaultSubscriptionTreeMatcher() {

    }

    public static void main(String[] args) {

        Pattern pattern = Pattern.compile("(\\$share/[a-zA-Z0-9]+/)(\\S+)");
        Matcher m = pattern.matcher("$share/g1/test/1");
        if (m.find()) {
            System.out.println("args0 = " + m.group(0));
            System.out.println("args1 = " + m.group(1));
            System.out.println("args2 = " + m.group(2));
        }
        System.out.println(
            "args = " + new DefaultSubscriptionTreeMatcher().isMatch("test/1", "$share/g1/test/#"));
    }

    @Override
    public boolean subscribe(Subscription subscription) {
        try {
            String topic = subscription.getTopic();
            TreeNode currentNode = recursionGetTreeNode(topic, root);
            Set<Subscription> subscriptions = currentNode.getSubscribers();
            if (subscriptions.contains(subscription)) {
                for (Subscription sub : subscriptions) {
                    if (sub.equals(subscription)) {
                        if (sub.getQos() == subscription.getQos()) {
                            return false;
                        } else {
                            sub.setQos(subscription.getQos());
                            return true;
                        }
                    }
                }
            }
            currentNode.addSubscriber(subscription);
        } catch (Exception ex) {
            LogUtil.warn(log, "[Subscription] -> Subscribe failed,clientId={},topic={},qos={}",
                subscription.getClientId(), subscription.getTopic(), subscription.getQos());
            return true;
        }
        return true;
    }

    @Override
    public boolean unSubscribe(String topic, String clientId) {
        TreeNode currentNode = recursionGetTreeNode(topic, root);
        currentNode.getSubscribers().remove(new Subscription(clientId, topic, 1));
        return true;
    }

    private TreeNode recursionGetTreeNode(String topic, TreeNode node) {
        String[] tokens = topic.split("/");
        Token token = new Token(tokens[0]);
        TreeNode matchNode = node.getChildNodeByToken(token);
        if (Objects.isNull(matchNode)) {
            synchronized (lock) {
                matchNode = node.getChildNodeByToken(token);
                if (Objects.isNull(matchNode)) {
                    matchNode = new TreeNode(token);
                    node.addChild(matchNode);
                }
            }
        }
        if (tokens.length > 1) {
            String childTopic = topic.substring(topic.indexOf("/") + 1);
            return recursionGetTreeNode(childTopic, matchNode);
        } else {
            return matchNode;
        }
    }

    @Override
    public Set<Subscription> match(String topic, String clientId) {
        Set<Subscription> subscriptions = new HashSet<>();
        recursionMatch(topic, root, false, subscriptions);
        return subscriptions;
    }

    /**
     * TODO 按照均衡策略选择一个订阅者;
     * <pre>
     * 策略：
     * random	在所有订阅者中随机选择;
     * hash	按照发布者 ClientID 的哈希值;
     * </pre>
     */
    private Subscription selectOneByStrategy(Set<Subscription> group, String clientId) {
        return group.iterator().next();
    }

    @Override
    public boolean isMatch(String pubTopic, String subTopic) {
        if (pubTopic.equals(subTopic)) {
            return true;
        }
        String sharedTopic = SubscriptionMatcher.groupTopic(subTopic);
        if (StringUtils.isNotEmpty(sharedTopic)) {
            return innerIsMatch(pubTopic, sharedTopic);
        }

        return innerIsMatch(pubTopic, subTopic);
    }

    private boolean innerIsMatch(String pubTopic, String subTopic) {
        if (pubTopic.equals(subTopic)) {
            return true;
        }
        String[] pubTokenStr = pubTopic.split("/");
        String[] subTokenStr = subTopic.split("/");
        int pubLen = pubTokenStr.length;
        int subLen = subTokenStr.length;
        if (pubLen != subLen) {
            Token lastSubToken = new Token(subTokenStr[subLen - 1]);
            if (subLen > pubLen || (!lastSubToken.equals(MULTY))) {
                return false;
            }
        }
        for (int i = 0; i < pubLen; i++) {
            Token pubToken = new Token(pubTokenStr[i]);
            Token subToken = new Token(subTokenStr[i]);
            if (subToken.equals(MULTY)) {
                return true;
            } else if ((!pubToken.equals(subToken)) && (!subToken.equals(SINGLE))) {
                return false;
            }
            if (i == subLen - 1) {
                return true;
            }
        }
        return false;
    }

    private void recursionMatch(String topic, TreeNode node, boolean isGroupToken,
        Set<Subscription> subscriptions) {
        String[] topics = topic.split("/");
        Token token = new Token(topics[0]);
        List<TreeNode> childNodes = node.getChildren();
        if (topics.length > 1) {
            String nextTopic = topic.substring(topic.indexOf("/") + 1);
            for (TreeNode itemNode : childNodes) {

                if (isGroupToken) {
                    recursionMatch(topic, itemNode, false, subscriptions);
                }
                if (itemNode.getToken().equals(token) || itemNode.getToken().equals(SINGLE)) {
                    recursionMatch(nextTopic, itemNode, false, subscriptions);
                }
                if (itemNode.getToken().equals(GROUP)) {
                    recursionMatch(topic, itemNode, true, subscriptions);
                }
                if (itemNode.getToken().equals(MULTY)) {
                    subscriptions.addAll(itemNode.getSubscribers());
                }
            }
        } else {
            for (TreeNode itemNode : childNodes) {
                if (itemNode.getToken().equals(token) || itemNode.getToken().equals(SINGLE)
                    || itemNode.getToken().equals(MULTY)) {
                    subscriptions.addAll(itemNode.getSubscribers());
                }
            }
        }
    }


    class Token {

        String token;

        public Token(String token) {
            this.token = token;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Token token1 = (Token) o;
            return Objects.equals(token, token1.token);
        }

        @Override
        public int hashCode() {
            return Objects.hash(token);
        }
    }

    class TreeNode {

        private Token token;
        private Set<Subscription> subscribers = new CopyOnWriteArraySet<>();
        private List<TreeNode> children = new CopyOnWriteArrayList<>();

        public TreeNode(Token token) {
            this.token = token;
        }

        public void addSubscriber(Subscription subscription) {
            this.subscribers.add(subscription);
        }

        public void addChild(TreeNode treeNode) {
            this.children.add(treeNode);
        }

        public Token getToken() {
            return token;
        }

        public Set<Subscription> getSubscribers() {
            return subscribers;
        }

        public List<TreeNode> getChildren() {
            return this.children;
        }

        ;

        public TreeNode getChildNodeByToken(Token token) {
            for (TreeNode childNode : this.children) {
                if (childNode.getToken().equals(token)) {
                    return childNode;
                }
            }
            return null;
        }
    }
}

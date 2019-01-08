package org.jmqtt.broker.subscribe;

import org.jmqtt.common.bean.Subscription;
import org.jmqtt.common.log.LoggerName;
import org.jmqtt.store.SubscriptionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class DefaultSubscriptionTreeMatcher implements SubscriptionMatcher {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CLIENT_TRACE);

    private TreeNode root = new TreeNode(new Token("root"));
    private Token EMPTY = new Token("");
    private Token SINGLE = new Token("+");
    private Token MULTY = new Token("*");


    public DefaultSubscriptionTreeMatcher(){
    };

    @Override
    public int subscribe(String topic, Subscription subscription) {
        int rs;
        try{
            TreeNode currentNode = recursionGetTreeNode(topic,root);
            Set<Subscription> subscriptions = currentNode.getSubscribers();
            for(Subscription sub : subscriptions){
                if(sub.equals(subscription)){
                    if(sub.getQos() == subscription.getQos()){
                        rs = 2;
                        return rs;
                    }
                }
            }
            rs = 1;
            currentNode.addSubscriber(subscription);
        }catch(Exception ex){
            log.warn("[Subscription] -> Subscribe failed,clientId={},topic={},qos={}",subscription.getClientId(),subscription.getTopic(),subscription.getQos());
            rs = 0;
        }
        return rs;
    }

    @Override
    public boolean unSubscribe(String topic, String clientId) {
        TreeNode currentNode = recursionGetTreeNode(topic,root);
        currentNode.getSubscribers().remove(new Subscription(clientId,topic,1));
        return true;
    }

    private TreeNode recursionGetTreeNode(String topic,TreeNode node){
        String[] tokens = topic.split("/");
        Token token = new Token(tokens[0]);
        TreeNode matchNode = node.getChildNodeByToken(token);
        TreeNode currentNode =  matchNode;
        if(Objects.isNull(currentNode)){
            currentNode = new TreeNode(token);
            node.addChild(currentNode);
        }
        if(tokens.length > 1){
            String childTopic = topic.substring(topic.indexOf("/")+1);
            return recursionGetTreeNode(childTopic,currentNode);
        }else{
            return currentNode;
        }
    }

    @Override
    public Set<Subscription> match(String topic) {
        Set<Subscription> subscriptions =  new HashSet<>();
        recursionMatch(topic,root,subscriptions);
        return subscriptions;
    }

    @Override
    public boolean isMatch(String pubTopic, String subTopic) {
        String[] pubTokenStr = pubTopic.split("/");
        String[] subTokenStr = subTopic.split("/");
        int pubLen = pubTokenStr.length;
        int subLen = subTokenStr.length;
        if(pubLen != subLen){
            Token lastSubToken = new Token(subTokenStr[subLen-1]);
            if(subLen > pubLen || (!lastSubToken.equals(MULTY))){
                return false;
            }
        }
        for(int i = 0; i < pubLen; i++){
            Token pubToken = new Token(pubTokenStr[i]);
            Token subToken = new Token(subTokenStr[i]);
            if(subToken.equals(MULTY)){
                return true;
            }else if((!pubToken.equals(subToken)) && (!subToken.equals(SINGLE))){
                return false;
            }
            if(i == subLen-1){
                return false;
            }
        }
        return false;
    }

    private void recursionMatch(String topic,TreeNode node,Set<Subscription> subscriptions){
        String[] topics = topic.split("/");
        Token token = new Token(topics[0]);
        List<TreeNode> childNodes = node.getChildren();
        if(topics.length > 1){
            String nextTopic = topic.substring(topic.indexOf("/")+1);
            for(TreeNode itemNode : childNodes){
                if(itemNode.getToken().equals(token) || itemNode.getToken().equals(SINGLE)){
                    recursionMatch(nextTopic,itemNode,subscriptions);
                }
                if(itemNode.getToken().equals(MULTY)){
                    subscriptions.addAll(itemNode.getSubscribers());
                }
            }
        }else{
            for(TreeNode itemNode : childNodes){
                if(itemNode.getToken().equals(token)  || itemNode.getToken().equals(SINGLE) || itemNode.getToken().equals(MULTY)){
                    subscriptions.addAll(itemNode.getSubscribers());
                }
            }
        }
    }


    class Token{
        String token;

        public Token(String token){
            this.token = token;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
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
        private Set<Subscription /*  */> subscribers = new CopyOnWriteArraySet<>();
        private List<TreeNode> children = new CopyOnWriteArrayList<>();

        public TreeNode(Token token){
            this.token = token;
        }

        public void addSubscriber(Subscription subscription){
            this.subscribers.add(subscription);
        }

        public void addChild(TreeNode treeNode){
            this.children.add(treeNode);
        }

        public Token getToken() {
            return token;
        }

        public Set<Subscription> getSubscribers() {
            return subscribers;
        }

        public List<TreeNode> getChildren(){
          return this.children;
        };

        public TreeNode getChildNodeByToken(Token token){
            for(TreeNode childNode : this.children){
                if(childNode.getToken().equals(token)){
                    return childNode;
                }
            }
            return null;
        }
    }

}

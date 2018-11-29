package org.jmqtt.broker.subscribe;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class DefaultSubscriptionTreeMatcher implements SubscriptionMatcher {

    private TreeNode root = new TreeNode(new Token("root"));

    public DefaultSubscriptionTreeMatcher(){};

    @Override
    public boolean subscribe(String topic,String clientId) {
        TreeNode currentNode = recursionGetTreeNode(topic,root);
        currentNode.addSubscriber(clientId);
        return true;
    }

    private TreeNode recursionGetTreeNode(String topic,TreeNode node){
        String[] tokens = topic.split("/");
        Token token = new Token(tokens[0]);
        TreeNode matchNode = node.getChildNodeByToken(token);
        if(tokens.length > 1){
            String childTopic = topic.substring(topic.indexOf("/"));
            return recursionGetTreeNode(childTopic,matchNode);
        }else{
            return matchNode;
        }
    }

    @Override
    public List<String> match(String topic) {
        return null;
    }

    class Token{
        final Token EMPTY = new Token(" ");
        final Token SINGLE = new Token(" ");
        final Token MULTY = new Token(" ");
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
        private Set<String /* clientId */> subscribers = new CopyOnWriteArraySet<>();
        private List<TreeNode> children = new CopyOnWriteArrayList<>();

        public TreeNode(Token token){
            this.token = token;
        }

        public void addSubscriber(String clientId){
            this.subscribers.add(clientId);
        }

        public void addChild(TreeNode treeNode){
            this.children.add(treeNode);
        }

        public Token getToken() {
            return token;
        }

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

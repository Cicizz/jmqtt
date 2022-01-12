package org.jmqtt.bus.subscription;

import io.netty.util.internal.StringUtil;
import org.jmqtt.bus.subscription.model.Subscription;

class DumpTreeVisitor implements CTrie.IVisitor<String> {

    String s = "";

    @Override
    public void visit(CNode node, int deep) {
        String indentTabs = indentTabs(deep);
        s += indentTabs + (node.getToken() == null ? "''" : node.getToken().toString()) + prettySubscriptions(node) + "\n";
    }

    private String prettySubscriptions(CNode node) {
        if (node instanceof TNode) {
            return "TNode";
        }
        if (node.subscriptions.isEmpty()) {
            return StringUtil.EMPTY_STRING;
        }
        StringBuilder subScriptionsStr = new StringBuilder(" ~~[");
        int counter = 0;
        for (Subscription couple : node.subscriptions) {
            subScriptionsStr
                .append("{filter=").append(couple.getTopic()).append(", ")
                .append("properties=").append(couple.getProperties()).append(", ")
                .append("client='").append(couple.getClientId()).append("'}");
            counter++;
            if (counter < node.subscriptions.size()) {
                subScriptionsStr.append(";");
            }
        }
        return subScriptionsStr.append("]").toString();
    }

    private String indentTabs(int deep) {
        StringBuilder s = new StringBuilder();
        if (deep > 0) {
            s.append("    ");
            for (int i = 0; i < deep - 1; i++) {
                s.append("| ");
            }
            s.append("|-");
        }
        return s.toString();
    }

    @Override
    public String getResult() {
        return s;
    }
}

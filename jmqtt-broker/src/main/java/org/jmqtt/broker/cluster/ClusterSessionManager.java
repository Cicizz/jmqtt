package org.jmqtt.broker.cluster;

import org.jmqtt.broker.cluster.command.CommandCode;
import org.jmqtt.broker.cluster.command.CommandReqOrResp;
import org.jmqtt.common.log.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ClusterSessionManager implements ClusterHandler {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.CLUSTER);

    public CommandReqOrResp process(CommandReqOrResp request) {
        log.debug("[Cluster command],commandCode={}", request.getCommandCode());
        switch (request.getCommandCode()) {
            case CommandCode
                    .CONNECT_QUERY_LAST_STATE:
                return queryLastState(request);
            case CommandCode.CONNECT_GET_SUBSCRIPTIONS:
                return getSubscriptions(request);
            default:
                return errorResponse();
        }
    }

    protected abstract CommandReqOrResp queryLastState(CommandReqOrResp request);

    protected abstract CommandReqOrResp getSubscriptions(CommandReqOrResp request);

    CommandReqOrResp errorResponse() {
        return new CommandReqOrResp(CommandCode.ERROR_CODE);
    }
}
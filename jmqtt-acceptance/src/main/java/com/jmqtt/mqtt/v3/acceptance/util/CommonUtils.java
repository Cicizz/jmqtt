package com.jmqtt.mqtt.v3.acceptance.util;


import org.slf4j.helpers.MessageFormatter;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CommonUtils {

    private static final char SPLIT_CHAR = ',';

    private static final char STRING_SIGNATURE = '\"';

    private static final char INSIDE_SIGNATURE = '\\';

    private static final String STRING_SIGNATURE_STR = "\"";

    private static final String INSIDE_REPLACEMENT = "\\\\\"";

    private CommonUtils() {
    }

    public static Map<String, String> filterMapIgnoreCase(final Map<String, String> origin,
            final List<String> whiteList) {
        if (CollectionUtils.isEmpty(origin) || CollectionUtils.isEmpty(whiteList)) {
            return new HashMap<>(0);
        }

        final Map<String, String> filteredHeaders = new HashMap<>();
        origin.keySet()
                .forEach(headerName -> {
                    for (final String requiredHeader : whiteList) {
                        if (requiredHeader.equalsIgnoreCase(headerName)) {
                            filteredHeaders.put(headerName, origin.get(headerName));
                        }
                    }
                });
        return filteredHeaders;
    }

    public static String formatString(final String template, final Object... args) {
        return MessageFormatter.arrayFormat(template, args).getMessage();
    }



}

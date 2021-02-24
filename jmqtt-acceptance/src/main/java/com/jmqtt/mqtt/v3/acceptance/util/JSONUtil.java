package com.jmqtt.mqtt.v3.acceptance.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * JSONUtil is to encapsulate Gson method, and make it easier to call with simple parameters
 */
public final class JSONUtil {

    private static final Logger logger = LoggerFactory.getLogger(JSONUtil.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private static final ObjectMapper STRICT_OBJECT_MAPPER = new ObjectMapper()
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private JSONUtil() {
    }

    /**
     * Parses the given JSON string with the specified class and then returns a new instance of that class. If the given
     * JSON string is blank, {@code null} is returned.
     *
     * @param jsonString the JSON string
     * @param clazz the class
     * @param <T> the type of the class
     * @return a new instance of the class, or {@code null} if failed
     */
    public static <T> T fromJson(final String jsonString, final Class<T> clazz) {
        if (StringUtils.isNotBlank(jsonString)) {
            try {
                return OBJECT_MAPPER.readValue(jsonString, clazz);
            } catch (final IOException ex) {
                logger.warn("Exception when de-serializing " + clazz + " with " + jsonString, ex);
            }
        }

        return null;
    }

    /**
     * Parses the given JSON string with the specified type reference and then returns a new instance of that type. If
     * the given JSON string is blank, {@code null} is returned. If you want to parse JSON to a parameterized type more
     * than two layer like List&lt;String&gt; or List&lt;Map&lt;String,SomeType&gt;&gt;, use this method.  Example :
     * List&lt;String&gt; should be put as new TypeToken&lt;List&lt;String&gt;&gt;(){}
     *
     * @param jsonString the JSON string
     * @param typeReference the type reference
     * @param <T> the type
     * @return a new instance of the class, or {@code null} if failed
     */
    public static <T> T fromJson(final String jsonString, final TypeReference<T> typeReference) {
        if (StringUtils.isNotBlank(jsonString)) {
            try {
                return OBJECT_MAPPER.readValue(jsonString, typeReference);
            } catch (final IOException ex) {
                logger.warn("Exception when de-serializing " + jsonString, ex);
            }
        }

        return null;
    }

    /**
     * Parses the given object and returns a JSON string representing this object. If the given object is {@code null},
     * it returns an empty string.
     *
     * @param object the object
     * @return a JSON string representing this object, or {@code null} if null
     */
    public static String toJson(final Object object) {
        if (object != null) {
            try {
                return OBJECT_MAPPER.writeValueAsString(object);
            } catch (final JsonProcessingException ex) {
                logger.warn("Exception when serializing " + object, ex);
            }
        }

        return "";
    }

    /**
     * Parses the given JSON string with the specified class and then returns a new instance of that class. If the given
     * JSON string is blank, {@code null} is returned.
     *
     * @param jsonString the JSON string
     * @param clazz the class
     * @param <T> the type of the class
     * @return a new instance of the class, or {@code null} if failed
     */
    public static <T> T fromJsonStrict(final String jsonString, final Class<T> clazz) {
        if (StringUtils.isNotBlank(jsonString)) {
            try {
                return STRICT_OBJECT_MAPPER.readValue(jsonString, clazz);
            } catch (final Exception ex) {
                logger.warn("Exception when de-serializing " + clazz + " with " + jsonString, ex);
            }
        }

        return null;
    }

    /**
     * validate the given jsonString
     *
     * @param jsonString
     * @return
     */
    public static Boolean isValidJson(final String jsonString) {
        if (StringUtils.isNotBlank(jsonString)) {
            try {
                return OBJECT_MAPPER.readValue(jsonString, JsonObject.class) == null ? false : true;
            } catch (final Exception ex) {
                logger.warn("Exception when de-serializing " + " with " + jsonString, ex);
            }
        }

        return false;
    }

    /**
     * Parses the given JSON string with the specified class and then returns a new instance of that class. If the given
     * JSON string is blank, {@code null} is returned.
     *
     * @param jsonString the JSON string
     * @param clazz the class
     * @param <T> the type of the class
     * @return a new instance of the class, or {@code null} if failed
     */
    public static <T> T fromJsonStrict(final String jsonString, final TypeReference<T> clazz) {
        if (StringUtils.isNotBlank(jsonString)) {
            try {
                return STRICT_OBJECT_MAPPER.readValue(jsonString, clazz);
            } catch (final Exception ex) {
                logger.warn("Exception when de-serializing " + clazz + " with " + jsonString, ex);
            }
        }

        return null;
    }

    private static class JsonObject {

    }
}

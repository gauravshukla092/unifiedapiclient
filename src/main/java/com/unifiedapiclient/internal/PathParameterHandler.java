package com.unifiedapiclient.internal;

import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles path parameter configuration for HTTP requests.
 */
final class PathParameterHandler {
    private static final int PAIR_SIZE = 2;

    private PathParameterHandler() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    static void configure(RequestSpecification request, String path, Object... pathParams) {
        if (pathParams.length == 0) {
            return;
        }

        // Single value mode: extract param name from path placeholder
        if (pathParams.length == 1) {
            String paramName = extractPathParamName(path);
            if (paramName != null) {
                request.pathParam(paramName, pathParams[0]);
                return;
            }
        }

        // Key-value pairs mode: validate and convert to map
        validateKeyValuePairs(pathParams);
        request.pathParams(convertToPathParamsMap(pathParams));
    }

    private static void validateKeyValuePairs(Object... pathParams) {
        if (pathParams.length % PAIR_SIZE != 0) {
            throw new IllegalArgumentException(
                    "pathParams must be key/value pairs or a single value for {param} syntax. " +
                    "Received %d parameter(s), which is not a valid pair count.".formatted(pathParams.length)
            );
        }
    }

    private static Map<String, Object> convertToPathParamsMap(Object... pathParams) {
        Map<String, Object> paramsMap = new HashMap<>();
        for (int i = 0; i < pathParams.length; i += PAIR_SIZE) {
            String key = String.valueOf(pathParams[i]);
            Object value = pathParams[i + 1];
            paramsMap.put(key, value);
        }
        return paramsMap;
    }

    private static String extractPathParamName(String path) {
        if (path == null) {
            return null;
        }

        int startIndex = path.indexOf('{');
        if (startIndex < 0) {
            return null;
        }

        int endIndex = path.indexOf('}', startIndex);
        if (endIndex <= startIndex) {
            return null;
        }

        return path.substring(startIndex + 1, endIndex);
    }
}


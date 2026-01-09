package com.unifiedapiclient.internal;

import com.unifiedapiclient.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.function.Function;

/**
 * Resolves configuration values with priority-based lookup.
 */
final class ConfigResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigResolver.class);
    private static final String ENV_PREFIX = "API_";
    private static final String CONFIG_PREFIX = "domain_apis.settings.";

    private ConfigResolver() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    static <T> T getApiSetting(
            ConfigSource configSource,
            String key,
            Function<String, T> parser,
            T defaultVal,
            String typeName
    ) {
        String envKey = ENV_PREFIX + key.toUpperCase(Locale.ROOT).replace(".", "_");
        String configKey = CONFIG_PREFIX + key;

        // Priority 1: Configuration source
        if (configSource != null) {
            String val = configSource.getOptional(configKey);
            if (StringUtils.hasText(val)) {
                T result = parseSettingResult(val, parser, "config", configKey, typeName);
                if (result != null) {
                    return result;
                }
            }
        }

        // Priority 2: Environment variable
        String val = System.getenv(envKey);
        if (StringUtils.hasText(val)) {
            T result = parseSettingResult(val, parser, "env var", envKey, typeName);
            if (result != null) {
                return result;
            }
        }

        // Priority 3: System property
        val = System.getProperty(envKey);
        if (StringUtils.hasText(val)) {
            T result = parseSettingResult(val, parser, "sys prop", envKey, typeName);
            if (result != null) {
                return result;
            }
        }

        // Priority 4: Default value
        return defaultVal;
    }

    static int getApiSettingInt(ConfigSource configSource, String key, int defaultValue) {
        return getApiSetting(configSource, key, Integer::parseInt, defaultValue, "integer");
    }

    static boolean getApiSettingBoolean(ConfigSource configSource, String key, boolean defaultValue) {
        return getApiSetting(configSource, key, Boolean::parseBoolean, defaultValue, "boolean");
    }

    static String getApiSettingString(ConfigSource configSource, String key, String defaultValue) {
        return getApiSetting(configSource, key, Function.identity(), defaultValue, "string");
    }

    static long getApiSettingLong(ConfigSource configSource, String key, long defaultValue) {
        return getApiSetting(configSource, key, Long::parseLong, defaultValue, "long");
    }

    private static <T> T parseSettingResult(
            String value,
            Function<String, T> parser,
            String source,
            String keyName,
            String typeName
    ) {
        try {
            T parsed = parser.apply(value);
            // API setting loaded from config source
            return parsed;
        } catch (Exception e) {
            LOGGER.warn("Invalid {} value for {} '{}': {}", typeName, source, keyName, value);
            return null;
        }
    }
}


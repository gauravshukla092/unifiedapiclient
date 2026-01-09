package com.unifiedapiclient;

import java.util.Map;
import java.util.Objects;

public interface ConfigSource {
    String getOptional(String key);

    record MapConfigSource(Map<String, String> values) implements ConfigSource {
        public MapConfigSource {
            values = Map.copyOf(Objects.requireNonNull(values, "values cannot be null"));
        }

        @Override
        public String getOptional(String key) {
            return values.get(key);
        }
    }
}


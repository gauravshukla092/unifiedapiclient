package com.unifiedapiclient.internal;

import com.unifiedapiclient.ConfigSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Multi-format configuration manager supporting .properties, YAML, and JSON files.
 */
public class ConfigManager implements ConfigSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);

    private static final String CONFIG_FILE_PROP = "unifiedapiclient.config.file";
    private static final String CONFIG_FILE_ENV = "UNIFIEDAPICLIENT_CONFIG_FILE";
    private static final String ENV_VAR_PLACEHOLDER_START = "${";
    private static final String ENV_VAR_PLACEHOLDER_END = "}";
    
    private static final String[] CONFIG_FILE_PATTERNS = {
        "config.properties", "application.properties",
        "config.yaml", "config.yml", "application.yaml", "application.yml",
        "config.json", "application.json"
    };

    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;
    private final Map<String, String> configCache = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;

    private ConfigManager() {
        this.jsonMapper = new ObjectMapper();
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }

    static ConfigManager getInstance() {
        ConfigManager instance = Holder.INSTANCE;
        instance.ensureInitialized();
        return instance;
    }

    private static final class Holder {
        private static final ConfigManager INSTANCE = new ConfigManager();
    }

    /**
     * @param key the configuration key
     * @return the configuration value, or null if not found
     */
    @Override
    public String getOptional(String key) {
        ensureInitialized();
        
        // Priority 1: System properties (highest)
        String value = System.getProperty(key);
        if (value != null && !value.isBlank()) {
            return value;
        }

        // Priority 2: Environment variables
        value = System.getenv(key);
        if (value != null && !value.isBlank()) {
            return value;
        }

        // Priority 3: Config files (properties, YAML, JSON)
        value = configCache.get(key);
        if (value != null) {
            return value;
        }

        // Priority 4: Hardcoded defaults (handled by caller)
        return null;
    }

    private synchronized void ensureInitialized() {
        if (!initialized) {
            loadConfigFiles();
            initialized = true;
        }
    }

    private void loadConfigFiles() {
        String explicitFile = getExplicitConfigFile();
        if (explicitFile != null) {
            loadConfigFile(explicitFile);
            return;
        }

        // Auto-detect config files (first found wins)
        for (String pattern : CONFIG_FILE_PATTERNS) {
            Path file = findConfigFile(pattern);
            if (file != null) {
                LOGGER.debug("Auto-detected config file: {}", file);
                loadConfigFile(file);
                return; // Use first found file
            }
        }

        // No config files found - will use system properties and environment variables only
    }

    private String getExplicitConfigFile() {
        String file = System.getProperty(CONFIG_FILE_PROP);
        if (StringUtils.hasText(file)) {
            return file;
        }

        file = System.getenv(CONFIG_FILE_ENV);
        if (StringUtils.hasText(file)) {
            return file;
        }

        return null;
    }

    private Path findConfigFile(String fileName) {
        // Check project root
        Path root = Paths.get(System.getProperty("user.dir"), fileName);
        if (Files.exists(root) && Files.isRegularFile(root)) {
            return root;
        }

        // Check classpath (resources)
        InputStream resource = ConfigManager.class.getClassLoader().getResourceAsStream(fileName);
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException ignored) {
            }
            // Return path for classpath resource (we'll handle it differently)
            return Paths.get(fileName); // Marker for classpath
        }

        return null;
    }

    private void loadConfigFile(String fileName) {
        Path filePath = Paths.get(fileName);
        loadConfigFile(filePath);
    }

    private void loadConfigFile(Path filePath) {
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            // Try classpath
            String fileName = filePath.getFileName().toString();
            loadFromClasspath(fileName);
            return;
        }

        String fileName = filePath.getFileName().toString().toLowerCase();
        
        if (fileName.endsWith(".properties")) {
            loadPropertiesFile(filePath);
        } else if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
            loadYamlFile(filePath);
        } else if (fileName.endsWith(".json")) {
            loadJsonFile(filePath);
        } else {
            LOGGER.warn("Unsupported config file format: {}", fileName);
        }
    }

    private void loadFromClasspath(String fileName) {
        try (InputStream is = ConfigManager.class.getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                LOGGER.debug("Config file not found in classpath: {}", fileName);
                return;
            }

            String lowerName = fileName.toLowerCase();
            if (lowerName.endsWith(".properties")) {
                loadPropertiesStream(is);
            } else if (lowerName.endsWith(".yaml") || lowerName.endsWith(".yml")) {
                loadYamlStream(is);
            } else if (lowerName.endsWith(".json")) {
                loadJsonStream(is);
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to load config from classpath: {}", fileName, e);
        }
    }

    private void loadPropertiesFile(Path filePath) {
        try (InputStream is = Files.newInputStream(filePath)) {
            loadPropertiesStream(is);
            LOGGER.debug("Loaded properties config from: {}", filePath);
        } catch (IOException e) {
            LOGGER.warn("Failed to load properties file: {}", filePath, e);
        }
    }

    private void loadPropertiesStream(InputStream is) throws IOException {
        Properties props = new Properties();
        props.load(is);
        for (String key : props.stringPropertyNames()) {
            String value = resolveEnvironmentVariables(props.getProperty(key));
            configCache.put(key, value);
        }
    }

    private void loadYamlFile(Path filePath) {
        try (InputStream is = Files.newInputStream(filePath)) {
            loadYamlStream(is);
            LOGGER.debug("Loaded YAML config from: {}", filePath);
        } catch (IOException e) {
            LOGGER.warn("Failed to load YAML file: {}", filePath, e);
        }
    }

    private void loadYamlStream(InputStream is) throws IOException {
        try {
            JsonNode root = yamlMapper.readTree(is);
            flattenJsonNode("", root, configCache);
        } catch (Exception e) {
            LOGGER.warn("Failed to parse YAML config", e);
        }
    }

    private void loadJsonFile(Path filePath) {
        try (InputStream is = Files.newInputStream(filePath)) {
            loadJsonStream(is);
            LOGGER.debug("Loaded JSON config from: {}", filePath);
        } catch (IOException e) {
            LOGGER.warn("Failed to load JSON file: {}", filePath, e);
        }
    }

    private void loadJsonStream(InputStream is) throws IOException {
        try {
            JsonNode root = jsonMapper.readTree(is);
            flattenJsonNode("", root, configCache);
        } catch (Exception e) {
            LOGGER.warn("Failed to parse JSON config", e);
        }
    }

    private void flattenJsonNode(String prefix, JsonNode node, Map<String, String> target) {
        if (node == null || node.isNull()) {
            return;
        }

        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                flattenJsonNode(key, entry.getValue(), target);
            });
        } else if (node.isArray()) {
            // Arrays: store as comma-separated values
            List<String> values = new ArrayList<>();
            for (JsonNode item : node) {
                if (item.isTextual()) {
                    values.add(item.asText());
                } else {
                    values.add(item.toString());
                }
            }
            if (!values.isEmpty()) {
                target.put(prefix, String.join(",", values));
            }
        } else {
            // Leaf value
            String value = node.isTextual() ? node.asText() : node.toString();
            value = resolveEnvironmentVariables(value);
            target.put(prefix, value);
        }
    }

    private String resolveEnvironmentVariables(String value) {
        if (value == null || !value.contains(ENV_VAR_PLACEHOLDER_START)) {
            return value;
        }

        StringBuilder result = new StringBuilder();
        int cursor = 0;
        int start;
        while ((start = value.indexOf(ENV_VAR_PLACEHOLDER_START, cursor)) >= 0) {
            int end = value.indexOf(ENV_VAR_PLACEHOLDER_END, start);
            if (end < 0) {
                break;
            }

            result.append(value, cursor, start);
            String key = value.substring(start + ENV_VAR_PLACEHOLDER_START.length(), end);
            
            // Try env var first, then system property
            String resolved = System.getenv(key);
            if (resolved == null) {
                resolved = System.getProperty(key);
            }

            if (resolved != null) {
                result.append(resolved);
            } else {
                LOGGER.debug("Variable '{}' not found, keeping placeholder", key);
                result.append(ENV_VAR_PLACEHOLDER_START).append(key).append(ENV_VAR_PLACEHOLDER_END);
            }
            cursor = end + 1;
        }
        result.append(value.substring(cursor));
        return result.toString();
    }

    // Legacy methods for backward compatibility
    
    /**
     * @param dottedKey the configuration key
     * @return the configuration value
     */
    public String getConfig(String dottedKey) {
        String value = getOptional(dottedKey);
        if (value == null) {
            throw new IllegalArgumentException("Key not found: %s".formatted(dottedKey));
        }
        return value;
    }

    /**
     * @param dottedKey the configuration key
     * @return the integer configuration value
     */
    public int getConfigInt(String dottedKey) {
        String value = getConfig(dottedKey);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer value for '%s': %s".formatted(dottedKey, value), e);
        }
    }

    /**
     * @param dottedKey the configuration key
     * @return the long configuration value
     */
    public long getConfigLong(String dottedKey) {
        String value = getConfig(dottedKey);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid long value for '%s': %s".formatted(dottedKey, value), e);
        }
    }

    /**
     * @param dottedKey the configuration key
     * @param defaultValue the default value
     * @return the boolean configuration value
     */
    public boolean getConfigBoolean(String dottedKey, boolean defaultValue) {
        String value = getOptional(dottedKey);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * @param dottedKey the configuration key
     * @return the configuration value, or null if not found
     */
    public String getConfigOptional(String dottedKey) {
        return getOptional(dottedKey);
    }

    /**
     * @return the maximum retry attempts
     */
    public int getRetryMaxAttempts() {
        String value = getOptional("retry.maxAttempts");
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid retry.maxAttempts value: {}", value);
            }
        }
        return 3; // Default
    }

    /**
     * @return the retry wait duration in seconds
     */
    public int getRetryWaitDurationSeconds() {
        String value = getOptional("retry.waitDurationSeconds");
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid retry.waitDurationSeconds value: {}", value);
            }
        }
        return 5; // Default
    }

    /**
     * @return true if configuration has been loaded
     */
    public boolean isConfigurationLoaded() {
        return initialized;
    }

    /**
     * Resets the configuration manager for testing purposes.
     */
    public static void resetForTests() {
        ConfigManager instance = Holder.INSTANCE;
        instance.configCache.clear();
        instance.initialized = false;
    }
}


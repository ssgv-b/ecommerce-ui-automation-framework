package framework.utils;

import java.io.InputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

public class ConfigReader {

    private static final String DEFAULT_CONFIG_FILE = "config.properties";
    private static final String TEST_ENV_PROPERTY = "test.env";
    private static final String TEST_ENV_VARIABLE = "TEST_ENV";
    private static final Properties properties = new Properties();
    private static final String CONFIG_FILE = resolveConfigFile();

    static {
        try (InputStream input = ConfigReader.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {

            if (input == null) {
                throw new RuntimeException(
                        "Could not find " + CONFIG_FILE + " in classpath."
                );
            }

            properties.load(input);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to load configuration file: " + CONFIG_FILE,
                    e
            );
        }
    }

    private static String resolveConfigFile() {
        String selectedEnv = firstNonBlank(
                System.getProperty(TEST_ENV_PROPERTY),
                System.getenv(TEST_ENV_VARIABLE)
        );
        if (selectedEnv == null) {
            return DEFAULT_CONFIG_FILE;
        }

        String envConfigFile = "config-" + selectedEnv.toLowerCase(Locale.ROOT) + ".properties";
        InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream(envConfigFile);
        if (input != null) {
            try {
                input.close();
            } catch (IOException ignored) {
                // no-op: check stream is only used to verify resource existence.
            }
            return envConfigFile;
        }
        return DEFAULT_CONFIG_FILE;
    }

    public static String getProperty(String key) {
        String value = firstNonBlank(
                System.getProperty(key),
                System.getenv(toEnvironmentKey(key)),
                properties.getProperty(key)
        );

        if (value == null || value.isBlank()) {
            throw new RuntimeException(
                    "Configuration key '" + key + "' is missing or empty in "
                            + CONFIG_FILE
            );
        }
        return value;
    }

    private static String toEnvironmentKey(String key) {
        return key.replaceAll("([a-z])([A-Z])", "$1_$2")
                .replace('.', '_')
                .toUpperCase(Locale.ROOT);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}

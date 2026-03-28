package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigReader loads settings from config.properties.
 *
 * Why a dedicated class?
 * Instead of hardcoding values like "http://localhost:8080" inside tests,
 * we read them from a single config file. If the URL changes, we update
 * one file — not every test.
 *
 * Usage:  ConfigReader.get("base.url")
 */
public class ConfigReader {

    private static final Properties properties = new Properties();

    // Static block runs once when the class is first loaded
    static {
        try (InputStream input = ConfigReader.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input == null) {
                throw new RuntimeException("config.properties not found in resources folder");
            }
            properties.load(input);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties: " + e.getMessage(), e);
        }
    }

    /** Returns the value for the given key from config.properties */
    public static String get(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Property '" + key + "' not found in config.properties");
        }
        return value.trim();
    }

    /** Returns the value as an integer */
    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }
}

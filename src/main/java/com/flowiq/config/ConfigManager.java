package com.flowiq.config;

import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;

@Slf4j
public final class ConfigManager {

    private static final String ENV_PROPERTY = "env";
    private static EnvironmentConfig config;

    private ConfigManager() {
    }

    public static EnvironmentConfig getConfig() {
        if (config == null) {
            synchronized (ConfigManager.class) {
                if (config == null) {
                    resolveEnv();
                    config = ConfigFactory.create(EnvironmentConfig.class);
                    log.info("Loaded configuration for environment: {}", config.env());
                }
            }
        }
        return config;
    }

    private static void resolveEnv() {
        String env = System.getProperty(ENV_PROPERTY);
        if (env == null || env.isBlank()) {
            env = System.getenv("ENV");
        }
        if (env == null || env.isBlank()) {
            env = "local";
        }
        System.setProperty(ENV_PROPERTY, env);
    }

    public static void reset() {
        config = null;
    }
}

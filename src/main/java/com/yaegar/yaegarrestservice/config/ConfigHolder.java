package com.yaegar.yaegarrestservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author Lateef Adeniji-Adele
 */
@Component
public class ConfigHolder implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigHolder.class);

    @Value("${app.max.login.attempts}")
    private Short maxLoginAttempts;

    public Short getMaxLoginAttempts() {
        return maxLoginAttempts;
    }

    @Autowired
    public ConfigHolder() {
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
    }
}

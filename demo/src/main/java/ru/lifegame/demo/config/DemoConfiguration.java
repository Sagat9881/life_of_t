package ru.lifegame.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC configuration for the demo module.
 *
 * Generated assets are now served by GeneratedAssetController (REST),
 * not by static resource handlers. This avoids NoResourceFoundException (500)
 * for missing files — the controller returns proper 404 responses.
 */
@Configuration
public class DemoConfiguration implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(DemoConfiguration.class);

    public DemoConfiguration() {
        log.info("DemoConfiguration loaded. Assets served via GeneratedAssetController.");
    }
}

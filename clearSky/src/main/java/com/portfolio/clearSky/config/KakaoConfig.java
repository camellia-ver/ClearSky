package com.portfolio.clearSky.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application-API-Keys.properties")
public class KakaoConfig {
}

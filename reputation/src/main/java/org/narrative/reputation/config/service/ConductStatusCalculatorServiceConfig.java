package org.narrative.reputation.config.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ConductStatusCalculatorServiceConfig {
    @Bean
    public Clock utcClock() { return Clock.systemUTC();}
}

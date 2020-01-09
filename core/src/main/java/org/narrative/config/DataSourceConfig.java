package org.narrative.config;

import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor;
import org.narrative.network.customizations.narrative.service.impl.common.AreaTaskExecutorImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class DataSourceConfig {
    @Bean
    @DependsOn("staticApplicationInitializer")
    public AreaTaskExecutor areaTaskExecutor() {
        return new AreaTaskExecutorImpl();
    }
}

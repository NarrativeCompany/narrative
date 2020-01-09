package org.narrative.reputation.config.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.narrative.shared.spring.metrics.TimedServiceAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
public class MetricsConfig {
    @Configuration
    @EnableAspectJAutoProxy
    public class AutoTimingConfiguration {
        @Bean
        public TimedServiceAspect timedAspect(MeterRegistry registry) {
            return new TimedServiceAspect(registry);
        }
    }
}

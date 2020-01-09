package org.narrative.network.customizations.narrative.actuator;

import org.narrative.network.core.system.HeartbeatServer;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Date: 10/18/18
 * Time: 7:38 PM
 *
 * @author brian
 */
@Component
public class HeartbeatCheck implements HealthIndicator {
    @Override
    public Health health() {
        Health.Builder builder;
        if (HeartbeatServer.INSTANCE.isRunning()) {
            builder = Health.up();
        } else {
            builder = Health.down();
        }

        return builder.build();
    }
}

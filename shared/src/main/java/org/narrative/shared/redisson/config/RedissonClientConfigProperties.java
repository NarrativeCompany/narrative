package org.narrative.shared.redisson.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Duration;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class RedissonClientConfigProperties {
    /**
     * The scheme to use for the Redis connection
     */
    @NonNull
    private String scheme;
    /**
     * Redis host
     */
    @NonNull
    private String host;
    /**
     * Redis port
     */
    @NonNull
    private String port;
    /**
     * Redisson client threads
     */
    @NonNull
    private Integer threads;
    /**
     * Redisson Netty threads
     */
    @NonNull
    private Integer nettyThreads;
    /**
     * Redisson client request timeout
     */
    @NonNull
    private Integer timeout;

    /**
     * Redisson connect timeout
     */
    @NonNull
    private Integer connectTimeout;

    /**
     * Redisson allowed subscriptions per connection
     */
    @NonNull
    private Integer subscriptionsPerConnection;

    /**
     * Redisson subscriptions connections allowed
     */
    @NonNull
    private Integer subscriptionConnectionPoolSize;

    /**
     * Single server mode
     */
    @NonNull
    private Boolean singleServer = true;
    /**
     * Name of the codec to use for this client
     */
    @NotEmpty
    private String codecName = JsonJacksonCodec.class.getName();
    private Codec codec;
    /**
     * Set a default name for the event loop group.  Unless overridden per client, this configuration will
     * use a shared (@link NioEventLoopGroup} for event handling for all clients.
     */
    @NotEmpty
    private String eventLoopGroupName = "defaultEventLoopGroup";
    /**
     * Database number to use for this client
     */
    @NotNull
    @Min(0)
    @Max(15)
    private Integer databaseNumber = 0;
    /**
     * Specify Redisson server scan interval
     */
    @NotNull
    private Duration scanInterval = Duration.ofSeconds(15);

    private String proRegistrationKey;

    /**
     * Build the Redisson client configuration from the specified properties.
     */
    @SneakyThrows
    public Config buildConfig() {
        // bl: set the redisson.pro.key if it is set. since this class is in shared and Reputation uses the free
        // version of Redisson and core uses the pro version of Redisson, we can't depend on the pro version
        // in the shared package, which means that we can't use Config.setRegistrationKey(). instead, we'll just
        // dynamically inject the system property here, which should have the same net effect.
        if(!StringUtils.isEmpty(proRegistrationKey)) {
            System.setProperty("redisson.pro.key", proRegistrationKey);
        }

        //Codec set on config overrides default codec name
        if (codec == null) {
            codec = (Codec) Class.forName(codecName).newInstance();
        }

        Config config = new Config()
                .setCodec(codec)
                .setThreads(threads)
                .setNettyThreads(nettyThreads);

        if (singleServer) {
            SingleServerConfig serverConfig = config.useSingleServer()
                    .setAddress(scheme + "://" + host + ":" + port)
                    .setTimeout(timeout)
                    .setConnectTimeout(connectTimeout)
                    .setDatabase(databaseNumber)
                    //Enables TCP keepAlive for connections
                    .setKeepAlive(true);

            if (subscriptionsPerConnection!=null) {
                serverConfig.setSubscriptionsPerConnection(subscriptionsPerConnection);
            }

            if (subscriptionConnectionPoolSize!=null) {
                serverConfig.setSubscriptionConnectionPoolSize(subscriptionConnectionPoolSize);
            }

        } else {
            //TODO: We need to decide what HA config we want to use for Redis and support it through config
            throw new IllegalArgumentException("Only single server configuration is currently supported");
        }

        return config;
    }
}

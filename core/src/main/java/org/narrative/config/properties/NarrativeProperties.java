package org.narrative.config.properties;

import org.narrative.config.cache.spring.CacheManagerDefaultConfig;
import org.narrative.network.core.system.EnvironmentType;
import org.narrative.network.customizations.narrative.paypal.PayPalConfigurationMode;
import com.paypal.base.Constants;
import com.paypal.base.rest.APIContext;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "narrative")
@Validated
public class NarrativeProperties {
    @Valid
    private final Spring spring = new Spring();
    @Valid
    private final Security security = new Security();
    @Valid
    private final Storage storage = new Storage();
    @Valid
    private final Storage kycStorage = new Storage();
    @Valid
    private final Cluster cluster = new Cluster();
    @Valid
    private final KycQueue kycQueue = new KycQueue();
    @Valid
    private final Tomcat tomcat = new Tomcat();
    @Valid
    private final Secrets secrets = new Secrets();
    @Valid
    private final HibernateCacheConfig hibernateCacheConfig = new HibernateCacheConfig();
    @Valid
    private final Stripe stripe = new Stripe();
    @Valid
    private final PayPal payPal = new PayPal();

    /**
     * Spring related configuration.
     */
    @Data
    public static class Spring {
        @Valid
        private Mvc mvc = new Mvc();
        @Valid
        private CacheManager cacheManager = new CacheManager();

        /**
         * Spring MVC related configuration.
         */
        @Data
        public static class Mvc {
            /**
             * Base URI for Spring MVC
             */
            @NotEmpty
            private String baseUri;

            /**
             * Webhooks base URI for Spring MVC
             */
            @NotEmpty
            private String webhooksBaseUri = "/webhooks/";

            /**
             * Max page size for Pageable resolver
             */
            @Positive
            private int maxPageSize;

            /**
             * this pattern is prefixed with the baseUri, so it needs no leading slash :/
             */
            @NotEmpty
            private String corsBypassPattern = "(posts/[0-9]+/attachments|temp-files)";
        }

        /**
         * Spring cache manager configuration.
         */
        @Data
        public static class CacheManager {
            /**
             * Primary cache manager configuration URL.  This path must be for a yml Redisson config file.  Settings in this file will override any settings defined by {@link CacheManagerDefaultConfig}
             */
            private String configFilePath;
            /**
             * Primary *local cache* cache manager configuration path.  This path must be for a yml Redisson config file.  Settings in this file will override any settings defined by {@link CacheManagerDefaultConfig}
             */
            private String localCacheConfigFilePath;
        }
    }

    @Data
    public static class Security {
        /**
         * Login URI.
         */
        @NotEmpty
        private String loginURI = "/api/login";
        /**
         * JWT expiration.
         */
        @NotNull
        private Duration expiration = Duration.ofHours(4);
        /**
         * JWT two factor authentication expiration.
         */
        @NotNull
        private Duration twoFactorExpiration = Duration.ofDays(1);
        /**
         * JWT two factor authentication expiration.
         */
        @NotNull
        private Duration twoFactorRememberMeExpiration = Duration.ofDays(30);
        /**
         * JWT secret.
         */
        @NotEmpty
        private String secret;

        /**
         * This setter mutates another field (yuck).  This field value is essentially constant because it is
         * initialized by Spring so no need for synchronization.
         */
        public void setSecret(String secret) {
            this.secret = secret;
            jwtSecretBytes = secret.getBytes(StandardCharsets.UTF_8);
        }

        /**
         * Value managed via #setJwtSecret so the value is cached
         */
        @Setter(AccessLevel.NONE)
        public volatile byte[] jwtSecretBytes;
    }

    @Data
    public static class Storage {
        /**
         * the name of the Google Cloud Storage bucket
         */
        @NotEmpty
        private String credentialsPath;

        /**
         * the name of the Google Cloud Storage bucket
         */
        @NotEmpty
        private String bucket;

        /**
         * the path prefix for the current environment to use when storing blobs in the storage bucket
         */
        @NotEmpty
        private String blobPathPrefix;
    }

    @Data
    public static class Cluster {
        /**
         * the port the cluster control panel runs on
         */
        @NotNull
        private Integer port = 8082;

        @NotNull
        private EnvironmentType environmentType = EnvironmentType.UNINITIALIZED;

        @NotNull
        private String platformDomain;

        public String getPlatformUrl() {
            return (environmentType.isUsesSsl() ? "https://" : "http://") + platformDomain;
        }

        @NotEmpty
        private String devOpsEmailAddress;

        private String servletName;

        /**
         * Zone Id for date/time display in KYC management in cluster control panel.
         */
        @NotNull
        private ZoneId kycManagementDisplayZoneId = ZoneId.of("US/Eastern");
    }

    @Data
    public static class KycQueue {
        /**
         * the port the KYC queue runs on
         */
        @NotNull
        private Integer port = 8083;

        private Map<String,String> users;
    }

    @Data
    public static class Tomcat {
        /**
         * the port the cluster control panel runs on
         */
        @NotEmpty
        private String webXml = "/WEB-INF/web.xml";
    }

    @Data
    public static class Secrets {
        /**
         * the CoinMarketCap API key
         */
        @NotEmpty
        private String coinMarketCapApiKey;
    }

    @Data
    public static class HibernateCacheConfig {
        /**
         * Redisson Hibernate configuration file that will override defaults - override if you need to use a
         * configuration that is not deployed with the application in the case of an emergency.
         */
        private String redissonHibernateCacheConfigResourcePath = "classpath:cacheManager-redissonhibernate-config.yml";
    }

    // jw: this is only used for webhook processing for chargebacks of previous payments at this point.
    @Data
    public static class Stripe {
        @Valid
        private final ApiConfig nichePayments = new ApiConfig();

        @Data
        public static class ApiConfig {
            @NotEmpty
            private String webhookSigningSecret;
        }
    }

    @Data
    public static class PayPal {
        @Valid
        private final AuctionConfig auctions = new AuctionConfig();

        @Valid
        private final ApiConfig channelPayments = new ApiConfig();

        @Valid
        private final KycApiConfig kycPayments = new KycApiConfig();

        @Data
        public static class ApiConfig {
            @NotNull
            private PayPalConfigurationMode mode;
            @NotEmpty
            private String clientId;
            @NotEmpty
            private String clientSecret;

            @NotEmpty
            private String webhookId;

            public APIContext getApiContext() {
                APIContext apiContext = new APIContext(getClientId(), getClientSecret(), getMode().getApiContextMode());
                // Set the webhookId that you received when you created this webhook.
                apiContext.addConfiguration(Constants.PAYPAL_WEBHOOK_ID, getWebhookId());

                return apiContext;
            }
        }

        @Data
        public static class KycApiConfig extends ApiConfig {
            @NotNull
            private BigDecimal initialPrice = BigDecimal.valueOf(15);
            @NotNull
            private BigDecimal retryPrice = BigDecimal.valueOf(5);

            // jw: both of these prices are temporary, and should only be specified if needed.
            private BigDecimal kycPromoPrice;
            private String kycPromoMessage;
        }

        @Data
        public static class AuctionConfig {
            @NotNull
            private BigDecimal securityDepositPrice = BigDecimal.valueOf(25);
        }
    }
}


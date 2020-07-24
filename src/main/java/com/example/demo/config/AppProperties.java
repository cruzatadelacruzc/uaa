package com.example.demo.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.cors.CorsConfiguration;

/**
 * Properties specific to Indicators.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 */
@Getter
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class AppProperties {

    private final Cache cache = new Cache();
    private final Payment payment = new Payment();
    private final KeyStore keyStore = new KeyStore();
    private final Security security = new Security();
    private final Register register = new Register();
    private final ClientApp clientApp = new ClientApp();
    private final CorsConfiguration cors = new CorsConfiguration();
    private final WebClientConfiguration webClientConfiguration = new WebClientConfiguration();


    @Getter
    public static class Cache{
        private int timeToLiveSeconds = 3600;
        private int backupCount = 1;
        private final ManagementCenter managementCenter = new ManagementCenter();
        @Getter
        public static class ManagementCenter {
            private boolean enabled = false;
            private int updateInterval = 3;
            private String url ="";

            public ManagementCenter setEnabled(boolean enabled) {
                this.enabled = enabled;
                return this;
            }

            public ManagementCenter setUpdateInterval(int updateInterval) {
                this.updateInterval = updateInterval;
                return this;
            }

            public ManagementCenter setUrl(String url) {
                this.url = url;
                return this;
            }
        }
    }

    @Getter
    public static class Security {

        private final ClientAuthorization clientAuthorization = new ClientAuthorization();

        @Data
        public static class ClientAuthorization {

            private String accessTokenUri = "http://uaa/oauth/token";

            private String tokenServiceId = "uaa";

            private String clientId = "internal";

            private String clientSecret = "internal";

            private long tokenValidityInSeconds = 1800; // 30 minutes

            private long tokenValidityInSecondsForRememberMe = 2592000; // 30 days
        }
    }

    @Getter
    public static class Register{
        private String discoveryUrl = "http://admin:eureka@localhost:8761/eureka/";

        public Register setDiscoveryUrl(String discoveryUrl) {
            this.discoveryUrl = discoveryUrl;
            return this;
        }
    }

    @Getter
    public static class Payment {
        private final Paypal paypal = new Paypal();

        @Data
        public static class Paypal {
           private final Credential credential = new Credential();
           private final Account account = new Account();
           private String mode;
           private String urlCancel;
           private String urlSuccess;

           @Data
            public static class Credential {
               private String clientId;
               private String clientSecret;
           }

           @Data
           public static class Account {
               private String identifier;
               private String ownerFirstName;
               private String ownerLastName;
           }
        }
    }

    @Data
    public static class KeyStore {
        //name of the keystore in the classpath
        private String name = "config/tls/keystore.p12";
        //password used to access the key
        private String password = "password";
        //name of the alias to fetch
        private String alias = "selfsigned";
    }

    @Getter
    public static class ClientApp {
        private String name = "uaaApp";

        public ClientApp setName(String name) {
            this.name = name;
            return this;
        }
    }

    @Getter
    public static class WebClientConfiguration{
        //validity of the short-lived access token in secs (min: 60), don't make it too long
        private int accessTokenValidityInSeconds = 5 * 60;
        //validity of the refresh token in secs (defines the duration of "remember me")
        private int refreshTokenValidityInSecondsForRememberMe = 7 * 24 * 60 * 60;
        private String clientId = "web_app";
        private String secret = "changeit";


        public WebClientConfiguration setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public WebClientConfiguration setAccessTokenValidityInSeconds(int accessTokenValidityInSeconds) {
            this.accessTokenValidityInSeconds = accessTokenValidityInSeconds;
            return this;
        }

        public WebClientConfiguration setSecret(String secret) {
            this.secret = secret;
            return this;
        }

        public WebClientConfiguration setRefreshTokenValidityInSecondsForRememberMe(int refreshTokenValidityInSecondsForRememberMe) {
            this.refreshTokenValidityInSecondsForRememberMe = refreshTokenValidityInSecondsForRememberMe;
            return this;
        }
    }
}

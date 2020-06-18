package com.example.demo.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "application")
public class AppProperties {
    private final ClientApp clientApp = new ClientApp();
    private final Payment payment = new Payment();

    public static class ClientApp {
        private String name;

        public ClientApp() {
            this.name = "uaaApp";
        }

        public String getName() {
            return name;
        }

        public ClientApp setName(String name) {
            this.name = name;
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
}

package com.sw.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "application")
public class ApplicationPropertiesConfig {

    private boolean emailEnabled;
    private String emailFrom;
    private String emailTo;

    private Ldap ldap;

    @Data
    public static class Ldap {
        private String url;
        private String baseDN;
        private String bindDN;
        private String bindPassword;
        private String trustStore;
        private String trustStorePassword;
    }
}

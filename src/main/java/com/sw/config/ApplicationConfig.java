package com.sw.config;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.pool.factory.PoolingContextSource;
import org.springframework.ldap.pool.validation.DefaultDirContextValidator;

import javax.annotation.PostConstruct;

@Setter
@Configuration
public class ApplicationConfig {

    @Autowired
    private ApplicationPropertiesConfig configuration;

    @Bean
    public PoolingContextSource poolingLdapContextSource() {
        PoolingContextSource poolingContextSource = new PoolingContextSource();
        poolingContextSource.setDirContextValidator(new DefaultDirContextValidator());
        poolingContextSource.setContextSource(ldapContextSource());
        return poolingContextSource;
    }

    @Bean
    public LdapContextSource ldapContextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(configuration.getLdap().getUrl());
        contextSource.setBase(configuration.getLdap().getBaseDN());
        contextSource.setUserDn(configuration.getLdap().getBindDN());
        contextSource.setPassword(configuration.getLdap().getBindPassword());
        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        return new LdapTemplate(poolingLdapContextSource());
    }

    @PostConstruct
    public void postConstruct() {
        System.setProperty("javax.net.ssl.trustStore", configuration.getLdap().getTrustStore());
        System.setProperty("javax.net.ssl.trustStorePassword", configuration.getLdap().getTrustStorePassword());
    }
}

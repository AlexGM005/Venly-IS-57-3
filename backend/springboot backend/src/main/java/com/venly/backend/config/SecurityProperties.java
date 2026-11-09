package com.venly.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "venly.security")
public class SecurityProperties {

    private String organizationKeyHash;
    private String organizationKeyDefault;

    public String getOrganizationKeyDefault() {
        return organizationKeyDefault;
    }

    public void setOrganizationKeyDefault(String organizationKeyDefault) {
        this.organizationKeyDefault = organizationKeyDefault;
    }

    public String getOrganizationKeyHash() {
        return organizationKeyHash;
    }

    public void setOrganizationKeyHash(String organizationKeyHash) {
        this.organizationKeyHash = organizationKeyHash;
    }
}

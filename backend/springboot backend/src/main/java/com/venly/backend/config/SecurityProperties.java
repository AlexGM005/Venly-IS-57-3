package com.venly.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "venly.security")
public class SecurityProperties {

    private String organizationKeyHash;

    public String getOrganizationKeyHash() {
        return organizationKeyHash;
    }

    public void setOrganizationKeyHash(String organizationKeyHash) {
        this.organizationKeyHash = organizationKeyHash;
    }
}

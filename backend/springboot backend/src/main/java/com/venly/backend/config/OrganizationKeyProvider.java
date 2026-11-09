package com.venly.backend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Resolves the BCrypt hash of the organization key once at startup:
 * uses VENLY_ORG_KEY_HASH when provided, otherwise hashes the plain-text default (test key).
 */
@Component
public class OrganizationKeyProvider {

    private final SecurityProperties properties;
    private final PasswordEncoder passwordEncoder;
    private String organizationKeyHash;

    public OrganizationKeyProvider(SecurityProperties properties, PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    void init() {
        String configuredHash = properties.getOrganizationKeyHash();
        if (configuredHash != null && !configuredHash.isBlank()) {
            organizationKeyHash = configuredHash.trim();
            return;
        }
        String defaultKey = properties.getOrganizationKeyDefault();
        if (defaultKey != null && !defaultKey.isBlank()) {
            organizationKeyHash = passwordEncoder.encode(defaultKey);
        }
    }

    public String getOrganizationKeyHash() {
        return organizationKeyHash;
    }
}

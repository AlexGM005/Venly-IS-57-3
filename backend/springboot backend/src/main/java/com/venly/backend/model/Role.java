package com.venly.backend.model;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum Role {
    @JsonEnumDefaultValue
    CLIENTE,
    GERENTE,
    JEFE;

    /** GERENTE and JEFE are organization staff with management permissions. */
    public boolean isStaff() {
        return this == GERENTE || this == JEFE;
    }
}

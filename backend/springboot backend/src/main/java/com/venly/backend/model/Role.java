package com.venly.backend.model;

public enum Role {
    CLIENTE,
    GERENTE,
    JEFE;

    /** GERENTE and JEFE are organization staff with management permissions. */
    public boolean isStaff() {
        return this == GERENTE || this == JEFE;
    }
}

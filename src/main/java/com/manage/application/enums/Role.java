package com.manage.application.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {
    ROLE_SUPER_ADMIN("Super admin"),
    ROLE_ADMIN("Admin"),
    ROLE_MANAGER("Manager"),
    ROLE_USER("User"),
    ROLE_HR("HR");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Role fromValue(String value) {
        for (Role role : Role.values()) {
            if (role.name().equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown enum name : " + value);
    }
}

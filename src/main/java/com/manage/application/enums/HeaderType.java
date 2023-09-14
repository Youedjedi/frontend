package com.manage.application.enums;

public enum HeaderType {
    AUTHORIZATION("Authorization"),
    JWT_TOKEN("Jwt-Token");

    private final String value;

    HeaderType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

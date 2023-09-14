package com.manage.application.enums;

public enum NotificationType {
    DEFAULT("default"),
    SUCCESS("success"),
    ERROR("error"),
    INFO("info"),
    WARNING("warning");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

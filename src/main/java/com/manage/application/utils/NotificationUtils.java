package com.manage.application.utils;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

public class NotificationUtils {

    public static void showNotification(String message, NotificationType type) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_END);
        if (type == NotificationType.SUCCESS) {
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else {
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    public enum NotificationType {
        SUCCESS,
        ERROR
    }
}


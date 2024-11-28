package com.manage.application.views.user;

import com.manage.application.constants.MessageConstants;
import com.manage.application.service.UserService;
import com.manage.application.utils.NotificationUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;

import java.util.List;
import java.util.Map;

@PageTitle("Reset Password")
@Route(value = "reset-password")
public class ResetPasswordView extends VerticalLayout implements BeforeEnterObserver {

    private final UserService userService = new UserService();
    private String token = "";
    private final Binder<PasswordForm> binder = new Binder<>(PasswordForm.class);

    public ResetPasswordView() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        QueryParameters queryParameters = event.getLocation().getQueryParameters();
        Map<String, List<String>> parametersMap = queryParameters.getParameters();

        if (parametersMap.containsKey("token")) {
            List<String> tokens = parametersMap.get("token");

            if (tokens != null && !tokens.isEmpty()) {
                token = tokens.get(0);
                boolean isTokenValid = userService.verifyTokenResetPassword(token);
                initUI(isTokenValid);
            } else {
                initUI(false);
            }
        } else {
            initUI(false);
        }
    }

    private void initUI(boolean isTokenValid) {
        removeAll();
        H2 title = new H2("Reset Password");
        Paragraph paragraph;

        if (isTokenValid) {
            paragraph = new Paragraph("Please enter your new password:");

            PasswordField newPassword = new PasswordField();
            newPassword.setLabel("New Password");

            PasswordField confirmNewPassword = new PasswordField();
            confirmNewPassword.setLabel("Confirm New Password");

            Button resetPasswordButton = new Button("Reset Password");

            binder.forField(newPassword)
                    .withValidator(password -> password.length() >= 8, "Password must be at least 8 characters long")
                    .bind(PasswordForm::getPassword, PasswordForm::setPassword);

            binder.forField(confirmNewPassword)
                    .withValidator(confirmPassword -> confirmPassword.equals(newPassword.getValue()), "Passwords do not match")
                    .bind(PasswordForm::getConfirmPassword, PasswordForm::setConfirmPassword);

            binder.setBean(new PasswordForm());

            resetPasswordButton.addClickListener(e -> {
                if (binder.isValid()) {
                   boolean isResetSuccessful = userService.resetPassword(token, confirmNewPassword.getValue());
                    navigateToLogin();
                    String message = isResetSuccessful ? MessageConstants.RESET_PASSWORD_SUCCESSFULLY : MessageConstants.RESET_PASSWORD_FAIL;
                    NotificationUtils.NotificationType notificationType = isResetSuccessful ? NotificationUtils.NotificationType.SUCCESS : NotificationUtils.NotificationType.ERROR;
                    NotificationUtils.showNotification(message, notificationType);
                }
            });

            add(title, paragraph, newPassword, confirmNewPassword, resetPasswordButton);

        } else {
            paragraph = new Paragraph("The link for resetting your password is invalid or has expired.");
            add(title, paragraph);
        }
    }
    private void navigateToLogin() {
        UI.getCurrent().navigate("/login");
    }

    public static class PasswordForm {
        private String password;
        private String confirmPassword;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }
}

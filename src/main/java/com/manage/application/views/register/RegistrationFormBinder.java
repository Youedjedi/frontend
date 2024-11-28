package com.manage.application.views.register;

import com.manage.application.service.AuthenticationService;
import com.manage.application.domain.request.user.UserRequest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;

public class RegistrationFormBinder {
    private RegistrationForm registrationForm;
    private boolean enablePasswordValidation;

    private AuthenticationService authenticationService = new AuthenticationService();
    public RegistrationFormBinder(RegistrationForm registrationForm) {
        this.registrationForm = registrationForm;
    }

    public void addBindingAndValidation() {
        BeanValidationBinder<UserRequest> binder = new BeanValidationBinder<>(UserRequest.class);
        binder.bindInstanceFields(registrationForm);
        binder.forField(registrationForm.getPasswordField())
                .withValidator(this::passwordValidator).bind("password");
        registrationForm.getPasswordConfirmField().addValueChangeListener(e -> {
            enablePasswordValidation = true;

            binder.validate();
        });
        binder.setStatusLabel(registrationForm.getErrorMessageField());
        registrationForm.getSubmitButton().addClickListener(event -> {
            try {
                UserRequest userRequest = new UserRequest();
                binder.writeBean(userRequest);
               boolean isRegisterSuccess = this.authenticationService.register(userRequest);
               if(isRegisterSuccess) {
                   navigateToLogin();
                   showSuccess(userRequest);
               }else {
                   showFailure("ERROR");
               }
            } catch (ValidationException exception) {
            }
        });
        // Validation for username
        binder.forField(registrationForm.getUsername())
                .withValidator(this::usernameValidator)
                .bind("username");
    }

    private ValidationResult passwordValidator(String pass1, ValueContext ctx) {

        if (pass1 == null || pass1.length() < 8) {
            return ValidationResult.error("Password should be at least 8 characters long");
        }
        if (!enablePasswordValidation) {
            enablePasswordValidation = true;
            return ValidationResult.ok();
        }

        String pass2 = registrationForm.getPasswordConfirmField().getValue();

        if (pass1 != null && pass1.equals(pass2)) {
            return ValidationResult.ok();
        }

        return ValidationResult.error("Passwords do not match");
    }

    private ValidationResult usernameValidator(String username, ValueContext ctx) {
        if (username == null || username.trim().isEmpty()) {
            return ValidationResult.error("Username should not be empty");
        }

        if (username.length() < 4) {
            return ValidationResult.error("Username should be at least 4 characters long");
        }

        // Add more conditions if you need

        return ValidationResult.ok();
    }
    private void showSuccess(UserRequest userRequest) {
        String message = "Please confirm your email to activate your account, " + userRequest.getEmail() + ".";
        Notification notification = Notification.show(message);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
    private void showFailure(String reason) {
        String message = "Registration failed: " + reason;
        Notification notification = Notification.show(message);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
    private void navigateToLogin() {
        UI.getCurrent().navigate("/login");
    }

}

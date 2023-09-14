package com.manage.application.views.login;
import com.manage.application.data.model.Account;
import com.manage.application.data.service.AuthenticationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import org.springframework.http.ResponseEntity;

public class LoginFormBinder {
    private LoginForm loginForm;
    private AuthenticationService authenticationService = new AuthenticationService();

    public LoginFormBinder(LoginForm loginForm) {
        this.loginForm = loginForm;
    }

    public void addBindingAndValidation() {
        BeanValidationBinder<Account> binder = new BeanValidationBinder<>(Account.class);
        binder.bindInstanceFields(loginForm);
        binder.setStatusLabel(loginForm.getErrorMessageField());

        loginForm.getSubmitButton().addClickListener(event -> {
            try {
                Account user = new Account();
                binder.writeBean(user);
                ResponseEntity<Account> userRes = this.authenticationService.login(user);
                this.authenticationService.saveToken(userRes.getHeaders().getFirst("Jwt-Token"));
                this.authenticationService.addUserToLocalCache(userRes.getBody());
                if (this.authenticationService.isUserLoggedIn()) {
                    loginSuccess(user);
                    navigateToHomeOrUserPage();
                }
            } catch (ValidationException exception) {
                showErrorNotification("Login failed. Please check your credentials.");
            }
        });
    }

    private void loginSuccess(Account userAccount) {
        Notification notification = Notification.show("Welcome back, " + userAccount.getEmail(), 5000, Notification.Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void navigateToHomeOrUserPage() {
        UI.getCurrent().navigate("/");
    }

    private void showErrorNotification(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

}


package com.manage.application.views.register;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
public class RegistrationFormBinder {
    private RegistrationForm registrationForm;
    private boolean enablePasswordValidation;
    public RegistrationFormBinder(RegistrationForm registrationForm) {
        this.registrationForm = registrationForm;
    }

    public void addBindingAndValidation() {
        BeanValidationBinder<UserDetails> binder = new BeanValidationBinder<>(UserDetails.class);
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
                UserDetails userBean = new UserDetails();
                binder.writeBean(userBean);
                showSuccess(userBean);

            } catch (ValidationException exception) {
            }
        });
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
    private void showSuccess(UserDetails userBean) {
        Notification notification =
                Notification.show("Data saved, welcome " + userBean.getFirstName());
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}

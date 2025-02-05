package com.manage.application.views.register;

import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import java.util.stream.Stream;
public class RegistrationForm extends FormLayout {
    private H3 title;
    private TextField firstName;
    private TextField lastName;

    private TextField username;
    private EmailField email;
    private PasswordField password;
    private PasswordField passwordConfirm;
    private Checkbox allowMarketing;
    private Span errorMessageField;
    private Button submitButton;
    public RegistrationForm() {
        title = new H3("Signup form");
        firstName = new TextField("First name");
        lastName = new TextField("Last name");
        username = new TextField("Username");
        email = new EmailField("Email");
        allowMarketing = new Checkbox("Allow Marketing Emails?");
        allowMarketing.getStyle().set("margin-top", "10px");
        password = new PasswordField("Password");
        passwordConfirm = new PasswordField("Confirm password");
        setRequiredIndicatorVisible(firstName, lastName, username, email, password,
                passwordConfirm);
        errorMessageField = new Span();
        submitButton = new Button("Join the community");
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add(title, firstName, lastName, username, email, password,
                passwordConfirm, allowMarketing, errorMessageField,
                submitButton);
        setMaxWidth("500px");
        setResponsiveSteps(
                new ResponsiveStep("0", 1, ResponsiveStep.LabelsPosition.TOP),
                new ResponsiveStep("490px", 2, ResponsiveStep.LabelsPosition.TOP));
        setColspan(title, 2);
        setColspan(email, 2);
        setColspan(username, 2);
        setColspan(errorMessageField, 2);
        setColspan(submitButton, 2);
    }
    public PasswordField getPasswordField() { return password; }
    public PasswordField getPasswordConfirmField() { return passwordConfirm; }
    public TextField getUsername() {
        return username;
    }
    public void setUsername(TextField username) {
        this.username = username;
    }
    public Span getErrorMessageField() { return errorMessageField; }
    public Button getSubmitButton() { return submitButton; }
    private void setRequiredIndicatorVisible(HasValueAndElement<?, ?>... components) {
        Stream.of(components).forEach(comp -> comp.setRequiredIndicatorVisible(true));
    }
}

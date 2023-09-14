package com.manage.application.views.login;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField; // Importing TextField
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;

public class LoginForm extends FormLayout {
    private H3 title;
    private TextField username; // Changed EmailField to TextField
    private PasswordField password;
    private Span errorMessageField;
    private Button submitButton;

    public LoginForm() {
        title = new H3("Login form");
        username = new TextField("Username"); // Changed "Email" to "Username"
        password = new PasswordField("Password");
        username.setRequired(true); // Updated variable name
        password.setRequired(true);
        errorMessageField = new Span();
        submitButton = new Button("Login");
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add(title, username, password, errorMessageField, submitButton); // Updated variable name
        setMaxWidth("500px");
    }

    public Span getErrorMessageField() {
        return errorMessageField;
    }

    public Button getSubmitButton() {
        return submitButton;
    }
}

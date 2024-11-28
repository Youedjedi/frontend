package com.manage.application.views.login;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class LoginForm extends FormLayout {
    private H3 title;
    private TextField email;
    private PasswordField password;
    private Span errorMessageField;
    private Button submitButton;
    private Button registerButton; // New Register Button
    private Button forgotPasswordButton; // New Forgot Password Button

    public LoginForm() {
        title = new H3("Login form");
        email = new TextField("Email");
        password = new PasswordField("Password");
        email.setRequired(true);
        password.setRequired(true);
        errorMessageField = new Span();
        submitButton = new Button("Login");
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Initialize the new Register Button
        registerButton = new Button("Register");
        registerButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // Initialize the new Forgot Password Button
        forgotPasswordButton = new Button("Forgot Password?");
        forgotPasswordButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

        // Create a layout to put the login and register buttons next to each other
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.add(submitButton, registerButton, forgotPasswordButton); // Add both buttons to layout

        // Add click listener for the Register Button
        registerButton.addClickListener(e -> UI.getCurrent().navigate("register"));

        // Add click listener for the Forgot Password Button
        forgotPasswordButton.addClickListener(e -> UI.getCurrent().navigate("forgot-password"));
        // Add all the components to the FormLayout
        add(title, email, password, errorMessageField, buttonsLayout); // Updated to include new buttons
        setMaxWidth("500px");
    }

    public Span getErrorMessageField() {
        return errorMessageField;
    }

    public Button getSubmitButton() {
        return submitButton;
    }

    public Button getRegisterButton() {
        return registerButton; // New getter for Register Button
    }

    public Button getForgotPasswordButton() {
        return forgotPasswordButton; // New getter for Forgot Password Button
    }
}

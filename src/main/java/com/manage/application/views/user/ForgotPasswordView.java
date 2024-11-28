package com.manage.application.views.user;

import com.manage.application.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Forgot Password")
@Route("forgot-password")
public class ForgotPasswordView extends VerticalLayout {
    private H3 title;
    private EmailField emailField;
    private Span messageField;
    private Button sendResetLinkButton;

    private UserService userService = new UserService();

    public ForgotPasswordView() {
        // Set up layout and styling
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        // Create UI components
        title = new H3("Forgot Password");
        emailField = new EmailField("Email");
        emailField.setPlaceholder("Enter your email");
        emailField.setRequired(true);
        emailField.setWidthFull();

        messageField = new Span();

        sendResetLinkButton = new Button("Send Reset Link");
        sendResetLinkButton.addClickListener(e -> sendResetLink());

        // Form Layout
        FormLayout formLayout = new FormLayout();
        formLayout.add(title, emailField, messageField, sendResetLinkButton);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        formLayout.setWidth("300px");

        // Wrapper Div for centering
        Div wrapper = new Div(formLayout);
        wrapper.setWidthFull();
        wrapper.setMaxWidth("400px");
        wrapper.getElement().getStyle().set("margin", "auto");

        // Add to main layout
        add(wrapper);
    }

    private void sendResetLink() {
        String email = emailField.getValue();
        userService.forgotPassword(email);
        messageField.setText("A reset link has been sent to " + email);
    }
}
package com.manage.application.views.user;

import com.manage.application.service.UserService;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import java.util.List;
import java.util.Map;

@PageTitle("Email Verification")
@Route(value = "verify")
public class EmailVerificationView extends VerticalLayout implements BeforeEnterObserver {

    private final UserService userService = new UserService();

    public EmailVerificationView() {
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
                String token = tokens.get(0);
                boolean isTokenValid = userService.verifyToken(token);
                initUI(isTokenValid);
            } else {
                initUI(false);
            }
        } else {
            initUI(false);
        }
    }

    private void initUI(boolean isTokenValid) {
        removeAll();  // Remove all components from the layout
        H2 title = new H2("Email Verification");
        Paragraph paragraph;
        if (isTokenValid) {
            paragraph = new Paragraph("You have successfully verified your email. Please click here to log in.");
            Anchor loginLink = new Anchor("/login", "Log In");
            add(title, paragraph, loginLink);
        } else {
            paragraph = new Paragraph("Invalid or expired token.");
            add(title, paragraph);
        }
    }
}


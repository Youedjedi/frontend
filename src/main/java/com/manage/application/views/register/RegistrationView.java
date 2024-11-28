package com.manage.application.views.register;

import com.manage.application.service.AuthenticationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
@Route("register")
@RouteAlias("/register")
public class RegistrationView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthenticationService authenticationService = new AuthenticationService();
    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (isUserLoggedIn()) {
            rerouteToHome(beforeEnterEvent);
        }else {
            RegistrationForm registrationForm = new RegistrationForm();
            setHorizontalComponentAlignment(Alignment.CENTER, registrationForm);
            add(registrationForm);
            RegistrationFormBinder registrationFormBinder = new RegistrationFormBinder(registrationForm);
            registrationFormBinder.addBindingAndValidation();
        }
    }
    private boolean isUserLoggedIn() {
        return authenticationService.isUserLoggedIn();
    }
    private void rerouteToHome(BeforeEnterEvent event) {
        UI.getCurrent().getPage().setLocation("/");
    }
}

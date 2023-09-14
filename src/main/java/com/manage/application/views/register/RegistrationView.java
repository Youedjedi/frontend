package com.manage.application.views.register;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
@Route("register")
@RouteAlias("/register")
public class RegistrationView extends VerticalLayout {
    public RegistrationView() {
        RegistrationForm registrationForm = new RegistrationForm();
        setHorizontalComponentAlignment(Alignment.CENTER, registrationForm);
        add(registrationForm);
        RegistrationFormBinder registrationFormBinder = new RegistrationFormBinder(registrationForm);
        registrationFormBinder.addBindingAndValidation();
    }
}

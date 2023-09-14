package com.manage.application.views.login;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("login")
public class LoginView extends VerticalLayout {
	private LoginForm loginForm;
	private LoginFormBinder loginFormBinder;

	public LoginView() {
		loginForm = new LoginForm();
		setHorizontalComponentAlignment(Alignment.CENTER, loginForm);
		add(loginForm);

		loginFormBinder = new LoginFormBinder(loginForm);
		loginFormBinder.addBindingAndValidation();
	}
}

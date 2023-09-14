package com.manage.application.views;

import com.manage.application.data.model.Account;
import com.manage.application.data.service.AuthenticationService;
import com.manage.application.views.about.AboutView;
import com.manage.application.views.user.UsersView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIcon;


/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private final AuthenticationService authenticationService = new AuthenticationService();

    private H2 viewTitle;

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        // Create user dropdown menu instead of breadcrumbs
        Account account = authenticationService.getUserFromLocalCache();
        Span userNameSpan = new Span();
        if (account !=null) {
            userNameSpan = new Span(account.getUsername()); // Replace with the actual
        }
        Button userMenuButton = new Button("Menu");

        // Create a HorizontalLayout to place the username next to the button
        HorizontalLayout userLayout = new HorizontalLayout(userNameSpan, userMenuButton);
        userLayout.setAlignItems(FlexComponent.Alignment.CENTER); // Center align
        userLayout.setSpacing(true); // Add spacing between components

        // Create ContextMenu (Dropdown)
        ContextMenu contextMenu = new ContextMenu(userMenuButton);
        contextMenu.setOpenOnClick(true);  // Open menu on left click
        contextMenu.addItem("Profile", e -> {
            UI.getCurrent().navigate("profile");
        });
        contextMenu.addItem("Logout", e -> {
            authenticationService.logOut();
            UI.getCurrent().navigate("login");
        });

        // Layout to hold the viewTitle on the left and the user menu on the right
        HorizontalLayout headerLayout = new HorizontalLayout(viewTitle, userLayout);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerLayout.setWidthFull();

        // Adjust the margin to the right
        headerLayout.getStyle().set("margin-right", "20px");
        headerLayout.getStyle().set("align-items", "center");

        addToNavbar(true, toggle, headerLayout);
    }

    private void addDrawerContent() {
        H1 appName = new H1("User Manager");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        nav.addItem(new SideNavItem("Users", UsersView.class, LineAwesomeIcon.FILTER_SOLID.create()));
        nav.addItem(new SideNavItem("Posts", AboutView.class, LineAwesomeIcon.FILE.create()));

        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}

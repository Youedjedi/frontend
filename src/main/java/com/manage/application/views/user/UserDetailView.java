package com.manage.application.views.user;

import com.manage.application.domain.response.user.UserListResponse;
import com.manage.application.domain.response.user.UserSingleApiResponse;
import com.manage.application.domain.response.user.UserSingleResponse;
import com.manage.application.service.AuthenticationService;
import com.manage.application.service.UserService;
import com.manage.application.enums.Role;
import com.manage.application.utils.NotificationUtils;
import com.manage.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility.*;

import java.text.SimpleDateFormat;

@PageTitle("View User Detail")
@Route(value = "user/detail", layout = MainLayout.class)
public class UserDetailView extends Div implements HasUrlParameter<String> {
    private String username;
    private UserSingleResponse UserResponse;
    private final UserService userService = new UserService();
    private final AuthenticationService authenticationService = new AuthenticationService();

    private boolean isUserLoggedIn() {
        return authenticationService.isUserLoggedIn();
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        if (isUserLoggedIn()) {
            this.username = parameter;
            UserSingleApiResponse userSingleResponse =  userService.getUserByUsername(username);
            if (userSingleResponse.getHttpStatusCode() == 200 && userSingleResponse.getData() != null) {
                UserResponse = userSingleResponse.getData(); // Fetch user details from API
                initUI();
            }else {
                String message = userSingleResponse.getMessage();
                NotificationUtils.NotificationType notificationType = NotificationUtils.NotificationType.ERROR;
                NotificationUtils.showNotification(message, notificationType);
            }
        }
    }

    private void initUI() {
        addClassNames(Display.FLEX, FlexDirection.COLUMN, Height.FULL);

        Main content = new Main();
        content.addClassNames(Display.GRID, Gap.XLARGE, AlignItems.START, JustifyContent.CENTER, MaxWidth.SCREEN_MEDIUM,
                Margin.Horizontal.AUTO, Padding.Bottom.LARGE, Padding.Horizontal.LARGE);

        content.add(createUserInfoForm());
        add(content);
    }

    private Component createUserInfoForm() {
        Section userInfoForm = new Section();
        userInfoForm.addClassNames(Display.FLEX, FlexDirection.ROW, Flex.GROW, "mobile-column");
        userInfoForm.getStyle().set("margin-top", "20px");

        // Avatar image
        Image avatar = new Image(UserResponse.getProfileImageUrl(), "User Avatar");
        avatar.addClassNames(BorderRadius.MEDIUM, Margin.Right.LARGE, "mobile-avatar-margin");
        avatar.setWidth("200px");
        avatar.setHeight("200px");

        // Thông tin người dùng
        Aside userDetails = createAside();

        userInfoForm.add(avatar, userDetails);

        return userInfoForm;
    }

    private Aside createAside() {
        Aside aside = new Aside();
        aside.addClassNames(Background.CONTRAST_5, BoxSizing.BORDER, Padding.LARGE, BorderRadius.LARGE,
                Position.STICKY);
        aside.setWidth("500px");

        Header headerSection = new Header();
        headerSection.addClassNames(Display.FLEX, AlignItems.CENTER, JustifyContent.BETWEEN, Margin.Bottom.MEDIUM);

        H3 header = new H3("User Information");
        header.addClassNames(Margin.NONE);
        headerSection.add(header);
        UnorderedList ul = new UnorderedList();
        ul.addClassNames(ListStyleType.NONE, Margin.NONE, Padding.NONE, Display.FLEX, FlexDirection.COLUMN, Gap.MEDIUM);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // Adjust the format as needed

        String formattedDate = UserResponse.getJoinDate() != null ? sdf.format(UserResponse.getJoinDate()): "";
        String isActiveString = UserResponse.isActive() ? "Yes" : "No";
        Role role = getRoleEnumName(UserResponse.getRole());  // Get the role String from the enum

        ul.add(createListItem("First Name:", UserResponse.getFirstName()));
        ul.add(createListItem("Last Name:", UserResponse.getLastName()));
        ul.add(createListItem("Email Address:", UserResponse.getEmail()));
        ul.add(createListItem("Date join:", formattedDate));
        ul.add(createListItem("Role:", role.getValue()));  // Use the roleString here
        ul.add(createListItem("is active:", isActiveString));

        aside.add(headerSection, ul);

        return aside;
    }

    private Role getRoleEnumName(String role) {
        return Role.valueOf(role.toUpperCase());
    }

    private ListItem createListItem(String label, String value) {
        ListItem item = new ListItem();
        item.addClassNames(Display.FLEX, JustifyContent.BETWEEN);

        Div subSection = new Div();
        subSection.addClassNames(Display.FLEX, FlexDirection.COLUMN);

        subSection.add(new Span(label));
        Span valueSpan = new Span(value);
        valueSpan.addClassNames(FontSize.SMALL, TextColor.SECONDARY);
        subSection.add(valueSpan);

        item.add(subSection);
        return item;
    }


}

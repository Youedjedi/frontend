package com.manage.application.views.user;

import com.manage.application.constants.MessageConstants;
import com.manage.application.data.model.PagedResponse;
import com.manage.application.data.model.User;
import com.manage.application.data.service.AuthenticationService;
import com.manage.application.data.service.UserService;
import com.manage.application.utils.NotificationUtils;
import com.manage.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.*;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.manage.application.constants.Constants.*;

@PageTitle("Mange users")
@Route(value = "user", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@Uses(Icon.class)
public class UsersView extends Div implements SelectionListener<Grid<User>, User>, BeforeEnterObserver {
    private Grid<User> grid;
    private Button prevButton;
    private Button nextButton;
    private Span pageLabel;
    private HorizontalLayout pageButtons;
    private Filters filters;
    private UserService userService;
    private final int page_size = 3;
    private final int pageRange = 3;
    private int totalElements = 3;
    private int currentPage = 0;
    private String sortedColumnName = "joinDate";
    private String sortDirectionShort = "DESC";
    private boolean isEditOrDeleteClicked = false;
    private AuthenticationService authenticationService = new AuthenticationService();
    private final TextField name = new TextField("Name");
    private final TextField phone = new TextField("Phone");
    private final DatePicker startDate = new DatePicker("Join date");
    private final DatePicker endDate = new DatePicker();
    private final MultiSelectComboBox<String> occupations = new MultiSelectComboBox<>("Occupation");
    private final CheckboxGroup<String> roles = new CheckboxGroup<>("Role");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private List<User> selectedUsers = new ArrayList<>();

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (!isUserLoggedIn()) {
            rerouteToLogin(beforeEnterEvent);
        } else {
            initLayout();
        }
    }
    private boolean isUserLoggedIn() {
        return authenticationService.isUserLoggedIn();
    }

    private void rerouteToLogin(BeforeEnterEvent event) {
        event.rerouteTo("login");
    }
    private void initLayout() {

        // Action buttons
        Button selectAllBtn = new Button("Delete Checked", new Icon(VaadinIcon.TRASH)); // Icon for the "Select All" button
        selectAllBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        selectAllBtn.addClickListener(e -> deleteSelectedUsers());

        Button resetBtn = new Button("Reset");
        resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetBtn.addClickListener(e -> {
            name.clear();
            phone.clear();
            startDate.clear();
            endDate.clear();
            occupations.clear();
            roles.clear();
        });

        Button searchBtn = new Button("Search");
        searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchBtn.addClickListener(e -> {
            currentPage  = 0;
            refreshGrid();
        });

        // Add "Add User" button
        Button addUserBtn = new Button("Add User", new Icon(VaadinIcon.PLUS));  // Icon for the "Add User" button
        addUserBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addUserBtn.addClickListener(e -> {
            AddUserDialogView addUserDialog = new AddUserDialogView(this);  // Create a new dialog
            addUserDialog.open();  // Open the dialog
        });

        // Create a new FlexLayout to house all the action buttons
        FlexLayout buttonsLayout = new FlexLayout();
        buttonsLayout.setWidthFull();  // Make sure the layout stretches to full width
        buttonsLayout.add(selectAllBtn, addUserBtn, resetBtn, searchBtn);
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END); // Align to the right
        buttonsLayout.getStyle().set("gap", "5px");

        setSizeFull();
        filters = new Filters(this::refreshGrid);
        VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, buttonsLayout, createGrid());
        addClassNames("hello-world-view");
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
    }

    public UsersView(UserService userService) {
        this.userService = userService;
    }

    private HorizontalLayout createMobileFilters() {
        // Mobile version
        HorizontalLayout mobileFilters = new HorizontalLayout();
        mobileFilters.setWidthFull();
        mobileFilters.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER,
                LumoUtility.AlignItems.CENTER);
        mobileFilters.addClassName("mobile-filters");

        Icon mobileIcon = new Icon("lumo", "plus");
        Span filtersHeading = new Span("Filters");
        mobileFilters.add(mobileIcon, filtersHeading);
        mobileFilters.setFlexGrow(1, filtersHeading);
        mobileFilters.addClickListener(e -> {
            if (filters.getClassNames().contains("visible")) {
                filters.removeClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:plus");
            } else {
                filters.addClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:minus");
            }
        });
        return mobileFilters;
    }
    @Override
    public void selectionChange(SelectionEvent<Grid<User>, User> selectionEvent) {
        selectedUsers = new ArrayList<>(selectionEvent.getAllSelectedItems());
    }
    private void deleteSelectedUsers() {
        if (selectedUsers.isEmpty()) {
            NotificationUtils.showNotification("No users selected", NotificationUtils.NotificationType.ERROR);
            return;
        }

        // Convert selected users to an array of usernames
        String[] usernames = selectedUsers.stream().map(User::getUsername).toArray(String[]::new);
        userService.deleteUsers(usernames);
        NotificationUtils.showNotification("Users deleted successfully", NotificationUtils.NotificationType.SUCCESS);
        selectedUsers.clear();
        this.currentPage = 0;
        this.refreshGrid();
    }

    public class Filters extends Div{
        public Filters(Runnable onSearch) {
            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);
            name.setPlaceholder("First or last name");
            occupations.setItems("Insurance Clerk", "Mortarman", "Beer Coil Cleaner", "Scale Attendant");
            roles.setItems("ROLE_SUPER_ADMIN", "Supervisor", "Manager", "External");
            roles.addClassName("double-width");

            // Add components to the main view
            add(name, phone, createDateRangeFilter(), occupations, roles);
        }

        private Component createDateRangeFilter() {
            startDate.setPlaceholder("From");
            endDate.setPlaceholder("To");
            startDate.setAriaLabel("From date");
            endDate.setAriaLabel("To date");
            FlexLayout dateRangeComponent = new FlexLayout(startDate, new Text(" – "), endDate);
            dateRangeComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
            dateRangeComponent.addClassName(LumoUtility.Gap.XSMALL);
            return dateRangeComponent;
        }
    }

    private Component createGrid() {
        initializeGrid();
        configureColumns();
        configureEventListeners();
        populateData();
        HorizontalLayout paginationBar = createPaginationBar();
        return new VerticalLayout(grid, paginationBar);
    }

    private void initializeGrid() {
        grid = new Grid<>(User.class, false);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.getStyle().setHeight("300px");
        grid.setWidth("100%");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
    }

    private void configureColumns() {
        grid.addComponentColumn(this::createImageColumn)
                .setAutoWidth(true)
                .setHeader("Image");

        grid.addColumn("username").setAutoWidth(true).setHeader("User Name");
        grid.addColumn("firstName").setAutoWidth(true).setHeader("First Name");
        grid.addColumn("lastName").setAutoWidth(true).setHeader("Last Name");
        grid.addColumn("email").setAutoWidth(true).setHeader("Email");
        grid.addColumn("role").setAutoWidth(true).setHeader("Role");

        grid.addComponentColumn(this::createActionsColumn)
                .setHeader("Actions")
                .setWidth(ACTIONS_COLUMN_WIDTH)
                .setTextAlign(ColumnTextAlign.CENTER);
    }

    private Component createImageColumn(User user) {
        String imageUrl = user.getProfileImageUrl();
        Image image = new Image(imageUrl, "alt text");
        image.getStyle().set("borderRadius", "50%");
        image.setWidth(IMAGE_WIDTH);
        image.setHeight(IMAGE_HEIGHT);

        return image;
    }

    private Component createActionsColumn(User user) {
        Button editButton = new Button(new Icon(VaadinIcon.EDIT));
        editButton.addClickListener(event -> handleEditClick(user));
        editButton.setId("edit-button");

        Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
        deleteButton.addClickListener(event -> handleDeleteClick(user));
        deleteButton.setId("delete-button");

        HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton);
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);
        actions.setWidthFull();

        return actions;
    }

    private void configureEventListeners() {
        grid.addItemClickListener(this::handleItemClick);
        grid.addSortListener(event -> {
            List<GridSortOrder<User>> sortOrderList = event.getSortOrder();
            handleGridSort(sortOrderList);
        });
        grid.addSelectionListener(this::selectionChange);
    }

    private void handleGridSort(List<GridSortOrder<User>> sortOrderList) {
        if (sortOrderList.isEmpty()) {
            sortedColumnName = DEFAULT_SORT_COLUMN;
            sortDirectionShort = DEFAULT_SORT_DIRECTION;
        } else {
            GridSortOrder<User> sortOrder = sortOrderList.get(0);
            Grid.Column<User> sortedColumn = sortOrder.getSorted();
            SortDirection direction = sortOrder.getDirection();
            String sortDirectionFull = direction.name();
            sortedColumnName = sortedColumn.getKey();
            sortDirectionShort = sortDirectionFull.replace("ENDING", "");
        }
        this.refreshGrid();
    }

    private void handleEditClick(User user) {
        isEditOrDeleteClicked = true;
        EditUserDialogView dialog = new EditUserDialogView(user, this);
        dialog.open();
    }

    private void handleDeleteClick(User user) {
        isEditOrDeleteClicked = true;

        // Create a confirmation dialog
        Dialog confirmDialog = new Dialog();
        confirmDialog.add(new Label(MessageConstants.CONFIRM_DELETE_MESSAGE));

        // Confirm button
        Button confirmButton = new Button("Confirm", event -> {
            // Call the deleteUser method from your service
            userService.deleteUser(user.getUsername());
            NotificationUtils.showNotification(MessageConstants.USER_DELETED_SUCCESSFULLY, NotificationUtils.NotificationType.SUCCESS);
            this.currentPage = 0;
            // Close the dialog
            confirmDialog.close();
            // Update UI or perform other actions after successful deletion
            this.refreshGrid();
        });

        // Cancel button
        Button cancelButton = new Button("Cancel", event -> {
            // Close the dialog without doing anything
            confirmDialog.close();
        });

        // Add buttons to dialog
        confirmDialog.add(new HorizontalLayout(confirmButton, cancelButton));

        // Show dialog
        confirmDialog.open();
    }



    private void handleItemClick(ItemClickEvent<User> event) {
        if (isEditOrDeleteClicked) {
            isEditOrDeleteClicked = false;
            return;
        }
        User selectedPerson = event.getItem();
        navigateToUserDetailView(selectedPerson);
    }

    private void populateData() {
        PagedResponse<User> listUsers = fetchUsers();
        totalElements = listUsers.getTotalElements();
        grid.setItems(listUsers.getContent());
    }

    private void navigateToUserDetailView(User person) {
        // Giả sử ID của SamplePerson được lưu trong thuộc tính 'id'
        getUI().ifPresent(ui -> ui.navigate("user/detail/" + person.getUsername()));
    }
    private HorizontalLayout createPaginationBar() {
        if (totalElements == 0) {
            return new HorizontalLayout();
        }
        prevButton = new Button("Previous", event -> navigatePage(currentPage - 1));
        prevButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        nextButton = new Button("Next", event -> navigatePage(currentPage + 1));
        nextButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        pageLabel = new Span("Page " + (currentPage + 1));
        pageLabel.getStyle().set("margin-left", "10px");
        pageLabel.getStyle().set("margin-right", "10px");

        pageButtons = buildPageButtons();

        HorizontalLayout paginationBar = new HorizontalLayout(prevButton, pageLabel, pageButtons, nextButton);
        paginationBar.setPadding(true);
        paginationBar.setSpacing(true);
        paginationBar.setAlignItems(FlexComponent.Alignment.CENTER);
        paginationBar.getStyle().set("margin", "20px auto");

        return paginationBar;
    }
    private HorizontalLayout buildPageButtons() {

        int totalPageCount = (int) Math.ceil(totalElements / (double) page_size);
        HorizontalLayout newPageButtons = new HorizontalLayout();

        int startPage;
        int endPage;

        if (totalPageCount <= this.pageRange) {
            startPage = 1;
            endPage = totalPageCount;
        } else {
            if (currentPage == 0) {
                startPage = 1;
                endPage = this.pageRange;
            } else if (currentPage == totalPageCount - 1) {
                startPage = totalPageCount - this.pageRange + 1;
                endPage = totalPageCount;
            } else {
                startPage = currentPage + 1 - (this.pageRange - 1) / 2;
                endPage = currentPage + 1 + (this.pageRange - 1) / 2;

                if (startPage < 1) {
                    startPage = 1;
                    endPage = this.pageRange;
                }
                if (endPage > totalPageCount) {
                    endPage = totalPageCount;
                    startPage = endPage - this.pageRange + 1;
                }
            }
        }

        for (int i = startPage; i <= endPage; i++) {
            int pageNumber = i;
            Button pageButton = new Button(String.valueOf(i), event -> {
                currentPage = pageNumber - 1;
                refreshGrid();
                updateActiveButton(newPageButtons);
                pageButtons.removeAll();
                pageButtons.add(buildPageButtons());
            });

            if (i == currentPage + 1) {
                pageButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            } else {
                pageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            }
            newPageButtons.add(pageButton);
        }

        return newPageButtons;
    }
    private void updateActiveButton(HorizontalLayout updatedPageButtons) {
        for (int i = 0; i < updatedPageButtons.getComponentCount(); i++) {
            Component component = updatedPageButtons.getComponentAt(i);

            if (component instanceof Button) {
                Button pageButton = (Button) component;

                if (Integer.parseInt(pageButton.getText()) == currentPage + 1) {
                    pageButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                } else {
                    pageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                }
            }
        }
    }

    private void navigatePage(int newPage) {
        currentPage = newPage;
        refreshGrid();
        updatePaginationControls();
        pageButtons.removeAll();
        pageButtons.add(buildPageButtons());
    }
    private void updatePaginationControls() {
        int totalPageCount = (int) Math.ceil(totalElements / (double) page_size);
        prevButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(currentPage < totalPageCount - 1);
        pageLabel.setText("Page " + (currentPage + 1));
    }
    public void refreshGrid() {
        PagedResponse<User> listUsers = fetchUsers();
        totalElements = listUsers.getTotalElements();
        grid.setItems(listUsers.getContent());
        grid.getDataProvider().refreshAll();
        updatePaginationControls();
        pageButtons.removeAll();
        pageButtons.add(buildPageButtons());
    }
    private PagedResponse<User> fetchUsers() {
        String endDate = getEndDateAsString();
        String startDate = getStartDateAsString();
        Set<String> role = roles.getSelectedItems();
        return userService.getUsers(currentPage, page_size, sortedColumnName, sortDirectionShort, startDate, endDate, name.getValue(), role);
    }
    public String getStartDateAsString() {
        LocalDate date = startDate.getValue();
        return (date != null) ? date.format(formatter) : null;
    }
    public String getEndDateAsString() {
        LocalDate date = endDate.getValue();
        return (date != null) ? date.format(formatter) : null;
    }

}

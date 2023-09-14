package com.manage.application.views.user;
import com.manage.application.constants.MessageConstants;
import com.manage.application.data.model.User;
import com.manage.application.data.service.UserService;
import com.manage.application.enums.Role;
import com.manage.application.utils.NotificationUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.server.StreamResource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditUserDialogView extends Dialog {
    private final TextField firstNameField;
    private final TextField lastNameField;
    private final TextField usernameField;
    private final EmailField emailField;
    private final ComboBox<Role> roleComboBox;
    private final Checkbox isActiveField;
    private final Checkbox isNonLockedField;
    private Button saveButton;
    private byte[] uploadedImageBytes = null;
    private String fileName = "";
    private String fileExtension = "";
    private Image profileImagePreview; // Thêm biến này
    private String currentUsername; // Thêm biến này
    private final Binder<User> binder = new Binder<>(User.class);
    private final UserService userService = new UserService();
    private UsersView  usersView;
    public EditUserDialogView(User user, UsersView  usersView) {
        this.usersView = usersView;
        currentUsername = user.getUsername();
        firstNameField = createTextField("First Name", "");
        lastNameField = createTextField("Last Name", "");
        usernameField = createTextField("Username", "");
        emailField = createEmailField("Email", "");
        roleComboBox = createRoleComboBox("Role", Role.USER);
        isActiveField = createCheckbox("Is Active", user.isActive());
        isNonLockedField = createCheckbox("Is Non Locked", user.isNotLocked());

        // 2. Bind fields
        binder.forField(firstNameField)
                .withValidator(new StringLengthValidator(
                        "First name must be between 3 and 50 characters", 3, 50))
                .bind(User::getFirstName, User::setFirstName);

        binder.forField(lastNameField)
                .withValidator(new StringLengthValidator(
                        "Last name must be between 3 and 50 characters", 3, 50))
                .bind(User::getLastName, User::setLastName);

        binder.forField(usernameField)
                .withValidator(new StringLengthValidator(
                        "Username must be between 3 and 50 characters", 3, 50))
                .bind(User::getUsername, User::setUsername);

        binder.forField(emailField)
                .withValidator(new EmailValidator("Invalid email address"))
                .bind(User::getEmail, User::setEmail);

        binder.forField(roleComboBox)
                .bind(User::getRole, User::setRole);

        profileImagePreview = new Image(); // Khởi tạo đối tượng Image
        profileImagePreview.setMaxHeight("100px"); // Cài đặt kích thước tối đa
        VerticalLayout layout = new VerticalLayout();
        layout.add(createFormRows());
        layout.add(createUploadField());
        layout.add(profileImagePreview); // Thêm thành phần hiển thị ảnh vào layout
        layout.add(createActionButtons());
        add(layout);
        binder.setBean(user);
        binder.addValueChangeListener(e -> validateForm());
        validateForm(); // Initial validation
    }
    private void validateForm() {
        boolean isValid = binder.isValid();
        saveButton.setEnabled(isValid);
    }
    private VerticalLayout createFormRows() {
        HorizontalLayout row1 = new HorizontalLayout(firstNameField, lastNameField);
        HorizontalLayout row2 = new HorizontalLayout(usernameField, emailField);

        // Tạo VerticalLayout để chứa isActiveField và isNonLockedField
        VerticalLayout checkboxColumn = new VerticalLayout(isActiveField, isNonLockedField);

        HorizontalLayout row3 = new HorizontalLayout(roleComboBox, checkboxColumn);

        VerticalLayout formRows = new VerticalLayout(row1, row2, row3);
        return formRows;
    }

    private Upload createUploadField() {
        MemoryBuffer buffer = new MemoryBuffer();
        Upload profileImageField = new Upload(buffer);
        profileImageField.addSucceededListener(event -> readImage(buffer, event.getFileName()));
        profileImageField.addFileRejectedListener(event -> clearPreview()); // Lắng nghe sự kiện tải lên thất bại
        profileImageField.getElement().addEventListener("file-remove", event -> {
            clearPreview();
        }).addEventData("event.detail.file");
        return profileImageField;
    }
    private void clearPreview() {
        profileImagePreview.setSrc(""); // Xóa hình ảnh xem trước
        uploadedImageBytes = null; // Xóa dữ liệu ảnh
        fileName = "";
        fileExtension = "";
    }

    private void readImage(MemoryBuffer buffer, String fileName) {
        try {
            InputStream inputStream = buffer.getInputStream();
            uploadedImageBytes = inputStream.readAllBytes();
            this.fileName = fileName;
            fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);

            // Cập nhật ảnh xem trước
            StreamResource resource = new StreamResource(fileName, () -> new ByteArrayInputStream(uploadedImageBytes));
            profileImagePreview.setSrc(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HorizontalLayout createActionButtons() {
        saveButton = new Button("Save", event -> handleSaveAction());
        Button closeButton = new Button("Close", event -> close());
        return new HorizontalLayout(saveButton, closeButton);
    }

    private void handleSaveAction() {
        boolean isSaveSuccessful = saveUser();
        String message = isSaveSuccessful ? MessageConstants.USER_UPDATED_SUCCESSFULLY : MessageConstants.USER_UPDATE_ERROR_FAIL;
        NotificationUtils.NotificationType notificationType = isSaveSuccessful ? NotificationUtils.NotificationType.SUCCESS : NotificationUtils.NotificationType.ERROR;
        NotificationUtils.showNotification(message, notificationType);
        this.usersView.refreshGrid();
        close();
    }

    private boolean saveUser() {
        User user = new User();
        user.setFirstName(firstNameField.getValue());
        user.setLastName(lastNameField.getValue());
        user.setUsername(usernameField.getValue());
        user.setEmail(emailField.getValue());
        user.setRole(roleComboBox.getValue());
        user.setActive(isActiveField.getValue());
        user.setNotLocked(isNonLockedField.getValue());

        try {
            ByteArrayInputStream byteArrayInputStream = null;

            if (uploadedImageBytes != null) {
                byteArrayInputStream = new ByteArrayInputStream(uploadedImageBytes);
            }

            return userService.updateUser(currentUsername, user, byteArrayInputStream, fileName, fileExtension);
        } catch (IOException e) {
            NotificationUtils.showNotification("Có lỗi xảy ra khi lưu người dùng", NotificationUtils.NotificationType.ERROR);
            return false;
        }
    }


    private ComboBox<Role> createRoleComboBox(String label, Role initialValue) {
        ComboBox<Role> comboBox = new ComboBox<>(label);
        comboBox.setItems(Role.values()); // Đặt danh sách các vai trò vào ComboBox
        comboBox.setValue(initialValue); // Đặt giá trị ban đầu nếu hợp lệ
        comboBox.setWidth("100%");
        return comboBox;
    }


    private TextField createTextField(String label, String initialValue) {
        TextField textField = new TextField(label, initialValue);
        textField.setWidth("100%");
        return textField;
    }

    private EmailField createEmailField(String label, String initialValue) {
        EmailField emailField = new EmailField(label, initialValue);
        emailField.setWidth("100%");
        return emailField;
    }

    private Checkbox createCheckbox(String label, boolean initialValue) {
        Checkbox checkbox = new Checkbox(label, initialValue);
        return checkbox;
    }
}

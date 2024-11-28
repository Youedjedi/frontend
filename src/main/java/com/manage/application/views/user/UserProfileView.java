package com.manage.application.views.user;

import com.manage.application.constants.MessageConstants;
import com.manage.application.domain.request.user.UserRequest;
import com.manage.application.domain.response.user.UserListResponse;
import com.manage.application.domain.response.user.UserSingleApiResponse;
import com.manage.application.domain.response.user.UserSingleResponse;
import com.manage.application.service.AuthenticationService;
import com.manage.application.service.EmailVerificationService;
import com.manage.application.service.UserService;
import com.manage.application.enums.Role;
import com.manage.application.utils.NotificationUtils;
import com.manage.application.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.modelmapper.ModelMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

@PageTitle("Profile")
@Route(value = "profile", layout = MainLayout.class)
public class UserProfileView extends VerticalLayout {
    private final TextField firstNameField;
    private final TextField lastNameField;
    private final TextField usernameField;
    private final EmailField emailField;
    private final PasswordField oldPasswordField;
    private final PasswordField newPasswordField;
    private final PasswordField confirmPasswordField;
    private Image userAvatar;
    private Button saveButton;
    private Button updateEmailButton; // Nút cập nhật email
    private Dialog otpDialog;         // Dialog yêu cầu nhập OTP
    private TextField otpField;       // Trường nhập OTP
    private byte[] uploadedImageBytes = null;
    private String fileName = "";
    private String fileExtension = "";
    private Image profileImagePreview; // Thêm biến này
    private String currentUsername; // Thêm biến này
    private UserSingleResponse currentUser;
    private UserSingleResponse account;
    private final Binder<UserRequest> binder = new Binder<>(UserRequest.class);

    private ModelMapper modelMapper = new ModelMapper();
    private final UserService userService = new UserService();
    private final EmailVerificationService emailVerificationService = new EmailVerificationService();
    private final AuthenticationService authenticationService = new AuthenticationService();

    public UserProfileView() {
        updateEmailButton = new Button("Update Email", e -> showOtpDialog());
        userAvatar = new Image();
        userAvatar.setMaxHeight("100px");
        account = authenticationService.getUserFromLocalCache();
        currentUsername = account.getUsername();
        if (account != null) {
            UserSingleApiResponse userSingleResponse = userService.getUserByUsername(currentUsername);
            if (userSingleResponse.getHttpStatusCode() == 200 && userSingleResponse.getData() != null) {
                currentUser = userSingleResponse.getData(); // Fetch user details from API
                userAvatar.setSrc(currentUser.getProfileImageUrl()); // Đặt đường dẫn mặc định hoặc URL
            }else {
                String message = userSingleResponse.getMessage();
                NotificationUtils.NotificationType notificationType = NotificationUtils.NotificationType.ERROR;
                NotificationUtils.showNotification(message, notificationType);
            }

        }

        firstNameField = createTextField("First Name", "");
        lastNameField = createTextField("Last Name", "");
        usernameField = createTextField("Username", "");
        emailField = createEmailField("Email", "");
        // 2. Bind fields
        binder.forField(firstNameField)
                .withValidator(new StringLengthValidator(
                        "First name must be between 3 and 50 characters", 3, 50))
                .bind(UserRequest::getFirstName, UserRequest::setFirstName);

        binder.forField(lastNameField)
                .withValidator(new StringLengthValidator(
                        "Last name must be between 3 and 50 characters", 3, 50))
                .bind(UserRequest::getLastName, UserRequest::setLastName);

        binder.forField(usernameField)
                .withValidator(new StringLengthValidator(
                        "Username must be between 3 and 50 characters", 3, 50))
                .bind(UserRequest::getUsername, UserRequest::setUsername);

        binder.forField(emailField)
                .withValidator(new EmailValidator("Invalid email address"))
                .bind(UserRequest::getEmail, UserRequest::setEmail);

        oldPasswordField = createPasswordField("Old Password", "");
        newPasswordField = createPasswordField("New Password", "");
        confirmPasswordField = createPasswordField("Confirm Password", "");

        oldPasswordField.addValueChangeListener(e -> validateForm());
        newPasswordField.addValueChangeListener(e -> validateForm());
        confirmPasswordField.addValueChangeListener(e -> validateForm());

        // Khởi tạo dialog và trường OTP
        otpDialog = new Dialog();
        otpField = new TextField("Enter OTP");
        Button confirmOtpButton = new Button("Confirm", e -> verifyOtp());
        otpDialog.add(otpField, confirmOtpButton);
        profileImagePreview = new Image(); // Khởi tạo đối tượng Image
        HorizontalLayout firstNameRow = new HorizontalLayout();
        firstNameRow.add(userAvatar, firstNameField);
        firstNameRow.setAlignItems(FlexComponent.Alignment.START); // Căn chỉnh các items ở đỉnh theo trục chéo
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setMaxWidth("900px"); // Đặt chiều rộng tối đa
        formLayout.add(firstNameRow);  // Thêm dòng First Name với ảnh đại diện vào form
        formLayout.add(createFormRows());  // Các dòng khác
        formLayout.add(createUploadField());
        formLayout.add(profileImagePreview);
        formLayout.add(createActionButtons());
        // Thêm main layout vào view
        if(isUserLoggedIn()) {
            add(formLayout);
            UserRequest userRequest = modelMapper.map(currentUser, UserRequest.class);
            binder.setBean(userRequest);
            binder.addValueChangeListener(e -> validateForm());
            validateForm(); // Initial validation
        }
    }

    private boolean isUserLoggedIn() {
        return authenticationService.isUserLoggedIn();
    }

    private void showOtpDialog() {
        emailVerificationService.sendOtp(emailField.getValue(), authenticationService.getUserFromLocalCache().getUsername());
        Button confirmOtpButton = new Button("Confirm", e -> verifyOtp());
        // Sử dụng Span thay cho Label để tránh cảnh báo
        Span otpMessage = new Span("Please check your email for the OTP code.");
        Span timerLabel = new Span("60");

        VerticalLayout layout = new VerticalLayout(otpMessage, otpField, timerLabel, confirmOtpButton);

        // Xóa tất cả các thành phần cũ khỏi otpDialog và thêm layout mới
        otpDialog.removeAll();
        otpDialog.add(layout);

        otpDialog.open();

        final int[] remainingSeconds = {60}; // Thời gian còn lại là 60 giây

        // Lấy tham chiếu đến UI hiện tại
        UI currentUI = UI.getCurrent();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                currentUI.access(() -> {
                    remainingSeconds[0] -= 1;
                    timerLabel.setText(String.valueOf(remainingSeconds[0]));

                    if (remainingSeconds[0] <= 0) {
                        otpMessage.setText("Your OTP code has expired. Please resend.");
                        // TODO: Add resend button or logic to allow the user to resend OTP
                        timer.cancel(); // Stop the timer
                    }
                    UI.getCurrent().setPollInterval(1000);
                });

            }
        }, 0, 1000);  // Thực hiện sau mỗi 1 giây (1000 ms)
    }

    // Xác minh mã OTP
    private void verifyOtp() {
        String enteredOtp = otpField.getValue();
        boolean isVerifyOtpSuccessful = emailVerificationService.verifyOtp(emailField.getValue(), enteredOtp);
        otpDialog.close();
        String message = isVerifyOtpSuccessful ?
                "Email was successfully updated." :
                "Failed to update email.";
        NotificationUtils.NotificationType notificationType = isVerifyOtpSuccessful ? NotificationUtils.NotificationType.SUCCESS : NotificationUtils.NotificationType.ERROR;
        NotificationUtils.showNotification(message, notificationType);
    }

    private void validateForm() {
        boolean isBasicInfoValid = binder.isValid();
        boolean isPasswordValid = true;

        // Reset custom error messages and invalid flags
        oldPasswordField.setErrorMessage(null);
        oldPasswordField.setInvalid(false);
        newPasswordField.setErrorMessage(null);
        newPasswordField.setInvalid(false);
        confirmPasswordField.setErrorMessage(null);
        confirmPasswordField.setInvalid(false);

        // Check password fields only if the user has entered a new password
        if (!newPasswordField.getValue().isEmpty()) {
            isPasswordValid = newPasswordField.getValue().equals(confirmPasswordField.getValue()) &&
                    newPasswordField.getValue().length() >= 8 && newPasswordField.getValue().length() <= 128 &&
                    oldPasswordField.getValue().length() >= 8 && oldPasswordField.getValue().length() <= 128;

            if (!isPasswordValid) {
                if (!newPasswordField.getValue().equals(confirmPasswordField.getValue())) {
                    confirmPasswordField.setErrorMessage("Password does not match");
                    confirmPasswordField.setInvalid(true);
                }

                if (newPasswordField.getValue().length() < 8 || newPasswordField.getValue().length() > 128) {
                    newPasswordField.setErrorMessage("New password must be between 8 and 128 characters");
                    newPasswordField.setInvalid(true);
                }

                if (oldPasswordField.getValue().length() < 8 || oldPasswordField.getValue().length() > 128) {
                    oldPasswordField.setErrorMessage("Old password must be between 8 and 128 characters");
                    oldPasswordField.setInvalid(true);
                }
            }
        }

        saveButton.setEnabled(isBasicInfoValid && isPasswordValid);
    }

    private FormLayout createFormRows() {
        FormLayout formRows = new FormLayout();

        HorizontalLayout emailRow = new HorizontalLayout(emailField, updateEmailButton);
        emailRow.setWidthFull();
        emailRow.setAlignItems(FlexComponent.Alignment.END);

        formRows.add(firstNameField, lastNameField, usernameField, emailRow, oldPasswordField, newPasswordField, confirmPasswordField);
        formRows.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("490px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP));

        return formRows;
    }


    private PasswordField createPasswordField(String label, String initialValue) {
        PasswordField passwordField = new PasswordField(label, initialValue);
        passwordField.setWidth("100%");
        return passwordField;
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
        return new HorizontalLayout(saveButton);
    }

    private void handleSaveAction() {
        boolean isSaveSuccessful = saveUser();
        account.setUsername(usernameField.getValue());
        authenticationService.addUserToLocalCache(account);
        String message = isSaveSuccessful ? MessageConstants.USER_UPDATED_SUCCESSFULLY : MessageConstants.USER_UPDATE_ERROR_FAIL;
        NotificationUtils.NotificationType notificationType = isSaveSuccessful ? NotificationUtils.NotificationType.SUCCESS : NotificationUtils.NotificationType.ERROR;
        NotificationUtils.showNotification(message, notificationType);
    }

    private boolean saveUser() {
        UserRequest user = new UserRequest();
        user.setFirstName(firstNameField.getValue());
        user.setLastName(lastNameField.getValue());
        user.setUsername(usernameField.getValue());
        user.setOldPassword(oldPasswordField.getValue());
        user.setNewPassword(newPasswordField.getValue());


        try {
            ByteArrayInputStream byteArrayInputStream = null;

            if (uploadedImageBytes != null) {
                byteArrayInputStream = new ByteArrayInputStream(uploadedImageBytes);
            }

            return userService.updateProfile(currentUsername, user, byteArrayInputStream, fileName, fileExtension);
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
}

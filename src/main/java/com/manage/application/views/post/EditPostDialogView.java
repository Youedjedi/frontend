package com.manage.application.views.post;

import com.manage.application.domain.request.post.PostRequest;
import com.manage.application.domain.response.post.PostSingleResponse;
import com.manage.application.domain.response.user.UserSelectResponse;
import com.manage.application.service.PostService;
import com.manage.application.service.UserService;
import com.manage.application.utils.NotificationUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EditPostDialogView extends Dialog {

    private final TextField titleField;

private final PostSingleResponse postEdit = new PostSingleResponse();
    private final TextArea excerptField;
    private final TextArea contentField;
    private Button saveButton;
    private final Image thumbnailPreview; // Uncomment this line
    private final Binder<PostRequest> binder; // Uncomment this line
    private final PostService postService = new PostService();
    private final UserService userService = new UserService();
    private Set<UserSelectResponse> selectedUsers = new HashSet<>();
    private byte[] uploadedImageBytes = null;
    private String fileName = "";
    private String fileExtension = "";
    private ImageListViewCard imageListView;

    public EditPostDialogView(Long id, ImageListViewCard imageListView) {
        this.imageListView  = imageListView;
        this.binder = new Binder<>(PostRequest.class);
        titleField = createTextField("Title", postEdit.getTitle());
        excerptField = createTextArea("Excerpt", postEdit.getExcerpt());
        contentField = createTextArea("Content", postEdit.getContent());
        thumbnailPreview = new Image();
        thumbnailPreview.setMaxHeight("150px");
        titleField.setWidthFull();
        excerptField.setWidthFull();
        contentField.setWidthFull();

        setupFieldBindings();

        // Use the new method to create the form for selecting users
        VerticalLayout userSelectionLayout = createFormRows();

        VerticalLayout layout = new VerticalLayout(titleField, excerptField, contentField, userSelectionLayout, createUploadField(), thumbnailPreview, createActionButtons());
        layout.setPadding(true);
        layout.setSpacing(true);
        this.setWidth("800px"); // Đặt chiều rộng là 800 pixels
        add(layout);
    }


    private VerticalLayout createFormRows() {
        // Create a DataProvider for user data
        DataProvider<UserSelectResponse, String> userDataProvider = createUserDataProvider();

        // Create MultiSelectComboBox
        MultiSelectComboBox<UserSelectResponse> multiSelectComboBox = createMultiSelectComboBox(userDataProvider);

        // Create a TextArea for displaying selected users
        TextArea selectedUsersField = createSelectedUsersDisplayArea(multiSelectComboBox);

        // Create the layout and add components
        VerticalLayout layout = new VerticalLayout();
        layout.add(multiSelectComboBox, selectedUsersField);
        layout.setWidthFull();

        return layout;
    }

    private DataProvider<UserSelectResponse, String> createUserDataProvider() {
        return DataProvider.fromFilteringCallbacks(
                query -> {
                    int limit = query.getLimit();
                    int offset = query.getOffset();
                    int page = query.getPage();
                    List<UserSelectResponse> users = userService.getUserSelect(query.getFilter().orElse("")).getData();
                    return users.stream();
                },
                query -> userService.getUserSelect(query.getFilter().orElse("")).getData().size()
        );
    }

    private MultiSelectComboBox<UserSelectResponse> createMultiSelectComboBox(DataProvider<UserSelectResponse, String> userDataProvider) {
        MultiSelectComboBox<UserSelectResponse> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(userDataProvider);
        comboBox.setLabel("Editors");
        comboBox.setPlaceholder("Select editors");
        comboBox.setItemLabelGenerator(UserSelectResponse::getUsername);
        comboBox.setWidthFull();

        // Set giá trị mặc định
        List<Long> editorIds = postEdit.getEditorIds();
        List<UserSelectResponse> users = userService.getUserSelect("").getData();
        Set<UserSelectResponse> defaultSelectedUsers = users.stream()
                .filter(user -> editorIds.contains(user.getId()))
                .collect(Collectors.toSet());
        comboBox.setValue(defaultSelectedUsers);

        return comboBox;
    }

    private TextArea createSelectedUsersDisplayArea(MultiSelectComboBox<UserSelectResponse> comboBox) {
        TextArea selectedUsersField = new TextArea("Selected Users");
        selectedUsersField.setReadOnly(true);
        selectedUsersField.setWidthFull();

        comboBox.addValueChangeListener(event -> {
            // Cập nhật danh sách selectedUsers dựa trên sự kiện thay đổi giá trị của comboBox
            selectedUsers.clear();
            selectedUsers.addAll(event.getValue());

            String selectedUsersText = selectedUsers.stream()
                    .map(UserSelectResponse::getUsername)
                    .collect(Collectors.joining(", "));
            selectedUsersField.setValue(selectedUsersText);
        });

        return selectedUsersField;
    }

    // Phương thức truy xuất danh sách editorIds từ selectedUsers
    public List<Long> getSelectedEditorIds() {
        return selectedUsers.stream()
                .map(UserSelectResponse::getId) // giả sử UserSelectResponse có phương thức getId() trả về Long
                .collect(Collectors.toList());
    }


    private Upload createUploadField() {
        MemoryBuffer buffer = new MemoryBuffer();
        Upload thumbnailUpload = new Upload(buffer);
        thumbnailUpload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif");
        thumbnailUpload.addSucceededListener(event -> {
            handleImageUpload(buffer, event.getFileName());
        });
        return thumbnailUpload;
    }

    private HorizontalLayout createActionButtons() {
        saveButton = new Button("Save", event -> handleSaveAction());
        Button closeButton = new Button("Close", event -> close());
        return new HorizontalLayout(saveButton, closeButton);
    }

    private void handleImageUpload(MemoryBuffer buffer, String fileName) {
        try {
            uploadedImageBytes = IOUtils.toByteArray(buffer.getInputStream());
            this.fileName = fileName;
            fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
            StreamResource resource = new StreamResource(fileName, () -> new ByteArrayInputStream(uploadedImageBytes));
            thumbnailPreview.setSrc(resource);
        } catch (IOException e) {
            Notification.show("Failed to upload the image.");
        }
    }


    private void setupFieldBindings() {
        // Bind và validate tiêu đề
        binder.forField(titleField)
                .withValidator(new StringLengthValidator(
                        "Title must be between 5 and 100 characters", 5, 100))
                .bind(PostRequest::getTitle, PostRequest::setTitle);

        // Bind và validate đoạn trích
        binder.forField(excerptField)
                .withValidator(new StringLengthValidator(
                        "Excerpt must be between 10 and 200 characters", 10, 200))
                .bind(PostRequest::getExcerpt, PostRequest::setExcerpt);

        // Bind và validate nội dung
        binder.forField(contentField)
                .withValidator(new StringLengthValidator(
                        "Content must be at least 10 characters", 10, null))
                .bind(PostRequest::getContent, PostRequest::setContent);

        // Lắng nghe sự kiện thay đổi giá trị để kiểm tra tình trạng của form
        binder.addValueChangeListener(e -> validateForm());
    }

    private void validateForm() {
        boolean isValid = binder.isValid();
        saveButton.setEnabled(isValid);
    }
    private void handleSaveAction() {

        PostRequest postRequest = new PostRequest();
        binder.writeBeanIfValid(postRequest);
        postRequest.setEditorIds(getSelectedEditorIds());
        try {
            ByteArrayInputStream byteArrayInputStream = null;

            if (uploadedImageBytes != null) {
                byteArrayInputStream = new ByteArrayInputStream(uploadedImageBytes);
            }
            postService.addPost(postRequest, byteArrayInputStream, fileName, fileExtension);
        } catch (IOException e) {
            NotificationUtils.showNotification("Có lỗi xảy ra khi lưu post.", NotificationUtils.NotificationType.ERROR);
        }
        NotificationUtils.showNotification("Post saved successfully.", NotificationUtils.NotificationType.SUCCESS);
        close();

    }

    private TextField createTextField(String label, String initialValue) {
        TextField textField = new TextField(label, initialValue);
        textField.setWidthFull();
        return textField;
    }

    private TextArea createTextArea(String label, String initialValue) {
        TextArea textArea = new TextArea(label, initialValue);
        textArea.setWidthFull();
        return textArea;
    }
}

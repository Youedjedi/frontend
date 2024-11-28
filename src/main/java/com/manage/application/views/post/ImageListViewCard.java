package com.manage.application.views.post;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility.*;

public class ImageListViewCard extends ListItem {

    public ImageListViewCard(String text, String excerpt, String url, Long id) {
        addClassNames(Background.CONTRAST_5, Display.FLEX, FlexDirection.COLUMN, AlignItems.START, Padding.MEDIUM,
                BorderRadius.LARGE);

        Div div = new Div();
        div.addClassNames(Background.CONTRAST, Display.FLEX, AlignItems.CENTER, JustifyContent.CENTER,
                Margin.Bottom.MEDIUM, Overflow.HIDDEN, BorderRadius.MEDIUM, Width.FULL);
        div.setHeight("160px");

        Image image = new Image();
        image.setWidth("100%");
        image.setSrc(url);
        image.setAlt(text);

        div.add(image);

        Span header = new Span();
        header.addClassNames(FontSize.XLARGE, FontWeight.SEMIBOLD);
        header.setText(text);

        Span subtitle = new Span();
        subtitle.addClassNames(FontSize.SMALL, TextColor.SECONDARY);
        subtitle.setText("Card subtitle");

        Paragraph description = new Paragraph(excerpt);
        description.addClassName(Margin.Vertical.MEDIUM);

        Span badge = new Span();
        badge.getElement().setAttribute("theme", "badge");
        badge.setText("Label");

        HorizontalLayout buttonContainer = new HorizontalLayout();
        buttonContainer.addClassName("button-container");
        buttonContainer.setWidthFull(); // Đặt chiều rộng tối đa cho container
        Button editButton = new Button("Edit");
        editButton.addClickListener(event -> handleEditClick(id));
        Button deleteButton = new Button("Delete");
        editButton.setWidthFull(); // Đặt chiều rộng tối đa cho nút Edit
        deleteButton.setWidthFull(); // Đặt chiều rộng tối đa cho nút Delete

        buttonContainer.add(editButton, deleteButton);


        add(div, header, subtitle, description, badge, buttonContainer);

    }
    private void handleEditClick(Long id) {
        EditPostDialogView dialog = new EditPostDialogView(id, this);
        dialog.open();
    }
}

package com.manage.application.views.post;

import com.manage.application.domain.response.post.PostListResponse;
import com.manage.application.domain.response.post.PostListApiResponse;
import com.manage.application.service.PostService;
import com.manage.application.views.MainLayout;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.OrderedList;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.*;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Post List")
@Route(value = "posts", layout = MainLayout.class)
public class ImageListView extends Main implements HasComponents, HasStyle {

    private TextField searchField;
    private OrderedList imageContainer;
    private int currentPage = 0;
    private Button loadMoreButton;
    private Icon refreshIcon;
    private final int itemsPerPage = 10;  // Number of items to show per page
    private PostService postService = new PostService();

    public ImageListView() {
        constructUI();
        loadPage(currentPage);
    }

    private void constructUI() {
        addClassNames("image-list-view");
        addClassNames(MaxWidth.SCREEN_LARGE, Margin.Horizontal.AUTO, Padding.Bottom.LARGE, Padding.Horizontal.LARGE);

        VerticalLayout container = new VerticalLayout();
        container.addClassNames(AlignItems.CENTER);

        imageContainer = new OrderedList();
        imageContainer.addClassNames(Gap.MEDIUM, Display.GRID, ListStyleType.NONE, Margin.NONE, Padding.NONE);

        loadMoreButton = new Button("Load More");
        refreshIcon = new Icon(VaadinIcon.REFRESH);
        loadMoreButton.setIcon(refreshIcon);
        loadMoreButton.addClickListener(e -> {
            loadMore();
        });
        // Tạo và cấu hình TextField và Button cho tìm kiếm
        searchField = new TextField();
        searchField.setPlaceholder("Enter search keyword...");
        searchField.setWidth("300px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(event -> {
                imageContainer.removeAll();
                currentPage = 0;  // Reset to first page for a new search
                loadPage(currentPage);
        });
        HorizontalLayout headerSearchLayout = new HorizontalLayout();
        headerSearchLayout.setWidthFull();  // Take full available width
        headerSearchLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);  // Space components out between each other
        headerSearchLayout.setAlignItems(FlexComponent.Alignment.CENTER);  // Vertically center align items

        HorizontalLayout searchLayout = new HorizontalLayout();
        searchLayout.setSpacing(true);  // Add some space between components
        searchLayout.add(searchField);

        Button addPostButton = new Button("Add Post", new Icon(VaadinIcon.PLUS));
        addPostButton.addClickListener(e -> {
            AddPostDialogView addPostDialogView = new AddPostDialogView(this);  // Create a new dialog
            addPostDialogView.open();  // Open the dialog
        });
        headerSearchLayout.add(searchLayout, addPostButton);  // Thêm nút "Add Post" vào layout
        container.add(headerSearchLayout, imageContainer, loadMoreButton);
        add(container);
    }

    public void loadMore() {
        startLoadingAnimation();

        UI.getCurrent().getPage().retrieveExtendedClientDetails(details -> {
            loadPage(++currentPage);
            stopLoadingAnimation();
        });
    }

    private void startLoadingAnimation() {
        refreshIcon.getElement().getStyle().set("animation", "spin 1s linear infinite");
    }

    private void stopLoadingAnimation() {
        refreshIcon.getElement().getStyle().set("animation", "none");
    }
    public void loadPage(int page) {
        if(page == 0) {
            imageContainer.removeAll();
        }
        List<ImageListViewCard> items = fetchAllItems(page, itemsPerPage);
        // Add items to the imageContainer
        for (ImageListViewCard item : items) {
            imageContainer.add(item);
        }
        // Load more items to check if there's another page available
        List<ImageListViewCard> checkItems = fetchAllItems(page + 1, itemsPerPage);
        loadMoreButton.setEnabled(!checkItems.isEmpty());
    }

    private List<ImageListViewCard> fetchAllItems(int page, int size) {
        List<ImageListViewCard> items = new ArrayList<>();

        String keyword = searchField.getValue();
        PostListApiResponse response = postService.getPosts(keyword, page, size);

        if (response.getHttpStatusCode() == 200 && response.getData() != null && response.getData().getContent() != null) {
            for (PostListResponse post : response.getData().getContent()) {
                items.add(new ImageListViewCard(post.getTitle(), post.getExcerpt(), post.getThumbnailUrl(), post.getId()));
            }
        }
        return items;
    }

}

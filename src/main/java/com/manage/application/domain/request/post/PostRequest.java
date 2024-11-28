package com.manage.application.domain.request.post;

import java.util.List;

public class PostRequest {
    private String title;
    private String excerpt;
    private String content;
    private String thumbnailUrl;
    private List<Long> editorIds;

    // Getters và Setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public List<Long> getEditorIds() {
        return editorIds;
    }

    public void setEditorIds(List<Long> editorIds) {
        this.editorIds = editorIds;
    }

    @Override
    public String toString() {
        return "PostRequest [title=" + title + ", excerpt=" + excerpt + ", content=" + content + ", thumbnailUrl="
                + thumbnailUrl + ", editorIds=" + editorIds + "]";
    }
}


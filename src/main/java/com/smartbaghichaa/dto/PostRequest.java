package com.smartbaghichaa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class PostRequest {

    @Size(max = 200, message = "Title must be under 200 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 10, max = 5000, message = "Content must be 10–5000 characters")
    private String content;

    private List<String> tags;
    private String photoData;

    public String getTitle()                { return title; }
    public void setTitle(String t)          { this.title = t; }
    public String getContent()              { return content; }
    public void setContent(String c)        { this.content = c; }
    public List<String> getTags()           { return tags; }
    public void setTags(List<String> t)     { this.tags = t; }
    public String getPhotoData()            { return photoData; }
    public void setPhotoData(String p)      { this.photoData = p; }
}

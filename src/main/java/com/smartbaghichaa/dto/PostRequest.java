package com.smartbaghichaa.dto;

import java.util.List;

public class PostRequest {
    private String content;
    private List<String> tags;

    public String getContent()         { return content; }
    public void setContent(String c)   { this.content = c; }
    public List<String> getTags()      { return tags; }
    public void setTags(List<String> t){ this.tags = t; }
}

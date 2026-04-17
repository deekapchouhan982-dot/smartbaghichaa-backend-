package com.smartbaghichaa.dto;

import jakarta.validation.constraints.Size;

public class CommentRequest {
    // ISSUE-16: Renamed from 'content' to 'text' for clarity. Backward compat via alias getter.
    @Size(min = 1, max = 1000, message = "Comment must be 1–1000 characters")
    private String text;

    @Size(min = 1, max = 1000, message = "Comment must be 1–1000 characters")
    private String content;  // backward-compat alias (old clients)

    public String getText()            { return text; }
    public void setText(String t)      { this.text = t; }
    public String getContent()         { return content; }
    public void setContent(String c)   { this.content = c; }

    // Returns whichever field was provided (text preferred, content as fallback)
    public String getEffectiveContent() {
        if (text != null && !text.isBlank()) return text;
        return content;
    }
}

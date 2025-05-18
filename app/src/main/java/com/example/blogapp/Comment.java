package com.example.blogapp;

public class Comment {
    private String username;
    private String commentText;
    private String timestamp;
    private String blogId;

    public Comment() {
        // Required empty constructor for Firebase
    }

    public Comment(String username, String commentText, String timestamp,String blogId) {
        this.username = username;
        this.commentText = commentText;
        this.timestamp = timestamp;
        this.blogId=blogId;
    }

    public String getUsername() { return username; }
    public String getCommentText() { return commentText; }
    public String getTimestamp() { return timestamp; }
    public String getBlogId() { return blogId; }

}

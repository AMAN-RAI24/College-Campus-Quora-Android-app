package com.example.collegecampusquora.model;

public class Comment {
    String text;
    Long commentTime;
    Long upVotes;
    Long downVotes;
    String author;

    public Comment(String text, Long commentTime, Long upVotes, Long downVotes, String author) {
        this.text = text;
        this.commentTime = commentTime;
        this.upVotes = upVotes;
        this.downVotes = downVotes;
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public Long getCommentTime() {
        return commentTime;
    }

    public Long getUpVotes() {
        return upVotes;
    }

    public Long getDownVotes() {
        return downVotes;
    }

    public String getAuthor() {
        return author;
    }
}

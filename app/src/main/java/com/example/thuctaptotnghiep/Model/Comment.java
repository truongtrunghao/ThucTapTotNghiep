package com.example.thuctaptotnghiep.Model;

public class Comment {
    private String comment;
    private String publisher;
    private Long datetime;
    private String postid;
    private String post_publisherid;
    private String reply_cmt_pulisherid;


    public Comment(String comment, String publisher, Long datetime, String postid, String post_publisherid, String reply_cmt_pulisherid) {
        this.comment = comment;
        this.publisher = publisher;
        this.datetime = datetime;
        this.postid = postid;
        this.post_publisherid = post_publisherid;
        this.reply_cmt_pulisherid = reply_cmt_pulisherid;
    }

    public Comment() {
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Long getDatetime() {
        return datetime;
    }

    public void setDatetime(Long datetime) {
        this.datetime = datetime;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getPost_publisherid() {
        return post_publisherid;
    }

    public void setPost_publisherid(String post_publisherid) {
        this.post_publisherid = post_publisherid;
    }

    public String getReply_cmt_pulisherid() {
        return reply_cmt_pulisherid;
    }

    public void setReply_cmt_pulisherid(String reply_cmt_pulisherid) {
        this.reply_cmt_pulisherid = reply_cmt_pulisherid;
    }
}

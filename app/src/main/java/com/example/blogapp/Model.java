package com.example.blogapp;

public class Model {
    String tittle, desc, author, date, img, share_count, id, timestamp, userId;

    public Model(String tittle, String desc, String author, String date, String img, String share_count, String id, String timestamp, String userId) {
        this.tittle = tittle;
        this.desc = desc;
        this.author = author;
        this.date = date;
        this.img = img;
        this.share_count = share_count;
        this.id = id;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public Model() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTittle() { return tittle; }
    public void setTittle(String tittle) { this.tittle = tittle; }

    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getImg() { return img; }
    public void setImg(String img) { this.img = img; }

    public String getShare_count() { return share_count; }
    public void setShare_count(String share_count) { this.share_count = share_count; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getUserId() { return userId; }
}

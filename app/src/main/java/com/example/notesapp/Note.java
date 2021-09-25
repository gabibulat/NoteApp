package com.example.notesapp;

public class Note {
    private String title, content, date, image;

    public Note(){}
    public Note (String title, String content, String date, String image){
        this.title = title;
        this.content = content;
        this.date = date;
        this.image = image;
    }

    //region getteri i setteri
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    //endregion
}

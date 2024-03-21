package com.example.myrail;

public class CoachItem {
    private int imageResource;
    private String text;

    public CoachItem(int imageResource, String text) {
        this.imageResource = imageResource;
        this.text = text;
    }

    public int getImageResource() {
        return imageResource;
    }

    public String getText() {
        return text;
    }
}

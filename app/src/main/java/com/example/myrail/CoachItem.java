package com.example.myrail;

public class CoachItem {
    private int imageResource;
    private String cid,cno;

    public CoachItem(int imageResource, String cid, String cno) {
        this.imageResource = imageResource;
        this.cid = cid;
        this.cno = cno;
    }

    public int getImageResource() {
        return imageResource;
    }

    public String getCid() {
        return cid;
    }
    public String getCno() {
        return cno;
    }
}

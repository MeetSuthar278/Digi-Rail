package com.example.myrail;

public class TrainDialogList {
    private String name;
    private String number;

    public TrainDialogList(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public String getTName() {
        return name;
    }

    public String getTNumber() {
        return number;
    }
}

package com.example.myrail;

public class TrainListPage {

    private String trainNumber;
    private String trainName;
    private String trainTime;

    private boolean[] days;

    public TrainListPage(String trainNumber, String trainName, String trainTime, boolean[] days) {
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.trainTime = trainTime;
        this.days = days;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public String getTrainTime() {
        return trainTime;
    }

    public boolean[] getDays(){ return days; }
}

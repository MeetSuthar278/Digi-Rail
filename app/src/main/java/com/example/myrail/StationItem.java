package com.example.myrail;

public class StationItem {
    private String StationName,StationCode;
    private int StationIcon;

    public StationItem(String StationName, String StationCode, int StationIcon){
        this.StationName = StationName;
        this.StationCode = StationCode;
        this.StationIcon = StationIcon;
    }
    public String getStationName(){
        return StationName;
    }
    public String getStationCode(){
        return StationCode;
    }
    public int getStationIcon(){
        return StationIcon;
    }
}
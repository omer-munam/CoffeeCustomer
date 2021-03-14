package com.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class TimingModel implements Serializable {

    ArrayList<Date> dateList;
    String storeID;
    String locID;
    ArrayList<String> costList;

    public TimingModel(ArrayList<Date> dateList, String storeID, String locID, ArrayList<String> costList) {
        this.dateList = dateList;
        this.storeID = storeID;
        this.locID = locID;
        this.costList = costList;
    }

    public ArrayList<Date> getDateList() {
        return dateList;
    }

    public void setDateList(ArrayList<Date> dateList) {
        this.dateList = dateList;
    }

    public String getStoreID() {
        return storeID;
    }

    public void setStoreID(String storeID) {
        this.storeID = storeID;
    }

    public String getLocID() {
        return locID;
    }

    public void setLocID(String locID) {
        this.locID = locID;
    }

    public ArrayList<String> getCostList() {
        return costList;
    }

    public void setCostList(ArrayList<String> costList) {
        this.costList = costList;
    }
}

package com.models;

import java.io.Serializable;

public class TimingListShowModel implements Serializable {


    public TimingListShowModel(String showTime, String actualTime) {
        this.showTime = showTime;
        this.actualTime = actualTime;
    }

    private String showTime;
    private String actualTime;

    public String getShowTime() {
        return showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }

    public String getActualTime() {
        return actualTime;
    }

    public void setActualTime(String actualTime) {
        this.actualTime = actualTime;
    }
}

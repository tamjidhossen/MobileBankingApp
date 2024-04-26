package com.example.mobilebankingapp;

public class ModelTransaction {

    String trnName, trnCategory, trnAmount, trnState;
    Long trnTime;

    public ModelTransaction() {

    }

    public ModelTransaction(String trnName, String trnCategory, Long trnTime, String trnAmount, String trnState) {
        this.trnName = trnName;
        this.trnCategory = trnCategory;
        this.trnTime = trnTime;
        this.trnAmount = trnAmount;
        this.trnState = trnState;
    }

    public String getTrnName() {
        return trnName;
    }

    public void setTrnName(String trnName) {
        this.trnName = trnName;
    }

    public String getTrnCategory() {
        return trnCategory;
    }

    public void setTrnCategory(String trnCategory) {
        this.trnCategory = trnCategory;
    }

    public Long getTrnTime() {
        return trnTime;
    }

    public void setTrnTime(Long trnTime) {
        this.trnTime = trnTime;
    }

    public String getTrnAmount() {
        return trnAmount;
    }

    public void setTrnAmount(String trnAmount) {
        this.trnAmount = trnAmount;
    }

    public String getTrnState() {
        return trnState;
    }

    public void setTrnState(String trnState) {
        this.trnState = trnState;
    }
}

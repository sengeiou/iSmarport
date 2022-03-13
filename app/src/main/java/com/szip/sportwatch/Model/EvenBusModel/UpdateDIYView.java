package com.szip.sportwatch.Model.EvenBusModel;

public class UpdateDIYView {
    private int type;
    private int data;


    public UpdateDIYView(int type) {
        this.type = type;
    }

    public UpdateDIYView(int type, int data) {
        this.type = type;
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public int getData() {
        return data;
    }
}

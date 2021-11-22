package com.szip.sportwatch.Model.EvenBusModel;

public class UpdateDialView {
    private int type;
    private int data;


    public UpdateDialView(int type) {
        this.type = type;
    }

    public UpdateDialView(int type, int data) {
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

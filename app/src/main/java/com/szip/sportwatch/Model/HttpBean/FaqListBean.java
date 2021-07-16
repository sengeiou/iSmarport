package com.szip.sportwatch.Model.HttpBean;


import com.szip.sportwatch.Model.FaqModel;

import java.util.ArrayList;

public class FaqListBean extends BaseApi{
    private Data data;

    public Data getData() {
        return data;
    }

    public class Data {
        private ArrayList<FaqModel> list;

        public ArrayList<FaqModel> getList() {
            return list;
        }
    }
}

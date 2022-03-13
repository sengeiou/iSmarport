package com.szip.sportwatch.Model.HttpBean;

import java.io.Serializable;
import java.util.ArrayList;

public class DialBean extends BaseApi{

    private  Data data;

    public Data getData() {
        return data;
    }

    public class Data{
        int pageNum;
        int pageSize;
        int size;
        int total;
        int pages;
        ArrayList<Dial> list;

        public ArrayList<Dial> getList() {
            return list;
        }
    }

    public class Dial implements Serializable {
        String watchModel;
        int screen;
        int pointerNumber;
        String previewUrl;
        String plateBgUrl;
        String pointerImg;

        public String getWatchModel() {
            return watchModel;
        }

        public int getScreen() {
            return screen;
        }

        public int getPointerNumber() {
            return pointerNumber;
        }

        public String getPreviewUrl() {
            return previewUrl;
        }

        public String getPlateBgUrl() {
            return plateBgUrl;
        }

        public String getPointerImg() {
            return pointerImg;
        }
    }
}

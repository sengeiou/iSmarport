package com.szip.sportwatch.Model.HttpBean;

import com.szip.sportwatch.Model.UserInfo;

public class AvatarBean extends BaseApi{

    private Data data;

    public class Data{
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}

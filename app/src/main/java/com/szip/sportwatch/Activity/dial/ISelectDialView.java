package com.szip.sportwatch.Activity.dial;

public interface ISelectDialView {
    void initList(boolean initSuccess);
    void setView(String id,String pictureId);
    void setDialView(String dialId,String pictureId);
    void setDialProgress(int max);
}

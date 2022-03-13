package com.szip.sportwatch.Activity.diy;

import android.net.Uri;

import androidx.recyclerview.widget.RecyclerView;

import com.szip.sportwatch.Model.HttpBean.DialBean;

import java.util.ArrayList;

public interface IDiyPresenter {
    void getViewConfig(RecyclerView dialRv, ArrayList<DialBean.Dial> dialArrayList);
    void sendDial(Uri resultUri,int clock);
    void cropPhoto(Uri uri);
    void setViewDestory();

    void startToSendDial();
    void sendDialDiy(String resultUri, int address);
    void resumeSendDial(int page);
}

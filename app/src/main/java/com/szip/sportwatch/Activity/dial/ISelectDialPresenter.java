package com.szip.sportwatch.Activity.dial;

import android.net.Uri;

import androidx.recyclerview.widget.RecyclerView;

import com.szip.sportwatch.Model.HttpBean.DialBean;

import java.util.ArrayList;

public interface ISelectDialPresenter {
    void getViewConfig(RecyclerView dialRv, ArrayList<DialBean.Dial> dialArrayList);
    void startToSendDial();
    void sendDial(String resultUri,int address);
    void resumeSendDial(int page);
    void setViewDeStory();
}

package com.szip.sportwatch.Activity.dial;

import android.net.Uri;

import androidx.recyclerview.widget.RecyclerView;

public interface ISelectDialPresenter {
    void getViewConfig(RecyclerView dialRv);
    void startToSendDial();
    void sendDial(String resultUri,int address);
    void resumeSendDial(int page);
    void setViewDestory();
}

package com.szip.sportwatch.Activity.dial;

import android.net.Uri;

import androidx.recyclerview.widget.RecyclerView;

public interface ISelectDialPresenter {
    void getViewConfig(RecyclerView dialRv);
    void sendDial(String resultUri, int clock);
    void setViewDestory();
}

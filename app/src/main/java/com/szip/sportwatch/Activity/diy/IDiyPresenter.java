package com.szip.sportwatch.Activity.diy;

import android.net.Uri;

import androidx.recyclerview.widget.RecyclerView;

public interface IDiyPresenter {
    void getViewConfig(RecyclerView dialRv);
    void sendDial(Uri resultUri,int clock);
    void cropPhoto(Uri uri);
    void setViewDestory();
}

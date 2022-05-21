package com.szip.sportwatch.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.szip.sportwatch.DB.dbModel.NotificationData;
import com.szip.sportwatch.DB.dbModel.NotificationData_Table;
import com.szip.sportwatch.Interface.OnSmsStateListener;
import com.szip.sportwatch.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends BaseAdapter {

    private List<NotificationData> notificationDatas = new ArrayList<>();
    private Context mContext;
    private OnSmsStateListener onSmsStateListener;

    public class ViewHolder {
        public ImageView package_icon;
        public TextView nameTv;
        public Switch stateSw;
    }

    public NotificationAdapter(Context context, OnSmsStateListener onSmsStateListener) {
        mContext = context;
        this.onSmsStateListener = onSmsStateListener;
    }

    public void setNotificationDatas(List<NotificationData> notificationDatas) {
        if (notificationDatas==null)
            this.notificationDatas.clear();
        else
            this.notificationDatas = notificationDatas;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return (notificationDatas.size());
    }

    @Override
    public Object getItem(int position) {
        return notificationDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {

        ViewHolder viewHolder = null;
        /*
         * TextView tvAppName; ImageView ivIcon; Switch swPush;
         */

        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.adaper_notification, null, false);
            viewHolder = new ViewHolder();
            viewHolder.nameTv = view.findViewById(R.id.nameTv);
            viewHolder.stateSw = view.findViewById(R.id.stateSw);
            viewHolder.package_icon = view.findViewById(R.id.package_icon);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        NotificationData packageItem = null;

        viewHolder.stateSw
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int index = position;
                        if (index == 0) {
                            if (onSmsStateListener!=null)
                                onSmsStateListener.onSmsStateChange(isChecked);
                        }
                        NotificationData data = notificationDatas.get(position);
                        data.state = isChecked;
                        data.update();
                    }
                });

        packageItem = notificationDatas.get(position);
        viewHolder.nameTv.setText(packageItem.name);
        viewHolder.stateSw.setChecked(packageItem.state);
        viewHolder.package_icon.setImageDrawable(mContext.getResources().getDrawable(packageItem.packageImgId));

        return view;
    }

    public void setSmsError(){
        if (null!=notificationDatas){
            NotificationData data = notificationDatas.get(0);
            data.state = false;
            data.update();
        }
        notifyDataSetChanged();
    }
}

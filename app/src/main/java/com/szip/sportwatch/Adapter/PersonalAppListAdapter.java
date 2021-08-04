package com.szip.sportwatch.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.szip.sportwatch.Interface.OnSmsStateListener;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Service.MainService;
import com.szip.sportwatch.Notification.BlockList;
import com.szip.sportwatch.Notification.IgnoreList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2020/1/7.
 */

public class PersonalAppListAdapter extends BaseAdapter {

    private static final String VIEW_ITEM_ICON = "package_icon";

    private static final String VIEW_ITEM_TEXT = "package_text";

    private static final String VIEW_ITEM_CHECKBOX = "package_switch";

    private static final String VIEW_ITEM_NAME = "package_name"; // Only for

    private List<Map<String, Object>> mPersonalAppList = new ArrayList<>();
    private Context mContext;
    private OnSmsStateListener onSmsStateListener;

    public class ViewHolder {
        public TextView tvAppName;

        public ImageView ivIcon;

        public Switch swPush;
    }

    public PersonalAppListAdapter(Context context, OnSmsStateListener onSmsStateListener) {
        mContext = context;
        this.onSmsStateListener = onSmsStateListener;
    }

    public void setmPersonalAppList(List<Map<String, Object>> mPersonalAppList) {
        this.mPersonalAppList = mPersonalAppList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return (mPersonalAppList.size() + 1);
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {

        ViewHolder viewHolder = null;
            /*
             * TextView tvAppName; ImageView ivIcon; Switch swPush;
             */

        if (view == null) {
            viewHolder = new ViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.adapter_package_list, null);
            view.setPadding(0, 30, 0, 30);
            viewHolder.tvAppName = (TextView) view.findViewById(R.id.package_text);
            viewHolder.ivIcon = (ImageView) view.findViewById(R.id.package_icon);
            viewHolder.swPush = (Switch) view.findViewById(R.id.package_switch);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
            if (viewHolder == null) {
                viewHolder = new ViewHolder();

                viewHolder.tvAppName = (TextView) view.findViewById(R.id.package_text);
                viewHolder.ivIcon = (ImageView) view.findViewById(R.id.package_icon);
                viewHolder.swPush = (Switch) view.findViewById(R.id.package_switch);
                view.setTag(viewHolder);
            }
        }

            /*
             * view = mInflater.inflate(R.layout.adapter_package_list, null);
             * view.setPadding(0, 20, 0, 20); tvAppName = (TextView)
             * view.findViewById(R.id.package_text); ivIcon = (ImageView)
             * view.findViewById(R.id.package_icon); swPush = (Switch)
             * view.findViewById(R.id.package_switch);
             */
        Map<String, Object> packageItem = null;

        viewHolder.swPush
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int index = position;
                        if (index == 0) {
                            if (onSmsStateListener!=null)
                                onSmsStateListener.onSmsStateChange(isChecked);
                            return;
                        }
                        Map<String, Object> item = mPersonalAppList.get(index - 1);
                        if (item == null) {
                            return;
                        }

                        // Toggle item selection
                        item.remove(VIEW_ITEM_CHECKBOX);
                        item.put(VIEW_ITEM_CHECKBOX, isChecked);

                        // update list data
                        String appName = (String) item.get(VIEW_ITEM_NAME);
                        if (!isChecked) {
                            IgnoreList.getInstance().addIgnoreItem(appName);
                        } else {
                            IgnoreList.getInstance().removeIgnoreItem(appName);
                            BlockList.getInstance().removeBlockItem(appName);
                        }
                    }
                });

        if (position >= 1) {
            packageItem = mPersonalAppList.get(position - 1);

            Drawable data = (Drawable) packageItem.get(VIEW_ITEM_ICON);
            viewHolder.ivIcon.setImageDrawable(data);

            String text = (String) packageItem.get(VIEW_ITEM_TEXT);
            viewHolder.tvAppName.setText(text);

            Boolean checked = (Boolean) packageItem.get(VIEW_ITEM_CHECKBOX);
            viewHolder.swPush.setChecked(checked);
        } else {
           if (position == 0) {
                viewHolder.ivIcon.setImageResource(R.mipmap.message_service);
                viewHolder.tvAppName.setText(R.string.sms_preference_title);
                Boolean checked = (MainService.getInstance().getSmsServiceStatus());
                viewHolder.swPush.setChecked(checked);
            }
        }
        return view;
    }


}

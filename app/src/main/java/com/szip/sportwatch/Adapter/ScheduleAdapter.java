package com.szip.sportwatch.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.szip.sportwatch.DB.dbModel.SportData;
import com.szip.sportwatch.Model.ScheduleData;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.DateUtil;
import com.szip.sportwatch.Util.MathUitl;

import java.util.ArrayList;
import java.util.List;

public class ScheduleAdapter extends BaseAdapter {
    private Context mContext;
    private List<ScheduleData> list = new ArrayList<>();


    public ScheduleAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setList(List<ScheduleData> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.adapter_schedule_list, null, false);
            holder = new ViewHolder();
            holder.timeTv = convertView.findViewById(R.id.timeTv);
            holder.msgTv = convertView.findViewById(R.id.msgTv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ScheduleData scheduleData = list.get(position);

        holder.msgTv.setText(scheduleData.getMsg());
        if (scheduleData.getTime()- DateUtil.getTimeOfToday()<24*60*60){
            holder.timeTv.setText(mContext.getString(R.string.today)+
                    DateUtil.getStringDateFromSecond1(scheduleData.getTime()," HH:mm"));
        }else {
            holder.timeTv.setText(DateUtil.getStringDateFromSecond1(scheduleData.getTime(),"MM/dd E HH:mm"));
        }

        return convertView;
    }
    private static class ViewHolder {
        TextView msgTv,timeTv;
    }
}

package com.szip.sportwatch.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.szip.sportwatch.Model.FaqModel;
import com.szip.sportwatch.R;

import java.util.ArrayList;

public class FaqAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<FaqModel> list = new ArrayList<>();


    public FaqAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setList(ArrayList<FaqModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size()+2;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position)==null?new FaqModel():list.get(position);
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
                    R.layout.adapter_faq, null, false);
            holder = new ViewHolder();
            holder.titleTv = convertView.findViewById(R.id.titleTv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position==list.size()+1)
            holder.titleTv.setText(mContext.getResources().getString(R.string.blePhone));
        else if (position == list.size())
            holder.titleTv.setText(mContext.getResources().getString(R.string.service));
        else
            holder.titleTv.setText(list.get(position).getTitle());
        return convertView;
    }

    private static class ViewHolder {
        TextView titleTv;
    }
}

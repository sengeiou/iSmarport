package com.szip.sportwatch.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.szip.sportwatch.Model.HttpBean.DialBean;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.View.CircularImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DialAdapter extends RecyclerView.Adapter<DialAdapter.Holder> {

    private int select = 0;
    private Context mContext;

    private ArrayList<DialBean.Dial> dialArrayList;
    private String faceType = "";


    public DialAdapter(ArrayList<DialBean.Dial> dialArrayList,Context context) {
        this.dialArrayList = dialArrayList;
        mContext = context;
        faceType = MyApplication.getInstance().getFaceType();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (faceType.indexOf("320*385")<0)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adaper_dail, null);
        else
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adaper_dail_06, null);
        final Holder holder = new Holder(view);
        //对加载的子项注册监听事件
        holder.fruitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select = holder.getAdapterPosition();
                onItemClickListener.onItemClick(holder.getAdapterPosition()==dialArrayList.size()?-1:holder.getAdapterPosition());
                notifyDataSetChanged();
            }
        });
        return holder;
    }


    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * 定义RecyclerView选项单击事件的回调接口
     */
    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        if (position==select){
            holder.selectView.setVisibility(View.VISIBLE);
        }else {
            holder.selectView.setVisibility(View.GONE);
        }

        if (position==dialArrayList.size()) {
            holder.fruitView.findViewById(R.id.diyRl).setVisibility(View.VISIBLE);
            holder.fruitView.findViewById(R.id.dailRl).setVisibility(View.GONE);
        } else {
            holder.fruitView.findViewById(R.id.diyRl).setVisibility(View.GONE);
            holder.fruitView.findViewById(R.id.dailRl).setVisibility(View.VISIBLE);
            Glide.with(mContext).load(dialArrayList.get(position).getPreviewUrl()).into(holder.imageView);
//            holder.imageView.setImageResource(dials[position]);
        }

    }

    @Override
    public int getItemCount() {
        return dialArrayList.size()+1;
    }


    class Holder extends RecyclerView.ViewHolder {
        private CircularImageView imageView;
        private View selectView;
        private View fruitView;  //表示我们自定义的控件的视图

        public Holder(View itemView) {
            super(itemView);
            fruitView = itemView;
            imageView = itemView.findViewById(R.id.imageView);
            selectView = itemView.findViewById(R.id.selectView);
        }
    }

}

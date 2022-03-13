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

public class DIYAdapter extends RecyclerView.Adapter<DIYAdapter.Holder>{

    private ArrayList<DialBean.Dial> dialArrayList = new ArrayList<>();
    private Context context;
    private int select = -1;

    public DIYAdapter(ArrayList<DialBean.Dial> dials,Context context) {
        this.context = context;
        this.dialArrayList = dials;
    }
    @NonNull
    @Override
    public DIYAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (!MyApplication.getInstance().getFaceType().equals("320*385"))
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adaper_diy, null);
        else
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adaper_diy_06, null);
        final DIYAdapter.Holder holder = new DIYAdapter.Holder(view);
        //对加载的子项注册监听事件
        holder.fruitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select = holder.getAdapterPosition();
                onItemClickListener.onItemClick(holder.getAdapterPosition());
                notifyDataSetChanged();
            }
        });
        return holder;
    }

    private DIYAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(DIYAdapter.OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * 定义RecyclerView选项单击事件的回调接口
     */
    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    @Override
    public void onBindViewHolder(@NonNull DIYAdapter.Holder holder, int position) {
        if (position==select){
            holder.selectView.setVisibility(View.VISIBLE);
        }else {
            holder.selectView.setVisibility(View.GONE);
        }
        Glide.with(context).load(dialArrayList.get(position).getPointerImg())
                .placeholder(R.mipmap.dial_default)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return dialArrayList.size();
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

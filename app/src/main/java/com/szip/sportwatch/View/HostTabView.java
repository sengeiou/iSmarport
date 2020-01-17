package com.szip.sportwatch.View;

import android.content.Context;
import android.graphics.Color;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.szip.sportwatch.R;

/**
 * Created by Administrator on 2019/12/1.
 */

public class HostTabView {

    private Context mContext;
    //正常情况下显示的图片
    private int imageNormal;
    //选中情况下显示的图片
    private int imagePress;
    //tab的名字
    private int title;
    private String titleString;

    //tab对应的fragment
    public Class<? extends Fragment> fragmentClass;

    public View view;
    public ImageView imageView;
    public TextView textView;

    public HostTabView(int imageNormal, int imagePress, int title, Class<? extends Fragment> fragmentClass, Context context) {
        this.imageNormal = imageNormal;
        this.imagePress = imagePress;
        this.title = title;
        this.fragmentClass =fragmentClass;
        this.mContext = context;
    }

    public Class<? extends  Fragment> getFragmentClass() {
        return fragmentClass;
    }
    public int getImageNormal() {
        return imageNormal;
    }

    public int getImagePress() {
        return imagePress;
    }

    public int getTitle() {
        return  title;
    }

    public String getTitleString() {
        if (title == 0) {
            return "";
        }
        if(TextUtils.isEmpty(titleString)) {
            titleString = mContext.getString(title);
        }
        return titleString;
    }

    public View getView() {
        if(this.view == null) {
            this.view = LayoutInflater.from(mContext).inflate(R.layout.tab_host_layout, null);
            this.imageView =  this.view.findViewById(R.id.tabIv);
            this.textView =  this.view.findViewById(R.id.tabTv);
            if(this.title == 0) {
                this.textView.setVisibility(View.GONE);
            } else {
                this.textView.setVisibility(View.VISIBLE);
                this.textView.setText(getTitleString());
            }
            this.imageView.setImageResource(imageNormal);
        }
        return this.view;
    }

    //切换tab的方法
    public void setChecked(boolean isChecked) {
        if(imageView != null) {
            if(isChecked) {
                imageView.setImageResource(imagePress);
                this.view.setBackground(mContext.getDrawable(R.drawable.bg_host_tab));
                textView.setTextColor(mContext.getResources().getColor(R.color.rayblue));
            }else {
                imageView.setImageResource(imageNormal);
                this.view.setBackgroundColor(Color.WHITE);
                textView.setTextColor(mContext.getResources().getColor(R.color.gray));
            }
        }


    }

}

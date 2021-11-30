package com.szip.sportwatch.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.szip.sportwatch.DB.dbModel.SportData;
import com.szip.sportwatch.MyApplication;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.DateUtil;
import com.szip.sportwatch.Util.MathUitl;

import java.util.List;
import java.util.Locale;

public class SportDataAdapter extends BaseAdapter {

    private Context mContext;
    private List<SportData> list;


    public SportDataAdapter(List<SportData> list, Context mContext) {
        this.list = list;
        this.mContext = mContext;
    }

    public void setList(List<SportData> list) {
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
                    R.layout.adapter_sport_list, null, false);
            holder = new ViewHolder();
            holder.timeTv = convertView.findViewById(R.id.timeTv);
            holder.dataTv = convertView.findViewById(R.id.dataTv);
            holder.buttonFirstTv = convertView.findViewById(R.id.buttonFirstTv);
            holder.typeIv = convertView.findViewById(R.id.typeIv);
            holder.buttonFirstUnitTv = convertView.findViewById(R.id.buttonFirstUnitTv);
            holder.buttonFirstIv = convertView.findViewById(R.id.buttonFirstIv);
            holder.locateIv = convertView.findViewById(R.id.locateIv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SportData sportData = list.get(position);

        holder.timeTv.setText(DateUtil.getStringDateFromSecond(sportData.time,"yyyy/MM/dd HH:mm:ss"));

        if (sportData.latArray!=null&&!sportData.latArray.equals("")){
            holder.locateIv.setVisibility(View.VISIBLE);
        }else {
            holder.locateIv.setVisibility(View.GONE);
        }
        holder.buttonFirstUnitTv.setVisibility(View.VISIBLE);
        holder.buttonFirstIv.setVisibility(View.VISIBLE);
        holder.buttonFirstTv.setVisibility(View.VISIBLE);
        switch (sportData.type){
            case 1:{//徒步
                holder.typeIv.setImageResource(R.mipmap.sport_list_icon_run);
                holder.dataTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",sportData.sportTime/3600,
                        sportData.sportTime%3600/60,sportData.sportTime%3600%60));
                holder.buttonFirstIv.setImageResource(R.mipmap.sport_list_icon_kcal);
                holder.buttonFirstTv.setText(String.format(Locale.ENGLISH,"%.1f",((sportData.calorie+55)/100)/10f));
            }
            break;
            case 2:{//跑步
                holder.typeIv.setImageResource(R.mipmap.sport_list_icon_outrun);
                holder.dataTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",sportData.sportTime/3600,
                        sportData.sportTime%3600/60,sportData.sportTime%3600%60));

                holder.buttonFirstIv.setImageResource(R.mipmap.sport_list_icon_kcal);
                holder.buttonFirstTv.setText(String.format(Locale.ENGLISH,"%.1f",((sportData.calorie+55)/100)/10f));
            }
            break;
            case 5:{//马拉松
                holder.typeIv.setImageResource(R.mipmap.sport_list_icon_marathon);
                holder.dataTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",sportData.sportTime/3600,
                        sportData.sportTime%3600/60,sportData.sportTime%3600%60));

                holder.buttonFirstIv.setImageResource(R.mipmap.sport_list_icon_kcal);
                holder.buttonFirstTv.setText(String.format(Locale.ENGLISH,"%.1f",((sportData.calorie+55)/100)/10f));
            }
            break;
            case 6:{
                holder.typeIv.setImageResource(R.mipmap.sport_list_icon_trainingrun);
                holder.dataTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",sportData.sportTime/3600,
                        sportData.sportTime%3600/60,sportData.sportTime%3600%60));

                holder.buttonFirstIv.setImageResource(R.mipmap.sport_list_icon_kcal);
                holder.buttonFirstTv.setText(String.format(Locale.ENGLISH,"%.1f",((sportData.calorie+55)/100)/10f));
            }
            break;
            case 7:
            case 3:{//室内跑步
                holder.typeIv.setImageResource(R.mipmap.sport_list_icon_treadmill);
                holder.dataTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",sportData.sportTime/3600,
                        sportData.sportTime%3600/60,sportData.sportTime%3600%60));
                holder.buttonFirstIv.setImageResource(R.mipmap.sport_list_icon_kcal);
                holder.buttonFirstTv.setText(String.format(Locale.ENGLISH,"%.1f",((sportData.calorie+55)/100)/10f));
            }
            break;
            case 4:{//登山
                holder.typeIv.setImageResource(R.mipmap.sport_list_icon_mountain);
                holder.dataTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",sportData.sportTime/3600,
                        sportData.sportTime%3600/60,sportData.sportTime%3600%60));

                holder.buttonFirstIv.setImageResource(R.mipmap.sport_list_icon_kcal);
                holder.buttonFirstTv.setText(String.format(Locale.ENGLISH,"%.1f",((sportData.calorie+55)/100)/10f));

            }
            break;
            case 8:{//跳绳

            }
            break;
            case 9:{//羽毛球
                holder.typeIv.setImageResource(R.mipmap.sport_list_icon_badminton);
                holder.dataTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",sportData.sportTime/3600,
                        sportData.sportTime%3600/60,sportData.sportTime%3600%60));
                holder.buttonFirstIv.setImageResource(R.mipmap.sport_list_icon_kcal);
                holder.buttonFirstTv.setText(String.format(Locale.ENGLISH,"%.1f",((sportData.calorie+55)/100)/10f));
            }
            break;
            case 10:{//篮球
                holder.typeIv.setImageResource(R.mipmap.sport_list_icon_basketball);
                holder.dataTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",sportData.sportTime/3600,
                        sportData.sportTime%3600/60,sportData.sportTime%3600%60));
                holder.buttonFirstIv.setImageResource(R.mipmap.sport_list_icon_kcal);
                holder.buttonFirstTv.setText(String.format(Locale.ENGLISH,"%.1f",((sportData.calorie+55)/100)/10f));
            }
            break;
            case 11:{//骑行
                holder.typeIv.setImageResource(R.mipmap.sport_list_icon_bike);
                holder.dataTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",sportData.sportTime/3600,
                            sportData.sportTime%3600/60,sportData.sportTime%3600%60));

                holder.buttonFirstIv.setImageResource(R.mipmap.sport_list_icon_kcal);
                holder.buttonFirstTv.setText(String.format(Locale.ENGLISH,"%.1f",((sportData.calorie+55)/100)/10f));
            }
            break;
            case 12:{//滑冰
                holder.typeIv.setImageResource(R.mipmap.sport_list_icon_skii);
                holder.dataTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",sportData.sportTime/3600,
                            sportData.sportTime%3600/60,sportData.sportTime%3600%60));

                holder.buttonFirstIv.setImageResource(R.mipmap.sport_list_icon_kcal);
                holder.buttonFirstTv.setText(String.format(Locale.ENGLISH,"%.1f",((sportData.calorie+55)/100)/10f));
            }
            break;
            case 13:{//健身房

            }
            break;
            case 14:{//瑜伽

            }
            break;
            case 15:{//网球

            }
            break;
            case 16:{//乒乓球
                holder.typeIv.setImageResource(R.mipmap.sport_list_icon_pingpong);
                holder.dataTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",sportData.sportTime/3600,
                        sportData.sportTime%3600/60,sportData.sportTime%3600%60));
                holder.buttonFirstIv.setImageResource(R.mipmap.sport_list_icon_kcal);
                holder.buttonFirstTv.setText(String.format(Locale.ENGLISH,"%.1f",((sportData.calorie+55)/100)/10f));
            }
            break;
            case 17:{//足球
                holder.typeIv.setImageResource(R.mipmap.sport_list_icon_football);
                holder.dataTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",sportData.sportTime/3600,
                        sportData.sportTime%3600/60,sportData.sportTime%3600%60));
                holder.buttonFirstIv.setImageResource(R.mipmap.sport_list_icon_kcal);
                holder.buttonFirstTv.setText(String.format(Locale.ENGLISH,"%.1f",((sportData.calorie+55)/100)/10f));
            }
            break;
            case 18:{//游泳
                holder.typeIv.setImageResource(R.mipmap.sport_list_icon_swim);
                holder.dataTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",sportData.sportTime/3600,
                            sportData.sportTime%3600/60,sportData.sportTime%3600%60));

                holder.buttonFirstIv.setImageResource(R.mipmap.sport_list_icon_kcal);
                holder.buttonFirstTv.setText(String.format(Locale.ENGLISH,"%.1f",((sportData.calorie+55)/100)/10f));
            }
            break;
            case 19:{//攀岩
                holder.typeIv.setImageResource(R.mipmap.sport_list_icon_climb);
                holder.dataTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",sportData.sportTime/3600,
                            sportData.sportTime%3600/60,sportData.sportTime%3600%60));

                holder.buttonFirstIv.setImageResource(R.mipmap.sport_list_icon_kcal);
                holder.buttonFirstTv.setText(String.format(Locale.ENGLISH,"%.1f",((sportData.calorie+55)/100)/10f));
            }
            break;
            case 20:{//划船
                holder.typeIv.setImageResource(R.mipmap.sport_list_icon_boat);
                holder.dataTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",sportData.sportTime/3600,
                            sportData.sportTime%3600/60,sportData.sportTime%3600%60));

                holder.buttonFirstIv.setImageResource(R.mipmap.sport_list_icon_kcal);
                holder.buttonFirstTv.setText(String.format(Locale.ENGLISH,"%.1f",((sportData.calorie+55)/100)/10f));
            }
            break;
            case 21:{//高尔夫
                holder.typeIv.setImageResource(R.mipmap.sport_icon_golf);
                holder.dataTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",sportData.sportTime/3600,
                        sportData.sportTime%3600/60,sportData.sportTime%3600%60));
                holder.buttonFirstIv.setImageResource(R.mipmap.sport_list_icon_kcal);
                holder.buttonFirstTv.setText(String.format(Locale.ENGLISH,"%.1f",((sportData.calorie+55)/100)/10f));
            }
            break;
            case 22:{//冲浪
                holder.typeIv.setImageResource(R.mipmap.sport_list_icon_surfing);
                holder.dataTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",sportData.sportTime/3600,
                            sportData.sportTime%3600/60,sportData.sportTime%3600%60));

                holder.buttonFirstIv.setImageResource(R.mipmap.sport_list_icon_kcal);
                holder.buttonFirstTv.setText(String.format(Locale.ENGLISH,"%.1f",((sportData.calorie+55)/100)/10f));
            }
            break;
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView timeTv,dataTv, buttonFirstTv,buttonFirstUnitTv;
        ImageView typeIv, buttonFirstIv,locateIv;
    }
}

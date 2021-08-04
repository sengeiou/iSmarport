package com.szip.sportwatch.Activity.help;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.szip.sportwatch.Fragment.BaseFragment;
import com.szip.sportwatch.R;

/**
 * Created by Administrator on 2019/12/22.
 */

public class BluetoochCallFragment extends BaseFragment {

    private int flag = 0;
    private TextView text,text1,guideText,guideText1;
    private ImageView imageView,guideImage;

    /**
     * 返回一个fragment实例，Activity中调用
     * */
    public static BluetoochCallFragment newInstance(int flag){
        Bundle bundle = new Bundle();
        bundle.putInt("flag",flag);
        BluetoochCallFragment fragment = new BluetoochCallFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_bluetooch_call;
    }

    @Override
    protected void afterOnCreated(Bundle savedInstanceState) {
        flag = getArguments().getInt("flag");
        text = getView().findViewById(R.id.text);
        text1 = getView().findViewById(R.id.text1);
        imageView = getView().findViewById(R.id.image);

        guideText = getView().findViewById(R.id.guideText);
        guideText1 = getView().findViewById(R.id.guideText1);
        guideImage = getView().findViewById(R.id.guideImage);

        if (flag == 0){
            getView().findViewById(R.id.bloodCallLl).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.guideLl).setVisibility(View.GONE);
            text.setText(getString(R.string.step1));
            text1.setText(getString(R.string.str1));
            if (getResources().getConfiguration().locale.getCountry().equals("CN")){
                imageView.setImageResource(R.mipmap.my_call_step1);
            }else{
                imageView.setImageResource(R.mipmap.my_call_step1_en);
            }
        }else if (flag==1){
            getView().findViewById(R.id.bloodCallLl).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.guideLl).setVisibility(View.GONE);
            text.setText(getString(R.string.step2));
            text1.setText(getString(R.string.str2));
            if (getResources().getConfiguration().locale.getCountry().equals("CN")){
                imageView.setImageResource(R.mipmap.my_call_step2);
            }else{
                imageView.setImageResource(R.mipmap.my_call_step2_en);
            }

        }else if (flag == 2){
            guideText.setText(getString(R.string.guideTitle1));
            guideText1.setText(getString(R.string.guide1));
            guideImage.setImageResource(R.mipmap.guidepage_1);
        }else if (flag == 3){
            guideText.setText(getString(R.string.guideTitle2));
            guideText1.setText(getString(R.string.guide2));
            guideImage.setImageResource(R.mipmap.guidepage_2);
        }else if (flag == 4){
            guideText.setText(getString(R.string.guideTitle3));
            guideText1.setText(getString(R.string.guide3));
            guideImage.setImageResource(R.mipmap.guidepage_3);
        }else if (flag == 5){
            guideText.setText(getString(R.string.guideTitle4));
            guideText1.setText(getString(R.string.guide4));
            guideImage.setImageResource(R.mipmap.guidepage_4);
        }
    }
}

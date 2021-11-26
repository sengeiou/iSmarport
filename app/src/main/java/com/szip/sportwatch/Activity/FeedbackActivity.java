package com.szip.sportwatch.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.szip.sportwatch.Model.HttpBean.BaseApi;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.HttpMessgeUtil;
import com.szip.sportwatch.Util.JsonGenericsSerializator;
import com.szip.sportwatch.Util.ProgressHudModel;
import com.szip.sportwatch.Util.StatusBarCompat;
import com.zhy.http.okhttp.callback.GenericsCallback;

import java.io.IOException;

import okhttp3.Call;

public class FeedbackActivity extends BaseActivity{

    private EditText feedbackEt;
    private TextView lenghtTv;
    private RelativeLayout feedbackRl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_feedback);
        StatusBarCompat.translucentStatusBar(this,true);
        setAndroidNativeLightStatusBar(this,true);
        initView();
        initEvent();

    }

    private void initView() {
        setTitleText(getString(R.string.feedback));

        feedbackEt = findViewById(R.id.feedbackEt);
        lenghtTv = findViewById(R.id.wordLenghtTv);
        feedbackRl = findViewById(R.id.feedbackRl);

    }

    private void initEvent() {
        findViewById(R.id.backIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.rightIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (feedbackEt.getText().toString().length()>300)
                    showToast(getString(R.string.tooLong));
                else if (feedbackEt.getText().toString().equals(""))
                    showToast(getString(R.string.empty));
                else
                    try {
                        ProgressHudModel.newInstance().show(FeedbackActivity.this,getString(R.string.waitting),
                                getString(R.string.httpError),5000);
                        HttpMessgeUtil.getInstance().postSendFeedback(feedbackEt.getText().toString(),callback);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        });

        feedbackEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int len = s.toString().length();
                lenghtTv.setText(String.format("%d/300",len));
                if (len>300)
                    lenghtTv.setTextColor(Color.RED);
                else
                    lenghtTv.setTextColor(getResources().getColor(R.color.black1));
            }
        });


        feedbackRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feedbackEt.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(feedbackEt, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private GenericsCallback<BaseApi> callback = new GenericsCallback<BaseApi>(new JsonGenericsSerializator()) {
        @Override
        public void onError(Call call, Exception e, int id) {

        }

        @Override
        public void onResponse(BaseApi response, int id) {
            ProgressHudModel.newInstance().diss();
            showToast(getString(R.string.send_success));
            finish();
        }
    };
}

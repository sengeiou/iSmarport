package com.szip.sportwatch.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.StatusBarCompat;

public class PrivacyActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_privacy);
        StatusBarCompat.translucentStatusBar(this,true);
        setAndroidNativeLightStatusBar(this,true);
        initView();
    }

    private void initView() {
        setTitleText(getString(R.string.privacy1));
        findViewById(R.id.rightIv).setVisibility(View.GONE);
        findViewById(R.id.backIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
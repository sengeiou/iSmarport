package com.szip.sportwatch.Activity.help;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.RadioGroup;

import com.szip.sportwatch.Activity.BaseActivity;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.StatusBarCompat;

public class ServicePrivacyActivity extends BaseActivity {

    private RadioGroup upRg,downRg;
    private WebView contentWeb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_service_privacy);
        StatusBarCompat.translucentStatusBar(this,true);
        setAndroidNativeLightStatusBar(this,true);
        initView();
        initEvent();
    }

    private void initEvent() {
        findViewById(R.id.backIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        upRg.setOnCheckedChangeListener(checkedChangeListener);
        downRg.setOnCheckedChangeListener(checkedChangeListener);
    }

    private RadioGroup.OnCheckedChangeListener checkedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId){
                case R.id.huaweiRb:
                    contentWeb.loadUrl("file:///android_asset/HUAWEI.html");
                    downRg.clearCheck();
                    break;
                case R.id.oppoRb:
                    contentWeb.loadUrl("file:///android_asset/oppo.html");
                    downRg.clearCheck();
                    break;
                case R.id.vivoRb:
                    contentWeb.loadUrl("file:///android_asset/vivo.html");
                    downRg.clearCheck();
                    break;
                case R.id.lenovoRb:
                    contentWeb.loadUrl("file:///android_asset/Lenovo.html");
                    downRg.clearCheck();
                    break;
                case R.id.sumsunRb:
                    contentWeb.loadUrl("file:///android_asset/Samsun.html");
                    upRg.clearCheck();
                    break;
                case R.id.miRb:
                    contentWeb.loadUrl("file:///android_asset/MI.html");
                    upRg.clearCheck();
                    break;
                case R.id.onePlusRb:
                    contentWeb.loadUrl("file:///android_asset/onePlus.html");
                    upRg.clearCheck();
                    break;
                case R.id.meizuRb:
                    contentWeb.loadUrl("file:///android_asset/MEIZU.html");
                    upRg.clearCheck();
                    break;
            }
            if (group.getCheckedRadioButtonId()!=checkedId)
                group.check(checkedId);
        }
    };

    private void initView() {
        findViewById(R.id.rightIv).setVisibility(View.GONE);
        setTitleText(getString(R.string.service));
        upRg = findViewById(R.id.upRg);
        downRg = findViewById(R.id.downRg);
        contentWeb = findViewById(R.id.contentWeb);
        contentWeb.loadUrl("file:///android_asset/HUAWEI.html");
        contentWeb.getSettings().setJavaScriptEnabled(true);
    }
}
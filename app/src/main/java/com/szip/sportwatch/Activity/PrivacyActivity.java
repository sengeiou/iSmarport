package com.szip.sportwatch.Activity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.StatusBarCompat;

public class PrivacyActivity extends BaseActivity {
    private WebView webView;

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
        webView = findViewById(R.id.webview);
        if(getResources().getConfiguration().locale.getLanguage().equals("zh"))
            webView.loadUrl("https://cloud.znsdkj.com:8443/file/contract/iSmarport/statement-zh.html");
        else
            webView.loadUrl("https://cloud.znsdkj.com:8443/file/contract/iSmarport/statement-en.html");
        webView.getSettings().setJavaScriptEnabled(true);
    }
}
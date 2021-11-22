package com.szip.sportwatch.Activity.initInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.szip.sportwatch.Activity.BaseActivity;
import com.szip.sportwatch.R;
import com.szip.sportwatch.Util.StatusBarCompat;

public class InitInfoActivity extends BaseActivity {
    private FragmentTransaction tx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_init_info);
        StatusBarCompat.translucentStatusBar(InitInfoActivity.this,true);
        setAndroidNativeLightStatusBar(this,true);
        initView();
    }

    private void initView() {
        UnitFragment unitFragment = new UnitFragment();
        tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.fragment, unitFragment, "unit");
        tx.addToBackStack(null);
        tx.commit();
    }

    public void nextPage() {
        UserFragment fragment = new UserFragment();
        tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.fragment, fragment, "user");
        tx.addToBackStack(null);
        tx.commit();
    }
}
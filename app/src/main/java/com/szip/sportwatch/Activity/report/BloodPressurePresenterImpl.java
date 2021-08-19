package com.szip.sportwatch.Activity.report;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.szip.sportwatch.Adapter.MyPagerAdapter;
import com.szip.sportwatch.Fragment.ReportFragment.bloodPressure.BloodPressureDayFragment;
import com.szip.sportwatch.Fragment.ReportFragment.bloodPressure.BloodPressureMonthFragment;
import com.szip.sportwatch.Fragment.ReportFragment.bloodPressure.BloodPressureWeekFragment;
import com.szip.sportwatch.Fragment.ReportFragment.bloodPressure.BloodPressureYearFragment;
import com.szip.sportwatch.R;

import java.util.ArrayList;

public class BloodPressurePresenterImpl implements ISportPresenter{
    private Context context;
    private ISportView iSportView;

    public BloodPressurePresenterImpl(Context context, ISportView iSportView) {
        this.context = context;
        this.iSportView = iSportView;
    }

    @Override
    public void getPageView(FragmentManager fragmentManager) {
        ArrayList<Fragment> fragments = new ArrayList<>();
        BloodPressureDayFragment dayFragment =  new BloodPressureDayFragment();
        BloodPressureWeekFragment weekFragment =  new BloodPressureWeekFragment();
        BloodPressureMonthFragment monthFragment =  new BloodPressureMonthFragment();
        BloodPressureYearFragment yearFragment =  new BloodPressureYearFragment();

        // 装填
        fragments.add(dayFragment);
        fragments.add(weekFragment);
        fragments.add(monthFragment);
        fragments.add(yearFragment);
        // 创建ViewPager适配器
        MyPagerAdapter myPagerAdapter = new MyPagerAdapter(fragmentManager);
        myPagerAdapter.setFragmentArrayList(fragments);
        if (iSportView!=null)
            iSportView.initPager(myPagerAdapter,context.getString(R.string.bloodPressureReport));
    }
    @Override
    public void setViewDestory() {
        iSportView = null;
    }
}

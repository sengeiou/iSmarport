package com.szip.sportwatch.Activity.report;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.szip.sportwatch.Adapter.MyPagerAdapter;
import com.szip.sportwatch.Fragment.ReportFragment.bloodOxygen.BloodOxygenDayFragment;
import com.szip.sportwatch.Fragment.ReportFragment.bloodOxygen.BloodOxygenMonthFragment;
import com.szip.sportwatch.Fragment.ReportFragment.bloodOxygen.BloodOxygenWeekFragment;
import com.szip.sportwatch.Fragment.ReportFragment.bloodOxygen.BloodOxygenYearFragment;
import com.szip.sportwatch.R;

import java.util.ArrayList;

public class BloodOxygenPresenterImpl implements ISportPresenter{
    private Context context;
    private ISportView iSportView;

    public BloodOxygenPresenterImpl(Context context, ISportView iSportView) {
        this.context = context;
        this.iSportView = iSportView;
    }

    @Override
    public void getPageView(FragmentManager fragmentManager) {
        ArrayList<Fragment> fragments = new ArrayList<>();
        BloodOxygenDayFragment dayFragment =  new BloodOxygenDayFragment();
        BloodOxygenWeekFragment weekFragment =  new BloodOxygenWeekFragment();
        BloodOxygenMonthFragment monthFragment =  new BloodOxygenMonthFragment();
        BloodOxygenYearFragment yearFragment =  new BloodOxygenYearFragment();

        // 装填
        fragments.add(dayFragment);
        fragments.add(weekFragment);
        fragments.add(monthFragment);
        fragments.add(yearFragment);
        // 创建ViewPager适配器
        MyPagerAdapter myPagerAdapter = new MyPagerAdapter(fragmentManager);
        myPagerAdapter.setFragmentArrayList(fragments);
        if (iSportView!=null)
            iSportView.initPager(myPagerAdapter,context.getString(R.string.bloodOxygenReport));
    }

    @Override
    public void setViewDestory() {
        iSportView = null;
    }
}

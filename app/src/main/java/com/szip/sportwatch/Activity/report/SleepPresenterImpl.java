package com.szip.sportwatch.Activity.report;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.szip.sportwatch.Adapter.MyPagerAdapter;
import com.szip.sportwatch.Fragment.ReportFragment.sleep.SleepDayFragment;
import com.szip.sportwatch.Fragment.ReportFragment.sleep.SleepMonthFragment;
import com.szip.sportwatch.Fragment.ReportFragment.sleep.SleepWeekFragment;
import com.szip.sportwatch.Fragment.ReportFragment.sleep.SleepYearFragment;
import com.szip.sportwatch.R;

import java.util.ArrayList;

public class SleepPresenterImpl implements ISportPresenter{
    private Context context;
    private ISportView iSportView;

    public SleepPresenterImpl(Context context, ISportView iSportView) {
        this.context = context;
        this.iSportView = iSportView;
    }

    @Override
    public void getPageView(FragmentManager fragmentManager) {
        ArrayList<Fragment> fragments = new ArrayList<>();
        SleepDayFragment dayFragment =  new SleepDayFragment();
        SleepWeekFragment weekFragment =  new SleepWeekFragment();
        SleepMonthFragment monthFragment =  new SleepMonthFragment();
        SleepYearFragment yearFragment =  new SleepYearFragment();

        // 装填
        fragments.add(dayFragment);
        fragments.add(weekFragment);
        fragments.add(monthFragment);
        fragments.add(yearFragment);
        // 创建ViewPager适配器
        MyPagerAdapter myPagerAdapter = new MyPagerAdapter(fragmentManager);
        myPagerAdapter.setFragmentArrayList(fragments);
        if (iSportView!=null)
            iSportView.initPager(myPagerAdapter,context.getString(R.string.sleepReport));
    }

    @Override
    public void setViewDestory() {
        iSportView = null;
    }
}

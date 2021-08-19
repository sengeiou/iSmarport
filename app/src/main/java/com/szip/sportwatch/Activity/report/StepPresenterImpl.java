package com.szip.sportwatch.Activity.report;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.szip.sportwatch.Adapter.MyPagerAdapter;
import com.szip.sportwatch.Fragment.ReportFragment.step.StepDayFragment;
import com.szip.sportwatch.Fragment.ReportFragment.step.StepMonthFragment;
import com.szip.sportwatch.Fragment.ReportFragment.step.StepWeekFragment;
import com.szip.sportwatch.Fragment.ReportFragment.step.StepYearFragment;
import com.szip.sportwatch.R;

import java.util.ArrayList;

public class StepPresenterImpl implements ISportPresenter{
    private Context context;
    private ISportView iSportView;

    public StepPresenterImpl(Context context, ISportView iSportView) {
        this.context = context;
        this.iSportView = iSportView;
    }

    @Override
    public void getPageView(FragmentManager fragmentManager) {
        ArrayList<Fragment> fragments = new ArrayList<>();
        // 创建一个集合,装填Fragment
        StepDayFragment dayFragment =  new StepDayFragment();
        StepWeekFragment weekFragment =  new StepWeekFragment();
        StepMonthFragment monthFragment =  new StepMonthFragment();
        StepYearFragment yearFragment =  new StepYearFragment();
        // 装填
        fragments.add(dayFragment);
        fragments.add(weekFragment);
        fragments.add(monthFragment);
        fragments.add(yearFragment);

        // 创建ViewPager适配器
        MyPagerAdapter myPagerAdapter = new MyPagerAdapter(fragmentManager);
        myPagerAdapter.setFragmentArrayList(fragments);
        if (iSportView!=null)
            iSportView.initPager(myPagerAdapter,context.getString(R.string.stepReport));
    }

    @Override
    public void setViewDestory() {
        iSportView = null;
    }
}

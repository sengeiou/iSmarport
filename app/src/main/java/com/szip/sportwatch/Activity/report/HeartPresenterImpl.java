package com.szip.sportwatch.Activity.report;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.szip.sportwatch.Adapter.MyPagerAdapter;
import com.szip.sportwatch.Fragment.ReportFragment.heart.HeartDayFragment;
import com.szip.sportwatch.Fragment.ReportFragment.heart.HeartMonthFragment;
import com.szip.sportwatch.Fragment.ReportFragment.heart.HeartWeekFragment;
import com.szip.sportwatch.Fragment.ReportFragment.heart.HeartYearFragment;
import com.szip.sportwatch.R;

import java.util.ArrayList;

public class HeartPresenterImpl implements ISportPresenter{
    private Context context;
    private ISportView iSportView;

    public HeartPresenterImpl(Context context, ISportView iSportView) {
        this.context = context;
        this.iSportView = iSportView;
    }

    @Override
    public void getPageView(FragmentManager fragmentManager) {
        ArrayList<Fragment> fragments = new ArrayList<>();
        HeartDayFragment dayFragment =  new HeartDayFragment();
        HeartWeekFragment weekFragment =  new HeartWeekFragment();
        HeartMonthFragment monthFragment =  new HeartMonthFragment();
        HeartYearFragment yearFragment =  new HeartYearFragment();

        // 装填
        fragments.add(dayFragment);
        fragments.add(weekFragment);
        fragments.add(monthFragment);
        fragments.add(yearFragment);
        // 创建ViewPager适配器
        MyPagerAdapter myPagerAdapter = new MyPagerAdapter(fragmentManager);
        myPagerAdapter.setFragmentArrayList(fragments);
        if (iSportView!=null)
            iSportView.initPager(myPagerAdapter,context.getString(R.string.heartReport));
    }

    @Override
    public void setViewDestory() {
        iSportView = null;
    }
}

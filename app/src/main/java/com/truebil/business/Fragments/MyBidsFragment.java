package com.truebil.business.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.truebil.business.Activities.BiddingActivity;
import com.truebil.business.Constants;
import com.truebil.business.Fragments.CarListingsFragment;
import com.truebil.business.Helper;
import com.truebil.business.R;

import java.util.List;

public class MyBidsFragment extends Fragment{

    public MyBidsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_bids, container, false);

        CustomPagerAdapter mCustomPagerAdapter = new CustomPagerAdapter(getChildFragmentManager());
        ViewPager view_pager = view.findViewById(R.id.view_pager);
        view_pager.setAdapter(mCustomPagerAdapter);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(view_pager);
        Helper.wrapTabIndicatorToTitle(tabLayout, Constants.Config.TAB_LAYOUT_EXTERNAL_MARGIN, Constants.Config.TAB_LAYOUT_EXTERNAL_MARGIN);

        return view;
    }

    class CustomPagerAdapter extends FragmentPagerAdapter {

        private CustomPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
           Bundle bundle = new Bundle();
            Fragment fragment = new CarListingsFragment();
            switch(position){
                case 0:
                    bundle.putString(Constants.Keys.FRAGMENT, Constants.Keys.MyBidsLosing);
                    break;
                case 1:
                    bundle.putString(Constants.Keys.FRAGMENT, Constants.Keys.MyBidsWinning);
                    break;
                case 2:
                    bundle.putString(Constants.Keys.FRAGMENT, Constants.Keys.MyBidsHistory);
                    break;
                default:
                    return null;
            }
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = "";

            switch(position){
                case 0:
                    title = "Losing";
                    break;
                case 1:
                    title = "Winning";
                    break;
                case 2:
                    title = "History";
                    break;
            }
            return title;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        List<Fragment> fragments = BiddingActivity.fragmentManager.getFragments();
        if (fragments != null) {
            FragmentTransaction ft = BiddingActivity.fragmentManager.beginTransaction();
            for (Fragment f : fragments) {
                //You can perform additional ic_check to remove some (not all) fragments:
                if ((f instanceof CarListingsFragment)) {
                    ft.remove(f);
                }
            }
            ft.commitAllowingStateLoss();
        }
    }

}

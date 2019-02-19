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

public class ProcuredFragment extends Fragment{

    public ProcuredFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_procured, container, false);

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

            Fragment fragment = new CarListingsFragment() ;
            Bundle bundle = new Bundle();
            switch(position){
                case 0:
                    bundle.putString(Constants.Keys.FRAGMENT, Constants.Keys.Negotiating);
                    break;
                case 1:
                    bundle.putString(Constants.Keys.FRAGMENT, Constants.Keys.Procured);
                    break;
                case 2:
                    bundle.putString(Constants.Keys.FRAGMENT, Constants.Keys.DealCancelled);
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
            String title;

            switch(position){
                case 0:
                    title = "Negotiating";
                    break;
                case 1:
                    title = "Procured";
                    break;
                case 2:
                    title = "Deal Cancelled";
                    break;
                default:
                    return "";
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
                if ((f instanceof CarListingsFragment)) {
                    ft.remove(f);
                }
            }
            ft.commitAllowingStateLoss();
        }
    }
}
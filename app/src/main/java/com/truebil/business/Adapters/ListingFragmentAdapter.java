package com.truebil.business.Adapters;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.truebil.business.CustomLayouts.WrapContentViewPager;
import com.truebil.business.Fragments.InspectionReportFragment;
import com.truebil.business.Fragments.ListingOverviewFragment;

import org.json.JSONArray;
import org.json.JSONObject;

public class ListingFragmentAdapter extends FragmentStatePagerAdapter {

    private JSONArray featureJSONArray, cargoNegativeComments, instaveritasVerificationDetails;
    private JSONObject overview, inspectionSummary, inspectionReport;
    private int mCurrentPosition = -1;

    public ListingFragmentAdapter(FragmentManager fm,
                                  JSONObject overview,
                                  JSONObject inspectionSummary,
                                  JSONArray featureJSONArray,
                                  JSONArray cargoNegativeComments,
                                  JSONObject inspectionReport,
                                  JSONArray instaveritasVerificationDetails) {
        super(fm);
        this.featureJSONArray = featureJSONArray;
        this.overview = overview;
        this.inspectionSummary = inspectionSummary;
        this.cargoNegativeComments = cargoNegativeComments;
        this.inspectionReport = inspectionReport;
        this.instaveritasVerificationDetails = instaveritasVerificationDetails;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                Bundle bundle = new Bundle();
                bundle.putString("OverviewInfo", overview.toString());
                bundle.putString("SummaryInfo", inspectionSummary.toString());
                bundle.putString("FeaturesInfo", featureJSONArray.toString());
                bundle.putString("CargoNegativeComments", cargoNegativeComments.toString());
                bundle.putString("InstaveritasVerificationDetails", instaveritasVerificationDetails.toString());

                ListingOverviewFragment listingOverviewFragment = new ListingOverviewFragment();
                listingOverviewFragment.setArguments(bundle);
                return listingOverviewFragment;

            case 1:
                bundle = new Bundle();
                bundle.putString("ReportInfo", inspectionReport.toString());
                InspectionReportFragment inspectionReportFragment = new InspectionReportFragment();
                inspectionReportFragment.setArguments(bundle);
                return inspectionReportFragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Overview";
            case 1:
                return "Inspection Report";
            default:
                return "";
        }
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        if (position != mCurrentPosition) {
            Fragment fragment = (Fragment) object;
            WrapContentViewPager pager = (WrapContentViewPager) container;
            if (fragment != null && fragment.getView() != null) {
                mCurrentPosition = position;
                pager.measureCurrentView(fragment.getView());
            }
        }
    }
}

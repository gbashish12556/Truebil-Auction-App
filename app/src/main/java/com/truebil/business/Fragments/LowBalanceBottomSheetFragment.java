package com.truebil.business.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.truebil.business.Activities.ListingActivity;
import com.truebil.business.Constants;
import com.truebil.business.Helper;
import com.truebil.business.R;

public class LowBalanceBottomSheetFragment extends Fragment {

    LowBalanceBottomSheetInterface mCallback;

    public LowBalanceBottomSheetFragment() {
    }

    public interface LowBalanceBottomSheetInterface {
        void onLowBalanceBottomSheetClosed();
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (LowBalanceBottomSheetInterface)context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement LowBalanceBottomSheetInterface");
        }
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_low_balance_bottom_sheet, container, false);

        ImageButton closeImageButton = rootView.findViewById(R.id.fragment_low_balance_close_image_button);
        Button callSalesButton = rootView.findViewById(R.id.fragment_low_balance_call_sales_button);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        final String salesPersonMobileNo = sharedPref.getString(Constants.SharedPref.SALES_PERSON_MOBILE,Constants.Config.TRUEBIL_DEFAULT_NO);

        callSalesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + salesPersonMobileNo));
                startActivity(callIntent);
            }
        });

        closeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onLowBalanceBottomSheetClosed();
            }
        });

        return rootView;
    }
}
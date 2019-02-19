package com.truebil.business.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.truebil.business.Activities.TruebilWebViewActivity;
import com.truebil.business.Constants;
import com.truebil.business.R;
import com.truebil.business.Utils.EventAnalytics;

public class VerificationSuccessFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_verification_success, container, false);

        final TextView mobileTextView = rootView.findViewById(R.id.fragment_verification_success_sales_person_mobile_text_view);
        ImageButton callSalesPersonButton = rootView.findViewById(R.id.fragment_verification_success_call_sales_person_image_button);
        Button webViewButton = rootView.findViewById(R.id.fragment_verification_success_webview_button);

        if (getArguments() != null) {
            String salesPersonMobileNumber = getArguments().getString(Constants.Keys.SALES_PERSON_MOBILE);
            String salesPersonName = getArguments().getString(Constants.Keys.SALES_PERSON_NAME);

            mobileTextView.setText(salesPersonMobileNumber);
        }

        final SharedPreferences sharedPref = getActivity().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        final String dealerMobile =  sharedPref.getString(Constants.SharedPref.DEALER_MOBILE,"");
        callSalesPersonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventAnalytics.getInstance(getActivity()).logEvent("Login", "clicked_support_number", dealerMobile, 0);
                String uri = "tel:02262459799" ;
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            }
        });

        webViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), TruebilWebViewActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
    }
}

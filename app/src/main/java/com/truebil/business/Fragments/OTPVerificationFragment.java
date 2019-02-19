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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.truebil.business.Constants;
import com.truebil.business.Helper;
import com.truebil.business.Network.VolleyService;
import com.truebil.business.R;
import com.truebil.business.Utils.EventAnalytics;

import org.json.JSONException;
import org.json.JSONObject;

public class OTPVerificationFragment extends Fragment {

    String mobileNumber, name;
    private final static String TAG = "OTPVerificationFragment";
    TextView wrongOTPTextView;
    OTPVerificationFragmentListener mCallback;
    ProgressBar loaderProgressBar;
    private RequestQueue queue;
    private TextView verifyOTPTextView;

    public interface OTPVerificationFragmentListener {
        void onVerifyOTPSuccess(boolean isVerifiedFromAdmin, String salesPersonName, String salesPersonMobile);
        void onEditMobileButtonClicked();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (OTPVerificationFragmentListener) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OTPVerificationFragmentListener");
        }
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_otp_verification, container, false);
        Helper.setupKeyboardHidingUI(rootView, getActivity());

        if (getArguments() != null) {
            mobileNumber = getArguments().getString(Constants.Keys.MOBILE);
            name = getArguments().getString(Constants.Keys.USERNAME);
        }

        TextView mobileNumberTextView = rootView.findViewById(R.id.fragment_otp_verification_phone_number_textview);
        mobileNumberTextView.setText(mobileNumber);

        TextView editMobileTextView = rootView.findViewById(R.id.fragment_otp_verification_edit_phone_textview);
        editMobileTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onEditMobileButtonClicked(); //Basically restart the LoginModalFragment
            }
        });

        final EditText otpEditText = rootView.findViewById(R.id.fragment_otp_verification_otp_edittext);
        TextView resendTextView = rootView.findViewById(R.id.fragment_otp_verification_resend_textview);
        resendTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide during retry
                wrongOTPTextView.setVisibility(View.GONE);

                // Call resend mobile otp api again.
                requestMobileOTP(mobileNumber);
            }
        });

        wrongOTPTextView = rootView.findViewById(R.id.fragment_otp_verification_wrong_otp_textview);
        wrongOTPTextView.setVisibility(View.GONE);

        loaderProgressBar = rootView.findViewById(R.id.fragment_otp_verification_progress_bar);
        loaderProgressBar.setVisibility(View.GONE);

        verifyOTPTextView = rootView.findViewById(R.id.fragment_otp_verification_verify_otp_text_view);

        verifyOTPTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventAnalytics.getInstance(getActivity()).logEvent("Login", "clicked_verify_otp", mobileNumber, 0);
                String otp = otpEditText.getText().toString();
                Helper.hideSoftKeyboard(getActivity());
                loaderProgressBar.setVisibility(View.VISIBLE);
                verifyMobileOTP(mobileNumber, name, otp);
                verifyOTPTextView.setEnabled(false);

                // Hide during retry
                wrongOTPTextView.setVisibility(View.GONE);
            }
        });

        // Call button
        ImageButton callSalesPersonButton = rootView.findViewById(R.id.fragment_otp_verification_call_truebil_image_button);
        callSalesPersonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "tel:02262459799";
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            }
        });

        return rootView;
    }

    void verifyMobileOTP(String mobileNumber, String name, String otp) {

        if (getContext() == null) {
            wrongOTPTextView.setText("Please try again");
            wrongOTPTextView.setVisibility(View.VISIBLE);
            verifyOTPTextView.setEnabled(true);
            return;
        }

        if (queue == null)
            queue = Volley.newRequestQueue(getContext());

        String url = Constants.Config.API_PATH + "/verify_otp/";

        final JSONObject apiParams = new JSONObject();
        try {
            apiParams.put("mobile", mobileNumber);
            apiParams.put("otp", otp);
            apiParams.put("name", name);
            JSONObject utmJson = new JSONObject(Constants.Config.UTM_PARAMS);
            apiParams.put("utm", utmJson);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(url, apiParams,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    parseResponse(response);
                    verifyOTPTextView.setEnabled(true);
                    loaderProgressBar.setVisibility(View.GONE);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        apiParams.put("API", "/verify_otp");
                        String errorMessage = VolleyService.handleVolleyError(error, apiParams, false, getContext());

                        wrongOTPTextView.setVisibility(View.VISIBLE);
                        wrongOTPTextView.setText(errorMessage);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    verifyOTPTextView.setEnabled(true);
                    loaderProgressBar.setVisibility(View.GONE);
                }
            }
        );

        jsonRequest.setTag(TAG);
        queue.add(jsonRequest);
    }

    /* {"mobile":9666706065, "otp":9319} */
    void parseResponse(JSONObject apiResponse) {
        try {
            boolean status = apiResponse.getBoolean("status");
            String message = apiResponse.getString("message");

            if (status) {

                JSONObject salesPerson = apiResponse.getJSONObject("sales_person");
                String salesPersonName = salesPerson.getString("Name");
                String salesPersonMobile = salesPerson.getString("Mobile");
                salesPersonMobile = "02262459799";

                JSONObject dealerInfo = apiResponse.getJSONObject("dealer_info");
                int dealerId = dealerInfo.getInt("dealer_id");
                String dealerName = dealerInfo.getString("name");
                String dealerMobile = dealerInfo.getString("mobile");
                boolean isVerifiedFromAdmin = dealerInfo.getBoolean("is_verified_from_admin");

                String token = apiResponse.getString("token");

                // To resolve the bug: getSharedPreferences() may produce NullPointerException
                if (getActivity() == null) {
                    wrongOTPTextView.setText("Please try again");
                    wrongOTPTextView.setVisibility(View.VISIBLE);
                    return;
                }

                //Save info in shared preferences
                SharedPreferences sharedPref = getActivity().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(Constants.SharedPref.JWT_TOKEN, token);
                editor.putInt(Constants.SharedPref.DEALER_ID, dealerId);
                editor.putString(Constants.SharedPref.DEALER_MOBILE, dealerMobile);
                editor.putBoolean(Constants.SharedPref.IS_VERIFIED_FROM_ADMIN, isVerifiedFromAdmin); //Set this for later checks.
                editor.putBoolean(Constants.SharedPref.HAS_LOGGED_IN_BEFORE, true); //Set this for later checks.
                editor.putString(Constants.SharedPref.SALES_PERSON_NAME,salesPersonName);
                editor.putString(Constants.SharedPref.SALES_PERSON_MOBILE,salesPersonMobile);
                editor.apply();

                // Add the dealer to crashlytics
                Crashlytics.setUserIdentifier(String.valueOf(dealerId));

                mCallback.onVerifyOTPSuccess(isVerifiedFromAdmin, salesPersonName, salesPersonMobile);
            }
            else {
                wrongOTPTextView.setVisibility(View.VISIBLE);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void requestMobileOTP(final String mobileNumber) {

        if (getContext() == null) {
            wrongOTPTextView.setText("Please try again");
            wrongOTPTextView.setVisibility(View.VISIBLE);
            return;
        }

        if (queue == null)
            queue = Volley.newRequestQueue(getContext());

        String url = Constants.Config.API_PATH + "/generate_otp/";

        final JSONObject apiParams = new JSONObject();
        try {
            apiParams.put("mobile", mobileNumber);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(url, apiParams,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        apiParams.put("API", "/generate_otp");
                        VolleyService.handleVolleyError(error, apiParams, true, getContext());
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        );

        jsonRequest.setTag(TAG);
        queue.add(jsonRequest);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (queue != null)
            queue.cancelAll(TAG);
    }
}

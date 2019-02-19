package com.truebil.business.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.truebil.business.Constants;
import com.truebil.business.Network.VolleyService;
import com.truebil.business.R;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class UserAccountFragment extends Fragment {

    private static final String TAG = "UserAccountFragment";
    private SharedPreferences sharedPref;
    private TextView userNameTextView, userMobileTextView, bankAccountTextView, ifscTextView, beneficiaryNameTextView, salesPersonNameTextView, salesPersonMobileTextView;
    private ProgressBar loaderProgressBar;
    private LinearLayout superLinearLayout;

    public UserAccountFragment() {
    }

    OnUserAccountFragmentClickListeners mCallback;

    public interface OnUserAccountFragmentClickListeners {
        void onGoToMoreMenuButtonClicked();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (OnUserAccountFragmentClickListeners) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnUserAccountFragmentClickListeners");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_account, container, false);
        sharedPref = getContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

        superLinearLayout = rootView.findViewById(R.id.fragment_user_account_super_linear_layout);
        userNameTextView = rootView.findViewById(R.id.fragment_user_account_user_name_text_view);
        userMobileTextView = rootView.findViewById(R.id.fragment_user_account_user_mobile_text_view);
        bankAccountTextView = rootView.findViewById(R.id.fragment_user_account_bank_account_text_view);
        ifscTextView = rootView.findViewById(R.id.fragment_user_account_ifsc_code_text_view);
        beneficiaryNameTextView = rootView.findViewById(R.id.fragment_user_account_beneficiary_name_text_view);
        salesPersonNameTextView = rootView.findViewById(R.id.fragment_user_account_sales_person_name_text_view);
        salesPersonMobileTextView = rootView.findViewById(R.id.fragment_user_account_sales_person_mobile_text_view);
        loaderProgressBar = rootView.findViewById(R.id.fragment_user_account_progress_bar);
        superLinearLayout.setVisibility(View.GONE);
        loaderProgressBar.setVisibility(View.VISIBLE);

        ImageButton backImageButton = rootView.findViewById(R.id.fragment_user_account_back_image_button);
        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onGoToMoreMenuButtonClicked();
            }
        });

        ImageButton callSalesImageButton = rootView.findViewById(R.id.fragment_user_account_call_sales_person_image_button);
        callSalesImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "tel:" + salesPersonMobileTextView.getText() ;
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            }
        });

        return rootView;
    }

    void updateTextViews(AccountDetails accountDetails) {
        userNameTextView.setText(accountDetails.getUserName());
        userMobileTextView.setText(String.format("Phone: %.0f", accountDetails.getUserMobile()));
        bankAccountTextView.setText(accountDetails.getBankAccountNumber());
        ifscTextView.setText(accountDetails.getIFSCCode());
        beneficiaryNameTextView.setText(accountDetails.getBeneficiaryName());
        salesPersonNameTextView.setText(accountDetails.getSalesPersonName() + " (Sales Person)");
        salesPersonMobileTextView.setText(accountDetails.getSalesPersonMobile());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fetchAccountDetails();
    }

    void fetchAccountDetails() {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = Constants.Config.API_PATH + "/dealer_status/";

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject apiResponse) {

                        AccountDetails accountDetails = new AccountDetails(apiResponse);
                        updateTextViews(accountDetails);
                        loaderProgressBar.setVisibility(View.GONE);
                        superLinearLayout.setVisibility(View.VISIBLE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        JSONObject additionalLogJson = new JSONObject();
                        try {
                            additionalLogJson.put("API", "/dealer_status");
                            VolleyService.handleVolleyError(error, additionalLogJson, true, getContext());
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                        loaderProgressBar.setVisibility(View.GONE);
                        superLinearLayout.setVisibility(View.VISIBLE);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                String dealerJWTToken = sharedPref.getString(Constants.SharedPref.JWT_TOKEN, "");
                headers.put("Authorization", "jwt " + dealerJWTToken);
                return headers;
            }
        };
        queue.add(jsonRequest);
    }

    class AccountDetails {

        String userName, bankAccountNumber, IFSCCode, beneficiaryName, salesPersonName, salesPersonMobile;
        Double userMobile;

        AccountDetails(JSONObject response) {

            try {
                Boolean status = response.getBoolean("status");
                if (!status)
                    return;

                JSONObject details = response.getJSONObject("details");
                userName = details.getString("name");
                userMobile = details.getDouble("mobile");

                JSONObject bankDetails = details.getJSONObject("truebil_bank_detail");
                bankAccountNumber = bankDetails.getString("acc_no");
                IFSCCode = bankDetails.getString("ifsc");
                beneficiaryName = bankDetails.getString("beneficiary_name");

                JSONObject salesDetails = details.getJSONObject("sales_rep_info");
                salesPersonName = salesDetails.getString("Name");
                salesPersonMobile = salesDetails.getString("Mobile");
                salesPersonMobile = "02262459799";
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

        }

        private String getUserName() {
            return userName;
        }

        private String getBankAccountNumber() {
            return bankAccountNumber;
        }

        private String getIFSCCode() {
            return IFSCCode;
        }

        private String getBeneficiaryName() {
            return beneficiaryName;
        }

        private String getSalesPersonName() {
            return salesPersonName;
        }

        private String getSalesPersonMobile() {
            return salesPersonMobile;
        }

        private Double getUserMobile() {
            return userMobile;
        }
    }


}

package com.truebil.business.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.truebil.business.Activities.BiddingActivity;
import com.truebil.business.Activities.CitiesAndRtosActivity;
import com.truebil.business.BuildConfig;
import com.truebil.business.Constants;
import com.truebil.business.Network.VolleyService;
import com.truebil.business.R;
import com.truebil.business.Utils.EventAnalytics;

import org.json.JSONException;
import org.json.JSONObject;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MoreMenuFragment extends Fragment {

    private static final String TAG = "MoreMenuFragment";
    private SharedPreferences sharedPref;
    private TextView userNameTextView, walletBalanceTextView, callTruebilTextView, showProfileTextView, remainingBidsTextView;
    private String smsNumber = "919619022022";
    private ProgressBar loaderProgressBar;
    private ScrollView superScrollView;

    public MoreMenuFragment() {
    }

    OnMoreMenuFragmentClickListeners mCallback;

    public interface OnMoreMenuFragmentClickListeners {
        void onShowProfileClicked();
        void onAllTransactionsClicked();
        void onFAQsClicked();
        void onTermsConditionsClicked();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (OnMoreMenuFragmentClickListeners) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnMoreMenuFragmentClickListeners");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_more_menu, container, false);
        sharedPref = getContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

        superScrollView = rootView.findViewById(R.id.fragment_more_menu_super_scroll_view);

        userNameTextView = rootView.findViewById(R.id.fragment_more_menu_profile_name_text_view);
        //userNameTextView.setText("Hi");

        walletBalanceTextView = rootView.findViewById(R.id.fragment_more_menu_wallet_balance_text_view);
        //walletBalanceTextView.setText("Rs.0");

        showProfileTextView = rootView.findViewById(R.id.fragment_more_menu_show_profile_text_view);
        showProfileTextView.setVisibility(View.INVISIBLE);
        final String dealerId = String.valueOf(sharedPref.getInt(Constants.SharedPref.DEALER_ID,-1));
        showProfileTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventAnalytics.getInstance(getActivity()).logEvent("More", "clicked_show_profile", dealerId, 0);
                mCallback.onShowProfileClicked();
            }
        });

        remainingBidsTextView = rootView.findViewById(R.id.fragment_more_menu_remaining_bids_text_view);

        TextView allTransactionsTextView = rootView.findViewById(R.id.fragment_more_menu_all_transactions_text_view);
        allTransactionsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventAnalytics.getInstance(getActivity()).logEvent("More", "clicked_all_transaction", dealerId, 0);
                mCallback.onAllTransactionsClicked();
            }
        });

        RelativeLayout citiesRTORelativeLayout = rootView.findViewById(R.id.fragment_more_menu_select_cities_rtos_relative_layout);
        citiesRTORelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventAnalytics.getInstance(getActivity()).logEvent("More", "clicked_select_cities_rto", dealerId, 0);
                Intent intent = new Intent(getActivity(), CitiesAndRtosActivity.class);
                startActivity(intent);
            }
        });

        /*
        RelativeLayout faqsRelativeLayout = rootView.findViewById(R.id.fragment_more_menu_faq_relative_layout);
        faqsRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onFAQsClicked();
            }
        });

        RelativeLayout termsConditionsRelativeLayout = rootView.findViewById(R.id.fragment_more_menu_terms_conditions_relative_layout);
        termsConditionsRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onTermsConditionsClicked();
            }
        });
        */

        RelativeLayout whatsappChatRelativeLayout = rootView.findViewById(R.id.fragment_more_menu_whatsapp_chat_relative_layout);
        whatsappChatRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventAnalytics.getInstance(getActivity()).logEvent("More", "clicked_whatsapp", dealerId, 0);
                PackageManager packageManager = getContext().getPackageManager();
                Intent i = new Intent(Intent.ACTION_VIEW);

                try {
                    String url = "https://api.whatsapp.com/send?phone=" + smsNumber + "&text=" + URLEncoder.encode("Hi", "UTF-8");
                    i.setPackage("com.whatsapp");
                    i.setData(Uri.parse(url));
                    if (i.resolveActivity(packageManager) != null) {
                        getContext().startActivity(i);
                    }
                    else {
                        Toast.makeText(getContext(), "Whatsapp is not installed", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        RelativeLayout callTruebilRelativeLayout = rootView.findViewById(R.id.fragment_more_menu_call_truebil_relative_layout);
        callTruebilRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventAnalytics.getInstance(getActivity()).logEvent("More", "clicked_support_number", dealerId, 0);
                String uri = "tel:02262459799" ;
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            }
        });

        callTruebilTextView = rootView.findViewById(R.id.fragment_more_menu_call_truebil_text_view);
        callTruebilTextView.setText("Call Truebil");
        loaderProgressBar = rootView.findViewById(R.id.fragment_more_menu_progress_bar);
        TextView appVersionTextView = rootView.findViewById(R.id.fragment_more_menu_app_version_text_view);

        appVersionTextView.setText("App Build: v" + BuildConfig.VERSION_CODE);
        loaderProgressBar.setVisibility(View.VISIBLE);
        superScrollView.setVisibility(View.GONE);

        return rootView;
    }

    void setWalletBalance(Double balance) {
        walletBalanceTextView.setText(String.format(Locale.US, "Rs. %.0f", balance));
        remainingBidsTextView.setText(String.format(Locale.US, "%.0f Bids Remaining", balance/5000));
    }

    void setShowMoreDetails(String userName, String truebilSalesPersonMobile) {
        userNameTextView.setText("Hi " + userName);
        callTruebilTextView.setText("Call Truebil: " + truebilSalesPersonMobile);
        showProfileTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loaderProgressBar.setVisibility(View.VISIBLE);
        fetchDealerInfo();
        fetchWalletDetails();
    }

    void fetchDealerInfo() {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = Constants.Config.API_PATH + "/dealer_status/";

        if (BiddingActivity.mIdlingResource != null) {
            BiddingActivity.mIdlingResource.setIdleState(false);
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject apiResponse) {

                        try {
                            JSONObject details = apiResponse.getJSONObject("details");
                            String name = details.getString("name");
                            String salesPersonName = details.getJSONObject("sales_rep_info").getString("Name");
                            String salesPersonMobile = details.getJSONObject("sales_rep_info").getString("Mobile");
                            salesPersonMobile = "02262459799";
                            setShowMoreDetails(name, salesPersonMobile);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                        loaderProgressBar.setVisibility(View.GONE);
                        superScrollView.setVisibility(View.VISIBLE);

                        if (BiddingActivity.mIdlingResource != null) {
                            BiddingActivity.mIdlingResource.setIdleState(true);
                        }
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
                        superScrollView.setVisibility(View.VISIBLE);

                        if (BiddingActivity.mIdlingResource != null) {
                            BiddingActivity.mIdlingResource.setIdleState(true);
                        }
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

    void fetchWalletDetails() {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = Constants.Config.API_PATH + "/my_account/";

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject apiResponse) {

                        try {
                            Double walletBalance = apiResponse.getJSONObject("details").getJSONObject("current_balance").getDouble("value");
                            setWalletBalance(walletBalance);
                            loaderProgressBar.setVisibility(View.GONE);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        JSONObject additionalLogJson = new JSONObject();
                        try {
                            additionalLogJson.put("API", "/my_account");
                            VolleyService.handleVolleyError(error, additionalLogJson, true, getContext());
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
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
}

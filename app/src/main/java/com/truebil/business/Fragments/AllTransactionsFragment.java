package com.truebil.business.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.truebil.business.Constants;
import com.truebil.business.Helper;
import com.truebil.business.Network.VolleyService;
import com.truebil.business.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AllTransactionsFragment extends Fragment {

    public AllTransactionsFragment() {
    }

    private static final int NUM_TRANSACTION_PAGES = 3;
    private static final String TAG = "AllTransactionsFragment";
    private SharedPreferences sharedPref;
    private ViewPager transactionsViewPager;
    private TabLayout tabLayout;
    private RequestQueue queue;
    TextView walletBalanceTextView, bidsRemainingTextView;

    OnAllTransactionsFragmentClickListeners mCallback;

    public interface OnAllTransactionsFragmentClickListeners {
        void onGoToMoreMenuButtonClicked();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (OnAllTransactionsFragmentClickListeners) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnAllTransactionsFragmentClickListeners");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_all_transaction, container, false);
        sharedPref = getContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

        transactionsViewPager = rootView.findViewById(R.id.all_transaction_fragment_view_pager);
        tabLayout = rootView.findViewById(R.id.all_transactions_fragment_tab_layout);
        tabLayout.setupWithViewPager(transactionsViewPager);

        walletBalanceTextView = rootView.findViewById(R.id.all_transaction_fragment_wallet_balance_text_view);
        bidsRemainingTextView = rootView.findViewById(R.id.all_transaction_fragment_bids_remaining_text_view);

        ImageButton backImageButton = rootView.findViewById(R.id.all_transactions_fragment_back_image_button);
        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onGoToMoreMenuButtonClicked();
            }
        });

        fetchTransactionDetails();

        return rootView;
    }

    void fetchTransactionDetails() {

        if (getActivity() == null)
            return;

        if (queue == null)
            queue = Volley.newRequestQueue(getActivity());

        String url = Constants.Config.API_PATH + "/my_account/";

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject apiResponse) {

                        TransactionData transactionData = new TransactionData(apiResponse);

                        JSONArray credit = transactionData.getCreditTransactions();
                        JSONArray debit = transactionData.getDebitTransactions();
                        JSONArray all = transactionData.getAllTransactions();
                        Double walletBalance = transactionData.getWalletBalance();

                        // Reset adapter to refresh data
                        PagerAdapter pagerAdapter = new TransactionHistoryPagerAdapter(getChildFragmentManager(), all, credit, debit);
                        transactionsViewPager.setAdapter(pagerAdapter);
                        Helper.wrapTabIndicatorToTitle(tabLayout, Constants.Config.TAB_LAYOUT_EXTERNAL_MARGIN, Constants.Config.TAB_LAYOUT_EXTERNAL_MARGIN);

                        //Fill wallet details
                        walletBalanceTextView.setText(String.format(Locale.US, "Rs. %.0f", walletBalance));
                        bidsRemainingTextView.setText(String.format(Locale.US, "%.0f Bids Remaining", walletBalance/5000));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        JSONObject additionalLogJson = new JSONObject();
                        try {
                            additionalLogJson.put("API", "/my_account");
                            additionalLogJson.put("JAVA_FILE", "AllTranscationsFragment.java");
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

        jsonRequest.setTag(TAG);
        queue.add(jsonRequest);
    }

    class TransactionData {

        private JSONArray creditTransactions = new JSONArray(), debitTransactions = new JSONArray(), allTransactions = new JSONArray();
        private double walletBalance = 0;

        TransactionData(JSONObject response) {
            try {
                boolean status = response.getBoolean("status");
                if (!status)
                    return;

                JSONObject details = response.getJSONObject("details");
                walletBalance = details.getJSONObject("current_balance").getDouble("value");

                allTransactions = details.getJSONObject("transactions").getJSONArray("all");

                for (int i=0; i<allTransactions.length(); i++) {
                    String transactionType = allTransactions.getJSONObject(i).getString("type");
                    if (transactionType.equals("CR")) {
                        creditTransactions.put(allTransactions.getJSONObject(i));
                    }
                    else if (transactionType.equals("DB")) {
                        debitTransactions.put(allTransactions.getJSONObject(i));
                    }
                }

            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public JSONArray getCreditTransactions() {
            return creditTransactions;
        }

        public JSONArray getDebitTransactions() {
            return debitTransactions;
        }

        public double getWalletBalance() {
            return walletBalance;
        }

        public JSONArray getAllTransactions() {
            return allTransactions;
        }
    }

    private class TransactionHistoryPagerAdapter extends FragmentPagerAdapter {

        private JSONArray all, credit, debit;

        TransactionHistoryPagerAdapter(FragmentManager fm, JSONArray all, JSONArray credit, JSONArray debit) {
            super(fm);
            this.all = all;
            this.credit = credit;
            this.debit = debit;
        }

        @Override
        public Fragment getItem(int position) {

            Fragment transactionHistoryFragment = new TransactionHistoryFragment();
            Bundle bundle = new Bundle();

            switch (position) {
                case 0:
                    bundle.putString("response", all.toString());
                    transactionHistoryFragment.setArguments(bundle);
                    return transactionHistoryFragment;

                case 1:
                    bundle.putString("response", credit.toString());
                    transactionHistoryFragment.setArguments(bundle);
                    return transactionHistoryFragment;

                case 2:
                    bundle.putString("response", debit.toString());
                    transactionHistoryFragment.setArguments(bundle);
                    return transactionHistoryFragment;

                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return "All";
                case 1:
                    return "Credit";
                case 2:
                    return "Debit";
                default:
                    return null;
            }
        }

        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return NUM_TRANSACTION_PAGES;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (queue != null)
            queue.cancelAll(TAG);
    }
}

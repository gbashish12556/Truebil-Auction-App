package com.truebil.business.Activities;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.truebil.business.Fragments.AllTransactionsFragment;
import com.truebil.business.Fragments.CarListingsFragment;
import com.truebil.business.Constants;
import com.truebil.business.Fragments.DisplayTextFragment;
import com.truebil.business.Fragments.MoreMenuFragment;
import com.truebil.business.Fragments.MyBidsFragment;
import com.truebil.business.Fragments.ProcuredFragment;
import com.truebil.business.Network.VolleyService;
import com.truebil.business.R;
import com.truebil.business.Fragments.UserAccountFragment;
import com.truebil.business.Utils.EventAnalytics;
import com.truebil.business.Utils.VolleyIdlingResource;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class BiddingActivity extends AppCompatActivity implements
        View.OnClickListener,
        MoreMenuFragment.OnMoreMenuFragmentClickListeners,
        AllTransactionsFragment.OnAllTransactionsFragmentClickListeners,
        UserAccountFragment.OnUserAccountFragmentClickListeners {

    public static FragmentManager fragmentManager;
    LinearLayout inAuctionLinearLayout, procuredLinearLayout, moreLinearLayout;
    RelativeLayout headerBarLinearLayout, myBidLinearLayout;
    TextView headerTextView;
    private ImageView inAuctionImageView, myBidsImageView, procuredImageView, moreImageView;
    private TextView inAuctionTextView, myBidsTextView, procuredTextView, moreTextView;
    private static final String TAG = "BiddingActivity";
    private RequestQueue requestQueue;
    private SharedPreferences sharedPref;
    @Nullable public static VolleyIdlingResource mIdlingResource;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bidding);

        sharedPref = this.getSharedPreferences("APP_PREFS", 0);

        inAuctionLinearLayout = findViewById(R.id.in_auction);
        myBidLinearLayout = findViewById(R.id.my_bid);
        procuredLinearLayout = findViewById(R.id.procured);
        moreLinearLayout = findViewById(R.id.more);

        inAuctionImageView = findViewById(R.id.activity_bidding_in_auction_image_view);
        myBidsImageView = findViewById(R.id.activity_bidding_my_bids_image_view);
        procuredImageView = findViewById(R.id.activity_bidding_procured_image_view);
        moreImageView = findViewById(R.id.activity_bidding_more_image_view);

        inAuctionTextView = findViewById(R.id.activity_bidding_in_auction_text_view);
        myBidsTextView = findViewById(R.id.activity_bidding_my_bids_text_view);
        procuredTextView = findViewById(R.id.activity_bidding_procured_text_view);
        moreTextView = findViewById(R.id.activity_bidding_more_text_view);

        getIdlingResource();

        // Load In Auction Fragment initially
        Fragment fragment = new CarListingsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.Keys.FRAGMENT, Constants.Keys.InAuction);
        fragment.setArguments(bundle);
        inAuctionImageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
        inAuctionTextView.setTextColor(getResources().getColor(R.color.colorPrimary));

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.main_content, fragment)
                .commit();

        inAuctionLinearLayout.setOnClickListener(this);
        myBidLinearLayout.setOnClickListener(this);
        procuredLinearLayout.setOnClickListener(this);
        moreLinearLayout.setOnClickListener(this);

        headerTextView = findViewById(R.id.activity_bidding_header_text_view);
        headerTextView.setText(R.string.title_fragment_in_auction);

        headerBarLinearLayout = findViewById(R.id.activity_bidding_header_relative_layout);
        setHeaderBarElevation(4);

        updateDeviceToken();
    }

    @Override
    public void onBackPressed() {

        // Remove stacked fragments first
        if (fragmentManager.getBackStackEntryCount() != 0) {
            fragmentManager.popBackStack();
            return;
        }

        // If not on inAuction fragment, jump to inAuction
        if (!headerTextView.getText().equals(getResources().getString(R.string.title_fragment_in_auction))) {
            inAuctionLinearLayout.performClick();
        }

        // Else exit
        else {
            super.onBackPressed();
        }
    }

    void setHeaderBarElevation(int value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            headerBarLinearLayout.setElevation(value * getApplicationContext().getResources().getDisplayMetrics().density);
        }
    }

    void setHeaderBarVisibility(int visibility) {
        headerBarLinearLayout.setVisibility(visibility);
    }

    @Override
    public void onClick(View view) {

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment;
        String title;
        Bundle bundle = new Bundle();

        inAuctionImageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.background_light_grey), android.graphics.PorterDuff.Mode.SRC_IN);
        myBidsImageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.background_light_grey), android.graphics.PorterDuff.Mode.SRC_IN);
        procuredImageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.background_light_grey), android.graphics.PorterDuff.Mode.SRC_IN);
        moreImageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.background_light_grey), android.graphics.PorterDuff.Mode.SRC_IN);

        inAuctionTextView.setTextColor(getResources().getColor(R.color.text_light_grey));
        myBidsTextView.setTextColor(getResources().getColor(R.color.text_light_grey));
        procuredTextView.setTextColor(getResources().getColor(R.color.text_light_grey));
        moreTextView.setTextColor(getResources().getColor(R.color.text_light_grey));

        String dealerId = String.valueOf(sharedPref.getInt(Constants.SharedPref.DEALER_ID,-1));
        String action;

        switch (view.getId()) {

            case R.id.in_auction:
                fragment = new CarListingsFragment();
                bundle.putString(Constants.Keys.FRAGMENT, Constants.Keys.InAuction);
                title = getResources().getString(R.string.title_fragment_in_auction);
                setHeaderBarElevation(4);
                setHeaderBarVisibility(View.VISIBLE);
                inAuctionImageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                inAuctionTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
                action = "clicked_inauction";
                break;

            case R.id.my_bid:
                fragment = new MyBidsFragment();
                title = getResources().getString(R.string.title_fragment_my_bids);
                setHeaderBarElevation(0);
                setHeaderBarVisibility(View.VISIBLE);
                myBidsImageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                myBidsTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
                action = "clicked_mybids";
                break;

            case R.id.procured:
                fragment = new ProcuredFragment();
                title = getResources().getString(R.string.title_fragment_procured);
                setHeaderBarElevation(0);
                setHeaderBarVisibility(View.VISIBLE);
                procuredImageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                procuredTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
                action = "clicked_procured";
                break;

            case R.id.more:
                fragment = new MoreMenuFragment();
                title = getResources().getString(R.string.title_fragment_more);
                setHeaderBarElevation(4);
                setHeaderBarVisibility(View.GONE);
                moreImageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                moreTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
                action = "clicked_more";
                break;

            default: //Similar to In Auction
                fragment = new CarListingsFragment();
                bundle.putString(Constants.Keys.FRAGMENT, Constants.Keys.InAuction);
                title = getResources().getString(R.string.title_fragment_in_auction);
                setHeaderBarElevation(4);
                setHeaderBarVisibility(View.VISIBLE);
                inAuctionImageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                inAuctionTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
                action = "clicked_inauction";
                break;
        }

        EventAnalytics.getInstance(this).logEvent("Nav_Bar", action, dealerId, 0);

        fragment.setArguments(bundle);
        headerTextView.setText(title);

        transaction.replace(R.id.main_content, fragment)
                .commitAllowingStateLoss();
    }

    @Override
    public void onShowProfileClicked() {
        UserAccountFragment userAccountFragment = new UserAccountFragment();

        fragmentManager.beginTransaction()
                .add(R.id.main_content, userAccountFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onAllTransactionsClicked() {
        AllTransactionsFragment allTransactionsFragment = new AllTransactionsFragment();

        fragmentManager.beginTransaction()
                .add(R.id.main_content, allTransactionsFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onTermsConditionsClicked() {
        Bundle bundle = new Bundle();
        bundle.putString("url", "https://url");
        DisplayTextFragment displayTextFragment = new DisplayTextFragment();
        displayTextFragment.setArguments(bundle);

        fragmentManager.beginTransaction()
                .add(R.id.main_content, displayTextFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onFAQsClicked() {
        Bundle bundle = new Bundle();
        bundle.putString("url", "https://url");
        DisplayTextFragment displayTextFragment = new DisplayTextFragment();
        displayTextFragment.setArguments(bundle);

        fragmentManager.beginTransaction()
                .add(R.id.main_content, displayTextFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onGoToMoreMenuButtonClicked() {

        if (fragmentManager.getBackStackEntryCount() != 0) {
            fragmentManager.popBackStack();
        }
    }

    void updateDeviceToken() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(BiddingActivity.this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String deviceToken = instanceIdResult.getToken();

                String previousSavedToken = sharedPref.getString(Constants.SharedPref.FIREBASE_TOKEN, "");
                String loginJWTToken = sharedPref.getString(Constants.SharedPref.JWT_TOKEN, "");

                if (!previousSavedToken.equals(deviceToken) && !loginJWTToken.isEmpty())
                    postFCMDeviceToken(deviceToken);
            }
        });
    }

    /*
     * TODO: Directly call the method defined in FirebaseMessageService.java
     * Currently not working because service methods cannot be called directly
     * by activity.
     */
    public void postFCMDeviceToken(final String deviceToken) {
        String dealerMobile = sharedPref.getString(Constants.SharedPref.DEALER_MOBILE, "");

        final JSONObject params = new JSONObject();
        try {
            params.put("token", deviceToken);
            params.put("device_source", "auction_app");
            params.put("type", "android");
            params.put("mobile", dealerMobile);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        String url = Constants.Config.API_PATH + "/register_device/";

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(getApplicationContext());

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Save the token in SharedPreferences upon API success
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(Constants.SharedPref.FIREBASE_TOKEN, deviceToken);
                        editor.apply();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyService.handleVolleyError(error, params, false, getApplicationContext());
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                String dealerJWTToken = sharedPref.getString(Constants.SharedPref.JWT_TOKEN, "");
                headers.put("Authorization", "jwt " + dealerJWTToken);
                return headers;
            }
        };

        jsonRequest.setTag(TAG);
        requestQueue.add(jsonRequest);
    }

    @VisibleForTesting
    @NonNull
    public IdlingResource getIdlingResource() {
        if (mIdlingResource == null) {
            mIdlingResource = new VolleyIdlingResource();
        }
        return mIdlingResource;
    }
}
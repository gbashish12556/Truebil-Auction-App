package com.truebil.business.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.instacart.library.truetime.TrueTime;
import com.truebil.business.Activities.ListingActivity;
import com.truebil.business.Constants;
import com.truebil.business.Helper;
import com.truebil.business.Models.ListingModel;
import com.truebil.business.Network.VolleyService;
import com.truebil.business.R;
import com.truebil.business.Utils.EventAnalytics;
import com.truebil.business.Utils.ServerTime;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static android.view.View.GONE;

public class InAuctionBidBottomSheetFragment extends Fragment {

    private static final String TAG = "InAuctionBidBottomSheetFragment";
    private LinearLayout submitBidLinearLayout, adjustAutoBidLinearLayout, editAutoBidSuperLinearLayout, submitAutoBidLinearLayout;
    private Button setBidLimitButton;
    private TextView highestBidTextView, bidStatusTextView, myBidTextView, timeRemainingTextView, submitBidTextView, suggestedAutoBidAmountTextView, autoBidAmountTextView;
    private ProgressBar bidProgressBar, confirmAutoBidProgressBar;
    private CountDownTimer countDownTimer;
    private RequestQueue requestQueue;
    private ValueEventListener firebaseListener;

    private int auctionId, highestBid;
    private int currentPossibleBid = 0;
    private int bidIncrement = 2000;
    private int dealerAutoBidAmount, suggestedAutoBidAmount, minThresholdAutoBidAmount;
    private String userId = "";
    private boolean hasDealerPlacedBid = false;

    private SharedPreferences sharedPref;
    private DatabaseReference firebaseAuctionNode;
    private InAuctionFragmentInterface mCallBack;
    private TranslateAnimation animationUp, animationDown;

    public interface InAuctionFragmentInterface {
        void onTimerEnd();
        void onLowBalanceBid();
        void setAuctionStatus(String status);
    }

    public InAuctionBidBottomSheetFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallBack = (InAuctionFragmentInterface) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement InAuctionFragmentInterface");
        }
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_in_auction_bid_bottom_sheet, container, false);

        sharedPref = getActivity().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        userId = String.valueOf(sharedPref.getInt(Constants.SharedPref.DEALER_ID, -1));
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        editAutoBidSuperLinearLayout = rootView.findViewById(R.id.fragment_in_auction_edit_bid_limit_super_linear_layout);
        adjustAutoBidLinearLayout = rootView.findViewById(R.id.fragment_in_auction_adjust_bid_limit_linear_layout);
        final LinearLayout editAutoBidLinearLayout = rootView.findViewById(R.id.fragment_in_auction_edit_bid_limit_linear_layout);
        submitBidLinearLayout = rootView.findViewById(R.id.fragment_in_auction_make_bid_linear_layout);
        setBidLimitButton = rootView.findViewById(R.id.fragment_in_auction_set_bid_limit_button);
        submitAutoBidLinearLayout = rootView.findViewById(R.id.fragment_in_auction_confirm_bid_limit_linear_layout);
        ImageButton decreaseAutoBidAmountImageButton = rootView.findViewById(R.id.fragment_in_auction_decrease_bid_limit_image_button);
        ImageButton increaseAutoBidAmountImageButton = rootView.findViewById(R.id.fragment_in_auction_increase_bid_limit_image_button);
        
        bidStatusTextView = rootView.findViewById(R.id.fragment_in_auction_bid_status_text_view);
        timeRemainingTextView = rootView.findViewById(R.id.fragment_in_auction_time_remaining_text_view);
        highestBidTextView = rootView.findViewById(R.id.fragment_in_auction_highest_bid_text_view);
        myBidTextView = rootView.findViewById(R.id.fragment_in_auction_my_bid_text_view);

        suggestedAutoBidAmountTextView = rootView.findViewById(R.id.fragment_in_auction_suggested_auto_bid_text_view);
        autoBidAmountTextView = rootView.findViewById(R.id.fragment_in_auction_set_bid_limit_text_view);
        submitBidTextView = rootView.findViewById(R.id.fragment_in_auction_make_bid_text_view);

        bidProgressBar = rootView.findViewById(R.id.fragment_in_auction_make_bid_progress_bar);
        confirmAutoBidProgressBar = rootView.findViewById(R.id.fragment_in_auction_confirm_bid_progress_bar);

        hideAllControls();
        bidProgressBar.setVisibility(GONE);
        confirmAutoBidProgressBar.setVisibility(GONE);
        bidStatusTextView.setVisibility(GONE);

        if (getArguments() != null && getArguments().getString("LISTING_JSON") == null) {
            return rootView;
        }

        final ListingModel listingModel;
        try {
            JSONObject listingJson = new JSONObject(getArguments().getString("LISTING_JSON"));
            listingModel = new ListingModel(listingJson);
        }
        catch (JSONException e) {
            e.printStackTrace();
            return rootView; // Do not proceed if LISTING_JSON is not well formed
        }

        auctionId = listingModel.getAuctionId();
        long bidEndTime = listingModel.getBidEndTime();
        highestBid = listingModel.getHighestBid();
        int myBid = listingModel.getMyDealerBid();
        final int walletBalance = listingModel.getWalletBalance();
        currentPossibleBid = bidIncrement + Math.max(listingModel.getMinBidAmount(), listingModel.getHighestBid()); // Next possible bid (or current possible bid) is max of highest bid till now or the min bid amount
        suggestedAutoBidAmount = listingModel.getSuggestedAutoBidPrice();
        dealerAutoBidAmount = listingModel.getDealerAutoBidAmount(); // TODO: Update this amount upon successful volley call
        String dealerAuctionStatus = listingModel.getDealerAuctionStatus();

        startCountDowntimer(bidEndTime);
        highestBidTextView.setText(Helper.convertToLakhs(highestBid));
        submitBidTextView.setText(String.format("%s%s", getString(R.string.bid_button_symbol), Helper.convertToLakhs(currentPossibleBid)));

        if (dealerAuctionStatus == null || dealerAuctionStatus.isEmpty()) { // Dealer has not begin participating
            myBidTextView.setText(getResources().getString(R.string.yet_to_bid));
            submitBidLinearLayout.setEnabled(true);

            //Animate Flash message
            if (getArguments() != null && getArguments().getString("SCREEN_SOURCE") != null) {
                if (getArguments().getString("SCREEN_SOURCE").equals("PushNotification")) {
                    animateFlashMessage();
                }
            }
        }
        else { // Dealer has participated
            myBidTextView.setText(Helper.convertToLakhs(myBid));

            if (dealerAuctionStatus.equalsIgnoreCase("Winning")) {
                bidStatusTextView.setBackgroundResource(R.drawable.background_green);
                bidStatusTextView.setText(getResources().getString(R.string.winning_bid_status));
                submitBidLinearLayout.setEnabled(false);
            }
            else if (dealerAuctionStatus.equalsIgnoreCase("Losing")) {
                bidStatusTextView.setBackgroundResource(R.drawable.background_orange);
                bidStatusTextView.setText(getResources().getString(R.string.losing_bid_status));
                submitBidLinearLayout.setEnabled(true);
            }
            bidStatusTextView.setVisibility(View.VISIBLE);
        }

        /*
         * We need to check if the dealer has enabled auto bid on this listing
         * If his/her auto bid price > 0, he/she has enabled auto bid (irrespective of losing or winning)
         * If his/her auto bid price = 0, we display the normal layout
         */
        if (dealerAutoBidAmount > 0) {
            editAutoBidSuperLinearLayout.setVisibility(View.VISIBLE);
            autoBidAmountTextView.setText(Helper.convertToLakhs(dealerAutoBidAmount)); //Display the value that user has submitted
            submitBidLinearLayout.setVisibility(View.VISIBLE);
        }
        else {
            setBidLimitButton.setVisibility(View.VISIBLE);
            submitBidLinearLayout.setVisibility(View.VISIBLE);
        }

        firebaseAuctionNode = database.getReference().getRoot().child(Constants.Keys.BidInfo).child(String.valueOf(auctionId));
        firebaseListener = firebaseAuctionNode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ds) {

                if (ds.getValue() == null)
                    return;

                int winningUserId = (int) (long) ds.child(Constants.Keys.USER_ID).getValue();
                highestBid = (int) (long) ds.child(Constants.Keys.HIGHEST_BID).getValue();
                String newBidEndTimeString = (String) ds.child(Constants.Keys.BID_END_TIME).getValue();

                // This is not required to be pulled from Firebase (?)
                // What if user has logged in from 2 devices and has opened the same listing
                if (ds.child(userId).getValue() != null) {
                    int myBid = (int) (long) ds.child(userId).getValue();
                    myBidTextView.setText(Helper.convertToLakhs(myBid));
                }

                currentPossibleBid = highestBid + bidIncrement;
                submitBidTextView.setText(String.format("%s%s", getString(R.string.bid_button_symbol), Helper.convertToLakhs(currentPossibleBid)));
                highestBidTextView.setText(Helper.convertToLakhs(highestBid));

                // If current user is winning
                if (userId.equalsIgnoreCase(String.valueOf(winningUserId))) {
                    bidStatusTextView.setText(getResources().getString(R.string.winning_bid_status));
                    bidStatusTextView.setBackgroundResource(R.drawable.background_green);
                    bidStatusTextView.setVisibility(View.VISIBLE);
                    submitBidLinearLayout.setEnabled(false);
                }
                // If current user is Not winning
                else {
                    // If current user is Losing
                    if (ds.hasChild(userId)) {
                        bidStatusTextView.setText(getResources().getString(R.string.losing_bid_status));
                        bidStatusTextView.setBackgroundResource(R.drawable.background_orange);
                    }

                    submitBidLinearLayout.setEnabled(true);
                }

                // If highestBid is less than dealerAutoBidAmount, show "Your bid is being placed"
                if (!userId.equalsIgnoreCase(String.valueOf(winningUserId)) && dealerAutoBidAmount > highestBid) {
                    bidStatusTextView.setText(getResources().getString(R.string.pending_auto_bid_status));
                    bidStatusTextView.setBackgroundResource(R.drawable.background_green);
                    bidStatusTextView.setVisibility(View.VISIBLE);
                    submitBidLinearLayout.setEnabled(false);
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                try {
                    long newBidEndTime = sdf.parse(newBidEndTimeString).getTime();
                    startCountDowntimer(newBidEndTime);
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        submitBidLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventAnalytics.getInstance(getActivity()).logEvent("Dedicated", "clicked_bid", String.valueOf(auctionId) + "," + String.valueOf(currentPossibleBid) + "," + userId, 0);

                if (walletBalance > Constants.Config.MIN_LOW_BALANCE) {
                    postDealerBid(auctionId, currentPossibleBid, false);
                }
                else {
                    mCallBack.onLowBalanceBid();
                }
            }
        });

        setBidLimitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableAllControls(false);
                getAutoBidDetails(auctionId);
            }
        });

        submitAutoBidLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (walletBalance > Constants.Config.MIN_LOW_BALANCE) {
                    postDealerBid(auctionId, suggestedAutoBidAmount, true);
                }
                else {
                    mCallBack.onLowBalanceBid();
                }
            }
        });

        editAutoBidLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableAllControls(false);
                getAutoBidDetails(auctionId);
            }
        });

        decreaseAutoBidAmountImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if value can be decreased further
                if (suggestedAutoBidAmount - bidIncrement < minThresholdAutoBidAmount)
                    return;

                suggestedAutoBidAmount -= bidIncrement;
                suggestedAutoBidAmountTextView.setText(Helper.convertToLakhs(suggestedAutoBidAmount));
            }
        });

        increaseAutoBidAmountImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                suggestedAutoBidAmount += bidIncrement;
                suggestedAutoBidAmountTextView.setText(Helper.convertToLakhs(suggestedAutoBidAmount));
            }
        });
        return rootView;
    }

    private void animateFlashMessage() {
        if (!hasDealerPlacedBid) {
            bidStatusTextView.setText(R.string.place_bid);
            bidStatusTextView.setBackgroundResource(R.drawable.background_green);

            animationUp = new TranslateAnimation(0, 0, 100, 0);
            animationUp.setDuration(1000);
            animationUp.setFillAfter(true);

            final Handler upHandler = new Handler();
            upHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bidStatusTextView.setVisibility(View.VISIBLE);
                    if (!hasDealerPlacedBid)
                        bidStatusTextView.startAnimation(animationUp);
                }
            }, 100);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // goes down
                    animationDown = new TranslateAnimation(0, 0, 0, 100);
                    animationDown.setDuration(1000);
                    animationDown.setFillAfter(true);
                    if (!hasDealerPlacedBid)
                        bidStatusTextView.startAnimation(animationDown);
                    animationDown.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            bidStatusTextView.setBackgroundColor(Color.TRANSPARENT);
                            bidStatusTextView.setVisibility(GONE);
                            animationUp = new TranslateAnimation(0, 0, 100, 0);
                            bidStatusTextView.startAnimation(animationUp);
                        }
                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                }
            }, 3000);
        }
    }

    private void startCountDowntimer(long bidEndTime) {

        long currentTime = System.currentTimeMillis();
        if (ServerTime.getInstance().isInitialised()) {
            currentTime = ServerTime.getInstance().getTime();
        }
        else if (TrueTime.isInitialized()) {
            currentTime = TrueTime.now().getTime();
        }

        final long timeRemainingMillis = bidEndTime - currentTime;

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (timeRemainingMillis > 0) {

            mCallBack.setAuctionStatus("active");
            countDownTimer = new CountDownTimer(timeRemainingMillis, 1000) {

                public void onTick(long millisUntilFinished) {
                    int hours = (int) (millisUntilFinished / 1000) / 3600;
                    int minutes = (int) ((millisUntilFinished / 1000) % 3600) / 60;
                    int seconds = (int) ((millisUntilFinished / 1000) % 3600) % (60);
                    String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                    timeRemainingTextView.setText(timeLeftFormatted + "s");

                    if (hours == 0 && minutes <= 1) {
                        timeRemainingTextView.setTextColor(getResources().getColor(R.color.orange_background));
                    }
                    else {
                        timeRemainingTextView.setTextColor(getResources().getColor(R.color.text_dark_grey));
                    }
                }

                public void onFinish() {
                    timeRemainingTextView.setText(getString(R.string.timer_ends));
                    mCallBack.setAuctionStatus("closed");
                    enableAllControls(false);
                    mCallBack.onTimerEnd();
                }

            }.start();
        }
        else {
            timeRemainingTextView.setText(getString(R.string.timer_ends));
            mCallBack.setAuctionStatus("closed");
            enableAllControls(false);
        }
    }

    public void postDealerBid(final int auctionId, final int bidAmount, final boolean isAutoBid) {
        hasDealerPlacedBid = true;

        bidStatusTextView.clearAnimation();
        if (animationUp != null)
            animationUp.cancel();
        if (animationDown != null)
            animationDown.cancel();

        if (getActivity() == null)
            return;

        String url = Constants.Config.API_PATH + "/dealer_bid/";

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(getActivity());

        if (ListingActivity.mIdlingResource != null) {
            ListingActivity.mIdlingResource.setIdleState(false);
        }

        final JSONObject params = new JSONObject();
        try {
            params.put("listing_auction_id", String.valueOf(auctionId));

            /* Either send the auto bid amount or send the bid amount */
            if (isAutoBid)
                params.put("auto_bid_amount", String.valueOf(bidAmount));
            else
                params.put("amount", String.valueOf(bidAmount));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        enableAllControls(false);
        if (isAutoBid)
            confirmAutoBidProgressBar.setVisibility(View.VISIBLE);
        else
            bidProgressBar.setVisibility(View.VISIBLE);

        final long apiStartTime = System.nanoTime();
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            bidProgressBar.setVisibility(GONE);
                            confirmAutoBidProgressBar.setVisibility(GONE);
                            enableAllControls(true);

                            Boolean status = (Boolean) response.get("status");
                            if (status) {
                                bidStatusTextView.setVisibility(View.VISIBLE);

                                long apiResponseDelayMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - apiStartTime);
                                String serverTime = response.getString("server_time");
                                ServerTime.getInstance().initServerDate(serverTime, apiResponseDelayMs / 2);

                                // Display the edit auto bid layout on success
                                if (isAutoBid) {
                                    hideAllControls();
                                    editAutoBidSuperLinearLayout.setVisibility(View.VISIBLE);
                                    submitBidLinearLayout.setVisibility(View.VISIBLE);
                                    autoBidAmountTextView.setText(Helper.convertToLakhs(bidAmount));
                                    dealerAutoBidAmount = bidAmount;
                                }
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (ListingActivity.mIdlingResource != null) {
                            ListingActivity.mIdlingResource.setIdleState(true);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        bidProgressBar.setVisibility(GONE);
                        confirmAutoBidProgressBar.setVisibility(GONE);

                        enableAllControls(true);
                        submitBidLinearLayout.setEnabled(true);

                        try {
                            params.put("API", "/dealer_bid");
                            VolleyService.handleVolleyError(error, params, true, getActivity());
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (isAutoBid) {
                            getAutoBidDetails(auctionId);
                        }

                        if (ListingActivity.mIdlingResource != null) {
                            ListingActivity.mIdlingResource.setIdleState(true);
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
        requestQueue.add(jsonRequest);
    }

    public void getAutoBidDetails(int auctionId) {
        if (getActivity() == null)
            return;

        String url = Constants.Config.API_PATH + "/auto_bid_details?auction_id=" + auctionId;

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(getActivity());

        final JSONObject params = new JSONObject();
        try {
            params.put("auction_id", auctionId);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hideAllControls();
                        enableAllControls(true);
                        adjustAutoBidLinearLayout.setVisibility(View.VISIBLE);
                        submitAutoBidLinearLayout.setVisibility(View.VISIBLE);

                        try {
                            Boolean status = (Boolean) response.get("status");
                            if (status) {
                                // Get highest auto bid, user id with highest auto bid, suggested auto bid, min auto bid.
                                JSONObject details = response.getJSONObject("details");
                                minThresholdAutoBidAmount = details.getInt("minimum_auto_bid_amount");
                                suggestedAutoBidAmount = details.getInt("suggested_auto_bid_amount");

                                if (!details.isNull("dealer_auto_bid_amount"))
                                    dealerAutoBidAmount = details.getInt("dealer_auto_bid_amount");
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }

                        suggestedAutoBidAmountTextView.setText(Helper.convertToLakhs(suggestedAutoBidAmount)); // Display the higher value b/w user's auto bid and suggested bid
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        enableAllControls(true);
                        try {
                            params.put("API", "/auto_bid_details (GET)");
                            VolleyService.handleVolleyError(error, params, true, getActivity());
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
        requestQueue.add(jsonRequest);
    }

    /*
     * This method is used to disable all buttons and linearlayouts
     * when user places a bid or an auto bid. These control buttons are
     * re-enabled once the network call completes.
     */
    void enableAllControls(boolean status) {
        /*
         * Do not enable this button on a successful dealer_bid response.
         * This button is enabled or disabled depending on the data in Firebase
         */
        if (!status) {
            submitBidLinearLayout.setEnabled(false);
        }

        editAutoBidSuperLinearLayout.setEnabled(status);
        adjustAutoBidLinearLayout.setEnabled(status);
        submitAutoBidLinearLayout.setEnabled(status);
        setBidLimitButton.setEnabled(status);
    }

    /*
     * Helper method to easily hide all buttons and relative layouts
     */
    void hideAllControls() {
        submitBidLinearLayout.setVisibility(GONE);
        editAutoBidSuperLinearLayout.setVisibility(GONE);
        adjustAutoBidLinearLayout.setVisibility(GONE);
        submitAutoBidLinearLayout.setVisibility(GONE);
        setBidLimitButton.setVisibility(GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (requestQueue != null)
            requestQueue.cancelAll(TAG);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Remove firebase listener
        if (firebaseAuctionNode != null && firebaseListener != null) {
            firebaseAuctionNode.removeEventListener(firebaseListener);
        }
        //Cancel countdown timer
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}

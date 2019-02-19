package com.truebil.business.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.truebil.business.Activities.BiddingActivity;
import com.truebil.business.Activities.CitiesAndRtosActivity;
import com.truebil.business.Activities.ListingActivity;
import com.truebil.business.Adapters.ListingImagesRecyclerViewAdapter;
import com.truebil.business.Helper;
import com.truebil.business.Models.CarListModel;
import com.truebil.business.Constants;
import com.truebil.business.CustomLayouts.CustomSwipeRefreshLayout;
import com.truebil.business.Network.VolleyService;
import com.truebil.business.R;
import com.truebil.business.Utils.EventAnalytics;
import com.truebil.business.Utils.ServerTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class CarListingsFragment extends Fragment implements ListingImagesRecyclerViewAdapter.ListingImagesRecyclerViewInterface {

    private CarListAdapter listAdapter;
    private LinearLayout volleyErrorLinearLayout, noListingsLinearLayout, noRTOLinearLayout;
    private CustomSwipeRefreshLayout swipeRefreshLayout;
    private ListView carListingListView;
    private View footerProgressView;
    private ProgressBar listingProgressBar;
    private ArrayList<CarListModel> carDataList = new ArrayList<>();
    private final static String TAG = "CarListingFragment";
    protected RequestQueue volleyRequestQueue;
    private DatabaseReference bidInfoReference;
    private int preLast;
    private String nextURL, userId, apiUrl = null, fragmentName;
    boolean displayBiddingInfo = true, displayMyBidInfo = false;
    SharedPreferences sharedPref;
    Boolean isRtoSelected = false;


    public CarListingsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_car_listings, container, false);

        if (getActivity() == null || getContext() == null)
            return rootView;

        sharedPref = getContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        userId = String.valueOf(sharedPref.getInt(Constants.SharedPref.DEALER_ID,-1));

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        bidInfoReference = database.getReference().getRoot().child("BidInfo");

        volleyErrorLinearLayout = rootView.findViewById(R.id.item_volley_error_linear_layout);
        noListingsLinearLayout = rootView.findViewById(R.id.item_no_listings_linear_layout);
        noRTOLinearLayout = rootView.findViewById(R.id.item_no_rto_linear_layout);

        listAdapter = new CarListAdapter(carDataList, getContext());
        carListingListView = rootView.findViewById(R.id.fragment_car_listings_list_view);
        carListingListView.setAdapter(listAdapter);
        setupEndlessListener();

        listingProgressBar = rootView.findViewById(R.id.fragment_car_listing_progress_bar);
        footerProgressView = inflater.inflate(R.layout.item_progress_bar_footer, carListingListView, false);

        Button retryVolleyButton = rootView.findViewById(R.id.retry_volley_button);
        retryVolleyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volleyErrorLinearLayout.setVisibility(View.GONE);
                noListingsLinearLayout.setVisibility(View.GONE);
                carListingListView.setVisibility(View.VISIBLE);
                fetchCarListInfo(apiUrl);
            }
        });

        Button selectRTOButton = rootView.findViewById(R.id.select_rto_button);
        selectRTOButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CitiesAndRtosActivity.class);
                startActivity(intent);
            }
        });

        swipeRefreshLayout = rootView.findViewById(R.id.fragment_car_listings_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);

                // Clear contents of list view
                clearModelsDataList();
                listAdapter.notifyDataSetChanged();
                carListingListView.setVisibility(View.VISIBLE);
                carListingListView.removeFooterView(footerProgressView);

                // Hide volley error layout
                volleyErrorLinearLayout.setVisibility(View.GONE);
                noListingsLinearLayout.setVisibility(View.GONE);

                // Try fetching data again
                fetchCarListInfo(apiUrl);
            }
        });

        // Set ApiURL and fetch data if rto is selected
        if (getArguments() != null) {
            fragmentName = getArguments().getString(Constants.Keys.FRAGMENT);

            if (fragmentName == null)
                return rootView;

            String rtoStatesPref = Helper.getPreference(getActivity(), Constants.Keys.STATES);
            String rtoCitiesPref = Helper.getPreference(getActivity(), Constants.Keys.CITIES);

            switch (fragmentName) {
                case Constants.Keys.InAuction:
                    apiUrl = Constants.Config.API_PATH + "/auction_listings/?auction_status_id=1&state_code=" + rtoStatesPref + "&city_id=" + rtoCitiesPref;
                    break;
                case Constants.Keys.MyBidsWinning:
                    apiUrl = Constants.Config.API_PATH + "/my_bids/?status=winning";
                    break;
                case Constants.Keys.MyBidsLosing:
                    apiUrl = Constants.Config.API_PATH + "/my_bids/?status=losing";
                    break;
                case Constants.Keys.MyBidsHistory:
                    apiUrl = Constants.Config.API_PATH + "/my_bids/?status=history";
                    displayBiddingInfo = false;
                    break;
                case Constants.Keys.Negotiating:
                    apiUrl = Constants.Config.API_PATH + "/auction_listings/?auction_status_id=2,8";
                    displayBiddingInfo = false;
                    displayMyBidInfo = true;
                    break;
                case Constants.Keys.Procured:
                    apiUrl = Constants.Config.API_PATH + "/auction_listings/?auction_status_id=6";
                    displayBiddingInfo = false;
                    displayMyBidInfo = true;
                    break;
                case Constants.Keys.DealCancelled:
                    apiUrl = Constants.Config.API_PATH + "/auction_listings/?auction_status_id=3,4";
                    displayBiddingInfo = false;
                    break;
                default:
                    apiUrl = Constants.Config.API_PATH + "/auction_listings/?auction_status_id=1";
                    break;
            }
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        String rtoCities = Helper.getPreference(getActivity(), Constants.Keys.CITIES);
        String rtoStates = Helper.getPreference(getActivity(), Constants.Keys.STATES);

        if (rtoCities.equalsIgnoreCase("") || rtoStates.equalsIgnoreCase("")) {
            isRtoSelected = false;
            noRTOLinearLayout.setVisibility(View.VISIBLE);

            // Hide other layouts
            volleyErrorLinearLayout.setVisibility(View.GONE);
            noListingsLinearLayout.setVisibility(View.GONE);
            carListingListView.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.GONE);
        }
        else {
            isRtoSelected = true;

            // Hide other layouts
            volleyErrorLinearLayout.setVisibility(View.GONE);
            noListingsLinearLayout.setVisibility(View.GONE);
            noRTOLinearLayout.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);

            // Clear contents of list view
            clearModelsDataList();
            listAdapter.notifyDataSetChanged();
            carListingListView.setVisibility(View.VISIBLE);
            carListingListView.removeFooterView(footerProgressView);
            fetchCarListInfo(apiUrl);
        }
    }

    void fetchCarListInfo(final String url) {

        if (getActivity() == null)
            return;

        if (volleyRequestQueue == null)
            volleyRequestQueue = Volley.newRequestQueue(getActivity());

        if (carDataList.size() == 0)
            listingProgressBar.setVisibility(View.VISIBLE);

        if (BiddingActivity.mIdlingResource != null) {
            BiddingActivity.mIdlingResource.setIdleState(false);
        }

        final long apiStartTime = System.nanoTime();
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (!isRtoSelected) {
                        return;
                    }

                    // View CarListingListView
                    carListingListView.setVisibility(View.VISIBLE);

                    // Enable SwipeRefresh
                    swipeRefreshLayout.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);

                    // Hide other layouts
                    listingProgressBar.setVisibility(View.GONE);
                    volleyErrorLinearLayout.setVisibility(View.GONE);
                    noListingsLinearLayout.setVisibility(View.GONE);
                    noRTOLinearLayout.setVisibility(View.GONE);

                    long apiResponseDelayMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - apiStartTime);
                    parseCarListInfo(response, apiResponseDelayMs);

                    if (BiddingActivity.mIdlingResource != null) {
                        BiddingActivity.mIdlingResource.setIdleState(true);
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    // Stop refreshing
                    swipeRefreshLayout.setRefreshing(false);
                    listingProgressBar.setVisibility(View.GONE);

                    if (carDataList.size() != 0 && isRtoSelected) {

                        // Display Volley Error Layout
                        volleyErrorLinearLayout.setVisibility(View.VISIBLE);

                        // Hide other layouts
                        carListingListView.setVisibility(View.GONE);
                        noListingsLinearLayout.setVisibility(View.GONE);
                        swipeRefreshLayout.setVisibility(View.GONE);
                        noRTOLinearLayout.setVisibility(View.GONE);
                    }

                    JSONObject params = new JSONObject();
                    try {
                        params.put("API_URL", url);
                        VolleyService.handleVolleyError(error, params, true, getContext());
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }

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

        jsonRequest.setTag(TAG);
        volleyRequestQueue.add(jsonRequest);
    }

    void setupEndlessListener() {

        carListingListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                final int lastItem = firstVisibleItem + visibleItemCount;

                if (lastItem == totalItemCount) {
                    if (preLast != lastItem) { //to avoid multiple calls for last item
                        preLast = lastItem;

                        if (nextURL != null) {
                            fetchCarListInfo(nextURL);
                        }
                    }
                }
            }
        });
    }

    void parseCarListInfo(JSONObject response, long apiResponseDelayMs) {
        try {
            nextURL = null;
            if (!response.isNull("next"))
                nextURL = response.getString("next");

            // Sync time
            String serverTime = response.getString("server_time");
            ServerTime.getInstance().initServerDate(serverTime, apiResponseDelayMs/2);

            JSONArray carListingArray = response.getJSONArray("results");

            // If count is 0, display no car found layout
            if (carListingArray.length() == 0) {
                carListingListView.setVisibility(View.GONE);
                noListingsLinearLayout.setVisibility(View.VISIBLE);
            }

            for (int i=0; i<carListingArray.length(); i++) {
                JSONObject individualResultJSON = carListingArray.getJSONObject(i);

                CarListModel carListModel = new CarListModel(individualResultJSON);
                carDataList.add(carListModel);
            }

            listAdapter.notifyDataSetChanged();

            // Set bottom progress bar
            if (nextURL != null) {
                if (carListingListView.getFooterViewsCount() == 0)
                    carListingListView.addFooterView(footerProgressView);
            }
            else // Remove the footer
                carListingListView.removeFooterView(footerProgressView);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    CarListModel getListingModelFromAuctionId(String auctionId) {
        // TODO: Optimize
        for (CarListModel carListModel : carDataList) {
            if (carListModel.getAuctionId().equals(auctionId))
                return carListModel;
        }
        return null;
    }

    void clearModelsDataList() {
        for (CarListModel carListModel : carDataList) {
            String auctionId = String.valueOf(carListModel.getAuctionId());
            DatabaseReference firebaseAuctionNode = bidInfoReference.child(auctionId);

            // Cancel existing CountDownTimer
            if (carListModel.getCountDowntimer() != null) {
                carListModel.getCountDowntimer().cancel();
                carListModel.setCountDownTimer(null);
            }

            // Remove existing FirebaseValueListener
            if (carListModel.getValueEventListener() != null) {
                firebaseAuctionNode.removeEventListener(carListModel.getValueEventListener());
                carListModel.setValueEventListener(null);
            }
        }
        carDataList.clear();
    }

    public class CarListAdapter extends ArrayAdapter<CarListModel> {

        CarListAdapter(ArrayList<CarListModel> data, Context context) {
            super(context, R.layout.item_car_listing, data);
        }

        @NonNull @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            final CarListModel carListModel = getItem(position);

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_car_listing, parent, false);
            }
            else {
                // Remove previous CountDownTimer, ValueEventListener, etc.

                // Find the last auction id that was used to insert data in this convertview.
                // This could be done by finding the last tag value.
                String auctionIdReused = String.valueOf(convertView.getTag());

                // Now using this tag value, get the countdown timer from the Model
                // and finish it.
                CarListModel reusedCarListModel = getListingModelFromAuctionId(auctionIdReused);
                if (reusedCarListModel != null && reusedCarListModel.getCountDowntimer() != null) {
                    reusedCarListModel.getCountDowntimer().cancel();
                    reusedCarListModel.setCountDownTimer(null);
                }

                // Remove any existing firebase listeners
                DatabaseReference firebaseAuctionNode = bidInfoReference.child(auctionIdReused);
                if (reusedCarListModel != null && reusedCarListModel.getValueEventListener() != null) {
                    firebaseAuctionNode.removeEventListener(reusedCarListModel.getValueEventListener());
                    reusedCarListModel.setValueEventListener(null);
                }
            }

            // Important: Set tag for the convertview
            if (carListModel != null) {
                convertView.setTag(carListModel.getAuctionId());
            }

            RecyclerView listingImagesRecyclerView = convertView.findViewById(R.id.item_car_listing_recycler_view);
            final ImageView auctionClosedImageView = convertView.findViewById(R.id.item_car_listing_auction_closed_image_view);

            TextView manufacturingYearTextView = convertView.findViewById(R.id.item_car_listing_manufacturing_year_text_view);
            TextView variantNameTextView = convertView.findViewById(R.id.item_car_listing_variant_name_text_view);
            TextView fuelTypeTextView = convertView.findViewById(R.id.item_car_listing_fuel_type_text_view);
            TextView cityNameTextView = convertView.findViewById(R.id.item_car_listing_city_name_text_view);
            TextView mileageTextView = convertView.findViewById(R.id.item_car_listing_mileage_text_view);
            TextView ownerDetailsTextView = convertView.findViewById(R.id.item_car_listing_owner_detail_text_view);
            TextView rtoNameTextView = convertView.findViewById(R.id.item_car_listing_rto_name_text_view);

            LinearLayout biddingInfoLinearLayout = convertView.findViewById(R.id.item_car_listing_bid_info_linear_layout);
            final TextView timeRemainingTextView = convertView.findViewById(R.id.item_car_listing_time_remaining_text_view);
            final TextView topOfferTextView = convertView.findViewById(R.id.item_car_listing_highest_bid_text_view);
            final TextView myBidTextView = convertView.findViewById(R.id.item_car_listing_my_bid_text_view);
            final TextView bidStatusTextView = convertView.findViewById(R.id.item_car_listing_bidding_status_text_view);

            LinearLayout myBidAmountLinearLayout = convertView.findViewById(R.id.item_car_listing_bid_amount_linear_layout);
            TextView myBidAmountTextView = convertView.findViewById(R.id.item_car_listing_bid_amount_text_view);

            topOfferTextView.setText(Helper.getIndianCurrencyFormat(0));
            myBidTextView.setText(R.string.yet_to_bid);
            bidStatusTextView.setVisibility(View.GONE);
            timeRemainingTextView.setText(getString(R.string.timer_ends));

            if (carListModel != null) {

                // set up the RecyclerView
                LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
                listingImagesRecyclerView.setLayoutManager(horizontalLayoutManager);
                ListingImagesRecyclerViewAdapter adapter = new ListingImagesRecyclerViewAdapter(CarListingsFragment.this, carListModel);
                listingImagesRecyclerView.setAdapter(adapter);

                manufacturingYearTextView.setText(carListModel.getManufactutingYear());
                variantNameTextView.setText(carListModel.getVariantName());
                fuelTypeTextView.setText(carListModel.getFuelType());
                cityNameTextView.setText(carListModel.getCityName());
                mileageTextView.setText(carListModel.getMileage() + " Km");
                ownerDetailsTextView.setText(carListModel.getOwnerDetail() + " Owner");
                rtoNameTextView.setText(carListModel.getRto());

                // Display Dealer's Bid in Negotiation and Procured tab
                if (displayMyBidInfo) {
                    int myBidAmount = carListModel.getMyBid();
                    myBidAmountLinearLayout.setVisibility(View.VISIBLE);
                    myBidAmountTextView.setText(Helper.getIndianCurrencyFormat(myBidAmount));
                }
                else {
                    myBidAmountLinearLayout.setVisibility(View.GONE);
                }

                // Display timer and bidding info in InAuction, Winning and Losing
                if (displayBiddingInfo) {

                    // Display bid info such as Timer, Highest Bid, My Bid
                    biddingInfoLinearLayout.setVisibility(View.VISIBLE);

                    // Fetch Firebase database reference for this auction id
                    DatabaseReference firebaseAuctionNode = bidInfoReference.child(carListModel.getAuctionId());

                    // Get highest and my bid from Model
                    int highestBid = carListModel.getHighestBid();
                    int myBid = carListModel.getMyBid();
                    setBidValues(topOfferTextView, myBidTextView, bidStatusTextView, highestBid, myBid);

                    // Get Bid End Time from Model and start timer
                    startTimer(auctionClosedImageView, timeRemainingTextView, carListModel);

                    // Remove any previous firebase listeners
                    if (carListModel.getValueEventListener() != null) {
                        firebaseAuctionNode.removeEventListener(carListModel.getValueEventListener());
                        carListModel.setValueEventListener(null);
                    }

                    // Attach a new firebase listener
                    // Now this piece of code is executed whenever the value changes in firebase for this auction id
                    ValueEventListener firebaseListener = firebaseAuctionNode.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            // Update highest bid in Model
                            if (dataSnapshot.child("highest_bid").getValue() != null) {
                                int newHighestBid = (int) (long) dataSnapshot.child("highest_bid").getValue();
                                carListModel.setHighestBid(newHighestBid);
                            }

                            // Update My Bid //TODO: Should happen inside the app without Firebase
                            if (dataSnapshot.child(userId).getValue() != null) {
                                int myNewBid = (int) (long) dataSnapshot.child(userId).getValue();
                                carListModel.setMyBid(myNewBid);
                            }

                            // Update Bid End Time in Model
                            if (dataSnapshot.child("bid_end_time").getValue() != null) {
                                String newBidEndTime = (String) dataSnapshot.child("bid_end_time").getValue();

                                if (!carListModel.getBidEndTime().equals(newBidEndTime)) {
                                    carListModel.setBidEndTime(newBidEndTime);
                                    startTimer(auctionClosedImageView, timeRemainingTextView, carListModel);
                                }
                            }

                            int myBid = carListModel.getMyBid();
                            int highestBid = carListModel.getHighestBid();
                            setBidValues(topOfferTextView, myBidTextView, bidStatusTextView, highestBid, myBid);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });

                    // Add the new firebase listener
                    carListModel.setValueEventListener(firebaseListener);
                }

                // If did info is not supposed to be displayed (History, Procured, Negotiation, etc)
                else {
                    biddingInfoLinearLayout.setVisibility(View.GONE);
                    bidStatusTextView.setVisibility(View.VISIBLE);

                    if (carListModel.getAuctionStatus().equalsIgnoreCase("active")) {
                        auctionClosedImageView.setVisibility(View.GONE);
                        switch (carListModel.getDealerAuctionStatus()) {
                            case "winning":
                                bidStatusTextView.setText(R.string.winning_bid_status);
                                bidStatusTextView.setBackgroundResource(R.drawable.background_green);
                                break;
                            case "losing":
                                bidStatusTextView.setText(R.string.losing_bid_status);
                                bidStatusTextView.setBackgroundResource(R.drawable.background_orange);
                                break;
                        }
                    }
                    else if (carListModel.getDealerAuctionStatus().equalsIgnoreCase("lost")) {
                        auctionClosedImageView.setVisibility(View.VISIBLE);
                        bidStatusTextView.setText(R.string.lost_status);
                        bidStatusTextView.setBackgroundResource(R.drawable.background_orange);
                    }
                    else {
                        auctionClosedImageView.setVisibility(View.VISIBLE);
                        switch (carListModel.getAuctionStatus()) {
                            case "negotiation":
                                bidStatusTextView.setText(R.string.negotiating_status);
                                bidStatusTextView.setBackgroundResource(R.drawable.background_green);
                                break;
                            case "waiting_for_procurement":
                                bidStatusTextView.setText(R.string.waiting_for_procurement_status);
                                bidStatusTextView.setBackgroundResource(R.drawable.background_green);
                                break;
                            case "payment_waiting":
                                bidStatusTextView.setText(R.string.waiting_payment_status);
                                bidStatusTextView.setBackgroundResource(R.drawable.background_green);
                                break;
                            case "dealer_cancelled":
                                bidStatusTextView.setText(R.string.dealer_cancelled_status);
                                bidStatusTextView.setBackgroundResource(R.drawable.background_orange);
                                break;
                            case "seller_cancelled":
                                bidStatusTextView.setText(R.string.seller_cancelled_status);
                                bidStatusTextView.setBackgroundResource(R.drawable.background_orange);
                                break;
                            case "dealer_rejected":
                                bidStatusTextView.setText(R.string.dealer_rejected_status);
                                bidStatusTextView.setBackgroundResource(R.drawable.background_orange);
                                break;
                            case "seller_rejected":
                                bidStatusTextView.setText(R.string.seller_rejected_status);
                                bidStatusTextView.setBackgroundResource(R.drawable.background_orange);
                                break;
                            default:
                                break;
                        }
                    }
                }

                // Go to dedicated activity
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startListingActivity(carListModel);
                    }
                });
            }

            return convertView;
        }

        private void startTimer(final ImageView auctionClosedImageView, final TextView timeRemainingTextView, final CarListModel carListModel) {

            // Cancel previous timer
            if (carListModel.getCountDowntimer() != null) {
                carListModel.getCountDowntimer().cancel();
                carListModel.setCountDownTimer(null);
            }

            String bidEndTime = carListModel.getBidEndTime();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            try {
                Date date = simpleDateFormat.parse(bidEndTime);
                long endTime = date.getTime();
                long currentTime = System.currentTimeMillis();

                if (ServerTime.getInstance().isInitialised()) {
                    currentTime = ServerTime.getInstance().getTime();
                }
                else if (TrueTime.isInitialized()) {
                    currentTime = TrueTime.now().getTime();
                }

                long timeRemainingMillis = endTime - currentTime;

                if (timeRemainingMillis > 0) {
                    auctionClosedImageView.setVisibility(View.GONE);

                    CountDownTimer countDownTimer = new CountDownTimer(timeRemainingMillis, 1000) {
                        public void onTick(long millisUntilFinished) {
                            int hours = (int) (millisUntilFinished / 1000) / 3600;
                            int minutes = (int) (( millisUntilFinished / 1000 ) % 3600) / 60;
                            int seconds = (int) ((millisUntilFinished / 1000) % 3600) % (60);

                            String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds) + "s";
                            timeRemainingTextView.setText(timeLeftFormatted);

                            if (hours == 0 && minutes <= 1) {
                                timeRemainingTextView.setTextColor(getResources().getColor(R.color.orange_background));
                            }
                            else {
                                timeRemainingTextView.setTextColor(getResources().getColor(R.color.text_dark_grey));
                            }
                        }

                        public void onFinish() {
                            timeRemainingTextView.setText(getString(R.string.timer_ends));
                            auctionClosedImageView.setVisibility(View.VISIBLE);
                            // TODO: Change the bid status
                        }
                    };

                    countDownTimer.start();

                    // Add new timer in Model
                    carListModel.setCountDownTimer(countDownTimer);
                }
                else {
                    timeRemainingTextView.setText(getString(R.string.timer_ends));
                    auctionClosedImageView.setVisibility(View.VISIBLE);
                    // TODO: Change the bid status also
                }
            }
            catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    void setBidValues(TextView topOfferTextView, TextView myBidTextView, TextView bidStatusTextView, int highestBid, int myBid) {
        topOfferTextView.setText(Helper.getIndianCurrencyFormat(highestBid));
        myBidTextView.setText(Helper.getIndianCurrencyFormat(myBid));

        // Either I have not made a bid yet, in which case set it as "Yet To Bid"
        if (myBid == 0) {
            bidStatusTextView.setVisibility(View.GONE);
            myBidTextView.setText(R.string.yet_to_bid);
        }
        // Or I have made a bid but it is less than the highest bid, in which case I am losing
        else if (highestBid > myBid) {
            bidStatusTextView.setVisibility(View.VISIBLE);
            bidStatusTextView.setText(R.string.losing_bid_status);
            bidStatusTextView.setBackgroundResource(R.drawable.background_orange);
        }
        // Or my bid is greater or equat to highest bid, in which case I am winning
        else if (myBid >= highestBid){
            bidStatusTextView.setVisibility(View.VISIBLE);
            bidStatusTextView.setText(R.string.winning_bid_status);
            bidStatusTextView.setBackgroundResource(R.drawable.background_green);
        }
    }

    @Override
    public void onListingRecyclerViewImageClick(CarListModel carListModel) {
        startListingActivity(carListModel);
    }

    void startListingActivity(CarListModel carListModel) {
        EventAnalytics.getInstance(getActivity()).logEvent("Results", "clicked_listing", String.valueOf(carListModel.getAuctionId())+","+fragmentName+","+userId, 0);

        Intent intent = new Intent(getContext(), ListingActivity.class);
        intent.putExtra("LISTING_ID", carListModel.getAuctionId());
        intent.putExtra("CAR_ID", carListModel.getCarId());
        intent.putExtra("SCREEN_SOURCE", fragmentName);
        intent.putStringArrayListExtra("SHOWCASE_IMAGE_URLS", carListModel.getShowcaseImageList());
        intent.putExtra("LISTING_YEAR", carListModel.getManufactutingYear());
        intent.putExtra("LISTING_NAME", carListModel.getVariantName());
        intent.putExtra("LISTING_MILEAGE", carListModel.getMileage());
        intent.putExtra("LISTING_FUEL_TYPE", carListModel.getFuelType());
        intent.putExtra("LISTING_OWNER", carListModel.getOwnerDetail());

        startActivity(intent);
        //getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public void onStop() {
        super.onStop();

        // Cancel Volley Calls
        if (volleyRequestQueue != null)
            volleyRequestQueue.cancelAll(TAG);

        // Remove firebase listeners and CountdownTimer
        clearModelsDataList();
    }
}
package com.truebil.business.Activities;

import android.arch.lifecycle.Lifecycle;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
import android.support.test.espresso.IdlingResource;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.truebil.business.Adapters.ListingFragmentAdapter;
import com.truebil.business.Adapters.ListingImagesViewPagerAdapter;
import com.truebil.business.Constants;
import com.truebil.business.Fragments.InAuctionBidBottomSheetFragment;
import com.truebil.business.Fragments.InspectionReportFragment;
import com.truebil.business.Fragments.LowBalanceBottomSheetFragment;
import com.truebil.business.Fragments.WonBottomSheetFragment;
import com.truebil.business.Helper;
import com.truebil.business.Models.ListingModel;
import com.truebil.business.Network.VolleyService;
import com.truebil.business.R;
import com.truebil.business.Utils.EventAnalytics;
import com.truebil.business.Utils.ServerTime;
import com.truebil.business.Utils.VolleyIdlingResource;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ListingActivity extends AppCompatActivity implements
        InAuctionBidBottomSheetFragment.InAuctionFragmentInterface, InspectionReportFragment.InspectionReportFragmentInterface,
        ListingImagesViewPagerAdapter.ListingImagesViewPagerInterface, LowBalanceBottomSheetFragment.LowBalanceBottomSheetInterface {

    private static final String TAG = "ListingActivity";
    private ImageView auctionClosedImage;
    private ViewPager viewPager, imagePager;
    private ScrollView listingScrollView;
    private Button seePhotosButton;
    private RelativeLayout superRelativeLayout;
    private LinearLayout truescoreLinearLayout;
    private ProgressBar loaderProgressBar;
    private TabLayout tabLayout;
    private TextView suggestedPriceTextView, suggestedPriceTitleTextView, yearVariantNameTextView, ownerDetailTextView, fuelTypeTextView, mileageTextView;
    private RequestQueue volleyRequestQueue;
    private FragmentManager fragmentManager;
    private SharedPreferences sharedPref;
    private String auctionId, screenSource, userId = "";
    private ListingModel listingModel;
    private BottomSheetBehavior bottomSheetBehavior;
    @Nullable public static VolleyIdlingResource mIdlingResource;
    private ImageButton previousImageButton, nextImageButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);

        sharedPref = this.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        fragmentManager = getSupportFragmentManager();

        userId = String.valueOf(sharedPref.getInt(Constants.SharedPref.DEALER_ID, -1));
        auctionId = getIntent().getStringExtra("LISTING_ID");
        screenSource = getIntent().getStringExtra("SCREEN_SOURCE");
        superRelativeLayout = findViewById(R.id.activity_listing_super_relative_layout);
        superRelativeLayout.setVisibility(View.INVISIBLE);
        listingScrollView = findViewById(R.id.activity_listing_scroll_view);
        viewPager = findViewById(R.id.activity_listing_details_view_pager);
        imagePager = findViewById(R.id.activity_listing_showcase_images_view_pager);
        auctionClosedImage = findViewById(R.id.auction_closed_image);
        loaderProgressBar = findViewById(R.id.activity_listing_progress_bar);
        seePhotosButton = findViewById(R.id.activity_listing_see_photos_button);
        ImageButton backImageButton = findViewById(R.id.activity_listing_back_image_button);
        suggestedPriceTextView = findViewById(R.id.activity_listing_suggested_price_text_view);
        suggestedPriceTitleTextView = findViewById(R.id.activity_listing_suggested_price_title_text_view);
        truescoreLinearLayout = findViewById(R.id.activity_listing_truescore_linear_layout);
        previousImageButton = findViewById(R.id.activity_listing_previous_image_button); // Handle left and right image buttons for showcase images (view pager)
        nextImageButton = findViewById(R.id.activity_listing_next_image_button);
        FrameLayout bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        truescoreLinearLayout.setVisibility(View.GONE);
        seePhotosButton.setVisibility(View.GONE);
        suggestedPriceTextView.setVisibility(View.GONE);
        suggestedPriceTitleTextView.setVisibility(View.GONE);
        previousImageButton.setVisibility(View.INVISIBLE);

        tabLayout = findViewById(R.id.activity_listing_tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        getIdlingResource();
        fetchListingData(auctionId);
        loadIntentData();

        seePhotosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventAnalytics.getInstance(ListingActivity.this).logEvent("Dedicated", "clicked_see_photos", String.valueOf(auctionId) + "," + screenSource + "," + userId, 0);
                startGalleryActivity();
            }
        });

        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        previousImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePager.setCurrentItem(imagePager.getCurrentItem() - 1);
            }
        });

        nextImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePager.setCurrentItem(imagePager.getCurrentItem() + 1);
            }
        });

        bottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        imagePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                if (imagePager.getCurrentItem() == 0) {
                    previousImageButton.setVisibility(View.INVISIBLE);
                }
                else if (imagePager.getCurrentItem() == imagePager.getAdapter().getCount()-1) {
                    nextImageButton.setVisibility(View.INVISIBLE);
                }
                else {
                    previousImageButton.setVisibility(View.VISIBLE);
                    nextImageButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }

    void loadIntentData() {
        ArrayList<String> showcaseImageUrls = getIntent().getStringArrayListExtra("SHOWCASE_IMAGE_URLS");
        if (showcaseImageUrls != null) {
            ListingImagesViewPagerAdapter viewPagerAdapter = new ListingImagesViewPagerAdapter(this, showcaseImageUrls);
            imagePager.setAdapter(viewPagerAdapter);
            loaderProgressBar.setVisibility(View.GONE);
            superRelativeLayout.setVisibility(View.VISIBLE);
        }

        String modelName = getIntent().getStringExtra("LISTING_NAME");
        String manufacturingYear = getIntent().getStringExtra("LISTING_YEAR");
        String carId = getIntent().getStringExtra("CAR_ID");

        yearVariantNameTextView = findViewById(R.id.activity_listing_year_make_name_text_view);
        if (modelName != null && manufacturingYear != null && carId != null) {
            yearVariantNameTextView.setText(manufacturingYear + " " + modelName + " (#" + carId + ")");
            setListingIdSpanColor(yearVariantNameTextView);
        }

        mileageTextView = findViewById(R.id.activity_listing_mileage_text_view);
        String mileage = getIntent().getStringExtra("LISTING_MILEAGE");
        if (mileage != null) {
            mileageTextView.setText(mileage + " km");
        }

        fuelTypeTextView = findViewById(R.id.activity_listing_fuel_type_text_view);
        String fuelType = getIntent().getStringExtra("LISTING_FUEL_TYPE");
        if (fuelType != null) {
            fuelTypeTextView.setText(fuelType);
        }

        ownerDetailTextView = findViewById(R.id.activity_listing_owner_detail_text_view);
        String owner = getIntent().getStringExtra("LISTING_OWNER");
        if (owner != null) {
            ownerDetailTextView.setText(owner + " Owner");
        }
    }

    void fetchListingData(final String auctionId) {
        String url = Constants.Config.API_PATH + "/auction_listings/" + auctionId + "/";

        if (volleyRequestQueue == null)
            volleyRequestQueue = Volley.newRequestQueue(getApplicationContext());

        if (mIdlingResource != null) {
            mIdlingResource.setIdleState(false);
        }

        final long apiStartTime = System.nanoTime();
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loaderProgressBar.setVisibility(View.GONE);

                        // Sync Server Time
                        try {
                            String serverTime = response.getString("server_time");
                            long apiResponseDelayMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - apiStartTime);
                            ServerTime.getInstance().initServerDate(serverTime, apiResponseDelayMs / 2);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Parse listing information
                        parseListingInfo(response);

                        if (mIdlingResource != null) {
                            mIdlingResource.setIdleState(true);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loaderProgressBar.setVisibility(View.GONE);

                        JSONObject additionalLogJson = new JSONObject();
                        try {
                            additionalLogJson.put("API", "/auction_listings");
                            additionalLogJson.put("AUCTION_ID", auctionId);
                            VolleyService.handleVolleyError(error, additionalLogJson, true, getApplicationContext());
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (mIdlingResource != null) {
                            mIdlingResource.setIdleState(true);
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

        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                Constants.Config.MAX_TIMEOUT_TIME,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        jsonRequest.setTag(TAG);
        volleyRequestQueue.add(jsonRequest);
    }

    void parseListingInfo(JSONObject response) {
        listingModel = new ListingModel(response);

        //Set Visibility of all views
        superRelativeLayout.setVisibility(View.VISIBLE);
        seePhotosButton.setVisibility(View.VISIBLE);
        suggestedPriceTextView.setVisibility(View.VISIBLE);
        suggestedPriceTitleTextView.setVisibility(View.VISIBLE);
        truescoreLinearLayout.setVisibility(View.VISIBLE);

        // Populate views
        yearVariantNameTextView.setText(listingModel.getManufacturingYear() + " " + listingModel.getVariantName() + " (#" + listingModel.getListingId() + ")");
        ownerDetailTextView.setText(listingModel.getOwner() + " Owner") ;
        fuelTypeTextView.setText(listingModel.getFuelType());
        mileageTextView.setText(listingModel.getMileage() + " km");
        suggestedPriceTextView.setText(listingModel.getSuggestedPrice());
        TextView truescoreTextView = findViewById(R.id.activity_listing_truescore_text_view);
        truescoreTextView.setText(listingModel.getTruebilScore());

        // Set color to listing id
        setListingIdSpanColor(yearVariantNameTextView);

        // Either display suggested price or My Bid
        if (!listingModel.getAuctionStatus().equals("active")) {
            suggestedPriceTitleTextView.setText("My Bid: ");
            suggestedPriceTextView.setText(Helper.getIndianCurrencyFormat(listingModel.getMyDealerBid()));
        }

        // Listing Overview, Inspection Report Fragment Viewpager
        ListingFragmentAdapter listingFragmentAdapter = new ListingFragmentAdapter(getSupportFragmentManager(),
                listingModel.getOverview(),
                listingModel.getInspectionSummary(),
                listingModel.getFeatureJSONArray(),
                listingModel.getCargoNegativeComments(),
                listingModel.getInspectionReport(),
                listingModel.getInstaveritasVerificationDetails());
        viewPager.setAdapter(listingFragmentAdapter);
        Helper.wrapTabIndicatorToTitle(tabLayout, Constants.Config.TAB_LAYOUT_EXTERNAL_MARGIN, Constants.Config.TAB_LAYOUT_EXTERNAL_MARGIN);

        // Listing showcase images Viewpager
        ListingImagesViewPagerAdapter viewPagerAdapter = new ListingImagesViewPagerAdapter(this, listingModel.getShowcaseImageURLList());
        imagePager.setAdapter(viewPagerAdapter);

        handleBottomSheetCases();
    }

    public void handleBottomSheetCases() {

        String dealerAuctionStatus = listingModel.getDealerAuctionStatus();
        String auctionStatus = listingModel.getAuctionStatus();

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) listingScrollView.getLayoutParams();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Active Auction
        if (auctionStatus.equals("active")) {
            Fragment fragment = new InAuctionBidBottomSheetFragment();
            Bundle bundle = new Bundle();
            bundle.putString("LISTING_JSON", String.valueOf(listingModel.getListingApiResponse()));
            bundle.putString("SCREEN_SOURCE", screenSource);
            fragment.setArguments(bundle);
            transaction.replace(R.id.bottom_sheet, fragment);

            setBottomSheetMargin();
            //layoutParams.setMargins(0, 0, 0, Helper.convertDpToPixel(115,this));
        }

        // Auction Won and Closed
        else if (dealerAuctionStatus.equalsIgnoreCase("won")) {
            WonBottomSheetFragment fragment = new WonBottomSheetFragment();
            Bundle bundle = new Bundle();
            bundle.putString("JSON", String.valueOf(listingModel.getListingApiResponse()));
            fragment.setArguments(bundle);
            transaction.replace(R.id.bottom_sheet, fragment);

            // Dealer has not made a payment yet
            if (auctionStatus.equals("payment_waiting")) {
                bottomSheetBehavior.setPeekHeight(Helper.convertDpToPixel(120, this));
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                bottomSheetBehavior.setHideable(false);
            }

            layoutParams.setMargins(0, 0, 0, Helper.convertDpToPixel(120,this));
            auctionClosedImage.setVisibility(View.VISIBLE);
        }

        // Lost
        else {
            layoutParams.setMargins(0, 0, 0, 0);
            auctionClosedImage.setVisibility(View.VISIBLE);
        }

        listingScrollView.setLayoutParams(layoutParams);
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onTimerEnd() {
        /*
         * Remove all fragments in the bottom sheet
         * Only call the popBackStack() method when the activity is running
         * Use lifecycle components to check the current state of the activity
         */
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
                fragmentManager.popBackStack();
            }
        }

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) listingScrollView.getLayoutParams();
        layoutParams.setMargins(0, 0, 0, 0);
        listingScrollView.setLayoutParams(layoutParams);

        // Reload activity
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchListingData(auctionId);
            }
        }, Constants.Config.LISTING_ACTIVITY_RELOAD_DELAY);
    }

    @Override
    public void onLowBalanceBid() {
        Fragment fragment = new LowBalanceBottomSheetFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.bottom_sheet, fragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setPeekHeight(Helper.convertDpToPixel(240, this));
        bottomSheetBehavior.setHideable(false);
    }

    @Override
    public void setAuctionStatus(String status) {
        if (status.equals("active"))
            auctionClosedImage.setVisibility(View.GONE);

        if (status.equals("closed"))
            auctionClosedImage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLowBalanceBottomSheetClosed() {
        if (fragmentManager.getBackStackEntryCount() != 0)
            fragmentManager.popBackStack();
    }

    @Override
    public void onInspectionErrorImageClick() {
        startGalleryActivity();
    }

    @Override
    public void onListingViewPagerImageClicked() {
        startGalleryActivity();
    }

    void startGalleryActivity() {

        if (listingModel == null)
            return;

        Intent intent = new Intent(getApplicationContext(), GalleryActivity.class);
        intent.putExtra("LISTING_ID", listingModel.getListingId());
        intent.putExtra("LISTING_YEAR", listingModel.getManufacturingYear());
        intent.putExtra("LISTING_NAME", listingModel.getVariantName());
        intent.putExtra("LISTING_MILEAGE", listingModel.getMileage());
        intent.putExtra("LISTING_CITY_NAME", listingModel.getCityName());
        intent.putExtra("LISTING_FUEL_TYPE", listingModel.getFuelType());
        intent.putExtra("LISTING_OWNER", listingModel.getOwner());
        intent.putExtra("LISTING_RTO_INFO", listingModel.getRtoName());
        startActivity(intent);
    }

    void setListingIdSpanColor(TextView listingTextView) {

        String listingText = listingTextView.getText().toString();
        Spannable listingSpan = new SpannableString(listingTextView.getText());

        int startPos = listingText.indexOf("(#");
        int endPos = listingText.indexOf(")");

        if (endPos < startPos)
            return;

        listingSpan.setSpan(new ForegroundColorSpan(Color.GRAY), startPos, endPos + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //listingSpan.setSpan(new RelativeSizeSpan(0.8f), startPos, endPos + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        listingTextView.setText(listingSpan);
    }

    void setBottomSheetMargin() {
        final FrameLayout layout = findViewById(R.id.bottom_sheet);
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) listingScrollView.getLayoutParams();

        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int width = layout.getMeasuredWidth();
                int height = layout.getMeasuredHeight();

                if (width > 0 && height > 0)
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                Log.d(TAG, "Height " + height + " Width " + width);
                layoutParams.setMargins(0, 0, 0, height - 20);
                listingScrollView.setLayoutParams(layoutParams);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            Intent intent = new Intent(getApplicationContext(), BiddingActivity.class);
            startActivity(intent);
        }
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (volleyRequestQueue != null)
            volleyRequestQueue.cancelAll(TAG);
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
